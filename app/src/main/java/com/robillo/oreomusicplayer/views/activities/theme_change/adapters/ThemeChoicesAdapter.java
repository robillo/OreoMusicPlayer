package com.robillo.oreomusicplayer.views.activities.theme_change.adapters;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.robillo.oreomusicplayer.R;
import com.robillo.oreomusicplayer.models.ThemeColors;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ThemeChoicesAdapter extends RecyclerView.Adapter<ThemeChoicesAdapter.ThemeChoicesHolder> {

    private Context context;
    List<ThemeColors> themeColors;

    public ThemeChoicesAdapter(Context context, List<ThemeColors> themeColors) {
        this.context = context;
        this.themeColors = themeColors;
    }

    @NonNull
    @Override
    public ThemeChoicesHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ThemeChoicesHolder(LayoutInflater.from(parent.getContext()).
                inflate(R.layout.row_theme_colors, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ThemeChoicesHolder holder, int position) {
        holder.gradientImageView.setBackground(createGradientDrawable(
                themeColors.get(position).getColorPrimaryDark(),
                themeColors.get(position).getColorPrimary(),
                themeColors.get(position).getColorAccent()
        ));
        holder.colorName.setText(themeColors.get(position).getColorName());
    }

    @Override
    public int getItemCount() {
        return themeColors.size();
    }

    private GradientDrawable createGradientDrawable(int colorPrimaryDark, int colorPrimary, int colorAccent) {
        return new GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                new int[]{
                        context.getResources().getColor(colorPrimary),
                        context.getResources().getColor(colorPrimary),
                        context.getResources().getColor(colorPrimary)
                }
        );
    }

    class ThemeChoicesHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.gradient_image_view)
        ImageView gradientImageView;

        @BindView(R.id.color_name)
        TextView colorName;

        ThemeChoicesHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
