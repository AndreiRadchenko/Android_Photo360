package unidesign.photo360

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.content.Context

class AppViewModel(var app: Application) : AndroidViewModel( app) {

    private val pref = SettingsPreferences(app)
    private val json_string_preset1 = MutableLiveData<String>()
    private val json_string_preset2 = MutableLiveData<String>()
    private val json_string_preset3 = MutableLiveData<String>()
    private val json_string_preset4 = MutableLiveData<String>()

    fun initPreferencesRequest() {
        /* expensive operation, e.g. network request */
        json_string_preset1.value = pref.preset1
        json_string_preset2.value = pref.preset2
        json_string_preset3.value = pref.preset3
        json_string_preset4.value = pref.preset4
    }

    fun getPreset1(): LiveData<String> {
        return json_string_preset1
    }

    fun getPreset2(): LiveData<String> {
        return json_string_preset2
    }

    fun getPreset3(): LiveData<String> {
        return json_string_preset3
    }

    fun getPreset4(): LiveData<String> {
        return json_string_preset4
    }

}