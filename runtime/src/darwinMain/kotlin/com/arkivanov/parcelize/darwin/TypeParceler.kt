package com.arkivanov.parcelize.darwin

/**
 * Specifies what [Parceler] should be used for a particular type [T].
 */
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.CLASS)
annotation class TypeParceler<T, P : Parceler<in T>>
