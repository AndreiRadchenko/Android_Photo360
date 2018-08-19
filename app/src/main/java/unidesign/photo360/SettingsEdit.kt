package unidesign.photo360

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class SettingsEdit : AppCompatActivity() {

    lateinit var etSSID: EditText
    lateinit var etPassword: EditText
    lateinit var et_allSteps: EditText
    lateinit var et_callSpeed: EditText
    lateinit var txtCalibrExplanation: TextView
    lateinit var calibrate_button: Button
    lateinit var settingsPrefs: SettingsPreferences
    lateinit var viewModel: FragmentViewModel
    var oldSet = Settings()
    var page: Int = FragmentViewModel.CALIBRATION_MOD

    companion object {
        var calibrateSettings = Settings()
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)

        //sharedPrefs = PreferenceManager(applicationContext)
        settingsPrefs = SettingsPreferences(applicationContext)
        val myToolbar = findViewById<View>(R.id.settings_toolbar) as Toolbar
        setSupportActionBar(myToolbar)
        // Get a support ActionBar corresponding to this toolbar
        val ab = supportActionBar

        // Enable the Up button
        ab!!.setDisplayHomeAsUpEnabled(true)

        etSSID = findViewById<View>(R.id.etSSID) as EditText
        etPassword = findViewById<View>(R.id.etPassword) as EditText
        et_allSteps = findViewById<View>(R.id.et_allSteps) as EditText
        et_callSpeed = findViewById<View>(R.id.et_callSpeed) as EditText
        txtCalibrExplanation = findViewById<View>(R.id.txtCalibrExplanation) as TextView
        calibrate_button = findViewById<View>(R.id.calibrate_button) as Button
        calibrate_button.isEnabled = false

        //viewModel = ViewModelProviders.of(this).get(FragmentViewModel::class.java)
        viewModel = ViewModelProviders.of(this, CustomViewModelFactory(this!!.application, 4)).
                get(FragmentViewModel::class.java)

        viewModel.getPreset().
                observe(this, Observer<Settings> { set -> displaySettings(set!!) })

        viewModel.initPreferencesRequest()

        calibrate_button.setOnClickListener(View.OnClickListener {

            if (calibrateSettings?.state == "waiting" || calibrateSettings?.state == "stop"){
                //var calibrateSettings = Settings()
                calibrateSettings.direction = 1
                calibrateSettings.allSteps = -1 //calibration mod
                calibrateSettings.speed = et_callSpeed.text.toString().toInt()
                calibrateSettings.state = "start"
//                calibrate_button.setText(R.string.stop_calibrate_btn)

                try {
                    MainActivity.mWebSocketClient!!.send(calibrateSettings.getJSON().toString())
                }
                catch (e: Exception) {
                    Toast.makeText(application, "Turnable not connected", Toast.LENGTH_SHORT).show()
                    calibrate_button.isEnabled = false
                }
            }
            else {
                calibrate_button.setText(R.string.start_calibrate_btn)
                calibrateSettings.state = "stop"
                try {
                    MainActivity.mWebSocketClient!!.send(calibrateSettings.getJSON().toString())
                }
                catch (e: Exception) {
                    Toast.makeText(application, "Turnable not connected", Toast.LENGTH_SHORT).show()
                    calibrate_button.isEnabled = false
                }
            }

        })
    }

    override fun onResume() {
        super.onResume()
        if (MainActivity.wsConnected)
            calibrate_button.isEnabled = true
    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        EventBus.getDefault().unregister(this)
        super.onStop()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public fun onMessage(event: wsMessage){
/*
        Log.d("AcViewModel onMessage()", Settings.FRAMES_LEFT + ": " + postSettings.value?.framesLeft)
        Log.d("AcViewModel onMessage()", Settings.STATE + ": " + postSettings.value?.state)
*/
        calibrateSettings.allSteps = event.allSteps
        calibrateSettings.state = event.state
            viewModel.setCalibrationSettings(calibrateSettings)
    }

    fun displaySettings (mSettings: Settings){

        etSSID.setText(mSettings.wifiSsid)
        etPassword.setText(mSettings.wifiPassword)
        et_allSteps.setText(mSettings.allSteps.toString())
        et_callSpeed.setText(mSettings.speed.toString())
        if (calibrateSettings?.state == "waiting" || calibrateSettings?.state == "stop")
            calibrate_button.setText(R.string.start_calibrate_btn)
        else
            calibrate_button.setText(R.string.stop_calibrate_btn)

        //= sharedPrefs.shootingMode
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.settings_edit_menu, menu);
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_save_settings -> {
                Log.d("In action Save", "allSteps = " + et_allSteps.text)
                var set = Settings()
                set.wifiSsid = etSSID.text.toString()
                set.wifiPassword = etPassword.text.toString()
                set.allSteps = et_allSteps.text.toString().toInt()
                set.speed = et_callSpeed.text.toString().toInt()

                settingsPrefs.saveSettings(set)
                settingsPrefs.setChanges(4, set)
/*                for (p in 0..4){
                    oldSet = Settings(viewModel.getPreset(p).value!!)
                    oldSet.wifiSsid = etSSID.text.toString()
                    oldSet.wifiPassword = etPassword.text.toString()
                    oldSet.allSteps = et_allSteps.text as Int
                    settingsPrefs.setChanges(p, oldSet)
                }*/

                Toast.makeText(applicationContext, R.string.preferences_saved, Toast.LENGTH_LONG).show()
                finish()
                return true
            }
            android.R.id.home -> {
                onBackPressed()
                return true
            }
            else ->
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item)
        }
    }
}