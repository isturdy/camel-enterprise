package com.gthishub.isturdy.camelenterprise.util

fun Float.clampAngle(): Float {
    return if (this > 180) this - 360 else if (this < -180) this + 360 else this
}