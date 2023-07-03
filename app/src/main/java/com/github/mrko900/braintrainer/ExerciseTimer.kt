package com.github.mrko900.braintrainer

import android.animation.ValueAnimator
import android.os.Handler
import android.os.Looper
import android.view.animation.LinearInterpolator

class ExerciseTimer(
    private val timedOutCallback: (timestamp: Long) -> Unit,
    private val isActive: () -> Boolean,
    private val exerciseControl: ExerciseControl,
    secondsPerQuestion: Int
) {
    private val handler = Handler(Looper.getMainLooper())
    var secondsPerQuestion = secondsPerQuestion

    private lateinit var progressAnim: ValueAnimator

    fun start(): Long {
        val time0 = System.currentTimeMillis()
        var targetNextTimerUpd = time0 + 1000L
        val runnable = object : Runnable {
            override fun run() {
                val time = System.currentTimeMillis()
                if (!isActive())
                    return
                exerciseControl.timer -= 1
                if (exerciseControl.timer == 0) {
                    timedOutCallback(time)
                    return
                }
                targetNextTimerUpd += 1000L
                handler.postDelayed(this, targetNextTimerUpd - time)
            }
        }
        handler.postDelayed(runnable, 1000L)

        progressAnim = ValueAnimator.ofFloat(1f, 0f)
        progressAnim.addUpdateListener { anim -> exerciseControl.progress = anim.animatedValue as Float }
        progressAnim.duration = secondsPerQuestion * 1000L
        progressAnim.interpolator = LinearInterpolator()
        progressAnim.start()
        return time0
    }

    fun getProgress(): Float {
        return progressAnim.animatedValue as Float
    }

    fun end() {
        progressAnim.end()
        handler.removeCallbacksAndMessages(null)
    }
}
