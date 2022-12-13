package com.arkivanov.parcelize.darwin

import platform.Foundation.NSSecureCodingProtocol

interface Parcelable {

    fun coding(): NSSecureCodingProtocol? = NotImplementedCoding()
}
