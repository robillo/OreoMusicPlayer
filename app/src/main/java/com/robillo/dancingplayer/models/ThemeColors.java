package com.robillo.dancingplayer.models;

public class ThemeColors {

    private int colorPrimary, colorPrimaryDark, colorAccent, colorMat;
    private String colorName;

    public ThemeColors(int colorPrimary, int colorPrimaryDark, int colorAccent, int colorMat, String colorName) {
        this.colorPrimary = colorPrimary;
        this.colorPrimaryDark = colorPrimaryDark;
        this.colorAccent = colorAccent;
        this.colorName = colorName;
        this.colorMat = colorMat;
    }

    public int getColorMat() {
        return colorMat;
    }

    public void setColorMat(int colorMat) {
        this.colorMat = colorMat;
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
