package com.arkivanov.parcelize.darwin

import platform.Foundation.NSCoder
import platform.Foundation.NSLock
import platform.Foundation.NSSecureCodingProtocol
import platform.Foundation.decodeObjectOfClass
import platform.Foundation.encodeObject

fun NSCoder.encodeParcelableOrNull(value: Parcelable?, key: String) {
    val coding = value?.requireCoding()
    encodeObject(coding, key)
}

private fun Parcelable.requireCoding(): NSSecureCodingProtocol =
    requireNotNull(coding()) { "Coding returned by ${this::class} is null" }

@Throws(IllegalStateException::class)
@Suppress("UNCHECKED_CAST")
fun <T : Parcelable> NSCoder.decodeParcelableOrNull(key: String): T? =
    (decodeObjectOfClass(aClass = NSLock, forKey = key) as DecodedValue?)?.value as T?

fun NSCoder.encodeParcelable(value: Parcelable, key: String) {
    encodeParcelableOrNull(value, key)
}

@Throws(IllegalStateException::class)
fun <T : Parcelable> NSCoder.decodeParcelable(key: String): T =
    requireNotNull(decodeParcelableOrNull(key = key))
