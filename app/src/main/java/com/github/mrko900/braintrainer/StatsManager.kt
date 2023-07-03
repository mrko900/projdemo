package com.github.mrko900.braintrainer

import java.util.Calendar
import java.util.Date
import java.util.GregorianCalendar
import java.util.Random
import kotlin.math.sqrt

class StatsManager {
    // null = all exercises
    fun getGeneralStats(exercise: ExerciseMode?): GeneralStats {
        if (exercise == null) {
            return GeneralStats(
                exercisesCompleted = 312,
                totalQuestions = 1864,
                correctAnswers = 1051,
                wrongAnswers = 323,
                timeouts = 490,
                avgTimePerQuestion = 8.44f,
                avgTimeForCorrectAnswer = 7.99f,
                avgTimeForWrongAnswer = 6.13f,
                aggregateScore = 15615,
                avgScore = 50.0048f,
                avgImprovementPerExercise = 0.51f,
                avgImprovementPerHour = 1.1f,
                totalTimeSpent = 10.46f
            )
        } else {
            return GeneralStats(
                exercisesCompleted = 99,
                totalQuestions = 693,
                correctAnswers = 378,
                wrongAnswers = 114,
                timeouts = 201,
                avgTimePerQuestion = 5.19f,
                avgTimeForCorrectAnswer = 4.76f,
                avgTimeForWrongAnswer = 4.31f,
                aggregateScore = 6373,
                avgScore = 64.37f,
                avgImprovementPerExercise = 0.80f,
                avgImprovementPerHour = 1.67f,
                totalTimeSpent = 2.85f
            )
        }
    }

    fun getRatingHistory(exercise: ExerciseMode): List<Pair<GregorianCalendar, Float>> {
        val res = ArrayList<Pair<GregorianCalendar, Float>>()
        val random = Random()
        var prev = random.nextInt(704) + 999
        for (i in 1..103) {
            if (random.nextInt(10) == 0) {
                continue
            }
            val gc = GregorianCalendar(2022, 11, 27)
            val date = gc.time.time + (i - 1) * 86400000L
            val cal = Calendar.getInstance()
            cal.time = Date(date)
            val r = prev + random.nextInt(25) - 10
            res.add(Pair(GregorianCalendar(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)), r.toFloat()))
            prev = r
        }
        return res
    }

    fun getPerfHistory(exercise: ExerciseMode): List<Pair<GregorianCalendar, Float>> {
        val res = ArrayList<Pair<GregorianCalendar, Float>>()
        val random = Random()
        for (i in 1..103) {
            if (random.nextInt(10) == 0) {
                continue
            }
            val gc = GregorianCalendar(2022, 11, 27)
            val date = gc.time.time + (i - 1) * 86400000L
            val cal = Calendar.getInstance()
            cal.time = Date(date)
            val r = random.nextInt(30 + i / 3) + 10
            res.add(Pair(GregorianCalendar(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)), r.toFloat()))
        }
        return res
    }

    fun getPerfByTimeOfDay(mode: ExerciseMode): List<Pair<Int, Float>> {
        val random = Random()
        val res = ArrayList<Pair<Int, Float>>()
        for (i in 0..23) {
            val v: Int
            when (i) {
                7 -> v = random.nextInt(5) + 40
                8 -> v = random.nextInt(5) + 40
                9 -> v = random.nextInt(5) + 45
                10 -> v = random.nextInt(5) + 50
                12 -> v = random.nextInt(5) + 55
                13 -> v = random.nextInt(5) + 60
                14 -> v = random.nextInt(5) + 55
                15 -> v = random.nextInt(5) + 55
                16 -> v = random.nextInt(5) + 55
                17 -> v = random.nextInt(5) + 50
                18 -> v = random.nextInt(5) + 50
                19 -> v = random.nextInt(5) + 45
                20 -> v = random.nextInt(5) + 40
                22 -> v = random.nextInt(5) + 35
                23 -> v = random.nextInt(5) + 30
                else -> v = 0
            }
            res.add(Pair(i, v.toFloat()))
        }
        return res
    }

    fun getPerfByExPlayed(mode: ExerciseMode): List<Pair<Int, Float>> {
        val random = Random()
        val res = ArrayList<Pair<Int, Float>>()
        for (i in 0..12) {
            if (random.nextInt(i + 1) <= sqrt(i.toDouble())) {
                res.add(Pair(i, (30 + (12 - i) * 2 + random.nextInt(6).toFloat())))
            }
        }
        return res
    }

    fun getActivity(exercise: ExerciseMode?): List<Pair<GregorianCalendar, Int>> {
        val res = ArrayList<Pair<GregorianCalendar, Int>>()
        val random = Random()
        for (i in 1..103) {
            if (random.nextInt(10) == 0) {
                continue
            }
            val gc = GregorianCalendar(2022, 11, 27)
            val date = gc.time.time + (i - 1) * 86400000L
            val cal = Calendar.getInstance()
            cal.time = Date(date)
            val r = random.nextInt(if (exercise != null) 12 else 26)
            res.add(Pair(GregorianCalendar(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)), r))
        }
        return res
    }
}

data class GeneralStats(
    val exercisesCompleted: Int,
    val totalQuestions: Int,
    val correctAnswers: Int,
    val wrongAnswers: Int,
    val timeouts: Int,
    val avgTimePerQuestion: Float,
    val avgTimeForCorrectAnswer: Float,
    val avgTimeForWrongAnswer: Float,
    val aggregateScore: Int,
    val avgScore: Float,
    val avgImprovementPerExercise: Float,
    val avgImprovementPerHour: Float,
    val totalTimeSpent: Float
)
