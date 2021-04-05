package com.arkivanov.parcelize.darwin.runtime

import platform.Foundation.NSCoder
import platform.Foundation.decodeBoolForKey
import platform.Foundation.decodeDoubleForKey
import platform.Foundation.decodeFloatForKey
import platform.Foundation.decodeInt32ForKey
import platform.Foundation.decodeInt64ForKey
import platform.Foundation.decodeObjectForKey
import platform.Foundation.encodeBool
import platform.Foundation.encodeDouble
import platform.Foundation.encodeFloat
import platform.Foundation.encodeInt32
import platform.Foundation.encodeInt64
import platform.Foundation.encodeObject

fun NSCoder.encodeParcelable(value: Parcelable?, key: String) {
    encodeObject(value?.coding(), key)
}

fun NSCoder.decodeParcelable(key: String): Parcelable? =
    (decodeObjectForKey(key) as DecodedValue?)?.value

fun NSCoder.encodeString(value: String?, key: String) {
    encodeObject(value, key)
}

fun NSCoder.decodeString(key: String): String? =
    decodeObjectForKey(key) as String?

fun NSCoder.encodeIntOrNull(value: Int?, key: String) {
    encodeNullableObject(value, key, NSCoder::encodeInt32)
}

fun NSCoder.decodeIntOrNull(key: String): Int? =
    decodeNullableObject(key, NSCoder::decodeInt32ForKey)

fun NSCoder.encodeLongOrNull(value: Long?, key: String) {
    encodeNullableObject(value, key, NSCoder::encodeInt64)
}

fun NSCoder.decodeLongOrNull(key: String): Long? =
    decodeNullableObject(key, NSCoder::decodeInt64ForKey)

fun NSCoder.encodeFloatOrNull(value: Float?, key: String) {
    encodeNullableObject(value, key, NSCoder::encodeFloat)
}

fun NSCoder.decodeFloatOrNull(key: String): Float? =
    decodeNullableObject(key, NSCoder::decodeFloatForKey)

fun NSCoder.encodeDoubleOrNull(value: Double?, key: String) {
    encodeNullableObject(value, key, NSCoder::encodeDouble)
}

fun NSCoder.decodeDoubleOrNull(key: String): Double? =
    decodeNullableObject(key, NSCoder::decodeDoubleForKey)

fun NSCoder.encodeShort(value: Short, key: String) {
    encodeInt32(value.toInt(), key)
}

fun NSCoder.decodeShort(key: String): Short =
    decodeInt32ForKey(key).toShort()

fun NSCoder.encodeShortOrNull(value: Short?, key: String) {
    encodeNullableObject(value?.toInt(), key, NSCoder::encodeInt32)
}

fun NSCoder.decodeShortOrNull(key: String): Short? =
    decodeNullableObject(key, NSCoder::decodeInt32ForKey)?.toShort()

fun NSCoder.encodeByte(value: Byte, key: String) {
    encodeInt32(value.toInt(), key)
}

fun NSCoder.decodeByte(key: String): Byte =
    decodeInt32ForKey(key).toByte()

fun NSCoder.encodeByteOrNull(value: Byte?, key: String) {
    encodeNullableObject(value?.toInt(), key, NSCoder::encodeInt32)
}

fun NSCoder.decodeByteOrNull(key: String): Byte? =
    decodeNullableObject(key, NSCoder::decodeInt32ForKey)?.toByte()

fun NSCoder.encodeChar(value: Char, key: String) {
    encodeInt32(value.toInt(), key)
}

fun NSCoder.decodeChar(key: String): Char =
    decodeInt32ForKey(key).toChar()

fun NSCoder.encodeCharOrNull(value: Char?, key: String) {
    encodeNullableObject(value?.toInt(), key, NSCoder::encodeInt32)
}

fun NSCoder.decodeCharOrNull(key: String): Char? =
    decodeNullableObject(key, NSCoder::decodeInt32ForKey)?.toChar()

fun NSCoder.encodeBooleanOrNull(value: Boolean?, key: String) {
    encodeNullableObject(value, key, NSCoder::encodeBool)
}

fun NSCoder.decodeBooleanOrNull(key: String): Boolean? =
    decodeNullableObject(key, NSCoder::decodeBoolForKey)

private fun <T : Any> NSCoder.encodeNullableObject(value: T?, key: String, encode: NSCoder.(T, key: String) -> Unit) {
    encodeBool(value != null, "$key-exists")
    if (value != null) {
        encode(value, key)
    }
}

private fun <T : Any> NSCoder.decodeNullableObject(key: String, decode: NSCoder.(key: String) -> T?): T? {
    val exists = decodeBoolForKey("$key-exists")

    return if (exists) decode(key) else null
}

// region Collection

fun NSCoder.encodeCollection(value: Collection<Any?>?, key: String) {
    encodeInt32(value?.size ?: -1, "$key-size")

    value?.forEachIndexed { index, item ->
        val itemKey = "$key-$index"
        val itemTypeKey = "$itemKey-type"

        when (item) {
            is Parcelable? -> {
                encodeItemType(ItemType.PARCELABLE, itemTypeKey)
                encodeParcelable(item, itemKey)
            }

            is String -> {
                encodeItemType(ItemType.STRING, itemTypeKey)
                encodeString(item, itemKey)
            }

            else -> error("Unsupported item type: $item")
        }
    }
}

fun NSCoder.decodeList(key: String): MutableList<Any?>? = decodeCollection(key, mutableListOf())

fun NSCoder.decodeSet(key: String): MutableSet<Any?>? = decodeCollection(key, mutableSetOf())

private fun <C : MutableCollection<Any?>> NSCoder.decodeCollection(key: String, collection: C): C? {
    val size = decodeInt32ForKey("$key-size").takeIf { it >= 0 } ?: return null

    repeat(size) { index ->
        val itemKey = "$key-$index"
        val itemType = decodeItemType("$itemKey-type")

        collection +=
            when (itemType) {
                ItemType.PARCELABLE -> decodeParcelable(itemKey)
                ItemType.STRING -> decodeString(itemKey)
            }
    }

    return collection
}

fun NSCoder.encodeMap(map: Map<Any?, Any?>?, key: String) {
    encodeCollection(map?.keys, "$key-keys")
    encodeCollection(map?.values, "$key-values")
}

fun NSCoder.decodeMap(key: String): MutableMap<Any?, Any?>? {
    val keys = decodeList("$key-keys") ?: return null
    val values = decodeList("$key-values") ?: return null

    val map = mutableMapOf<Any?, Any?>()
    keys.forEachIndexed { index, itemKey ->
        map[itemKey] = values[index]
    }

    return map
}

private fun NSCoder.encodeItemType(itemType: ItemType, key: String) {
    encodeInt32(itemType.ordinal, key)
}

private fun NSCoder.decodeItemType(key: String): ItemType =
    ItemType.VALUES[decodeInt32ForKey(key)]

private enum class ItemType {
    PARCELABLE,
    STRING;

    companion object {
        val VALUES: List<ItemType> = values().asList()
    }
}

// endregion
