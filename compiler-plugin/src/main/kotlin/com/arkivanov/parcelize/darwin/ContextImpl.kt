package com.arkivanov.parcelize.darwin

import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.symbols.IrClassSymbol

class ContextImpl(
    override val pluginContext: IrPluginContext
) : Context {

    override val nsObjectClass: IrClassSymbol = pluginContext.referenceClass(nsObjectName).require()
    override val nsLockClass: IrClassSymbol = pluginContext.referenceClass(nsLockName).require()
    override val nsCodingClass: IrClassSymbol = pluginContext.referenceClass(nsCodingName).require()
    override val nsCodingMetaClass: IrClassSymbol = pluginContext.referenceClass(nsCodingMetaName).require()
    override val nsCoderClass: IrClassSymbol = pluginContext.referenceClass(nsCoderName).require()
    override val parcelableClass: IrClassSymbol = pluginContext.referenceClass(parcelableName).require()
}