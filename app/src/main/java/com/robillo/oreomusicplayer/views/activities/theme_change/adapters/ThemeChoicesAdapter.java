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
import com.robillo.oreomusicplayer.models.ThemeChangeEvent;
import com.robillo.oreomusicplayer.models.ThemeColors;
import com.robillo.oreomusicplayer.preferences.AppPreferencesHelper;
import com.robillo.oreomusicplayer.views.activities.theme_change.ThemeChangeActivity;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ThemeChoicesAdapter extends RecyclerView.Adapter<ThemeChoicesAdapter.ThemeChoicesHolder> {

    private Context context;
    private List<ThemeColors> themeColors;
    private int currentUserThemeColorsIndex = -1;

    public ThemeChoicesAdapter(Context context, List<ThemeColors> themeColors, ThemeColors currentUserThemeColors) {
        this.context = context;
        this.themeColors = themeColors;

        for(int i = 0; i < themeColors.size(); i++) {
            if(themeColors.get(i).equals(currentUserThemeColors)) {
                currentUserThemeColorsIndex = i;
                break;
            }
        }
    }

    @NonNull
    @Override
    public ThemeChoicesHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ThemeChoicesHolder(LayoutInflater.from(parent.getContext()).
                inflate(R.layout.row_theme_colors, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ThemeChoicesHolder holder, int position) {

        holder.colorName.setText(themeColors.get(position).getColorName());

        if(position == currentUserThemeColorsIndex) {
            holder.colorName.setTextColor(context.getResources().getColor(R.color.white));
            holder.colorName.setBackgroundColor(context.getResources().getColor(R.color.green_primary_dark));
        }
        else {
            holder.colorName.setTextColor(context.getResources().getColor(R.color.colorTextOne));
            holder.colorName.setBackgroundColor(context.getResources().getColor(R.color.white));
        }

        holder.gradientImageView.setBackground(createGradientDrawable(
                themeColors.get(position).getColorPrimaryDark(),
                themeColors.get(position).getColorPrimary(),
                themeColors.get(position).getColorAccent()
        ));

        //noinspection UnnecessaryLocalVariable
        final int pos = position;
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentUserThemeColorsIndex = pos;
                notifyDataSetChanged();

                AppPreferencesHelper helper = new AppPreferencesHelper(context);
                helper.setUserThemeName(themeColors.get(pos).getColorName());

                ((ThemeChangeActivity) context).showSnackBarThemeSet(themeColors.get(pos).getColorName());

                EventBus.getDefault().postSticky(new ThemeChangeEvent());
            }
        });
    }

    @Override
    public int getItemCount() {
        return themeColors.size();
    }

    @SuppressWarnings("unused")
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
