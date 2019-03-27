package com.robillo.dancingplayer.views.activities.main.songs_sort_frag

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.robillo.dancingplayer.R
import com.robillo.dancingplayer.models.SortItem
import com.robillo.dancingplayer.models.ThemeColors
import com.robillo.dancingplayer.preferences.PreferencesHelper
import com.robillo.dancingplayer.utils.AppConstants
import com.robillo.dancingplayer.views.activities.main.songs_sort_frag.adapters.SortSongsAdapter

import java.util.ArrayList
import butterknife.OnClick

import com.robillo.dancingplayer.utils.AppConstants.ALBUM_ASCENDING
import com.robillo.dancingplayer.utils.AppConstants.ALBUM_DESCENDING
import com.robillo.dancingplayer.utils.AppConstants.ARTIST_ASCENDING
import com.robillo.dancingplayer.utils.AppConstants.ARTIST_DESCENDING
import com.robillo.dancingplayer.utils.AppConstants.DATE_ADDED_ASCENDING
import com.robillo.dancingplayer.utils.AppConstants.DATE_ADDED_DESCENDING
import com.robillo.dancingplayer.utils.AppConstants.DATE_MODIFIED_ASCENDING
import com.robillo.dancingplayer.utils.AppConstants.DATE_MODIFIED_DESCENDING
import com.robillo.dancingplayer.utils.AppConstants.DURATION_ASCENDING
import com.robillo.dancingplayer.utils.AppConstants.DURATION_DESCENDING
import com.robillo.dancingplayer.utils.AppConstants.SIZE_ASCENDING
import com.robillo.dancingplayer.utils.AppConstants.SIZE_DESCENDING
import com.robillo.dancingplayer.utils.AppConstants.TITLE_ASCENDING
import com.robillo.dancingplayer.utils.AppConstants.TITLE_DESCENDING
import com.robillo.dancingplayer.utils.AppConstants.YEAR_ASCENDING
import com.robillo.dancingplayer.utils.AppConstants.YEAR_DESCENDING
import kotlinx.android.synthetic.main.fragment_songs_sort.*
import kotlinx.android.synthetic.main.fragment_songs_sort.view.*

/**
 * A simple [Fragment] subclass.
 */
class SongsSortFragment : Fragment(), SongsSortMvpView {

    override fun refreshForUserThemeColors(currentUserThemeColors: ThemeColors?) {
    }

    private var sortItems: MutableList<SortItem> = ArrayList()
    private var helper: PreferencesHelper? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val v = inflater.inflate(R.layout.fragment_songs_sort, container, false)
        setup(v)
        return v
    }

    override fun setup(v: View) {
        helper = PreferencesHelper(activity!!)
        val currentUserThemeColors = AppConstants.themeMap[helper!!.userThemeName]
        refreshForUserThemeColors(currentUserThemeColors, v)

        inflateSortItemsList()
        v.recycler_view.layoutManager = LinearLayoutManager(activity)
        val helper = PreferencesHelper(activity!!)
        val sortAdapter = SortSongsAdapter(activity!!, sortItems, helper.sortOrderForSongs)
        v.recycler_view.adapter = sortAdapter

        setClickListeners(v)
    }

    private fun setClickListeners(v: View) {
        v.coordinator_layout.setOnClickListener { setCoordinatorLayout() }
    }

    private fun refreshForUserThemeColors(currentUserThemeColors: ThemeColors?, v: View) {
        v.header.setBackgroundColor(resources.getColor(currentUserThemeColors!!.colorPrimaryDark))
        v.line_colored.setBackgroundColor(resources.getColor(currentUserThemeColors.colorPrimary))
    }

    override fun inflateSortItemsList() {
        sortItems.add(SortItem(AppConstants.sortOrderMap[SIZE_ASCENDING], SIZE_ASCENDING))
        sortItems.add(SortItem(AppConstants.sortOrderMap[SIZE_DESCENDING], SIZE_DESCENDING))
        sortItems.add(SortItem(AppConstants.sortOrderMap[YEAR_ASCENDING], YEAR_ASCENDING))
        sortItems.add(SortItem(AppConstants.sortOrderMap[YEAR_DESCENDING], YEAR_DESCENDING))
        sortItems.add(SortItem(AppConstants.sortOrderMap[ALBUM_ASCENDING], ALBUM_ASCENDING))
        sortItems.add(SortItem(AppConstants.sortOrderMap[ALBUM_DESCENDING], ALBUM_DESCENDING))
        sortItems.add(SortItem(AppConstants.sortOrderMap[TITLE_ASCENDING], TITLE_ASCENDING))
        sortItems.add(SortItem(AppConstants.sortOrderMap[TITLE_DESCENDING], TITLE_DESCENDING))
        sortItems.add(SortItem(AppConstants.sortOrderMap[ARTIST_ASCENDING], ARTIST_ASCENDING))
        sortItems.add(SortItem(AppConstants.sortOrderMap[ARTIST_DESCENDING], ARTIST_DESCENDING))
        sortItems.add(SortItem(AppConstants.sortOrderMap[DURATION_ASCENDING], DURATION_ASCENDING))
        sortItems.add(SortItem(AppConstants.sortOrderMap[DURATION_DESCENDING], DURATION_DESCENDING))
        sortItems.add(SortItem(AppConstants.sortOrderMap[DATE_ADDED_ASCENDING], DATE_ADDED_ASCENDING))
        sortItems.add(SortItem(AppConstants.sortOrderMap[DATE_ADDED_DESCENDING], DATE_ADDED_DESCENDING))
        sortItems.add(SortItem(AppConstants.sortOrderMap[DATE_MODIFIED_ASCENDING], DATE_MODIFIED_ASCENDING))
        sortItems.add(SortItem(AppConstants.sortOrderMap[DATE_MODIFIED_DESCENDING], DATE_MODIFIED_DESCENDING))
    }

    @OnClick(R.id.coordinator_layout)
    fun setCoordinatorLayout() {
        if (activity != null) activity!!.onBackPressed()
    }
}// Required empty public constructor
