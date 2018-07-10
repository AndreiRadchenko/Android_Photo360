package unidesign.photo360

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONException
import org.json.JSONObject

class SettingsPreferences(_context: Context) {

    var mcontext: Context// = _context

    init  {
        mcontext = _context
    }

    var presetArray = arrayOf(Preset(mcontext, PRESET_1), Preset(mcontext, PRESET_2), Preset(mcontext, PRESET_3),
            Preset(mcontext, PRESET_4), Preset(mcontext, CALIBRATION))

    fun setChanges(presetNum: Int, settings: Settings){
        presetArray[presetNum].set(settings.getJSON().toString())
    }

    fun saveSettings(set: Settings) {

        for (i in presetArray.indices){
            presetArray[i].set( saveSettingsInPreset(set, presetArray[i].get()) )
        }
    }

    fun saveSettingsInPreset(set: Settings, preset: String): String {

        var newSettings = Settings(preset)
        newSettings.wifiSsid = set.wifiSsid
        newSettings.wifiPassword = set.wifiPassword
        newSettings.allSteps = set.allSteps

        return newSettings.getJSON().toString()
    }

//    init {
//
//
//    }
    companion object {
        private val PRESET_1 = 0
        private val PRESET_2 = 1
        private val PRESET_3 = 2
        private val PRESET_4 = 3
        private val CALIBRATION = 4
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