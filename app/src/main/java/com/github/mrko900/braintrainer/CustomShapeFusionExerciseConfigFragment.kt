package com.github.mrko900.braintrainer

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.github.mrko900.braintrainer.databinding.ShapeFusionExerciseCustomConfigBinding
import com.google.android.material.slider.Slider

class CustomShapeFusionExerciseConfigFragment : Fragment() {
    companion object {
        fun configureDurationSlider(textView: TextView, slider: Slider) {
            slider.value = 11f
            slider.valueFrom = 1f
            slider.valueTo = 35f
            slider.stepSize = 1f
            textView.setText(R.string.number_of_rounds)
        }
    }

    private lateinit var binding: ShapeFusionExerciseCustomConfigBinding
    private lateinit var mainActivity: MainActivity

    private var currentOperationsSelection = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        if (activity == null) {
            Log.e(LOGGING_TAG, "activity must be non-null")
            throw IllegalStateException("activity must be non-null")
        }
        mainActivity = activity as MainActivity
        binding = ShapeFusionExerciseCustomConfigBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = ArrayAdapter.createFromResource(
            mainActivity,
            R.array.shape_fusion_exercise_operations,
            android.R.layout.simple_spinner_dropdown_item
        )

        binding.operations.setAdapter(adapter)
        binding.operations.setText(adapter.getItem(currentOperationsSelection), false)
        binding.operations.setOnItemClickListener { parent, view, position, id ->
            currentOperationsSelection = position
        }

        binding.nTermsSlider.value = 3f
        binding.nTermsSlider.valueFrom = 2f
        binding.nTermsSlider.valueTo = 4f
        binding.nTermsSlider.stepSize = 1f

        binding.nChoicesSlider.value = 4f
        binding.nChoicesSlider.valueFrom = 2f
        binding.nChoicesSlider.valueTo = 4f
        binding.nChoicesSlider.stepSize = 1f

        binding.shapeSizeSlider.value = 4f
        binding.shapeSizeSlider.valueFrom = 1f
        binding.shapeSizeSlider.valueTo = 6f
        binding.shapeSizeSlider.stepSize = 1f

        binding.secondsPerQuestionSlider.value = 8f
        binding.secondsPerQuestionSlider.valueFrom = 1f
        binding.secondsPerQuestionSlider.valueTo = 20f
        binding.secondsPerQuestionSlider.stepSize = 1f

        configureDurationSlider(binding.include.textView14, binding.include.durationSlider)
    }

    fun getNTerms(): Int {
        return binding.nTermsSlider.value.toInt()
    }

    fun getNChoices(): Int {
        return binding.nChoicesSlider.value.toInt()
    }

    fun isDynamic(): Boolean {
        return binding.dynamicCheckBox.isChecked
    }

    fun hasAdditionOperation(): Boolean {
        return currentOperationsSelection != 2
    }

    fun hasSubtractionOperation(): Boolean {
        return currentOperationsSelection != 1
    }

    fun getShapeSide(): Int {
        return binding.shapeSizeSlider.value.toInt()
    }

    fun getNumberOfRounds(): Int {
        return binding.include.durationSlider.value.toInt()
    }

    fun getSecondsPerQuestion(): Int {
        return binding.secondsPerQuestionSlider.value.toInt()
    }
}
