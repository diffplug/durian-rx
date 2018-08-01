# DurianRx releases

### Version 3.1.0-SNAPSHOT - TBD ([javadoc](http://diffplug.github.io/durian-rx/javadoc/snapshot/) [snapshot](https://oss.sonatype.org/content/repositories/snapshots/com/diffplug/durian/durian-rx/))

### Version 3.0.0 - August 1st 2018 ([javadoc](http://diffplug.github.io/durian-rx/javadoc/3.0.0/), [jcenter](https://bintray.com/diffplug/opensource/durian-rx/3.0.0/view))

* `DisposableEar`'s final name is `Chit`.
* Added `Rx.sync(RxBox<T> a, RxBox<T> b)`.
* Added `MultiSelectModel` for a UI-independent multi-selection model.

### Version 3.0.0.BETA2 - March 8th 2017 ([javadoc](http://diffplug.github.io/durian-rx/javadoc/3.0.0.BETA2/), [jcenter](https://bintray.com/diffplug/opensource/durian-rx/3.0.0.BETA2/view))

* Got rid of the `RxListener.IsLogging` marker interface.
* Made `RxListener.isLogging()` public, and added `RxListener.onErrorDontLog(Throwable)`.
	+ Combined, these methods make it possible for an external framework to detect and hijack logging for a specific listener.
	+ Used by the Agent framework in DiffPlug 2+
* Added `CasBox.getAndSet()`.
* `DispoableEar.Settable` now allows `dispose()` to be called multiple times, to comply with the `Disposable` contract.
* An `RxJavaCompat` layer for turning `Single` and `Maybe` into `CompletionStage`.

### Version 3.0.0.BETA - February 7th 2017 ([javadoc](http://diffplug.github.io/durian-rx/javadoc/3.0.0.BETA/), [jcenter](https://bintray.com/diffplug/opensource/durian-rx/3.0.0.BETA/view))

* Added `DisposableEar` and `GuardedExecutor`.
* Fixed a bug in `ForwardingBox.modify()`.
* `RxExecutor` now exposes the underlying `Executor`, `Scheduler`, and `RxTracingPolicy`.

### Version 3.0.0.ALPHA - November 11th 2016 ([javadoc](http://diffplug.github.io/durian-rx/javadoc/3.0.0.ALPHA/), [jcenter](https://bintray.com/diffplug/opensource/durian-rx/3.0.0.ALPHA/view))

* Bumped RxJava to 2.0, and incorporated `RxTracingPolicy` into `RxJavaPlugins`.
* Fixed a bug in `ForwardingBox.modify()`.
* `RxExecutor` now exposes the underlying `Executor`, `Scheduler`, and `RxTracingPolicy`.

### Version 2.0.0 - July 13th 2016 ([javadoc](http://diffplug.github.io/durian-rx/javadoc/2.0.0/), [jcenter](https://bintray.com/diffplug/opensource/durian-rx/2.0.0/view))

* `Immutables` has moved to `com.diffplug.durian:durian-collect`.
* Removed collections-specific classes.
	+ `RxOptional` -> `RxBox<Optional>`
	+ `RxSet` -> `RxBox<ImmutableSet>`
	+ This makes it possible to mix-and-match RxBox implementations and collection implementations.
* `Box` and `RxBox` had poorly defined behavior around race conditions.  It is now implemented by the following well-defined classes:
	+ `RxBox.of(initialValue)` makes no atomicity guarantees.
	+ `CasBox` supports compare-and-swap atomic modifications.
	+ `LockBox` supports mutex-based atomic modifications.
	+ `RxLockBox` supports mutex-based atomic modification with RxJava-based notifications.
* Broke the overly crowded `Rx` class into serveral pieces:
	+ `Rx` is now only a collection of utility methods.
	+ `RxListener` is now the listener interface for `Observer<T> & FutureCallback<T>`.
	+ `Rx.RxExecutor` is now `RxExecutor`, and `Rx.HasRxExecutor` is `RxExecutor.Has`.
	+ `RxGetter` no longer enforces `distinctUntilChanged`.
* Adopted Durian and its new `ConverterNonNull`.
* Added `OrderedLock`, which takes multiple locks in a guaranteed lock-free way.
* Added `Breaker`, for temporarily breaking a connection between observable values.

### Version 1.3.0 - February 9th 2016 ([javadoc](http://diffplug.github.io/durian-rx/javadoc/1.3.0/), [jcenter](https://bintray.com/diffplug/opensource/durian-rx/1.3.0/view))

* Ditched Guava for DurianGuava.

### Version 1.2.0 - November 18th 2015 ([javadoc](http://diffplug.github.io/durian-rx/javadoc/1.2.0/), [jcenter](https://bintray.com/diffplug/opensource/durian-rx/1.2.0/view))

* Added support for `CompletionStage` (and therefore `CompletableFuture`), with the same behavior as `ListenableFuture`.

### Version 1.1.0 - October 19th 2015 ([javadoc](http://diffplug.github.io/durian-rx/javadoc/1.1.0/), [jcenter](https://bintray.com/diffplug/opensource/durian-rx/1.1.0/view))

* Changed OSGi metadata Bundle-SymbolicName to `com.diffplug.durian.rx`.
* OSGi metadata is now auto-generated using bnd.

### Version 1.0.1 - July 27th 2015 ([javadoc](http://diffplug.github.io/durian-rx/javadoc/1.0.1/), [jcenter](https://bintray.com/diffplug/opensource/durian-rx/1.0.1/view))

* Gah! MANIFEST.MF still had -SNAPSHOT version.  Fixed now.  Would be really nice if we could get MANIFEST.MF generation working.

### Version 1.0 - May 13th 2015 ([javadoc](http://diffplug.github.io/durian-rx/javadoc/1.0/), [jcenter](https://bintray.com/diffplug/opensource/durian-rx/1.0/view))

* First stable release.
