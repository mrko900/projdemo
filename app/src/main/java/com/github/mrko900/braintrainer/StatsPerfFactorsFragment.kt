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
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mrko900.braintrainer.databinding.StatsPerfFactorsBinding

class StatsPerfFactorsFragment : Fragment() {
    private lateinit var binding: StatsPerfFactorsBinding
    private lateinit var mainActivity: MainActivity
    private var currentConfigSelection = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        if (activity == null) {
            Log.e(LOGGING_TAG, "activity must be non-null")
            throw IllegalStateException("activity must be non-null")
        }
        mainActivity = activity as MainActivity
        binding = StatsPerfFactorsBinding.inflate(inflater, container, false)
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
        mainActivity.supportActionBar!!.title = mainActivity.getString(R.string.stats_perf_factors)

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
        renderPerfByTimeOfDayChart(mode)
        renderPerfByExPlayed(mode)
    }

    private fun renderPerfByTimeOfDayChart(exerciseMode: ExerciseMode) {
        val data = ArrayList<BarEntry>()
        for (e in mainActivity.statsManager.getPerfByTimeOfDay(exerciseMode)) {
            data.add(BarEntry(e.first.toFloat(), e.second))
        }
        val chart = binding.chart2
        val dataSet = BarDataSet(data, "Test")

        dataSet.setDrawValues(false)
        chart.legend.isEnabled = false
        chart.description.isEnabled = false
        chart.data = BarData(dataSet)
        chart.xAxis.setDrawGridLines(false)
        val xAxis = binding.chart2.xAxis
        xAxis.granularity = 1f
        xAxis.valueFormatter = object : ValueFormatter() {
            override fun getAxisLabel(value: Float, axis: AxisBase?): String {
                return "${value.toInt() % 12 + 1} ${if (value >= 12) "PM" else "AM"}"
            }
        }
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        chart.isScaleYEnabled = false
        chart.invalidate()
    }

    private fun renderPerfByExPlayed(exerciseMode: ExerciseMode) {
        val data = ArrayList<Entry>()
        for (e in mainActivity.statsManager.getPerfByExPlayed(exerciseMode)) {
            data.add(Entry(e.first.toFloat(), e.second))
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
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        chart.invalidate()
    }
}
