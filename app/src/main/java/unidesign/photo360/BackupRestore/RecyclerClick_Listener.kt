package unidesign.photo360.BackupRestore

import android.view.View

/**
 * Created by United on 12/27/2017.
 */

interface RecyclerClick_Listener {
    /**
     * Interface for Recycler View Click listener
     */

    fun onClick(view: View, position: Int)

    fun onLongClick(view: View, position: Int)
}
