package com.arkivanov.parcelize.darwin

import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.symbols.IrClassSymbol
import org.jetbrains.kotlin.ir.symbols.IrConstructorSymbol
import org.jetbrains.kotlin.ir.symbols.IrSimpleFunctionSymbol
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.defaultType
import org.jetbrains.kotlin.ir.types.makeNullable
import org.jetbrains.kotlin.ir.util.constructors
import org.jetbrains.kotlin.ir.util.getSimpleFunction

interface Symbols {

    val nsObjectType: IrType
    val nsStringClass: IrClassSymbol
    val nsDataClass: IrClassSymbol
    val nsLockType: IrType
    val nsSecureCodingType: IrType
    val nsSecureCodingMetaType: IrType
    val nsCoderType: IrType
    val objCClassType: IrType
    val nsArrayType: IrType
    val nsMutableArrayType: IrType

    val parcelableType: IrType
    val parcelableNType: IrType
    val iteratorType: IrType
    val collectionType: IrType
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
    val uLongType: IrType

    val arrayListConstructor: IrConstructorSymbol
    val hashSetConstructor: IrConstructorSymbol
    val hashMapConstructor: IrConstructorSymbol
    val nsMutableArrayConstructor: IrConstructorSymbol
    val nsKeyedArchiverConstructor: IrConstructorSymbol
    val nsKeyedUnarchiverConstructor: IrConstructorSymbol
    val illegalStateExceptionConstructor: IrConstructorSymbol
    val shortToInt: IrSimpleFunctionSymbol
    val intToShort: IrSimpleFunctionSymbol
    val byteToInt: IrSimpleFunctionSymbol
    val intToByte: IrSimpleFunctionSymbol
    val charToInt: IrSimpleFunctionSymbol
    val intToChar: IrSimpleFunctionSymbol
    val getCoding: IrSimpleFunctionSymbol
    val println: IrSimpleFunctionSymbol

    val encodedData: IrSimpleFunctionSymbol
    val setRequireSecureCoding: IrSimpleFunctionSymbol
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
    private val pluginContext: IrPluginContext,
) : Symbols {

    private val parcelableClass: IrClassSymbol = pluginContext.referenceClass(parcelableClassId).require()

    override val nsObjectType: IrType = pluginContext.referenceClass(nsObjectClassId).require().defaultType
    override val nsStringClass: IrClassSymbol = pluginContext.referenceClass(nsStringClassId).require()
    override val nsDataClass: IrClassSymbol = pluginContext.referenceClass(nsDataClassId).require()
    override val nsLockType: IrType = pluginContext.referenceClass(nsLockClassId).require().defaultType
    override val nsSecureCodingType: IrType = pluginContext.referenceClass(nsSecureCodingClassId).require().defaultType
    override val nsSecureCodingMetaType: IrType = pluginContext.referenceClass(nsSecureCodingMetaClassId).require().defaultType
    override val nsCoderType: IrType = pluginContext.referenceClass(nsCoderClassId).require().defaultType
    override val objCClassType: IrType = pluginContext.referenceClass(objCClassClassId).require().defaultType
    override val nsArrayType: IrType = pluginContext.referenceClass(nsArrayClassId).require().defaultType
    override val nsMutableArrayType: IrType = pluginContext.referenceClass(nsMutableArrayClassId).require().defaultType

    override val parcelableType: IrType = parcelableClass.defaultType
    override val parcelableNType: IrType = parcelableType.makeNullable()
    override val iteratorType: IrType = pluginContext.irBuiltIns.iteratorClass.defaultType
    override val collectionType: IrType = pluginContext.irBuiltIns.collectionClass.defaultType
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
    override val uLongType: IrType = pluginContext.referenceClass(ClassId(name = "kotlin.ULong")).require().defaultType

    override val arrayListConstructor: IrConstructorSymbol =
        pluginContext.referenceClass(arrayListClassId).require()
            .owner
            .constructors
            .first { it.valueParameters.isEmpty() }
            .symbol

    override val hashSetConstructor: IrConstructorSymbol =
        pluginContext.referenceClass(hashSetClassId).require()
            .owner
            .constructors
            .first { it.valueParameters.isEmpty() }
            .symbol

    override val hashMapConstructor: IrConstructorSymbol =
        pluginContext.referenceClass(hashMapClassId).require()
            .owner
            .constructors
            .first { it.valueParameters.isEmpty() }
            .symbol

    override val nsMutableArrayConstructor: IrConstructorSymbol =
        pluginContext.referenceClass(nsMutableArrayClassId).require()
            .owner
            .constructors
            .first { it.valueParameters.isEmpty() }
            .symbol

    override val nsKeyedArchiverConstructor: IrConstructorSymbol =
        pluginContext.referenceClass(nsKeyedArchiverClassId).require()
            .owner
            .constructors
            .first { it.valueParameters.size == 1 }
            .symbol

    override val nsKeyedUnarchiverConstructor: IrConstructorSymbol =
        pluginContext.referenceClass(nsKeyedUnarchiverClassId).require()
            .owner
            .constructors
            .first { it.valueParameters.size == 1 }
            .symbol

    override val illegalStateExceptionConstructor: IrConstructorSymbol =
        pluginContext.referenceClass(ClassId("kotlin.IllegalStateException")).require()
            .owner
            .constructors
            .first { it.valueParameters.size == 1 }
            .symbol

    override val shortToInt: IrSimpleFunctionSymbol = shortType.requireClass().requireFunction(name = "toInt")
    override val intToShort: IrSimpleFunctionSymbol = intType.requireClass().requireFunction(name = "toShort")
    override val byteToInt: IrSimpleFunctionSymbol = byteType.requireClass().requireFunction(name = "toInt")
    override val intToByte: IrSimpleFunctionSymbol = intType.requireClass().requireFunction(name = "toByte")
    override val charToInt: IrSimpleFunctionSymbol = charType.requireClass().requireFunction(name = "toInt")
    override val intToChar: IrSimpleFunctionSymbol = intType.requireClass().requireFunction(name = "toChar")
    override val getCoding: IrSimpleFunctionSymbol = parcelableClass.getSimpleFunction("coding")!!

    override val println: IrSimpleFunctionSymbol =
        pluginContext.referenceFunction(
            callableId = CallableId(packageName = "kotlin.io", callableName = "println"),
            valueParameterTypes = listOf(anyNType),
        )

    override val encodedData: IrSimpleFunctionSymbol =
        pluginContext.referenceFunction(
            callableId = CallableId(classId = nsKeyedArchiverClassId, callableName = "encodedData"),
            valueParameterTypes = listOf(),
        )

    override val setRequireSecureCoding: IrSimpleFunctionSymbol =
        pluginContext.referenceFunction(
            callableId = CallableId(classId = nsKeyedUnarchiverClassId, callableName = "setRequiresSecureCoding"),
            valueParameterTypes = listOf(booleanType),
        )

    override val encodeInt: IrSimpleFunctionSymbol =
        pluginContext.referenceFunction(
            callableId = CallableId(packageName = packageFoundation, callableName = "encodeInt32"),
            extensionReceiverParameterType = nsCoderType,
            valueParameterTypes = listOf(intType, pluginContext.irBuiltIns.stringType),
        )

    override val decodeInt: IrSimpleFunctionSymbol =
        pluginContext.referenceFunction(
            callableId = CallableId(packageName = packageFoundation, callableName = "decodeInt32ForKey"),
            extensionReceiverParameterType = nsCoderType,
            valueParameterTypes = listOf(stringType),
        )

    override val encodeLong: IrSimpleFunctionSymbol =
        pluginContext.referenceFunction(
            callableId = CallableId(packageName = packageFoundation, callableName = "encodeInt64"),
            extensionReceiverParameterType = nsCoderType,
            valueParameterTypes = listOf(longType, stringType),
        )

    override val decodeLong: IrSimpleFunctionSymbol =
        pluginContext.referenceFunction(
            callableId = CallableId(packageName = packageFoundation, callableName = "decodeInt64ForKey"),
            extensionReceiverParameterType = nsCoderType,
            valueParameterTypes = listOf(stringType),
        )

    override val encodeFloat: IrSimpleFunctionSymbol =
        pluginContext.referenceFunction(
            callableId = CallableId(packageName = packageFoundation, callableName = "encodeFloat"),
            extensionReceiverParameterType = nsCoderType,
            valueParameterTypes = listOf(floatType, stringType),
        )

    override val decodeFloat: IrSimpleFunctionSymbol =
        pluginContext.referenceFunction(
            callableId = CallableId(packageName = packageFoundation, callableName = "decodeFloatForKey"),
            extensionReceiverParameterType = nsCoderType,
            valueParameterTypes = listOf(stringType),
        )

    override val encodeDouble: IrSimpleFunctionSymbol =
        pluginContext.referenceFunction(
            callableId = CallableId(packageName = packageFoundation, callableName = "encodeDouble"),
            extensionReceiverParameterType = nsCoderType,
            valueParameterTypes = listOf(doubleType, stringType),
        )

    override val decodeDouble: IrSimpleFunctionSymbol =
        pluginContext.referenceFunction(
            callableId = CallableId(packageName = packageFoundation, callableName = "decodeDoubleForKey"),
            extensionReceiverParameterType = nsCoderType,
            valueParameterTypes = listOf(stringType),
        )

    override val encodeBoolean: IrSimpleFunctionSymbol =
        pluginContext.referenceFunction(
            callableId = CallableId(packageName = packageFoundation, callableName = "encodeBool"),
            extensionReceiverParameterType = nsCoderType,
            valueParameterTypes = listOf(booleanType, pluginContext.irBuiltIns.stringType),
        )

    override val decodeBoolean: IrSimpleFunctionSymbol =
        pluginContext.referenceFunction(
            callableId = CallableId(packageName = packageFoundation, callableName = "decodeBoolForKey"),
            extensionReceiverParameterType = nsCoderType,
            valueParameterTypes = listOf(stringType),
        )

    override val encodeObject: IrSimpleFunctionSymbol =
        pluginContext.referenceFunction(
            callableId = CallableId(packageName = packageFoundation, callableName = "encodeObject"),
            extensionReceiverParameterType = nsCoderType,
            valueParameterTypes = listOf(pluginContext.irBuiltIns.anyNType, stringType),
        )

    override val decodeObject: IrSimpleFunctionSymbol =
        pluginContext.referenceFunction(
            callableId = CallableId(packageName = packageFoundation, callableName = "decodeObjectOfClass"),
            extensionReceiverParameterType = nsCoderType,
            valueParameterTypes = listOf(objCClassType, stringType),
        )
}
