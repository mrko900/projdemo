package com.github.mrko900.braintrainer

import android.content.res.Resources
import android.util.Log
import java.util.Random

class MathChainsExerciseLogic(
    initialSecondsPerQuestion: Int,
    val totalRounds: Int,
    initialNChains: Int,
    val exerciseControl: ExerciseControl,
    val res: Resources
) {
    var secondsPerQuestion = initialSecondsPerQuestion
        private set

    var nChains = initialNChains
        private set

    private val chainVals0 = ArrayList<Int>()

    val chainVals: List<Int>
        get() = chainVals0

    var score = 0
        private set

    private val random = Random()

    init {
        for (i in 1..initialNChains) {
            chainVals0.add(-1)
        }
    }

    fun success(secondsElapsed: Float) {
        Log.d(LOGGING_TAG, "ELAPSED $secondsElapsed")
        exerciseControl.setStatus(
            res.getString(R.string.status_correct_guess),
            res.getInteger(R.integer.status_fade_in).toLong(),
            res.getInteger(R.integer.status_fade_out).toLong(),
            res.getInteger(R.integer.status_duration_default).toLong()
        )
        score += secondsPerQuestion - secondsElapsed.toInt()
        updateScore()
    }

    fun timedOut(secondsElapsed: Float) {
        exerciseControl.setStatus(
            res.getString(R.string.status_timed_out),
            res.getInteger(R.integer.status_fade_in).toLong(),
            res.getInteger(R.integer.status_fade_out).toLong(),
            res.getInteger(R.integer.status_duration_default).toLong()
        )
        score -= secondsPerQuestion / 4
        score = score.coerceAtLeast(0)
        updateScore()
    }

    private fun updateScore() {
        exerciseControl.score = score
    }

    fun setChainVal(chain: Int, newVal: Int) {
        chainVals0[chain] = newVal
    }
}
