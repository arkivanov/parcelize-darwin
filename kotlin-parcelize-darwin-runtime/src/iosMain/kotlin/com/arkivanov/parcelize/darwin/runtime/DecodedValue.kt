package com.arkivanov.parcelize.darwin.runtime

import platform.Foundation.NSCoder
import platform.Foundation.NSCodingProtocol
import platform.darwin.NSObject

class DecodedValue(
    val value: Parcelable?
) : NSObject(), NSCodingProtocol {

    override fun encodeWithCoder(coder: NSCoder) {
        throw NotImplementedError()
    }

    override fun initWithCoder(coder: NSCoder): NSCodingProtocol? {
        throw NotImplementedError()
    }
}