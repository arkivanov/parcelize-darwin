package com.arkivanov.parcelize.darwin.runtime

import platform.Foundation.NSCodingProtocol

interface Parcelable {

    fun coding(): NSCodingProtocol
}
