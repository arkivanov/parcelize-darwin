# kotlin-parcelize-darwin

Kotlin/Native compiler plugin generating `Parcelable` implementations for Darwin/Apple.
Allows writing `Parcelable` classes for iOS (eventually for all Darwin targets, possibly),
similary to the Android's `kotlin-parcelize` plugin. Can be used together with `kotlin-parcelize`
plugin to write `Parcelable` classes in the `commonMain` source set.

It is working, but still work in progress.

Supported targets: `iosX64`, `iosArm64`.

Supported types:
- All Kotlin primitive types
- The `String` type
- `Parcelable` classes
- Collections: `List`, `MutableList`, `Set`, `MutableSet`, `Map`, `MutableMap` of all supported types

## Setup

The plugin is not published yet, but can be used when published locally.

1. Checkout the repository
2. Run `./gradlew publishToMavenLocal`

3. In the root `build.gradle`:
```groovy
buildscript {
    repositories {
        mavenLocal()
    }
    dependencies {
        classpath "com.arkivanov.parcelize.darwin:kotlin-parcelize-darwin:0.1.0"
    }
}
```

4. In the project's `build.gradle`:
```kotlin
apply plugin: "kotlin-parcelize-darwin"

repositories {
    mavenLocal()
}

kotlin {
    ios()

    sourceSets {
        iosMain {
            dependencies {
                implementation "com.arkivanov.parcelize.darwin:kotlin-parcelize-darwin-runtime:0.1.0"
            }
        }
    }
}
```

## Using

The plugin works similary to the Android's `kotlin-parcelize` plugin.

The `kotlin-parcelize-darwin-runtime` module provides the following things:
- [Parcelable](https://github.com/arkivanov/kotlin-parcelize-darwin/blob/master/kotlin-parcelize-darwin-runtime/src/iosMain/kotlin/com/arkivanov/parcelize/darwin/runtime/Parcelable.kt) interface
- [@Parcelize](https://github.com/arkivanov/kotlin-parcelize-darwin/blob/master/kotlin-parcelize-darwin-runtime/src/iosMain/kotlin/com/arkivanov/parcelize/darwin/runtime/Parcelize.kt) annotation
- Some handy [CoderUtils](https://github.com/arkivanov/kotlin-parcelize-darwin/blob/master/kotlin-parcelize-darwin-runtime/src/iosMain/kotlin/com/arkivanov/parcelize/darwin/runtime/CoderUtils.kt)

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

Encoding a `Parcelable` class using `NSCoder`:
```kotlin
import com.arkivanov.parcelize.darwin.runtime.encodeParcelable

fun encode(coder: NSCoder, user: User) {
    coder.encodeParcelable(value = user, key = "user")
}
```

Decoding a `Parcelable` class using `NSCoder`:
```kotlin
import com.arkivanov.parcelize.darwin.runtime.decodeParcelable

fun decode(coder: NSCoder) {
    val user: User? = coder.decodeParcelable(key = "user") as User?
}
```

### Writing Parcelables in commonMain

The plugin can be used to write `Parcelable` classes in the `commonMain` source set:

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
actual typealias Parcelable = com.arkivanov.parcelize.darwin.runtime.Parcelable
actual typealias Parcelize = com.arkivanov.parcelize.darwin.runtime.Parcelize
```

4. Define the following actual for all other targets that do not support `Parcelize`:

```kotlin
actual interface Parcelable
// No need to define Parcelize, it is optional
```

5. Make sure you have enabled the plugin as described in the *Setup* section

Now you should be able to use `Parcelize` feature as usual in the `commonMain` source set.
The code will be automatically generated for Android and iOS, without affecting all other targets.

### Limitations

There is no proper IDE support at the moment. So `Parcelable` classes in the `iosMain` source set
are highlighted as incomplete, but the code still compiles just fine. There are no IDE errors in the `commonMain` source set.

### Tests as using examples

Some tests can be found [here](https://github.com/arkivanov/kotlin-parcelize-darwin/blob/master/tests/src/iosTest/kotlin/com/arkivanov/parcelize/darwin/tests/ParcelizeTest.kt).

## Author

Twitter: [@arkann1985](https://twitter.com/arkann1985)

If you like this project you can always <a href="https://www.buymeacoffee.com/arkivanov" target="_blank"><img src="https://cdn.buymeacoffee.com/buttons/v2/default-blue.png" alt="Buy Me A Coffee" height=32></a> ;-)
