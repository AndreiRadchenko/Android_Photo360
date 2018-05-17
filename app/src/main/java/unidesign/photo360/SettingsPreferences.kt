package unidesign.photo360

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONException
import org.json.JSONObject

class SettingsPreferences(internal var _context: Context) {

    internal var pref: SharedPreferences
    internal var editor: SharedPreferences.Editor
    internal val defaultSettings = Settings().getJSON().toString()

    // shared pref mode
    internal var PRIVATE_MODE = 0

    var preset1: String
        get() = pref.getString(PRESET_1, defaultSettings)
        set(mpreset) {
            editor.putString(PRESET_1, mpreset)
            editor.commit()
        }

    var preset2: String
        get() = pref.getString(PRESET_2, defaultSettings)
        set(mpreset) {
            editor.putString(PRESET_2, mpreset)
            editor.commit()
        }

    var preset3: String
        get() = pref.getString(PRESET_3, defaultSettings)
        set(mpreset) {
            editor.putString(PRESET_3, mpreset)
            editor.commit()
        }

    var preset4: String
        get() = pref.getString(PRESET_4, defaultSettings)
        set(mpreset) {
            editor.putString(PRESET_4, mpreset)
            editor.commit()
        }

    init {
        pref = _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE)
        editor = pref.edit()
    }

    companion object {

        // Shared preferences file name
        private val PREF_NAME = "Main4_preferences"

        private val PRESET_1 = "preset1"
        private val PRESET_2 = "preset2"
        private val PRESET_3 = "preset3"
        private val PRESET_4 = "preset4"

    }
}

/*var config = {
  firmwareVersion: 'PhotoPizza AP',
  wifiSsid: 'Photo360',
  wifiPassword: '',
  wsPort: 8000,
  state: 'waiting',//started
  framesLeft: 36,
  frame: 36,
  allSteps: 109000,
  pause: 100,
  delay: 300,
  speed: 3000,
  acceleration: 100,
  shootingMode: 'inter',
  direction: 1
};
*/