package com.example.myandroidapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.myandroidapp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var counter = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        counter = savedInstanceState?.getInt(KEY_COUNTER) ?: 0
        renderCounter()

        binding.buttonIncrement.setOnClickListener {
            counter++
            renderCounter()
        }

        binding.buttonReset.setOnClickListener {
            counter = 0
            renderCounter()
        }
    }

    private fun renderCounter() {
        binding.textCounter.text = getString(R.string.counter_value, counter)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(KEY_COUNTER, counter)
    }

    private companion object {
        const val KEY_COUNTER = "counter"
    }
}
