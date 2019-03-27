package com.robillo.dancingplayer.views.activities.main.song_list_frag.adapters

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageButton
import kotlinx.android.synthetic.main.row_song.view.*

class SongHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    var parentView: View = itemView
    var moreButton: ImageButton = itemView.more
    var linearLayout = itemView.linear_layout
    var songCard = itemView.song_card
    var mTitle = itemView.title
    var artistDuration = itemView.artist_duration
    var albumArt = itemView.album_art
}
