package com.arkivanov.parcelize.darwin

import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.symbols.IrClassSymbol
import org.jetbrains.kotlin.ir.symbols.IrConstructorSymbol
import org.jetbrains.kotlin.ir.symbols.IrSimpleFunctionSymbol
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.classOrNull
import org.jetbrains.kotlin.ir.types.defaultType
import org.jetbrains.kotlin.ir.types.makeNullable
import org.jetbrains.kotlin.ir.util.constructors
import org.jetbrains.kotlin.ir.util.getSimpleFunction
import org.jetbrains.kotlin.name.FqName

interface Symbols {

    val nsStringClass: IrClassSymbol
    val nsLockClass: IrClassSymbol
    val objCClassType: IrType
    val decodedValueType: IrType

    val parcelableType: IrType
    val parcelableNType: IrType
    val iteratorType: IrType
    val iterableType: IrType
    val collectionType: IrType
    val mutableCollectionType: IrType
    val listType: IrType
    val mutableListType: IrType
    val setType: IrType
    val mutableSetType: IrType
    val mapType: IrType
    val mutableMapType: IrType

    val anyNType: IrType
    val unitType: IrType
    val intType: IrType
    val intNType: IrType
    val longType: IrType
    val longNType: IrType
    val shortType: IrType
    val shortNType: IrType
    val byteType: IrType
    val byteNType: IrType
    val charType: IrType
    val charNType: IrType
    val floatType: IrType
    val floatNType: IrType
    val doubleType: IrType
    val doubleNType: IrType
    val booleanType: IrType
    val booleanNType: IrType
    val stringType: IrType
    val stringNType: IrType

    val arrayListConstructor: IrConstructorSymbol
    val hashSetConstructor: IrConstructorSymbol
    val hashMapConstructor: IrConstructorSymbol
    val illegalStateExceptionConstructor: IrConstructorSymbol
    val shortToInt: IrSimpleFunctionSymbol
    val intToShort: IrSimpleFunctionSymbol
    val byteToInt: IrSimpleFunctionSymbol
    val intToByte: IrSimpleFunctionSymbol
    val charToInt: IrSimpleFunctionSymbol
    val intToChar: IrSimpleFunctionSymbol
    val getCoding: IrSimpleFunctionSymbol
    val println: IrSimpleFunctionSymbol

    val encodeInt: IrSimpleFunctionSymbol
    val decodeInt: IrSimpleFunctionSymbol
    val encodeLong: IrSimpleFunctionSymbol
    val decodeLong: IrSimpleFunctionSymbol
    val encodeFloat: IrSimpleFunctionSymbol
    val decodeFloat: IrSimpleFunctionSymbol
    val encodeDouble: IrSimpleFunctionSymbol
    val decodeDouble: IrSimpleFunctionSymbol
    val encodeBoolean: IrSimpleFunctionSymbol
    val decodeBoolean: IrSimpleFunctionSymbol
    val encodeObject: IrSimpleFunctionSymbol
    val decodeObject: IrSimpleFunctionSymbol
}

class DefaultSymbols(
    pluginContext: IrPluginContext,
) : Symbols {

    private val nsCoderClass: IrClassSymbol = pluginContext.referenceClass(nsCoderName).require()
    private val parcelableClass: IrClassSymbol = pluginContext.referenceClass(parcelableName).require()
    private val nsCoderType: IrType = nsCoderClass.defaultType

    override val nsStringClass: IrClassSymbol = pluginContext.referenceClass(nsStringName).require()
    override val nsLockClass: IrClassSymbol = pluginContext.referenceClass(nsLockName).require()
    override val objCClassType: IrType = pluginContext.referenceClass(objCClassName)!!.defaultType
    override val decodedValueType: IrType = pluginContext.referenceClass(decodedValueName)!!.defaultType

    override val parcelableType: IrType = parcelableClass.defaultType
    override val parcelableNType: IrType = parcelableType.makeNullable()
    override val iteratorType: IrType = pluginContext.irBuiltIns.iteratorClass.defaultType
    override val iterableType: IrType = pluginContext.irBuiltIns.iterableClass.defaultType
    override val collectionType: IrType = pluginContext.irBuiltIns.collectionClass.defaultType
    override val mutableCollectionType: IrType = pluginContext.irBuiltIns.mutableCollectionClass.defaultType
    override val listType: IrType = pluginContext.irBuiltIns.listClass.defaultType
    override val mutableListType: IrType = pluginContext.irBuiltIns.mutableListClass.defaultType
    override val setType: IrType = pluginContext.irBuiltIns.setClass.defaultType
    override val mutableSetType: IrType = pluginContext.irBuiltIns.mutableSetClass.defaultType
    override val mapType: IrType = pluginContext.irBuiltIns.mapClass.defaultType
    override val mutableMapType: IrType = pluginContext.irBuiltIns.mutableMapClass.defaultType

    override val anyNType: IrType = pluginContext.irBuiltIns.anyNType
    override val unitType: IrType = pluginContext.irBuiltIns.unitType
    override val intType: IrType = pluginContext.irBuiltIns.intType
    override val intNType: IrType = intType.makeNullable()
    override val longType: IrType = pluginContext.irBuiltIns.longType
    override val longNType: IrType = longType.makeNullable()
    override val shortType: IrType = pluginContext.irBuiltIns.shortType
    override val shortNType: IrType = shortType.makeNullable()
    override val byteType: IrType = pluginContext.irBuiltIns.byteType
    override val byteNType: IrType = byteType.makeNullable()
    override val charType: IrType = pluginContext.irBuiltIns.charType
    override val charNType: IrType = charType.makeNullable()
    override val floatType: IrType = pluginContext.irBuiltIns.floatType
    override val floatNType: IrType = floatType.makeNullable()
    override val doubleType: IrType = pluginContext.irBuiltIns.doubleType
    override val doubleNType: IrType = doubleType.makeNullable()
    override val booleanType: IrType = pluginContext.irBuiltIns.booleanType
    override val booleanNType: IrType = booleanType.makeNullable()
    override val stringType: IrType = pluginContext.irBuiltIns.stringType
    override val stringNType: IrType = stringType.makeNullable()

    override val arrayListConstructor: IrConstructorSymbol =
        pluginContext.referenceClass(FqName("kotlin.collections.ArrayList"))!!
            .owner
            .constructors
            .first { it.valueParameters.isEmpty() }
            .symbol

    override val hashSetConstructor: IrConstructorSymbol =
        pluginContext.referenceClass(FqName("kotlin.collections.HashSet"))!!
            .owner
            .constructors
            .first { it.valueParameters.isEmpty() }
            .symbol

    override val hashMapConstructor: IrConstructorSymbol =
        pluginContext.referenceClass(FqName("kotlin.collections.HashMap"))!!
            .owner
            .constructors
            .first { it.valueParameters.isEmpty() }
            .symbol

    override val illegalStateExceptionConstructor: IrConstructorSymbol =
        pluginContext.referenceClass(FqName("kotlin.IllegalStateException"))!!
            .owner
            .constructors
            .first { it.valueParameters.size == 1 }
            .symbol

    override val shortToInt: IrSimpleFunctionSymbol = shortType.classOrNull!!.getSimpleFunction("toInt")!!
    override val intToShort: IrSimpleFunctionSymbol = intType.classOrNull!!.getSimpleFunction("toShort")!!
    override val byteToInt: IrSimpleFunctionSymbol = byteType.classOrNull!!.getSimpleFunction("toInt")!!
    override val intToByte: IrSimpleFunctionSymbol = intType.classOrNull!!.getSimpleFunction("toByte")!!
    override val charToInt: IrSimpleFunctionSymbol = charType.classOrNull!!.getSimpleFunction("toInt")!!
    override val intToChar: IrSimpleFunctionSymbol = intType.classOrNull!!.getSimpleFunction("toChar")!!

    override val getCoding: IrSimpleFunctionSymbol = parcelableClass.getSimpleFunction("coding")!!

    override val println: IrSimpleFunctionSymbol =
        pluginContext.referenceFunction(
            name = "kotlin.io.println",
            valueParameterTypes = listOf(anyNType),
        )

    override val encodeInt: IrSimpleFunctionSymbol =
        pluginContext.referenceFunction(
            name = "$packageFoundation.encodeInt32",
            extensionReceiverParameterType = nsCoderType,
            valueParameterTypes = listOf(intType, pluginContext.irBuiltIns.stringType),
        )

    override val decodeInt: IrSimpleFunctionSymbol =
        pluginContext.referenceFunction(
            name = "$packageFoundation.decodeInt32ForKey",
            extensionReceiverParameterType = nsCoderType,
            valueParameterTypes = listOf(stringType),
        )

    override val encodeLong: IrSimpleFunctionSymbol =
        pluginContext.referenceFunction(
            name = "$packageFoundation.encodeInt64",
            extensionReceiverParameterType = nsCoderType,
            valueParameterTypes = listOf(longType, stringType),
        )

    override val decodeLong: IrSimpleFunctionSymbol =
        pluginContext.referenceFunction(
            name = "$packageFoundation.decodeInt64ForKey",
            extensionReceiverParameterType = nsCoderType,
            valueParameterTypes = listOf(stringType),
        )

    override val encodeFloat: IrSimpleFunctionSymbol =
        pluginContext.referenceFunction(
            name = "$packageFoundation.encodeFloat",
            extensionReceiverParameterType = nsCoderType,
            valueParameterTypes = listOf(floatType, stringType),
        )

    override val decodeFloat: IrSimpleFunctionSymbol =
        pluginContext.referenceFunction(
            name = "$packageFoundation.decodeFloatForKey",
            extensionReceiverParameterType = nsCoderType,
            valueParameterTypes = listOf(stringType),
        )

    override val encodeDouble: IrSimpleFunctionSymbol =
        pluginContext.referenceFunction(
            name = "$packageFoundation.encodeDouble",
            extensionReceiverParameterType = nsCoderType,
            valueParameterTypes = listOf(doubleType, stringType),
        )

    override val decodeDouble: IrSimpleFunctionSymbol =
        pluginContext.referenceFunction(
            name = "$packageFoundation.decodeDoubleForKey",
            extensionReceiverParameterType = nsCoderType,
            valueParameterTypes = listOf(stringType),
        )

    override val encodeBoolean: IrSimpleFunctionSymbol =
        pluginContext.referenceFunction(
            name = "$packageFoundation.encodeBool",
            extensionReceiverParameterType = nsCoderType,
            valueParameterTypes = listOf(booleanType, pluginContext.irBuiltIns.stringType),
        )

    override val decodeBoolean: IrSimpleFunctionSymbol =
        pluginContext.referenceFunction(
            name = "$packageFoundation.decodeBoolForKey",
            extensionReceiverParameterType = nsCoderType,
            valueParameterTypes = listOf(stringType),
        )

    override val encodeObject: IrSimpleFunctionSymbol =
        pluginContext.referenceFunction(
            name = "$packageFoundation.encodeObject",
            extensionReceiverParameterType = nsCoderType,
            valueParameterTypes = listOf(pluginContext.irBuiltIns.anyNType, stringType),
        )

    override val decodeObject: IrSimpleFunctionSymbol =
        pluginContext.referenceFunction(
            name = "$packageFoundation.decodeObjectOfClass",
            extensionReceiverParameterType = nsCoderType,
            valueParameterTypes = listOf(objCClassType, stringType),
        )
}
