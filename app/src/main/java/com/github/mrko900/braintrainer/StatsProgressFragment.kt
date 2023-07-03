package com.github.mrko900.braintrainer

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.navigation.NavOptions
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mrko900.braintrainer.databinding.StatsProgressBinding
import java.util.Calendar
import java.util.Date

class StatsProgressFragment : Fragment() {
    private lateinit var binding: StatsProgressBinding
    private lateinit var mainActivity: MainActivity
    private var currentConfigSelection = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        if (activity == null) {
            Log.e(LOGGING_TAG, "activity must be non-null")
            throw IllegalStateException("activity must be non-null")
        }
        mainActivity = activity as MainActivity
        binding = StatsProgressBinding.inflate(inflater, container, false)
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
        mainActivity.supportActionBar!!.title = mainActivity.getString(R.string.stats_progress)

        val adapter = ArrayAdapter.createFromResource(
            mainActivity,
            R.array.stats_exercise_selection_ind,
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
        render(when (currentConfigSelection) {
            0 -> ExerciseMode.SHAPE_FUSION
            1 -> ExerciseMode.TRAILS
            2 -> ExerciseMode.MATH_CHAINS
            else -> throw IllegalArgumentException()
        })
    }

    private fun render(mode: ExerciseMode) {
        renderRatingChart(mode)
        renderPerfChart(mode)
    }

    private fun renderRatingChart(exerciseMode: ExerciseMode) {
        val data = ArrayList<Entry>()
        for (e in mainActivity.statsManager.getRatingHistory(exerciseMode)) {
            data.add(Entry((e.first.time.time / 86400000L).toFloat(), e.second))
        }
        val chart = binding.chart
        val dataSet = LineDataSet(data, "Test")
        dataSet.circleRadius = 4f
        dataSet.circleHoleRadius = 2.5f
        dataSet.lineWidth = 2f
        dataSet.setDrawValues(false)
        chart.legend.isEnabled = false
        chart.description.isEnabled = false
        chart.data = LineData(dataSet)
        chart.xAxis.setDrawGridLines(false)
        val xAxis = binding.chart.xAxis
        xAxis.granularity = 1f
        xAxis.valueFormatter = object : ValueFormatter() {
            override fun getAxisLabel(value: Float, axis: AxisBase?): String {
                val date = Calendar.getInstance()
                date.time = Date(86400000L * value.toLong())
                val month = date.get(Calendar.MONTH)
                val months = arrayOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
                val monthName = months[month]
                val dayOfMonth = date.get(Calendar.DAY_OF_MONTH).toString()
                return "$monthName $dayOfMonth"
            }
        }
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        chart.invalidate()
    }

    private fun renderPerfChart(exerciseMode: ExerciseMode) {
        val data = ArrayList<Entry>()
        for (e in mainActivity.statsManager.getPerfHistory(exerciseMode)) {
            data.add(Entry((e.first.time.time / 86400000L).toFloat(), e.second))
        }
        val chart = binding.chart2
        val dataSet = LineDataSet(data, "Test")
        dataSet.circleRadius = 4f
        dataSet.circleHoleRadius = 2.5f
        dataSet.lineWidth = 2f
        dataSet.setDrawValues(false)
        chart.legend.isEnabled = false
        chart.description.isEnabled = false
        chart.data = LineData(dataSet)
        chart.xAxis.setDrawGridLines(false)
        val xAxis = binding.chart2.xAxis
        xAxis.granularity = 1f
        xAxis.valueFormatter = object : ValueFormatter() {
            override fun getAxisLabel(value: Float, axis: AxisBase?): String {
                val date = Calendar.getInstance()
                date.time = Date(86400000L * value.toLong())
                val month = date.get(Calendar.MONTH)
                val months = arrayOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
                val monthName = months[month]
                val dayOfMonth = date.get(Calendar.DAY_OF_MONTH).toString()
                return "$monthName $dayOfMonth"
            }
        }
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        chart.invalidate()
    }
}
