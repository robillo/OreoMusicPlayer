package com.robillo.oreomusicplayer.models;

import android.graphics.Color;

@SuppressWarnings("unused")
public class ThemeColor {

    private int colorPrimary, colorPrimaryDark, colorAccent;

    public ThemeColor(int colorPrimary, int colorPrimaryDark, int colorAccent) {
        this.colorPrimary = colorPrimary;
        this.colorPrimaryDark = colorPrimaryDark;
        this.colorAccent = colorAccent;
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
