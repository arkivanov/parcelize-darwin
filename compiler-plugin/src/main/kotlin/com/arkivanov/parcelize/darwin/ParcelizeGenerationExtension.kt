package com.arkivanov.parcelize.darwin

import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.common.lower
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment

class ParcelizeGenerationExtension(
    private val logs: MessageCollector
) : IrGenerationExtension {

    override fun generate(moduleFragment: IrModuleFragment, pluginContext: IrPluginContext) {
        val symbols = DefaultSymbols(pluginContext)

        ParcelizeClassLoweringPass(
            context = ContextImpl(pluginContext),
            symbols = symbols,
            coderFactory = CoderFactory(symbols),
            logs = logs,
        ).lower(moduleFragment)
    }
}
