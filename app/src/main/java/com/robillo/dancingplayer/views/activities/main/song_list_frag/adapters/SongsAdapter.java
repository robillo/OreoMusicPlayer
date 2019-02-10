package com.robillo.dancingplayer.views.activities.main.song_list_frag.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.robillo.dancingplayer.R;
import com.robillo.dancingplayer.models.Song;
import com.robillo.dancingplayer.preferences.AppPreferencesHelper;
import com.robillo.dancingplayer.utils.ApplicationUtils;
import com.robillo.dancingplayer.views.activities.main.MainActivity;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import java.sql.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.robillo.dancingplayer.utils.AppConstants.ALBUM_ASCENDING;
import static com.robillo.dancingplayer.utils.AppConstants.ALBUM_DESCENDING;
import static com.robillo.dancingplayer.utils.AppConstants.ARTIST_ASCENDING;
import static com.robillo.dancingplayer.utils.AppConstants.ARTIST_DESCENDING;
import static com.robillo.dancingplayer.utils.AppConstants.DATE_ADDED_ASCENDING;
import static com.robillo.dancingplayer.utils.AppConstants.DATE_ADDED_DESCENDING;
import static com.robillo.dancingplayer.utils.AppConstants.DATE_MODIFIED_ASCENDING;
import static com.robillo.dancingplayer.utils.AppConstants.DATE_MODIFIED_DESCENDING;
import static com.robillo.dancingplayer.utils.AppConstants.SIZE_ASCENDING;
import static com.robillo.dancingplayer.utils.AppConstants.SIZE_DESCENDING;
import static com.robillo.dancingplayer.utils.AppConstants.TITLE_ASCENDING;
import static com.robillo.dancingplayer.utils.AppConstants.TITLE_DESCENDING;
import static com.robillo.dancingplayer.utils.AppConstants.YEAR_ASCENDING;
import static com.robillo.dancingplayer.utils.AppConstants.YEAR_DESCENDING;

public class SongsAdapter
        extends RecyclerView.Adapter<SongsAdapter.SongHolder>
        implements FastScrollRecyclerView.SectionedAdapter {

    private List<Song> list;
    private Context context;
    private Context parentContext;

    public SongsAdapter(List<Song> list, Context context) {
        this.list = list;
        this.context = context;
    }

    @NonNull
    @Override
    public SongHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        parentContext = parent.getContext();
        return new SongHolder(LayoutInflater.from(parentContext).inflate(R.layout.row_song, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull SongHolder holder, @SuppressLint("RecyclerView") final int position) {

        //noinspection UnnecessaryLocalVariable
        final int _pos = position;

        //noinspection ConstantConditions
        if(list.get(_pos) == null || list.get(_pos).getId() == null) {
            holder.moreButton.setVisibility(View.GONE);

            holder.itemView.setVisibility(View.INVISIBLE);
            String empty = "";
            holder.title.setText(empty);
            holder.artistDuration.setText(empty);
            holder.itemView.setClickable(false);
        }
        else {
            holder.itemView.setVisibility(View.VISIBLE);
            holder.moreButton.setVisibility(View.VISIBLE);

            setAlbumArt(holder.albumArt, context, list.get(position));

            holder.title.setText(list.get(_pos).getTitle());
            long duration = 0;
            try {
                duration = Integer.valueOf(list.get(_pos).getDuration())/1000;
            }
            catch (NumberFormatException ignored) { }
            String temp = list.get(_pos).getArtist() + " (" + new ApplicationUtils().formatStringOutOfSeconds((int) duration) + ")";
            holder.artistDuration.setText(temp);

            holder.albumArt.setOnClickListener(v -> {
                if(context!=null){
                    Log.e("song id", "id " + list.get(position).getId() + " " + list.get(position).getTitle());
                    ((MainActivity) context).playSong(_pos);
                }
            });

            holder.linearLayout.setOnClickListener(v -> {
                if(context!=null){
                    Log.e("song id", "id " + list.get(position).getId() + " " + list.get(position).getTitle());
                    ((MainActivity) context).playSong(_pos);
                }
            });

            holder.linearLayout.setOnLongClickListener(view -> {
                ((MainActivity) context).showSongOptionsOnBottomSheet(list.get(_pos), _pos);
                return true;
            });

            holder.moreButton.setOnClickListener(view -> ((MainActivity) context).showSongOptionsOnBottomSheet(list.get(_pos), _pos));
        }
    }

    private void setAlbumArt(ImageView imageView, Context context, Song song) {
        String path = null;
        if(context!=null) {

            //get path for the album art for this song
            Cursor cursor = context.getContentResolver().query(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
                    new String[] {MediaStore.Audio.Albums._ID, MediaStore.Audio.Albums.ALBUM_ART},
                    MediaStore.Audio.Albums._ID+ "=?",
                    new String[] {String.valueOf(song.getAlbumId())},
                    null);
            if(cursor!=null && cursor.moveToFirst()) {
                path = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Albums.ALBUM_ART));
                // do whatever you need to do
                cursor.close();
            }

            //set album art
            Glide.with(context)
                    .load(path)
                    .apply(RequestOptions.centerCropTransform().placeholder(R.drawable.song_placeholder))
                    .into(imageView);

        }
    }

    @Override
    public int getItemCount() {
        return list!=null ? list.size() : 0;
    }

    public void removeListItem(int position) {
        list.remove(position);
    }

    class SongHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.album_art)
        ImageView albumArt;

        @BindView(R.id.title)
        TextView title;

        @BindView(R.id.artist_duration)
        TextView artistDuration;

        @BindView(R.id.song_card)
        CardView songCard;

        @BindView(R.id.linear_layout)
        LinearLayout linearLayout;

        @BindView(R.id.more)
        ImageButton moreButton;

        SongHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    @NonNull
    @Override
    public String getSectionName(int position) {
        String sectionString = "";
        if(parentContext != null && list != null) {
            String SORT_ORDER = new AppPreferencesHelper(parentContext).getSortOrderForSongs();
            switch (SORT_ORDER) {
                case SIZE_ASCENDING:
                case SIZE_DESCENDING:
                    if (list.get(position).getSize() != null) {
                        try {
                            double size = Integer.valueOf(list.get(position).getSize())/1024.0;
                            sectionString = new ApplicationUtils().formatSizeKBtoMB(size);
                        }
                        catch(IllegalArgumentException ignored) { }
                    }
                    break;
                case YEAR_ASCENDING:
                case YEAR_DESCENDING:
                    if (list.get(position).getYear() != null)
                        sectionString = list.get(position).getYear();
                    break;
                case ALBUM_ASCENDING:
                case ALBUM_DESCENDING:
                    if (list.get(position).getAlbum() != null)
                        sectionString = list.get(position).getAlbum().substring(0, 1);
                    break;
                case TITLE_ASCENDING:
                case TITLE_DESCENDING:
                    if (list.get(position).getTitle() != null)
                        sectionString = list.get(position).getTitle().substring(0, 1);
                    break;
                case ARTIST_ASCENDING:
                case ARTIST_DESCENDING:
                    if (list.get(position).getArtist() != null)
                        sectionString = list.get(position).getArtist().substring(0, 1);
                    break;
                case DATE_ADDED_ASCENDING:
                case DATE_ADDED_DESCENDING:
                    if (list.get(position).getDateAdded() != null) {
                        try {
                            long date = Long.valueOf(list.get(position).getDateAdded());
                            sectionString = DateFormat.format("MM/dd/yyyy",
                                    new Date(date * 1000)).toString();
                        }
                        catch(IllegalArgumentException e) {
                            Log.e("tag", "illegal argument adapter 163");
                        }
                    }
                    break;
                case DATE_MODIFIED_ASCENDING:
                case DATE_MODIFIED_DESCENDING:
                    if (list.get(position).getDateModified() != null) {
                        try {
                            long date = Long.valueOf(list.get(position).getDateModified());
                            sectionString = DateFormat.format("MM/dd/yyyy",
                                    new Date(date * 1000)).toString();
                        }
                        catch(IllegalArgumentException e) {
                            Log.e("tag", "illegal argument adapter 163");
                        }
                    }
                    break;
            }
        }
        return sectionString;
    }
}
