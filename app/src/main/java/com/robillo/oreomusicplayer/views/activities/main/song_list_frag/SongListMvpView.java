package com.robillo.oreomusicplayer.views.activities.main.song_list_frag;

import android.database.Cursor;
import android.view.View;

/**
 * Created by robinkamboj on 05/03/18.
 */

public interface SongListMvpView {

    void setUp(View v);

    String returnCursorElement(Cursor cursor, String string);
}
