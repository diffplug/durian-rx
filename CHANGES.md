# DurianRx releases

## [Unreleased]
### Changed
- **BREAKING** Replace `RxJava Disposable` with `Kotlin Job`, and remove `rxjava` completely. ([#10](https://github.com/diffplug/durian-rx/pull/10))
- Add strict nullability to RxBox and improve efficiency. ([#12](https://github.com/diffplug/durian-rx/pull/12))
- Bump required java from 11 to 17. ([#9](https://github.com/diffplug/durian-rx/pull/9))

## [4.0.1] - 2022-12-20
### Fixed
- Generics on `MultiSelectModel` have changed from `T` to `T : Any` to play nicely with Kotlin's new stricter generic nullability bounds. ([#8](https://github.com/diffplug/durian-rx/pull/8))

## [4.0.0] - 2022-09-29
### Added
* Add `merge` function to `MultiSelectModel.Trumped`
* `RxExecutor` now includes a `dispatcher: CoroutineDispatcher` field
### Changed (important)
* **BREAKING** `RxBox`, `RxGetter`, and `IObservable` are now based on kotlin `Flow` rather than `RxJava`.
### Changed (but probably doesn't affect you)
* **BREAKING** `Chit.Settable` no longer implements `io.reactivex.disposables.Disposable`.
* **BREAKING** Removed `RxGetter.fromVolatile`, `Breaker`, and `RateProbe`.
* **BREAKING** Removed `RxavaCompat`.
- **BREAKING** Removed OSGi metadata.

## [3.1.2] - 2021-10-21
### Fixed
* Added some missing default methods in `Rx`

## [3.1.1] - 2021-10-21
### Fixed
* Added some missing default methods in `RxSubscriber`

## [3.1.0] - 2021-10-21
### Added
* Added support for kotlinx `Flow` and `Deferred`. ([#6](https://github.com/diffplug/durian-rx/pull/6))

## [3.0.2] - 2020-05-26
### Fixed
* `Chit.isDisposed()` now returns true before calling the `runWhenDisposed` callbacks.

## [3.0.1] - 2019-11-12
* RxExecutor is now more consistent about failure - if the `onSuccess` throws an exception, it will always be passed to the `onFailure` handler as a `CompletionException`.

## [3.0.0] - 2018-08-01
* `DisposableEar`'s final name is `Chit`.
* Added `Rx.sync(RxBox<T> a, RxBox<T> b)`.
* Added `MultiSelectModel` for a UI-independent multi-selection model.

## [3.0.0.BETA2] - 2017-03-08
* Got rid of the `RxListener.IsLogging` marker interface.
* Made `RxListener.isLogging()` public, and added `RxListener.onErrorDontLog(Throwable)`.
  + Combined, these methods make it possible for an external framework to detect and hijack logging for a specific listener.
  + Used by the Agent framework in DiffPlug 2+
* Added `CasBox.getAndSet()`.
* `DispoableEar.Settable` now allows `dispose()` to be called multiple times, to comply with the `Disposable` contract.
* An `RxJavaCompat` layer for turning `Single` and `Maybe` into `CompletionStage`.

## [3.0.0.BETA] - 2017-02-07
* Added `DisposableEar` and `GuardedExecutor`.
* Fixed a bug in `ForwardingBox.modify()`.
* `RxExecutor` now exposes the underlying `Executor`, `Scheduler`, and `RxTracingPolicy`.

## [3.0.0.ALPHA] - 2016-11-11
* Bumped RxJava to 2.0, and incorporated `RxTracingPolicy` into `RxJavaPlugins`.
* Fixed a bug in `ForwardingBox.modify()`.
* `RxExecutor` now exposes the underlying `Executor`, `Scheduler`, and `RxTracingPolicy`.

## [2.0.0] - 2016-07-13
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

## [1.3.0] - 2016-02-09
* Ditched Guava for DurianGuava.

## [1.2.0] - 2015-11-18
* Added support for `CompletionStage` (and therefore `CompletableFuture`), with the same behavior as `ListenableFuture`.

## [1.1.0] - 2015-10-19
* Changed OSGi metadata Bundle-SymbolicName to `com.diffplug.durian.rx`.
* OSGi metadata is now auto-generated using bnd.

## [1.0.1] - 2015-07-27
* Gah! MANIFEST.MF still had -SNAPSHOT version.  Fixed now.  Would be really nice if we could get MANIFEST.MF generation working.

## [1.0] - 2015-05-13
* First stable release.
