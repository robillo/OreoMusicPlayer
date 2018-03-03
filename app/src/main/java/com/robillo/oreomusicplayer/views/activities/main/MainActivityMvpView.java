package com.robillo.oreomusicplayer.views.activities.main;

/**
 * Created by robinkamboj on 04/03/18.
 */

public interface MainActivityMvpView {

    void setUp();

    void setSongListFragment();

    void setSongPlayFragment();

    void askForDevicePermissions();
}
