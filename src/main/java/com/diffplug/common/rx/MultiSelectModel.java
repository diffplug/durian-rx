/*
 * Copyright 2018 DiffPlug
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.diffplug.common.rx;

import java.util.Objects;
import java.util.Optional;

import com.diffplug.common.base.Box;
import com.diffplug.common.base.Either;
import com.diffplug.common.collect.ImmutableSet;
import io.reactivex.subjects.PublishSubject;

/**
 * Manages a selection based on a a MouseOver / Selection combination.
 */
public class MultiSelectModel<T> {
	protected final RxBox<Optional<T>> mouseOver;
	protected final RxBox<ImmutableSet<T>> selection;
	protected final PublishSubject<T> clicked;
	protected boolean isCtrl;

	/** Creates a MultiSelectModel from the given SelectionModels. */
	public MultiSelectModel(RxBox<Optional<T>> mouseOver, RxBox<ImmutableSet<T>> selection, PublishSubject<T> clicked) {
		this.mouseOver = Objects.requireNonNull(mouseOver);
		this.selection = Objects.requireNonNull(selection);
		this.clicked = Objects.requireNonNull(clicked);
	}

	/** Creates an MultiSelectModel from scratch. */
	public MultiSelectModel() {
		this(RxBox.of(Optional.empty()), RxBox.of(ImmutableSet.of()), PublishSubject.create());
	}

	/** Returns the mouseOver selection. */
	public RxBox<Optional<T>> mouseOver() {
		return mouseOver;
	}

	/** Returns the current selection. */
	public RxBox<ImmutableSet<T>> selection() {
		return selection;
	}

	/** A publish subject which responds to clicks. */
	public PublishSubject<T> clicked() {
		return clicked;
	}

	public boolean isCtrl() {
		return isCtrl;
	}

	public void isCtrl(boolean isCtrl) {
		this.isCtrl = isCtrl;
	}

	/** Mouseover and selection in this model will trump whatever is in the other. */
	public <U> Trumped<T, U> trump(MultiSelectModel<U> other) {
		// make the selections impose exclusivity on themselves
		this.selectionExclusive(other);
		other.selectionExclusive(this);

		// maintain the combined selection
		Box<Either<ImmutableSet<T>, ImmutableSet<U>>> lastSelection = Box.ofVolatile(Either.createLeft(ImmutableSet.of()));
		RxGetter<Either<ImmutableSet<T>, ImmutableSet<U>>> getterSelection = RxGetter.combineLatest(selection, other.selection,
				(ImmutableSet<T> left, ImmutableSet<U> right) -> {
					if (!left.isEmpty() && !right.isEmpty()) {
						// if both are present, we'll keep what we've got, while the two work it out amongst themselves
						return lastSelection.get();
					} else {
						Either<ImmutableSet<T>, ImmutableSet<U>> newValue = left.isEmpty() ? Either.createRight(right) : Either.createLeft(left);
						lastSelection.set(newValue);
						return newValue;
					}
				});
		// when someone sets the combined selection, carry that over to the constituent selections
		RxBox<Either<ImmutableSet<T>, ImmutableSet<U>>> valueSelection = RxBox.from(getterSelection, either -> {
			either.acceptBoth(this.selection, other.selection, ImmutableSet.of(), ImmutableSet.of());
		});

		// make the mouseOvers impose exclusivity on themselves
		this.mouseOverTrumps(other);

		// and maintain a combined mouseOver
		Box<Either<Optional<T>, Optional<U>>> lastMouseOver = Box.ofVolatile(Either.createLeft(Optional.empty()));
		RxGetter<Either<Optional<T>, Optional<U>>> getterMouseOver = RxGetter.combineLatest(mouseOver, other.mouseOver,
				(Optional<T> left, Optional<U> right) -> {
					if (left.isPresent() && right.isPresent()) {
						// if both are present, we'll keep what we've got, while the two work it out amongst themselves
						return lastMouseOver.get();
					} else {
						Either<Optional<T>, Optional<U>> newValue = left.isPresent() ? Either.createLeft(left) : Either.createRight(right);
						lastMouseOver.set(newValue);
						return newValue;
					}
				});

		RxBox<Either<Optional<T>, Optional<U>>> valueMouseOver = RxBox.from(getterMouseOver, either -> {
			either.acceptBoth(this.mouseOver, other.mouseOver, Optional.empty(), Optional.empty());
		});

		return new Trumped<T, U>(valueMouseOver, valueSelection);
	}

	/** A MultiSelectModel-ish which represents two trumped selections. */
	public static class Trumped<T, U> {
		private final RxBox<Either<Optional<T>, Optional<U>>> mouseOver;
		private final RxBox<Either<ImmutableSet<T>, ImmutableSet<U>>> selection;

		public Trumped(RxBox<Either<Optional<T>, Optional<U>>> mouseOver, RxBox<Either<ImmutableSet<T>, ImmutableSet<U>>> selection) {
			this.mouseOver = mouseOver;
			this.selection = selection;
		}

		public RxBox<Either<Optional<T>, Optional<U>>> getMouseOver() {
			return mouseOver;
		}

		public RxBox<Either<ImmutableSet<T>, ImmutableSet<U>>> getSelection() {
			return selection;
		}
	}

	/** Creates an Optional<Either> from an Either<Optional>. */
	public static <T, U> Optional<Either<T, U>> optEitherFrom(Either<Optional<T>, Optional<U>> either) {
		return either.fold(leftOpt -> leftOpt.map(Either::createLeft), rightOpt -> rightOpt.map(Either::createRight));
	}

	/** Separate from selectionExclusive to avoid infinite loop. */
	private <V> void selectionExclusive(MultiSelectModel<V> other) {
		Rx.subscribe(selection, newSelection -> {
			// our selection has changed
			if (newSelection.isEmpty() || other.selection().get().isEmpty()) {
				return;
			}
			// and both us and our mutually-exclusive friend are non-empty
			// which means we've gotta empty somebody
			if (isCtrl) {
				// if we're in the middle of trying to set the selection using ctrl,
				// then we'll let our friend keep their selection and we'll sacrifice our own
				selection.set(ImmutableSet.<T> of());
			} else {
				// otherwise, we'll sabotage our friend
				other.selection().set(ImmutableSet.<V> of());
			}
		});
	}

	/** Enforces that non-empty mouseOver on this MultiSelectManager will force mouseOver on the other MultiSelectManager to be empty. */
	private <V> void mouseOverTrumps(MultiSelectModel<V> multiSelect) {
		class EmptyEnforcer {
			private final RxBox<Optional<V>> single;
			private boolean enabled = false;

			public EmptyEnforcer(RxBox<Optional<V>> single) {
				this.single = single;
				Rx.subscribe(single, next -> {
					if (enabled && next.isPresent()) {
						single.set(Optional.empty());
					}
				});
			}

			public void setEnabled(boolean enabled) {
				this.enabled = enabled;
				if (enabled) {
					single.set(Optional.empty());
				}
			}
		}

		EmptyEnforcer emptyEnforcer = new EmptyEnforcer(multiSelect.mouseOver());
		Rx.subscribe(mouseOver, next -> emptyEnforcer.setEnabled(next.isPresent()));
	}
}
