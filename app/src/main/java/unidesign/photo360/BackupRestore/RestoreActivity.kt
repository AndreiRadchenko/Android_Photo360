package unidesign.photo360.BackupRestore

import android.content.ContentValues
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.support.v7.app.AppCompatActivity
import android.support.v7.view.ActionMode
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.nio.channels.FileChannel

import unidesign.photo360.R

import java.util.ArrayList
import java.util.Collections
import java.util.Comparator
import kotlin.text.Charsets.UTF_8


/**
 * Created by United on 12/26/2017.
 */

class RestoreActivity : AppCompatActivity(), RestoreTask.AsyncResponse {

    private var recyclerView: RecyclerView? = null
    private var mLayoutManager: RecyclerView.LayoutManager? = null
    lateinit var mAdapter: RestoreTemplateAdapter
    internal var values = ContentValues()
    var listItems: MutableList<RestoreRecyclerItem> = ArrayList<RestoreRecyclerItem>()

    internal var myFileArray: MutableList<String> = ArrayList()
    internal var BackupName = ""
    lateinit internal var sdPath: File
    var mActionMode: ActionMode? = null
    lateinit var AsyncRestore: RestoreTask
    lateinit internal var myToolbar: Toolbar

    //val DIR_SD: String = getString(R.string.app_name)
    val DIR_SD: String = "Photo360"
    //val DIR_SD: String = getString(R.string.app_name)

    /* Checks if external storage is available for read and write */
    val isExternalStorageWritable: Boolean
        get() {
            val state = Environment.getExternalStorageState()
            return if (Environment.MEDIA_MOUNTED == state) {
                true
            } else false
        }

    override fun onCreate(savedInstanceState: Bundle?) {

        // Use the chosen theme
        val sharedPrefs = android.preference.PreferenceManager.getDefaultSharedPreferences(this)
/*        val useDarkTheme = sharedPrefs.getBoolean(pref_items.pref_DarkTheme, false)

        if (useDarkTheme) {
            setTheme(R.style.Theme_Dark)
        }*/

        super.onCreate(savedInstanceState)
        setContentView(R.layout.restore_activity)

        myToolbar = findViewById<View>(R.id.restore_toolbar) as Toolbar
        setSupportActionBar(myToolbar)
        // Get a support ActionBar corresponding to this toolbar
        val ab = supportActionBar

        // Enable the Up button
        ab!!.setDisplayHomeAsUpEnabled(true)

        recyclerView = findViewById<View>(R.id.restore_layout) as RecyclerView

        mLayoutManager = LinearLayoutManager(this)

        val llm = LinearLayoutManager(this)
        recyclerView!!.layoutManager = llm
        recyclerView!!.setHasFixedSize(true)

        myFileArray.clear()

        sdPath = File(Environment.getExternalStorageDirectory().toString()
                + File.separator + DIR_SD)
        if (!sdPath.exists()) {
            // ñîçäàåì êàòàëîã
            myFileArray.add("Nothing to restore")
        } else {
            val files = sdPath.list()
            val filesLength = files!!.size
            for (i in 0 until filesLength) {
                val Backupitem = getBackup(files, files[i])
                if (!Backupitem.name.equals(""))
                    listItems.add(Backupitem)
            }
        }

        //Log.d(LOG_TAG, "initializeData(): "+ listItems.get(0).getTemplatename());
        val RA = this

        Collections.sort(listItems, Comparator<RestoreRecyclerItem>
                { obj1, obj2 -> obj1.name.toString().compareTo(obj2.name.toString(), true) })

        mAdapter = RestoreTemplateAdapter(this, listItems, object : RestoreTemplateAdapter.OnItemClickListener {
            override fun onItemClick(item: RestoreRecyclerItem) {
                try {
                    AsyncRestore = RestoreTask(applicationContext, RA)

                } catch (e: Exception) {
                    throw RuntimeException(e)
                }
//                Log.d("OnItemClickListener","OnItemClickListener item = " + item)
//                Log.d("OnItemClickListener","OnItemClickListener item.filepath = " + item.filepath)
                AsyncRestore.execute(item.filepath)
            }
        })

        recyclerView!!.adapter = mAdapter
        //touchHelper.attachToRecyclerView(recyclerView);
        implementRecyclerViewClickListeners()

    }

    override fun onOptionsItemSelected(menuItem: MenuItem): Boolean {
        if (menuItem.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(menuItem)
    }

    override fun onBackPressed() {
        super.onBackPressed()
    }

    internal fun getBackup(files: Array<String>, file: String): RestoreRecyclerItem {
        val mBackup = RestoreRecyclerItem("", "", "")

        var delims = "[_.]"
        var tokens = file.split(delims.toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

        val json = loadJSONFromFile(sdPath.toString() + File.separator + file)

        try {
            val obj = JSONObject(json)
            mBackup.comment = obj.getString("comment")
            mBackup.name = obj.getString("filename")
            mBackup.filepath = sdPath.toString() + File.separator + file

            } catch (e: JSONException) {
                    e.printStackTrace()
            }

        return mBackup
    }

    fun loadJSONFromFile(sourceFile: String): String? {
        var json: String? = null
        try {
            val inpStream = FileInputStream(sourceFile)
            val size = inpStream.available()
            val buffer = ByteArray(size)
            inpStream.read(buffer)
            inpStream.close()
            json = String(buffer, UTF_8)
        } catch (ex: IOException) {
            ex.printStackTrace()
            return null
        }

        return json
    }

    //Implement item click and long click over recycler view
    private fun implementRecyclerViewClickListeners() {
        recyclerView!!.addOnItemTouchListener(RecyclerTouchListener(this, recyclerView!!,
                object : RecyclerClick_Listener {
                    override fun onClick(view: View, position: Int) {
                        //If ActionMode not null select item
                        if (mActionMode != null)
                            onListItemSelect(position)
                        Log.d("RecyclerClick_Listener","RecyclerClick_Listener")
                    }

                    override fun onLongClick(view: View, position: Int) {
                        //Select item on long click
                        onListItemSelect(position)
                    }
                }))
    }

    //List item select method
    private fun onListItemSelect(position: Int) {
        mAdapter.toggleSelection(position)//Toggle the selection

        val hasCheckedItems = mAdapter.getSelectedCount() > 0//Check if any items are already selected or not


        if (hasCheckedItems && mActionMode == null) {
            // there are some selected items, start the actionMode
            mActionMode = startSupportActionMode(Toolbar_ActionMode_Callback(this,
                    mAdapter, listItems, false))
            //myToolbar.setVisibility(View.INVISIBLE);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                //                StatusbarColorAnimator anim = new StatusbarColorAnimator(this,
                //                        getResources().getColor(R.color.colorPrimaryDark),
                //                        getResources().getColor(R.color.select_mod_status_bar));
                //                anim.setDuration(250).start();
                val anim = StatusbarColorAnimator(this,
                        resources.getColor(R.color.colorPrimary),
                        resources.getColor(R.color.select_mod_status_bar))
                anim.setDuration(250).start()
            }
        } else if (!hasCheckedItems && mActionMode != null) {
            // there no selected items, finish the actionMode
            mActionMode!!.finish()
            //myToolbar.setVisibility(View.VISIBLE);
        }

        if (mActionMode != null)
        //set action mode title on item selection
            mActionMode!!.title = getString(R.string.restore_select_title) + " " + mAdapter
                    .getSelectedCount()


    }

    fun setNullToActionMode() {
        if (mActionMode != null)
            mActionMode = null
    }

    //Delete selected rows
    fun deleteRows() {
        val selected = mAdapter
                .getSelectedIds()//Get selected ids
        var file4delete: File

        //Loop all selected ids
        for (i in selected!!.size() - 1 downTo 0) {
            if (selected.valueAt(i)) {
                //If current id is selected remove the item via key
                file4delete = File(listItems[selected.keyAt(i)].filepath)

                if (file4delete.exists())
                    file4delete.delete()
                //                deleteFile(listItems.get(selected.keyAt(i)).getSMS_file_path());
                //                deleteFile(listItems.get(selected.keyAt(i)).getUSSD_file_path());
                listItems.removeAt(selected.keyAt(i))
                mAdapter.notifyDataSetChanged()//notify adapter

            }
        }
        //selected.size()
        Toast.makeText(this, resources.getQuantityString(
                R.plurals.Deleted_backup_message, selected.size(), selected.size()), Toast.LENGTH_SHORT).show()//Show Toast
        mActionMode!!.finish()//Finish action mode after use

    }

    override fun processFinish(backup_name: String) {
        val greetingText = String.format(resources.getString(R.string.BackupRestoredMessage), backup_name)
        Toast.makeText(this, greetingText, Toast.LENGTH_LONG).show()


    }

    fun copyFileOrDirectory(srcDir: String, destDir: String) {
        try {
            val src = File(srcDir)
            val dst = File(destDir, src.name)

            if (src.isDirectory) {
                val files = src.list()
                val filesLength = files!!.size
                for (i in 0 until filesLength) {
                    val src1 = File(src, files[i]).path
                    val dst1 = dst.path
                    copyFileOrDirectory(src1, dst1)
                }
            } else {
                copyFile(src, dst)
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    @Throws(IOException::class)
    fun copyFile(sourceFile: File, destFile: File) {
        if (!destFile.parentFile.exists())
            destFile.parentFile.mkdirs()

        if (!destFile.exists())
            destFile.createNewFile()

        var source: FileChannel? = null
        var destination: FileChannel? = null

        try {
            source = FileInputStream(sourceFile).channel
            destination = FileOutputStream(destFile).channel
            destination!!.transferFrom(source, 0, source!!.size())
        } finally {
            source?.close()
            destination?.close()
        }
    }

    companion object {


        internal val LOG_TAG = "RestoreTemplateActivity"
    }

}
