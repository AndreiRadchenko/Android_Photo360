package unidesign.photo360

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.content.Context
import android.support.v4.app.Fragment

class FragmentViewModel(var app: Application, var page: Int) : AndroidViewModel(app)  {

    var settingsPrefs = SettingsPreferences(app)
    private val postSettings = MutableLiveData<Settings>()

    fun getPreset(): LiveData<Settings> {
        return postSettings
    }


    init {
        postSettings.value = Settings(settingsPrefs.presetArray[page].get())
    }

    fun initPreferencesRequest() {
        postSettings.value = Settings(settingsPrefs.presetArray[page].get())
    }

    fun setChanges2View(framesLeft: Int) {
        var newSet = postSettings.value
        newSet!!.framesLeft = framesLeft
        postSettings.value = newSet

    }

    fun setCalibrationSettings(calibrateSettings: Settings) {
        var newSet = postSettings.value
        newSet!!.allSteps = calibrateSettings.allSteps
        newSet!!.state = calibrateSettings.state
        postSettings.value = newSet

    }

    companion object {
        val CALIBRATION_MOD = 4
    }

}
