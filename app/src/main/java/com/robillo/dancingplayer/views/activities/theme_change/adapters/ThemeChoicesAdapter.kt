package com.robillo.dancingplayer.views.activities.theme_change.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView

import com.robillo.dancingplayer.R
import com.robillo.dancingplayer.events.ThemeChangeEvent
import com.robillo.dancingplayer.models.ThemeColors
import com.robillo.dancingplayer.preferences.PreferencesHelper
import com.robillo.dancingplayer.views.activities.theme_change.ThemeChangeActivity

import org.greenrobot.eventbus.EventBus

import kotlinx.android.synthetic.main.row_theme_colors.view.*

class ThemeChoicesAdapter(private val context: Context, private val themeColors: List<ThemeColors>, currentUserThemeColors: ThemeColors) : RecyclerView.Adapter<ThemeChoicesAdapter.ThemeChoicesHolder>() {
    private var currentUserThemeColorsIndex = -1

    init {

        for (i in themeColors.indices) {
            if (themeColors[i] == currentUserThemeColors) {
                currentUserThemeColorsIndex = i
                break
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ThemeChoicesHolder {
        return ThemeChoicesHolder(LayoutInflater.from(parent.context).inflate(R.layout.row_theme_colors, parent, false))
    }

    override fun onBindViewHolder(holder: ThemeChoicesHolder, @SuppressLint("RecyclerView") position: Int) {

        holder.colorName.text = themeColors[position].colorName

        if (position == currentUserThemeColorsIndex) {
            holder.colorName.setTextColor(context.resources.getColor(R.color.colorTextFive))
            holder.colorName.setBackgroundColor(context.resources.getColor(R.color.green_primary_dark))
        } else {
            holder.colorName.setTextColor(context.resources.getColor(R.color.colorTextOne))
            holder.colorName.setBackgroundColor(context.resources.getColor(R.color.colorTextFive))
        }

        holder.gradientImageView.background = createGradientDrawable(
                themeColors[position].colorPrimaryDark,
                themeColors[position].colorPrimaryDark,
                themeColors[position].colorPrimaryDark
        )


        holder.itemView.setOnClickListener { v ->
            currentUserThemeColorsIndex = position
            notifyDataSetChanged()

            val helper = PreferencesHelper(context)
            helper.userThemeName = themeColors[position].colorName

            (context as ThemeChangeActivity).showSnackBarThemeSet(themeColors[position].colorName)

            EventBus.getDefault().postSticky(ThemeChangeEvent())
        }
    }

    override fun getItemCount(): Int {
        return themeColors.size
    }

    private fun createGradientDrawable(colorPrimaryDark: Int, colorPrimary: Int, colorAccent: Int): GradientDrawable {
        return GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                intArrayOf(context.resources.getColor(colorPrimaryDark), context.resources.getColor(colorPrimary), context.resources.getColor(colorAccent))
        )
    }

    inner class ThemeChoicesHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var gradientImageView: ImageView = itemView.gradient_image_view
        var colorName: TextView = itemView.color_name
    }
}
