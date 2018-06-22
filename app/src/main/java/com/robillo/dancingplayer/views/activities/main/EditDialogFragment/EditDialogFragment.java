package com.robillo.dancingplayer.views.activities.main.EditDialogFragment;

import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.robillo.dancingplayer.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.robillo.dancingplayer.utils.AppConstants.CREATE_NEW_PLAYLIST;
import static com.robillo.dancingplayer.utils.AppConstants.CREATE_NEW_PLAYLIST_STRING;
import static com.robillo.dancingplayer.utils.AppConstants.EDIT_PLAYLIST_NAME;
import static com.robillo.dancingplayer.utils.AppConstants.EDIT_PLAYLIST_NAME_STRING;
import static com.robillo.dancingplayer.utils.AppConstants.FROM;

public class EditDialogFragment extends DialogFragment implements EditDialogMvpView {

    @SuppressWarnings("FieldCanBeLocal")
    private Bundle args;
    private int from;

    @BindView(R.id.done)
    TextView done;

    @BindView(R.id.cancel)
    TextView cancel;

    @BindView(R.id.header)
    TextView header;

    @BindView(R.id.edit_text)
    EditText editText;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_edit_dialog, container, false);
        ButterKnife.bind(this, view);
        setup(view);
        return view;
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @OnClick(R.id.done)
    public void setDone() {
        if(editText.getText().length() == 0) {
            Toast.makeText(getActivity(), R.string.enter_playlist_name, Toast.LENGTH_SHORT).show();
        }
        else {
            if(from == EDIT_PLAYLIST_NAME) {
                //change playlist name in activity list as well as recycler list and re-render recycler
            }
            else if(from == CREATE_NEW_PLAYLIST) {
                //create new playlist, add playlist item in activity list as well as recycler list and re-render recycler
            }
            this.dismiss();
        }
    }

    @OnClick(R.id.cancel)
    public void setCancel() {
        if(from == EDIT_PLAYLIST_NAME) {
            Toast.makeText(getActivity(), R.string.edit_cancel, Toast.LENGTH_SHORT).show();
        }
        else if(from == CREATE_NEW_PLAYLIST) {
            Toast.makeText(getActivity(), R.string.creat_cancel, Toast.LENGTH_SHORT).show();
        }
        this.dismiss();
    }

    @OnClick(R.id.header)
    public void setHeader() {

    }

    @Override
    public void setup(View v) {
        args = getArguments();
        if(args != null) {
            from = args.getInt(FROM);

            if(from == EDIT_PLAYLIST_NAME) {
                header.setText(EDIT_PLAYLIST_NAME_STRING);
            }
            else if(from == CREATE_NEW_PLAYLIST) {
                header.setText(CREATE_NEW_PLAYLIST_STRING);
            }
        }
    }
}
