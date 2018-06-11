package unidesign.photo360

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Build
import android.os.Bundle
import android.support.annotation.RequiresApi
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.w3c.dom.Text
import java.util.*

class SettingsEdit : AppCompatActivity() {

    lateinit var etSSID: EditText
    lateinit var etPassword: EditText
    lateinit var et_allSteps: EditText
    lateinit var txtCalibrExplanation: TextView
    lateinit var calibrate_button: Button
    lateinit var settingsPrefs: SettingsPreferences
    lateinit var viewModel: FragmentViewModel
    lateinit var oldSet: Settings
    var page: Int = 0

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
        txtCalibrExplanation = findViewById<View>(R.id.txtCalibrExplanation) as TextView
        calibrate_button = findViewById<View>(R.id.calibrate_button) as Button

        viewModel = ViewModelProviders.of(this).get(FragmentViewModel::class.java)

        viewModel.getPreset(page).
                observe(this, object: Observer<String> {
                    override fun onChanged(jss: String?) {
                        var settings = Settings(jss!!)
                        displaySettings(settings)
                    }
                })

        viewModel.initPreferencesRequest(page)

        calibrate_button.setOnClickListener(View.OnClickListener {

            var calibrateSettings = Settings()
            calibrateSettings.direction = 1
            calibrateSettings.allSteps = -1 //calibration mod
            calibrateSettings.speed = 3000
            calibrateSettings.state = "start"
            calibrate_button.setText(R.string.stop_calibrate_btn)

            try {
                MainActivity.mWebSocketClient!!.send(calibrateSettings.getJSON().toString())
            }
            catch (e: Exception) {
                Toast.makeText(application, "Turnable not connected", Toast.LENGTH_SHORT).show()
                calibrate_button.setText(R.string.start_calibrate_btn)
        }
        })
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public fun onMessage(event: wsMessage){
/*
        Log.d("AcViewModel onMessage()", Settings.FRAMES_LEFT + ": " + postSettings.value?.framesLeft)
        Log.d("AcViewModel onMessage()", Settings.STATE + ": " + postSettings.value?.state)
*/
            viewModel.setCalibration(page, event.allFrames)
    }

    fun displaySettings (mSettings: Settings){

        etSSID.setText(mSettings.wifiSsid)
        etPassword.setText(mSettings.wifiPassword)
        et_allSteps.setText(mSettings.allSteps.toString())
        //= sharedPrefs.shootingMode
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.pref_edit_menu, menu);
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_save -> {
                for (preset in 0..3){
                    oldSet = Settings(viewModel.getPreset(preset).value!!)
                    oldSet.wifiSsid = etSSID.text.toString()
                    oldSet.wifiPassword = etPassword.text.toString()
                    oldSet.allSteps = et_allSteps.text.toString().toInt()
                    settingsPrefs.setChanges(preset, oldSet)
                }

                Toast.makeText(applicationContext, R.string.preferences_saved, Toast.LENGTH_LONG).show()
                finish()
                return true
            }
            else ->
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item)
        }
    }
}