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

import org.junit.Assert;
import org.junit.Test;

import com.diffplug.common.base.Box;

public class DisposableEarTest {
	@Test
	public void alreadyDisposed() {
		assertDisposedBehavior(DisposableEar.alreadyDisposed());
	}

	@Test
	public void settable() {
		DisposableEar.Settable ear = DisposableEar.settable();
		Assert.assertFalse(ear.isDisposed());

		Box<Boolean> hasBeenDisposed = Box.of(false);
		ear.runWhenDisposed(() -> hasBeenDisposed.set(true));

		Assert.assertFalse(hasBeenDisposed.get());
		ear.dispose();
		Assert.assertTrue(hasBeenDisposed.get());
		Assert.assertTrue(ear.isDisposed());

		assertDisposedBehavior(ear);
	}

	private void assertDisposedBehavior(DisposableEar ear) {
		Assert.assertTrue(ear.isDisposed());
		Box<Boolean> hasBeenDisposed = Box.of(false);
		ear.runWhenDisposed(() -> hasBeenDisposed.set(true));
		Assert.assertTrue(hasBeenDisposed.get());
	}
}
