package com.arkivanov.parcelize.darwin

import platform.Foundation.NSCoder
import platform.Foundation.NSSecureCodingProtocol
import platform.Foundation.NSSecureCodingProtocolMeta
import platform.darwin.NSObject

internal class NotImplementedCoding : NSObject(), NSSecureCodingProtocol {

    override fun encodeWithCoder(coder: NSCoder) {
        throw NotImplementedError("Coding is not implemented. Make sure that you have applied the plugin.")
    }

    override fun initWithCoder(coder: NSCoder): NSSecureCodingProtocol? {
        throw NotImplementedError("Coding is not implemented. Make sure that you have applied the plugin.")
    }

    companion object : NSObject(), NSSecureCodingProtocolMeta {
        override fun supportsSecureCoding(): Boolean = true
    }
}