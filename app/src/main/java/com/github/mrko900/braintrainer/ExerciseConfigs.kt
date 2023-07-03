package com.github.mrko900.braintrainer

data class ShapeFusionExerciseConfig(
    val nTermsInitial: Int,
    val nChoicesInitial: Int,
    val dynamic: Boolean,
    val additionOperation: Boolean,
    val subtractionOperation: Boolean,
    val shapeSide: Int,
    val secondsPerQuestion: Int,
    val nRounds: Int
)

data class TrailsExerciseConfig(
    val dynamic: Boolean,
    val fieldSize: Int,
    val secondsPerQuestion: Int,
    val nRounds: Int,
    val instructionLength: Int
)

data class MathChainsExerciseConfig(
    val dynamic: Boolean,
    val nChains: Int,
    val secondsPerQuestion: Int,
    val nRounds: Int,
    val extentOfNumbers: Int
)
