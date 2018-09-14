package unidesign.photo360

import android.content.Context
import android.content.SharedPreferences

class Preset(_context: Context, page: Int) {

    internal var pref: SharedPreferences
    internal var editor: SharedPreferences.Editor
    internal val defaultSettings = Settings().getJSON().toString()
    //internal var PRIVATE_MODE = 0
    internal var mpage: Int

    init {
        pref = _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE)
        editor = pref.edit()
        mpage = page
    }

    companion object {

        // Shared preferences file name
        val PREF_NAME = "Main4_preferences"
        val PRIVATE_MODE = 0

        private val PRESET_1 = "preset1"
        private val PRESET_2 = "preset2"
        private val PRESET_3 = "preset3"
        private val PRESET_4 = "preset4"
        private val CALIBRATION = "calibration"

    }

    fun get(): String {
        var ret: String = pref.getString(PRESET_1, defaultSettings)
        when (mpage){
            0 ->  ret = pref.getString(PRESET_1, defaultSettings)
            1 ->  ret = pref.getString(PRESET_2, defaultSettings)
            2 ->  ret = pref.getString(PRESET_3, defaultSettings)
            3 ->  ret = pref.getString(PRESET_4, defaultSettings)
            4 ->  ret = pref.getString(CALIBRATION, defaultSettings)
        }
        return ret
    }

    fun set(mpreset: String) {
        when (mpage){
            0 ->  editor.putString(PRESET_1, mpreset)
            1 ->  editor.putString(PRESET_2, mpreset)
            2 ->  editor.putString(PRESET_3, mpreset)
            3 ->  editor.putString(PRESET_4, mpreset)
            4 ->  editor.putString(CALIBRATION, mpreset)
        }
            editor.commit()
        }

}