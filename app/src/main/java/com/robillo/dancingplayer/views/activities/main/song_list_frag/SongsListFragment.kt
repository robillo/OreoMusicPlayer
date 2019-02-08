package com.robillo.dancingplayer.views.activities.main.song_list_frag

import android.database.Cursor
import android.os.Bundle
import android.provider.MediaStore
import android.support.v4.app.Fragment
import android.support.v4.app.LoaderManager
import android.support.v4.content.ContextCompat
import android.support.v4.content.CursorLoader
import android.support.v4.content.Loader
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.GestureDetector
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Toast

import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.robillo.dancingplayer.R
import com.robillo.dancingplayer.models.ThemeColors
import com.robillo.dancingplayer.utils.AppConstants
import com.robillo.dancingplayer.utils.ApplicationUtils
import com.robillo.dancingplayer.views.activities.main.song_list_frag.adapters.SongsAdapter
import com.robillo.dancingplayer.models.Song
import com.robillo.dancingplayer.preferences.AppPreferencesHelper
import com.robillo.dancingplayer.views.activities.main.MainActivity
import com.simplecityapps.recyclerview_fastscroll.interfaces.OnFastScrollStateChangeListener
import com.willowtreeapps.spruce.Spruce
import com.willowtreeapps.spruce.animation.DefaultAnimations
import com.willowtreeapps.spruce.sort.DefaultSort

import java.util.ArrayList

import butterknife.ButterKnife
import com.robillo.dancingplayer.utils.AppConstants.*

import kotlinx.android.synthetic.main.fragment_songs_list.view.*
import kotlinx.android.synthetic.main.include_bottom_controller.view.*

/**
 * A simple [Fragment] subclass.
 */
class SongsListFragment : Fragment(), LoaderManager.LoaderCallbacks<Cursor> {

    companion object {
        private const val LOADER_ID = 0
        private var LAUNCHED_FROM = FROM_FRAGMENT
    }

    private lateinit var v: View

    private var hideOnFastScroll = false
    private var isAnimatingUpper = false
    private var isAnimatingController = false

    private var currentSong: Song? = null
    private var mAdapter: SongsAdapter? = null
    private var audioList: ArrayList<Song>? = null

    private lateinit var rotatingAlbumAnim: Animation
    private lateinit  var fadeInAnimationUpper: Animation
    private lateinit  var fadeOutAnimationUpper: Animation
    private lateinit  var fadeInAnimationController: Animation
    private lateinit  var fadeOutAnimationController: Animation

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val v = inflater.inflate(R.layout.fragment_songs_list, container, false)
        ButterKnife.bind(this, v)
        setUp(v)
        return v
    }

    private fun setUp(v: View) {
        this.v = v
        var themeName: String? = null
        if (activity != null)
            themeName = AppPreferencesHelper(activity!!).userThemeName

        val currentUserThemeColors = AppConstants.themeMap[themeName]
        refreshForUserThemeColors(currentUserThemeColors, themeName)

        fadeOutAnimationUpper = AnimationUtils.loadAnimation(activity, R.anim.fade_out)
        fadeInAnimationUpper = AnimationUtils.loadAnimation(activity, R.anim.fade_in)
        fadeInAnimationController = AnimationUtils.loadAnimation(activity, R.anim.fade_in_controller)
        fadeOutAnimationController = AnimationUtils.loadAnimation(activity, R.anim.fade_out_controller)

        rotatingAlbumAnim = AnimationUtils.loadAnimation(activity, R.anim.rotate)

        v.current_song_title.isSelected = true
        v.current_song_artist.isSelected = true

        v.recycler_view.layoutManager = LinearLayoutManager(activity)

        v.recycler_view.setOnFastScrollStateChangeListener(object : OnFastScrollStateChangeListener {
            override fun onFastScrollStart() {
                hideOnFastScroll = true
                if (v.hide_or_show_upper.visibility == View.VISIBLE && !isAnimatingUpper)
                    fadeOutUpper()
                if (v.bottom_controller.visibility == View.VISIBLE && !isAnimatingController)
                    fadeOutController()
            }

            override fun onFastScrollStop() {
                hideOnFastScroll = false
            }
        })

        v.recycler_view.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (!hideOnFastScroll) {
                    if (dy > 0) {      //scrolled up
                        if (v.hide_or_show_upper.visibility == View.VISIBLE && !isAnimatingUpper)
                            fadeOutUpper()
                        if (v.bottom_controller.visibility == View.VISIBLE && !isAnimatingController)
                            fadeOutController()
                    } else {          //scrolled down
                        if (v.hide_or_show_upper.visibility == View.GONE && !isAnimatingUpper)
                            fadeInUpper()
                        if (v.bottom_controller.visibility == View.GONE && !isAnimatingController && currentSong != null)
                            fadeInController()
                    }
                }
            }
        })

        setClickListeners()
    }

    private fun setClickListeners() {
        v.app_name.setOnClickListener { setAppName() }
        v.play_pause_song.setOnClickListener { playOrPausePlayer() }
        v.sort_options.setOnClickListener { setSortOptions() }
        v.play_next_song.setOnClickListener { playNextSong() }
        v.play_previous_song.setOnClickListener { playPreviousSong() }
        v.launch_play_frag_one.setOnClickListener { setSongPlayFragment() }
        v.launch_play_frag_two.setOnClickListener { setSongPlayFragment() }
        v.app_menu_options.setOnClickListener { setAppMenuOptions() }
        v.rescan_device.setOnClickListener { setRescanDevice() }
    }

    fun playOrPausePlayer() {
        activity?.let {
            if ((it as MainActivity).isPlaying) pausePlayer(FROM_FRAGMENT)
            else playPlayer(FROM_FRAGMENT)
        }
    }

    fun setAppName() {}

    private fun getHexColor(color: Int): Int { return context?.let { ContextCompat.getColor(it, color) } ?: 0 }

    private fun startThemeChangeActivity() { activity?.let { (it as MainActivity).startThemeChangeActivity() } }

    private fun setSongPlayFragment() { activity?.let { (it as MainActivity).setSongPlayFragment() } }

    fun setSortOptions() { activity?.let { (it as MainActivity).setSongsSortFragment() } }

    fun getControllerVisibility(): Int { return v.bottom_controller.visibility }

    fun playNextSong() { activity?.let { (it as MainActivity).playNextSong() } }

    fun setAppMenuOptions() { startThemeChangeActivity() }

    fun playPreviousSong() {
        activity?.let { (it as MainActivity).playNextSong() }
        if (activity != null) (activity as MainActivity).playPreviousSong()
    }

    fun setRescanDevice() {
        v.error_layout.visibility = View.GONE
        fetchSongs(FROM_FRAGMENT)
    }

    fun refreshForUserThemeColors(userThemeColors: ThemeColors?, themeName: String?) {

        userThemeColors?.let {
            v.hide_or_show_upper.setBackgroundColor(getHexColor(it.colorPrimaryDark))
            v.recycler_view.setPopupBgColor(getHexColor(it.colorPrimaryDark))
            v.launch_play_frag_two.setBackgroundColor(getHexColor(it.colorPrimaryDark))

            if (themeName == PITCH_BLACK || themeName == BLUE_GREY) {
                v.bottom_line.setBackgroundColor(getHexColor(R.color.readBlack))
                v.top_line.setBackgroundColor(getHexColor(R.color.readBlack))
            } else {
                v.bottom_line.setBackgroundColor(getHexColor(it.colorPrimary))
                v.top_line.setBackgroundColor(getHexColor(it.colorPrimary))
            }
        }
    }

    fun refreshSongListFragmentForSongDelete(index: Int) {
        mAdapter?.let {
            it.notifyItemRemoved(index)
            it.notifyItemRangeChanged(index, mAdapter!!.itemCount)
            it.removeListItem(index)

            if (it.itemCount <= TOP_EMPTY_CELLS_COUNT + BOTTOM_EMPTY_CELLS_COUNT) showErrorLayout()
            else hideErrorLayout()
        }
    }

    fun fetchSongs(from: Int) {
        LAUNCHED_FROM = from
        if (activity != null) {
            activity!!.supportLoaderManager.initLoader(LOADER_ID, null, this)
        }
    }

    override fun onResume() {
        super.onResume()
        if (audioList == null && LAUNCHED_FROM != FROM_ACTIVITY) {
            fetchSongs(FROM_FRAGMENT)
        } else if (activity != null) {
            if (activity!!.intent.getBooleanExtra(LAUNCHED_FROM_NOTIFICATION, false)) {
                fetchSongs(FROM_FRAGMENT)
            }
        }
    }

    override fun onCreateLoader(id: Int, args: Bundle?): Loader<Cursor> {
        val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        return CursorLoader(activity!!, uri, null, null, null, null)
    }

    override fun onLoadFinished(loader: Loader<Cursor>, cursor: Cursor?) {

        val list = ArrayList<Song>()

        cursor?.let {
            if(cursor.count > 0)
                while (cursor.moveToNext()) {
                    list.add(Song(
                            returnCursorElement(it, MediaStore.Audio.Media.DATA),
                            returnCursorElement(it, MediaStore.Audio.Media.TITLE),
                            returnCursorElement(it, MediaStore.Audio.Media.TITLE_KEY),
                            returnCursorElement(it, MediaStore.Audio.Media._ID),
                            returnCursorElement(it, MediaStore.Audio.Media.DATE_ADDED),
                            returnCursorElement(it, MediaStore.Audio.Media.DATE_MODIFIED),
                            returnCursorElement(it, MediaStore.Audio.Media.DURATION),
                            returnCursorElement(it, MediaStore.Audio.Media.COMPOSER),
                            returnCursorElement(it, MediaStore.Audio.Media.ALBUM),
                            returnCursorElement(it, MediaStore.Audio.Media.ALBUM_ID),
                            returnCursorElement(it, MediaStore.Audio.Media.ALBUM_KEY),
                            returnCursorElement(it, MediaStore.Audio.Media.ARTIST),
                            returnCursorElement(it, MediaStore.Audio.Media.ARTIST_ID),
                            returnCursorElement(it, MediaStore.Audio.Media.ARTIST_KEY),
                            returnCursorElement(it, MediaStore.Audio.Media.SIZE),
                            returnCursorElement(it, MediaStore.Audio.Media.YEAR)
                    ))
                }
        }

        val activity = activity as MainActivity?
        activity?.putSongsListIntoDatabase(list)

        if (LAUNCHED_FROM == FROM_ACTIVITY) {
            Toast.makeText(activity, R.string.rescanned, Toast.LENGTH_SHORT).show()
            LAUNCHED_FROM = FROM_FRAGMENT
        }
        activity?.supportLoaderManager?.destroyLoader(LOADER_ID)
    }

    fun renderRecyclerViewForAudioList(list: List<Song>) {

        val activity = activity as MainActivity?
        activity?.removeObservers()

        audioList = null
        audioList = ArrayList()

        audioList?.let {
            it.add(0, Song())
            it.addAll(list)
            for (i in 0 until BOTTOM_EMPTY_CELLS_COUNT) it.add(Song())
        }

        mAdapter = SongsAdapter(audioList, getActivity())
        v.recycler_view.adapter = mAdapter
    }

    override fun onLoaderReset(loader: Loader<Cursor>) {}

    private fun returnCursorElement(cursor: Cursor?, string: String?): String {
        cursor?.let { c ->
            string?.let { s ->
                c.getString(c.getColumnIndex(s))?.let {
                    return it
                }
            }
        } ?: return ""
    }

    fun setCurrentSong(song: Song) {

        currentSong = song

        if (currentSong == null)
            return

        if (v.bottom_controller.visibility == View.GONE)
            if (v.hide_or_show_upper.visibility == View.VISIBLE)
            //either both should be visible or both shouldn't
                v.bottom_controller.visibility = View.VISIBLE

        v.current_song_title.text = song.title

        val duration = java.lang.Long.valueOf(song.duration) / 1000
        v.current_song_artist.text = song.artist
        v.current_song_duration.text = ApplicationUtils().formatStringOutOfSeconds(duration.toInt())

        var path: String? = null
        if (activity != null) {

            //get path for the album art for this song
            val cursor = activity!!.contentResolver.query(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
                    arrayOf(MediaStore.Audio.Albums._ID, MediaStore.Audio.Albums.ALBUM_ART),
                    MediaStore.Audio.Albums._ID + "=?",
                    arrayOf(song.albumId.toString()), null)
            if (cursor != null && cursor.moveToFirst()) {
                path = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Albums.ALBUM_ART))
                // do whatever you need to do
                cursor.close()
            }

            //set album art
            Glide.with(activity!!)
                    .load(path)
                    .apply(RequestOptions
                            .centerCropTransform()
                            .placeholder(R.drawable.icon_drawable)
                    )
                    .into(v.current_song_album_art)

        }

        //start rotating animation
        if (v.bottom_controller.visibility == View.GONE) {
            v.hide_or_show_upper.visibility = View.VISIBLE
            v.bottom_controller.visibility = View.VISIBLE
        }
        playPlayer(FROM_ACTIVITY)
    }

    fun playPlayer(from: Int) {
        activity?.let {
            if (from == FROM_FRAGMENT) (it as MainActivity).start()
            v.play_pause_song.setImageDrawable(it.getDrawable(R.drawable.ic_pause_black_24dp))
        }
        resetAlbumArtAnimation()
        v.rotate_view_album_art.startAnimation(rotatingAlbumAnim)
    }

    fun pausePlayer(from: Int) {
        activity?.let {
            if (from == FROM_FRAGMENT) (it as MainActivity).pause()
            v.play_pause_song.setImageDrawable(it.getDrawable(R.drawable.ic_play_arrow_black_24dp))
        }
        resetAlbumArtAnimation()
    }

    private fun resetAlbumArtAnimation() {
        v.rotate_view_album_art.animation?.let {
            v.rotate_view_album_art.animation.cancel()
            v.rotate_view_album_art.animation = null
        }
    }

    fun fadeOutUpper() {
        val listener = object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation) {
                isAnimatingUpper = true
            }

            override fun onAnimationEnd(animation: Animation) {
                v.hide_or_show_upper.visibility = View.GONE
                isAnimatingUpper = false
            }

            override fun onAnimationRepeat(animation: Animation) {}
        }

        fadeOutAnimationUpper.setAnimationListener(listener)
        v.hide_or_show_upper.startAnimation(fadeOutAnimationUpper)
    }

    fun fadeInUpper() {
        val listener = object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation) {
                isAnimatingUpper = true
            }

            override fun onAnimationEnd(animation: Animation) {
                v.hide_or_show_upper.visibility = View.VISIBLE
                isAnimatingUpper = false
            }

            override fun onAnimationRepeat(animation: Animation) {}
        }
        fadeInAnimationUpper.setAnimationListener(listener)
        v.hide_or_show_upper.startAnimation(fadeInAnimationUpper)
    }

    fun fadeOutController() {
        val listener = object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation) {
                isAnimatingController = true
            }

            override fun onAnimationEnd(animation: Animation) {
                v.bottom_controller.visibility = View.GONE
                isAnimatingController = false
            }

            override fun onAnimationRepeat(animation: Animation) {}
        }
        fadeOutAnimationController.setAnimationListener(listener)
        v.bottom_controller.startAnimation(fadeOutAnimationController)
    }

    fun fadeInController() {
        val listener = object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation) {
                isAnimatingController = true
            }

            override fun onAnimationEnd(animation: Animation) {
                v.bottom_controller.visibility = View.VISIBLE
                isAnimatingController = false
            }

            override fun onAnimationRepeat(animation: Animation) {}
        }
        fadeInAnimationController.setAnimationListener(listener)
        v.bottom_controller.startAnimation(fadeInAnimationController)
    }

    fun showErrorLayout() {
        v.recycler_view.visibility = View.GONE
        v.error_layout.visibility = View.VISIBLE
        Spruce.SpruceBuilder(v.error_layout)
                .sortWith(DefaultSort(30L))
                .animateWith(DefaultAnimations.shrinkAnimator(v.error_layout, 200))
                .start()
    }

    fun hideErrorLayout() {
        v.recycler_view.visibility = View.VISIBLE
        v.error_layout.visibility = View.GONE
    }
}