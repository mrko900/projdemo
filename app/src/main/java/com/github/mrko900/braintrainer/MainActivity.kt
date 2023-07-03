package com.github.mrko900.braintrainer

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.util.Consumer
import androidx.core.view.iterator
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.fragment.NavHostFragment
import com.github.mrko900.braintrainer.databinding.MainBinding

class MainActivity : AppCompatActivity() {
    companion object {
        private fun getFragmentId(navId: Int): Int = when (navId) {
            R.id.home -> R.id.fragment_home
            R.id.exercises -> R.id.fragment_exercise_list
            R.id.stats -> R.id.fragment_stats
            R.id.profile -> R.id.fragment_profile
            else -> throw IllegalArgumentException("unknown fragment")
        }
    }

    private lateinit var binding: MainBinding
    lateinit var navigation: NavController

    var currentExercise: ExerciseParams? = null

    var currentExerciseResult: ExerciseResult? = null
    var currentExerciseResultManager: ExerciseResultManager? = null

    val statsManager = StatsManager()

    var testCurrentRating = 0

    private fun getMenuIndices(menu: Menu): Map<Int, Int> {
        val res = HashMap<Int, Int>()
        var i = 0
        for (item in menu) {
            res[item.itemId] = i++
        }
        return res
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = MainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        navigation = (supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment).navController

        val animRight: NavOptions = NavOptions.Builder()
            .setEnterAnim(R.anim.slide_in_right_fade_in)
            .setExitAnim(R.anim.slide_out_right_fade_out)
            .build()
        val animLeft: NavOptions = NavOptions.Builder()
            .setEnterAnim(R.anim.slide_in_left_fade_in)
            .setExitAnim(R.anim.slide_out_left_fade_out)
            .build()

        val menuIndices = getMenuIndices(binding.navView.menu)
        val animDuration: Long = resources.getInteger(R.integer.anim_duration).toLong()
        val handler = Handler(Looper.getMainLooper())

        var clickable = true

        binding.navView.setOnItemSelectedListener {
            if (!clickable || binding.navView.selectedItemId == it.itemId)
                return@setOnItemSelectedListener false
            val slideLeft = menuIndices.getValue(it.itemId) > menuIndices.getValue(binding.navView.selectedItemId)
            navigation.navigate(
                getFragmentId(it.itemId),
                navOptions = if (slideLeft) animLeft else animRight, args = null
            )
            clickable = false
            handler.postDelayed({
                clickable = true
            }, animDuration)
            return@setOnItemSelectedListener true
        }
    }

    fun createExercise(group: ViewGroup, onFinishedCallback: Consumer<ExerciseResult>): Exercise {
        if (currentExercise == null) {
            throw IllegalStateException("can't create exercise")
        }
        val exerciseControl = ExerciseControl(
            this,
            group.findViewById(R.id.timer),
            group.findViewById(R.id.timerProgressBar),
            group.findViewById(R.id.scoreStatus),
            group.findViewById(R.id.roundStatus),
            group.findViewById(R.id.status)
        )
        return when (currentExercise!!.mode) {
            ExerciseMode.SHAPE_FUSION -> {
                ShapeFusionExercise(
                    exerciseControl, onFinishedCallback, group, layoutInflater, this,
                    currentExercise!!.config as ShapeFusionExerciseConfig
                )
            }
            ExerciseMode.TRAILS -> {
                TrailsExercise(
                    exerciseControl, onFinishedCallback, group, layoutInflater, this,
                    currentExercise!!.config as TrailsExerciseConfig
                )
            }
            ExerciseMode.MATH_CHAINS -> {
                MathChainsExercise(
                    exerciseControl, onFinishedCallback, group, layoutInflater, this,
                    currentExercise!!.config as MathChainsExerciseConfig
                )
            }
            else -> throw UnsupportedOperationException()
        }
    }

    fun setNavPaneVisible(visible: Boolean) {
        binding.navView.visibility = if (visible) View.VISIBLE else View.GONE
    }

    var onBackPressedCallback: Runnable? = null

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (item.itemId == android.R.id.home) {
            onBackPressedCallback?.run()
            true
        } else {
            false
        }
    }
}
