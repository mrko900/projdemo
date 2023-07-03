package com.github.mrko900.braintrainer

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.NavOptions
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.mrko900.braintrainer.databinding.MenuItemBinding
import com.github.mrko900.braintrainer.databinding.StatsMenuBinding

class StatsFragment : Fragment() {
    private lateinit var binding: StatsMenuBinding
    private lateinit var mainActivity: MainActivity

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        if (activity == null) {
            Log.e(LOGGING_TAG, "activity must be non-null")
            throw IllegalStateException("activity must be non-null")
        }
        mainActivity = activity as MainActivity
        binding = StatsMenuBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val list = binding.menu
        list.layoutManager = LinearLayoutManager(mainActivity)
        list.adapter = Adapter(mainActivity)
    }

    private data class ListItem(val title: Int, val fragmentId: Int)

    private class Adapter(private val activity: MainActivity) : RecyclerView.Adapter<VH>() {
        companion object {
            private val items = listOf(
                ListItem(R.string.stats_general, R.id.fragment_stats_general),
                ListItem(R.string.stats_progress, R.id.fragment_stats_progress),
                ListItem(R.string.stats_perf_factors, R.id.fragment_stats_perf_factors),
                ListItem(R.string.stats_activity, R.id.fragment_stats_activity)
            )
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            return VH(MenuItemBinding.inflate(activity.layoutInflater, parent, false))
        }

        override fun onBindViewHolder(holder: VH, position: Int) {
            holder.binding.textView5.text = activity.getString(items[position].title)
            holder.binding.item.setOnClickListener {
                activity.navigation.navigate(
                    items[position].fragmentId,
                    navOptions = NavOptions.Builder()
                        .setEnterAnim(R.anim.slide_in_left_fade_in)
                        .setExitAnim(R.anim.slide_out_left_fade_out)
                        .build(),
                    args = null
                )
            }
        }

        override fun getItemCount(): Int {
            return items.size
        }
    }

    private class VH(val binding: MenuItemBinding) : RecyclerView.ViewHolder(binding.root)
}
