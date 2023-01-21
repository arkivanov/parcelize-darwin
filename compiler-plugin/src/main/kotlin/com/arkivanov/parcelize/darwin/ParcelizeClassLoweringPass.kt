package com.arkivanov.parcelize.darwin

import org.jetbrains.kotlin.backend.common.ClassLoweringPass
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.common.lower.irIfThen
import org.jetbrains.kotlin.backend.common.lower.irThrow
import org.jetbrains.kotlin.backend.wasm.ir2wasm.getSuperClass
import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.descriptors.DescriptorVisibilities
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.ir.UNDEFINED_OFFSET
import org.jetbrains.kotlin.ir.builders.IrBlockBodyBuilder
import org.jetbrains.kotlin.ir.builders.IrBlockBuilder
import org.jetbrains.kotlin.ir.builders.createTmpVariable
import org.jetbrains.kotlin.ir.builders.declarations.addFunction
import org.jetbrains.kotlin.ir.builders.declarations.addGetter
import org.jetbrains.kotlin.ir.builders.declarations.addProperty
import org.jetbrains.kotlin.ir.builders.declarations.addValueParameter
import org.jetbrains.kotlin.ir.builders.declarations.buildClass
import org.jetbrains.kotlin.ir.builders.declarations.buildField
import org.jetbrains.kotlin.ir.builders.irBlock
import org.jetbrains.kotlin.ir.builders.irGet
import org.jetbrains.kotlin.ir.builders.irGetField
import org.jetbrains.kotlin.ir.builders.irGetObject
import org.jetbrains.kotlin.ir.builders.irInt
import org.jetbrains.kotlin.ir.builders.irNotEquals
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
import org.jetbrains.kotlin.ir.expressions.IrFunctionAccessExpression
import org.jetbrains.kotlin.ir.expressions.IrStatementOrigin
import org.jetbrains.kotlin.ir.expressions.impl.IrGetValueImpl
import org.jetbrains.kotlin.ir.interpreter.toIrConst
import org.jetbrains.kotlin.ir.symbols.IrConstructorSymbol
import org.jetbrains.kotlin.ir.util.SYNTHETIC_OFFSET
import org.jetbrains.kotlin.ir.util.addChild
import org.jetbrains.kotlin.ir.util.addSimpleDelegatingConstructor
import org.jetbrains.kotlin.ir.util.constructors
import org.jetbrains.kotlin.ir.util.copyTo
import org.jetbrains.kotlin.ir.util.createImplicitParameterDeclarationWithWrappedDescriptor
import org.jetbrains.kotlin.ir.util.defaultType
import org.jetbrains.kotlin.ir.util.functions
import org.jetbrains.kotlin.ir.util.isObject
import org.jetbrains.kotlin.ir.util.primaryConstructor
import org.jetbrains.kotlin.ir.util.properties
import org.jetbrains.kotlin.ir.util.render
import org.jetbrains.kotlin.name.Name

class ParcelizeClassLoweringPass(
    private val context: IrPluginContext,
    private val symbols: Symbols,
    private val coderFactory: CoderFactory,
) : ClassLoweringPass {

    private val irFactory = context.irFactory
    private val irBuiltIns = context.irBuiltIns

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

        val mainClassHash = irClass.calculateHash()

        codingClass.addEncodeWithCoderFunction(
            mainClass = irClass,
            dataGetter = dataGetter,
            mainClassHash = mainClassHash,
        )

        codingClass.addInitWithCoderFunction(
            mainClass = irClass,
            mainClassHash = mainClassHash,
        )

        val codingCompanionClass = codingClass.addCodingCompanionObject()
        codingCompanionClass.addSupportsSecureCodingFunction()

        irClass.generateCodingBody(codingClass)
    }

    private fun IrClass.calculateHash(): Int {
        var hash = if (isObject) 1 else 0

        parcelableFields.forEach { field ->
            hash = hash * 31 + field.render().hashCode()
        }

        return hash
    }

    // region CodingImpl

    private fun IrClass.addCodingClass(): IrClass =
        irFactory
            .buildClass {
                name = Name.identifier("CodingImpl")
                kind = ClassKind.CLASS
                visibility = DescriptorVisibilities.PRIVATE
                modality = Modality.FINAL
                startOffset = SYNTHETIC_OFFSET
                endOffset = SYNTHETIC_OFFSET
            }
            .also(::addChild)
            .apply {
                superTypes = listOf(symbols.nsLockType, symbols.nsSecureCodingType)
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
                superTypes = listOf(symbols.nsObjectType, symbols.nsSecureCodingMetaType)
                createImplicitParameterDeclarationWithWrappedDescriptor()
            }

    private fun IrClass.addSupportsSecureCodingFunction() {
        addFunction {
            name = Name.identifier("supportsSecureCoding")
            returnType = symbols.booleanType
        }.apply {
            overriddenSymbols = listOf(symbols.nsSecureCodingMetaType.requireClass().requireFunction(name = "supportsSecureCoding"))
            dispatchReceiverParameter = this@addSupportsSecureCodingFunction.thisReceiver?.copyTo(this)

            setBody(context) {
                +irReturnTrue()
            }
        }
    }

    private fun getExportObjCClassAnnotationCall(name: String = ""): IrConstructorCall =
        context
            .referenceConstructors(exportObjCClassClassId)
            .map(IrConstructorSymbol::owner)
            .single { it.valueParameters.map(IrValueParameter::type) == listOf(symbols.stringType) }
            .toIrConstructorCall()
            .apply { putValueArgument(0, name.toIrConst(symbols.stringType)) }

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

            setBody(context) {
                +irReturn(
                    irGetField(
                        irGet(receiverParameter.type, receiverParameter.symbol),
                        dataField
                    )
                )
            }
        }

    // region Encode

    private fun IrClass.addEncodeWithCoderFunction(mainClass: IrClass, dataGetter: IrFunction, mainClassHash: Int) {
        addFunction {
            name = Name.identifier("encodeWithCoder")
            returnType = symbols.unitType
        }.apply {
            overriddenSymbols = listOf(symbols.nsSecureCodingType.requireClass().requireFunction(name = "encodeWithCoder"))
            dispatchReceiverParameter = this@addEncodeWithCoderFunction.thisReceiver?.copyTo(this)

            addValueParameter {
                name = Name.identifier("coder")
                type = symbols.nsCoderType
            }

            setEncodeWithCoderBody(
                mainClass = mainClass,
                dataGetter = dataGetter,
                mainClassHash = mainClassHash,
            )
        }
    }

    private fun IrSimpleFunction.setEncodeWithCoderBody(mainClass: IrClass, dataGetter: IrFunction, mainClassHash: Int) {
        val thisReceiver = dispatchReceiverParameter!!
        val coderArgument = valueParameters[0]

        setBody(context) {
            +irBlock {
                val coder = irGet(coderArgument)

                addEncodeHashStatement(mainClassHash = mainClassHash, coder = coder)

                if (!mainClass.isObject) {
                    val data =
                        irCallCompat(callee = dataGetter, origin = IrStatementOrigin.GET_PROPERTY) {
                            dispatchReceiver = irGet(thisReceiver.type, thisReceiver.symbol)
                        }

                    mainClass.parcelableFields.forEach { field ->
                        try {
                            addEncodeFieldStatement(
                                field = field,
                                data = data,
                                coder = coder
                            )
                        } catch (e: Throwable) {
                            throw IllegalStateException(
                                "Error generating encode statement for ${field.render()} of ${mainClass.render()}",
                                e
                            )
                        }
                    }
                }
            }
        }
    }

    private fun IrBlockBuilder.addEncodeHashStatement(mainClassHash: Int, coder: IrExpression) {
        with(coderFactory.get(type = SupportedType.PrimitiveInt(isNullable = false))) {
            +encode(
                value = irInt(mainClassHash),
                coder = coder,
                key = irString("__parcelize_hash"),
            )
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

    private fun IrClass.addInitWithCoderFunction(mainClass: IrClass, mainClassHash: Int) {
        addFunction {
            name = Name.identifier("initWithCoder")
            returnType = symbols.nsSecureCodingType
        }.apply {
            overriddenSymbols = listOf(symbols.nsSecureCodingType.requireClass().requireFunction(name = "initWithCoder"))
            dispatchReceiverParameter = this@addInitWithCoderFunction.thisReceiver?.copyTo(this)

            addValueParameter {
                name = Name.identifier("coder")
                type = symbols.nsCoderType
            }

            setInitWithCoderBody(mainClass, mainClassHash)
        }
    }

    private fun IrSimpleFunction.setInitWithCoderBody(mainClass: IrClass, mainClassHash: Int) {
        val coderArgument = valueParameters[0]

        setBody(context) {
            val decodedHash =
                with(coderFactory.get(type = SupportedType.PrimitiveInt(isNullable = false))) {
                    decode(
                        coder = irGet(coderArgument),
                        key = irString("__parcelize_hash"),
                    )
                }

            +irIfThen(
                condition = irNotEquals(decodedHash, irInt(mainClassHash)),
                thenPart = irThrow(
                    irCallCompat(callee = symbols.illegalStateExceptionConstructor) {
                        putValueArgument(0, irString("Signature mismatch for ${mainClass.render()}"))
                    }
                ),
            )

            val nsMutableArray = createTmpVariable(irCallCompat(symbols.nsMutableArrayConstructor))

            if (mainClass.isObject) {
                +irCallCompat(
                    callee = symbols.nsMutableArrayType.requireClass().requireFunction(
                        name = "addObject",
                        valueParameterTypes = listOf(symbols.anyNType),
                    ),
                    dispatchReceiver = irGet(nsMutableArray),
                    arguments = listOf(irGetObject(mainClass.symbol)),
                )
            } else {
                val coder = irGet(coderArgument)
                val dataConstructorCall =
                    irCallCompat(mainClass.primaryConstructor!!.symbol) {
                        val valueParameterNames = mainClass.primaryConstructor!!.valueParameters.map { it.name }
                        mainClass.properties
                            .filter { it.name in valueParameterNames }
                            .mapNotNull(IrProperty::backingField)
                            .forEachIndexed { index, field ->
                                try {
                                    addDecodeFieldStatement(
                                        field = field,
                                        index = index,
                                        dataConstructorCall = this,
                                        coder = coder
                                    )
                                } catch (e: Throwable) {
                                    throw IllegalStateException("Error generating decode statement for ${field.render()}", e)
                                }
                            }
                    }

                +irCallCompat(
                    callee = symbols.nsMutableArrayType.requireClass().requireFunction(
                        name = "addObject",
                        valueParameterTypes = listOf(symbols.anyNType),
                    ),
                    dispatchReceiver = irGet(nsMutableArray),
                    arguments = listOf(dataConstructorCall),
                )
            }

            +irReturn(irGet(nsMutableArray))
        }
    }

    private fun IrBlockBodyBuilder.addDecodeFieldStatement(
        field: IrField,
        index: Int,
        dataConstructorCall: IrFunctionAccessExpression,
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

        setBody(context) {
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
