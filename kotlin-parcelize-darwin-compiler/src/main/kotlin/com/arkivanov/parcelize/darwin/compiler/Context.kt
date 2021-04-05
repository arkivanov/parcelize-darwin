package com.arkivanov.parcelize.darwin.compiler

import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.declarations.IrFactory
import org.jetbrains.kotlin.ir.descriptors.IrBuiltIns
import org.jetbrains.kotlin.ir.symbols.IrClassSymbol
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.defaultType
import org.jetbrains.kotlin.ir.types.withHasQuestionMark

interface Context {

    val pluginContext: IrPluginContext
    val nsObjectClass: IrClassSymbol
    val nsCodingClass: IrClassSymbol
    val nsCoderClass: IrClassSymbol
    val decodedValueClass: IrClassSymbol
    val parcelableClass: IrClassSymbol
}

val Context.irFactory: IrFactory get() = pluginContext.irFactory
val Context.irBuiltIns: IrBuiltIns get() = pluginContext.irBuiltIns
val Context.nsObjectType: IrType get() = nsObjectClass.defaultType
val Context.nsCodingType: IrType get() = nsCodingClass.defaultType
val Context.nsCoderType: IrType get() = nsCoderClass.defaultType
val Context.parcelableType: IrType get() = parcelableClass.defaultType
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
val Context.mapClass: IrClassSymbol get()  = pluginContext.symbols.map
val Context.mapType: IrType get() = mapClass.defaultType
val Context.mutableMapClass: IrClassSymbol get()  = pluginContext.symbols.mutableMap
val Context.mutableMapType: IrType get() = mutableMapClass.defaultType
val Context.unitType: IrType get() = pluginContext.irBuiltIns.unitType
val Context.anyType: IrType get() = pluginContext.irBuiltIns.anyType
val Context.anyNType: IrType get() = pluginContext.irBuiltIns.anyNType
val Context.intType: IrType get() = pluginContext.irBuiltIns.intType
val Context.intNType: IrType get() = intType.withHasQuestionMark(true)
val Context.longType: IrType get() = pluginContext.irBuiltIns.longType
val Context.longNType: IrType get() = longType.withHasQuestionMark(true)
val Context.floatType: IrType get() = pluginContext.irBuiltIns.floatType
val Context.floatNType: IrType get() = floatType.withHasQuestionMark(true)
val Context.doubleType: IrType get() = pluginContext.irBuiltIns.doubleType
val Context.doubleNType: IrType get() = doubleType.withHasQuestionMark(true)
val Context.shortType: IrType get() = pluginContext.irBuiltIns.shortType
val Context.shortNType: IrType get() = shortType.withHasQuestionMark(true)
val Context.byteType: IrType get() = pluginContext.irBuiltIns.byteType
val Context.byteNType: IrType get() = byteType.withHasQuestionMark(true)
val Context.charType: IrType get() = pluginContext.irBuiltIns.charType
val Context.charNType: IrType get() = charType.withHasQuestionMark(true)
val Context.booleanType: IrType get() = pluginContext.irBuiltIns.booleanType
val Context.booleanNType: IrType get() = booleanType.withHasQuestionMark(true)
val Context.stringClass: IrClassSymbol get()  = pluginContext.symbols.string
val Context.stringType: IrType get() = stringClass.defaultType
val Context.stringNType: IrType get() = stringType.withHasQuestionMark(true)