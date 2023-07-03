package com.github.mrko900.braintrainer

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.util.Consumer

interface Exercise {
    fun init()
    fun start()
    fun pause()
    fun resume()
}

abstract class AbstractExercise(
    protected val exerciseControl: ExerciseControl,
    private val onFinishedCallback: Consumer<ExerciseResult>,
    protected val group: ViewGroup,
    protected val inflater: LayoutInflater,
    protected val activity: MainActivity
) : Exercise {
    override fun start() {
        activity.setNavPaneVisible(false)
    }

    protected open fun finish(result: ExerciseResult) {
        onFinishedCallback.accept(result)
        activity.setNavPaneVisible(true)
    }
}
