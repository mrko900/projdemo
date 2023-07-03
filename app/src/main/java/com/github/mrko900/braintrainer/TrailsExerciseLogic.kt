package com.github.mrko900.braintrainer

import android.content.res.Resources

class TrailsExerciseLogic(
    initialSecondsPerQuestion: Int,
    fieldSize: Int,
    instructionLength: Int,
    val totalRounds: Int,
    val dynamic: Boolean,
    val exerciseControl: ExerciseControl,
    val res: Resources
) {
    var secondsPerQuestion = initialSecondsPerQuestion
        private set

    var fieldSize = fieldSize
        private set

    var score = 0
        private set

    var instructionLength = instructionLength
        private set

    fun correctChoice(secondsElapsed: Float) {
        exerciseControl.setStatus(
            res.getString(R.string.status_correct_guess),
            res.getInteger(R.integer.status_fade_in).toLong(),
            res.getInteger(R.integer.status_fade_out).toLong(),
            res.getInteger(R.integer.status_duration_default).toLong()
        )
        score += secondsPerQuestion - secondsElapsed.toInt()
        updateScore()
    }

    fun incorrectChoice(secondsElapsed: Float) {
        exerciseControl.setStatus(
            res.getString(R.string.status_wrong_guess),
            res.getInteger(R.integer.status_fade_in).toLong(),
            res.getInteger(R.integer.status_fade_out).toLong(),
            res.getInteger(R.integer.status_duration_default).toLong()
        )
        score -= secondsPerQuestion / 2
        score = score.coerceAtLeast(0)
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
}
