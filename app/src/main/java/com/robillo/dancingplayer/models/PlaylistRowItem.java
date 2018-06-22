package com.robillo.dancingplayer.models;

@SuppressWarnings("unused")
public class PlaylistRowItem {

    private String title;
    private boolean isPersistent;    //Recently Added, Recently Played, Most Played would be permanently stored as Playlists

    public PlaylistRowItem(String title, boolean isPersistent) {
        this.title = title;
        this.isPersistent = isPersistent;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public boolean isPersistent() {
        return isPersistent;
    }

    public void setPersistent(boolean persistent) {
        isPersistent = persistent;
    }
}
