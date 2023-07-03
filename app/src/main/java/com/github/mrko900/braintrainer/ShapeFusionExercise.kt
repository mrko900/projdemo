package com.github.mrko900.braintrainer

import android.animation.Animator
import android.animation.ObjectAnimator
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Space
import android.widget.TableLayout
import androidx.annotation.ColorInt
import androidx.core.util.Consumer
import java.util.Random

data class Shape(private val matrix: List<MutableList<Boolean>>, private val width: Int, private val height: Int) {
    companion object {
        private fun emptyMatrix(w: Int, h: Int): List<MutableList<Boolean>> {
            val mat = ArrayList<MutableList<Boolean>>(h)
            for (i in 1..h) {
                val row = ArrayList<Boolean>(h)
                for (j in 1..w)
                    row.add(false)
                mat.add(row)
            }
            return mat
        }

        private fun deepCopy(mat: List<MutableList<Boolean>>, h: Int): List<MutableList<Boolean>> {
            val res = ArrayList<MutableList<Boolean>>()
            for (i in 0 until h) {
                res.add(ArrayList(mat[i]))
            }
            return res
        }
    }

    constructor(width: Int, height: Int) : this(emptyMatrix(width, height), width, height)

    constructor(other: Shape) : this(ArrayList(deepCopy(other.matrix, other.height)), other.width, other.height)

    fun getHeight(): Int {
        return height
    }

    fun getWidth(): Int {
        return width
    }

    fun get(x: Int, y: Int): Boolean {
        if (x < 0 || x >= width || y < 0 || y >= height)
            throw IllegalArgumentException("coords not in range: whxy $width,$height,$x,$y")
        return matrix[y][x]
    }

    fun set(x: Int, y: Int, value: Boolean) {
        if (x < 0 || x >= width || y < 0 || y >= height)
            throw IllegalArgumentException("coords not in range")
        matrix[y][x] = value
    }

    override fun equals(other: Any?): Boolean {
        if (this === other)
            return true
        if (other !is Shape)
            return false
        for (x in 0 until width) {
            for (y in 0 until height) {
                if (get(x, y) != other.get(x, y))
                    return false
            }
        }
        return true
    }

    override fun hashCode(): Int {
        return matrix.hashCode()
    }
}

data class ShapeFusionExerciseQuestion(val expression: Expression, val choices: List<Shape>, val answerIndex: Int) {
    val answer = choices[answerIndex]

    data class Expression(val operands: List<Shape>, val operators: List<Operator>)

    enum class Operator {
        ADDITION, SUBTRACTION
    }

    init {
        if (!(answerIndex >= 0 && answerIndex < choices.size))
            throw IllegalArgumentException("answer out of bounds")
    }
}

data class ShapeFusionExerciseQuestionStats(val result: ShapeFusionExercise.QuestionResult, val seconds: Float)

class ShapeFusionExerciseStats {
    val questions = ArrayList<ShapeFusionExerciseQuestionStats>()
}

class ShapeFusionExercise(
    exerciseControl: ExerciseControl,
    onFinishedCallback: Consumer<ExerciseResult>,
    group: ViewGroup,
    inflater: LayoutInflater,
    activity: MainActivity,
    private val config: ShapeFusionExerciseConfig
) : AbstractExercise(exerciseControl, onFinishedCallback, group, inflater, activity) {
    private lateinit var currentQuestion: ShapeFusionExerciseQuestion
    private lateinit var currentQuestionParams: QuestionParams
    private lateinit var rootFrame: FrameLayout
    private lateinit var frame: ViewGroup
    private lateinit var choiceListView: LinearLayout
    private lateinit var exprFrameView: TableLayout
    private val res = group.resources
    private var newQuestion = false

    private val operandViews: MutableList<View> = ArrayList()
    private val operatorViews: MutableList<View> = ArrayList()
    private val choiceViews: MutableList<View> = ArrayList()

    private lateinit var logic: ShapeFusionExerciseLogic

    private var targetNextTimerUpd: Long = 0

    private val random = Random()

    private var firstExprFadeIn = true
    private var firstQuestion = true

    private data class QuestionParams(@ColorInt val color: Int)

    private var state = State.TRANSITION

    private val stats = ShapeFusionExerciseStats()

    private val timer = ExerciseTimer({ t -> timedOut(); timerEnded = t }, { state == State.QUESTION_ACTIVE },
        exerciseControl, 0)

    enum class State {
        QUESTION_ACTIVE, TRANSITION
    }

    enum class QuestionResult {
        CORRECT, WRONG, TIMEOUT
    }

    override fun init() {
        rootFrame = group.findViewById(R.id.frame)
        frame = inflater.inflate(R.layout.shape_fusion_exercise_frame, rootFrame, true) as ViewGroup
        choiceListView = frame.findViewById(R.id.choices)
        exprFrameView = frame.findViewById(R.id.exprFrame)
        logic = ShapeFusionExerciseLogic(
            initialSecondsPerQuestion = config.secondsPerQuestion,
            initialNTerms = config.nTermsInitial,
            initialNChoices = config.nChoicesInitial,
            hasAdditionOperation = config.additionOperation,
            hasSubtractionOperation = config.subtractionOperation,
            dynamic = config.dynamic,
            totalRounds = config.nRounds,
            shapeSide = config.shapeSide,
            exerciseControl = exerciseControl,
            res = res
        )
        timer.secondsPerQuestion = logic.secondsPerQuestion
    }

    override fun start() {
        super.start()
        exerciseControl.totalRounds = logic.totalRounds
        exerciseControl.round = 0
        exerciseControl.score = 0
        initViews()
        nextQuestion()
    }

    private fun endExercise() {
        Log.d(LOGGING_TAG, "Exercise completed")
        finish(ExerciseResult(ExerciseMode.SHAPE_FUSION, exerciseControl.score, stats))
    }

    override fun finish(result: ExerciseResult) {
        super.finish(result)
        exerciseControl.endExercise(
            result,
            ShapeFusionExerciseResultManager()
        )
    }

    override fun pause() {
    }

    override fun resume() {
    }

    private fun initViews() {
        // init operands and operators
        for (i in 0 until logic.nTerms) {
            val row = addOperand()
            val start: FrameLayout = row.findViewById(R.id.start)
            // operator
            if (i != 0) {
                val operatorView = inflater.inflate(R.layout.expr_operator, start, false)
                operatorViews.add(operatorView)
                start.addView(operatorView)
                val operatorViewLayoutParams = operatorView.layoutParams as FrameLayout.LayoutParams
                operatorViewLayoutParams.gravity = Gravity.CENTER_VERTICAL or Gravity.END
            }
        }

        // init choices
        var space = Space(choiceListView.context)
        choiceListView.addView(space)
        (space.layoutParams as LinearLayout.LayoutParams).weight = 1F
        for (i in 0 until logic.nChoices) {
            val view = inflater.inflate(R.layout.choice_card, choiceListView, false)
            choiceViews.add(view)
            choiceListView.addView(view)
            choiceListView.addView(Space(choiceListView.context))
            space = Space(choiceListView.context)
            choiceListView.addView(space)
            (space.layoutParams as LinearLayout.LayoutParams).weight = 1F
        }
    }

    private fun randomShape(): Shape {
        val shapeData = ArrayList<ArrayList<Boolean>>()
        for (y in 1..logic.shapeSide) {
            val row = ArrayList<Boolean>()
            shapeData.add(row)
            for (x in 1..logic.shapeSide) {
                row.add(random.nextInt(2) == 1)
            }
        }
        return Shape(shapeData, logic.shapeSide, logic.shapeSide)
    }

    private fun randomOperator(): ShapeFusionExerciseQuestion.Operator {
        return if (random.nextInt(2) == 1)
            ShapeFusionExerciseQuestion.Operator.ADDITION else ShapeFusionExerciseQuestion.Operator.SUBTRACTION
    }

    private data class Choices(val arr: ArrayList<Shape>, val ans: Int)

    private fun genChoices(expr: FullExpr): Choices {
        val choices = ArrayList<Shape>()
        val ansPos = random.nextInt(logic.nChoices)
        for (i in 0 until logic.nChoices) {
            if (i == ansPos)
                choices.add(expr.answer)
            else
                choices.add(randomShape())
        }
        return Choices(choices, ansPos)
    }

    private data class FullExpr(val expression: ShapeFusionExerciseQuestion.Expression, val answer: Shape)

    private fun performOperation(operand: Shape, argument: Shape, operator: ShapeFusionExerciseQuestion.Operator) {
        val v = operator == ShapeFusionExerciseQuestion.Operator.ADDITION
        for (x in 0 until argument.getWidth()) {
            for (y in 0 until argument.getHeight()) {
                if (argument.get(x, y))
                    operand.set(x, y, v)
            }
        }
    }

    private fun genExpression(): FullExpr {
        val operands = ArrayList<Shape>()
        val operators = ArrayList<ShapeFusionExerciseQuestion.Operator>()
        for (i in 1..logic.nTerms) {
            operands.add(randomShape())
            operators.add(randomOperator())
        }
        val answ = Shape(operands[0])
        for (i in 2..logic.nTerms) {
            performOperation(answ, operands[i - 1], operators[i - 2])
        }
        return FullExpr(ShapeFusionExerciseQuestion.Expression(operands, operators), answ)
    }

    @ColorInt
    private fun randomColor(): Int {
        val palette = intArrayOf(
            Color.RED, Color.GREEN, Color.BLUE, Color.MAGENTA, Color.BLACK, Color.parseColor("#FFA500")
        )
        return palette[random.nextInt(palette.size)]
    }

    private fun clear() {
        if (!firstQuestion) {
            operandViews.first().visibility = View.VISIBLE

            val fadeOut = AlphaAnimation(1f, 0f)
            fadeOut.duration = res.getInteger(R.integer.shape_fusion_exercise_first_bitmap_fade_out_duration).toLong()
            val img: ImageView = operandViews.last().findViewById(R.id.imageView2)
            img.startAnimation(fadeOut)
            img.visibility = View.INVISIBLE
        }

        for (choiceView in choiceViews) {
            choiceView.visibility = View.INVISIBLE
            if (!firstQuestion) {
                val fadeOut = AlphaAnimation(1f, 0f)
                fadeOut.duration = res.getInteger(R.integer.shape_fusion_exercise_choice_fade_out_duration).toLong()
                choiceView.startAnimation(fadeOut)
            }
        }
        for (operatorView in operatorViews) {
            operatorView.visibility = View.INVISIBLE
        }
        for (operandView in operandViews.subList(0, logic.nTerms - 1)) {
            operandView.visibility = View.INVISIBLE
        }
    }

    private fun setupNextQuestion() {
        val expr = genExpression()
        val choices = genChoices(expr)
        currentQuestion = ShapeFusionExerciseQuestion(expr.expression, choices.arr, choices.ans)
        currentQuestionParams = QuestionParams(color = randomColor())
        newQuestion = true
        render()
    }

    private fun nextQuestion() {
        if (exerciseControl.round == logic.totalRounds) {
            endExercise()
            return
        }
        exerciseControl.round++
        exerciseControl.timer = logic.secondsPerQuestion
        exerciseControl.progress = 1f
        if (firstQuestion) {
            exprFrameView.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    exprFrameView.viewTreeObserver.removeOnGlobalLayoutListener(this)
                    val first = IntArray(2)
                    operandViews.first().getLocationOnScreen(first)
                    val last = IntArray(2)
                    operandViews.last().getLocationOnScreen(last)
                    operandViews.last().translationY = (first[1] - last[1]).toFloat()
                }
            })
        }

        clear()
        Handler(Looper.getMainLooper()).postDelayed({
            setupNextQuestion()
        }, res.getInteger(R.integer.shape_fusion_exercise_delay_between_questions).toLong())

        if (firstQuestion)
            firstQuestion = false
    }

    private fun getImage(shape: Shape): Bitmap {
        val w = frame.resources.getDimension(R.dimen.choice_card_image_size)
        val h = w
        val gap = frame.resources.getDimension(R.dimen.shape_fusion_gap)

        val bitmap = Bitmap.createBitmap(w.toInt(), h.toInt(), Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint()
        paint.style = Paint.Style.FILL
        paint.color = currentQuestionParams.color

        val xStep = (w - gap * (shape.getWidth() - 1)) / shape.getWidth()
        val yStep = (h - gap * (shape.getHeight() - 1)) / shape.getHeight()
        for (j in 1..shape.getWidth()) {
            for (i in shape.getHeight() downTo 1) {
                if (!shape.get(j - 1, i - 1))
                    continue
                val x = (j - 1) * (xStep + gap)
                val y = (i - 1) * (yStep + gap)
                canvas.drawRect(x, y, x + xStep, y + yStep, paint)
            }
        }

        return bitmap
    }

    private fun render() {
        if (newQuestion) {
            newQuestion = false
            renderChoices()
            renderExpression()
        }
    }

    private fun questionUnloaded() {
        nextQuestion()
    }

    private fun endQuestion(result: QuestionResult) {
        if (timerEnded == -1L) {
            timerEnded = System.currentTimeMillis()
        }
        state = State.TRANSITION
        expressionFadeOut()
        val valCopy = timer.getProgress()
        timer.end()
        exerciseControl.progress = valCopy
        stats.questions.add(
            ShapeFusionExerciseQuestionStats(
                result,
                (timerEnded - timerStarted) / 1000f
            )
        )
    }

    private fun handleCorrectChoice() {
        Log.d(LOGGING_TAG, "Correct choice")
        endQuestion(QuestionResult.CORRECT)
        logic.correctChoice((timerEnded - timerStarted) / 1000f)
    }

    private fun questionFailed(result: QuestionResult) {
        Log.d(LOGGING_TAG, "Question failed")
        endQuestion(result)
    }

    private fun handleIncorrectChoice() {
        questionFailed(QuestionResult.WRONG)
        logic.incorrectChoice((timerEnded - timerStarted) / 1000f)
    }

    private inner class ChoiceListener(val correct: Boolean) : View.OnClickListener {
        override fun onClick(v: View) {
            if (state != State.QUESTION_ACTIVE)
                return
            Log.v(LOGGING_TAG, "Click!")
            if (correct)
                handleCorrectChoice()
            else
                handleIncorrectChoice()
        }
    }

    private fun renderChoices() {
        for (i in 0 until currentQuestion.choices.size) {
            val choice = currentQuestion.choices[i]
            val view = choiceViews[i]
            view.visibility = View.VISIBLE
            val fadeIn = AlphaAnimation(0f, 1f)
            fadeIn.duration = res.getInteger(R.integer.shape_fusion_exercise_choice_fade_in_duration).toLong()
            view.startAnimation(fadeIn)
            view.findViewById<ImageView>(R.id.imageView2).setImageBitmap(getImage(choice))
            view.setOnClickListener(ChoiceListener(choice === currentQuestion.answer || choice == currentQuestion.answer))
        }
    }

    private fun addOperand(): View {
        val row = inflater.inflate(R.layout.expr_row, exprFrameView, false)
        val center: FrameLayout = row.findViewById(R.id.center)

        val operandView = inflater.inflate(R.layout.choice_card, center, false)
        operandViews.add(operandView)
        val operandImg: ImageView = operandView.findViewById(R.id.imageView2)
        operandImg.layoutParams.width = operandView.resources.getDimension(R.dimen.expr_card_image_size).toInt()
        operandImg.layoutParams.height = operandView.resources.getDimension(R.dimen.expr_card_image_size).toInt()
        center.addView(operandView)
        val operandViewLayoutParams = operandView.layoutParams as FrameLayout.LayoutParams
        operandViewLayoutParams.gravity = Gravity.CENTER
        operandViewLayoutParams.leftMargin = 0
        operandViewLayoutParams.rightMargin = 0
        operandViewLayoutParams.topMargin = 0
        operandViewLayoutParams.bottomMargin = 0

        exprFrameView.addView(row)
        return row
    }

    private fun renderExpression() {
        for (i in 0 until currentQuestion.expression.operands.size) {
            // configure operand
            operandViews[i].visibility = View.VISIBLE
            val operandImg: ImageView = operandViews[i].findViewById(R.id.imageView2)
            operandImg.setImageBitmap(getImage(currentQuestion.expression.operands[i]))
            operandImg.visibility = View.VISIBLE
            if (i == logic.nTerms - 1) {
                val fadeIn = AlphaAnimation(0f, 1f)
                fadeIn.duration = res.getInteger(R.integer.shape_fusion_exercise_first_bitmap_fade_in_duration).toLong()
                operandImg.startAnimation(fadeIn)
            }

            // operator
            if (i != 0) {
                val operatorView = operatorViews[i - 1]
                operatorView.visibility = View.VISIBLE
                val img: ImageView = operatorView.findViewById(R.id.imageView3)
                img.setImageResource(
                    if (currentQuestion.expression.operators[i - 1] == ShapeFusionExerciseQuestion.Operator.ADDITION)
                        R.drawable.ic_baseline_add_circle_24
                    else
                        R.drawable.ic_baseline_remove_circle_24
                )
                val color = TypedValue()
                img.context.theme.resolveAttribute(
                    if (currentQuestion.expression.operators[i - 1] == ShapeFusionExerciseQuestion.Operator.ADDITION)
                        R.attr.colorAdd
                    else
                        R.attr.colorSubtract,
                    color, true
                )
                img.imageTintList = ColorStateList.valueOf(color.data)
            }
        }
        if (firstExprFadeIn) {
            firstExprFadeIn = false
            exprFrameView.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    exprFrameView.viewTreeObserver.removeOnGlobalLayoutListener(this)
                    expressionFadeIn()
                }
            })
        } else {
            expressionFadeIn()
        }
    }

    // note: endQuestion() should always be called the first

    private fun timedOut() {
        questionFailed(QuestionResult.TIMEOUT)
        logic.timedOut((timerEnded - timerStarted) / 1000f)
    }

    private var timerStarted = 0L
    private var timerEnded = -1L

    private fun questionLoaded() {
        state = State.QUESTION_ACTIVE
        timerEnded = -1L
        timerStarted = timer.start()
    }

    private fun expressionFadeIn() {
        if (operandViews.isEmpty())
            return

        val alphaAnim = AlphaAnimation(0f, 1f)
        alphaAnim.duration = res.getInteger(R.integer.shape_fusion_exercise_fade_in_duration_operator).toLong()

        val animQueue = ArrayList<Animator>()
        var it: Iterator<Animator>? = null
        var current: Animator?
        var curi = 0
        val last = operandViews.last()
        last.translationX = 0f
        last.translationY = 0f

        val initialLocation = IntArray(2)
        operandViews.first().getLocationOnScreen(initialLocation)
        val finalLocation = IntArray(2)
        last.getLocationOnScreen(finalLocation)

        for (i in 1 until operandViews.size) {
            val prevLocation = IntArray(2)
            operandViews[i - 1].getLocationOnScreen(prevLocation)
            val currentLocation = IntArray(2)
            operandViews[i].getLocationOnScreen(currentLocation)
            val path = Path()
            val diff = currentLocation[1] - prevLocation[1]
            path.arcTo(
                -0.4f * diff,
                (prevLocation[1] - finalLocation[1]).toFloat(),
                0.4f * diff,
                (currentLocation[1] - finalLocation[1]).toFloat(),
                -90f,
                180f,
                false
            )
            val anim = ObjectAnimator.ofFloat(
                last,
                "translationX", "translationY",
                path
            )
            anim.duration = res.getInteger(R.integer.shape_fusion_exercise_fade_in_duration_operand).toLong()
            anim.addListener(object : Animator.AnimatorListener {
                override fun onAnimationEnd(animator: Animator?) {
                    if (it!!.hasNext()) {
                        current = it!!.next()
                        current!!.start()
                    } else {
                        last.translationX = 0f
                        last.translationY = 0f
                        questionLoaded()
                    }
                }

                override fun onAnimationStart(animator: Animator?) {
                    operandViews[curi].visibility = View.VISIBLE
                    val j = curi
                    Handler(Looper.getMainLooper()).postDelayed({
                        operatorViews[j].visibility = View.VISIBLE
                        operatorViews[j].startAnimation(alphaAnim)
                    }, res.getInteger(R.integer.shape_fusion_exercise_fade_in_delay_operator).toLong())
                    ++curi
                }

                override fun onAnimationRepeat(animator: Animator?) {
                }

                override fun onAnimationCancel(animation: Animator?) {
                }
            })
            animQueue.add(anim)
        }
        for (v in operandViews.subList(1, logic.nTerms - 1)) {
            v.visibility = View.INVISIBLE
        }
        for (v in operatorViews) {
            v.visibility = View.INVISIBLE
        }
        if (animQueue.isNotEmpty()) {
            it = animQueue.iterator()
            current = it.next()
            current!!.start()
        }
    }

    private fun expressionFadeOut() {
        val finalLocation = IntArray(2)
        operandViews.last().getLocationOnScreen(finalLocation)

        var operi = logic.nTerms - 2

        val alphaAnim = AlphaAnimation(1f, 0f)
        alphaAnim.duration = res.getInteger(R.integer.shape_fusion_exercise_fade_out_duration_operator).toLong()
        alphaAnim.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation?) {
            }

            override fun onAnimationEnd(animation: Animation?) {
                operatorViews[operi--].visibility = View.INVISIBLE
            }

            override fun onAnimationRepeat(animation: Animation?) {
            }
        })

        val animQueue = ArrayList<Animator>()
        var it: Iterator<Animator>? = null
        var current: Animator?
        val last = operandViews.last()
        var curi = 0
        for (i in operandViews.size - 1 downTo 1) {
            val nextLocation = IntArray(2)
            operandViews[i - 1].getLocationOnScreen(nextLocation)
            val currentLocation = IntArray(2)
            operandViews[i].getLocationOnScreen(currentLocation)
            val path = Path()
            val diff = currentLocation[1] - nextLocation[1]
            path.arcTo(
                -0.4f * diff,
                (nextLocation[1] - finalLocation[1]).toFloat(),
                0.4f * diff,
                (currentLocation[1] - finalLocation[1]).toFloat(),
                90f,
                -180f,
                false
            )
            val anim = ObjectAnimator.ofFloat(
                last,
                "translationX", "translationY",
                path
            )
            anim.duration = res.getInteger(R.integer.shape_fusion_exercise_fade_out_duration_operand).toLong()
            anim.addListener(object : Animator.AnimatorListener {
                override fun onAnimationEnd(animator: Animator?) {
                    operandViews[logic.nTerms - curi - 2].visibility = View.INVISIBLE
                    ++curi
                    if (it!!.hasNext()) {
                        current = it!!.next()
                        current!!.start()
                    } else {
                        questionUnloaded()
                    }
                }

                override fun onAnimationStart(animator: Animator?) {
                    operatorViews[logic.nTerms - curi - 2].startAnimation(alphaAnim)
                }

                override fun onAnimationRepeat(animator: Animator?) {
                }

                override fun onAnimationCancel(animation: Animator?) {
                }
            })
            animQueue.add(anim)
        }
        if (animQueue.isNotEmpty()) {
            it = animQueue.iterator()
            current = it.next()
            current!!.start()
        }
    }
}
