package com.example.sleeptracker

import android.content.Context
import android.os.SystemClock
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView

class Chronometer @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : AppCompatTextView(context, attrs) {

    var base: Long = SystemClock.elapsedRealtime()
        set(value) {
            field = value
            updateText()
        }

    private var running = false

    private val ticker = object : Runnable {
        override fun run() {
            updateText()
            postDelayed(this, 500)
        }
    }

    fun start() {
        if (!running) {
            running = true
            post(ticker)
        }
    }

    fun stop() {
        if (running) {
            running = false
            removeCallbacks(ticker)
        }
    }

    private fun updateText() {
        val elapsed  = SystemClock.elapsedRealtime() - base
        val totalSec = (elapsed / 1000).toInt()
        val h = totalSec / 3600
        val m = (totalSec % 3600) / 60
        val s = totalSec % 60
        text = "%02d:%02d:%02d".format(h, m, s)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        stop()
    }
}
