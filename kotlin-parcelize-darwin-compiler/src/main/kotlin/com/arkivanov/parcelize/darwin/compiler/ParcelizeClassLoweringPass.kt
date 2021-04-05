package com.arkivanov.parcelize.darwin.compiler

import com.arkivanov.parcelize.darwin.compiler.ParcelizeClassLoweringPass.FieldValueDecoder
import org.jetbrains.kotlin.backend.common.ClassLoweringPass
import org.jetbrains.kotlin.backend.common.ir.addChild
import org.jetbrains.kotlin.backend.common.ir.addSimpleDelegatingConstructor
import org.jetbrains.kotlin.backend.common.ir.copyTo
import org.jetbrains.kotlin.backend.common.ir.createImplicitParameterDeclarationWithWrappedDescriptor
import org.jetbrains.kotlin.backend.wasm.ir2wasm.getSuperClass
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.descriptors.DescriptorVisibilities
import org.jetbrains.kotlin.ir.UNDEFINED_OFFSET
import org.jetbrains.kotlin.ir.builders.IrBlockBodyBuilder
import org.jetbrains.kotlin.ir.builders.declarations.addFunction
import org.jetbrains.kotlin.ir.builders.declarations.addGetter
import org.jetbrains.kotlin.ir.builders.declarations.addProperty
import org.jetbrains.kotlin.ir.builders.declarations.addValueParameter
import org.jetbrains.kotlin.ir.builders.declarations.buildClass
import org.jetbrains.kotlin.ir.builders.declarations.buildField
import org.jetbrains.kotlin.ir.builders.irCall
import org.jetbrains.kotlin.ir.builders.irGet
import org.jetbrains.kotlin.ir.builders.irGetField
import org.jetbrains.kotlin.ir.builders.irReturn
import org.jetbrains.kotlin.ir.builders.irString
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrConstructor
import org.jetbrains.kotlin.ir.declarations.IrDeclarationOrigin
import org.jetbrains.kotlin.ir.declarations.IrField
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.declarations.IrProperty
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.declarations.IrValueParameter
import org.jetbrains.kotlin.ir.descriptors.toIrBasedDescriptor
import org.jetbrains.kotlin.ir.expressions.IrBlockBody
import org.jetbrains.kotlin.ir.expressions.IrConstructorCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrStatementOrigin
import org.jetbrains.kotlin.ir.expressions.impl.IrGetValueImpl
import org.jetbrains.kotlin.ir.interpreter.toIrConst
import org.jetbrains.kotlin.ir.symbols.IrClassSymbol
import org.jetbrains.kotlin.ir.symbols.IrConstructorSymbol
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.classOrNull
import org.jetbrains.kotlin.ir.types.defaultType
import org.jetbrains.kotlin.ir.types.isSubtypeOfClass
import org.jetbrains.kotlin.ir.types.withHasQuestionMark
import org.jetbrains.kotlin.ir.util.constructors
import org.jetbrains.kotlin.ir.util.defaultType
import org.jetbrains.kotlin.ir.util.dump
import org.jetbrains.kotlin.ir.util.functions
import org.jetbrains.kotlin.ir.util.getSimpleFunction
import org.jetbrains.kotlin.ir.util.primaryConstructor
import org.jetbrains.kotlin.ir.util.properties
import org.jetbrains.kotlin.name.Name

class ParcelizeClassLoweringPass(
    context: Context,
    private val logs: MessageCollector
) : ClassLoweringPass, Context by context {

    override fun lower(irClass: IrClass) {
        if (!irClass.toIrBasedDescriptor().isValidForParcelize()) {
            return
        }

        val codingClass = irClass.addCodingClass()
        val codingClassConstructor = codingClass.addSimpleDelegatingPrimaryConstructor()

        val codingClassConstructorParameter =
            codingClassConstructor.addValueParameter {
                name = Name.identifier("data")
                type = irClass.defaultType
            }

        val dataField = codingClass.addDataField(irClass, codingClassConstructorParameter)
        val dataProperty = codingClass.addDataProperty(dataField)
        val dataGetter = dataProperty.addDataGetter(irClass, codingClass, dataField)

        codingClass.addEncodeWithCoderFunction(irClass, dataGetter)
        codingClass.addInitWithCoderFunction(irClass)

        irClass.generateCodingBody(codingClass)
    }

    // region CodingImpl

    private fun IrClass.addCodingClass(): IrClass =
        irFactory
            .buildClass {
                name = Name.identifier("CodingImpl")
                kind = ClassKind.CLASS
                visibility = DescriptorVisibilities.PRIVATE
            }
            .also(::addChild)
            .apply {
                superTypes = listOf(nsObjectType, nsCodingType)
                annotations = listOf(getExportObjCClassAnnotationCall(name = getFullCapitalizedName()))
                createImplicitParameterDeclarationWithWrappedDescriptor()
            }

    private fun getExportObjCClassAnnotationCall(name: String = ""): IrConstructorCall =
        pluginContext
            .referenceConstructors(exportObjCClassName)
            .map(IrConstructorSymbol::owner)
            .single { it.valueParameters.map(IrValueParameter::type) == listOf(stringType) }
            .toIrConstructorCall()
            .apply { putValueArgument(0, name.toIrConst(stringType)) }

    private fun IrClass.addDataField(mainClass: IrClass, constructorParameter: IrValueParameter): IrField =
        irFactory
            .buildField {
                name = Name.identifier("data")
                visibility = DescriptorVisibilities.PRIVATE
                type = mainClass.defaultType
                origin = IrDeclarationOrigin.PROPERTY_BACKING_FIELD
            }
            .apply {
                parent = this@addDataField

                initializer =
                    irFactory.createExpressionBody(
                        startOffset = UNDEFINED_OFFSET,
                        endOffset = UNDEFINED_OFFSET,
                        expression = IrGetValueImpl(
                            startOffset = UNDEFINED_OFFSET,
                            endOffset = UNDEFINED_OFFSET,
                            symbol = constructorParameter.symbol,
                            origin = IrStatementOrigin.INITIALIZE_PROPERTY_FROM_PARAMETER
                        )
                    )
            }

    private fun IrClass.addDataProperty(dataField: IrField): IrProperty =
        addProperty {
            name = Name.identifier("data")
            visibility = DescriptorVisibilities.PRIVATE
        }.apply {
            backingField = dataField
        }

    private fun IrProperty.addDataGetter(mainClass: IrClass, codingClass: IrClass, dataField: IrField): IrSimpleFunction =
        addGetter {
            returnType = mainClass.defaultType
            visibility = DescriptorVisibilities.PRIVATE
            origin = IrDeclarationOrigin.DEFAULT_PROPERTY_ACCESSOR
        }.apply {
            val receiverParameter = codingClass.thisReceiver!!.copyTo(this)
            dispatchReceiverParameter = receiverParameter

            setBody(pluginContext) {
                +irReturn(
                    irGetField(
                        irGet(receiverParameter.type, receiverParameter.symbol),
                        dataField
                    )
                )
            }
        }

    // region Encode

    private fun IrClass.addEncodeWithCoderFunction(mainClass: IrClass, dataGetter: IrFunction) {
        addFunction {
            name = Name.identifier("encodeWithCoder")
            returnType = unitType
        }.apply {
            overriddenSymbols = listOf(nsCodingClass.getSimpleFunction("encodeWithCoder")!!)
            dispatchReceiverParameter = this@addEncodeWithCoderFunction.thisReceiver?.copyTo(this)

            addValueParameter {
                name = Name.identifier("coder")
                type = nsCoderType
            }

            setEncodeWithCoderBody(mainClass, dataGetter)
        }
    }

    private fun IrSimpleFunction.setEncodeWithCoderBody(mainClass: IrClass, dataGetter: IrFunction): IrBlockBody {
        val thisReceiver = dispatchReceiverParameter!!
        val coderArgument = valueParameters[0]

        return setBody(pluginContext) {
            val data = irCall(dataGetter, IrStatementOrigin.GET_PROPERTY)
            data.dispatchReceiver = irGet(thisReceiver.type, thisReceiver.symbol)
            val coder = irGet(coderArgument)

            mainClass.properties.mapNotNull(IrProperty::backingField).forEach { field ->
                addEncodeFieldStatement(
                    field = field,
                    data = data,
                    coder = coder
                )
            }
        }
    }

    private fun IrBlockBodyBuilder.addEncodeFieldStatement(field: IrField, data: IrExpression, coder: IrExpression) {
        object : FieldValueEncoder {
            override fun encode(functionName: String, valueType: IrType?) {
                +irCall(
                    pluginContext.referenceFunction(
                        name = functionName,
                        extensionReceiverParameterType = nsCoderType,
                        valueParameterTypes = listOf((valueType ?: field.type), stringType)
                    )
                ).apply {
                    extensionReceiver = coder
                    putValueArgument(0, irGetField(data, field))
                    putValueArgument(1, irString(field.name.identifier))
                }
            }
        }.encodeField(field)
    }

    private fun FieldValueEncoder.encodeField(field: IrField) {
        val fieldType = field.type

        when {
            fieldType == intType -> encode("$packageFoundation.encodeInt32")
            fieldType == intNType -> encode("$packageRuntime.encodeIntOrNull")
            fieldType == longType -> encode("$packageFoundation.encodeInt64")
            fieldType == longNType -> encode("$packageRuntime.encodeLongOrNull")
            fieldType == floatType -> encode("$packageFoundation.encodeFloat")
            fieldType == floatNType -> encode("$packageRuntime.encodeFloatOrNull")
            fieldType == doubleType -> encode("$packageFoundation.encodeDouble")
            fieldType == doubleNType -> encode("$packageRuntime.encodeDoubleOrNull")
            fieldType == shortType -> encode("$packageRuntime.encodeShort")
            fieldType == shortNType -> encode("$packageRuntime.encodeShortOrNull")
            fieldType == byteType -> encode("$packageRuntime.encodeByte")
            fieldType == byteNType -> encode("$packageRuntime.encodeByteOrNull")
            fieldType == charType -> encode("$packageRuntime.encodeChar")
            fieldType == charNType -> encode("$packageRuntime.encodeCharOrNull")
            fieldType == booleanType -> encode("$packageFoundation.encodeBool")
            fieldType == booleanNType -> encode("$packageRuntime.encodeBooleanOrNull")
            fieldType in listOf(stringType, stringNType) -> encode("$packageFoundation.encodeString", stringNType)

            fieldType.isParcelable() ->
                encode("$packageRuntime.encodeParcelable", parcelableType)

            fieldType.classOrNull!! in listOf(listClass, mutableListClass, setClass, mutableSetClass) ->
                encode("$packageRuntime.encodeCollection", collectionType)

            fieldType.classOrNull!! in listOf(mapClass, mutableMapClass) ->
                encode("$packageRuntime.encodeMap", mapType)

            else -> unsupportedFieldError(field)
        }
    }

    private interface FieldValueEncoder {
        fun encode(functionName: String, valueType: IrType? = null)
    }

    // endregion

    // region Decode

    private fun IrClass.addInitWithCoderFunction(mainClass: IrClass) {
        addFunction {
            name = Name.identifier("initWithCoder")
            returnType = pluginContext.referenceClass(nsCodingName)!!.defaultType.withHasQuestionMark(true) // FIXME: x
        }.apply {
            overriddenSymbols = listOf(nsCodingClass.getSimpleFunction("initWithCoder")!!)
            dispatchReceiverParameter = this@addInitWithCoderFunction.thisReceiver?.copyTo(this)

            addValueParameter {
                name = Name.identifier("coder")
                type = nsCoderType
            }

            setInitWithCoderBody(mainClass)
        }
    }

    private fun IrSimpleFunction.setInitWithCoderBody(mainClass: IrClass) {
        val coderArgument = valueParameters[0]

        setBody(pluginContext) {
            val coder = irGet(coderArgument)
            val dataConstructorCall = mainClass.primaryConstructor!!.toIrConstructorCall()

            mainClass.properties.mapNotNull(IrProperty::backingField).forEachIndexed { index, field ->
                addDecodeFieldStatement(
                    field = field,
                    index = index,
                    dataConstructorCall = dataConstructorCall,
                    coder = coder
                )
            }

            val valueConstructorCall = decodedValueClass.owner.primaryConstructor!!.toIrConstructorCall()
            valueConstructorCall.putValueArgument(0, dataConstructorCall)

            +irReturn(valueConstructorCall)
        }
    }

    private fun IrBlockBodyBuilder.addDecodeFieldStatement(
        field: IrField,
        index: Int,
        dataConstructorCall: IrConstructorCall,
        coder: IrExpression
    ) {
        FieldValueDecoder { functionName ->
            dataConstructorCall.putValueArgument(
                index,
                irCall(
                    pluginContext.referenceFunction(
                        name = functionName,
                        extensionReceiverParameterType = nsCoderType,
                        valueParameterTypes = listOf(stringType)
                    )
                ).apply {
                    extensionReceiver = coder
                    putValueArgument(0, irString(field.name.identifier))
                }
            )
        }.decodeField(field)
    }

    private fun FieldValueDecoder.decodeField(field: IrField) {
        val fieldType = field.type

        when {
            fieldType == intType -> decode("$packageFoundation.decodeInt32ForKey")
            fieldType == intNType -> decode("$packageRuntime.decodeIntOrNull")
            fieldType == longType -> decode("$packageFoundation.decodeInt64ForKey")
            fieldType == longNType -> decode("$packageRuntime.decodeLongOrNull")
            fieldType == floatType -> decode("$packageFoundation.decodeFloatForKey")
            fieldType == floatNType -> decode("$packageRuntime.decodeFloatOrNull")
            fieldType == doubleType -> decode("$packageFoundation.decodeDoubleForKey")
            fieldType == doubleNType -> decode("$packageRuntime.decodeDoubleOrNull")
            fieldType == shortType -> decode("$packageRuntime.decodeShort")
            fieldType == shortNType -> decode("$packageRuntime.decodeShortOrNull")
            fieldType == byteType -> decode("$packageRuntime.decodeByte")
            fieldType == byteNType -> decode("$packageRuntime.decodeByteOrNull")
            fieldType == charType -> decode("$packageRuntime.decodeChar")
            fieldType == charNType -> decode("$packageRuntime.decodeCharOrNull")
            fieldType == booleanType -> decode("$packageFoundation.decodeBoolForKey")
            fieldType == booleanNType -> decode("$packageRuntime.decodeBooleanOrNull")

            fieldType.isSubtypeOfClass(parcelableClass) ->
                decode("$packageRuntime.decodeParcelable")

            fieldType.classOrNull!! in listOf(listClass, mutableListClass) ->
                decode("$packageRuntime.decodeList")

            fieldType.classOrNull!! in listOf(setClass, mutableSetClass) ->
                decode("$packageRuntime.decodeSet")

            fieldType.classOrNull!! in listOf(mapClass, mutableMapClass) ->
                decode("$packageRuntime.decodeMap")

            else -> error("Unsupported field type: ${field.dump()}")
        }
    }

    private fun interface FieldValueDecoder {
        fun decode(functionName: String)
    }

    // endregion

    // endregion

    private fun IrClass.generateCodingBody(codingClass: IrClass) {
        functions
            .single { (it.name == codingName) && it.valueParameters.isEmpty() }
            .apply { setCodingBody(codingClass) }
    }

    private fun IrSimpleFunction.setCodingBody(codingClass: IrClass) {
        val thisReceiver = dispatchReceiverParameter!!

        setBody(pluginContext) {
            val constructorCall = codingClass.primaryConstructor!!.toIrConstructorCall()
            constructorCall.putValueArgument(0, irGet(thisReceiver.type, thisReceiver.symbol))
            +irReturn(constructorCall)
        }
    }

    private fun IrClass.addSimpleDelegatingPrimaryConstructor(): IrConstructor =
        addSimpleDelegatingConstructor(
            superConstructor = getSuperClass(irBuiltIns)!!.constructors.first(),
            irBuiltIns = irBuiltIns,
            isPrimary = true
        )


    private fun IrClassSymbol.getCodingName(): String? =
        when {
            isParcelable() -> "Parcelable"
            this == stringClass -> "String"
            else -> null
        }

    private fun unsupportedFieldError(field: IrField): Nothing =
        error("Unsupported field: ${field.dump()}")
}
