package com.robillo.dancingplayer.views.activities.main.EditDialogFragment

import android.app.DialogFragment
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast

import com.robillo.dancingplayer.R
import com.robillo.dancingplayer.views.activities.home.HomeActivity

import com.robillo.dancingplayer.utils.AppConstants.CREATE_NEW_PLAYLIST
import com.robillo.dancingplayer.utils.AppConstants.CREATE_NEW_PLAYLIST_STRING
import com.robillo.dancingplayer.utils.AppConstants.EDIT_PLAYLIST_NAME
import com.robillo.dancingplayer.utils.AppConstants.EDIT_PLAYLIST_NAME_STRING
import com.robillo.dancingplayer.utils.AppConstants.FROM
import com.robillo.dancingplayer.utils.AppConstants.OLD_PLAYLIST_NAME
import com.robillo.dancingplayer.utils.AppConstants.POSITION
import kotlinx.android.synthetic.main.fragment_edit_dialog.*
import kotlinx.android.synthetic.main.fragment_edit_dialog.view.*

class EditDialogFragment : DialogFragment(), EditDialogMvpView {

    private var args: Bundle? = null
    private var from: Int = 0
    private var oldPlaylistName: String? = null
    private var position = -1

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_edit_dialog, container, false)
        setup(view)
        return view
    }

    private fun setDone() {
        if (edit_text!!.text.isEmpty()) {
            Toast.makeText(activity, R.string.enter_playlist_name, Toast.LENGTH_SHORT).show()
        } else {
            val activity = activity as HomeActivity

            if (from == EDIT_PLAYLIST_NAME) {
                Log.e("tag", edit_text!!.text.toString())
                activity.handleEditPlaylistName(edit_text!!.text.toString(), position, oldPlaylistName!!)

            } else if (from == CREATE_NEW_PLAYLIST) {
                activity.handleCreateNewPlaylist(edit_text!!.text.toString())
            }
            this.dismiss()
        }
    }

    private fun setCancel() {
        if (from == EDIT_PLAYLIST_NAME) {
            Toast.makeText(activity, R.string.edit_cancel, Toast.LENGTH_SHORT).show()
        } else if (from == CREATE_NEW_PLAYLIST) {
            Toast.makeText(activity, R.string.creat_cancel, Toast.LENGTH_SHORT).show()
        }
        this.dismiss()
    }

    private fun setHeader() {

    }

    override fun setup(v: View) {
        args = arguments
        if (args != null) {
            from = args!!.getInt(FROM)
            position = args!!.getInt(POSITION)
            oldPlaylistName = args!!.getString(OLD_PLAYLIST_NAME)

            if (from == EDIT_PLAYLIST_NAME) {
                header?.text = EDIT_PLAYLIST_NAME_STRING
            } else if (from == CREATE_NEW_PLAYLIST) {
                header?.text = CREATE_NEW_PLAYLIST_STRING
            }
        }
        setClickListeners(v)
    }

    private fun setClickListeners(v: View) {
        v.header.setOnClickListener { setHeader() }
        v.cancel.setOnClickListener { setCancel() }
        v.done.setOnClickListener { setDone() }
    }
}
