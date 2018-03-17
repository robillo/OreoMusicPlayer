package com.robillo.oreomusicplayer.models;

/**
 * Created by robinkamboj on 17/03/18.
 */

public class SortItem {

    private String textToDisplay;
    private String constantSortOrder;

    public SortItem(String textToDisplay, String constantSortOrder) {
        this.textToDisplay = textToDisplay;
        this.constantSortOrder = constantSortOrder;
    }

    public String getTextToDisplay() {
        return textToDisplay;
    }

    public void setTextToDisplay(String textToDisplay) {
        this.textToDisplay = textToDisplay;
    }

    public String getConstantSortOrder() {
        return constantSortOrder;
    }

    public void setConstantSortOrder(String constantSortOrder) {
        this.constantSortOrder = constantSortOrder;
    }
}
