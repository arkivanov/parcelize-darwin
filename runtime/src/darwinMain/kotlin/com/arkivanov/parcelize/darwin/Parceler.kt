package com.arkivanov.parcelize.darwin

import platform.Foundation.NSCoder

/**
 * Interface for custom [Parcelize] serializers.
 */
interface Parceler<T> {

    /**
     * Creates a new instance of [T] and reads all its data from the provided [coder].
     */
    fun create(coder: NSCoder): T

    /**
     * Writes the [T] instance state to the provided [coder].
     */
    fun T.write(coder: NSCoder)
}
