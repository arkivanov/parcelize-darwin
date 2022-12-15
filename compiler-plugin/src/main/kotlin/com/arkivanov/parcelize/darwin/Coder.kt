package com.arkivanov.parcelize.darwin

import org.jetbrains.kotlin.ir.builders.IrBuilderWithScope
import org.jetbrains.kotlin.ir.builders.createTmpVariable
import org.jetbrains.kotlin.ir.builders.irBlock
import org.jetbrains.kotlin.ir.builders.irBoolean
import org.jetbrains.kotlin.ir.builders.irCall
import org.jetbrains.kotlin.ir.builders.irEquals
import org.jetbrains.kotlin.ir.builders.irGet
import org.jetbrains.kotlin.ir.builders.irGetObject
import org.jetbrains.kotlin.ir.builders.irIfNull
import org.jetbrains.kotlin.ir.builders.irIfThen
import org.jetbrains.kotlin.ir.builders.irIfThenElse
import org.jetbrains.kotlin.ir.builders.irInt
import org.jetbrains.kotlin.ir.builders.irLong
import org.jetbrains.kotlin.ir.builders.irNotEquals
import org.jetbrains.kotlin.ir.builders.irNull
import org.jetbrains.kotlin.ir.builders.irString
import org.jetbrains.kotlin.ir.builders.irTrue
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.symbols.IrConstructorSymbol
import org.jetbrains.kotlin.ir.symbols.IrSimpleFunctionSymbol
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.classOrNull
import org.jetbrains.kotlin.ir.types.getClass
import org.jetbrains.kotlin.ir.types.isString
import org.jetbrains.kotlin.ir.types.makeNullable
import org.jetbrains.kotlin.ir.util.companionObject
import org.jetbrains.kotlin.ir.util.functions
import org.jetbrains.kotlin.ir.util.getPropertyGetter

interface Coder {

    fun IrBuilderWithScope.encode(
        coder: IrExpression,
        value: IrExpression,
        key: IrExpression,
    ): IrExpression

    fun IrBuilderWithScope.decode(
        coder: IrExpression,
        key: IrExpression,
    ): IrExpression
}

class CoderFactory(
    private val symbols: Symbols,
) {

    fun get(type: IrType): Coder =
        get(type = type.toSupportedType(symbols))

    fun get(type: SupportedType): Coder =
        when (type) {
            is SupportedType.PrimitiveInt ->
                PrimitiveCoder(
                    symbols = symbols,
                    isNullable = type.isNullable,
                    encodeFunction = symbols.encodeInt,
                    decodeFunction = symbols.decodeInt,
                )

            is SupportedType.PrimitiveLong ->
                PrimitiveCoder(
                    symbols = symbols,
                    isNullable = type.isNullable,
                    encodeFunction = symbols.encodeLong,
                    decodeFunction = symbols.decodeLong,
                )

            is SupportedType.PrimitiveShort ->
                PrimitiveCoder(
                    symbols = symbols,
                    isNullable = type.isNullable,
                    encodeFunction = symbols.encodeInt,
                    decodeFunction = symbols.decodeInt,
                    valueTo = { irCallCompat(callee = symbols.shortToInt, dispatchReceiver = it) },
                    valueFrom = { irCallCompat(callee = symbols.intToShort, dispatchReceiver = it) },
                )

            is SupportedType.PrimitiveByte ->
                PrimitiveCoder(
                    symbols = symbols,
                    isNullable = type.isNullable,
                    encodeFunction = symbols.encodeInt,
                    decodeFunction = symbols.decodeInt,
                    valueTo = { irCallCompat(callee = symbols.byteToInt, dispatchReceiver = it) },
                    valueFrom = { irCallCompat(callee = symbols.intToByte, dispatchReceiver = it) },
                )

            is SupportedType.PrimitiveChar ->
                PrimitiveCoder(
                    symbols = symbols,
                    isNullable = type.isNullable,
                    encodeFunction = symbols.encodeInt,
                    decodeFunction = symbols.decodeInt,
                    valueTo = { irCallCompat(callee = symbols.charToInt, dispatchReceiver = it) },
                    valueFrom = { irCallCompat(callee = symbols.intToChar, dispatchReceiver = it) },
                )

            is SupportedType.PrimitiveBoolean ->
                PrimitiveCoder(
                    symbols = symbols,
                    isNullable = type.isNullable,
                    encodeFunction = symbols.encodeBoolean,
                    decodeFunction = symbols.decodeBoolean,
                )

            is SupportedType.PrimitiveFloat ->
                PrimitiveCoder(
                    symbols = symbols,
                    isNullable = type.isNullable,
                    encodeFunction = symbols.encodeFloat,
                    decodeFunction = symbols.decodeFloat,
                )

            is SupportedType.PrimitiveDouble ->
                PrimitiveCoder(
                    symbols = symbols,
                    isNullable = type.isNullable,
                    encodeFunction = symbols.encodeDouble,
                    decodeFunction = symbols.decodeDouble,
                )

            is SupportedType.String -> StringCoder(symbols = symbols)

            is SupportedType.Enum ->
                EnumCoder(
                    symbols = symbols,
                    type = type.type,
                    stringCoder = get(type = SupportedType.String),
                )

            is SupportedType.Parcelable -> ParcelableCoder(symbols = symbols)

            is SupportedType.List ->
                CollectionCoder(
                    symbols = symbols,
                    collectionConstructor = symbols.arrayListConstructor,
                    itemCoder = get(type = type.itemType),
                )

            is SupportedType.MutableList ->
                CollectionCoder(
                    symbols = symbols,
                    collectionConstructor = symbols.arrayListConstructor,
                    itemCoder = get(type = type.itemType),
                )

            is SupportedType.Set ->
                CollectionCoder(
                    symbols = symbols,
                    collectionConstructor = symbols.hashSetConstructor,
                    itemCoder = get(type = type.itemType),
                )

            is SupportedType.MutableSet ->
                CollectionCoder(
                    symbols = symbols,
                    collectionConstructor = symbols.hashSetConstructor,
                    itemCoder = get(type = type.itemType),
                )

            is SupportedType.Map ->
                MapCoder(
                    symbols = symbols,
                    mapConstructor = symbols.hashMapConstructor,
                    keysCoder = get(type = SupportedType.List(itemType = type.keyType)),
                    valuesCoder = get(type = SupportedType.List(itemType = type.valueType)),
                )

            is SupportedType.MutableMap ->
                MapCoder(
                    symbols = symbols,
                    mapConstructor = symbols.hashMapConstructor,
                    keysCoder = get(type = SupportedType.List(itemType = type.keyType)),
                    valuesCoder = get(type = SupportedType.List(itemType = type.valueType)),
                )
        }
}

private class PrimitiveCoder(
    private val symbols: Symbols,
    private val isNullable: Boolean,
    private val encodeFunction: IrSimpleFunctionSymbol,
    private val decodeFunction: IrSimpleFunctionSymbol,
    private val valueTo: IrBuilderWithScope.(IrExpression) -> IrExpression = { it },
    private val valueFrom: IrBuilderWithScope.(IrExpression) -> IrExpression = { it },
) : Coder {

    override fun IrBuilderWithScope.encode(coder: IrExpression, value: IrExpression, key: IrExpression): IrExpression {
        val encode =
            irCallCompat(
                callee = encodeFunction,
                extensionReceiver = coder,
                arguments = listOf(valueTo(value), key),
            )

        return if (isNullable) {
            irIfNull(
                type = symbols.unitType,
                subject = value,
                thenPart = irCallCompat(
                    callee = symbols.encodeBoolean,
                    extensionReceiver = coder,
                    arguments = listOf(irBoolean(false), irConcatStrings(key, irString("-exists"))),
                ),
                elsePart = irBlock {
                    +irCallCompat(
                        callee = symbols.encodeBoolean,
                        extensionReceiver = coder,
                        arguments = listOf(irBoolean(true), irConcatStrings(key, irString("-exists"))),
                    )

                    +encode
                },
            )
        } else {
            encode
        }
    }

    override fun IrBuilderWithScope.decode(coder: IrExpression, key: IrExpression): IrExpression {
        val decode =
            valueFrom(
                irCallCompat(
                    callee = decodeFunction,
                    extensionReceiver = coder,
                    arguments = listOf(key),
                )
            )

        return if (isNullable) {
            irIfThenElse(
                type = decode.type.makeNullable(),
                condition = irCallCompat(
                    callee = symbols.decodeBoolean,
                    extensionReceiver = coder,
                    arguments = listOf(irConcatStrings(key, irString("-exists"))),
                ),
                thenPart = decode,
                elsePart = irNull(),
            )
        } else {
            decode
        }
    }
}

private class StringCoder(
    private val symbols: Symbols,
) : Coder {

    override fun IrBuilderWithScope.encode(coder: IrExpression, value: IrExpression, key: IrExpression): IrExpression =
        irCallCompat(
            callee = symbols.encodeObject,
            extensionReceiver = coder,
            arguments = listOf(value, key),
        )

    override fun IrBuilderWithScope.decode(coder: IrExpression, key: IrExpression): IrExpression =
        irCallCompat(
            callee = symbols.decodeObject,
            extensionReceiver = coder,
            arguments = listOf(
                irGetObject(symbols.nsStringClass.owner.companionObject()!!.symbol),
                key,
            ),
        )
}

private class EnumCoder(
    private val symbols: Symbols,
    private val type: IrType,
    private val stringCoder: Coder,
) : Coder {
    private val enumValueOf =
        type.getClass()!!.functions.first { function ->
            (function.name.asString() == "valueOf") &&
                (function.dispatchReceiverParameter == null) &&
                (function.extensionReceiverParameter == null) &&
                (function.valueParameters.size == 1) &&
                function.valueParameters.single().type.isString()
        }.symbol

    override fun IrBuilderWithScope.encode(coder: IrExpression, value: IrExpression, key: IrExpression): IrExpression =
        irBlock {
            val name =
                createTmpVariable(
                    irIfNull(
                        type = symbols.stringType,
                        subject = value,
                        thenPart = irNull(),
                        elsePart = irCallCompat(
                            callee = type.getClass()!!.getPropertyGetter("name")!!,
                            dispatchReceiver = value,
                        ),
                    )
                )

            with(stringCoder) {
                +encode(
                    coder = coder,
                    value = irGet(name),
                    key = key,
                )
            }
        }

    override fun IrBuilderWithScope.decode(coder: IrExpression, key: IrExpression): IrExpression =
        irBlock {
            val name =
                createTmpVariable(
                    with(stringCoder) {
                        decode(coder = coder, key = key)
                    },
                )

            +irIfNull(
                type = type,
                subject = irGet(name),
                thenPart = irNull(),
                elsePart = irCallCompat(
                    callee = enumValueOf,
                    arguments = listOf(irGet(name)),
                ),
            )
        }
}

private class ParcelableCoder(
    private val symbols: Symbols,
) : Coder {

    override fun IrBuilderWithScope.encode(coder: IrExpression, value: IrExpression, key: IrExpression): IrExpression =
        irIfNull(
            type = symbols.unitType,
            subject = value,
            thenPart = irCallCompat(
                callee = symbols.encodeObject,
                extensionReceiver = coder,
                arguments = listOf(irNull(), key),
            ),
            elsePart = irCallCompat(
                callee = symbols.encodeObject,
                extensionReceiver = coder,
                arguments = listOf(
                    irCallCompat(callee = symbols.getCoding, dispatchReceiver = value),
                    key,
                ),
            ),
        )

    override fun IrBuilderWithScope.decode(coder: IrExpression, key: IrExpression): IrExpression =
        irBlock {
            val nsMutableArray =
                createTmpVariable(
                    irCallCompat(
                        callee = symbols.decodeObject,
                        extensionReceiver = coder,
                        arguments = listOf(
                            irGetObject(symbols.nsLockType.requireClass().companionObject()!!.symbol),
                            key,
                        ),
                    ),
                )

            +irIfNull(
                type = symbols.parcelableNType,
                subject = irGet(nsMutableArray),
                thenPart = irNull(),
                elsePart = irCallCompat(
                    callee = symbols.nsArrayType.requireClass().requireFunction(name = "objectAtIndex"),
                    dispatchReceiver = irGet(nsMutableArray),
                    arguments = listOf(irLong(value = 0, type = symbols.uLongType)),
                ),
            )
        }
}

private class CollectionCoder(
    private val symbols: Symbols,
    private val collectionConstructor: IrConstructorSymbol,
    private val itemCoder: Coder,
) : Coder {

    override fun IrBuilderWithScope.encode(coder: IrExpression, value: IrExpression, key: IrExpression): IrExpression =
        irBlock {
            val collection = createTmpVariable(value)

            val size =
                createTmpVariable(
                    irIfNull(
                        type = symbols.intType,
                        subject = irGet(collection),
                        thenPart = irInt(-1),
                        elsePart = irCallCompat(
                            callee = symbols.collectionType.classOrNull!!.getPropertyGetter("size")!!,
                            dispatchReceiver = irGet(collection),
                        ),
                    )
                )

            +irCallCompat(
                callee = symbols.encodeInt,
                extensionReceiver = coder,
                arguments = listOf(irGet(size), irConcatStrings(key, irString("-size"))),
            )

            +irIfThen(
                type = symbols.unitType,
                condition = irNotEquals(irGet(size), irInt(-1)),
                thenPart = irBlock {
                    val iterator =
                        createTmpVariable(
                            irCallCompat(
                                callee = symbols.collectionType.requireClass().requireFunction(name = "iterator"),
                                dispatchReceiver = irGet(collection),
                            ),
                        )

                    val index = createTmpVariable(irExpression = irInt(0), isMutable = true)

                    +irWhile(
                        condition = irEquals(
                            arg1 = irCallCompat(
                                callee = symbols.iteratorType.requireClass().requireFunction(name = "hasNext"),
                                dispatchReceiver = irGet(iterator),
                            ),
                            arg2 = irTrue(),
                        ),
                        body = irBlock {
                            val item =
                                createTmpVariable(
                                    irCallCompat(
                                        callee = symbols.iteratorType.requireClass().requireFunction(name = "next"),
                                        dispatchReceiver = irGet(iterator),
                                    )
                                )

                            with(itemCoder) {
                                +encode(
                                    coder = coder,
                                    value = irGet(item),
                                    key = irConcatStrings(key, irString("-"), irGet(index)),
                                )
                            }

                            +irIncrementVariable(index)
                        }
                    )
                },
            )
        }

    override fun IrBuilderWithScope.decode(coder: IrExpression, key: IrExpression): IrExpression =
        irBlock {
            val size =
                createTmpVariable(
                    irCallCompat(
                        callee = symbols.decodeInt,
                        extensionReceiver = coder,
                        arguments = listOf(irConcatStrings(key, irString("-size"))),
                    )
                )

            +irIfThenElse(
                type = collectionConstructor.owner.returnType.makeNullable(),
                condition = irNotEquals(irGet(size), irInt(-1)),
                thenPart = irBlock {
                    val collection = createTmpVariable(irCallCompat(callee = collectionConstructor))
                    val index = createTmpVariable(irExpression = irInt(0), isMutable = true)

                    +irWhile(
                        condition = irNotEquals(arg1 = irGet(index), arg2 = irGet(size)),
                        body = irBlock {
                            +irCallCompat(
                                callee = collectionConstructor.owner.returnType.requireClass().requireFunction(name = "add"),
                                dispatchReceiver = irGet(collection),
                                arguments = listOf(
                                    with(itemCoder) {
                                        decode(
                                            coder = coder,
                                            key = irConcatStrings(key, irString("-"), irGet(index)),
                                        )
                                    },
                                ),
                            )

                            +irIncrementVariable(index)
                        }
                    )

                    +irGet(collection)
                },
                elsePart = irNull(),
            )
        }
}

private class MapCoder(
    private val symbols: Symbols,
    private val mapConstructor: IrConstructorSymbol,
    private val keysCoder: Coder,
    private val valuesCoder: Coder,
) : Coder {

    override fun IrBuilderWithScope.encode(coder: IrExpression, value: IrExpression, key: IrExpression): IrExpression =
        irBlock {
            with(keysCoder) {
                +encode(
                    coder = coder,
                    value = irIfNull(
                        type = symbols.collectionType,
                        subject = value,
                        thenPart = irNull(),
                        elsePart = irCallCompat(
                            callee = value.type.getClass()!!.getPropertyGetter("keys")!!,
                            dispatchReceiver = value,
                        ),
                    ),
                    key = irConcatStrings(key, irString("-keys")),
                )
            }

            with(valuesCoder) {
                +encode(
                    coder = coder,
                    value = irIfNull(
                        type = symbols.collectionType,
                        subject = value,
                        thenPart = irNull(),
                        elsePart = irCallCompat(
                            callee = value.type.getClass()!!.getPropertyGetter("values")!!,
                            dispatchReceiver = value,
                        ),
                    ),
                    key = irConcatStrings(key, irString("-values")),
                )
            }
        }

    override fun IrBuilderWithScope.decode(coder: IrExpression, key: IrExpression): IrExpression =
        irBlock {
            val keys =
                createTmpVariable(
                    with(keysCoder) {
                        decode(
                            coder = coder,
                            key = irConcatStrings(key, irString("-keys")),
                        )
                    }
                )

            val values =
                createTmpVariable(
                    with(valuesCoder) {
                        decode(
                            coder = coder,
                            key = irConcatStrings(key, irString("-values")),
                        )
                    }
                )

            val newMapType = mapConstructor.owner.returnType

            +irIfNull(
                type = newMapType.makeNullable(),
                subject = irGet(keys),
                thenPart = irNull(),
                elsePart = irBlock {
                    val map = createTmpVariable(irCallCompat(callee = mapConstructor))

                    val keysSize =
                        createTmpVariable(
                            irCallCompat(
                                callee = symbols.listType.classOrNull!!.getPropertyGetter("size")!!,
                                dispatchReceiver = irGet(keys),
                            )
                        )

                    val index = createTmpVariable(irExpression = irInt(0), isMutable = true)

                    +irWhile(
                        condition = irNotEquals(arg1 = irGet(index), arg2 = irGet(keysSize)),
                        body = irBlock {
                            +irCallCompat(
                                callee = mapConstructor.owner.returnType.requireClass().requireFunction(name = "put"),
                                dispatchReceiver = irGet(map),
                                arguments = listOf(
                                    irCallCompat(
                                        callee = symbols.listType.requireClass().requireFunction(name = "get"),
                                        dispatchReceiver = irGet(keys),
                                        arguments = listOf(irGet(index)),
                                    ),
                                    irCallCompat(
                                        callee = symbols.listType.requireClass().requireFunction(name = "get"),
                                        dispatchReceiver = irGet(values),
                                        arguments = listOf(irGet(index)),
                                    ),
                                ),
                            )

                            +irIncrementVariable(index)
                        }
                    )

                    +irGet(map)
                },
            )
        }
}
