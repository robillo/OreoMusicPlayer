package com.robillo.oreomusicplayer.views.activities.main.songs_sort_frag.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.robillo.oreomusicplayer.R;
import com.robillo.oreomusicplayer.models.SortItem;
import com.robillo.oreomusicplayer.models.ThemeColors;
import com.robillo.oreomusicplayer.preferences.AppPreferencesHelper;
import com.robillo.oreomusicplayer.utils.AppConstants;
import com.robillo.oreomusicplayer.views.activities.main.MainActivity;
import com.robillo.oreomusicplayer.views.activities.main.songs_sort_frag.SongsSortFragment;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SortSongsAdapter extends RecyclerView.Adapter<SortSongsAdapter.SortSongsHolder> {

    private Context activityContext;
    private List<SortItem> sortItems;
    private int currentSongOrderForSongsIndex = -1;

    public SortSongsAdapter(Context activityContext,
                            List<SortItem> sortItems,
                            String currentSongOrderForSongs) {
        this.activityContext = activityContext;
        this.sortItems = sortItems;

        SortItem currentSortItem = new SortItem(AppConstants.sortOrderMap.get(currentSongOrderForSongs), currentSongOrderForSongs);
        for(int i = 0; i < sortItems.size(); i++) {
            if(currentSortItem.getConstantSortOrder().equals(sortItems.get(i).getConstantSortOrder())) {
                currentSongOrderForSongsIndex = i;
                break;
            }
        }
    }

    @NonNull
    @Override
    public SortSongsHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new SortSongsHolder(
                LayoutInflater.from(parent.getContext()).inflate(R.layout.row_sort_order, parent, false)
        );
    }

    @Override
    public void onBindViewHolder(@NonNull final SortSongsHolder holder, int position) {

        final AppPreferencesHelper helper = new AppPreferencesHelper(activityContext);
        final ThemeColors currentUserThemeColors = AppConstants.themeMap.get(helper.getUserThemeName());

        holder.title.setText(sortItems.get(position).getTextToDisplay());
        if(currentSongOrderForSongsIndex == position) {
            holder.title.setTextColor(
                    activityContext.getResources().getColor(currentUserThemeColors.getColorPrimary())
            );    //highlight selected item
            holder.title.setBackgroundColor(activityContext.getResources().getColor(R.color.colorTextFive));
        }
        else {
            holder.title.setTextColor(activityContext.getResources().getColor(R.color.colorTextOne));
            holder.title.setBackgroundColor(activityContext.getResources().getColor(R.color.white));
        }

        final int pos = position;
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentSongOrderForSongsIndex = pos;
                holder.title.setTextColor(activityContext.getResources().getColor(
                        currentUserThemeColors.getColorPrimary())
                );
                holder.title.setBackgroundColor(activityContext.getResources().getColor(R.color.colorTextFive));
                notifyDataSetChanged();


                helper.setSortOrderForSongs(sortItems.get(pos).getConstantSortOrder());

                //refresh the loader for new sort order here
                //Possibly using EventBus on loader in SongListFragment
                ((MainActivity) activityContext).repopulateListSongsListFragment();
            }
        });
    }

    @Override
    public int getItemCount() {
        return sortItems.size();
    }

    class SortSongsHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.text)
        TextView title;

        SortSongsHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
