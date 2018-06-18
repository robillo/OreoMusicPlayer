package com.robillo.dancingplayer.models;

public class SetSeekBarEvent {

    private int totalDuration;
    private  int currentDuration;

    public SetSeekBarEvent(int currentDuration, int totalDuration) {
        this.currentDuration = currentDuration;
        this.totalDuration = totalDuration;
    }

    public int getTotalDuration() {
        return totalDuration;
    }

    public void setTotalDuration(int totalDuration) {
        this.totalDuration = totalDuration;
    }

    public int getCurrentDuration() {
        return currentDuration;
    }

    public void setCurrentDuration(int currentDuration) {
        this.currentDuration = currentDuration;
    }
}
