package com.github.mrko900.braintrainer

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.github.mrko900.braintrainer.databinding.TrailsExerciseCustomConfigBinding
import com.google.android.material.slider.Slider

class CustomTrailsExerciseConfigFragment : Fragment() {
    companion object {
        fun configureDurationSlider(textView: TextView, slider: Slider) {
            slider.value = 11f
            slider.valueFrom = 1f
            slider.valueTo = 35f
            slider.stepSize = 1f
            textView.setText(R.string.number_of_rounds)
        }
    }

    private lateinit var binding: TrailsExerciseCustomConfigBinding
    private lateinit var mainActivity: MainActivity

    private var currentOperationsSelection = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        if (activity == null) {
            Log.e(LOGGING_TAG, "activity must be non-null")
            throw IllegalStateException("activity must be non-null")
        }
        mainActivity = activity as MainActivity
        binding = TrailsExerciseCustomConfigBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = ArrayAdapter.createFromResource(
            mainActivity,
            R.array.shape_fusion_exercise_operations,
            android.R.layout.simple_spinner_dropdown_item
        )

        binding.fieldSizeSlider.value = 3f
        binding.fieldSizeSlider.valueFrom = 3f
        binding.fieldSizeSlider.valueTo = 12f
        binding.fieldSizeSlider.stepSize = 1f

        binding.instructionLengthSlider.value = 6f
        binding.instructionLengthSlider.valueFrom = 2f
        binding.instructionLengthSlider.valueTo = 10f
        binding.instructionLengthSlider.stepSize = 1f

        binding.secondsPerQuestionSlider.value = 8f
        binding.secondsPerQuestionSlider.valueFrom = 1f
        binding.secondsPerQuestionSlider.valueTo = 20f
        binding.secondsPerQuestionSlider.stepSize = 1f

        configureDurationSlider(binding.include.textView14, binding.include.durationSlider)
    }

    fun getFieldSize(): Int {
        return binding.fieldSizeSlider.value.toInt()
    }

    fun getInstructionLength(): Int {
        return binding.instructionLengthSlider.value.toInt()
    }

    fun isDynamic(): Boolean {
        return binding.dynamicCheckBox.isChecked
    }

    fun getNumberOfRounds(): Int {
        return binding.include.durationSlider.value.toInt()
    }

    fun getSecondsPerQuestion(): Int {
        return binding.secondsPerQuestionSlider.value.toInt()
    }
}
