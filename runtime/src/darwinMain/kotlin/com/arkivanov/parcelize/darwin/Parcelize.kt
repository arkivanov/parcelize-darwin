package com.arkivanov.parcelize.darwin

/**
 * Instructs the `parcelize-darwin` compiler plugin to generate [Parcelable] implementation for the annotated class.
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.BINARY)
annotation class Parcelize
