package com.github.mrko900.braintrainer

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.mrko900.braintrainer.databinding.ExerciseListBinding
import kotlin.math.roundToInt

class ExerciseListFragment : Fragment() {
    private lateinit var binding: ExerciseListBinding
    private lateinit var mainActivity: MainActivity

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        if (activity == null) {
            Log.e(LOGGING_TAG, "activity must be non-null")
            throw IllegalStateException("activity must be non-null")
        }
        mainActivity = activity as MainActivity
        binding = ExerciseListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initListView(binding.exerciseListView)
        postponeEnterTransition()
    }

    private fun initListView(listView: RecyclerView) {
        val spanCount = resources.getInteger(R.integer.exercise_list_span_count)
        listView.layoutManager = GridLayoutManager(context, spanCount)
        val nav = (activity!!.supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment)
            .navController
        val exerciseListItems = exerciseListItems()
        val adapter = ExerciseListViewAdapter(listView, layoutInflater, resources, nav, exerciseListItems.size, {
            startPostponedEnterTransition()
        }, 6, mainActivity) // todo calc maxItemsVisibleAtFirst and preCreateViews
        adapter.preCreateViewHolders()
        listView.adapter = adapter
        listView.addItemDecoration(
            ExerciseListViewItemDecoration(
                resources.getDimension(R.dimen.exercise_list_spacing).roundToInt(), spanCount
            )
        )
        for (item in exerciseListItems)
            adapter.addItem(item)
    }
}
