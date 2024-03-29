package com.arkivanov.parcelize.darwin.tests

import com.arkivanov.parcelize.darwin.Parcelable
import com.arkivanov.parcelize.darwin.Parceler
import com.arkivanov.parcelize.darwin.Parcelize
import com.arkivanov.parcelize.darwin.TypeParceler
import com.arkivanov.parcelize.darwin.WriteWith
import com.arkivanov.parcelize.darwin.decodeParcelable
import com.arkivanov.parcelize.darwin.encodeParcelable
import platform.Foundation.NSCoder
import platform.Foundation.NSKeyedArchiver
import platform.Foundation.NSKeyedUnarchiver
import platform.Foundation.decodeInt32ForKey
import platform.Foundation.encodeInt32
import kotlin.test.Test
import kotlin.test.assertEquals

class ParcelizeTest {

    @Test
    fun encodes_and_decodes() {
        val some =
            Some(
                i1 = 1,
                i2 = 2,
                i3 = null,
                l1 = 1L,
                l2 = 2L,
                l3 = null,
                f1 = 1F,
                f2 = 2F,
                f3 = null,
                d1 = 1.0,
                d2 = 2.0,
                d3 = null,
                h1 = 1,
                h2 = 2,
                h3 = null,
                b1 = 1,
                b2 = 2,
                b3 = null,
                c1 = 'a',
                c2 = 'b',
                c3 = null,
                z1 = false,
                z2 = true,
                z3 = null,
                s1 = "str",
                s2 = "str",
                s3 = null,
                parcelable1 = Other(a = 1),
                parcelable2 = Other(a = 1),
                parcelable3 = null,
                other1 = Other(a = 1),
                other2 = Other(a = 2),
                other3 = null,
                obj1 = Obj,
                obj2 = Obj,
                obj3 = null,
                enum1 = SomeEnum.A,
                enum2 = SomeEnum.B,
                enum3 = null,
                notParcelable11 = NotParcelable1(value = 1),
                notParcelable12 = NotParcelable1(value = 2),
                notParcelable13 = null,
                notParcelable21 = NotParcelable2(value = 1),
                notParcelable22 = NotParcelable2(value = 2),
                notParcelable23 = null,

                intList1 = listOf(1, 2),
                intList2 = listOf(3, 4),
                intList3 = null,
                intList4 = listOf(5, null),
                intList5 = listOf(6, null),
                intList6 = null,

                longList1 = listOf(1L, 2L),
                longList2 = listOf(3L, 4L),
                longList3 = null,
                longList4 = listOf(5L, null),
                longList5 = listOf(6L, null),
                longList6 = null,

                shortList1 = listOf(1.toShort(), 2.toShort()),
                shortList2 = listOf(3.toShort(), 4.toShort()),
                shortList3 = null,
                shortList4 = listOf(5.toShort(), null),
                shortList5 = listOf(6.toShort(), null),
                shortList6 = null,

                byteList1 = listOf(1.toByte(), 2.toByte()),
                byteList2 = listOf(3.toByte(), 4.toByte()),
                byteList3 = null,
                byteList4 = listOf(5.toByte(), null),
                byteList5 = listOf(6.toByte(), null),
                byteList6 = null,

                charList1 = listOf('a', 'b'),
                charList2 = listOf('c', 'd'),
                charList3 = null,
                charList4 = listOf('e', null),
                charList5 = listOf('f', null),
                charList6 = null,

                floatList1 = listOf(1F, 2F),
                floatList2 = listOf(3F, 4F),
                floatList3 = null,
                floatList4 = listOf(5F, null),
                floatList5 = listOf(6F, null),
                floatList6 = null,

                doubleList1 = listOf(1.0, 2.0),
                doubleList2 = listOf(3.0, 4.0),
                doubleList3 = null,
                doubleList4 = listOf(5.0, null),
                doubleList5 = listOf(6.0, null),
                doubleList6 = null,

                booleanList1 = listOf(false, true),
                booleanList2 = listOf(true, false),
                booleanList3 = null,
                booleanList4 = listOf(false, null),
                booleanList5 = listOf(true, null),
                booleanList6 = null,

                stringList1 = listOf("a", "b"),
                stringList2 = listOf("c", "d"),
                stringList3 = null,
                stringList4 = listOf("e", null),
                stringList5 = listOf("f", null),
                stringList6 = null,

                parcelableList1 = listOf(Other(a = 1), Other(a = 2)),
                parcelableList2 = listOf(Other(a = 3), Other(a = 4)),
                parcelableList3 = null,
                parcelableList4 = listOf(Other(a = 5), null),
                parcelableList5 = listOf(Other(a = 6), null),
                parcelableList6 = null,

                intMutableList1 = mutableListOf(1, 2),
                intMutableList2 = mutableListOf(3, 4),
                intMutableList3 = null,
                intMutableList4 = mutableListOf(5, null),
                intMutableList5 = mutableListOf(6, null),
                intMutableList6 = null,

                longMutableList1 = mutableListOf(1L, 2L),
                longMutableList2 = mutableListOf(3L, 4L),
                longMutableList3 = null,
                longMutableList4 = mutableListOf(5L, null),
                longMutableList5 = mutableListOf(6L, null),
                longMutableList6 = null,

                shortMutableList1 = mutableListOf(1.toShort(), 2.toShort()),
                shortMutableList2 = mutableListOf(3.toShort(), 4.toShort()),
                shortMutableList3 = null,
                shortMutableList4 = mutableListOf(5.toShort(), null),
                shortMutableList5 = mutableListOf(6.toShort(), null),
                shortMutableList6 = null,

                byteMutableList1 = mutableListOf(1.toByte(), 2.toByte()),
                byteMutableList2 = mutableListOf(3.toByte(), 4.toByte()),
                byteMutableList3 = null,
                byteMutableList4 = mutableListOf(5.toByte(), null),
                byteMutableList5 = mutableListOf(6.toByte(), null),
                byteMutableList6 = null,

                charMutableList1 = mutableListOf('a', 'b'),
                charMutableList2 = mutableListOf('c', 'd'),
                charMutableList3 = null,
                charMutableList4 = mutableListOf('e', null),
                charMutableList5 = mutableListOf('f', null),
                charMutableList6 = null,

                floatMutableList1 = mutableListOf(1F, 2F),
                floatMutableList2 = mutableListOf(3F, 4F),
                floatMutableList3 = null,
                floatMutableList4 = mutableListOf(5F, null),
                floatMutableList5 = mutableListOf(6F, null),
                floatMutableList6 = null,

                doubleMutableList1 = mutableListOf(1.0, 2.0),
                doubleMutableList2 = mutableListOf(3.0, 4.0),
                doubleMutableList3 = null,
                doubleMutableList4 = mutableListOf(5.0, null),
                doubleMutableList5 = mutableListOf(6.0, null),
                doubleMutableList6 = null,

                booleanMutableList1 = mutableListOf(false, true),
                booleanMutableList2 = mutableListOf(true, false),
                booleanMutableList3 = null,
                booleanMutableList4 = mutableListOf(false, null),
                booleanMutableList5 = mutableListOf(true, null),
                booleanMutableList6 = null,

                stringMutableList1 = mutableListOf("a", "b"),
                stringMutableList2 = mutableListOf("c", "d"),
                stringMutableList3 = null,
                stringMutableList4 = mutableListOf("e", null),
                stringMutableList5 = mutableListOf("f", null),
                stringMutableList6 = null,

                parcelableMutableList1 = mutableListOf(Other(a = 1), Other(a = 2)),
                parcelableMutableList2 = mutableListOf(Other(a = 3), Other(a = 4)),
                parcelableMutableList3 = null,
                parcelableMutableList4 = mutableListOf(Other(a = 5), null),
                parcelableMutableList5 = mutableListOf(Other(a = 6), null),
                parcelableMutableList6 = null,

                intSet1 = setOf(1, 2),
                intSet2 = setOf(3, 4),
                intSet3 = null,
                intSet4 = setOf(5, null),
                intSet5 = setOf(6, null),
                intSet6 = null,

                longSet1 = setOf(1L, 2L),
                longSet2 = setOf(3L, 4L),
                longSet3 = null,
                longSet4 = setOf(5L, null),
                longSet5 = setOf(6L, null),
                longSet6 = null,

                shortSet1 = setOf(1.toShort(), 2.toShort()),
                shortSet2 = setOf(3.toShort(), 4.toShort()),
                shortSet3 = null,
                shortSet4 = setOf(5.toShort(), null),
                shortSet5 = setOf(6.toShort(), null),
                shortSet6 = null,

                byteSet1 = setOf(1.toByte(), 2.toByte()),
                byteSet2 = setOf(3.toByte(), 4.toByte()),
                byteSet3 = null,
                byteSet4 = setOf(5.toByte(), null),
                byteSet5 = setOf(6.toByte(), null),
                byteSet6 = null,

                charSet1 = setOf('a', 'b'),
                charSet2 = setOf('c', 'd'),
                charSet3 = null,
                charSet4 = setOf('e', null),
                charSet5 = setOf('f', null),
                charSet6 = null,

                floatSet1 = setOf(1F, 2F),
                floatSet2 = setOf(3F, 4F),
                floatSet3 = null,
                floatSet4 = setOf(5F, null),
                floatSet5 = setOf(6F, null),
                floatSet6 = null,

                doubleSet1 = setOf(1.0, 2.0),
                doubleSet2 = setOf(3.0, 4.0),
                doubleSet3 = null,
                doubleSet4 = setOf(5.0, null),
                doubleSet5 = setOf(6.0, null),
                doubleSet6 = null,

                booleanSet1 = setOf(false, true),
                booleanSet2 = setOf(true, false),
                booleanSet3 = null,
                booleanSet4 = setOf(false, null),
                booleanSet5 = setOf(true, null),
                booleanSet6 = null,

                stringSet1 = setOf("a", "b"),
                stringSet2 = setOf("c", "d"),
                stringSet3 = null,
                stringSet4 = setOf("e", null),
                stringSet5 = setOf("f", null),
                stringSet6 = null,

                parcelableSet1 = setOf(Other(a = 1), Other(a = 2)),
                parcelableSet2 = setOf(Other(a = 3), Other(a = 4)),
                parcelableSet3 = null,
                parcelableSet4 = setOf(Other(a = 5), null),
                parcelableSet5 = setOf(Other(a = 6), null),
                parcelableSet6 = null,

                intMutableSet1 = mutableSetOf(1, 2),
                intMutableSet2 = mutableSetOf(3, 4),
                intMutableSet3 = null,
                intMutableSet4 = mutableSetOf(5, null),
                intMutableSet5 = mutableSetOf(6, null),
                intMutableSet6 = null,

                longMutableSet1 = mutableSetOf(1L, 2L),
                longMutableSet2 = mutableSetOf(3L, 4L),
                longMutableSet3 = null,
                longMutableSet4 = mutableSetOf(5L, null),
                longMutableSet5 = mutableSetOf(6L, null),
                longMutableSet6 = null,

                shortMutableSet1 = mutableSetOf(1.toShort(), 2.toShort()),
                shortMutableSet2 = mutableSetOf(3.toShort(), 4.toShort()),
                shortMutableSet3 = null,
                shortMutableSet4 = mutableSetOf(5.toShort(), null),
                shortMutableSet5 = mutableSetOf(6.toShort(), null),
                shortMutableSet6 = null,

                byteMutableSet1 = mutableSetOf(1.toByte(), 2.toByte()),
                byteMutableSet2 = mutableSetOf(3.toByte(), 4.toByte()),
                byteMutableSet3 = null,
                byteMutableSet4 = mutableSetOf(5.toByte(), null),
                byteMutableSet5 = mutableSetOf(6.toByte(), null),
                byteMutableSet6 = null,

                charMutableSet1 = mutableSetOf('a', 'b'),
                charMutableSet2 = mutableSetOf('c', 'd'),
                charMutableSet3 = null,
                charMutableSet4 = mutableSetOf('e', null),
                charMutableSet5 = mutableSetOf('f', null),
                charMutableSet6 = null,

                floatMutableSet1 = mutableSetOf(1F, 2F),
                floatMutableSet2 = mutableSetOf(3F, 4F),
                floatMutableSet3 = null,
                floatMutableSet4 = mutableSetOf(5F, null),
                floatMutableSet5 = mutableSetOf(6F, null),
                floatMutableSet6 = null,

                doubleMutableSet1 = mutableSetOf(1.0, 2.0),
                doubleMutableSet2 = mutableSetOf(3.0, 4.0),
                doubleMutableSet3 = null,
                doubleMutableSet4 = mutableSetOf(5.0, null),
                doubleMutableSet5 = mutableSetOf(6.0, null),
                doubleMutableSet6 = null,

                booleanMutableSet1 = mutableSetOf(false, true),
                booleanMutableSet2 = mutableSetOf(true, false),
                booleanMutableSet3 = null,
                booleanMutableSet4 = mutableSetOf(false, null),
                booleanMutableSet5 = mutableSetOf(true, null),
                booleanMutableSet6 = null,

                stringMutableSet1 = mutableSetOf("a", "b"),
                stringMutableSet2 = mutableSetOf("c", "d"),
                stringMutableSet3 = null,
                stringMutableSet4 = mutableSetOf("e", null),
                stringMutableSet5 = mutableSetOf("f", null),
                stringMutableSet6 = null,

                parcelableMutableSet1 = mutableSetOf(Other(a = 1), Other(a = 2)),
                parcelableMutableSet2 = mutableSetOf(Other(a = 3), Other(a = 4)),
                parcelableMutableSet3 = null,
                parcelableMutableSet4 = mutableSetOf(Other(a = 5), null),
                parcelableMutableSet5 = mutableSetOf(Other(a = 6), null),
                parcelableMutableSet6 = null,

                intMap1 = mapOf(1 to 11, 2 to 22),
                intMap2 = mapOf(3 to 33, 4 to 44),
                intMap3 = null,
                intMap4 = mapOf(5 to 55, null to null),
                intMap5 = mapOf(6 to 66, null to null),
                intMap6 = null,

                longMap1 = mapOf(1L to 11L, 2L to 22L),
                longMap2 = mapOf(3L to 33L, 4L to 44L),
                longMap3 = null,
                longMap4 = mapOf(5L to 55L, null to null),
                longMap5 = mapOf(6L to 66L, null to null),
                longMap6 = null,

                shortMap1 = mapOf(1.toShort() to 11.toShort(), 2.toShort() to 22.toShort()),
                shortMap2 = mapOf(3.toShort() to 33.toShort(), 4.toShort() to 44.toShort()),
                shortMap3 = null,
                shortMap4 = mapOf(5.toShort() to 55.toShort(), null to null),
                shortMap5 = mapOf(6.toShort() to 66.toShort(), null to null),
                shortMap6 = null,

                byteMap1 = mapOf(1.toByte() to 11.toByte(), 2.toByte() to 22.toByte()),
                byteMap2 = mapOf(3.toByte() to 33.toByte(), 4.toByte() to 44.toByte()),
                byteMap3 = null,
                byteMap4 = mapOf(5.toByte() to 55.toByte(), null to null),
                byteMap5 = mapOf(6.toByte() to 66.toByte(), null to null),
                byteMap6 = null,

                charMap1 = mapOf('a' to 'A', 'b' to 'B'),
                charMap2 = mapOf('c' to 'C', 'd' to 'D'),
                charMap3 = null,
                charMap4 = mapOf('e' to 'E', null to null),
                charMap5 = mapOf('f' to 'F', null to null),
                charMap6 = null,

                floatMap1 = mapOf(1F to 11F, 2F to 22F),
                floatMap2 = mapOf(3F to 33F, 4F to 44F),
                floatMap3 = null,
                floatMap4 = mapOf(5F to 55F, null to null),
                floatMap5 = mapOf(6F to 66F, null to null),
                floatMap6 = null,

                doubleMap1 = mapOf(1.0 to 11.0, 2.0 to 22.0),
                doubleMap2 = mapOf(3.0 to 33.0, 4.0 to 44.0),
                doubleMap3 = null,
                doubleMap4 = mapOf(5.0 to 55.0, null to null),
                doubleMap5 = mapOf(6.0 to 66.0, null to null),
                doubleMap6 = null,

                booleanMap1 = mapOf(false to true, true to false),
                booleanMap2 = mapOf(true to false, false to true),
                booleanMap3 = null,
                booleanMap4 = mapOf(false to true, null to null),
                booleanMap5 = mapOf(true to false, null to null),
                booleanMap6 = null,

                stringMap1 = mapOf("a" to "A", "b" to "B"),
                stringMap2 = mapOf("c" to "C", "d" to "D"),
                stringMap3 = null,
                stringMap4 = mapOf("e" to "E", null to null),
                stringMap5 = mapOf("f" to "F", null to null),
                stringMap6 = null,

                parcelableMap1 = mapOf(Other(a = 1) to Other(a = 11), Other(a = 2) to Other(a = 22)),
                parcelableMap2 = mapOf(Other(a = 3) to Other(a = 33), Other(a = 4) to Other(a = 44)),
                parcelableMap3 = null,
                parcelableMap4 = mapOf(Other(a = 5) to Other(a = 55), null to null),
                parcelableMap5 = mapOf(Other(a = 6) to Other(a = 66), null to null),
                parcelableMap6 = null,

                intMutableMap1 = mutableMapOf(1 to 11, 2 to 22),
                intMutableMap2 = mutableMapOf(3 to 33, 4 to 44),
                intMutableMap3 = null,
                intMutableMap4 = mutableMapOf(5 to 55, null to null),
                intMutableMap5 = mutableMapOf(6 to 66, null to null),
                intMutableMap6 = null,

                longMutableMap1 = mutableMapOf(1L to 11L, 2L to 22L),
                longMutableMap2 = mutableMapOf(3L to 33L, 4L to 44L),
                longMutableMap3 = null,
                longMutableMap4 = mutableMapOf(5L to 55L, null to null),
                longMutableMap5 = mutableMapOf(6L to 66L, null to null),
                longMutableMap6 = null,

                shortMutableMap1 = mutableMapOf(1.toShort() to 11.toShort(), 2.toShort() to 22.toShort()),
                shortMutableMap2 = mutableMapOf(3.toShort() to 33.toShort(), 4.toShort() to 44.toShort()),
                shortMutableMap3 = null,
                shortMutableMap4 = mutableMapOf(5.toShort() to 55.toShort(), null to null),
                shortMutableMap5 = mutableMapOf(6.toShort() to 66.toShort(), null to null),
                shortMutableMap6 = null,

                byteMutableMap1 = mutableMapOf(1.toByte() to 11.toByte(), 2.toByte() to 22.toByte()),
                byteMutableMap2 = mutableMapOf(3.toByte() to 33.toByte(), 4.toByte() to 44.toByte()),
                byteMutableMap3 = null,
                byteMutableMap4 = mutableMapOf(5.toByte() to 55.toByte(), null to null),
                byteMutableMap5 = mutableMapOf(6.toByte() to 66.toByte(), null to null),
                byteMutableMap6 = null,

                charMutableMap1 = mutableMapOf('a' to 'A', 'b' to 'B'),
                charMutableMap2 = mutableMapOf('c' to 'C', 'd' to 'D'),
                charMutableMap3 = null,
                charMutableMap4 = mutableMapOf('e' to 'E', null to null),
                charMutableMap5 = mutableMapOf('f' to 'F', null to null),
                charMutableMap6 = null,

                floatMutableMap1 = mutableMapOf(1F to 11F, 2F to 22F),
                floatMutableMap2 = mutableMapOf(3F to 33F, 4F to 44F),
                floatMutableMap3 = null,
                floatMutableMap4 = mutableMapOf(5F to 55F, null to null),
                floatMutableMap5 = mutableMapOf(6F to 66F, null to null),
                floatMutableMap6 = null,

                doubleMutableMap1 = mutableMapOf(1.0 to 11.0, 2.0 to 22.0),
                doubleMutableMap2 = mutableMapOf(3.0 to 33.0, 4.0 to 44.0),
                doubleMutableMap3 = null,
                doubleMutableMap4 = mutableMapOf(5.0 to 55.0, null to null),
                doubleMutableMap5 = mutableMapOf(6.0 to 66.0, null to null),
                doubleMutableMap6 = null,

                booleanMutableMap1 = mutableMapOf(false to true, true to false),
                booleanMutableMap2 = mutableMapOf(true to false, false to true),
                booleanMutableMap3 = null,
                booleanMutableMap4 = mutableMapOf(false to true, null to null),
                booleanMutableMap5 = mutableMapOf(true to false, null to null),
                booleanMutableMap6 = null,

                stringMutableMap1 = mutableMapOf("a" to "A", "b" to "B"),
                stringMutableMap2 = mutableMapOf("c" to "C", "d" to "D"),
                stringMutableMap3 = null,
                stringMutableMap4 = mutableMapOf("e" to "E", null to null),
                stringMutableMap5 = mutableMapOf("f" to "F", null to null),
                stringMutableMap6 = null,

                parcelableMutableMap1 = mutableMapOf(Other(a = 1) to Other(a = 11), Other(a = 2) to Other(a = 22)),
                parcelableMutableMap2 = mutableMapOf(Other(a = 3) to Other(a = 33), Other(a = 4) to Other(a = 44)),
                parcelableMutableMap3 = null,
                parcelableMutableMap4 = mutableMapOf(Other(a = 5) to Other(a = 55), null to null),
                parcelableMutableMap5 = mutableMapOf(Other(a = 6) to Other(a = 66), null to null),
                parcelableMutableMap6 = null,
            )

        val some2 = some.encodeAndDecode()

        assertEquals(some, some2)
    }

    private fun Parcelable.encodeAndDecode(): Parcelable {
        val arch = NSKeyedArchiver(requiringSecureCoding = true)
        arch.encodeParcelable(value = this, key = "key")
        val data = arch.encodedData

        val unarch = NSKeyedUnarchiver(forReadingWithData = data)
        unarch.requiresSecureCoding = true

        return unarch.decodeParcelable("key")
    }

    @Parcelize
    private data class Other(
        val a: Int
    ) : SomeClass()

    @Parcelize
    private object Obj : Parcelable

    private enum class SomeEnum {
        A, B
    }

    @TypeParceler<NotParcelable2, NotParcelable2Parceler>
    @Parcelize
    private data class Some(
        val i1: Int,
        val i2: Int?,
        val i3: Int?,
        val l1: Long,
        val l2: Long?,
        val l3: Long?,
        val f1: Float,
        val f2: Float?,
        val f3: Float?,
        val d1: Double,
        val d2: Double?,
        val d3: Double?,
        val h1: Short,
        val h2: Short?,
        val h3: Short?,
        val b1: Byte,
        val b2: Byte?,
        val b3: Byte?,
        val c1: Char,
        val c2: Char?,
        val c3: Char?,
        val z1: Boolean,
        val z2: Boolean?,
        val z3: Boolean?,
        val s1: String,
        val s2: String?,
        val s3: String?,
        val parcelable1: Parcelable,
        val parcelable2: Parcelable?,
        val parcelable3: Parcelable?,
        val other1: Other,
        val other2: Other?,
        val other3: Other?,
        val obj1: Obj,
        val obj2: Obj?,
        val obj3: Obj?,
        val enum1: SomeEnum,
        val enum2: SomeEnum?,
        val enum3: SomeEnum?,
        val notParcelable11: @WriteWith<NotParcelable1Parceler> NotParcelable1,
        val notParcelable12: @WriteWith<NotParcelable1Parceler> NotParcelable1?,
        val notParcelable13: @WriteWith<NotParcelable1Parceler> NotParcelable1?,
        val notParcelable21: NotParcelable2,
        val notParcelable22: NotParcelable2?,
        val notParcelable23: NotParcelable2?,

        val intList1: List<Int>,
        val intList2: List<Int>?,
        val intList3: List<Int>?,
        val intList4: List<Int?>,
        val intList5: List<Int?>?,
        val intList6: List<Int?>?,

        val longList1: List<Long>,
        val longList2: List<Long>?,
        val longList3: List<Long>?,
        val longList4: List<Long?>,
        val longList5: List<Long?>?,
        val longList6: List<Long?>?,

        val shortList1: List<Short>,
        val shortList2: List<Short>?,
        val shortList3: List<Short>?,
        val shortList4: List<Short?>,
        val shortList5: List<Short?>?,
        val shortList6: List<Short?>?,

        val byteList1: List<Byte>,
        val byteList2: List<Byte>?,
        val byteList3: List<Byte>?,
        val byteList4: List<Byte?>,
        val byteList5: List<Byte?>?,
        val byteList6: List<Byte?>?,

        val charList1: List<Char>,
        val charList2: List<Char>?,
        val charList3: List<Char>?,
        val charList4: List<Char?>,
        val charList5: List<Char?>?,
        val charList6: List<Char?>?,

        val floatList1: List<Float>,
        val floatList2: List<Float>?,
        val floatList3: List<Float>?,
        val floatList4: List<Float?>,
        val floatList5: List<Float?>?,
        val floatList6: List<Float?>?,

        val doubleList1: List<Double>,
        val doubleList2: List<Double>?,
        val doubleList3: List<Double>?,
        val doubleList4: List<Double?>,
        val doubleList5: List<Double?>?,
        val doubleList6: List<Double?>?,

        val booleanList1: List<Boolean>,
        val booleanList2: List<Boolean>?,
        val booleanList3: List<Boolean>?,
        val booleanList4: List<Boolean?>,
        val booleanList5: List<Boolean?>?,
        val booleanList6: List<Boolean?>?,

        val stringList1: List<String>,
        val stringList2: List<String>?,
        val stringList3: List<String>?,
        val stringList4: List<String?>,
        val stringList5: List<String?>?,
        val stringList6: List<String?>?,

        val parcelableList1: List<Other>,
        val parcelableList2: List<Other>?,
        val parcelableList3: List<Other>?,
        val parcelableList4: List<Other?>,
        val parcelableList5: List<Other?>?,
        val parcelableList6: List<Other?>?,

        val intMutableList1: MutableList<Int>,
        val intMutableList2: MutableList<Int>?,
        val intMutableList3: MutableList<Int>?,
        val intMutableList4: MutableList<Int?>,
        val intMutableList5: MutableList<Int?>?,
        val intMutableList6: MutableList<Int?>?,

        val longMutableList1: MutableList<Long>,
        val longMutableList2: MutableList<Long>?,
        val longMutableList3: MutableList<Long>?,
        val longMutableList4: MutableList<Long?>,
        val longMutableList5: MutableList<Long?>?,
        val longMutableList6: MutableList<Long?>?,

        val shortMutableList1: MutableList<Short>,
        val shortMutableList2: MutableList<Short>?,
        val shortMutableList3: MutableList<Short>?,
        val shortMutableList4: MutableList<Short?>,
        val shortMutableList5: MutableList<Short?>?,
        val shortMutableList6: MutableList<Short?>?,

        val byteMutableList1: MutableList<Byte>,
        val byteMutableList2: MutableList<Byte>?,
        val byteMutableList3: MutableList<Byte>?,
        val byteMutableList4: MutableList<Byte?>,
        val byteMutableList5: MutableList<Byte?>?,
        val byteMutableList6: MutableList<Byte?>?,

        val charMutableList1: MutableList<Char>,
        val charMutableList2: MutableList<Char>?,
        val charMutableList3: MutableList<Char>?,
        val charMutableList4: MutableList<Char?>,
        val charMutableList5: MutableList<Char?>?,
        val charMutableList6: MutableList<Char?>?,

        val floatMutableList1: MutableList<Float>,
        val floatMutableList2: MutableList<Float>?,
        val floatMutableList3: MutableList<Float>?,
        val floatMutableList4: MutableList<Float?>,
        val floatMutableList5: MutableList<Float?>?,
        val floatMutableList6: MutableList<Float?>?,

        val doubleMutableList1: MutableList<Double>,
        val doubleMutableList2: MutableList<Double>?,
        val doubleMutableList3: MutableList<Double>?,
        val doubleMutableList4: MutableList<Double?>,
        val doubleMutableList5: MutableList<Double?>?,
        val doubleMutableList6: MutableList<Double?>?,

        val booleanMutableList1: MutableList<Boolean>,
        val booleanMutableList2: MutableList<Boolean>?,
        val booleanMutableList3: MutableList<Boolean>?,
        val booleanMutableList4: MutableList<Boolean?>,
        val booleanMutableList5: MutableList<Boolean?>?,
        val booleanMutableList6: MutableList<Boolean?>?,

        val stringMutableList1: MutableList<String>,
        val stringMutableList2: MutableList<String>?,
        val stringMutableList3: MutableList<String>?,
        val stringMutableList4: MutableList<String?>,
        val stringMutableList5: MutableList<String?>?,
        val stringMutableList6: MutableList<String?>?,

        val parcelableMutableList1: MutableList<Other>,
        val parcelableMutableList2: MutableList<Other>?,
        val parcelableMutableList3: MutableList<Other>?,
        val parcelableMutableList4: MutableList<Other?>,
        val parcelableMutableList5: MutableList<Other?>?,
        val parcelableMutableList6: MutableList<Other?>?,

        val intSet1: Set<Int>,
        val intSet2: Set<Int>?,
        val intSet3: Set<Int>?,
        val intSet4: Set<Int?>,
        val intSet5: Set<Int?>?,
        val intSet6: Set<Int?>?,

        val longSet1: Set<Long>,
        val longSet2: Set<Long>?,
        val longSet3: Set<Long>?,
        val longSet4: Set<Long?>,
        val longSet5: Set<Long?>?,
        val longSet6: Set<Long?>?,

        val shortSet1: Set<Short>,
        val shortSet2: Set<Short>?,
        val shortSet3: Set<Short>?,
        val shortSet4: Set<Short?>,
        val shortSet5: Set<Short?>?,
        val shortSet6: Set<Short?>?,

        val byteSet1: Set<Byte>,
        val byteSet2: Set<Byte>?,
        val byteSet3: Set<Byte>?,
        val byteSet4: Set<Byte?>,
        val byteSet5: Set<Byte?>?,
        val byteSet6: Set<Byte?>?,

        val charSet1: Set<Char>,
        val charSet2: Set<Char>?,
        val charSet3: Set<Char>?,
        val charSet4: Set<Char?>,
        val charSet5: Set<Char?>?,
        val charSet6: Set<Char?>?,

        val floatSet1: Set<Float>,
        val floatSet2: Set<Float>?,
        val floatSet3: Set<Float>?,
        val floatSet4: Set<Float?>,
        val floatSet5: Set<Float?>?,
        val floatSet6: Set<Float?>?,

        val doubleSet1: Set<Double>,
        val doubleSet2: Set<Double>?,
        val doubleSet3: Set<Double>?,
        val doubleSet4: Set<Double?>,
        val doubleSet5: Set<Double?>?,
        val doubleSet6: Set<Double?>?,

        val booleanSet1: Set<Boolean>,
        val booleanSet2: Set<Boolean>?,
        val booleanSet3: Set<Boolean>?,
        val booleanSet4: Set<Boolean?>,
        val booleanSet5: Set<Boolean?>?,
        val booleanSet6: Set<Boolean?>?,

        val stringSet1: Set<String>,
        val stringSet2: Set<String>?,
        val stringSet3: Set<String>?,
        val stringSet4: Set<String?>,
        val stringSet5: Set<String?>?,
        val stringSet6: Set<String?>?,

        val parcelableSet1: Set<Other>,
        val parcelableSet2: Set<Other>?,
        val parcelableSet3: Set<Other>?,
        val parcelableSet4: Set<Other?>,
        val parcelableSet5: Set<Other?>?,
        val parcelableSet6: Set<Other?>?,

        val intMutableSet1: MutableSet<Int>,
        val intMutableSet2: MutableSet<Int>?,
        val intMutableSet3: MutableSet<Int>?,
        val intMutableSet4: MutableSet<Int?>,
        val intMutableSet5: MutableSet<Int?>?,
        val intMutableSet6: MutableSet<Int?>?,

        val longMutableSet1: MutableSet<Long>,
        val longMutableSet2: MutableSet<Long>?,
        val longMutableSet3: MutableSet<Long>?,
        val longMutableSet4: MutableSet<Long?>,
        val longMutableSet5: MutableSet<Long?>?,
        val longMutableSet6: MutableSet<Long?>?,

        val shortMutableSet1: MutableSet<Short>,
        val shortMutableSet2: MutableSet<Short>?,
        val shortMutableSet3: MutableSet<Short>?,
        val shortMutableSet4: MutableSet<Short?>,
        val shortMutableSet5: MutableSet<Short?>?,
        val shortMutableSet6: MutableSet<Short?>?,

        val byteMutableSet1: MutableSet<Byte>,
        val byteMutableSet2: MutableSet<Byte>?,
        val byteMutableSet3: MutableSet<Byte>?,
        val byteMutableSet4: MutableSet<Byte?>,
        val byteMutableSet5: MutableSet<Byte?>?,
        val byteMutableSet6: MutableSet<Byte?>?,

        val charMutableSet1: MutableSet<Char>,
        val charMutableSet2: MutableSet<Char>?,
        val charMutableSet3: MutableSet<Char>?,
        val charMutableSet4: MutableSet<Char?>,
        val charMutableSet5: MutableSet<Char?>?,
        val charMutableSet6: MutableSet<Char?>?,

        val floatMutableSet1: MutableSet<Float>,
        val floatMutableSet2: MutableSet<Float>?,
        val floatMutableSet3: MutableSet<Float>?,
        val floatMutableSet4: MutableSet<Float?>,
        val floatMutableSet5: MutableSet<Float?>?,
        val floatMutableSet6: MutableSet<Float?>?,

        val doubleMutableSet1: MutableSet<Double>,
        val doubleMutableSet2: MutableSet<Double>?,
        val doubleMutableSet3: MutableSet<Double>?,
        val doubleMutableSet4: MutableSet<Double?>,
        val doubleMutableSet5: MutableSet<Double?>?,
        val doubleMutableSet6: MutableSet<Double?>?,

        val booleanMutableSet1: MutableSet<Boolean>,
        val booleanMutableSet2: MutableSet<Boolean>?,
        val booleanMutableSet3: MutableSet<Boolean>?,
        val booleanMutableSet4: MutableSet<Boolean?>,
        val booleanMutableSet5: MutableSet<Boolean?>?,
        val booleanMutableSet6: MutableSet<Boolean?>?,

        val stringMutableSet1: MutableSet<String>,
        val stringMutableSet2: MutableSet<String>?,
        val stringMutableSet3: MutableSet<String>?,
        val stringMutableSet4: MutableSet<String?>,
        val stringMutableSet5: MutableSet<String?>?,
        val stringMutableSet6: MutableSet<String?>?,

        val parcelableMutableSet1: MutableSet<Other>,
        val parcelableMutableSet2: MutableSet<Other>?,
        val parcelableMutableSet3: MutableSet<Other>?,
        val parcelableMutableSet4: MutableSet<Other?>,
        val parcelableMutableSet5: MutableSet<Other?>?,
        val parcelableMutableSet6: MutableSet<Other?>?,

        val intMap1: Map<Int, Int>,
        val intMap2: Map<Int, Int>?,
        val intMap3: Map<Int, Int>?,
        val intMap4: Map<Int?, Int?>,
        val intMap5: Map<Int?, Int?>?,
        val intMap6: Map<Int?, Int?>?,

        val longMap1: Map<Long, Long>,
        val longMap2: Map<Long, Long>?,
        val longMap3: Map<Long, Long>?,
        val longMap4: Map<Long?, Long?>,
        val longMap5: Map<Long?, Long?>?,
        val longMap6: Map<Long?, Long?>?,

        val shortMap1: Map<Short, Short>,
        val shortMap2: Map<Short, Short>?,
        val shortMap3: Map<Short, Short>?,
        val shortMap4: Map<Short?, Short?>,
        val shortMap5: Map<Short?, Short?>?,
        val shortMap6: Map<Short?, Short?>?,

        val byteMap1: Map<Byte, Byte>,
        val byteMap2: Map<Byte, Byte>?,
        val byteMap3: Map<Byte, Byte>?,
        val byteMap4: Map<Byte?, Byte?>,
        val byteMap5: Map<Byte?, Byte?>?,
        val byteMap6: Map<Byte?, Byte?>?,

        val charMap1: Map<Char, Char>,
        val charMap2: Map<Char, Char>?,
        val charMap3: Map<Char, Char>?,
        val charMap4: Map<Char?, Char?>,
        val charMap5: Map<Char?, Char?>?,
        val charMap6: Map<Char?, Char?>?,

        val floatMap1: Map<Float, Float>,
        val floatMap2: Map<Float, Float>?,
        val floatMap3: Map<Float, Float>?,
        val floatMap4: Map<Float?, Float?>,
        val floatMap5: Map<Float?, Float?>?,
        val floatMap6: Map<Float?, Float?>?,

        val doubleMap1: Map<Double, Double>,
        val doubleMap2: Map<Double, Double>?,
        val doubleMap3: Map<Double, Double>?,
        val doubleMap4: Map<Double?, Double?>,
        val doubleMap5: Map<Double?, Double?>?,
        val doubleMap6: Map<Double?, Double?>?,

        val booleanMap1: Map<Boolean, Boolean>,
        val booleanMap2: Map<Boolean, Boolean>?,
        val booleanMap3: Map<Boolean, Boolean>?,
        val booleanMap4: Map<Boolean?, Boolean?>,
        val booleanMap5: Map<Boolean?, Boolean?>?,
        val booleanMap6: Map<Boolean?, Boolean?>?,

        val stringMap1: Map<String, String>,
        val stringMap2: Map<String, String>?,
        val stringMap3: Map<String, String>?,
        val stringMap4: Map<String?, String?>,
        val stringMap5: Map<String?, String?>?,
        val stringMap6: Map<String?, String?>?,

        val parcelableMap1: Map<Other, Other>,
        val parcelableMap2: Map<Other, Other>?,
        val parcelableMap3: Map<Other, Other>?,
        val parcelableMap4: Map<Other?, Other?>,
        val parcelableMap5: Map<Other?, Other?>?,
        val parcelableMap6: Map<Other?, Other?>?,

        val intMutableMap1: MutableMap<Int, Int>,
        val intMutableMap2: MutableMap<Int, Int>?,
        val intMutableMap3: MutableMap<Int, Int>?,
        val intMutableMap4: MutableMap<Int?, Int?>,
        val intMutableMap5: MutableMap<Int?, Int?>?,
        val intMutableMap6: MutableMap<Int?, Int?>?,

        val longMutableMap1: MutableMap<Long, Long>,
        val longMutableMap2: MutableMap<Long, Long>?,
        val longMutableMap3: MutableMap<Long, Long>?,
        val longMutableMap4: MutableMap<Long?, Long?>,
        val longMutableMap5: MutableMap<Long?, Long?>?,
        val longMutableMap6: MutableMap<Long?, Long?>?,

        val shortMutableMap1: MutableMap<Short, Short>,
        val shortMutableMap2: MutableMap<Short, Short>?,
        val shortMutableMap3: MutableMap<Short, Short>?,
        val shortMutableMap4: MutableMap<Short?, Short?>,
        val shortMutableMap5: MutableMap<Short?, Short?>?,
        val shortMutableMap6: MutableMap<Short?, Short?>?,

        val byteMutableMap1: MutableMap<Byte, Byte>,
        val byteMutableMap2: MutableMap<Byte, Byte>?,
        val byteMutableMap3: MutableMap<Byte, Byte>?,
        val byteMutableMap4: MutableMap<Byte?, Byte?>,
        val byteMutableMap5: MutableMap<Byte?, Byte?>?,
        val byteMutableMap6: MutableMap<Byte?, Byte?>?,

        val charMutableMap1: MutableMap<Char, Char>,
        val charMutableMap2: MutableMap<Char, Char>?,
        val charMutableMap3: MutableMap<Char, Char>?,
        val charMutableMap4: MutableMap<Char?, Char?>,
        val charMutableMap5: MutableMap<Char?, Char?>?,
        val charMutableMap6: MutableMap<Char?, Char?>?,

        val floatMutableMap1: MutableMap<Float, Float>,
        val floatMutableMap2: MutableMap<Float, Float>?,
        val floatMutableMap3: MutableMap<Float, Float>?,
        val floatMutableMap4: MutableMap<Float?, Float?>,
        val floatMutableMap5: MutableMap<Float?, Float?>?,
        val floatMutableMap6: MutableMap<Float?, Float?>?,

        val doubleMutableMap1: MutableMap<Double, Double>,
        val doubleMutableMap2: MutableMap<Double, Double>?,
        val doubleMutableMap3: MutableMap<Double, Double>?,
        val doubleMutableMap4: MutableMap<Double?, Double?>,
        val doubleMutableMap5: MutableMap<Double?, Double?>?,
        val doubleMutableMap6: MutableMap<Double?, Double?>?,

        val booleanMutableMap1: MutableMap<Boolean, Boolean>,
        val booleanMutableMap2: MutableMap<Boolean, Boolean>?,
        val booleanMutableMap3: MutableMap<Boolean, Boolean>?,
        val booleanMutableMap4: MutableMap<Boolean?, Boolean?>,
        val booleanMutableMap5: MutableMap<Boolean?, Boolean?>?,
        val booleanMutableMap6: MutableMap<Boolean?, Boolean?>?,

        val stringMutableMap1: MutableMap<String, String>,
        val stringMutableMap2: MutableMap<String, String>?,
        val stringMutableMap3: MutableMap<String, String>?,
        val stringMutableMap4: MutableMap<String?, String?>,
        val stringMutableMap5: MutableMap<String?, String?>?,
        val stringMutableMap6: MutableMap<String?, String?>?,

        val parcelableMutableMap1: MutableMap<Other, Other>,
        val parcelableMutableMap2: MutableMap<Other, Other>?,
        val parcelableMutableMap3: MutableMap<Other, Other>?,
        val parcelableMutableMap4: MutableMap<Other?, Other?>,
        val parcelableMutableMap5: MutableMap<Other?, Other?>?,
        val parcelableMutableMap6: MutableMap<Other?, Other?>?,
    ) : SomeInterface {

        val someProperty: List<Any> = listOf(Any())
    }

    private interface SomeInterface : Parcelable

    private abstract class SomeClass : Parcelable

    private data class NotParcelable1(
        val value: Int,
    )

    private object NotParcelable1Parceler : Parceler<NotParcelable1> {
        override fun create(coder: NSCoder): NotParcelable1 =
            NotParcelable1(value = coder.decodeInt32ForKey(key = "value"))

        override fun NotParcelable1.write(coder: NSCoder) {
            coder.encodeInt32(value = value, forKey = "value")
        }
    }

    private data class NotParcelable2(
        val value: Int,
    )

    private object NotParcelable2Parceler : Parceler<NotParcelable2> {
        override fun create(coder: NSCoder): NotParcelable2 =
            NotParcelable2(value = coder.decodeInt32ForKey(key = "value"))

        override fun NotParcelable2.write(coder: NSCoder) {
            coder.encodeInt32(value = value, forKey = "value")
        }
    }
}
