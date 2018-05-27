package unidesign.photo360

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.content.Context
import android.util.Log
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class ActivityViewModel(var app: Application) : AndroidViewModel( app) {

    //private val pref = SettingsPreferences(app)
    private val runnigFragment = MutableLiveData<Int>()
    private val isTurntableRunning = MutableLiveData<Boolean>()
    private val postSettings = MutableLiveData<Settings>()

    fun getSettings(): LiveData<Settings>{
        return postSettings
    }

    fun setSettings(set: Settings){
        postSettings.value = set
    }

    fun isTtRun(): LiveData<Boolean>{
        return isTurntableRunning
    }

    fun setTtRun(mrun: Boolean){
        isTurntableRunning.value = mrun
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public fun onMessage(event: wsMessage){
/*
        Log.d("AcViewModel onMessage()", Settings.FRAMES_LEFT + ": " + postSettings.value?.framesLeft)
        Log.d("AcViewModel onMessage()", Settings.STATE + ": " + postSettings.value?.state)
*/
        var newSettings = Settings(postSettings.value?.getJSON().toString())
        newSettings.framesLeft = event.framesLeft
        newSettings.state = event.state
        postSettings.value = newSettings
    }

    init {
        EventBus.getDefault().register(this)
    }

    override protected fun onCleared() {
        EventBus.getDefault().unregister(this)
    }

}