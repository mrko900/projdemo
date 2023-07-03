package com.github.mrko900.braintrainer

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.navigation.NavOptions
import com.github.mrko900.braintrainer.databinding.StatsGeneralBinding
import kotlin.math.abs

class StatsGeneralFragment : Fragment() {
    private lateinit var binding: StatsGeneralBinding
    private lateinit var mainActivity: MainActivity
    private var currentConfigSelection = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        if (activity == null) {
            Log.e(LOGGING_TAG, "activity must be non-null")
            throw IllegalStateException("activity must be non-null")
        }
        mainActivity = activity as MainActivity
        binding = StatsGeneralBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mainActivity.setSupportActionBar(binding.toolbar)
        mainActivity.supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        mainActivity.supportActionBar!!.setDisplayShowHomeEnabled(true)
        mainActivity.onBackPressedCallback = Runnable {
            mainActivity.navigation.navigate(
                R.id.fragment_stats,
                navOptions = NavOptions.Builder()
                    .setEnterAnim(R.anim.slide_in_right_fade_in)
                    .setExitAnim(R.anim.slide_out_right_fade_out)
                    .build(),
                args = null
            )
        }
        mainActivity.supportActionBar!!.title = mainActivity.getString(R.string.stats_general)

        val adapter = ArrayAdapter.createFromResource(
            mainActivity,
            R.array.stats_exercise_selection,
            android.R.layout.simple_spinner_dropdown_item
        )
        binding.config.setAdapter(adapter)
        binding.config.setOnItemClickListener { parent, view, position, id ->
            currentConfigSelection = position
            render()
        }
        binding.config.setText(adapter.getItem(currentConfigSelection), false)

        render()
    }

    private fun render() {
        render(mainActivity.statsManager.getGeneralStats(when (currentConfigSelection) {
            0 -> null
            1 -> ExerciseMode.SHAPE_FUSION
            2 -> ExerciseMode.TRAILS
            3 -> ExerciseMode.MATH_CHAINS
            else -> throw IllegalArgumentException()
        }))
    }

    private fun render(stats: GeneralStats) {
        binding.lblExercisesCompleted.text = stats.exercisesCompleted.toString()
        binding.lblTotalQuestions.text = stats.totalQuestions.toString()
        binding.lblCorrectAnswers.text = stats.correctAnswers.toString()
        binding.lblWrongAnswers.text = stats.wrongAnswers.toString()
        binding.lblTimeouts.text = stats.timeouts.toString()
        binding.lblAvgTimePerQuestion.text = "%.2f s".format(stats.avgTimePerQuestion)
        binding.lblAvgTimeForCorrectAnswer.text = "%.2f s".format(stats.avgTimeForCorrectAnswer)
        binding.lblAvgTimeForWrongAnswer.text = "%.2f s".format(stats.avgTimeForWrongAnswer)
        binding.lblTotalScore.text = stats.aggregateScore.toString()
        binding.lblAvgScore.text = stats.avgScore.toString()
        binding.lblAvgImprovementPerExercise.text = "${if (stats.avgImprovementPerExercise >= 0) "+" else "-"}%.2f%%"
            .format(abs(stats.avgImprovementPerExercise))
        binding.lblAvgImprovementPerHour.text = "${if (stats.avgImprovementPerHour >= 0) "+" else "-"}%.2f%%"
            .format(abs(stats.avgImprovementPerHour))
        binding.lblTotalTimeSpent.text = "%.2f hrs".format(stats.totalTimeSpent)
    }
}
