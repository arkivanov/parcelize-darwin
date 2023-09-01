package com.arkivanov.parcelize.darwin

import org.jetbrains.kotlin.backend.jvm.ir.erasedUpperBound
import org.jetbrains.kotlin.ir.backend.js.utils.typeArguments
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.makeNotNull
import org.jetbrains.kotlin.ir.types.typeOrNull
import org.jetbrains.kotlin.ir.util.defaultType
import org.jetbrains.kotlin.ir.util.getAnnotation
import org.jetbrains.kotlin.ir.util.hasAnnotation
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
    data class Custom(val type: IrType, val parcelerType: IrType) : SupportedType
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


class IrTypeToSupportedTypeMapper(
    private val symbols: Symbols,
    private val typeParcelers: Map<IrType, IrType>,
) {
    fun map(type: IrType): SupportedType =
        map(type = type, nonNullType = type.makeNotNull())

    private fun map(type: IrType, nonNullType: IrType): SupportedType =
        when {
            type == symbols.intType -> SupportedType.PrimitiveInt(isNullable = false)
            type == symbols.intNType -> SupportedType.PrimitiveInt(isNullable = true)
            type == symbols.longType -> SupportedType.PrimitiveLong(isNullable = false)
            type == symbols.longNType -> SupportedType.PrimitiveLong(isNullable = true)
            type == symbols.shortType -> SupportedType.PrimitiveShort(isNullable = false)
            type == symbols.shortNType -> SupportedType.PrimitiveShort(isNullable = true)
            type == symbols.byteType -> SupportedType.PrimitiveByte(isNullable = false)
            type == symbols.byteNType -> SupportedType.PrimitiveByte(isNullable = true)
            type == symbols.charType -> SupportedType.PrimitiveChar(isNullable = false)
            type == symbols.charNType -> SupportedType.PrimitiveChar(isNullable = true)
            type == symbols.floatType -> SupportedType.PrimitiveFloat(isNullable = false)
            type == symbols.floatNType -> SupportedType.PrimitiveFloat(isNullable = true)
            type == symbols.doubleType -> SupportedType.PrimitiveDouble(isNullable = false)
            type == symbols.doubleNType -> SupportedType.PrimitiveDouble(isNullable = true)
            type == symbols.booleanType -> SupportedType.PrimitiveBoolean(isNullable = false)
            type == symbols.booleanNType -> SupportedType.PrimitiveBoolean(isNullable = true)

            nonNullType.hasAnnotation(writeWithName) ->
                SupportedType.Custom(
                    type = type,
                    parcelerType = requireNotNull(nonNullType.getAnnotation(writeWithName)?.typeArguments?.first()),
                )

            nonNullType in typeParcelers ->
                SupportedType.Custom(
                    type = type,
                    parcelerType = typeParcelers.getValue(nonNullType),
                )

            (type == symbols.stringType) || (type == symbols.stringNType) -> SupportedType.String
            type.erasedUpperBound.isEnumClass -> SupportedType.Enum(type = type)
            type.isParcelable() -> SupportedType.Parcelable

            type.erasedUpperBoundType == symbols.listType ->
                SupportedType.List(itemType = map(type.getTypeArgument(0)))

            type.erasedUpperBoundType == symbols.mutableListType ->
                SupportedType.MutableList(itemType = map(type.getTypeArgument(0)))

            type.erasedUpperBoundType == symbols.setType ->
                SupportedType.Set(itemType = map(type.getTypeArgument(0)))

            type.erasedUpperBoundType == symbols.mutableSetType ->
                SupportedType.MutableSet(itemType = map(type.getTypeArgument(0)))

            type.erasedUpperBoundType == symbols.mapType ->
                SupportedType.Map(
                    keyType = map(type.getTypeArgument(0)),
                    valueType = map(type.getTypeArgument(1)),
                )

            type.erasedUpperBoundType == symbols.mutableMapType ->
                SupportedType.MutableMap(
                    keyType = map(type.getTypeArgument(0)),
                    valueType = map(type.getTypeArgument(1)),
                )

            else -> error("Unsupported type: ${type.render()}")
        }

    private fun IrType.getTypeArgument(index: Int): IrType =
        asIrSimpleType().arguments[index].typeOrNull!!

    private val IrType.erasedUpperBoundType: IrType
        get() = erasedUpperBound.defaultType
}
