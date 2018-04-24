package unidesign.photo360


import android.content.Context
import android.content.SharedPreferences

class PreferenceManager(internal var _context: Context) {

    internal var pref: SharedPreferences
    internal var editor: SharedPreferences.Editor

    // shared pref mode
    internal var PRIVATE_MODE = 0

    var firmwareVersion: String
        get() = pref.getString(FIRMWARE_VERSION, "Photo360 AP")
        set(mfirmwareVersion) {
            editor.putString(FIRMWARE_VERSION, mfirmwareVersion)
            editor.commit()
        }

    var wifiSsid: String
        get() = pref.getString(WIFI_SSID, "Photo360")
        set(mwifiSsid) {
            editor.putString(WIFI_SSID, mwifiSsid)
            editor.commit()
        }

    init {
        pref = _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE)
        editor = pref.edit()
    }

    companion object {

        // Shared preferences file name
        private val PREF_NAME = "Photo360_Preferences"
        private val FIRMWARE_VERSION = "firmwareVersion"
        private val WIFI_SSID = "wifiSsid"
        private val WIFI_PASSWORD = "wifiPassword"
        private val WS_PORT = "wsPort"
        private val STATE = "state"
        private val FRAMES_LEFT = "framesLeft"
        private val FRAME = "frame"
        private val ALL_STEPS = "allSteps"
        private val PAUSE = "pause"
        private val DELAY = "delay"
        private val SPEED = "speed"
        private val ACCELERATION = "acceleration"
        private val SHOOTING_MODE = "shootingMode"
        private val DIRECTION = "direction"
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