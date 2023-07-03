package com.github.mrko900.braintrainer

import android.animation.ValueAnimator
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Color.parseColor
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.annotation.ColorInt
import androidx.core.animation.doOnEnd
import androidx.core.graphics.blue
import androidx.core.graphics.green
import androidx.core.graphics.red
import androidx.core.util.Consumer
import androidx.gridlayout.widget.GridLayout
import java.util.Random

enum class Direction {
    UP, DOWN, RIGHT, LEFT
}

data class TrailsExerciseQuestion(
    val instruction: List<Direction>,
    val fromX: Int,
    val fromY: Int,
    val toX: Int,
    val toY: Int
)

class TrailsExercise(
    exerciseControl: ExerciseControl,
    onFinishedCallback: Consumer<ExerciseResult>,
    group: ViewGroup,
    inflater: LayoutInflater,
    activity: MainActivity,
    private val config: TrailsExerciseConfig
) : AbstractExercise(exerciseControl, onFinishedCallback, group, inflater, activity) {
    private lateinit var rootFrame: FrameLayout
    private lateinit var frame: ViewGroup

    private lateinit var fieldView: GridLayout
    private lateinit var instructionView: LinearLayout

    private lateinit var logic: TrailsExerciseLogic

    private val random = Random()

    private lateinit var currentQuestion: TrailsExerciseQuestion
    private var newQuestion = false

    private val innerViews = ArrayList<ArrayList<ImageView>>()
    private val outerViews = ArrayList<ArrayList<ImageView>>()

    private var beginX = 0
    private var beginY = 0

    private var state = State.TRANSITION

    private val res = activity.resources

    private var timerStarted = 0L
    private var timerEnded = -1L

    private val timer = ExerciseTimer({ t -> timedOut(); timerEnded = t }, { state == State.QUESTION_ACTIVE },
        exerciseControl, 0
    )

    enum class State {
        QUESTION_ACTIVE, TRANSITION
    }

    enum class QuestionResult {
        CORRECT, WRONG, TIMEOUT
    }

    override fun init() {
        rootFrame = group.findViewById(R.id.frame)
        frame = inflater.inflate(R.layout.trails_exercise_frame, rootFrame, true) as ViewGroup
        fieldView = frame.findViewById(R.id.field)
        instructionView = frame.findViewById(R.id.instruction)
        logic = TrailsExerciseLogic(
            initialSecondsPerQuestion = config.secondsPerQuestion,
            dynamic = config.dynamic,
            totalRounds = config.nRounds,
            fieldSize = config.fieldSize,
            exerciseControl = exerciseControl,
            instructionLength = config.instructionLength,
            res = res
        )
        timer.secondsPerQuestion = logic.secondsPerQuestion
        beginX = random.nextInt(logic.fieldSize)
        beginY = random.nextInt(logic.fieldSize)
    }

    override fun start() {
        super.start()
        exerciseControl.totalRounds = logic.totalRounds
        exerciseControl.round = 0
        exerciseControl.score = 0
        initField()
        nextQuestion()
    }

    // todo postpone enter transition
    private fun initField() {
        fieldView.rowCount = logic.fieldSize
        fieldView.columnCount = logic.fieldSize
        for (i in 0 until logic.fieldSize) { // i - row
            outerViews.add(ArrayList())
            innerViews.add(ArrayList())
            for (j in 0 until logic.fieldSize) { // j - column
                val outerView = createFieldSubView(true)
                val innerView = createFieldSubView(false)
                fieldView.addView(outerView, createFieldSubViewLayoutParams(i, j, true))
                fieldView.addView(innerView, createFieldSubViewLayoutParams(i, j, false))
                outerViews[i].add(outerView)
                innerViews[i].add(innerView)
                outerView.setOnClickListener {
                    if (state != State.QUESTION_ACTIVE) {
                        return@setOnClickListener
                    }
                    handleChoice(j, i)
                }
            }
        }
    }

    // todo theme
    private val innerColor = parseColor("#e3e3e3")
    private val outerColor = parseColor("#a3a3a3")
    private val innerColorSelected = Color.GREEN
    private val outerColorSelected = Color.DKGRAY

    private fun createFieldSubView(outline: Boolean): ImageView {
        val view = ImageView(activity)
        view.setImageResource(R.drawable.ic_baseline_circle_24)
        view.imageTintList = ColorStateList.valueOf(if (outline) outerColor else innerColor)
        // todo add elevation
        return view
    }

    private fun createFieldSubViewLayoutParams(row: Int, column: Int, outline: Boolean): GridLayout.LayoutParams {
        val lp = GridLayout.LayoutParams(
            GridLayout.spec(logic.fieldSize - row - 1, GridLayout.CENTER),
            GridLayout.spec(column, GridLayout.CENTER)
        )
        var size = activity.resources.displayMetrics.widthPixels / logic.fieldSize - TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, 8f, activity.resources.displayMetrics
        )
        if (!outline) {
            size *= 0.85f
        }
        lp.width = size.toInt()
        lp.height = size.toInt()
        return lp
    }

    private fun clear() {
        instructionView.removeAllViews()
    }

    private fun nextQuestion() {
        if (exerciseControl.round == logic.totalRounds) {
            endExercise()
            return
        }
        exerciseControl.round++
        exerciseControl.timer = logic.secondsPerQuestion
        exerciseControl.progress = 1f
        clear()
        setupNextQuestion()
    }

    private fun setupNextQuestion() {
        val fromX = beginX
        val fromY = beginY
        var x = fromX
        var y = fromY
        val instruction = ArrayList<Direction>()
        for (i in 1..logic.instructionLength) {
            val set = HashSet<Direction>()
            for (dir in Direction.values()) {
                set.add(dir)
            }
            if (x == 0)
                set.remove(Direction.LEFT)
            if (x == logic.fieldSize - 1)
                set.remove(Direction.RIGHT)
            if (y == 0)
                set.remove(Direction.DOWN)
            if (y == logic.fieldSize - 1)
                set.remove(Direction.UP)
            val current = set.random()
            x = updX(x, current)
            y = updY(y, current)
            instruction.add(current)
        }
        beginX = x
        beginY = y
        currentQuestion = TrailsExerciseQuestion(instruction, fromX, fromY, x, y)
        newQuestion = true
        render()
    }

    private var firstRender = true

    private fun render() {
        if (newQuestion) {
            newQuestion = false
        } else {
            return
        }

        // show instruction
        for (dir in currentQuestion.instruction) {
            instructionView.addView(createDirectionView(dir))
        }

        if (firstRender) {
            firstRender = false
            val anim = ValueAnimator.ofFloat(0f, 1f)
            anim.addUpdateListener {
                innerViews[currentQuestion.fromY][currentQuestion.fromX].imageTintList = ColorStateList.valueOf(
                    averageColor(innerColor, innerColorSelected, anim.animatedValue as Float)
                )
                outerViews[currentQuestion.fromY][currentQuestion.fromX].imageTintList = ColorStateList.valueOf(
                    averageColor(outerColor, outerColorSelected, anim.animatedValue as Float)
                )
            }
            anim.duration = res.getInteger(R.integer.trails_exercise_selection_fade_in_out).toLong()
            anim.start()
            anim.doOnEnd {
                questionLoaded()
            }
        } else {
            // update field
            innerViews[currentQuestion.fromY][currentQuestion.fromX].imageTintList =
                ColorStateList.valueOf(innerColorSelected)
            outerViews[currentQuestion.fromY][currentQuestion.fromX].imageTintList =
                ColorStateList.valueOf(outerColorSelected)

            questionLoaded()
        }
    }

    private fun questionLoaded() {
        state = State.QUESTION_ACTIVE
        timerEnded = -1L
        timerStarted = timer.start()
    }

    private fun questionUnloaded() {
        Log.d(LOGGING_TAG, "${ExerciseMode.TRAILS} question unloaded.")

        // reset data
        animPathCompleted = false

        nextQuestion()
    }

    private fun createDirectionView(dir: Direction): View {
        val view = ImageView(activity)
        view.setImageResource(R.drawable.ic_baseline_arrow_forward_24)
        view.rotation = when (dir) {
            Direction.RIGHT -> 0f
            Direction.DOWN -> 90f
            Direction.LEFT -> 180f
            Direction.UP -> 270f
        }

        val size = ((0.8f * activity.resources.displayMetrics.widthPixels) / currentQuestion.instruction.size
                - TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8f, activity.resources.displayMetrics)).toInt()
        view.layoutParams = LinearLayout.LayoutParams(size, size)

        return view
    }

    private fun handleChoice(x: Int, y: Int) {
        if (x == currentQuestion.toX && y == currentQuestion.toY) {
            handleCorrectChoice()
        } else {
            handleIncorrectChoice()
        }
    }

    private fun rgb(red: Int, green: Int, blue: Int): Int {
        return -0x1000000 or (red shl 16) or (green shl 8) or blue
    }

    private fun averageColor(@ColorInt a: Int, @ColorInt b: Int, k: Float): Int {
        val r1 = a.red
        val g1 = a.green
        val b1 = a.blue
        val r2 = b.red
        val g2 = b.green
        val b2 = b.blue
        return rgb(r1 + (k * (r2 - r1)).toInt(), g1 + (k * (g2 - g1)).toInt(), b1 + (k * (b2 - b1)).toInt())
    }

    private fun getDegreeColor(degree: Int): Int = when (degree) {
        5 -> innerColorSelected
        4 -> rgb(107, 255, 107)
        3 -> rgb(140, 255, 140)
        2 -> rgb(177, 255, 177)
        1 -> rgb(205, 255, 205)
        0 -> innerColor
        else -> throw IllegalArgumentException()
    }

    private val colorPaintDegreeMap: MutableMap<Pair<Int, Int>, Int> = HashMap()

    private var animPathCompleted = false

    private fun colorPoint(degree: Int, x: Int, y: Int, handler: Handler) {
        if (colorPaintDegreeMap.contains(Pair(x, y)) && colorPaintDegreeMap[Pair(x, y)]!! > degree
            || degree != 5 && x == currentQuestion.toX && y == currentQuestion.toY && animPathCompleted
        ) {
            colorPaintDegreeMap.remove(Pair(x, y))
            return
        }
        innerViews[y][x].imageTintList = ColorStateList.valueOf(getDegreeColor(degree))
//        innerViews[y][x].imageTintList = ColorStateList.valueOf(averageColor(Color.WHITE, Color.GREEN, degree / 3.0f))
        if (degree != 0 && (x != currentQuestion.toX || y != currentQuestion.toY || !animPathCompleted)) {
            colorPaintDegreeMap[Pair(x, y)] = degree - 1
            handler.postDelayed(
                { colorPoint(degree - 1, x, y, handler) },
                activity.resources.getInteger(R.integer.trails_exercise_movement_anim_delay).toLong()
            )
        } else {
            colorPaintDegreeMap.remove(Pair(x, y))
        }
        if (colorPaintDegreeMap.isEmpty()) {
            questionUnloaded()
        }
    }

    private fun handleCorrectChoice() {
        Log.d(LOGGING_TAG, "Correct choice")

        endQuestion(QuestionResult.CORRECT)
        logic.correctChoice((timerEnded - timerStarted) / 1000f)

        var x = currentQuestion.fromX
        var y = currentQuestion.fromY
        val handler = Handler(Looper.getMainLooper())
        val iterator = currentQuestion.instruction.iterator()
        colorPoint(4, x, y, handler)
        val runnable = object : Runnable {
            override fun run() {
                val dir = iterator.next()
                outerViews[y][x].imageTintList = ColorStateList.valueOf(outerColor)
                x = updX(x, dir)
                y = updY(y, dir)
                if (!iterator.hasNext()) {
                    animPathCompleted = true
                }
                colorPoint(5, x, y, handler)
                outerViews[y][x].imageTintList = ColorStateList.valueOf(outerColorSelected)
                if (iterator.hasNext()) {
                    handler.postDelayed(
                        this, activity.resources.getInteger(R.integer.trails_exercise_movement_anim_delay).toLong()
                    )
                }
            }
        }
        runnable.run()
    }

    private fun updX(x: Int, dir: Direction): Int = when (dir) {
        Direction.RIGHT -> x + 1
        Direction.LEFT -> x - 1
        else -> x
    }

    private fun updY(y: Int, dir: Direction): Int = when (dir) {
        Direction.UP -> y + 1
        Direction.DOWN -> y - 1
        else -> y
    }

    private fun questionFailed(result: QuestionResult) {
        Log.d(LOGGING_TAG, "Question failed")
        endQuestion(result)

        var nextCoords = random.nextInt(logic.fieldSize * logic.fieldSize - 1)
        val currentCoords = currentQuestion.fromY * logic.fieldSize + currentQuestion.fromX
        if (nextCoords >= currentCoords) {
            ++nextCoords
        }
        beginX = nextCoords % logic.fieldSize
        beginY = nextCoords / logic.fieldSize

        val anim = ValueAnimator.ofFloat(0f, 1f)
        anim.addUpdateListener {
            innerViews[currentQuestion.fromY][currentQuestion.fromX].imageTintList = ColorStateList.valueOf(
                averageColor(innerColorSelected, innerColor, anim.animatedValue as Float)
            )
            outerViews[currentQuestion.fromY][currentQuestion.fromX].imageTintList = ColorStateList.valueOf(
                averageColor(outerColorSelected, outerColor, anim.animatedValue as Float)
            )
            innerViews[beginY][beginX].imageTintList = ColorStateList.valueOf(
                averageColor(innerColor, innerColorSelected, anim.animatedValue as Float)
            )
            outerViews[beginY][beginX].imageTintList = ColorStateList.valueOf(
                averageColor(outerColor, outerColorSelected, anim.animatedValue as Float)
            )
        }
        anim.duration = res.getInteger(R.integer.trails_exercise_selection_fade_in_out).toLong()
        anim.doOnEnd {
            questionUnloaded()
        }
        anim.start()
    }

    private fun handleIncorrectChoice() {
        questionFailed(QuestionResult.WRONG)
        logic.incorrectChoice((timerEnded - timerStarted) / 1000f)
    }

    private fun timedOut() {
        questionFailed(QuestionResult.TIMEOUT)
        logic.timedOut((timerEnded - timerStarted) / 1000f)
    }

    private fun endQuestion(result: QuestionResult) {
        if (timerEnded == -1L) {
            timerEnded = System.currentTimeMillis()
        }

        state = State.TRANSITION

        val valCopy = timer.getProgress()
        timer.end()
        exerciseControl.progress = valCopy
    }

    private fun endExercise() {
        Log.d(LOGGING_TAG, "Exercise completed")
        finish(ExerciseResult(ExerciseMode.SHAPE_FUSION, exerciseControl.score, Any()))
    }

    override fun pause() {
        TODO("Not yet implemented")
    }

    override fun resume() {
        TODO("Not yet implemented")
    }
}
