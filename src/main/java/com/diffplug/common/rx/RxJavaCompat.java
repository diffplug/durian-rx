/*
 * Copyright 2016 DiffPlug
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

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import io.reactivex.Maybe;
import io.reactivex.Single;

public class RxJavaCompat {
	public static <T> CompletionStage<T> toFuture(Single<T> single) {
		CompletableFuture<T> future = new CompletableFuture<>();
		single.subscribe(future::complete, future::completeExceptionally);
		return future;
	}

	public static <T> CompletionStage<Optional<T>> toFuture(Maybe<T> single) {
		return toFuture(single.map(Optional::of).toSingle(Optional.empty()));
	}
}
