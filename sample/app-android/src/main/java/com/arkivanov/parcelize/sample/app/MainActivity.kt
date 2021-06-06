package com.arkivanov.parcelize.sample.app

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.arkivanov.parcelize.sample.SomeLogic

class MainActivity : AppCompatActivity() {

    private lateinit var someLogic: SomeLogic
    private lateinit var valueText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.main_activity)

        someLogic = SomeLogic(savedInstanceState?.getParcelable(KEY_MAIN_SCREEN_STATE))

        valueText = requireViewById(R.id.text_value)

        updateValue()

        requireViewById<View>(R.id.button_generate).setOnClickListener {
            someLogic.generate()
            updateValue()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putParcelable(KEY_MAIN_SCREEN_STATE, someLogic.saveState())
    }

    private fun updateValue() {
        valueText.text = getString(R.string.value_fmt, someLogic.value)
    }

    private companion object {
        private const val KEY_MAIN_SCREEN_STATE = "MAIN_SCREEN_STATE"
    }
}