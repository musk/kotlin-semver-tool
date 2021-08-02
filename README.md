The semver kotlin utility
=========================

This is a Kotlin port of [groovy-semver-tool](https://github.com/Wonno/groovy-semver-tool)

License [MIT](https://github.com/musk/kotlin-semver-tool/blob/main/LICENSE)

semver is a little tool to manipulate the version bumping in a project that follows the [Semver Specification 2.0](https://semver.org/spec/v2.0.0.html) .

Its use are:
- bump version
- extract specific version part
- compare versions

A  version must match the following regular expression:
```
^[vV]?(0|[1-9][0-9]*)\.(0|[1-9][0-9]*)\.(0|[1-9][0-9]*)(\-(0|[1-9][0-9]*|[0-9]*[A-Za-z-][0-9A-Za-z-]*)(\.(0|[1-9][0-9]*|[0-9]*[A-Za-z-][0-9A-Za-z-]*))*)?(\+[0-9A-Za-z-]+(\.[0-9A-Za-z-]+)*)?$
```

In English:

The version must match `X.Y.Z[-PRERELEASE][+BUILD]` where `X`, `Y` and `Z` are non-negative integers. 
`-PRERELEASE` is a dot separated sequence of non-negative integers and/or identifiers composed of alphanumeric characters and hyphens (with at least one non-digit). Numeric identifiers must not have leading zeros. A hyphen (\"-\") introduces this optional part.
`-BUILD` is a dot separated sequence of identifiers composed of alphanumeric characters and hyphens. A plus ("+") introduces this optional part.

## Build
```
gradlew assemble
```

## Examples
```kotlin
import com.github.musk.semver.Semver

// version validation
assert(!Semver.validate("1.2.invalid"))

// version change
val version = Semver.parse("1.2.3+abcd").prerel("rc1").minor()
assert(version.toString() == "1.3.0")
assert(version == Semver(1,3,0))

// version comparison v1 < v2
val v1 = Semver.parse("1.0.7+acf430")
val v2 = Semver(1,0,6).patch()
assert (v1 < v2)
```
See [Java Demo](https://github.com/musk/kotlin-semver-tool/blob/main/semver-demo-java/src/main/java/io/github/musk/semver/demo/java/HandleRelease.java) for a demonstration of how to use the library in Java.  
See [Kotlin Demo](https://github.com/musk/kotlin-semver-tool/blob/main/semver-demo-kotlin/src/main/kotlin/io/github/musk/semver/demo/kotlin/HandleRelease.kt) for another demonstration of how to use the library in Kotlin.

## Links
* [Semver Specification 2.0](https://semver.org/spec/v2.0.0.html)
* Inspired by [semver-tool](https://github.com/fsaintjacques/semver-tool/) written in bash.

## Credits
* ported from [groovy-semver-tool](https://github.com/Wonno/groovy-semver-tool)
* [semver-tool](https://github.com/fsaintjacques/semver-tool/) project for the regex and the testcases
