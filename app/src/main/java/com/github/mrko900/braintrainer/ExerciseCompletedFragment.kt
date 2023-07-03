package com.github.mrko900.braintrainer

import android.animation.ValueAnimator
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.github.mrko900.braintrainer.databinding.ExerciseCompletedBinding

class ExerciseCompletedFragment : Fragment() {
    private lateinit var binding: ExerciseCompletedBinding
    private lateinit var mainActivity: MainActivity

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        if (activity == null) {
            Log.e(LOGGING_TAG, "activity must be non-null")
            throw IllegalStateException("activity must be non-null")
        }
        mainActivity = activity as MainActivity
        binding = ExerciseCompletedBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val anim = ValueAnimator.ofFloat(1f, 0f)
        val tmp = binding.circularProgressBar.filledColor
        binding.circularProgressBar.filledColor = binding.circularProgressBar.blankColor
        binding.circularProgressBar.blankColor = tmp
        anim.addUpdateListener { v ->
            binding.circularProgressBar.percentage = v.animatedValue as Float
            binding.circularProgressBar.invalidate()
        }
        anim.duration = mainActivity.resources.getInteger(R.integer.exercise_completed_progress_anim_duration).toLong()
        anim.start()

        binding.score.text = mainActivity.currentExerciseResult!!.score.toString()

        binding.mode.text = getExerciseName(mainActivity.currentExerciseResult!!.mode, mainActivity.resources)
            .lowercase()
        mainActivity.currentExerciseResultManager!!.out(mainActivity, binding, mainActivity.currentExerciseResult!!)
    }
}
