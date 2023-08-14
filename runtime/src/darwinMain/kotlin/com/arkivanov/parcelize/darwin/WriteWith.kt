package com.arkivanov.parcelize.darwin

/**
 * Specifies what [Parceler] should be used for the annotated type.
 */
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.TYPE)
annotation class WriteWith<P : Parceler<*>>
