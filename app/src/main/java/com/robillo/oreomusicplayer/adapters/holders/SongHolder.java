package com.robillo.oreomusicplayer.adapters.holders;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.robillo.oreomusicplayer.R;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by robinkamboj on 11/09/17.
 */

public class SongHolder extends RecyclerView.ViewHolder{

    @BindView(R.id.title)
    public TextView title;
    @BindView(R.id.artist_duration)
    public TextView artistDuration;
    @BindView(R.id.song_card)
    public CardView songCard;


    public SongHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }
}
