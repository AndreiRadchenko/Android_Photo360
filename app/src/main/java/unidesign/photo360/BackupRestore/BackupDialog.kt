package unidesign.photo360.BackupRestore

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog
import android.util.Log
import android.widget.EditText
import unidesign.photo360.R

/**
 * Created by United on 12/20/2017.
 */

class BackupDialog : DialogFragment() {

    // Use this instance of the interface to deliver action events
    lateinit internal var mListener: NoticeDialogListener

    /* The activity that creates an instance of this dialog fragment must
* implement this interface in order to receive event callbacks.
* Each method passes the DialogFragment in case the host needs to query it. */
    interface NoticeDialogListener {
        fun onDialogPositiveClick(dialog: DialogFragment, name: String, comment: String)
        fun onDialogNegativeClick(dialog: DialogFragment)
    }

    // Override the Fragment.onAttach() method to instantiate the NoticeDialogListener
    override fun onAttach(_context: Context?) {
        super.onAttach(_context)
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            mListener = _context as NoticeDialogListener
        } catch (e: ClassCastException) {
            // The activity doesn't implement the interface, throw exception
            throw ClassCastException(activity!!.toString() + " must implement NoticeDialogListener")
        }

    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val builder = AlertDialog.Builder(activity!!)
        // Get the layout inflater
        val inflater = activity!!.layoutInflater
//        val backup_name = arguments!!.getString("backup_name", "template_yy-MM-dd_HH-mm")
//        val time_name = getBackupName(backup_name)
        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout

        val dialogView = inflater.inflate(R.layout.dialog_backup, null)
        val eComment = dialogView.findViewById(R.id.backup_comment) as EditText
        val eName = dialogView.findViewById(R.id.backup_name) as EditText

        builder.setView(dialogView)

        builder.setTitle(resources.getString(R.string.backupDialogTitle))
                //.setMessage(time_name)
                // Add action buttons
                .setPositiveButton(android.R.string.ok) { dialog, id ->
                    // sign in the user ...
                    //RN_USSD.drawer.closeDrawer(Gravity.LEFT, false);
                    mListener.onDialogPositiveClick(this@BackupDialog, eName.text.toString(),
                            eComment.text.toString())
                }
                .setNegativeButton(android.R.string.cancel) { dialog, id -> mListener.onDialogNegativeClick(this@BackupDialog) }
        return builder.create()
    }

    companion object {

    }

}
