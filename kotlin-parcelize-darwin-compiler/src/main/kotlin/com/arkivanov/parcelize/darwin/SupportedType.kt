package com.arkivanov.parcelize.darwin

import org.jetbrains.kotlin.backend.jvm.ir.erasedUpperBound
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.typeOrNull
import org.jetbrains.kotlin.ir.util.defaultType
import org.jetbrains.kotlin.ir.util.isEnumClass
import org.jetbrains.kotlin.ir.util.render

sealed interface SupportedType {

    data class PrimitiveInt(val isNullable: Boolean) : SupportedType
    data class PrimitiveLong(val isNullable: Boolean) : SupportedType
    data class PrimitiveShort(val isNullable: Boolean) : SupportedType
    data class PrimitiveByte(val isNullable: Boolean) : SupportedType
    data class PrimitiveChar(val isNullable: Boolean) : SupportedType
    data class PrimitiveFloat(val isNullable: Boolean) : SupportedType
    data class PrimitiveDouble(val isNullable: Boolean) : SupportedType
    data class PrimitiveBoolean(val isNullable: Boolean) : SupportedType
    object String : SupportedType
    data class Enum(val type: IrType) : SupportedType
    object Parcelable : SupportedType
    data class List(val itemType: SupportedType) : SupportedType
    data class MutableList(val itemType: SupportedType) : SupportedType
    data class Set(val itemType: SupportedType) : SupportedType
    data class MutableSet(val itemType: SupportedType) : SupportedType
    data class Map(val keyType: SupportedType, val valueType: SupportedType) : SupportedType
    data class MutableMap(val keyType: SupportedType, val valueType: SupportedType) : SupportedType
}

fun IrType.toSupportedType(symbols: Symbols): SupportedType =
    when {
        this == symbols.intType -> SupportedType.PrimitiveInt(isNullable = false)
        this == symbols.intNType -> SupportedType.PrimitiveInt(isNullable = true)
        this == symbols.longType -> SupportedType.PrimitiveLong(isNullable = false)
        this == symbols.longNType -> SupportedType.PrimitiveLong(isNullable = true)
        this == symbols.shortType -> SupportedType.PrimitiveShort(isNullable = false)
        this == symbols.shortNType -> SupportedType.PrimitiveShort(isNullable = true)
        this == symbols.byteType -> SupportedType.PrimitiveByte(isNullable = false)
        this == symbols.byteNType -> SupportedType.PrimitiveByte(isNullable = true)
        this == symbols.charType -> SupportedType.PrimitiveChar(isNullable = false)
        this == symbols.charNType -> SupportedType.PrimitiveChar(isNullable = true)
        this == symbols.floatType -> SupportedType.PrimitiveFloat(isNullable = false)
        this == symbols.floatNType -> SupportedType.PrimitiveFloat(isNullable = true)
        this == symbols.doubleType -> SupportedType.PrimitiveDouble(isNullable = false)
        this == symbols.doubleNType -> SupportedType.PrimitiveDouble(isNullable = true)
        this == symbols.booleanType -> SupportedType.PrimitiveBoolean(isNullable = false)
        this == symbols.booleanNType -> SupportedType.PrimitiveBoolean(isNullable = true)
        (this == symbols.stringType) || (this == symbols.stringNType) -> SupportedType.String
        erasedUpperBound.isEnumClass -> SupportedType.Enum(type = this)
        isParcelable() -> SupportedType.Parcelable

        erasedUpperBoundType == symbols.listType ->
            SupportedType.List(itemType = getTypeArgument(0).toSupportedType(symbols))

        erasedUpperBoundType == symbols.mutableListType ->
            SupportedType.MutableList(itemType = getTypeArgument(0).toSupportedType(symbols))

        erasedUpperBoundType == symbols.setType ->
            SupportedType.Set(itemType = getTypeArgument(0).toSupportedType(symbols))

        erasedUpperBoundType == symbols.mutableSetType ->
            SupportedType.MutableSet(itemType = getTypeArgument(0).toSupportedType(symbols))

        erasedUpperBoundType == symbols.mapType ->
            SupportedType.Map(
                keyType = getTypeArgument(0).toSupportedType(symbols),
                valueType = getTypeArgument(1).toSupportedType(symbols),
            )

        erasedUpperBoundType == symbols.mutableMapType ->
            SupportedType.MutableMap(
                keyType = getTypeArgument(0).toSupportedType(symbols),
                valueType = getTypeArgument(1).toSupportedType(symbols),
            )

        else -> error("Unsupported type: ${render()}")
    }

private fun IrType.getTypeArgument(index: Int): IrType =
    asIrSimpleType().arguments[index].typeOrNull!!

private val IrType.erasedUpperBoundType: IrType
    get() = erasedUpperBound.defaultType

