/**
 * Copyright 2015 DiffPlug
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

import java.util.Collection;
import java.util.Iterator;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;

import com.diffplug.common.base.Unhandled;

/** 
 * An extension of RxValue<ImmutableSet<T>>, with
 * convenience methods for modifying and observing the set,
 * as well as for converting it into an RxValue<Optional<T>>.
 */
public class RxSet<T> extends RxValue<ImmutableSet<T>> implements Set<T> {
	/** Creates an RxSet with an initially empty value. */
	public static <T> RxSet<T> ofEmpty() {
		return of(ImmutableSet.of());
	}

	/** Creates an RxSet with the given initial value. */
	public static <T> RxSet<T> of(Set<T> initial) {
		return new RxSet<T>(ImmutableSet.copyOf(initial));
	}

	/** Initally holds the given collection. */
	protected RxSet(ImmutableSet<T> initial) {
		super(initial);
	}

	/** Removes the given value from the set. */
	public void set(Set<T> value) {
		super.set(ImmutableSet.copyOf(value));
	}

	/** Returns a mirror of this Set as an RxOptional. */
	public RxOptional<T> asOptional(Function<ImmutableSet<T>, T> onMultiple) {
		RxOptional<T> optional = new RxOptional<T>(optionalFromSet(get(), onMultiple)) {
			@Override
			public void set(Optional<T> t) {
				if (t.isPresent()) {
					RxSet.this.set(ImmutableSet.of(t.get()));
				} else {
					RxSet.this.set(ImmutableSet.of());
				}
			}

			@Override
			public Optional<T> get() {
				return optionalFromSet(RxSet.this.get(), onMultiple);
			}
		};

		asObservable().subscribe(val -> {
			optional.set(optionalFromSet(val, onMultiple));
		});

		return optional;
	}

	/** Convenience method for turning an optional into a set. */
	private static <T> Optional<T> optionalFromSet(ImmutableSet<T> set, Function<ImmutableSet<T>, T> mode) {
		if (set.size() == 0) {
			return Optional.empty();
		} else if (set.size() == 1) {
			return Optional.of(Iterables.getOnlyElement(set));
		} else if (set.size() > 1) {
			return Optional.of(mode.apply(set));
		} else {
			throw Unhandled.integerException(set.size());
		}
	}

	/** Functions for dealing with the impedance mismatch between set and optionals. */
	public static class OnMultiple {
		private OnMultiple() {}

		/** Throws an exception when ImmutableSet is multiple. */
		public static <T> Function<ImmutableSet<T>, T> error() {
			return val -> {
				throw new IllegalArgumentException();
			};
		}

		/** Throws an exception when ImmutableSet is multiple. */
		public static <T> Function<ImmutableSet<T>, T> takeFirst() {
			return val -> val.asList().get(0);
		}
	}

	////////////////////////
	// Set implementation //
	////////////////////////
	@Override
	public boolean add(T element) {
		ImmutableSet<T> value = super.get();
		if (value.contains(element)) {
			return false;
		} else {
			super.set(ImmutableUtil.add(value, element));
			return true;
		}
	}

	@Override
	public boolean addAll(Collection<? extends T> collection) {
		ImmutableSet<T> value = super.get();
		ImmutableSet<T> newValue = ImmutableUtil.addAll(value, collection);
		if (value.size() == newValue.size()) {
			return false;
		} else {
			super.set(newValue);
			return true;
		}
	}

	@Override
	public void clear() {
		super.set(ImmutableSet.of());
	}

	@Override
	public boolean contains(Object object) {
		return super.get().contains(object);
	}

	@Override
	public boolean containsAll(Collection<?> collection) {
		return super.get().containsAll(collection);
	}

	@Override
	public boolean isEmpty() {
		return get().isEmpty();
	}

	@Override
	public Iterator<T> iterator() {
		return get().iterator();
	}

	@Override
	public boolean remove(Object object) {
		ImmutableSet<T> value = super.get();
		if (value.contains(object)) {
			super.set(ImmutableUtil.remove(value, object));
			return true;
		} else {
			return false;
		}
	}

	@Override
	public boolean removeAll(Collection<?> collection) {
		ImmutableSet<T> value = super.get();
		ImmutableSet<T> newValue = ImmutableUtil.removeAll(value, collection);
		if (value.size() == newValue.size()) {
			return false;
		} else {
			super.set(newValue);
			return true;
		}
	}

	@Override
	public boolean retainAll(Collection<?> collection) {
		ImmutableSet<T> value = super.get();
		ImmutableSet<T> newValue = ImmutableUtil.retainAll(value, collection);
		if (value.size() == newValue.size()) {
			return false;
		} else {
			super.set(newValue);
			return true;
		}
	}

	@Override
	public int size() {
		return get().size();
	}

	@Override
	public Object[] toArray() {
		return get().toArray();
	}

	@Override
	public <R> R[] toArray(R[] array) {
		return get().toArray(array);
	}
}
