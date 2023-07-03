package com.github.mrko900.braintrainer

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.util.Consumer
import java.util.Random


class MathChainsExercise(
    exerciseControl: ExerciseControl,
    onFinishedCallback: Consumer<ExerciseResult>,
    group: ViewGroup,
    inflater: LayoutInflater,
    activity: MainActivity,
    private val config: MathChainsExerciseConfig
) : AbstractExercise(exerciseControl, onFinishedCallback, group, inflater, activity) {
    enum class Operation {
        ADD, SUBTRACT, MULTIPLY, DIVIDE
    }

    enum class State {
        QUESTION_ACTIVE, TRANSITION
    }

    private lateinit var rootFrame: FrameLayout
    private lateinit var frame: ViewGroup

    private lateinit var chainsView: LinearLayout

    private lateinit var logic: MathChainsExerciseLogic

    private val random = Random()

    private lateinit var currentQuestion: Question

    private var state = State.TRANSITION

    private var timerStarted = 0L
    private var timerEnded = -1L

    private val lastResult = ArrayList<Boolean>() // true = success

    private val timer = ExerciseTimer({ t -> timedOut(); timerEnded = t }, { state == State.QUESTION_ACTIVE },
        exerciseControl, 0
    )

    private val res = activity.resources

    override fun init() {
        rootFrame = group.findViewById(R.id.frame)
        frame = inflater.inflate(R.layout.math_chains_exercise_frame, rootFrame, true) as ViewGroup
        chainsView = frame.findViewById(R.id.chains)
        logic = MathChainsExerciseLogic(
            exerciseControl = exerciseControl,
            res = res,
            initialSecondsPerQuestion = config.secondsPerQuestion,
            totalRounds = config.nRounds,
            initialNChains = config.nChains
        )
        for (i in 1..logic.nChains) {
            lastResult.add(false)
        }
    }

    override fun pause() {
        TODO("Not yet implemented")
    }

    override fun resume() {
        TODO("Not yet implemented")
    }

    override fun start() {
        super.start()
        exerciseControl.totalRounds = logic.totalRounds
        exerciseControl.round = 0
        exerciseControl.score = 0

        initViews()
        showKeyboard()
        initKeyboard()
        initData()

        nextQuestion()
    }

    private fun initData() {
        for (i in 0 until logic.nChains) {
            setChainVal(i, random.nextInt(30) + 1) // todo difficulty
        }
    }

    private fun showKeyboard() {
        val view = frame.findViewById<View>(R.id.kbhost)
        view.requestFocus()
        view.requestFocusFromTouch()
        val imm = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
    }

    private fun initKeyboard() {
        val view = frame.findViewById<EditText>(R.id.kbhost)
        view.setOnEditorActionListener(TextView.OnEditorActionListener { _, _, _ ->
            Log.d(LOGGING_TAG, "EVENT")
            return@OnEditorActionListener true
        })
        view.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s!!.length > 4) {
                    view.setText(s.subSequence(0, 4))
                    view.setSelection(4)
                }
                frame.findViewById<TextView>(R.id.input).text = view.text
                if (view.text.isNotEmpty()) {
                    attempt()
                }
            }

            override fun afterTextChanged(s: Editable?) {
            }
        })
    }

    private fun attempt() {
        val ans = frame.findViewById<TextView>(R.id.input).text.toString().toInt()
        Log.d(LOGGING_TAG, "ans, eval, op = " + ans + " " + currentQuestion.eval + " " + currentQuestion.op)
        if (ans == currentQuestion.eval) {
            handleSuccess()
        }
    }

    private fun handleSuccess() {
        Log.d(LOGGING_TAG, "Success")
        lastResult[currentQuestion.chain] = true
        endQuestion()
        logic.success((timerEnded - timerStarted) / 1000f)
        questionUnloaded() // todo anim
    }

    private fun endQuestion() {
        if (timerEnded == -1L) {
            timerEnded = System.currentTimeMillis()
        }
        state = State.TRANSITION
        val valCopy = timer.getProgress()
        timer.end()
        exerciseControl.progress = valCopy

        setChainVal(currentQuestion.chain, currentQuestion.eval)
    }

    private fun timedOut() {
        questionFailed()
        logic.timedOut((timerEnded - timerStarted) / 1000f)
        questionUnloaded() // todo anim
    }

    private fun questionFailed() {
        Log.d(LOGGING_TAG, "Question failed")
        lastResult[currentQuestion.chain] = false
        endQuestion()
    }

    private fun questionLoaded() {
        state = State.QUESTION_ACTIVE
        timerEnded = -1L
        timerStarted = timer.start()
    }

    private fun questionUnloaded() {
        Log.d(LOGGING_TAG, "${ExerciseMode.TRAILS} question unloaded.")
        frame.findViewById<TextView>(R.id.input).text = ""
        frame.findViewById<EditText>(R.id.kbhost).setText("")
        nextQuestion()
    }

    private fun initViews() {
        for (i in 1..logic.nChains) {
            val chainView = inflater.inflate(R.layout.math_chain, chainsView, false)
            val lp = chainView.layoutParams as LinearLayout.LayoutParams
            lp.weight = 1f
            chainsView.addView(chainView)
        }
    }

    private fun setOperation(chain: Int, op: Operation) {
        val res = when (op) {
            Operation.ADD, Operation.MULTIPLY -> R.drawable.ic_baseline_add_24
            Operation.SUBTRACT -> R.drawable.ic_baseline_remove_24
            Operation.DIVIDE -> R.drawable.ic_baseline_percent_24
        }
        val img = chainsView.getChildAt(chain).findViewById<ImageView>(R.id.operation)
        img.setImageResource(res)
        if (op == Operation.MULTIPLY || op == Operation.DIVIDE) {
            img.rotation = 45f
        } else {
            img.rotation = 0f
        }
    }

    private fun setChainVal(chain: Int, newVal: Int) {
        logic.setChainVal(chain, newVal)
        chainsView.getChildAt(chain).findViewById<TextView>(R.id.chainVal).text = newVal.toString()
    }

    private fun setValue(chain: Int, value: Int) {
        chainsView.getChildAt(chain).findViewById<TextView>(R.id.value).text = value.toString()
    }

    private inner class Question(val chain: Int, val op: Operation, val operand: Int) {
        val eval: Int = when (op) {
            Operation.ADD -> {
                logic.chainVals[chain] + operand
            }
            Operation.SUBTRACT -> {
                logic.chainVals[chain] - operand
            }
            Operation.MULTIPLY -> {
                logic.chainVals[chain] * operand
            }
            Operation.DIVIDE -> {
                logic.chainVals[chain] / operand
            }
        }
    }

    private fun genQuestion(): Question {
        // TODO pick the operation with current value in mind (e.g. avoid multiplying large numbers)
        val chain = random.nextInt(logic.nChains)
        val op = Operation.values()[random.nextInt(Operation.values().size)]
        val num: Int
        if (op == Operation.DIVIDE) {
            val divisors = ArrayList<Int>()
            var i = 1
            while (i * i <= logic.chainVals[chain]) {
                if (logic.chainVals[chain] % i == 0) {
                    divisors.add(i)
                    if (i * i != logic.chainVals[chain]) {
                        divisors.add(logic.chainVals[chain] / i)
                    }
                }
                ++i
            }
            num = divisors[random.nextInt(divisors.size)]
        } else if (op == Operation.SUBTRACT) {
            num = random.nextInt(logic.chainVals[chain])
        } else if (op == Operation.ADD) {
            num = random.nextInt(20) // TODO difficulty
        } else {
            num = random.nextInt(15)
        }
        return Question(chain, op, num)
    }

    private fun showOrHideChainVal() {
        Log.d(LOGGING_TAG, "chain val test ? ${currentQuestion.chain} ${lastResult[currentQuestion.chain]}")
        chainsView.getChildAt(currentQuestion.chain).findViewById<TextView>(R.id.chainVal).visibility =
            if (!lastResult[currentQuestion.chain]) View.VISIBLE else View.INVISIBLE
    }

    private fun nextQuestion() {
        if (exerciseControl.round == logic.totalRounds) {
            endExercise()
            return
        }
        exerciseControl.round++
        exerciseControl.timer = logic.secondsPerQuestion
        exerciseControl.progress = 1f

        currentQuestion = genQuestion()
        showOrHideChainVal()
        setOperation(currentQuestion.chain, currentQuestion.op)
        setValue(currentQuestion.chain, currentQuestion.operand)
        for (i in 0 until logic.nChains) {
            chainsView.getChildAt(i).visibility = if (i == currentQuestion.chain) View.VISIBLE else View.INVISIBLE
        }
        questionLoaded()
    }

    private fun endExercise() {
        Log.d(LOGGING_TAG, "Exercise completed")
        finish(ExerciseResult(ExerciseMode.SHAPE_FUSION, exerciseControl.score, Any()))
    }
}
