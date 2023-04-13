package com.arkivanov.parcelize.sample

@Parcelize
private data class Other(
    val a: Int
) : SomeClass()

@Parcelize
private object Obj : Parcelable

private enum class SomeEnum {
    A, B
}

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

    val intList1: List<Int>,
    val longList1: List<Long>,
    val shortList1: List<Short>,
    val byteList1: List<Byte>,
    val charList1: List<Char>,
    val floatList1: List<Float>,
    val doubleList1: List<Double>,
    val booleanList1: List<Boolean>,
    val stringList1: List<String>,
    val parcelableList1: List<Other>,

    val intMap1: Map<Int, Int>,
    val longMap1: Map<Long, Long>,
    val shortMap1: Map<Short, Short>,
    val byteMap1: Map<Byte, Byte>,
    val charMap1: Map<Char, Char>,
    val floatMap1: Map<Float, Float>,
    val doubleMap1: Map<Double, Double>,
    val booleanMap1: Map<Boolean, Boolean>,
    val stringMap1: Map<String, String>,
    val parcelableMap1: Map<Other, Other>,
) : SomeInterface {

    val someProperty: List<Any> = listOf(Any())
}

private interface SomeInterface : Parcelable

private abstract class SomeClass : Parcelable