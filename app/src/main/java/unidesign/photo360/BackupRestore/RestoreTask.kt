package unidesign.photo360.BackupRestore

import android.app.ProgressDialog
import android.content.ContentValues
import android.content.Context
import android.os.AsyncTask
import android.util.Log
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import unidesign.photo360.SettingsPreferences
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.InputStreamReader


/**
 * Created by United on 12/29/2017.
 */

class RestoreTask(private val mContext: Context, var RA: RestoreActivity) : AsyncTask<String, Void, String>() {

    var pDialog: ProgressDialog? = null
    var settingsPrefs = SettingsPreferences(mContext)
    var listener: RestoreTask.AsyncResponse? = null//Call back interface
    var ImageName: String? = null

    internal var reader: BufferedReader? = null
    internal var resultJson = ""

    interface AsyncResponse {
        fun processFinish(backup_name: String)
    }


    init {
        listener = RA
        //        delegate = asyncResponse;//Assigning call back interfacethrough constructor
    }

    /** progress dialog to show user that the backup is processing.  */
    /**
     * application context.
     */
    override fun onPreExecute() {
        super.onPreExecute()

        /*        pDialog = new ProgressDialog(ITA);
        pDialog.setMessage(mContext.getString(R.string.LoadDialogText));
        pDialog.setCancelable(true);
        pDialog.show();*/
    }

    override fun doInBackground(vararg params: String): String {

        val filepath = params[0]
        //        Log.d(LOG_TAG, "-- ImageName --" + ImageName);
        // получаем данные с карты памяти
        Log.d("RestoreTask", "item.filepath $filepath" )
        var RestoreJson = getStringFromFile(filepath)

        restoreJSON2settingsPref(RestoreJson)

        return filepath
    }

    override fun onPostExecute(strJson: String) {
        super.onPostExecute(strJson)
        listener!!.processFinish(strJson)
        //RN_USSD.closeDriwer = true;
        RA.finish()
    }

    internal fun getStringFromFile(path: String): String {

        var line: String
        var line1 = ""

        try {
            val presetfile = File(path)
            val instream = FileInputStream(presetfile)
            //InputStream instream = openFileInput("E:\\test\\src\\com\\test\\mani.txt");
            if (instream != null) {
                val inputreader = InputStreamReader(instream)
                val buffreader = BufferedReader(inputreader)

                try {
                    line = buffreader.readLine()
                    while (line != null) {
                        line1 += line
                        Log.d("getStringFromFile", "line1 = " + line1)
                        line = buffreader.readLine()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                buffreader.close()
            }
            instream.close()
        } catch (e: Exception) {

        }

        return line1
    }

    internal fun restoreJSON2settingsPref(jsonstr: String) {

        var dataJsonObj: JSONObject? = null
        var datafild: String? = null

        var tableObject = JSONObject()
        var presetArray = JSONArray()

        val values = ContentValues()

        try {
            //Log.d("restorJSON2settingsPref", "jsonstr $jsonstr")
            dataJsonObj = JSONObject(jsonstr)
            presetArray = dataJsonObj.getJSONArray("presets")
            //Log.d(LOG_TAG, "onPostExecute, data_fild: "+ data_fild);

            //var i = 0
            for (i in settingsPrefs.presetArray.indices) {

                try {
                    tableObject = presetArray.getJSONObject(i)
                    Log.d("restorJSON2settingsPref", "tableObject = " + tableObject.getString("preset$i"))
                    settingsPrefs.presetArray[i].set(tableObject.getString("preset$i"))
                } catch (e: JSONException) {
                    e.printStackTrace()
                }

            }

        } catch (e: JSONException) {
            e.printStackTrace()
        }

    }

    companion object {

        var LOG_TAG = "RestoreTask"
    }

}
