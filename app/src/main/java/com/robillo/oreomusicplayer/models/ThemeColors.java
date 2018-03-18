package com.robillo.oreomusicplayer.models;

import android.graphics.Color;

@SuppressWarnings("unused")
public class ThemeColors {

    private int colorPrimary, colorPrimaryDark, colorAccent;
    private String colorName;

    public ThemeColors(int colorPrimary, int colorPrimaryDark, int colorAccent, String colorName) {
        this.colorPrimary = colorPrimary;
        this.colorPrimaryDark = colorPrimaryDark;
        this.colorAccent = colorAccent;
        this.colorName = colorName;
    }

    public String getColorName() {
        return colorName;
    }

    public void setColorName(String colorName) {
        this.colorName = colorName;
    }

    public int getColorPrimary() {
        return colorPrimary;
    }

    public void setColorPrimary(int colorPrimary) {
        this.colorPrimary = colorPrimary;
    }

    public int getColorPrimaryDark() {
        return colorPrimaryDark;
    }

    public void setColorPrimaryDark(int colorPrimaryDark) {
        this.colorPrimaryDark = colorPrimaryDark;
    }

    public int getColorAccent() {
        return colorAccent;
    }

    public void setColorAccent(int colorAccent) {
        this.colorAccent = colorAccent;
    }
}
