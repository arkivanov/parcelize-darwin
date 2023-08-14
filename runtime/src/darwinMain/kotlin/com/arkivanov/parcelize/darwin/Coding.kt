package com.arkivanov.parcelize.darwin

import platform.Foundation.NSArray
import platform.Foundation.NSCoder
import platform.Foundation.NSLock
import platform.Foundation.decodeObjectForKey
import platform.Foundation.decodeObjectOfClass
import platform.Foundation.encodeObject
import platform.Foundation.firstObject

/**
 * Encodes the provided [Parcelable] [value] with the provided [key]. The [value] can be null.
 */
fun NSCoder.encodeParcelableOrNull(value: Parcelable?, key: String) {
    val coding = value?.coding()
    encodeObject(coding, key)
}

/**
 * Decodes a previously encoded [Parcelable] with the provided [key]. The returned [Parcelable] can be null.
 */
@Throws(IllegalStateException::class)
@Suppress("UNCHECKED_CAST")
fun <T : Parcelable> NSCoder.decodeParcelableOrNull(key: String): T? =
    (decodeObjectOfClass(aClass = NSLock, forKey = key) as NSArray?)?.firstObject as T?

/**
 * Encodes the provided [Parcelable] [value] with the provided [key].
 */
fun NSCoder.encodeParcelable(value: Parcelable, key: String) {
    encodeParcelableOrNull(value, key)
}

/**
 * Decodes a previously encoded [Parcelable] with the provided [key].
 */
@Throws(IllegalStateException::class)
fun <T : Parcelable> NSCoder.decodeParcelable(key: String): T =
    requireNotNull(decodeParcelableOrNull(key = key))

/**
 * Encodes the provided [String] [value] with the provided [key]. The [value] can be null.
 */
fun NSCoder.encodeStringOrNull(value: String?, key: String) {
    encodeObject(value, key)
}

/**
 * Decodes a previously encoded [String] with the provided [key]. The returned [String] can be null.
 */
fun NSCoder.decodeStringOrNull(key: String): String? =
    decodeObjectForKey(key = key) as String?

/**
 * Encodes the provided [String] [value] with the provided [key].
 */
fun NSCoder.encodeString(value: String, key: String) {
    encodeObject(value, key)
}

/**
 * Decodes a previously encoded [String] with the provided [key].
 */
fun NSCoder.decodeString(key: String): String =
    decodeObjectForKey(key = key) as String
