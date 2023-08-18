package com.arkivanov.parcelize.darwin

import platform.Foundation.NSCoder

/**
 * Interface for custom [Parcelize] serializers.
 */
interface Parceler<T> {

    /**
     * Creates a new instance of [T] and reads all its data from the provided [coder].
     */
    fun create(coder: NSCoder): T =
        throw NotImplementedError("Parceler#create is not implemented: $this")

    /**
     * Writes the [T] instance state to the provided [coder].
     */
    fun T.write(coder: NSCoder) {
        throw NotImplementedError("Parceler#write is not implemented: $this")
    }
}
