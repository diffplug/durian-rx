# <img align="left" src="durian-rx.png"> DurianRx: Reactive getters, powered by RxJava and ListenableFuture

[![JCenter artifact](https://img.shields.io/badge/mavenCentral-com.diffplug.durian%3Adurian--rx-blue.svg)](https://bintray.com/diffplug/opensource/durian-rx/view)
[![Branch master](http://img.shields.io/badge/master-1.0-lightgrey.svg)](https://github.com/diffplug/durian-rx/releases/latest)
[![Branch develop](http://img.shields.io/badge/develop-1.1--SNAPSHOT-lightgrey.svg)](https://github.com/diffplug/durian-rx/tree/develop)
[![Branch develop Travis CI](https://travis-ci.org/diffplug/durian-rx.svg?branch=develop)](https://travis-ci.org/diffplug/durian-rx)
[![License](https://img.shields.io/badge/license-Apache-blue.svg)](https://tldrlegal.com/license/apache-license-2.0-(apache-2.0))

# NOT YET SUITABLE FOR USE - we're releasing a formerly internal library, bear with us as we clean it up for public release

DurianRx unifies RxJava's [Observable](http://reactivex.io/documentation/observable.html) with Guava's [ListenableFuture](https://code.google.com/p/guava-libraries/wiki/ListenableFutureExplained).

```java
ListenableFuture<SomeType> future = someFuture();
Observable<SomeType> observable = someObservable();
Rx.subscribe(future, val -> doSomething(val));
Rx.subscribe(observable, val -> doSomething(val));
```

It also provides [reactive getters](src/com/diffplug/common/rx/RxGetter.java?ts=4), a simple abstraction for piping data which allows access via `T get()` or `Observable<T> asObservable()`.

```java
RxValue<Point> mousePos = RxValue.of(new Point(0, 0));
this.addMouseListener(e -> mousePos.set(new Point(e.x, e.y)));

Rectangle hotSpot = new Rectangle(0, 0, 10, 10)
RxGetter<Boolean> isMouseOver = mousePos.map(hotSpot::contains);
```

Debugging an error which involves lots of callbacks can be difficult.  To make this easier, DurianRx includes a [tracing capability](src/com/diffplug/common/rx/RxTracingPolicy.java?ts=4), which makes this task easier.

```java
// anytime an error is thrown in an Rx callback, the stack trace of the error
// will be wrapped by the stack trace of the original subscription
DurianPlugins.set(RxTracingPolicy.class, new LogSubscriptionTrace()).
```

Lastly, DurianRx provides convenience classes for manipulating Guava's immutable collections inside of reactive containers, such as `RxSet<T> extends RxValue<ImmutableSet<T>>`, which can be used as such:

```java
public void mouseClicked(MouseEvent e) {
	rxMouseOver.get().ifPresent(cell -> {
		Set<Integer> currentSelection = rxSelection.get();
		if (e.isControlDown()) {
			// control => toggle mouseOver item in selection
			if (currentSelection.contains(cell)) {
				rxSelection.remove(cell);
			} else {
				rxSelection.add(cell);
			}
		} else {
			// no control => set selection to mouseOver
			rxSelection.set(Collections.singleton(cell));
		}
	});
}

...

Rx.subscribe(rxSelection, set -> {
	// take some action in response to 
	// selection change
});
```

DurianRx's only requirements are: Guava, RxJava, and Durian.  It is published to MavenCentral at the maven coordinates `com.diffplug.durian:durian-rx`.

## Acknowledgements

* Many thanks to [RxJava]() and [Guava]().
* Formatted by [spotless](https://github.com/diffplug/spotless)    [build.gradle](https://github.com/diffplug/durian-rx/blob/develop/build.gradle?ts=4#L70-90)
* Bugs found by [findbugs](http://findbugs.sourceforge.net/)    [build.gradle](https://github.com/diffplug/durian-rx/blob/develop/build.gradle?ts=4#L101-102)
* Built by [gradle](http://gradle.org/).
* Tested by [junit](http://junit.org/).
* Maintained by [DiffPlug](http://www.diffplug.com/).
