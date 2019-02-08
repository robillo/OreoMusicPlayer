package com.robillo.dancingplayer.views.activities.main.songs_sort_frag.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.robillo.dancingplayer.R;
import com.robillo.dancingplayer.models.SortItem;
import com.robillo.dancingplayer.models.ThemeColors;
import com.robillo.dancingplayer.preferences.AppPreferencesHelper;
import com.robillo.dancingplayer.utils.AppConstants;
import com.robillo.dancingplayer.views.activities.main.MainActivity;

import java.util.List;
import java.util.Locale;

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
            holder.title.setBackgroundColor(activityContext.getResources().getColor(R.color.colorTextFour));
        }
        else {
            holder.title.setTextColor(activityContext.getResources().getColor(R.color.colorTextOne));
            holder.title.setBackgroundColor(activityContext.getResources().getColor(R.color.white));
        }

        final int pos = position;
        holder.itemView.setOnClickListener(v -> {
            currentSongOrderForSongsIndex = pos;
            holder.title.setTextColor(activityContext.getResources().getColor(
                    currentUserThemeColors.getColorPrimary())
            );
            holder.title.setBackgroundColor(activityContext.getResources().getColor(R.color.colorTextFive));
            notifyDataSetChanged();

            helper.setSortOrderForSongs(sortItems.get(pos).getConstantSortOrder());

            ((MainActivity) activityContext).showSnackBar(
                    String.format(Locale.ENGLISH, "%s - %s",
                            activityContext.getString(R.string.sort_successful), sortItems.get(pos).getTextToDisplay())
            );

            //refresh the loader for new sort order here
            //Possibly using EventBus on loader in SongListFragment
            ((MainActivity) activityContext).repopulateListSongsListFragment();
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