package unidesign.photo360.BackupRestore

import android.content.Context
import android.database.Cursor
import android.os.AsyncTask
import android.os.Environment
import android.util.Log
import android.widget.Toast
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import unidesign.photo360.R
import unidesign.photo360.SettingsPreferences
import java.io.*
import java.nio.channels.FileChannel
import java.util.ArrayList

/**
 * Created by United on 12/8/2017.
 */

class BackupTask(private val mContext: Context) : AsyncTask<String, Int, String>() {

    internal var backupFile = "backup"
    var settingsPrefs = SettingsPreferences(mContext)
    val DIR_SD: String = mContext.getString(R.string.app_name)

    override fun onPreExecute() {
        super.onPreExecute()
    }

    override fun doInBackground(vararg params: String): String? {

        val myName = params[0]
        val myComment = params[1]

        if (Environment.getExternalStorageState() != Environment.MEDIA_MOUNTED) {
            Toast.makeText(mContext, R.string.no_sdcard, Toast.LENGTH_LONG).show()
            return null
        }

        val sdPath = File(Environment.getExternalStorageDirectory().toString()
                + File.separator + DIR_SD)
        if (!sdPath.exists()) {
            sdPath.mkdirs()
        }

        backupFile = "$myName.json"
        val BackupPath = File(sdPath, backupFile)
        if (BackupPath.exists())
            BackupPath.delete()
        //Log.d("doInBackground: ", String.valueOf(ussdsdFile));

        var fos: FileOutputStream? = null

        try {
            BackupPath.createNewFile()
            fos = FileOutputStream(BackupPath, true)
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        val ps = PrintStream(fos!!)

        ps.append(preferances2JSON(myName, myComment).toString())

        return myName
    }

    override fun onPostExecute(result: String?) {
        super.onPostExecute(result)
        if (result != null) {
            //            Toast.makeText(mContext, mContext.getResources().getString(R.string.Backup_sucsess, result),
            //                    Toast.LENGTH_LONG).show();
            val greetingText = String.format(mContext.resources.getString(R.string.Backup_sucsess), result)
            Toast.makeText(mContext, greetingText, Toast.LENGTH_LONG).show()
        }
    }


    fun preferances2JSON(name: String, comment: String): JSONObject {

        val tableObject = JSONObject()
        val presetArray = JSONArray()

        //var i = 0
        for (i in settingsPrefs.presetArray.indices) {
            val rObject = JSONObject()
            try {
                rObject.put("preset$i", settingsPrefs.presetArray[i].get())
                presetArray.put(i, rObject)
            } catch (e: JSONException) {
                e.printStackTrace()
            }

        }

        try {
            tableObject.put("filename", name)
            tableObject.put("comment", comment)
            //tableObject.put("data1", "dbUSSDKyivstar");
            tableObject.put("presets", presetArray)
        } catch (e: JSONException) {
            e.printStackTrace()
        }

        return tableObject
    }

}
