# parcelize-darwin

**Experimental** Kotlin/Native compiler plugin that generates `Parcelable` implementations for Darwin (Apple) targets. Allows writing `Parcelable` classes for all Darwin targets, similary to the Android's `kotlin-parcelize` plugin. Can be also used together with the `kotlin-parcelize` plugin to write `Parcelable` classes in the `commonMain` source set.

Supported targets: `ios`, `watchos`, `tvos` and `macos`

Supported types:

- Kotlin primitive types and their nullable counterparts
- `String` type
- `Enum` classes
- `Parcelable` classes
- Collections: `List`, `MutableList`, `Set`, `MutableSet`, `Map`, `MutableMap` of all supported types

## Setup

Apply the plugin in your `build.gradle` file.

```groovy
// Root build.gradle

buildscript {
    repositories {
        mavenCentral()
    }

    dependencies {
        classpath("com.arkivanov.parcelize.darwin:gradle-plugin:<version>)
    }
}

// Module's build.gradle

plugins {
    id("com.arkivanov.parcelize.darwin")
}
```

The plugin automatically adds the `runtime` dependency to all Darwin source sets. However, if you need to use `Parcelable` interface or `@Parcelize` annotation in a shared source set (e.g. `iosMain`), you will need to add the `runtime` dependency manually.

```groovy
kotlin {
    sourceSets {
        iosMain {
            dependencies {
                implementation "com.arkivanov.parcelize.darwin:runtime:<version>"
            }
        }
    }
}
```

The `runtime` dependency often needs to be [exported](https://kotlinlang.org/docs/multiplatform-build-native-binaries.html#export-dependencies-to-binaries) to the Apple framework. This allows encoding and decoding `Parcelable` classes in Swift.

## Using

The plugin works similary to the Android's `kotlin-parcelize` plugin.

The `runtime` module provides the following:

- [Parcelable](https://github.com/arkivanov/parcelize-darwin/blob/master/runtime/src/darwinMain/kotlin/com/arkivanov/parcelize/darwin/Parcelable.kt) - interface for classes that require serialization/deserialization.
- [@Parcelize](https://github.com/arkivanov/parcelize-darwin/blob/master/runtime/src/darwinMain/kotlin/com/arkivanov/parcelize/darwin/Parcelize.kt) - an annotation that instructs the compiler plugin to generate `Parcelable` implementations for the annotated classes.
- [Parceler](https://github.com/arkivanov/parcelize-darwin/blob/master/runtime/src/darwinMain/kotlin/com/arkivanov/parcelize/darwin/Parceler.kt) - interface for custom `Parcelize` serializers.
- [WriteWith](https://github.com/arkivanov/parcelize-darwin/blob/master/runtime/src/darwinMain/kotlin/com/arkivanov/parcelize/darwin/WriteWith.kt) - specifies what `Parceler` should be used for the annotated type.
- [TypeParceler](https://github.com/arkivanov/parcelize-darwin/blob/master/runtime/src/darwinMain/kotlin/com/arkivanov/parcelize/darwin/TypeParceler.kt) - specifies what `Parceler` should be used for a particular type.
- Some handy utils (can be used from Swift) - [Coding.kt](https://github.com/arkivanov/parcelize-darwin/blob/master/runtime/src/darwinMain/kotlin/com/arkivanov/parcelize/darwin/Coding.kt)

## Examples

Here is an example of some `Parcelable` classes:

```kotlin
@Parcelize
data class User(
    val name: String,
    val age: Int
) : Parcelable

@Parcelize
data class UserGroup(
    val users: List<User>
) : Parcelable
```

A complete example can be found here - [sample](https://github.com/arkivanov/parcelize-darwin/tree/master/sample).

An example of the generated code can be found here - [SomeParcelable.kt](https://github.com/arkivanov/parcelize-darwin/blob/master/runtime/src/darwinTest/kotlin/com/arkivanov/parcelize/darwin/SomeParcelable.kt).

### Custom parcelers (starting from v0.2.0)

Suppose there is a type that we don't own and it has to be included in a `Parcelable` class.

```kotlin
class SomeTypeYouDontOwn(val someValue: Int)

@Parcelize
data class Foo(
    val data: SomeTypeYouDontOwn,
) : Parcelable
```

The above snippet won't compile because the compiler plugin doesn't know how to serialize `SomeTypeYouDontOwn`. Usually just implementing `Parcelable` interface and adding `@Parcelize` annotation would work, but not in this case, because we don't own that type. A solution is to write a custom parceler.

Write a `Parceler` object for that class.

```kotlin
internal object SomeTypeYouDontOwnParceler : Parceler<SomeTypeYouDontOwn> {
    override fun create(coder: NSCoder): SomeTypeYouDontOwn =
        SomeTypeYouDontOwn(
            someValue = coder.decodeInt32ForKey(key = "someValue"),
        )

    override fun SomeTypeYouDontOwn.write(coder: NSCoder) {
        coder.encodeInt32(value = someValue, forKey = "someValue")
    }
}
```

Now there are two ways of serializing that class.

#### Using WriteWith annotation

```kotlin
@Parcelize
data class Foo(
    val data: @WriteWith<SomeTypeYouDontOwnParceler> SomeTypeYouDontOwn,
) : Parcelable
```

#### Using TypeParceler annotation

```kotlin
@TypeParceler<SomeTypeYouDontOwn, SomeTypeYouDontOwnParceler>
@Parcelize
data class Foo(
    val data: SomeTypeYouDontOwn,
) : Parcelable
```

## Writing Parcelable classes in commonMain

The plugin can be used to write `Parcelable` classes in the `commonMain` source set. The recommended way is to use Essenty library, its [parcelable](https://github.com/arkivanov/Essenty#parcelable-and-parcelize) module bridges both Android and Darwin implementations in `commonMain`. There are APIs for state preservation as well.

### Manual usage

1. Define the following expectactions in the `commonMain` source set:

```kotlin
expect interface Parcelable

@OptIn(ExperimentalMultiplatform::class)
@OptionalExpectation
@Target(AnnotationTarget.CLASS)
expect annotation class Parcelize()
```

2. Define the following actuals in the `androidMain` source set (only needed if `kotlin-parcelize` plugin is used):

```kotlin
actual typealias Parcelable = android.os.Parcelable
actual typealias Parcelize = kotlinx.parcelize.Parcelize
```

3. Define the following actuals in the `iosMain` source set:

```kotlin
actual typealias Parcelable = com.arkivanov.parcelize.darwin.Parcelable
actual typealias Parcelize = com.arkivanov.parcelize.darwin.Parcelize
```

4. Define the following actual for all other targets that do not support `Parcelize`:

```kotlin
actual interface Parcelable
// No need to define Parcelize, it is optional
```

5. Make sure you have enabled the plugin as described in the *Setup* section

Now you should be able to use `Parcelize` feature as usual in the `commonMain` source set.
The code will be automatically generated for Android and iOS, without affecting all other targets.

## Limitations

There is no proper IDE support at the moment. So `Parcelable` classes in the `iosMain` source set
are highlighted as incomplete, but the code still compiles just fine. There are no IDE errors in the `commonMain` source set.

## Tests as usage examples

Some tests can be found [here](https://github.com/arkivanov/parcelize-darwin/blob/master/tests/src/darwinTest/kotlin/com/arkivanov/parcelize/darwin/tests/ParcelizeTest.kt).

## Author

Twitter: [@arkann1985](https://twitter.com/arkann1985)

If you like this project you can always <a href="https://www.buymeacoffee.com/arkivanov" target="_blank"><img src="https://cdn.buymeacoffee.com/buttons/v2/default-blue.png" alt="Buy Me A Coffee" height=32></a> ;-)
