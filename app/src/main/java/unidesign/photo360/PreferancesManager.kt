package unidesign.photo360


import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

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

    var wifiPassword: String
        get() = pref.getString(WIFI_PASSWORD, "")
        set(mwifiPassword) {
            editor.putString(WIFI_PASSWORD, mwifiPassword)
            editor.commit()
        }

    var wsPort: Int
        get() = pref.getInt(WS_PORT, 8000)
        set(mwsPort) {
            editor.putInt(WS_PORT, mwsPort)
            editor.commit()
        }

    var state: String
        get() = pref.getString(STATE, "waiting")
        set(mstate) {
            editor.putString(STATE, mstate)
            editor.commit()
        }

    var framesLeft: Int
        get() = pref.getInt(FRAMES_LEFT, 36)
        set(mframesLeft) {
            editor.putInt(FRAMES_LEFT, mframesLeft)
            editor.commit()
        }

    var frame: Int
        get() = pref.getInt(FRAME, 36)
        set(mframe) {
            editor.putInt(FRAME, mframe)
            editor.commit()
        }

    var allSteps: Int
        get() = pref.getInt(ALL_STEPS, 109000)
        set(mallSteps) {
            editor.putInt(ALL_STEPS, mallSteps)
            editor.commit()
        }

    var pause: Int
        get() = pref.getInt(PAUSE, 100)
        set(mpause) {
            editor.putInt(PAUSE, mpause)
            editor.commit()
        }

    var delay: Int
        get() = pref.getInt(DELAY, 300)
        set(mdelay) {
            editor.putInt(DELAY, mdelay)
            editor.commit()
        }

    var speed: Int
        get() = pref.getInt(SPEED, 3000)
        set(mspeed) {
            editor.putInt(SPEED, mspeed)
            editor.commit()
        }

    var acceleration: Int
        get() = pref.getInt(ACCELERATION, 100)
        set(macceleration) {
            editor.putInt(ACCELERATION, macceleration)
            editor.commit()
        }

    var shootingMode: String
        get() = pref.getString(SHOOTING_MODE, "inter")
        set(mshootingMode) {
            editor.putString(SHOOTING_MODE, mshootingMode)
            editor.commit()
        }

    var direction: Int
        get() = pref.getInt(DIRECTION, 1)
        set(mdirection) {
            editor.putInt(DIRECTION, mdirection)
            editor.commit()
        }

    init {
        pref = _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE)
        editor = pref.edit()
    }

    fun getJSON(): JSONObject {
        val rjsonObject = JSONObject()
            try {
                rjsonObject.put(FIRMWARE_VERSION, this.firmwareVersion)
                rjsonObject.put(WIFI_SSID, this.wifiSsid)
                rjsonObject.put(WIFI_PASSWORD, this.wifiPassword)
                rjsonObject.put(WS_PORT, this.wsPort)
                rjsonObject.put(STATE, this.state)
                rjsonObject.put(FRAMES_LEFT, this.framesLeft)
                rjsonObject.put(FRAME, this.frame)
                rjsonObject.put(ALL_STEPS, this.allSteps)
                rjsonObject.put(PAUSE, this.pause)
                rjsonObject.put(DELAY, this.delay)
                rjsonObject.put(SPEED, this.speed)
                rjsonObject.put(ACCELERATION, this.acceleration)
                rjsonObject.put(SHOOTING_MODE, this.shootingMode)
                rjsonObject.put(DIRECTION, this.direction)
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        return rjsonObject;
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