package com.arkivanov.parcelize.darwin

import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.ir.builders.IrBlockBodyBuilder
import org.jetbrains.kotlin.ir.builders.irBlockBody
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrConstructor
import org.jetbrains.kotlin.ir.declarations.IrField
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.expressions.IrBlockBody
import org.jetbrains.kotlin.ir.expressions.IrConstructorCall
import org.jetbrains.kotlin.ir.expressions.impl.IrConstructorCallImpl
import org.jetbrains.kotlin.ir.symbols.IrClassSymbol
import org.jetbrains.kotlin.ir.symbols.IrSimpleFunctionSymbol
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.classFqName
import org.jetbrains.kotlin.ir.types.defaultType
import org.jetbrains.kotlin.ir.util.packageFqName
import org.jetbrains.kotlin.ir.util.parentClassOrNull
import org.jetbrains.kotlin.ir.util.superTypes
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name

const val packageFoundation: String = "platform.Foundation"
const val packageRuntime: String = "com.arkivanov.parcelize.darwin"

val parcelizeName = FqName("$packageRuntime.Parcelize")
val parcelableName = FqName("$packageRuntime.Parcelable")
val decodedValueName = FqName("$packageRuntime.DecodedValue")
val nsCodingName = FqName("$packageFoundation.NSSecureCodingProtocol")
val nsCodingMetaName = FqName("$packageFoundation.NSSecureCodingProtocolMeta")
val nsCoderName = FqName("$packageFoundation.NSCoder")
val nsObjectName = FqName("platform.darwin.NSObject")
val nsLockName = FqName("$packageFoundation.NSLock")
val exportObjCClassName = FqName("kotlinx.cinterop.ExportObjCClass")
val codingName = Name.identifier("coding")

fun MessageCollector.log(text: String) {
    report(CompilerMessageSeverity.STRONG_WARNING, text)
}

fun ClassDescriptor.isParcelize(): Boolean =
    annotations.hasAnnotation(parcelizeName)

fun IrType.isParcelable(): Boolean =
    (classFqName == parcelableName) ||
        superTypes().any { (it.classFqName == parcelableName) || it.isParcelable() }

fun IrClassSymbol.isParcelable(): Boolean =
    defaultType.isParcelable()

fun IrFunction.setBody(context: IrPluginContext, body: IrBlockBodyBuilder.() -> Unit): IrBlockBody =
    DeclarationIrBuilder(context, symbol)
        .irBlockBody(body = body)
        .also { this.body = it }

fun IrConstructor.toIrConstructorCall(): IrConstructorCall =
    IrConstructorCallImpl.fromSymbolOwner(
        type = returnType,
        constructorSymbol = symbol
    )

fun IrClass.getFullCapitalizedName(): String {
    var str = name.identifier.capitalize()

    var cls: IrClass? = parentClassOrNull
    while (cls != null) {
        str = "${cls.name.identifier.capitalize()}_$str"
        cls = cls.parentClassOrNull
    }

    val prefix: String? =
        packageFqName
            ?.pathSegments()
            ?.joinToString(separator = "_") { it.identifier.capitalize() }

    if (prefix != null) {
        str = "${prefix}_$str"
    }

    return str
}

fun IrPluginContext.referenceFunction(
    name: String,
    extensionReceiverParameterType: IrType? = null,
    valueParameterTypes: List<IrType> = emptyList()
): IrSimpleFunctionSymbol =
    referenceFunctions(FqName(name))
        .firstOrNull {
            (it.owner.extensionReceiverParameter?.type == extensionReceiverParameterType) &&
                (it.owner.valueParameters.map { it.type.classFqName } == valueParameterTypes.map { it.classFqName })
        } ?: error("Function $name not found")

fun <T : Any> T?.require(): T = requireNotNull(this)

val IrField.description: String
    get() = "[$name: ${type.classFqName}]"
