package com.robillo.dancingplayer.utils

import android.content.ContentResolver
import android.content.ContentUris
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore

import com.robillo.dancingplayer.models.Song

import java.util.ArrayList
import java.util.Random

class MusicFetcher(private val contentResolver: ContentResolver) {

    var allSongs: List<Song> = ArrayList()
    private val mRandom = Random()

    val randomSong: Song?
        get() = if (allSongs.size <= 0) null else allSongs[mRandom.nextInt(allSongs.size)]

    fun prepare() {
        val uri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI

        val cursor: Cursor? = contentResolver.query(
                uri, null,
                MediaStore.Audio.Media.IS_MUSIC + " = 1",
                null,
                null
        ) ?: return

        val list = ArrayList<Song>()

        cursor?.let {
            if(it.count > 0)
                while (it.moveToNext()) {
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

        allSongs = list

        cursor?.close()
    }

    private fun returnCursorElement(cursor: Cursor?, string: String?): String {
        cursor?.let { c -> string?.let { s ->
            c.getString(c.getColumnIndex(s))?.let {
                return it
            } }
        } ?: return ""
    }

    companion object {
        private val TAG = "MPLAY_MUSIC_FINDER"
    }
}
