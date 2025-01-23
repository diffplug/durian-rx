/*
 * Copyright (C) 2020-2025 DiffPlug
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

import com.diffplug.common.base.Consumers;
import com.diffplug.common.testing.AbstractPackageSanityTests;
import java.util.Collections;

public class PackageSanityTests extends AbstractPackageSanityTests {
	public PackageSanityTests() {
		publicApiOnly();
		ignoreClasses(Collections.singleton(RxExample.class)::contains);
		setDefault(RxListener.class, Rx.onValue(Consumers.doNothing()));
		setDefault(RxExecutor.class, Rx.sameThreadExecutor());
	}
}
