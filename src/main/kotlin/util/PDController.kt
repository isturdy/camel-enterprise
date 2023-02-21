package com.github.isturdy.camelenterprise.util

class PDController(
    private val p: Float,
    private val d: Float,
    private val setpoint: Float = 0.0f
) {
    private var last = 0.0f
    fun advance(amount: Float, value: Float): Float {
        val rate = (value - last) / amount
        last = value
        return p * (value - setpoint) + d * rate
    }
}