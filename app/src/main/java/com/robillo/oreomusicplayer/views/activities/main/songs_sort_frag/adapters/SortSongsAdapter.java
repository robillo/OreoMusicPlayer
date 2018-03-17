package com.robillo.oreomusicplayer.views.activities.main.songs_sort_frag.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.robillo.oreomusicplayer.R;
import com.robillo.oreomusicplayer.models.SortItem;
import com.robillo.oreomusicplayer.views.activities.main.MainActivity;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SortSongsAdapter extends RecyclerView.Adapter<SortSongsAdapter.SortSongsHolder> {

    private Context context;
    private List<SortItem> sortItems;

    public SortSongsAdapter(Context context, List<SortItem> sortItems) {
        this.context = context;
        this.sortItems = sortItems;
    }

    @NonNull
    @Override
    public SortSongsHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new SortSongsHolder(
                LayoutInflater.from(parent.getContext()).inflate(R.layout.row_sort_order, parent, false)
        );
    }

    @Override
    public void onBindViewHolder(@NonNull SortSongsHolder holder, int position) {
        holder.title.setText(sortItems.get(position).getTextToDisplay());
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity) context).isPlaying();       //refresh the loader for new sort order here
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
