package com.robillo.oreomusicplayer.views.activities.main.songs_sort_frag;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.robillo.oreomusicplayer.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * A simple {@link Fragment} subclass.
 */
public class SongsSortFragment extends Fragment implements SongsSortMvpView {

    @BindView(R.id.coordinator_layout)
    CoordinatorLayout coordinatorLayout;

    public SongsSortFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_songs_sort, container, false);
        setup(v);
        return v;
    }

    @Override
    public void setup(View v) {
        ButterKnife.bind(this, v);
    }

    @OnClick(R.id.coordinator_layout)
    public void setCoordinatorLayout() {
        if(getActivity() != null) getActivity().onBackPressed();
    }
}
