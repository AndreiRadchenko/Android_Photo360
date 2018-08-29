package unidesign.photo360.BackupRestore

import android.content.Context
import android.os.Build
import android.support.v7.view.ActionMode
import android.util.SparseBooleanArray
import android.view.Menu
import android.view.MenuItem
import unidesign.photo360.R

class Toolbar_ActionMode_Callback(private val context: Context, private val recyclerView_adapter: RestoreTemplateAdapter,
                                  private val message_models: List<RestoreRecyclerItem>, private val isListViewFragment: Boolean) : ActionMode.Callback {

    override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
        mode.menuInflater.inflate(R.menu.menu_restore_actoinmod, menu)         //Inflate the menu over action mode
        return true
    }

    override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean {
        //Sometimes the meu will not be visible so for that we need to set their visibility manually in this method
        //So here show action menu according to SDK Levels

        return true
    }

    override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.restore_action_delete_selection -> {
                (context as RestoreActivity).deleteRows()//delete selected rows
                mode.finish()//Finish action mode
            }
            R.id.restore_action_select_all -> {
                //Get selected ids on basis of current fragment action mode
                val selected: SparseBooleanArray
                selected = recyclerView_adapter.getSelectedIds()!!

                if (selected.size() < message_models.size) {
                    recyclerView_adapter.selectAllView(message_models.size, true)
                    //set action mode title on item selection
                    (context as RestoreActivity).mActionMode!!.setTitle(recyclerView_adapter
                            .getSelectedCount().toString() + " selected")
                } else {
                    recyclerView_adapter.removeSelection()
                    (context as RestoreActivity).mActionMode!!.finish()
                    //                    ((RestoreActivity) context).mActionMode.setTitle(String.valueOf(recyclerView_adapter
                    //                        .getSelectedCount()) + " selected");
                }
            }
        }
        return false
    }

    override fun onDestroyActionMode(mode: ActionMode) {
        //When action mode destroyed remove selected selections and set action mode to null
        //First check current fragment action mode
        recyclerView_adapter.removeSelection()         // remove selection
        (context as RestoreActivity).setNullToActionMode()       //Set action mode null
        //        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        //            Window window = ((RestoreActivity) context).getWindow();
        //            window.setStatusBarColor(context.getResources().getColor(R.color.colorPrimaryDark));
        //        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            //            StatusbarColorAnimator anim = new StatusbarColorAnimator(context,
            //                    context.getResources().getColor(R.color.select_mod_status_bar),
            //                    context.getResources().getColor(R.color.colorPrimaryDark));
            //            anim.setDuration(250).start();
            val anim = StatusbarColorAnimator(context,
                    context.getResources().getColor(R.color.select_mod_status_bar),
                    context.getResources().getColor(R.color.colorPrimaryDark))
            anim.setDuration(250).start()
        }
    }
}