#!/bin/bash

# Do the Gradle build
./gradlew build || exit 1

if [ "$TRAVIS_REPO_SLUG" == "diffplug/durian-rx" ] && [ "$TRAVIS_PULL_REQUEST" == "false" ] && [ "$TRAVIS_BRANCH" == "master" ]; then
	# Publish the artifacts
	./gradlew publish || exit 1
	# Publish the javadoc
	./gradlew publishGhPages || exit 1
fi
