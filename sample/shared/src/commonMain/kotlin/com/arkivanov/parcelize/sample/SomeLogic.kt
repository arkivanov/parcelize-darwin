package com.arkivanov.parcelize.sample

import kotlin.random.Random

class SomeLogic(savedState: SavedState?) {

    var value: Int = savedState?.value ?: Random.nextInt()

    fun saveState(): SavedState =
        SavedState(value = value)

    fun generate() {
        value = Random.nextInt()
    }

    @Parcelize
    class SavedState(
        val value: Int
    ) : Parcelable
}