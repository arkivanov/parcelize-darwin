package com.arkivanov.parcelize.darwin

import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.ir.builders.IrBlockBodyBuilder
import org.jetbrains.kotlin.ir.builders.IrBlockBuilder
import org.jetbrains.kotlin.ir.builders.IrBuilderWithScope
import org.jetbrains.kotlin.ir.builders.irBlockBody
import org.jetbrains.kotlin.ir.builders.irCall
import org.jetbrains.kotlin.ir.builders.irConcat
import org.jetbrains.kotlin.ir.builders.irGet
import org.jetbrains.kotlin.ir.builders.irSet
import org.jetbrains.kotlin.ir.builders.irWhile
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrConstructor
import org.jetbrains.kotlin.ir.declarations.IrField
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.declarations.IrValueDeclaration
import org.jetbrains.kotlin.ir.expressions.IrBlockBody
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrConstructorCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.impl.IrConstructorCallImpl
import org.jetbrains.kotlin.ir.symbols.IrSimpleFunctionSymbol
import org.jetbrains.kotlin.ir.types.IrSimpleType
import org.jetbrains.kotlin.ir.types.IrStarProjection
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.IrTypeArgument
import org.jetbrains.kotlin.ir.types.IrTypeProjection
import org.jetbrains.kotlin.ir.types.classFqName
import org.jetbrains.kotlin.ir.types.getClass
import org.jetbrains.kotlin.ir.util.functions
import org.jetbrains.kotlin.ir.util.packageFqName
import org.jetbrains.kotlin.ir.util.parentClassOrNull
import org.jetbrains.kotlin.ir.util.primaryConstructor
import org.jetbrains.kotlin.ir.util.properties
import org.jetbrains.kotlin.ir.util.render
import org.jetbrains.kotlin.ir.util.superTypes
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.types.Variance

const val packageFoundation: String = "platform.Foundation"
const val packageRuntime: String = "com.arkivanov.parcelize.darwin"

val parcelizeName = FqName("$packageRuntime.Parcelize")
val parcelableName = FqName("$packageRuntime.Parcelable")
val nsSecureCodingName = FqName("$packageFoundation.NSSecureCodingProtocol")
val nsSecureCodingMetaName = FqName("$packageFoundation.NSSecureCodingProtocolMeta")
val nsCoderName = FqName("$packageFoundation.NSCoder")
val nsObjectName = FqName("platform.darwin.NSObject")
val nsStringName = FqName("$packageFoundation.NSString")
val nsLockName = FqName("$packageFoundation.NSLock")
val nsArrayName = FqName("$packageFoundation.NSArray")
val nsMutableArrayName = FqName("$packageFoundation.NSMutableArray")
val objCClassName = FqName("kotlinx.cinterop.ObjCClass")
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

fun IrClass.requireFunction(
    name: String,
    valueParameterTypes: List<IrType>? = null,
): IrSimpleFunctionSymbol =
    functions.first { f ->
        (f.name.asString() == name) &&
            ((valueParameterTypes == null) || (f.valueParameters.map { it.type.classFqName } == valueParameterTypes.map { it.classFqName }))
    }.symbol

fun IrType.requireClass(): IrClass =
    requireNotNull(getClass()) { "Class is not found for type: ${render()}" }

fun IrPluginContext.referenceFunction(
    name: String,
    extensionReceiverParameterType: IrType? = null,
    valueParameterTypes: List<IrType> = emptyList(),
): IrSimpleFunctionSymbol =
    requireNotNull(
        referenceFunctions(FqName(name)).firstOrNull { f ->
            (f.owner.extensionReceiverParameter?.type == extensionReceiverParameterType) &&
                (f.owner.valueParameters.map { it.type.classFqName } == valueParameterTypes.map { it.classFqName })
        }
    ) { "Function $name not found" }

fun IrBuilderWithScope.irCall(
    callee: IrSimpleFunctionSymbol,
    extensionReceiver: IrExpression? = null,
    dispatchReceiver: IrExpression? = null,
    arguments: List<IrExpression> = emptyList(),
): IrCall =
    irCall(callee).apply {
        this.extensionReceiver = extensionReceiver
        this.dispatchReceiver = dispatchReceiver

        arguments.forEachIndexed { index, argument ->
            putValueArgument(index, argument)
        }
    }

fun IrBlockBuilder.irConcatStrings(
    vararg args: IrExpression,
): IrExpression =
    irConcat().apply {
        arguments += args
    }

fun IrBlockBuilder.irPrintLn(callee: IrSimpleFunctionSymbol, obj: IrExpression): IrCall =
    irCall(callee = callee, arguments = listOf(obj))

fun IrTypeArgument.upperBound(anyNType: IrType): IrType =
    when (this) {
        is IrStarProjection -> anyNType

        is IrTypeProjection ->
            when (variance) {
                Variance.INVARIANT,
                Variance.OUT_VARIANCE -> type

                Variance.IN_VARIANCE -> anyNType
            }

        else -> error("Unknown type argument: ${render()}")
    }

fun IrBlockBuilder.irWhile(
    condition: IrExpression,
    body: IrExpression,
): IrExpression =
    irWhile().apply {
        this.condition = condition
        this.body = body
    }

fun IrBlockBuilder.irIncrementVariable(variable: IrValueDeclaration): IrExpression =
    irSet(
        variable = variable,
        value = irCall(
            callee = variable.type.requireClass().requireFunction(name = "inc"),
            dispatchReceiver = irGet(variable),
        ),
    )

val IrClass.parcelableFields: List<IrField>
    get() {
        if (kind != ClassKind.CLASS) {
            return emptyList()
        }

        val constructor = primaryConstructor ?: return emptyList()

        return constructor.valueParameters.mapNotNull { parameter ->
            val property = properties.firstOrNull { it.name == parameter.name }
            if (property == null /*|| property.hasAnyAnnotation(IGNORED_ON_PARCEL_FQ_NAMES)*/) {
                null
            } else {
                property.backingField
            }
        }
    }

fun IrType.asIrSimpleType(): IrSimpleType =
    this as IrSimpleType

fun <T : Any> T?.require(): T = requireNotNull(this)
