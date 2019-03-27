package com.robillo.dancingplayer.views.activities.main.song_list_frag.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.provider.MediaStore
import android.support.v7.widget.RecyclerView
import android.text.format.DateFormat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView

import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.robillo.dancingplayer.R
import com.robillo.dancingplayer.models.Song
import com.robillo.dancingplayer.preferences.PreferencesHelper
import com.robillo.dancingplayer.utils.ApplicationUtils
import com.robillo.dancingplayer.views.activities.home.HomeActivity
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView

import java.sql.Date

import com.robillo.dancingplayer.utils.AppConstants.ALBUM_ASCENDING
import com.robillo.dancingplayer.utils.AppConstants.ALBUM_DESCENDING
import com.robillo.dancingplayer.utils.AppConstants.ARTIST_ASCENDING
import com.robillo.dancingplayer.utils.AppConstants.ARTIST_DESCENDING
import com.robillo.dancingplayer.utils.AppConstants.DATE_ADDED_ASCENDING
import com.robillo.dancingplayer.utils.AppConstants.DATE_ADDED_DESCENDING
import com.robillo.dancingplayer.utils.AppConstants.DATE_MODIFIED_ASCENDING
import com.robillo.dancingplayer.utils.AppConstants.DATE_MODIFIED_DESCENDING
import com.robillo.dancingplayer.utils.AppConstants.SIZE_ASCENDING
import com.robillo.dancingplayer.utils.AppConstants.SIZE_DESCENDING
import com.robillo.dancingplayer.utils.AppConstants.TITLE_ASCENDING
import com.robillo.dancingplayer.utils.AppConstants.TITLE_DESCENDING
import com.robillo.dancingplayer.utils.AppConstants.YEAR_ASCENDING
import com.robillo.dancingplayer.utils.AppConstants.YEAR_DESCENDING
import java.util.ArrayList

class SongsAdapter(private val list: ArrayList<Song>?, private val context: Context?) : RecyclerView.Adapter<SongHolder>(), FastScrollRecyclerView.SectionedAdapter {

    private var parentContext: Context? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongHolder {
        parentContext = parent.context
        return SongHolder(LayoutInflater.from(parentContext).inflate(R.layout.row_song, parent, false))
    }

    override fun onBindViewHolder(holder: SongHolder, @SuppressLint("RecyclerView") position: Int) {

        list?.let {
            if(it[position] == null || it[position]?.id == null)  {
                holder.moreButton.visibility = View.GONE

                holder.itemView.visibility = View.INVISIBLE
                val empty = ""
                holder.mTitle.text = empty
                holder.artistDuration.text = empty
                holder.itemView.isClickable = false
            }
            else {
                holder.itemView.visibility = View.VISIBLE
                holder.moreButton.visibility = View.VISIBLE

                setAlbumArt(holder.albumArt, context, it[position]!!)

                holder.mTitle.text = it[position]!!.title
                var duration: Long = 0
                try {
                    duration = (Integer.valueOf(it[position]!!.duration) / 1000).toLong()
                } catch (ignored: NumberFormatException) {
                }

                val temp = it[position]!!.artist + " (" + ApplicationUtils().formatStringOutOfSeconds(duration.toInt()) + ")"
                holder.artistDuration.text = temp

                holder.albumArt.setOnClickListener { v ->
                    if (context != null) {
                        Log.e("song id", "id " + it[position]!!.id + " " + it[position]!!.title)
                        (context as HomeActivity).playSong(position)
                    }
                }

                holder.linearLayout.setOnClickListener { v ->
                    if (context != null) {
                        Log.e("song id", "id " + list[position]!!.id + " " + list[position]!!.title)
                        (context as HomeActivity).playSong(position)
                    }
                }

                holder.linearLayout.setOnLongClickListener { view ->
                    (context as HomeActivity).showSongOptionsOnBottomSheet(it[position]!!, position)
                    true
                }

                holder.moreButton.setOnClickListener { view -> (context as HomeActivity).showSongOptionsOnBottomSheet(it[position]!!, position) }
            }
        }
    }

    private fun setAlbumArt(imageView: ImageView, context: Context?, song: Song) {
        var path: String? = null
        if (context != null) {

            //get path for the album art for this song
            val cursor = context.contentResolver.query(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
                    arrayOf(MediaStore.Audio.Albums._ID, MediaStore.Audio.Albums.ALBUM_ART),
                    MediaStore.Audio.Albums._ID + "=?",
                    arrayOf(song.albumId.toString()), null)
            if (cursor != null && cursor.moveToFirst()) {
                path = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Albums.ALBUM_ART))
                // do whatever you need to do
                cursor.close()
            }

            //set album art
            Glide.with(context)
                    .load(path)
                    .apply(RequestOptions.centerCropTransform().placeholder(R.drawable.song_placeholder))
                    .into(imageView)

        }
    }

    override fun getItemCount(): Int {
        return list?.size ?: 0
    }

    fun removeListItem(position: Int) {
        list!!.removeAt(position)
    }

    override fun getSectionName(position: Int): String {
        var sectionString = ""
        if (parentContext != null && list != null) {
            val SORT_ORDER = PreferencesHelper(parentContext!!).sortOrderForSongs
            when (SORT_ORDER) {
                SIZE_ASCENDING, SIZE_DESCENDING -> if (list[position]!!.size != null) {
                    try {
                        val size = Integer.valueOf(list[position]!!.size) / 1024.0
                        sectionString = ApplicationUtils().formatSizeKBtoMB(size)
                    } catch (ignored: IllegalArgumentException) {
                    }

                }
                YEAR_ASCENDING, YEAR_DESCENDING -> if (list[position]!!.year != null)
                    sectionString = list[position]!!.year
                ALBUM_ASCENDING, ALBUM_DESCENDING -> if (list[position]!!.album != null)
                    sectionString = list[position]!!.album.substring(0, 1)
                TITLE_ASCENDING, TITLE_DESCENDING -> if (list[position]!!.title != null)
                    sectionString = list[position]!!.title.substring(0, 1)
                ARTIST_ASCENDING, ARTIST_DESCENDING -> if (list[position]!!.artist != null)
                    sectionString = list[position]!!.artist.substring(0, 1)
                DATE_ADDED_ASCENDING, DATE_ADDED_DESCENDING -> if (list[position]!!.dateAdded != null) {
                    try {
                        val date = java.lang.Long.valueOf(list[position]!!.dateAdded)
                        sectionString = DateFormat.format("MM/dd/yyyy",
                                Date(date * 1000)).toString()
                    } catch (e: IllegalArgumentException) {
                        Log.e("tag", "illegal argument adapter 163")
                    }

                }
                DATE_MODIFIED_ASCENDING, DATE_MODIFIED_DESCENDING -> if (list[position]!!.dateModified != null) {
                    try {
                        val date = java.lang.Long.valueOf(list[position]!!.dateModified)
                        sectionString = DateFormat.format("MM/dd/yyyy",
                                Date(date * 1000)).toString()
                    } catch (e: IllegalArgumentException) {
                        Log.e("tag", "illegal argument adapter 163")
                    }

                }
            }
        }
        return sectionString
    }
}
