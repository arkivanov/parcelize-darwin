package com.arkivanov.parcelize.darwin

import org.jetbrains.kotlin.backend.common.ClassLoweringPass
import org.jetbrains.kotlin.backend.wasm.ir2wasm.getSuperClass
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.descriptors.DescriptorVisibilities
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.ir.UNDEFINED_OFFSET
import org.jetbrains.kotlin.ir.builders.IrBlockBuilder
import org.jetbrains.kotlin.ir.builders.declarations.addFunction
import org.jetbrains.kotlin.ir.builders.declarations.addGetter
import org.jetbrains.kotlin.ir.builders.declarations.addProperty
import org.jetbrains.kotlin.ir.builders.declarations.addValueParameter
import org.jetbrains.kotlin.ir.builders.declarations.buildClass
import org.jetbrains.kotlin.ir.builders.declarations.buildField
import org.jetbrains.kotlin.ir.builders.irBlock
import org.jetbrains.kotlin.ir.builders.irCall
import org.jetbrains.kotlin.ir.builders.irGet
import org.jetbrains.kotlin.ir.builders.irGetField
import org.jetbrains.kotlin.ir.builders.irGetObject
import org.jetbrains.kotlin.ir.builders.irReturn
import org.jetbrains.kotlin.ir.builders.irReturnTrue
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
import org.jetbrains.kotlin.ir.expressions.IrConstructorCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrStatementOrigin
import org.jetbrains.kotlin.ir.expressions.impl.IrGetValueImpl
import org.jetbrains.kotlin.ir.interpreter.toIrConst
import org.jetbrains.kotlin.ir.symbols.IrConstructorSymbol
import org.jetbrains.kotlin.ir.types.defaultType
import org.jetbrains.kotlin.ir.types.makeNullable
import org.jetbrains.kotlin.ir.util.addChild
import org.jetbrains.kotlin.ir.util.addSimpleDelegatingConstructor
import org.jetbrains.kotlin.ir.util.constructors
import org.jetbrains.kotlin.ir.util.copyTo
import org.jetbrains.kotlin.ir.util.createImplicitParameterDeclarationWithWrappedDescriptor
import org.jetbrains.kotlin.ir.util.defaultType
import org.jetbrains.kotlin.ir.util.functions
import org.jetbrains.kotlin.ir.util.getSimpleFunction
import org.jetbrains.kotlin.ir.util.isObject
import org.jetbrains.kotlin.ir.util.primaryConstructor
import org.jetbrains.kotlin.ir.util.properties
import org.jetbrains.kotlin.name.Name

class ParcelizeClassLoweringPass(
    context: Context,
    private val coderFactory: CoderFactory,
    private val logs: MessageCollector
) : ClassLoweringPass, Context by context {

    override fun lower(irClass: IrClass) {
        if (!irClass.toIrBasedDescriptor().isParcelize()) {
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

        val codingCompanionClass = codingClass.addCodingCompanionObject()
        codingCompanionClass.addSupportsSecureCodingFunction()

        irClass.generateCodingBody(codingClass)
    }

    // region CodingImpl

    private fun IrClass.addCodingClass(): IrClass =
        irFactory
            .buildClass {
                name = Name.identifier("CodingImpl")
                kind = ClassKind.CLASS
                visibility = DescriptorVisibilities.PRIVATE
                modality = Modality.FINAL
            }
            .also(::addChild)
            .apply {
                superTypes = listOf(nsLockType, nsCodingType)
                annotations = listOf(getExportObjCClassAnnotationCall(name = getFullCapitalizedName()))
                createImplicitParameterDeclarationWithWrappedDescriptor()
            }

    private fun IrClass.addCodingCompanionObject(): IrClass =
        irFactory
            .buildClass {
                name = Name.identifier("Companion")
                kind = ClassKind.OBJECT
                visibility = DescriptorVisibilities.PUBLIC
                modality = Modality.FINAL
                isCompanion = true
            }
            .also(::addChild)
            .apply {
                superTypes = listOf(nsObjectType, nsCodingMetaType)
                createImplicitParameterDeclarationWithWrappedDescriptor()
            }

    private fun IrClass.addSupportsSecureCodingFunction() {
        addFunction {
            name = Name.identifier("supportsSecureCoding")
            returnType = booleanType
        }.apply {
            overriddenSymbols = listOf(nsCodingMetaClass.getSimpleFunction("supportsSecureCoding")!!)
            dispatchReceiverParameter = this@addSupportsSecureCodingFunction.thisReceiver?.copyTo(this)

            setBody(pluginContext) {
                +irReturnTrue()
            }
        }
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

    private fun IrSimpleFunction.setEncodeWithCoderBody(mainClass: IrClass, dataGetter: IrFunction) {
        val thisReceiver = dispatchReceiverParameter!!
        val coderArgument = valueParameters[0]

        setBody(pluginContext) {
            +irBlock {
                val data = irCall(dataGetter, IrStatementOrigin.GET_PROPERTY)
                data.dispatchReceiver = irGet(thisReceiver.type, thisReceiver.symbol)
                val coder = irGet(coderArgument)

                mainClass.parcelableFields.forEach { field ->
                    try {
                        addEncodeFieldStatement(
                            field = field,
                            data = data,
                            coder = coder
                        )
                    } catch (e: Throwable) {
                        throw IllegalStateException(
                            "Error generating encode statement for field: ${field.description} " +
                                "of class ${mainClass.defaultType.description}",
                            e,
                        )
                    }
                }
            }
        }
    }

    private fun IrBlockBuilder.addEncodeFieldStatement(field: IrField, data: IrExpression, coder: IrExpression) {
        +with(coderFactory.get(field.type)) {
            encode(
                coder = coder,
                value = irGetField(data, field),
                key = irString(field.name.identifier),
            )
        }
    }

    // endregion

    // region Decode

    private fun IrClass.addInitWithCoderFunction(mainClass: IrClass) {
        addFunction {
            name = Name.identifier("initWithCoder")
            returnType = pluginContext.referenceClass(nsCodingName)!!.defaultType.makeNullable() // FIXME: x
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
            +irBlock {
                val valueConstructorCall = decodedValueClass.owner.primaryConstructor!!.toIrConstructorCall()

                if (mainClass.isObject) {
                    valueConstructorCall.putValueArgument(0, irGetObject(mainClass.symbol))
                } else {
                    val coder = irGet(coderArgument)
                    val dataConstructorCall = mainClass.primaryConstructor!!.toIrConstructorCall()

                    val valueParameterNames = mainClass.primaryConstructor!!.valueParameters.map { it.name }
                    mainClass.properties
                        .filter { it.name in valueParameterNames }
                        .mapNotNull(IrProperty::backingField)
                        .forEachIndexed { index, field ->
                            try {
                                addDecodeFieldStatement(
                                    field = field,
                                    index = index,
                                    dataConstructorCall = dataConstructorCall,
                                    coder = coder
                                )
                            } catch (e: Throwable) {
                                throw IllegalStateException("Error generating decode statement for field: ${field.description}", e)
                            }
                        }

                    valueConstructorCall.putValueArgument(0, dataConstructorCall)
                }

                +irReturn(valueConstructorCall)
            }
        }
    }

    private fun IrBlockBuilder.addDecodeFieldStatement(
        field: IrField,
        index: Int,
        dataConstructorCall: IrConstructorCall,
        coder: IrExpression
    ) {
        dataConstructorCall.putValueArgument(
            index = index,
            valueArgument = with(coderFactory.get(field.type)) {
                decode(
                    coder = coder,
                    key = irString(field.name.identifier),
                )
            },
        )
    }

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
}
