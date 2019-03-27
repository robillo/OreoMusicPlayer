package com.robillo.dancingplayer.views.activities.main.song_play_frag

import android.annotation.SuppressLint
import android.app.Dialog
import android.database.Cursor
import android.graphics.Color
import android.os.Bundle
import android.os.CountDownTimer
import android.provider.MediaStore
import android.support.design.widget.BottomSheetBehavior
import android.support.design.widget.BottomSheetDialogFragment
import android.support.design.widget.CoordinatorLayout
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v7.widget.CardView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView

import com.bumptech.glide.Glide
import com.robillo.dancingplayer.R
import com.robillo.dancingplayer.models.Song
import com.robillo.dancingplayer.models.ThemeColors
import com.robillo.dancingplayer.preferences.PreferencesHelper
import com.robillo.dancingplayer.utils.AppConstants
import com.robillo.dancingplayer.utils.ApplicationUtils
import com.robillo.dancingplayer.views.activities.home.HomeActivity

import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick

import com.robillo.dancingplayer.utils.AppConstants.FROM_ACTIVITY
import com.robillo.dancingplayer.utils.AppConstants.FROM_FRAGMENT
import kotlinx.android.synthetic.main.fragment_song_play_fragment_sheet.view.*

/**
 * A simple [Fragment] subclass.
 */
class SongPlayFragmentSheet : BottomSheetDialogFragment() {

    private lateinit var v: View
    private var helper: PreferencesHelper? = null
    private var currentUserThemeColors: ThemeColors? = null
    private var rotatingAlbumAnim: Animation? = null
    private var currentDurationProgress: Int = 0
    private var totalDurationProgress: Int = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val v = inflater.inflate(R.layout.fragment_song_play_fragment_sheet, container, false)
        setUp(v)
        return v
    }

    override fun onStart() {
        super.onStart()
        val dialog = dialog

        if (dialog != null) {
            val bottomSheet = dialog.findViewById<View>(R.id.design_bottom_sheet)
            bottomSheet.layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
        }
        val view = view!!
        view.post {
            val parent = view.parent as View
            val params = parent.layoutParams as CoordinatorLayout.LayoutParams
            val behavior = params.behavior
            val bottomSheetBehavior = behavior as BottomSheetBehavior<*>?
            if (bottomSheetBehavior != null) bottomSheetBehavior.peekHeight = view.measuredHeight
            parent.setBackgroundColor(Color.TRANSPARENT)
        }
    }

    fun setUp(v: View) {

        this.v = v
        helper = PreferencesHelper(activity!!)
        currentUserThemeColors = AppConstants.themeMap[helper!!.userThemeName]
        refreshForUserThemeColors(currentUserThemeColors)
        rotatingAlbumAnim = AnimationUtils.loadAnimation(activity, R.anim.rotate)

        setPreferencesToViews()

        //      //set duration
        currentDurationProgress = (activity as HomeActivity).getCurrentSongDuration() / 1000
        totalDurationProgress = (activity as HomeActivity).duration / 1000

        setDurationValues(currentDurationProgress, totalDurationProgress)

        setSeekBarOnSeekChangeListener()

        //marqueue
        v.current_song_title!!.isSelected = true
        v.current_song_artist!!.isSelected = true
        v.album_name!!.isSelected = true

        if (activity != null) {
            currentSong = (activity as HomeActivity).getCurrentSong()
            setCurrentSong(currentSong)
        }

        setClickListeners(v)
    }

    private fun setClickListeners(v: View) {
        v.back_to_song_list!!.setOnClickListener { view -> setBackToSongList() }
        v.back_ten_seconds!!.setOnClickListener { view -> setTenSecondsBack() }
        v.forward_ten_seconds!!.setOnClickListener { view -> setTenSecondsForward() }
        v.repeat_song!!.setOnClickListener { view -> toggleRepeatSong() }
        v.shuffle_songs!!.setOnClickListener { view -> toggleShuffleSongs() }
        v.play_previous_song!!.setOnClickListener { view -> playPreviousSong() }
        v.play_next_song!!.setOnClickListener { view -> playNextSong() }
        v.play_pause_song!!.setOnClickListener { view -> playOrPausePlayer() }
    }

    fun refreshForUserThemeColors(currentUserThemeColors: ThemeColors?) {
        v.bottom_controller!!.setBackgroundColor(resources.getColor(currentUserThemeColors!!.colorPrimaryDark))
    }

    fun setCurrentSong(song: Song?) {

        currentSong = song

        if (song != null) {
            v.current_song_title!!.text = song.title
            v.album_name!!.text = song.album

            v.current_song_artist!!.text = song.artist

            v.current_song_max_progress!!.text = ApplicationUtils().formatStringOutOfSeconds(Integer.valueOf(song.duration) / 1000)

            v.current_song_current_progress!!.text = ApplicationUtils().formatStringOutOfSeconds(currentDurationProgress)

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
                if (path != null && path != "null") {
                    Glide.with(activity!!).load(path).into(v.current_song_album_art!!)
                } else {
                    Glide.with(activity!!).load(R.drawable.circle_placeholder).into(v.current_song_album_art!!)
                }
            }

            if (activity != null) {                                                   //play or pause
                if ((activity as HomeActivity).isPlaying)
                    playPlayer(FROM_ACTIVITY)
                else
                    pausePlayer(FROM_ACTIVITY)
            }
        }
    }

    fun resetAlbumArtAnimation() {
        v.rotate_view_album_art!!.clearAnimation()
    }

    fun playPlayer(from: Int) {
        assert(activity != null)
        if (from == FROM_FRAGMENT) (activity as HomeActivity).start()

        startTimerForProgress()
        v.play_pause_song!!.setImageDrawable(activity!!.getDrawable(R.drawable.ic_pause_circle_filled_black_24dp))
        resetAlbumArtAnimation()
        v.rotate_view_album_art!!.startAnimation(rotatingAlbumAnim)

        helper!!.setIsSongPlaying(true)
    }

    fun pausePlayer(from: Int) {
        assert(activity != null)
        if (from == FROM_FRAGMENT) (activity as HomeActivity).pause()

        stopTimerForProgress()
        v.play_pause_song!!.setImageDrawable(activity!!.getDrawable(R.drawable.ic_play_circle_filled_black_24dp))

        resetAlbumArtAnimation()

        helper!!.setIsSongPlaying(false)
    }

    fun setPreferencesToViews() {
        if (activity != null) {

            when (helper!!.isRepeatModeOn) {
                AppConstants.REPEAT_MODE_VALUE_LINEARLY_TRAVERSE_ONCE -> {
                    //change tint to linear traverse => "unticked grey" and drawable to loop
                    v.repeat_song!!
                            .setImageDrawable(
                                    resources.getDrawable(R.drawable.ic_repeat_black_24dp)
                            )
                    v.repeat_song!!
                            .setColorFilter(
                                    ContextCompat.getColor(activity!!, R.color.colorTextOne)
                            )
                }
                AppConstants.REPEAT_MODE_VALUE_LOOP -> {
                    //change tint to loop list => "ticked primary" and drawable to loop
                    v.repeat_song!!
                            .setImageDrawable(
                                    resources.getDrawable(R.drawable.ic_repeat_black_24dp)
                            )
                    v.repeat_song!!
                            .setColorFilter(
                                    ContextCompat.getColor(activity!!, currentUserThemeColors!!.colorPrimary)
                            )
                }
                AppConstants.REPEAT_MODE_VALUE_REPEAT_SAME_SONG -> {
                    //change tint to repeat => "ticked primary" and drawable to repeat one
                    v.repeat_song!!
                            .setImageDrawable(
                                    resources.getDrawable(R.drawable.ic_repeat_one_black_24dp)
                            )
                    v.repeat_song!!
                            .setColorFilter(
                                    ContextCompat.getColor(activity!!, currentUserThemeColors!!.colorPrimary)
                            )
                }
            }


            if (helper!!.isShuffleModeOn)
            //change tint to repeat => "repeat"
                v.shuffle_songs!!
                        .setColorFilter(
                                ContextCompat.getColor(activity!!, currentUserThemeColors!!.colorPrimary)
                        )
            else
            //change tint to repeat => "non repeat"
                v.shuffle_songs!!
                        .setColorFilter(
                                ContextCompat.getColor(activity!!, R.color.colorTextOne)
                        )
        }
    }

    fun seekTenSecondsForward() {
        if (activity != null) (activity as HomeActivity).seekTenSecondsForward()
    }

    fun seekTenSecondsBackwards() {
        if (activity != null) (activity as HomeActivity).seekTenSecondsBackwards()
    }

    fun playOrPausePlayer() {
        if (activity != null) {
            if ((activity as HomeActivity).isPlaying) {
                pausePlayer(FROM_FRAGMENT)
            } else {
                playPlayer(FROM_FRAGMENT)
            }
        }
    }

    fun playNextSong() {
        if (activity != null) (activity as HomeActivity).playNextSong()
    }

    fun playPreviousSong() {
        if (activity != null) (activity as HomeActivity).playPreviousSong()
    }

    internal fun toggleShuffleSongs() {
        if (activity != null) {

            val helper = PreferencesHelper(activity!!)

            if (helper.isShuffleModeOn) {
                helper.setIsShuffleModeOn(false)

                //change tint to normal => "non repeat"
                v.shuffle_songs!!
                        .setColorFilter(
                                ContextCompat.getColor(activity!!, R.color.colorTextOne)
                        )
            } else {
                helper.setIsShuffleModeOn(true)

                //change tint to repeat => "repeat"
                v.shuffle_songs!!
                        .setColorFilter(
                                ContextCompat.getColor(activity!!, currentUserThemeColors!!.colorPrimary)
                        )
            }

            (activity as HomeActivity).toggleShuffleModeInService()
        }
    }

    internal fun toggleRepeatSong() {
        if (activity != null) {

            val helper = PreferencesHelper(activity!!)

            when (helper.isRepeatModeOn) {
                AppConstants.REPEAT_MODE_VALUE_LINEARLY_TRAVERSE_ONCE -> {

                    helper.setIsRepeatModeOn(AppConstants.REPEAT_MODE_VALUE_LOOP)
                    v.repeat_song!!
                            .setImageDrawable(
                                    resources.getDrawable(R.drawable.ic_repeat_black_24dp)
                            )
                    v.repeat_song!!
                            .setColorFilter(
                                    ContextCompat.getColor(activity!!, currentUserThemeColors!!.colorPrimary)
                            )
                }
                AppConstants.REPEAT_MODE_VALUE_LOOP -> {

                    helper.setIsRepeatModeOn(AppConstants.REPEAT_MODE_VALUE_REPEAT_SAME_SONG)
                    v.repeat_song!!
                            .setImageDrawable(
                                    resources.getDrawable(R.drawable.ic_repeat_one_black_24dp)
                            )
                    v.repeat_song!!
                            .setColorFilter(
                                    ContextCompat.getColor(activity!!, currentUserThemeColors!!.colorPrimary)
                            )
                }
                AppConstants.REPEAT_MODE_VALUE_REPEAT_SAME_SONG -> {

                    helper.setIsRepeatModeOn(AppConstants.REPEAT_MODE_VALUE_LINEARLY_TRAVERSE_ONCE)
                    v.repeat_song!!
                            .setImageDrawable(
                                    resources.getDrawable(R.drawable.ic_repeat_black_24dp)
                            )
                    v.repeat_song!!
                            .setColorFilter(
                                    ContextCompat.getColor(activity!!, R.color.colorTextOne)
                            )
                }
            }

            (activity as HomeActivity).toggleRepeatModeInService(helper.isRepeatModeOn)
        }
    }

    internal fun setTenSecondsForward() {
        currentDurationProgress += 10
        setProgressToSeekBar(currentDurationProgress, totalDurationProgress)
        seekTenSecondsForward()
    }

    internal fun setTenSecondsBack() {
        currentDurationProgress -= 10
        setProgressToSeekBar(currentDurationProgress, totalDurationProgress)
        seekTenSecondsBackwards()
    }

    internal fun setBackToSongList() {
        val activity = activity as HomeActivity?
        activity?.hideSongPlayFragment(this)
    }

    fun setProgressToSeekBar(currentDuration: Int, totalDuration: Int) {
        if (totalDuration != 0) {
            val percentProgress = currentDuration * 100 / totalDuration
            v.current_song_progress_seekbar!!.progress = percentProgress

            if (currentDuration <= totalDuration)
                v.current_song_current_progress!!.text = ApplicationUtils().formatStringOutOfSeconds(currentDuration)
        } else {
            v.current_song_progress_seekbar!!.progress = 0
        }
    }

    fun setDurationValues(currentDuration: Int, totalDuration: Int) {
        currentDurationProgress = currentDuration
        totalDurationProgress = totalDuration
        setProgressToSeekBar(currentDuration, totalDuration)

        if (helper!!.isSongPlaying) startTimerForProgress()
    }

    fun startTimerForProgress() {
        updateTimer((totalDurationProgress - currentDurationProgress) * 1000, 1000)
        timer!!.start()
    }

    fun stopTimerForProgress() {
        if (timer != null) {
            timer!!.cancel()
            timer = null
        }
    }

    fun updateTimer(millisInFuture: Int, countDownInterval: Int) {
        if (timer != null) {
            timer!!.cancel()
            timer = null
        }

        timer = object : CountDownTimer(millisInFuture.toLong(), countDownInterval.toLong()) {
            override fun onTick(millisUntilFinished: Long) {
                currentDurationProgress += 1
                setProgressToSeekBar(currentDurationProgress, totalDurationProgress)
            }

            override fun onFinish() {
                timer!!.cancel()
                timer = null
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (activity != null) currentDurationProgress = (activity as HomeActivity).getCurrentSongDuration() / 1000
        setProgressToSeekBar(currentDurationProgress, totalDurationProgress)
        if (helper!!.isSongPlaying) startTimerForProgress()
    }

    fun setSeekBarOnSeekChangeListener() {
        v.current_song_progress_seekbar!!.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, percentage: Int, isChangedByUser: Boolean) {

            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                currentDurationProgress = (seekBar.progress.toFloat() / 100.toFloat() * totalDurationProgress.toFloat()).toInt()
                if (activity != null) (activity as HomeActivity).seekTo(currentDurationProgress * 1000)
                updateTimer((totalDurationProgress - currentDurationProgress) * 1000, 1000)
                timer!!.start()
            }
        })
    }

    companion object {

        private var timer: CountDownTimer? = null
        private var currentSong: Song? = null
    }
}// Required empty public constructor
