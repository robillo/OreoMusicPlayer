package com.robillo.dancingplayer.views.activities.home

import android.app.Activity
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Observer
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.provider.MediaStore
import android.support.design.widget.BottomSheetBehavior
import android.support.design.widget.Snackbar
import android.support.v4.content.ContextCompat
import android.support.v7.widget.LinearLayoutManager
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.LinearLayout
import android.widget.MediaController
import android.widget.Toast
import com.robillo.dancingplayer.R
import com.robillo.dancingplayer.databases.AllSongsDatabase.SongDatabase
import com.robillo.dancingplayer.databases.AllSongsDatabase.model_and_dao_most_played.MostPlayedRepository
import com.robillo.dancingplayer.databases.AllSongsDatabase.model_and_dao_playlists.PlaylistRepository
import com.robillo.dancingplayer.databases.AllSongsDatabase.model_and_dao_songs.SongRepository
import com.robillo.dancingplayer.events.PlayerStateNoSongPlayingEvent
import com.robillo.dancingplayer.events.SongChangeEvent
import com.robillo.dancingplayer.events.ThemeChangeEvent
import com.robillo.dancingplayer.models.*
import com.robillo.dancingplayer.preferences.PreferencesHelper
import com.robillo.dancingplayer.services.MusicService
import com.robillo.dancingplayer.utils.AppConstants
import com.robillo.dancingplayer.utils.AppConstants.*
import com.robillo.dancingplayer.utils.ApplicationUtils
import com.robillo.dancingplayer.views.activities.main.EditDialogFragment.EditDialogFragment
import com.robillo.dancingplayer.views.activities.main.adapters.PlaylistAdapter
import com.robillo.dancingplayer.views.activities.main.bottom_sheet.BottomSheetFragment
import com.robillo.dancingplayer.views.activities.main.song_list_frag.SongsListFragment
import com.robillo.dancingplayer.views.activities.main.song_play_frag.SongPlayFragmentSheet
import com.robillo.dancingplayer.views.activities.main.songs_sort_frag.SongsSortFragment
import com.robillo.dancingplayer.views.activities.theme_change.ThemeChangeActivity
import io.github.inflationx.viewpump.ViewPumpContextWrapper
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.playlist_bottom_sheet.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.io.File
import java.util.*

class HomeActivity : AppCompatActivity(), MediaController.MediaPlayerControl {

    //TODO: inject later
    private lateinit var songDatabase: SongDatabase

    //generic stuff
    val REQUEST_CODE = 101

    private lateinit var playlistSheetBehavior: BottomSheetBehavior<*>
    private lateinit var bottomSheetFragment: BottomSheetFragment
    private lateinit var musicService: MusicService
    private lateinit var playIntent: Intent
    private var musicBound = false
    private var playlistRowItems: MutableList<PlaylistRowItem> = ArrayList()
    private lateinit var currentUserThemeColors: ThemeColors
    private lateinit var helper: PreferencesHelper
    private lateinit var songRepository: SongRepository
    private lateinit var playlistRepository: PlaylistRepository
    private lateinit var mostPlayedRepository: MostPlayedRepository
    private lateinit var listLiveData: LiveData<List<Song>>
    private lateinit var currentSong: Song
    private lateinit var selectedPlayList: PlaylistRowItem

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        setup()
    }

    private fun setup() {
        songDatabase = SongDatabase.getInstance(this)
        helper = PreferencesHelper(this)

        selectedPlayList = ApplicationUtils()
                .convertStringToPlaylistRowItem(helper.currentPlaylistTitle)

        songRepository = getSongRepository()
        songRepository.deleteAllSongs()

        refreshForUserThemeColors()
        setSongListFragment()
        setPlaylistBottomSheet()
        setClickListeners()
    }

    private fun setClickListeners() {
        create_new_playlist.setOnClickListener {
            showEditCreateDialogFragment(CREATE_NEW_PLAYLIST, -1, "")
        }
        layout_lift_burrow_playlists.setOnClickListener {
            togglePlaylistBottomSheet()
        }
    }

    fun getSongRepository(): SongRepository {
        if (!::songDatabase.isInitialized)
            songDatabase = SongDatabase.getInstance(this)

        return SongRepository(songDatabase.songDao)
    }

    private fun refreshForUserThemeColors() {

        helper = PreferencesHelper(this)
        currentUserThemeColors = AppConstants.themeMap[helper.userThemeName]!!

        val window = window
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = ContextCompat.getColor(this, currentUserThemeColors.colorPrimaryDark)
    }

    private fun setSongListFragment() {
        val fragment = SongsListFragment()

        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(fragment_container.id, fragment, getString(R.string.songs_list))
        transaction.commitAllowingStateLoss()

        fragment.fetchSongsAsync(FROM_FRAGMENT)
    }

    private fun setPlaylistBottomSheet() {
        loadPlaylistItems(FIRST_LOAD, null)
        setBehaviorCallbacks()
        setCurrentPlaylistAsHeader()
        loadPlaylistsIntoRecyclerView(FROM_BOTTOM_CONTROLLER, null)
    }

    private fun loadPlaylistsIntoRecyclerView(from: Int, songId: String?) {
        loadPlaylistItems(from, songId)
    }

    private fun loadPlaylistItems(from: Int, songId: String?) {
        if (!::playlistRepository.isInitialized)
            playlistRepository = getPlaylistRepository()

        val utils = ApplicationUtils()

        val liveData = playlistRepository.distinctPlaylistNames

        liveData.observe(this, Observer { strings ->

            playlistRowItems = ArrayList()
            playlistRowItems.add(utils.convertStringToPlaylistRowItem(AppConstants.DEFAULT_PLAYLIST_TITLE))
            playlistRowItems.add(utils.convertStringToPlaylistRowItem(AppConstants.RECENTLY_ADDED))
            playlistRowItems.add(utils.convertStringToPlaylistRowItem(AppConstants.RECENTLY_PLAYED))
            playlistRowItems.add(utils.convertStringToPlaylistRowItem(AppConstants.MOST_PLAYED))

            if (strings != null)
                for (s in strings)
                    playlistRowItems.add(utils.convertStringToPlaylistRowItem(s))

            songId?.let {
                loadSongsInRvAfterRowItemsLoaded(from, it)
            }
        })
    }

    fun loadSongsInRvAfterRowItemsLoaded(from: Int, songId: String) {
        val itemsToDisplay = ArrayList<PlaylistRowItem>()

        for (item in playlistRowItems) {
            if (item.title != selectedPlayList.title) {
                itemsToDisplay.add(item)
            }
        }

        val playlistAdapter = PlaylistAdapter(itemsToDisplay, this, from, songId)
        playlist_recycler_view.setLayoutManager(LinearLayoutManager(this))

        if (from == FIRST_LOAD) hidePlaylistBottomSheet()

        playlist_recycler_view.adapter = playlistAdapter
    }

    fun hidePlaylistBottomSheet() {
        if (::playlistSheetBehavior.isInitialized && playlistSheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED) {
            playlistSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            up_down_fragment_playlist.animate().rotation(0f).start()
        }
    }

    fun getPlaylistRepository(): PlaylistRepository {
        if (!::songDatabase.isInitialized)
            songDatabase = SongDatabase.getInstance(this)

        return PlaylistRepository(songDatabase.playlistDao)
    }

    fun setBehaviorCallbacks() {
        playlistSheetBehavior = BottomSheetBehavior.from<LinearLayout>(bottom_sheet)

        playlistSheetBehavior.setBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                when (newState) {
                    BottomSheetBehavior.STATE_EXPANDED -> {
                        up_down_fragment_playlist.animate().rotation(180f).start()
                    }
                    BottomSheetBehavior.STATE_COLLAPSED -> {
                        up_down_fragment_playlist.animate().rotation(0f).start()
                        create_new_playlist.setVisibility(View.VISIBLE)
                        loadPlaylistsIntoRecyclerView(FROM_BOTTOM_CONTROLLER, null)
                    }
                    BottomSheetBehavior.STATE_HIDDEN, BottomSheetBehavior.STATE_DRAGGING, BottomSheetBehavior.STATE_SETTLING -> {
                    }
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {}
        })
    }

    fun updatePlaylistListForSelectedItem(playlistRowItem: PlaylistRowItem, position: Int) {

        selectedPlayList = playlistRowItem
        helper.currentPlaylistTitle = selectedPlayList.getTitle()
        setCurrentPlaylistAsHeader()

        loadPlaylistsIntoRecyclerView(FIRST_LOAD, null)

        if (!::songRepository.isInitialized) songRepository = getSongRepository()

        loadSongsForSelectedPlaylistFromDb()
    }

    fun loadSongsForSelectedPlaylistFromDb() {

        val helper = PreferencesHelper(this)

        listLiveData = songRepository
                .getAllSongs(
                        selectedPlayList.getTitle(),
                        helper.sortOrderForSongs,
                        helper.recentlyAddedCount,
                        helper.recentlyPlayedCount,
                        helper.mostPlayedCount
                )

        //TODO: cast to mutable list may cause problems
        listLiveData.observe(this, Observer { songs ->
            if (songs != null && songs.size > 0) {
                updateRecyclerViewForLoadedPlaylist(songs, songs.size)
                startMusicServiceForCurrentPlaylist(songs as MutableList<Song>)
            } else {
                updateRecyclerViewForLoadedPlaylist(songs as MutableList<Song>, -1)
            }
        })
    }

    fun getMusicService(): MusicService? {
        if(::musicService.isInitialized)
            return musicService
        else
            return null
    }


    fun startServiceForAudioList(songList: ArrayList<Song>) {

        //connect to the service
        val musicConnection = object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName, service: IBinder) {
                val binder = service as MusicService.MusicBinder
                //get service
                musicService = binder.service
                //pass list
                musicService.setSongsList(songList)
                musicBound = true
            }

            override fun onServiceDisconnected(name: ComponentName) {
                musicBound = false
            }
        }
        if (!::playIntent.isInitialized) {
            playIntent = Intent(this, MusicService::class.java)
            bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE)
            startService(playIntent)
        }
    }

    private fun startMusicServiceForCurrentPlaylist(audioList: MutableList<Song>) {
        audioList.add(0, Song())
        audioList.add(Song())
        audioList.add(Song())

        getMusicService()?.let {
            updateServiceList(ArrayList(audioList))
        } ?: run {
            startServiceForAudioList(ArrayList(audioList))
        }
    }

    private fun updateServiceList(updatedAudioList: ArrayList<Song>) {
        musicService.setSongPosn(0)
        musicService.updateAudioList(updatedAudioList)
    }

    private fun updateRecyclerViewForLoadedPlaylist(audioList: List<Song>, size: Int) {
        val fragment = supportFragmentManager.findFragmentByTag(getString(R.string.songs_list)) as SongsListFragment?

        if (fragment != null) {
            fragment.renderRecyclerViewForAudioList(audioList)
            if (size < 0) {
                fragment.showErrorLayout()
            } else
                fragment.hideErrorLayout()
        }
    }

    fun showPlaylistBottomSheet(from: Int, songId: String) {
        if (from == FROM_BOTTOM_CONTROLLER) {
            if (::playlistSheetBehavior.isInitialized && playlistSheetBehavior.state == BottomSheetBehavior.STATE_COLLAPSED) {
                playlistSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
                up_down_fragment_playlist.animate().rotation(180f).start()
            }
        } else if (from == FROM_SONGS_LIST) {
            create_new_playlist.visibility = View.GONE
            if (::playlistSheetBehavior.isInitialized && playlistSheetBehavior.state == BottomSheetBehavior.STATE_COLLAPSED) {
                Handler().postDelayed({
                    playlistSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
                    up_down_fragment_playlist.animate().rotation(180f).start()
                }, 100)
            }
        }

        loadPlaylistsIntoRecyclerView(from, songId)
    }

    fun showEditCreateDialogFragment(from: Int, position: Int, oldPlaylistName: String) {
        val fragment = EditDialogFragment()
        val args = Bundle()
        args.putInt(FROM, from)
        args.putInt(POSITION, position)
        args.putString(OLD_PLAYLIST_NAME, oldPlaylistName)
        fragment.arguments = args
        fragment.show(fragmentManager, getString(R.string.edit_dialog_fragment))
    }

    fun handleCreateNewPlaylist(playlistName: String) {
        if (!::playlistRepository.isInitialized) playlistRepository = getPlaylistRepository()
        playlistRepository.insertPlaylistItem(Playlist(null, playlistName))

        loadPlaylistsIntoRecyclerView(MODIFY, null)

        Toast.makeText(this, getString(R.string.playlist_created), Toast.LENGTH_SHORT).show()
    }

    fun handleEditPlaylistName(newPlaylistName: String, position: Int, oldPlaylistName: String) {
        if (!::playlistRepository.isInitialized) playlistRepository = getPlaylistRepository()
        playlistRepository.changePlaylistName(oldPlaylistName, newPlaylistName)

        loadPlaylistsIntoRecyclerView(MODIFY, null)

        Toast.makeText(this, getString(R.string.playlist_modified), Toast.LENGTH_SHORT).show()
    }

    fun handleDeletePlaylist(playlistName: String) {
        deletePlaylistInDb(playlistName)

        loadPlaylistsIntoRecyclerView(MODIFY, null)

        Toast.makeText(this, getString(R.string.playlist_deleted), Toast.LENGTH_SHORT).show()
    }

    private fun deletePlaylistInDb(playlistName: String) {
        if(!::playlistRepository.isInitialized) playlistRepository = getPlaylistRepository()

        playlistRepository.deleteAllInstancesOfPlaylist(playlistName)
    }

    private fun setCurrentPlaylistAsHeader() {
        if (!::selectedPlayList.isInitialized)
            selectedPlayList = ApplicationUtils().convertStringToPlaylistRowItem(helper.currentPlaylistTitle)

        val animation = AnimationUtils.loadAnimation(this, R.anim.item_animation_fall_down)
        animation.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation) {
                selected_playlist.text = selectedPlayList.title
            }

            override fun onAnimationEnd(animation: Animation) {}

            override fun onAnimationRepeat(animation: Animation) {}
        })
        selected_playlist.startAnimation(animation)
    }

    fun togglePlaylistBottomSheet() {
        if (playlistSheetBehavior.state != BottomSheetBehavior.STATE_EXPANDED) {
            playlistSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
            up_down_fragment_playlist.animate().rotation(180f).start()
        } else {
            playlistSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            up_down_fragment_playlist.animate().rotation(0f).start()
        }
    }

    private fun changeSongIfSameAsCurrentlyPlaying(deleteSong: Song) {
        val currentSong = getMusicService()?.getSong()
        if (currentSong != null && currentSong.id == deleteSong.id) {
            getMusicService()?.cancelNotification()
            removeSongFromListInMusicService(deleteSong)
            playNextSong()
        } else
            removeSongFromListInMusicService(deleteSong)
    }

    fun getMostPlayedRepository(): MostPlayedRepository {
        if (!::songDatabase.isInitialized) songDatabase = SongDatabase.getInstance(this)

        return MostPlayedRepository(songDatabase.mostPlayedDao)
    }

    fun hideSongPlayFragment(fragmentSheet: SongPlayFragmentSheet?) {
        if (fragmentSheet != null && fragmentSheet.isAdded && fragmentSheet.isVisible) fragmentSheet.dismiss()
    }

    fun setSongPlayFragment() {

        val fragmentSheet = supportFragmentManager.findFragmentByTag(getString(R.string.song_play)) as SongPlayFragmentSheet?

        if (fragmentSheet == null)
            SongPlayFragmentSheet().show(supportFragmentManager, getString(R.string.song_play))
        else if (!fragmentSheet.isAdded && fragmentSheet.isHidden) fragmentSheet.show(supportFragmentManager, getString(R.string.song_play))
    }

    fun setSongsSortFragment() {
        if (supportFragmentManager.findFragmentByTag(getString(R.string.songs_sort)) == null) {

            val transaction = supportFragmentManager.beginTransaction()
            transaction.setCustomAnimations(R.anim.songs_sort_fade_in, 0, 0, 0)
            transaction.add(fragment_container.id, SongsSortFragment(), getString(R.string.songs_sort))
            transaction.addToBackStack(getString(R.string.songs_sort))
            transaction.commit()
        }
    }

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase))
    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onResume() {
        super.onResume()

        val fragment = supportFragmentManager.findFragmentByTag(getString(R.string.songs_list)) as SongsListFragment?

        if (fragment != null) {
            if (::currentSong.isInitialized) {
                fragment.setCurrentSong(currentSong)

                if (PreferencesHelper(this).isPlayEvent)
                    fragment.playPlayer(FROM_ACTIVITY)
                else
                    fragment.pausePlayer(FROM_ACTIVITY)
            }
        }
    }

    public override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }

    override fun onBackPressed() {
        val songPlayFragment = supportFragmentManager.findFragmentByTag(getString(R.string.song_play))
        val songsSortFragment = supportFragmentManager.findFragmentByTag(getString(R.string.songs_sort))

        if (songPlayFragment != null) {                                  //remove song play fragment from back stack
            super.onBackPressed()
        } else if (songsSortFragment != null) {
            super.onBackPressed()
        } else if (playlistSheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED) {
            hidePlaylistBottomSheet()
        } else {                                                          //don't remove song list fragment from activity
            val homeIntent = Intent(Intent.ACTION_MAIN)
            homeIntent.addCategory(Intent.CATEGORY_HOME)
            homeIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(homeIntent)
        }
    }

    override fun onDestroy() {
        if (::musicService.isInitialized) musicService.onDestroy()
        if (::playIntent.isInitialized) stopService(playIntent)
        super.onDestroy()
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: ThemeChangeEvent) {

        val songListFragment = supportFragmentManager.findFragmentByTag(getString(R.string.songs_list)) as SongsListFragment?

        if (songListFragment != null) {
            refreshForUserThemeColors()
            songListFragment.applyUserTheme(
                    currentUserThemeColors,
                    PreferencesHelper(this).userThemeName
            )
        }
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: SongChangeEvent) {

        if (event.song != null) {

            if (!::mostPlayedRepository.isInitialized) mostPlayedRepository = getMostPlayedRepository()
            mostPlayedRepository.checkIfExistsAndInsertMostPlayed(this, event.song.id)

            currentSong = event.song

            val songListFragment = supportFragmentManager.findFragmentByTag(getString(R.string.songs_list)) as SongsListFragment?

            if (songListFragment != null) {
                songListFragment.setCurrentSong(currentSong)

                val helper = PreferencesHelper(this)

                if (helper.isPlayEvent)
                    songListFragment.playPlayer(FROM_ACTIVITY)
                else
                    songListFragment.pausePlayer(FROM_ACTIVITY)
            }

            val songPlayFragment = supportFragmentManager.findFragmentByTag(getString(R.string.song_play)) as SongPlayFragmentSheet?

            if (songPlayFragment != null) {
                songPlayFragment.setCurrentSong(currentSong)

                val helper = PreferencesHelper(this)

                if (helper.isPlayEvent)
                    songPlayFragment.playPlayer(FROM_ACTIVITY)
                else
                    songPlayFragment.pausePlayer(FROM_ACTIVITY)
            }
        }
        EventBus.getDefault().removeStickyEvent(event)
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: SetSeekBarEvent) {

        val fragment = supportFragmentManager.findFragmentByTag(getString(R.string.song_play)) as SongPlayFragmentSheet?

        fragment?.setDurationValues(event.currentDuration, event.totalDuration / 1000)

        EventBus.getDefault().removeStickyEvent(event)
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: PlayerStateNoSongPlayingEvent) {

        if (::currentSong.isInitialized) {

            val manager = supportFragmentManager

            val playFragment = manager.findFragmentByTag(getString(R.string.song_play)) as SongPlayFragmentSheet?
            val listFragment = manager.findFragmentByTag(getString(R.string.songs_list)) as SongsListFragment?
            if (playFragment != null && playFragment.isVisible) playFragment.dismiss()

            if (listFragment != null) {
                //TODO: make current song null?
                //TODO: currentSong = null
                listFragment.setCurrentSong(null)
                if (listFragment.getControllerVisibility() == View.VISIBLE) listFragment.fadeOutController()

                val toast = Toast.makeText(this, getString(R.string.end_of_list), Toast.LENGTH_SHORT)
                toast.setGravity(Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL, 0, 10)
                toast.show()
            }
        }
        EventBus.getDefault().removeStickyEvent(event)
    }

    fun toggleRepeatModeInService(value: String) {
        if (::musicService.isInitialized)
            musicService.toggleRepeatMode(value)
    }

    fun toggleShuffleModeInService() {
        if (::musicService.isInitialized)
            musicService.toggleShuffleMode()
    }

    fun seekTenSecondsForward() {
        if (::musicService.isInitialized)
            musicService.seekTenSecondsForward()
    }

    fun seekTenSecondsBackwards() {
        if (::musicService.isInitialized)
            musicService.seekTenSecondsBackwards()
    }

    fun repopulateListSongsListFragment() {
        val fragment = supportFragmentManager.findFragmentByTag(getString(R.string.songs_list)) as SongsListFragment?

        fragment?.fetchSongsAsync(FROM_FRAGMENT)
    }

    fun removeSongFromListInMusicService(song: Song) {
        val service = getMusicService()
        service?.removeSongFromList(song)
    }

    fun playNextSong() {
        musicService.playNext()
    }

    fun playPreviousSong() {
        musicService.playPrevious()
    }

    fun getCurrentSong(): Song {
        return currentSong
    }

    private fun deleteFromDevice(deleteSong: Song) {

        var isInfoDeleted = false
        var isFileDeleted = false

        val numRows = contentResolver.delete(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                MediaStore.Audio.Media._ID + "=?",
                arrayOf(deleteSong.id))

        if (numRows > 0) isInfoDeleted = true

        val deleteFile = File(deleteSong.data)
        if (deleteFile.exists())
            isFileDeleted = deleteFile.delete()

        if (isInfoDeleted && !isFileDeleted)
            Toast.makeText(this,
                    String.format(Locale.ENGLISH, "%s %s", getString(R.string.song_details), getString(R.string.deleted)),
                    Toast.LENGTH_SHORT).show()

        if (!isInfoDeleted && isFileDeleted)
            Toast.makeText(this,
                    String.format(Locale.ENGLISH, "%s %s", getString(R.string.song), getString(R.string.deleted)),
                    Toast.LENGTH_SHORT).show()

        if (isInfoDeleted && isFileDeleted)
            Toast.makeText(this,
                    String.format(
                            Locale.ENGLISH, "%s and %s %s",
                            getString(R.string.song),
                            getString(R.string.song_details),
                            getString(R.string.deleted)),
                    Toast.LENGTH_SHORT).show()

        if (!isInfoDeleted && !isFileDeleted)
            Toast.makeText(this, getString(R.string.deletion_error), Toast.LENGTH_SHORT).show()
    }


    fun showSnackBar(text: String) {
        Snackbar.make(
                findViewById(R.id.coordinator_layout), text, Snackbar.LENGTH_SHORT
        ).show()
    }

    fun startThemeChangeActivity() {
        startActivityForResult(Intent(this, ThemeChangeActivity::class.java), REQUEST_CODE)
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
    }

    fun putSongsListIntoDatabase(audioList: List<Song>) {
        if (::songRepository.isInitialized) songRepository = getSongRepository()

        songRepository.insertSongs(*audioList.toTypedArray())

        loadSongsForSelectedPlaylistFromDb()
    }

    fun removeObservers() {
        if (::listLiveData.isInitialized) listLiveData.removeObservers(this)
    }

    fun playSong(songIndex: Int) {
        if (::musicService.isInitialized) musicService.setSong(songIndex)
    }

    fun setUp() {
        songDatabase = SongDatabase.getInstance(this)
        if (!::helper.isInitialized) helper = PreferencesHelper(this)
        selectedPlayList = ApplicationUtils().convertStringToPlaylistRowItem(helper.currentPlaylistTitle)

        songRepository = getSongRepository()
        songRepository.deleteAllSongs()

        refreshForUserThemeColors()
        setSongListFragment()
        setPlaylistBottomSheet()
    }

    fun removeSongCurrentPlaylist(song: Song, index: Int) {
        if (::songRepository.isInitialized) songRepository = getSongRepository()
        if (::playlistRepository.isInitialized) playlistRepository = getPlaylistRepository()

        val serviceSong = getMusicService()?.getSong()
        if (serviceSong != null && serviceSong.id == song.id) {
            playNextSong()
            getMusicService()?.cancelNotification()
        }
        getMusicService()?.removeSongFromListInMusicServiceById(song.id)

        playlistRepository.removeSongFromPlaylist(song.id, PreferencesHelper(this).currentPlaylistTitle)

        val liveData = songRepository.numRows
        liveData.observe(this, Observer { integer ->
            refreshSongListFragmentForSongDelete(song, index)
            Toast.makeText(this, "Song Removed From Playlist", Toast.LENGTH_SHORT).show()
            liveData.removeObservers(this)
        })
    }

    fun addSongToPlaylist(songId: String, playlist: String) {
        if (::songDatabase.isInitialized) songDatabase = SongDatabase.getInstance(this)

        if (::playlistRepository.isInitialized) getPlaylistRepository()

        val listLiveData = getPlaylistRepository().getSongsByPlaylistName(playlist)

        listLiveData.observe(this, Observer { songs ->
            var isAlreadyPresent = false
            if (songs != null) {
                for (s in songs) {
                    if (s.id == songId) {
                        isAlreadyPresent = true
                        Toast.makeText(this, getString(R.string.song_present_in_playlist), Toast.LENGTH_SHORT).show()
                        break
                    }
                }
            }
            if (!isAlreadyPresent) {
                getPlaylistRepository().insertPlaylistItem(Playlist(songId, playlist))
                Toast.makeText(this, getString(R.string.song_added_to_playlist), Toast.LENGTH_SHORT).show()
            }
            listLiveData.removeObservers(this)
        })
    }

    fun getCurrentSongDuration(): Int {
        return if (::musicService.isInitialized && musicBound)
            musicService.position
        else
            0
    }

    fun rescanDevice() {
        if (supportFragmentManager.findFragmentByTag(getString(R.string.songs_list)) != null) {
            val fragment = supportFragmentManager.findFragmentByTag(getString(R.string.songs_list)) as SongsListFragment?
            fragment?.fetchSongsAsync(FROM_ACTIVITY)
        }
    }

    fun removeSongFromDb(songId: String) {
        if (::songRepository.isInitialized) songRepository = getSongRepository()
        if (::playlistRepository.isInitialized) playlistRepository = getPlaylistRepository()

        songRepository.deleteSongById(songId)
        playlistRepository.deleteSongById(songId)
    }

    fun showSongOptionsOnBottomSheet(song: Song, index: Int) {
        if (::bottomSheetFragment.isInitialized) {
            val bundle = Bundle()
            bundle.putInt(INDEX, index)

            bundle.putString(DATA, song.data)
            bundle.putString(TITLE, song.title)
            bundle.putString(TITLE_KEY, song.titleKey)
            bundle.putString(ID, song.id)
            bundle.putString(DATE_ADDED, song.dateAdded)
            bundle.putString(DATE_MODIFIED, song.dateModified)
            bundle.putString(DURATION, song.duration)
            bundle.putString(COMPOSER, song.composer)
            bundle.putString(ALBUM, song.album)
            bundle.putString(ALBUM_ID, song.albumId)
            bundle.putString(ALBUM_KEY, song.albumKey)
            bundle.putString(ARTIST, song.artist)
            bundle.putString(ARTIST_ID, song.artistId)
            bundle.putString(ARTIST_KEY, song.artistKey)
            bundle.putString(SIZE, song.size)
            bundle.putString(YEAR, song.year)
            bottomSheetFragment.arguments = bundle

            if (playlistSheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED) {
                hidePlaylistBottomSheet()
                Handler().postDelayed({
                    hideOrRemoveBottomSheet()
                    if (!bottomSheetFragment.isAdded)
                        bottomSheetFragment.show(supportFragmentManager, bottomSheetFragment.tag)
                }, 350)
            } else {
                if (!bottomSheetFragment.isAdded)
                    bottomSheetFragment.show(supportFragmentManager, bottomSheetFragment.tag)
            }

        } else {
            bottomSheetFragment = BottomSheetFragment()
            showSongOptionsOnBottomSheet(song, index)
        }
    }

    fun deleteSong(recyclerIndex: Int, deleteSong: Song) {
        if (!::songRepository.isInitialized) songRepository = getSongRepository()
        if (!::playlistRepository.isInitialized) playlistRepository = getPlaylistRepository()

        deleteSongFromSongRepository(deleteSong.id)
        deleteSongFromPlaylistRepository(deleteSong.id)
        changeSongIfSameAsCurrentlyPlaying(deleteSong)
        deleteFromDevice(deleteSong)

        refreshSongListFragmentForSongDelete(deleteSong, recyclerIndex)
        hideOrRemoveBottomSheet()
    }

    fun hideOrRemoveBottomSheet() {
        if (::bottomSheetFragment.isInitialized) {
            if (!bottomSheetFragment.isHidden) {
                if (::bottomSheetFragment.isInitialized) bottomSheetFragment.dismiss()
            }
        }
    }

    fun refreshSongListFragmentForSongDelete(song: Song, index: Int) {
        val fragment = supportFragmentManager.findFragmentByTag(getString(R.string.songs_list)) as SongsListFragment?
        fragment?.refreshSongListFragmentForSongDelete(index)
    }

    private fun deleteSongFromSongRepository(songId: String) {
        songRepository.deleteSongById(songId)
    }

    private fun deleteSongFromPlaylistRepository(songId: String) {
        playlistRepository.deleteSongById(songId)
    }

    override fun getDuration(): Int {
        return if (::musicService.isInitialized && musicBound)
            musicService.duration
        else 0
    }

    override fun pause() {
        //TODO: inject
        val helper = PreferencesHelper(this)
        helper.setIsPlayEvent(false)

        musicService.pausePlayer()
    }

    override fun getBufferPercentage(): Int {
        return 0
    }

    override fun seekTo(position: Int) {
        musicService.seekPlayer(position)
    }

    override fun getCurrentPosition(): Int {
        return if (::musicService.isInitialized && musicBound)
            musicService.position
        else 0
    }

    override fun start() {
        //TODO: inject
        val helper = PreferencesHelper(this)
        helper.setIsPlayEvent(true)

        musicService.playPlayer()
    }

    override fun getAudioSessionId(): Int {
        return 0
    }

    override fun isPlaying(): Boolean {
        return ::musicService.isInitialized && musicService.isPlaying
    }

    override fun canPause(): Boolean {
        return true
    }

    override fun canSeekForward(): Boolean {
        return true
    }

    override fun canSeekBackward(): Boolean {
        return true
    }
}
