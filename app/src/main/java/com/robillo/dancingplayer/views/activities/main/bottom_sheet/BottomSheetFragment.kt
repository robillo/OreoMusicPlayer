package com.robillo.dancingplayer.views.activities.main.bottom_sheet

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.design.widget.BottomSheetDialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.robillo.dancingplayer.R
import com.robillo.dancingplayer.models.Song
import com.robillo.dancingplayer.preferences.PreferencesHelper
import com.robillo.dancingplayer.utils.AppConstants
import com.robillo.dancingplayer.utils.ApplicationUtils
import com.robillo.dancingplayer.views.activities.home.HomeActivity

import butterknife.ButterKnife

import com.robillo.dancingplayer.utils.AppConstants.ALBUM
import com.robillo.dancingplayer.utils.AppConstants.ALBUM_ID
import com.robillo.dancingplayer.utils.AppConstants.ALBUM_KEY
import com.robillo.dancingplayer.utils.AppConstants.ARTIST
import com.robillo.dancingplayer.utils.AppConstants.ARTIST_ID
import com.robillo.dancingplayer.utils.AppConstants.ARTIST_KEY
import com.robillo.dancingplayer.utils.AppConstants.COMPOSER
import com.robillo.dancingplayer.utils.AppConstants.DATA
import com.robillo.dancingplayer.utils.AppConstants.DATE_ADDED
import com.robillo.dancingplayer.utils.AppConstants.DATE_MODIFIED
import com.robillo.dancingplayer.utils.AppConstants.DEFAULT_PLAYLIST_TITLE
import com.robillo.dancingplayer.utils.AppConstants.DURATION
import com.robillo.dancingplayer.utils.AppConstants.FROM_SONGS_LIST
import com.robillo.dancingplayer.utils.AppConstants.ID
import com.robillo.dancingplayer.utils.AppConstants.INDEX
import com.robillo.dancingplayer.utils.AppConstants.MOST_PLAYED
import com.robillo.dancingplayer.utils.AppConstants.RECENTLY_ADDED
import com.robillo.dancingplayer.utils.AppConstants.RECENTLY_PLAYED
import com.robillo.dancingplayer.utils.AppConstants.SIZE
import com.robillo.dancingplayer.utils.AppConstants.TITLE
import com.robillo.dancingplayer.utils.AppConstants.TITLE_KEY
import com.robillo.dancingplayer.utils.AppConstants.YEAR
import kotlinx.android.synthetic.main.fragment_bottom_sheet_dialog.view.*

class BottomSheetFragment : BottomSheetDialogFragment(), BottomSheetMvpView {

    private var song = Song()
    private var index = -1

    private var helper: PreferencesHelper? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.fragment_bottom_sheet_dialog, container, false)
        setup(v)
        return v
    }

    override fun setup(v: View) {
        ButterKnife.bind(this, v)
        val bundle = arguments


        helper = PreferencesHelper(activity!!)
        val currentUserThemeColors = AppConstants.themeMap[helper!!.userThemeName]
        v.title!!.setBackgroundColor(resources.getColor(R.color.white))
        v.artist_size!!.setBackgroundColor(resources.getColor(R.color.white))
        v.line!!.setBackgroundColor(resources.getColor(currentUserThemeColors!!.colorPrimaryDark))

        v.title!!.isSelected = true
        v.artist_size!!.isSelected = true

        showRemoveFromPlaylistIfUserPlaylist(v)

        if (bundle != null) {
            index = bundle.getInt(INDEX)

            song.data = bundle.getString(DATA)
            song.title = bundle.getString(TITLE)
            song.titleKey = bundle.getString(TITLE_KEY)

            song.id = bundle.getString(ID)!!
            song.dateAdded = bundle.getString(DATE_ADDED)
            song.dateModified = bundle.getString(DATE_MODIFIED)
            song.duration = bundle.getString(DURATION)
            song.composer = bundle.getString(COMPOSER)
            song.album = bundle.getString(ALBUM)
            song.albumId = bundle.getString(ALBUM_ID)
            song.albumKey = bundle.getString(ALBUM_KEY)
            song.artist = bundle.getString(ARTIST)
            song.artistId = bundle.getString(ARTIST_ID)
            song.artistKey = bundle.getString(ARTIST_KEY)
            song.year = bundle.getString(YEAR)
            song.size = bundle.getString(SIZE)

            v.title!!.text = song.title
            val size = Integer.valueOf(song.size) / 1024
            val artistSize = song.artist + " (" + ApplicationUtils().formatSizeKBtoMB(size.toDouble()) + ")"
            v.artist_size!!.text = artistSize
        }

        setClickListeners(v)
    }

    private fun setClickListeners(v: View) {
        v.remove_from_playlist!!.setOnClickListener { removeFromPlaylist() }
        v.add_to_playlist!!.setOnClickListener { addToPlaylist() }
        v.rate_app!!.setOnClickListener { setRateApp() }
        v.delete_song!!.setOnClickListener { setDeleteSong() }
    }

    override fun showRemoveFromPlaylistIfUserPlaylist(v: View) {
        if (activity != null) {
            val current = PreferencesHelper(activity!!).currentPlaylistTitle
            if (current == DEFAULT_PLAYLIST_TITLE || current == MOST_PLAYED || current == RECENTLY_ADDED || current == RECENTLY_PLAYED) {
                v.remove_from_playlist!!.visibility = View.GONE
                v.findViewById<View>(R.id.line_remove_from_playlist).visibility = View.GONE
            }
        }
    }

    fun setDeleteSong() {
        val activity = activity as HomeActivity?
        activity?.deleteSong(index, song)
    }

    fun setRateApp() {
        if (activity != null) {
            val uri = Uri.parse("market://details?id=" + activity!!.packageName)
            val goToMarket = Intent(Intent.ACTION_VIEW, uri)
            // To count with Play market backstack, After pressing back button,
            // to taken back to our application, we need to add following flags to intent.
            goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY or
                    Intent.FLAG_ACTIVITY_NEW_DOCUMENT or
                    Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
            try {
                startActivity(goToMarket)
            } catch (e: ActivityNotFoundException) {
                startActivity(Intent(Intent.ACTION_VIEW,
                        Uri.parse("http://play.google.com/store/apps/details?id=" + activity!!.packageName)))
            }

        }
    }

    fun addToPlaylist() {
        val activity = activity as HomeActivity?
        if (activity != null) {
            activity.hideOrRemoveBottomSheet()
            activity.showPlaylistBottomSheet(FROM_SONGS_LIST, song.id)
        }
    }

    fun removeFromPlaylist() {
        val activity = activity as HomeActivity?
        if (activity != null) {
            activity.hideOrRemoveBottomSheet()
            activity.removeSongCurrentPlaylist(song, index)
        }
    }
}
