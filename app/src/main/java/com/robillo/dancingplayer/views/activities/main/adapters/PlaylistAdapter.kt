package com.robillo.dancingplayer.views.activities.main.adapters

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast

import com.robillo.dancingplayer.R
import com.robillo.dancingplayer.models.PlaylistRowItem
import com.robillo.dancingplayer.utils.AppConstants
import com.robillo.dancingplayer.views.activities.home.HomeActivity

import kotlinx.android.synthetic.main.row_playlist_item.view.*

class PlaylistAdapter(private val list: MutableList<PlaylistRowItem>?, private var pContext: Context?, private val from: Int, private val songId: String) : RecyclerView.Adapter<PlaylistAdapter.PlaylistHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaylistHolder {
        pContext = parent.context
        return PlaylistHolder(
                LayoutInflater.from(pContext).inflate(R.layout.row_playlist_item, parent, false)
        )
    }

    override fun onBindViewHolder(holder: PlaylistHolder, position: Int) {
        holder.playlist_title.text = list!![position].title

        if (from != AppConstants.FROM_SONGS_LIST) {
            if (list[position].isPersistent) {
                holder.delete_playlist.visibility = View.GONE
                holder.edit_name.visibility = View.GONE
            } else {
                holder.delete_playlist.visibility = View.VISIBLE
                holder.edit_name.visibility = View.VISIBLE
            }
        } else {
            holder.delete_playlist.visibility = View.GONE
            holder.edit_name.visibility = View.GONE

            if (list[position].isPersistent) {
                holder.blank_view.visibility = View.GONE
                holder.add_to_this_playlist.visibility = View.GONE
                holder.playlist_title.setTextColor(pContext!!.resources.getColor(R.color.colorTextThree))
            } else {
                holder.blank_view.visibility = View.VISIBLE
                holder.add_to_this_playlist.visibility = View.VISIBLE
                holder.playlist_title.setTextColor(pContext!!.resources.getColor(R.color.colorTextOne))
            }
        }

        holder.playlist_title.setOnClickListener { v ->
            val activity = pContext as HomeActivity?
            if (activity != null) {
                handlePlaylistClick(activity, from, position, songId)
            }
        }

        holder.blank_view.setOnClickListener { v ->
            val activity = pContext as HomeActivity?
            if (activity != null) {
                handlePlaylistClick(activity, from, position, songId)
            }
        }

        holder.edit_name.setOnClickListener { v ->
            (pContext as HomeActivity)
                    .showEditCreateDialogFragment(AppConstants.EDIT_PLAYLIST_NAME, position, list[position].title)
        }

        holder.delete_playlist.setOnClickListener { v ->
            (pContext as HomeActivity)
                    .handleDeletePlaylist(list[position].title)
            Log.e("tag", "onclick " + list[position].title)
        }

        holder.add_to_this_playlist.setOnClickListener { v ->
            val activity = pContext as HomeActivity?
            if (activity != null) {
                handlePlaylistClick(activity, from, position, songId)
            }
        }
    }

    private fun handlePlaylistClick(activity: HomeActivity, from: Int, position: Int, songId: String) {
        if (from != AppConstants.FROM_SONGS_LIST) {
            activity.updatePlaylistListForSelectedItem(list!![position], position)
            Log.e("tag", "update playlist")
        } else {
            if (list!![position].isPersistent) {
                Toast.makeText(pContext, "You can only modify playlists that you created", Toast.LENGTH_SHORT).show()
            } else {
                Log.e("tag", "add song")
                activity.addSongToPlaylist(songId, list[position].title)
            }
        }
    }

    fun addItem(item: PlaylistRowItem) {
        list!!.add(item)
    }

    override fun getItemCount(): Int {
        return list?.size ?: 0
    }

    inner class PlaylistHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var background_view: LinearLayout = itemView.background_view
        var add_to_this_playlist: ImageView = itemView.add_to_this_playlist
        var blank_view: View = itemView.blank_view
        var edit_name: ImageView = itemView.edit_name
        var delete_playlist: ImageView = itemView.delete_playlist
        var playlist_title: TextView = itemView.playlist_title
    }
}
