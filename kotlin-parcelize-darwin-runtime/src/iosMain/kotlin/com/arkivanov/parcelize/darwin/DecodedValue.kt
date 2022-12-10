package com.arkivanov.parcelize.darwin

import platform.Foundation.NSCoder
import platform.Foundation.NSSecureCodingProtocol
import platform.darwin.NSObject

class DecodedValue(
    val value: Parcelable?
) : NSObject(), NSSecureCodingProtocol {

    override fun encodeWithCoder(coder: NSCoder) {
        throw NotImplementedError()
    }

    override fun initWithCoder(coder: NSCoder): NSSecureCodingProtocol? {
        throw NotImplementedError()
    }
}
