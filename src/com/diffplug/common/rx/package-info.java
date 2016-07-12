/**
 * DurianRx unifies RxJava's [Observable](http://reactivex.io/documentation/observable.html) with Guava's [ListenableFuture](https://code.google.com/p/guava-libraries/wiki/ListenableFutureExplained).  If you happen to be using SWT as a widget toolkit, then you'll want to look at [DurianSwt](https://github.com/diffplug/durian-swt) as well.
 * 
 * ```java
 * Observable<SomeType> observable = someObservable();
 * ListenableFuture<SomeType> future = someFuture();
 * Rx.subscribe(observable, val -> doSomething(val));
 * Rx.subscribe(future, val -> doSomething(val));
 * ```
 * 
 * It also provides {@linkplain RxGetter reactive getters}, a simple abstraction for piping data which allows access via `T get()` or `Observable<T> asObservable()`.
 * 
 * ```java
 * RxBox<Point> mousePos = RxBox.of(new Point(0, 0));
 * this.addMouseListener(e -> mousePos.set(new Point(e.x, e.y)));
 * 
 * Rectangle hotSpot = new Rectangle(0, 0, 10, 10)
 * RxGetter<Boolean> isMouseOver = mousePos.map(hotSpot::contains);
 * ```
 * 
 * Debugging an error which involves lots of callbacks can be difficult.  To make this easier, DurianRx includes a {@linkplain RxTracingPolicy tracing capability}, which makes this task easier.
 * 
 * ```java
 * // anytime an error is thrown in an Rx callback, the stack trace of the error
 * // will be wrapped by the stack trace of the original subscription
 * DurianPlugins.set(RxTracingPolicy.class, new LogSubscriptionTrace()).
 * ```
 */

@ParametersAreNonnullByDefault
package com.diffplug.common.rx;

import javax.annotation.ParametersAreNonnullByDefault;
