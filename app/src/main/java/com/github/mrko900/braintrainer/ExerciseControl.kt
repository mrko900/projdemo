package com.github.mrko900.braintrainer

import android.os.Handler
import android.os.Looper
import android.view.animation.Animation
import android.view.animation.ScaleAnimation
import android.widget.TextView
import androidx.navigation.NavOptions

class ExerciseControl(
    private val activity: MainActivity,
    private val timerView: TextView,
    private val progressBar: CircularProgressBar,
    private val scoreView: TextView,
    private val roundView: TextView,
    private val statusView: TextView
) {
    var timer: Int = 0
        set(value) {
            field = value
            timerView.text = value.toString()
        }

    var progress: Float = 0f
        set(value) {
            field = value
            progressBar.percentage = value
            progressBar.invalidate()
        }

    var score: Int = 0
        set(value) {
            field = value
            scoreView.text = value.toString()
        }

    var totalRounds: Int = 0
        set(value) {
            field = value
            roundView.text = "$round/$value"
        }

    var round: Int = 0
        set(value) {
            field = value
            roundView.text = "$value/$totalRounds"
        }

    // todo handle conflicting statuses
    fun setStatus(message: String, fadeIn: Long, fadeOut: Long, duration: Long) {
        statusView.text = message
        val scaleIn = ScaleAnimation(0f, 1f, 0f, 1f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f)
        scaleIn.duration = fadeIn
        val scaleOut =
            ScaleAnimation(1f, 0f, 1f, 0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f)
        scaleOut.duration = fadeOut
        scaleOut.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationEnd(animation: Animation?) {
                statusView.text = null
            }

            override fun onAnimationStart(animation: Animation?) {
            }

            override fun onAnimationRepeat(animation: Animation?) {
            }
        })
        statusView.startAnimation(scaleIn)
        val handler = Handler(Looper.getMainLooper())
        handler.postDelayed({
            statusView.startAnimation(scaleOut)
        }, fadeIn + duration)
    }

    fun endExercise(result: ExerciseResult, resultManager: ExerciseResultManager) {
        activity.currentExerciseResultManager = resultManager
        activity.currentExerciseResult = result
        activity.navigation.navigate(
            R.id.fragment_exercise_completed,
            navOptions = NavOptions.Builder()
                .setEnterAnim(R.anim.slide_in_left_fade_in)
                .setExitAnim(R.anim.slide_out_left_fade_out)
                .build(),
            args = null
        )
    }
}
