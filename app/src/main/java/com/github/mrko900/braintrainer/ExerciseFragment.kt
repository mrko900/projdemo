package com.github.mrko900.braintrainer

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.github.mrko900.braintrainer.databinding.ExerciseBinding

class ExerciseFragment : Fragment() {
    private lateinit var binding: ExerciseBinding
    private lateinit var mainActivity: MainActivity
    private lateinit var exercise: Exercise

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        if (activity == null) {
            Log.e(LOGGING_TAG, "activity must be non-null")
            throw IllegalStateException("activity must be non-null")
        }

        Log.d(LOGGING_TAG, "Init exercise view: ") // todo exercise name

        mainActivity = activity as MainActivity
        binding = ExerciseBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.timerProgressBar.strokeWidth = 12f
        binding.timerProgressBar.filledColor = Color.parseColor("#222222")

        exercise = mainActivity.createExercise(binding.root) { }
        exercise.init()
        exercise.start()
    }
}
