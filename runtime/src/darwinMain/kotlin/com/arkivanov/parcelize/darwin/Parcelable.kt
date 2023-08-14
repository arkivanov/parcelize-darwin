package com.arkivanov.parcelize.darwin

import platform.Foundation.NSSecureCodingProtocol

/**
 * Interface for serializable classes. The serialization is performed via [NSSecureCodingProtocol].
 */
interface Parcelable {

    /**
     * Returns an instance of [NSSecureCodingProtocol] responsible for serialization and deserialization.
     */
    fun coding(): NSSecureCodingProtocol = NotImplementedCoding()
}
