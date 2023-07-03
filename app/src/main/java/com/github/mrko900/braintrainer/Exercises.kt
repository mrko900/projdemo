package com.github.mrko900.braintrainer

import android.content.res.Resources
import android.graphics.Color
import java.util.Random

enum class ExerciseMode {
    SHAPE_FUSION, TRAILS, MATH_CHAINS
}

data class ExerciseParams(var mode: ExerciseMode, var config: Any?)

fun exerciseListItems(): MutableList<ExerciseListViewItemParams> {
    val list = ArrayList<ExerciseListViewItemParams>()
    list.add(exerciseListItemShapeFusion())
    list.add(exerciseListItemTrails())
    list.add(exerciseListItemMathChains())
    return list
}

fun exerciseListItemShapeFusion(): ExerciseListViewItemParams {
    return ExerciseListViewItemParams(
        R.string.exercise_title_shape_fusion,
        Color.parseColor("#FF8A65"),
        Color.parseColor("#FBE9E7"),
        ExerciseMode.SHAPE_FUSION,
        Random().nextInt(1000) + 900,
        Random().nextInt(101) - 50,
    )
}

fun exerciseListItemTrails(): ExerciseListViewItemParams {
    return ExerciseListViewItemParams(
        R.string.exercise_title_trails,
        Color.parseColor("#81C784"),
        Color.parseColor("#E8F5E9"),
        ExerciseMode.TRAILS,
        Random().nextInt(1000) + 900,
        Random().nextInt(101) - 50,
    )
}

fun exerciseListItemMathChains(): ExerciseListViewItemParams {
    return ExerciseListViewItemParams(
        R.string.exercise_title_math_chains,
        Color.parseColor("#BA68C8"),
        Color.parseColor("#F3E5F5"),
        ExerciseMode.MATH_CHAINS,
        Random().nextInt(1000) + 900,
        Random().nextInt(101) - 50,
    )
}

fun getExerciseName(mode: ExerciseMode, res: Resources): String {
    return res.getString(when (mode) {
        ExerciseMode.SHAPE_FUSION -> R.string.exercise_title_shape_fusion
        ExerciseMode.TRAILS -> R.string.exercise_title_trails
        ExerciseMode.MATH_CHAINS -> R.string.exercise_title_math_chains
        else -> throw UnsupportedOperationException()
    })
}
