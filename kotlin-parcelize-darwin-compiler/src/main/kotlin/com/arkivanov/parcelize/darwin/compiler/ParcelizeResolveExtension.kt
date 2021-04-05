package com.arkivanov.parcelize.darwin.compiler

import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.descriptors.*
import org.jetbrains.kotlin.descriptors.annotations.Annotations
import org.jetbrains.kotlin.descriptors.deserialization.ClassDescriptorFactory
import org.jetbrains.kotlin.descriptors.impl.SimpleFunctionDescriptorImpl
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.resolve.descriptorUtil.builtIns
import org.jetbrains.kotlin.resolve.descriptorUtil.module
import org.jetbrains.kotlin.resolve.extensions.SyntheticResolveExtension
import org.jetbrains.kotlin.types.KotlinTypeFactory
import org.jetbrains.kotlin.types.TypeProjectionImpl

class ParcelizeResolveExtension(
    private val logs: MessageCollector
) : SyntheticResolveExtension {

    override fun getSyntheticFunctionNames(thisDescriptor: ClassDescriptor): List<Name> =
        if (thisDescriptor.isValidForParcelize()) {
            listOf(codingName)
        } else {
            emptyList()
        }

    override fun generateSyntheticMethods(
        thisDescriptor: ClassDescriptor,
        name: Name,
        bindingContext: BindingContext,
        fromSupertypes: List<SimpleFunctionDescriptor>,
        result: MutableCollection<SimpleFunctionDescriptor>
    ) {
        if (!thisDescriptor.isValidForParcelize() || (name != codingName)) {
            return
        }

        result += createCodingFunctionDescriptor(thisDescriptor)
    }

    private fun createCodingFunctionDescriptor(thisDescriptor: ClassDescriptor): SimpleFunctionDescriptorImpl =
        SimpleFunctionDescriptorImpl
            .create(
                thisDescriptor,
                Annotations.EMPTY,
                codingName,
                CallableMemberDescriptor.Kind.SYNTHESIZED,
                thisDescriptor.source
            )
            .initialize(
                null,
                thisDescriptor.thisAsReceiverParameter,
                emptyList(),
                emptyList(),
                thisDescriptor.module.findClassAcrossModuleDependencies(ClassId.topLevel(nsCodingName))!!.defaultType,
                Modality.FINAL,
                DescriptorVisibilities.PUBLIC
            )
}
