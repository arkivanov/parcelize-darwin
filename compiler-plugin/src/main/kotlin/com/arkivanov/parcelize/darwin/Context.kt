package com.arkivanov.parcelize.darwin

import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.IrBuiltIns
import org.jetbrains.kotlin.ir.declarations.IrFactory
import org.jetbrains.kotlin.ir.symbols.IrClassSymbol
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.defaultType
import org.jetbrains.kotlin.ir.types.makeNullable

// TODO: Remove
interface Context {

    val pluginContext: IrPluginContext
    val nsObjectClass: IrClassSymbol
    val nsLockClass: IrClassSymbol
    val nsCodingClass: IrClassSymbol
    val nsCodingMetaClass: IrClassSymbol
    val nsCoderClass: IrClassSymbol
    val parcelableClass: IrClassSymbol
}

val Context.irFactory: IrFactory get() = pluginContext.irFactory
val Context.irBuiltIns: IrBuiltIns get() = pluginContext.irBuiltIns
val Context.nsObjectType: IrType get() = nsObjectClass.defaultType
val Context.nsLockType: IrType get() = nsLockClass.defaultType
val Context.nsCodingType: IrType get() = nsCodingClass.defaultType
val Context.nsCodingMetaType: IrType get() = nsCodingMetaClass.defaultType
val Context.nsCoderType: IrType get() = nsCoderClass.defaultType
val Context.parcelableType: IrType get() = parcelableClass.defaultType
val Context.parcelableNType: IrType get() = parcelableType.makeNullable()
val Context.collectionClass: IrClassSymbol get() = pluginContext.symbols.collection
val Context.collectionType: IrType get() = collectionClass.defaultType
val Context.listClass: IrClassSymbol get() = pluginContext.symbols.list
val Context.listType: IrType get() = listClass.defaultType
val Context.mutableListClass: IrClassSymbol get()  = pluginContext.symbols.mutableList
val Context.mutableListType: IrType get() = mutableListClass.defaultType
val Context.setClass: IrClassSymbol get()  = pluginContext.symbols.set
val Context.setType: IrType get() = setClass.defaultType
val Context.mutableSetClass: IrClassSymbol get()  = pluginContext.symbols.mutableSet
val Context.mutableSetType: IrType get() = mutableSetClass.defaultType
val Context.mapClass: IrClassSymbol get() = pluginContext.symbols.map
val Context.mapType: IrType get() = mapClass.defaultType
val Context.mutableMapClass: IrClassSymbol get() = pluginContext.symbols.mutableMap
val Context.mutableMapType: IrType get() = mutableMapClass.defaultType
val Context.unitType: IrType get() = pluginContext.irBuiltIns.unitType
val Context.anyType: IrType get() = pluginContext.irBuiltIns.anyType
val Context.anyNType: IrType get() = pluginContext.irBuiltIns.anyNType
val Context.intType: IrType get() = pluginContext.irBuiltIns.intType
val Context.intNType: IrType get() = intType.makeNullable()
val Context.longType: IrType get() = pluginContext.irBuiltIns.longType
val Context.longNType: IrType get() = longType.makeNullable()
val Context.floatType: IrType get() = pluginContext.irBuiltIns.floatType
val Context.floatNType: IrType get() = floatType.makeNullable()
val Context.doubleType: IrType get() = pluginContext.irBuiltIns.doubleType
val Context.doubleNType: IrType get() = doubleType.makeNullable()
val Context.shortType: IrType get() = pluginContext.irBuiltIns.shortType
val Context.shortNType: IrType get() = shortType.makeNullable()
val Context.byteType: IrType get() = pluginContext.irBuiltIns.byteType
val Context.byteNType: IrType get() = byteType.makeNullable()
val Context.charType: IrType get() = pluginContext.irBuiltIns.charType
val Context.charNType: IrType get() = charType.makeNullable()
val Context.booleanType: IrType get() = pluginContext.irBuiltIns.booleanType
val Context.booleanNType: IrType get() = booleanType.makeNullable()
val Context.stringClass: IrClassSymbol get()  = pluginContext.symbols.string
val Context.stringType: IrType get() = stringClass.defaultType
val Context.stringNType: IrType get() = stringType.makeNullable()
