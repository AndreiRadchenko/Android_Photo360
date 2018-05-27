package unidesign.photo360

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONException
import org.json.JSONObject

class Settings() {

    companion object {

        //private val PRESET_FRAGMENT = "presetFragment"
        private val PRESET_NAME = "presetName"
        private val FIRMWARE_VERSION = "firmwareVersion"
        private val WIFI_SSID = "wifiSsid"
        private val WIFI_PASSWORD = "wifiPassword"
        private val WS_PORT = "wsPort"
        public val STATE = "state"
        public val FRAMES_LEFT = "framesLeft"
        private val FRAME = "frame"
        private val ALL_STEPS = "allSteps"
        private val PAUSE = "pause"
        private val DELAY = "delay"
        private val SPEED = "speed"
        private val ACCELERATION = "acceleration"
        private val SHOOTING_MODE = "shootingMode"
        private val DIRECTION = "direction"
    }

    //var presetFragment: Int

    var presetName: String

    var firmwareVersion: String

    var wifiSsid: String

    var wifiPassword: String

    var wsPort: Int

    var state: String

    var framesLeft: Int

    var frame: Int

    var allSteps: Int

    var pause: Int

    var delay: Int

    var speed: Int

    var acceleration: Int

    var shootingMode: String

    var direction: Int

    fun getJSON(): JSONObject {
        val rjsonObject = JSONObject()
        try {
            //rjsonObject.put(PRESET_FRAGMENT, this.presetFragment)
            rjsonObject.put(PRESET_NAME, this.presetName)
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

    init {
        //presetFragment = 0
        presetName = "Preset 1"
        firmwareVersion= "PhotoPizza AP"
        wifiSsid = "Photo360"
        wifiPassword = ""
        wsPort = 8000
        state = "waiting"
        framesLeft = 36
        frame = 36
        allSteps = 109000
        pause = 100
        delay = 300
        speed = 3000
        acceleration = 100
        shootingMode = "inter"
        direction = 1
    }

    constructor(initJsonString: String) : this(){
        var initJsonObj = JSONObject(initJsonString)

        //presetFragment = initJsonObj.getInt(PRESET_FRAGMENT)
        presetName = initJsonObj.getString(PRESET_NAME)
        firmwareVersion = initJsonObj.getString(FIRMWARE_VERSION)
        wifiSsid = initJsonObj.getString(WIFI_SSID)
        wifiPassword = initJsonObj.getString(WIFI_PASSWORD)
        wsPort = initJsonObj.getInt(WS_PORT)
        state = initJsonObj.getString(STATE)
        framesLeft = initJsonObj.getInt(FRAMES_LEFT)
        frame = initJsonObj.getInt(FRAME)
        allSteps = initJsonObj.getInt(ALL_STEPS)
        pause = initJsonObj.getInt(PAUSE)
        delay = initJsonObj.getInt(DELAY)
        speed = initJsonObj.getInt(SPEED)
        acceleration = initJsonObj.getInt(ACCELERATION)
        shootingMode = initJsonObj.getString(SHOOTING_MODE)
        direction = initJsonObj.getInt(DIRECTION)
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