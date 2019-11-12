# <img align="left" src="durian-rx.png"> DurianRx: Reactive getters, powered by RxJava and ListenableFuture

<!---freshmark shields
output = [
	link(shield('Maven artifact', 'mavenCentral', '{{group}}:{{name}}', 'blue'), 'https://bintray.com/{{org}}/opensource/{{name}}/view'),
	link(shield('Latest version', 'latest', '{{stable}}', 'blue'), 'https://github.com/{{org}}/{{name}}/releases/latest'),
	link(shield('Javadoc', 'javadoc', 'OK', 'blue'), 'https://javadoc.io/doc/com.diffplug.durian/durian-rx/{{stable}}/'),
	link(shield('License Apache', 'license', 'Apache', 'blue'), 'https://tldrlegal.com/license/apache-license-2.0-(apache-2.0)'),
	'',
	link(shield('Changelog', 'changelog', '{{version}}', 'brightgreen'), 'CHANGES.md'),
	link(image('Travis CI', 'https://travis-ci.org/{{org}}/{{name}}.svg?branch=master'), 'https://travis-ci.org/{{org}}/{{name}}'),
	link(shield('Live chat', 'gitter', 'live chat', 'brightgreen'), 'https://gitter.im/diffplug/durian')
	].join('\n');
-->
[![Maven artifact](https://img.shields.io/badge/mavenCentral-com.diffplug.durian%3Adurian--rx-blue.svg)](https://bintray.com/diffplug/opensource/durian-rx/view)
[![Latest version](https://img.shields.io/badge/latest-3.0.1-blue.svg)](https://github.com/diffplug/durian-rx/releases/latest)
[![Javadoc](https://img.shields.io/badge/javadoc-OK-blue.svg)](https://javadoc.io/doc/com.diffplug.durian/durian-rx/3.0.1/)
[![License Apache](https://img.shields.io/badge/license-Apache-blue.svg)](https://tldrlegal.com/license/apache-license-2.0-(apache-2.0))

[![Changelog](https://img.shields.io/badge/changelog-3.0.1-brightgreen.svg)](CHANGES.md)
[![Travis CI](https://travis-ci.org/diffplug/durian-rx.svg?branch=master)](https://travis-ci.org/diffplug/durian-rx)
[![Live chat](https://img.shields.io/badge/gitter-live_chat-brightgreen.svg)](https://gitter.im/diffplug/durian)
<!---freshmark /shields -->

<!---freshmark javadoc
output = prefixDelimiterReplace(input, 'https://javadoc.io/doc/com.diffplug.durian/durian-rx/', '/', stable);
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
