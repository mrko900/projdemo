package com.github.mrko900.braintrainer

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.github.mrko900.braintrainer.databinding.ExerciseConfigDefaultBinding

class DefaultExerciseConfigFragment : Fragment() {
    private lateinit var binding: ExerciseConfigDefaultBinding
    private lateinit var mainActivity: MainActivity

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        if (activity == null) {
            Log.e(LOGGING_TAG, "activity must be non-null")
            throw IllegalStateException("activity must be non-null")
        }
        mainActivity = activity as MainActivity
        binding = ExerciseConfigDefaultBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        when (mainActivity.currentExercise!!.mode) {
            ExerciseMode.SHAPE_FUSION -> {
                CustomShapeFusionExerciseConfigFragment.configureDurationSlider(
                    binding.include.textView14,
                    binding.include.durationSlider
                )
            }
            else -> {}
        }
    }

    fun getDuration(): Int {
        return binding.include.durationSlider.value.toInt()
    }
}
