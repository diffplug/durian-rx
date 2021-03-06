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
package com.diffplug.common.rx;

import static java.util.Objects.requireNonNull;

import com.diffplug.common.base.Errors;
import com.diffplug.common.util.concurrent.FutureCallback;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import java.util.Optional;
import java.util.function.Consumer;
import javax.annotation.Nullable;

public final class RxListener<T> implements Observer<T>, FutureCallback<T> {
	private final Consumer<T> onValue;
	private final Consumer<Optional<Throwable>> onTerminate;

	RxListener(Consumer<T> onValue, Consumer<Optional<Throwable>> onTerminate) {
		this.onValue = requireNonNull(onValue);
		this.onTerminate = requireNonNull(onTerminate);
	}

	//////////////
	// Observer //
	//////////////
	@Override
	public void onSubscribe(Disposable d) {
		requireNonNull(d);
	}

	@Override
	public final void onNext(@Nullable T t) {
		onValue.accept(t);
	}

	@Override
	public void onComplete() {
		onTerminate.accept(Optional.empty());
	}

	@Override
	public final void onError(Throwable e) {
		onTerminate.accept(Optional.of(e));
	}

	/////////////
	// Futures //
	/////////////
	@Override
	public final void onSuccess(@Nullable T result) {
		onValue.accept(result);
		onTerminate.accept(Optional.empty());
	}

	@Override
	public final void onFailure(Throwable e) {
		onTerminate.accept(Optional.of(e));
	}

	public void onErrorDontLog(Throwable e) {
		if (onTerminate == logErrors) {
			return;
		} else {
			Optional<Throwable> optError = Optional.of(e);
			if (onTerminate instanceof DefaultTerminate) {
				((DefaultTerminate) onTerminate).onTerminate.accept(optError);
			} else {
				onTerminate.accept(optError);
			}
		}
	}

	/** Returns true iff the given Rx is a logging Rx. */
	public boolean isLogging() {
		return onTerminate == logErrors || onTerminate instanceof DefaultTerminate;
	}

	static final Consumer<Optional<Throwable>> logErrors = error -> {
		if (error.isPresent()) {
			Errors.log().accept(error.get());
		}
	};

	/** An error listener which promises to pass log all errors, without requiring the user to. */
	static class DefaultTerminate implements Consumer<Optional<Throwable>> {
		private final Consumer<Optional<Throwable>> onTerminate;

		DefaultTerminate(Consumer<Optional<Throwable>> onTerminate) {
			this.onTerminate = requireNonNull(onTerminate);
		}

		@Override
		public void accept(Optional<Throwable> t) {
			onTerminate.accept(t);
			if (t.isPresent()) {
				RxListener.logErrors.accept(t);
			}
		}
	}
}
