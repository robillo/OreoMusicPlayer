package com.robillo.dancingplayer.views.activities.main.songs_sort_frag.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

import com.robillo.dancingplayer.R
import com.robillo.dancingplayer.models.SortItem
import com.robillo.dancingplayer.preferences.PreferencesHelper
import com.robillo.dancingplayer.utils.AppConstants
import com.robillo.dancingplayer.views.activities.home.HomeActivity
import java.util.Locale

import kotlinx.android.synthetic.main.row_sort_order.view.*

class SortSongsAdapter(private val activityContext: Context,
                       private val sortItems: List<SortItem>,
                       currentSongOrderForSongs: String) : RecyclerView.Adapter<SortSongsAdapter.SortSongsHolder>() {
    private var currentSongOrderForSongsIndex = -1

    init {

        val currentSortItem = SortItem(AppConstants.sortOrderMap[currentSongOrderForSongs], currentSongOrderForSongs)
        for (i in sortItems.indices) {
            if (currentSortItem.constantSortOrder == sortItems[i].constantSortOrder) {
                currentSongOrderForSongsIndex = i
                break
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SortSongsHolder {
        return SortSongsHolder(
                LayoutInflater.from(parent.context).inflate(R.layout.row_sort_order, parent, false)
        )
    }

    override fun onBindViewHolder(holder: SortSongsHolder, @SuppressLint("RecyclerView") position: Int) {

        val helper = PreferencesHelper(activityContext)
        val currentUserThemeColors = AppConstants.themeMap[helper.userThemeName]

        holder.title.text = sortItems[position].textToDisplay
        if (currentSongOrderForSongsIndex == position) {
            holder.title.setTextColor(
                    activityContext.resources.getColor(currentUserThemeColors!!.colorPrimary)
            )    //highlight selected item
            holder.title.setBackgroundColor(activityContext.resources.getColor(R.color.colorTextFour))
        } else {
            holder.title.setTextColor(activityContext.resources.getColor(R.color.colorTextOne))
            holder.title.setBackgroundColor(activityContext.resources.getColor(R.color.white))
        }

        holder.itemView.setOnClickListener { v ->
            currentSongOrderForSongsIndex = position
            holder.title.setTextColor(activityContext.resources.getColor(
                    currentUserThemeColors!!.colorPrimary)
            )
            holder.title.setBackgroundColor(activityContext.resources.getColor(R.color.colorTextFive))
            notifyDataSetChanged()

            helper.sortOrderForSongs = sortItems[position].constantSortOrder

            (activityContext as HomeActivity).showSnackBar(
                    String.format(Locale.ENGLISH, "%s - %s",
                            activityContext.getString(R.string.sort_successful), sortItems[position].textToDisplay)
            )

            //refresh the loader for new sort order here
            //Possibly using EventBus on loader in SongListFragment
            activityContext.repopulateListSongsListFragment()
        }
    }

    override fun getItemCount(): Int {
        return sortItems.size
    }

    inner class SortSongsHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var title: TextView = itemView.text
    }
}