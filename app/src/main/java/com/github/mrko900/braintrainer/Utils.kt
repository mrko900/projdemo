package com.github.mrko900.braintrainer

import android.util.Log

private class A

@JvmField
val LOGGING_TAG: String = A::class.java.`package`!!.name

fun logd(msg: String) {
    Log.d(LOGGING_TAG, msg)
}
