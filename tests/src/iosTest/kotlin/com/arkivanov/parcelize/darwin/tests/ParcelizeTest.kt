package com.arkivanov.parcelize.darwin.tests

import com.arkivanov.parcelize.darwin.DecodedValue
import com.arkivanov.parcelize.darwin.Parcelable
import com.arkivanov.parcelize.darwin.Parcelize
import com.arkivanov.parcelize.darwin.decodeParcelable
import com.arkivanov.parcelize.darwin.encodeParcelable
import kotlinx.cinterop.Arena
import kotlinx.cinterop.readBytes
import kotlinx.cinterop.toCValues
import platform.Foundation.NSData
import platform.Foundation.NSKeyedArchiver
import platform.Foundation.NSKeyedUnarchiver
import platform.Foundation.dataWithBytes
import kotlin.test.Test
import kotlin.test.assertEquals

class ParcelizeTest {

    @Test
    fun encodes_and_decodes() {
        val some =
            Some(
                s1 = "str",
                s2 = null,
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
                other1 = Other(a = 1),
                other2 = null,
                other3 = Other(a = 3),
                other4 = null,
                obj1 = Obj,
                obj2 = null,
                parcelableList1 = listOf(Other(a = 3), Other(a = 4)),
                parcelableList2 = null,
                parcelableList3 = mutableListOf(Other(a = 5), Other(a = 6)),
                parcelableList4 = null,
                parcelableSet1 = setOf(Other(a = 3), Other(a = 4)),
                parcelableSet2 = null,
                parcelableSet3 = mutableSetOf(Other(a = 5), Other(a = 6)),
                parcelableSet4 = null,
                stringList1 = listOf("a", "b"),
                stringList2 = null,
                stringList3 = mutableListOf("c", "d"),
                stringList4 = null,
                stringSet1 = setOf("aa", "bb"),
                stringSet2 = null,
                stringSet3 = mutableSetOf("cc", "dd"),
                stringSet4 = null,
                map1 = mapOf("a" to Other(1), "b" to Other(2)),
                map2 = null,
                map3 = mutableMapOf("a" to Other(3), "b" to Other(4)),
                map4 = null
            )

        val arch = NSKeyedArchiver(requiringSecureCoding = true)
        arch.encodeParcelable(value = some, key = "some")
        val data = arch.encodedData

        val unarch = NSKeyedUnarchiver(forReadingWithData = data)
        unarch.requiresSecureCoding = true
        val some2 = unarch.decodeParcelable("some")

        assertEquals(some, some2)
    }

    @Parcelize
    private data class Other(
        val a: Int
    ) : SomeClass()

    @Parcelize
    private object Obj : Parcelable

    @Parcelize
    private data class Some(
        val s1: String,
        val s2: String?,
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
        val other1: Other,
        val other2: Other?,
        val other3: Parcelable,
        val other4: Parcelable?,
        val obj1: Obj,
        val obj2: Obj?,
        val parcelableList1: List<Other>,
        val parcelableList2: List<Other>?,
        val parcelableList3: MutableList<Other>?,
        val parcelableList4: MutableList<Other>?,
        val parcelableSet1: Set<Other>,
        val parcelableSet2: Set<Other>?,
        val parcelableSet3: MutableSet<Other>?,
        val parcelableSet4: MutableSet<Other>?,
        val stringList1: List<String>,
        val stringList2: List<String>?,
        val stringList3: MutableList<String>,
        val stringList4: MutableList<String>?,
        val stringSet1: Set<String>,
        val stringSet2: Set<String>?,
        val stringSet3: MutableSet<String>,
        val stringSet4: MutableSet<String>?,
        val map1: Map<String, Other>,
        val map2: Map<String, Other>?,
        val map3: MutableMap<String, Other>,
        val map4: MutableMap<String, Other>?,
    ) : SomeInterface

    private interface SomeInterface : Parcelable

    private abstract class SomeClass : Parcelable
}
