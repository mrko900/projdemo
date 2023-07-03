package com.github.mrko900.braintrainer

import android.content.res.ColorStateList
import android.content.res.Resources
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.recyclerview.widget.RecyclerView
import com.github.mrko900.braintrainer.databinding.ExerciseListItemBinding
import java.lang.Integer.min
import kotlin.math.abs
import kotlin.math.roundToInt

private typealias VH = ExerciseListViewAdapterViewHolder

class ExerciseListViewAdapter(
    private val recyclerView: RecyclerView,
    private val layoutInflater: LayoutInflater,
    private val res: Resources,
    private val nav: NavController,
    private val preCreateViews: Int,
    private var onInitCallback: Runnable? = null,
    private val maxItemsVisibleAtFirst: Int = -1,
    private val activity: MainActivity
) : RecyclerView.Adapter<ExerciseListViewAdapterViewHolder>() {
    private val items: MutableList<ExerciseListViewItemParams> = ArrayList()
    private var createdCnt = 0
    private var initCnt = 0
    private val viewHolders: MutableList<VH> = ArrayList()

    init {
        if (preCreateViews <= 0)
            throw IllegalArgumentException("preCreateViews must be >= 0")
        if (onInitCallback != null && maxItemsVisibleAtFirst <= 0)
            throw IllegalArgumentException("maxItemsVisibleAtFirst must be >0 when onInitCallback is specified")
    }

    fun preCreateViewHolders() {
        for (item in 1..preCreateViews)
            viewHolders.add(VH(layoutInflater, recyclerView, nav, activity))
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExerciseListViewAdapterViewHolder {
        val viewHolder = viewHolders[createdCnt]
        viewHolder.itemView.layoutParams.height = res.getDimension(R.dimen.exercise_list_item_height).roundToInt()
        ++createdCnt
        return viewHolder
    }

    override fun onBindViewHolder(holder: ExerciseListViewAdapterViewHolder, position: Int) {
        val item = items[position]
        holder.mode = item.mode
        holder.binding.title.setText(item.titleResId)
        holder.binding.cardViewRoot.background = ColorDrawable(item.primaryColor)
        holder.binding.cardViewBottom.background = ColorDrawable(item.secondaryColor)
        holder.binding.playButton.backgroundTintList = ColorStateList.valueOf(item.secondaryColor)
        holder.binding.rating.text = item.testRating.toString()
        holder.binding.ratingProgress.text = abs(item.testRatingProgress).toString()
        holder.item = item
        if (item.testRatingProgress >= 0) {
            holder.binding.ratingProgressIcon.setImageResource(R.drawable.ic_baseline_arrow_drop_up_24)
            holder.binding.ratingProgressIcon.imageTintList = ColorStateList.valueOf(Color.parseColor("#3C960B"))
            holder.binding.ratingProgress.setTextColor(Color.parseColor("#3C960B"))
        } else {
            holder.binding.ratingProgressIcon.setImageResource(R.drawable.ic_baseline_arrow_drop_down_24)
            holder.binding.ratingProgressIcon.imageTintList = ColorStateList.valueOf(Color.parseColor("#940000"))
            holder.binding.ratingProgress.setTextColor(Color.parseColor("#940000"))
        }

        if (onInitCallback != null) {
            ++initCnt
            if (initCnt == min(preCreateViews, maxItemsVisibleAtFirst)) {
                onInitCallback!!.run()
                onInitCallback = null
            }
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    fun addItem(item: ExerciseListViewItemParams) {
        items.add(item)
    }

    // todo remove item
}

// todo don't inflate inside the constructor
class ExerciseListViewAdapterViewHolder : RecyclerView.ViewHolder {
    val binding: ExerciseListItemBinding
    private val nav: NavController
    private val activity: MainActivity
    lateinit var mode: ExerciseMode
    lateinit var item: ExerciseListViewItemParams

    constructor(layoutInflater: LayoutInflater, parent: ViewGroup?, nav: NavController, activity: MainActivity) :
            this(ExerciseListItemBinding.inflate(layoutInflater, parent, false), nav, activity)

    private constructor(binding: ExerciseListItemBinding, nav: NavController, activity: MainActivity) :
            super(binding.root) {
        this.binding = binding
        this.nav = nav
        this.activity = activity
        initListItem(binding)
    }

    private fun initListItem(binding: ExerciseListItemBinding) {
        binding.playButton.setOnClickListener {
            Log.d(LOGGING_TAG, "Exercise selected: $mode")
            activity.currentExercise = ExerciseParams(mode, null)
            activity.testCurrentRating = item.testRating
            nav.navigate(
                R.id.fragment_exercise_details,
                navOptions = NavOptions.Builder()
                    .setEnterAnim(R.anim.slide_in_left_fade_in)
                    .setExitAnim(R.anim.slide_out_left_fade_out)
                    .build(),
                args = null
            )
        }
    }
}

class ExerciseListViewItemDecoration(private val space: Int, private val columns: Int) : RecyclerView.ItemDecoration() {
    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        val nItems: Int = ((parent.adapter?.itemCount
            ?: throw IllegalArgumentException("RecyclerView has no adapter")) + columns - 1) / columns * columns
        outRect.left = if (parent.getChildLayoutPosition(view) % columns == 0) space * 2 else space
        outRect.right = if ((parent.getChildLayoutPosition(view) - columns + 1) % columns == 0) space * 2 else space
        outRect.top = if (parent.getChildLayoutPosition(view) < columns) space * 2 else space
        outRect.bottom = if (parent.getChildLayoutPosition(view) >= nItems - columns) space * 2 else space
    }
}

data class ExerciseListViewItemParams(
    val titleResId: Int,
    val primaryColor: Int,
    val secondaryColor: Int,
    val mode: ExerciseMode,
    val testRating: Int,
    val testRatingProgress: Int
)
