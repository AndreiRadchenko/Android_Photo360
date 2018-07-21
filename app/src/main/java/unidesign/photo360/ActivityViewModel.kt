package unidesign.photo360

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData

class ActivityViewModel(var app: Application) : AndroidViewModel( app) {

    //private val pref = SettingsPreferences(app)
    private var runningFragment: Int = 0
    private val isTurntableRunning = MutableLiveData<Boolean>()
    private val postSettings = MutableLiveData<Settings>()

    fun getSettings(): LiveData<Settings>{
        return postSettings
    }

    fun setSettings(set: Settings){
        postSettings.value = set
    }

    fun setRunningFragment(frag: Int){
        runningFragment = frag
    }

    fun getRunningFragment(): Int {
        return runningFragment
    }

    fun setTtRun(mrun: Boolean){
        isTurntableRunning.value = mrun
    }

/*    @Subscribe(threadMode = ThreadMode.MAIN)
    public fun onMessage(event: wsMessage){
*//*
        Log.d("AcViewModel onMessage()", Settings.FRAMES_LEFT + ": " + postSettings.value?.framesLeft)
        Log.d("AcViewModel onMessage()", Settings.STATE + ": " + postSettings.value?.state)
*//*
        var newSettings = Settings(postSettings.value?.getJSON().toString())
        newSettings.framesLeft = event.framesLeft
        newSettings.state = event.state
        postSettings.value = newSettings
    }*/

    init {
       // EventBus.getDefault().register(this)
    }

    override protected fun onCleared() {
        //EventBus.getDefault().unregister(this)
    }

    fun refreshWith(postSettings: Settings) {

    }

}