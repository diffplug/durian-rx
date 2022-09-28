/*
 * Copyright 2020 DiffPlug
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.diffplug.common.rx

import com.diffplug.common.base.Box
import com.diffplug.common.base.Converter
import com.diffplug.common.base.Either
import com.diffplug.common.collect.ImmutableSet
import com.diffplug.common.collect.Immutables
import java.util.*
import kotlinx.coroutines.flow.MutableSharedFlow

/** Manages a selection based on a a MouseOver / Selection combination. */
class MultiSelectModel<T>(
		val mouseOver: RxBox<Optional<T>> = RxBox.of(Optional.empty()),
		val selection: RxBox<ImmutableSet<T>> = RxBox.of(ImmutableSet.of()),
		val clicked: MutableSharedFlow<T> = Rx.createEmitFlow()
) {
	var isCtrl = false

	/** Mouseover and selection in this model will trump whatever is in the other. */
	fun <U> trump(other: MultiSelectModel<U>): Trumped<T, U> {
		// make the selections impose exclusivity on themselves
		selectionExclusive(other)
		other.selectionExclusive(this)

		// maintain the combined selection
		val lastSelection =
				Box.ofVolatile(Either.createLeft<ImmutableSet<T>, ImmutableSet<U>>(ImmutableSet.of()))
		val getterSelection =
				RxGetter.combineLatest(selection, other.selection) {
						left: ImmutableSet<T>,
						right: ImmutableSet<U> ->
					if (!left.isEmpty() && !right.isEmpty()) {
						// if both are present, we'll keep what we've got, while the two work it out amongst
						// themselves
						lastSelection.get()
					} else {
						val newValue =
								if (left.isEmpty()) Either.createRight(right)
								else Either.createLeft<ImmutableSet<T>, ImmutableSet<U>>(left)
						lastSelection.set(newValue)
						newValue
					}
				}
		// when someone sets the combined selection, carry that over to the constituent selections
		val valueSelection =
				RxBox.from(getterSelection) { either: Either<ImmutableSet<T>, ImmutableSet<U>> ->
					either.acceptBoth(selection, other.selection, ImmutableSet.of(), ImmutableSet.of())
				}

		// make the mouseOvers impose exclusivity on themselves
		mouseOverTrumps(other)

		// and maintain a combined mouseOver
		val lastMouseOver =
				Box.ofVolatile(Either.createLeft<Optional<T>, Optional<U>>(Optional.empty()))
		val getterMouseOver =
				RxGetter.combineLatest(mouseOver, other.mouseOver) { left: Optional<T>, right: Optional<U>
					->
					if (left.isPresent && right.isPresent) {
						// if both are present, we'll keep what we've got, while the two work it out amongst
						// themselves
						lastMouseOver.get()
					} else {
						val newValue =
								if (left.isPresent) Either.createLeft(left)
								else Either.createRight<Optional<T>, Optional<U>>(right)
						lastMouseOver.set(newValue)
						newValue
					}
				}
		val valueMouseOver =
				RxBox.from(getterMouseOver) { either: Either<Optional<T>, Optional<U>> ->
					either.acceptBoth(mouseOver, other.mouseOver, Optional.empty(), Optional.empty())
				}
		return Trumped(valueMouseOver, valueSelection)
	}

	/** A MultiSelectModel-ish which represents two trumped selections. */
	class Trumped<T, U>(
			val mouseOver: RxBox<Either<Optional<T>, Optional<U>>>,
			val selection: RxBox<Either<ImmutableSet<T>, ImmutableSet<U>>>
	) {
		fun merge(isReallySecondary: (T) -> U?, wrap: (U) -> T): MultiSelectModel<T> {
			fun toEither(t: T): Either<T, U> =
					isReallySecondary(t)?.let { Either.createRight(it) } ?: Either.createLeft(t)
			val convOpt =
					Converter.from<Either<Optional<T>, Optional<U>>, Optional<T>>(
							{ either -> either.fold({ t -> t }, { opt -> opt.map(wrap) }) },
							{ optT ->
								if (optT.isPresent) {
									val either = toEither(optT.get())
									either.mapLeft { Optional.of(it) }.mapRight { Optional.of(it) }
								} else {
									Either.createLeft(Optional.empty())
								}
							})

			val convSet =
					Converter.from<Either<ImmutableSet<T>, ImmutableSet<U>>, ImmutableSet<T>>(
							{ either ->
								either.fold({ t -> t }, { set -> Immutables.perElementMutateSet(set, wrap) })
							},
							{ setT ->
								val builder = ImmutableSet.builder<U>(setT.size)
								setT.forEach { isReallySecondary(it)?.let { builder.add(it) } }
								val u = builder.build()
								if (u.isEmpty()) {
									Either.createLeft(setT)
								} else {
									Either.createRight(u)
								}
							})
			return MultiSelectModel(mouseOver.map(convOpt), selection.map(convSet))
		}
	}

	/** Separate from selectionExclusive to avoid infinite loop. */
	private fun <V> selectionExclusive(other: MultiSelectModel<V>) {
		Rx.subscribe(selection) { newSelection: ImmutableSet<T> ->
			// our selection has changed
			if (newSelection.isEmpty() || other.selection.get().isEmpty()) {
				return@subscribe
			}
			// and both us and our mutually-exclusive friend are non-empty
			// which means we've gotta empty somebody
			if (isCtrl) {
				// if we're in the middle of trying to set the selection using ctrl,
				// then we'll let our friend keep their selection and we'll sacrifice our own
				selection.set(ImmutableSet.of())
			} else {
				// otherwise, we'll sabotage our friend
				other.selection.set(ImmutableSet.of())
			}
		}
	}

	/**
	 * Enforces that non-empty mouseOver on this MultiSelectManager will force mouseOver on the other
	 * MultiSelectManager to be empty.
	 */
	private fun <V> mouseOverTrumps(multiSelect: MultiSelectModel<V>) {
		class EmptyEnforcer(private val single: RxBox<Optional<V>>) {
			private var enabled = false

			init {
				Rx.subscribe(single) { next: Optional<V> ->
					if (enabled && next.isPresent) {
						single.set(Optional.empty())
					}
				}
			}

			fun setEnabled(enabled: Boolean) {
				this.enabled = enabled
				if (enabled) {
					single.set(Optional.empty())
				}
			}
		}

		val emptyEnforcer = EmptyEnforcer(multiSelect.mouseOver)
		Rx.subscribe(mouseOver) { next: Optional<T> -> emptyEnforcer.setEnabled(next.isPresent) }
	}

	companion object {
		/** Creates an Optional<Either> from an Either<Optional>. </Optional></Either> */
		fun <T, U> optEitherFrom(either: Either<Optional<T>, Optional<U>>): Optional<Either<T, U>> {
			return either.fold({ leftOpt: Optional<T> ->
				leftOpt.map { l: T -> Either.createLeft(l) }
			}) { rightOpt: Optional<U> -> rightOpt.map { r: U -> Either.createRight(r) } }
		}
	}
}
