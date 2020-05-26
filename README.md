# <img align="left" src="durian-rx.png"> DurianRx: Reactive getters, powered by RxJava and ListenableFuture

<!---freshmark shields
output = [
    link(shield('Maven central', 'mavencentral', 'com.diffplug.durian:durian-rx', 'blue'), 'https://search.maven.org/artifact/com.diffplug.durian/durian-rx'),
    link(shield('Apache 2.0', 'license', 'apache-2.0', 'blue'), 'https://tldrlegal.com/license/apache-license-2.0-(apache-2.0)'),
    '',
    link(shield('Changelog', 'changelog', versionLast, 'brightgreen'), 'CHANGES.md'),
    link(shield('Javadoc', 'javadoc', 'yes', 'brightgreen'), 'https://javadoc.io/doc/com.diffplug.durian/durian-rx/{{versionLast}}/'),
    link(shield('Live chat', 'gitter', 'chat', 'brightgreen'), 'https://gitter.im/diffplug/durian'),
    link(image('JitCI', 'https://jitci.com/gh/diffplug/durian-rx/svg'), 'https://jitci.com/gh/diffplug/durian-rx')
    ].join('\n');
-->
[![Maven central](https://img.shields.io/badge/mavencentral-com.diffplug.durian%3Adurian--rx-blue.svg)](https://search.maven.org/artifact/com.diffplug.durian/durian-rx)
[![Apache 2.0](https://img.shields.io/badge/license-apache--2.0-blue.svg)](https://tldrlegal.com/license/apache-license-2.0-(apache-2.0))

[![Changelog](https://img.shields.io/badge/changelog-3.0.2-brightgreen.svg)](CHANGES.md)
[![Javadoc](https://img.shields.io/badge/javadoc-yes-brightgreen.svg)](https://javadoc.io/doc/com.diffplug.durian/durian-rx/3.0.2/)
[![Live chat](https://img.shields.io/badge/gitter-chat-brightgreen.svg)](https://gitter.im/diffplug/durian)
[![JitCI](https://jitci.com/gh/diffplug/durian-rx/svg)](https://jitci.com/gh/diffplug/durian-rx)
<!---freshmark /shields -->

<!---freshmark javadoc
output = prefixDelimiterReplace(input, 'https://javadoc.io/static/com.diffplug.durian/durian-rx/', '/', versionLast);
-->
DurianRx unifies RxJava's [Observable](http://reactivex.io/documentation/observable.html) with Guava's [ListenableFuture](https://code.google.com/p/guava-libraries/wiki/ListenableFutureExplained).  If you happen to be using SWT as a widget toolkit, then you'll want to look at [DurianSwt](https://github.com/diffplug/durian-swt) as well.

```java
Observable<SomeType> observable = someObservable();
ListenableFuture<SomeType> future = someFuture();
Rx.subscribe(observable, val -> doSomething(val));
Rx.subscribe(future, val -> doSomething(val));
```

It also provides [reactive getters](src/com/diffplug/common/rx/RxGetter.java?ts=4), a simple abstraction for piping data which allows access via `T get()` or `Observable<T> asObservable()`.

```java
RxBox<Point> mousePos = RxBox.of(new Point(0, 0));
this.addMouseListener(e -> mousePos.set(new Point(e.x, e.y)));

Rectangle hotSpot = new Rectangle(0, 0, 10, 10)
RxGetter<Boolean> isMouseOver = mousePos.map(hotSpot::contains);
```

Debugging an error which involves lots of callbacks can be difficult.  To make this easier, DurianRx includes a [tracing capability](src/com/diffplug/common/rx/RxTracingPolicy.java?ts=4), which makes this task easier.

```java
// anytime an error is thrown in an Rx callback, the stack trace of the error
// will be wrapped by the stack trace of the original subscription
DurianPlugins.register(RxTracingPolicy.class, new LogSubscriptionTrace()).
```

DurianRx's only requirements are [durian-base, durian-collect, durian-concurrent](https://github.com/diffplug/durian), and [RxJava](https://github.com/reactivex/rxjava).

<!---freshmark /javadoc -->

## Acknowledgements

* Many thanks to [RxJava](https://github.com/reactivex/rxjava) and [Guava](https://github.com/google/guava).
* Built by [gradle](http://gradle.org/).
* Tested by [junit](http://junit.org/).
* Maintained by [DiffPlug](http://www.diffplug.com/).
