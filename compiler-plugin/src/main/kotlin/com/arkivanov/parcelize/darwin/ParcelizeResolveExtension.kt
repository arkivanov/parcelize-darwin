package com.arkivanov.parcelize.darwin

import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.descriptors.CallableMemberDescriptor
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.DescriptorVisibilities
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.descriptors.SimpleFunctionDescriptor
import org.jetbrains.kotlin.descriptors.annotations.Annotations
import org.jetbrains.kotlin.descriptors.findClassAcrossModuleDependencies
import org.jetbrains.kotlin.descriptors.impl.SimpleFunctionDescriptorImpl
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.resolve.descriptorUtil.module
import org.jetbrains.kotlin.resolve.extensions.SyntheticResolveExtension

class ParcelizeResolveExtension(
    private val logs: MessageCollector
) : SyntheticResolveExtension {

    override fun getSyntheticFunctionNames(thisDescriptor: ClassDescriptor): List<Name> =
        if (thisDescriptor.isParcelize()) {
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
        if (!thisDescriptor.isParcelize() || (name != codingName)) {
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
                emptyList(),
                thisDescriptor.module.findClassAcrossModuleDependencies(nsSecureCodingClassId).require().defaultType,
                Modality.FINAL,
                DescriptorVisibilities.PUBLIC
            )
}
