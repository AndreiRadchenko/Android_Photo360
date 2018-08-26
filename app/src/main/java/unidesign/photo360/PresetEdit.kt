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
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import java.util.*

class PresetEdit : AppCompatActivity() {

    lateinit var etName: EditText
    lateinit var etFrame: EditText
    lateinit var etDelay: EditText
    lateinit var etSpeed: EditText
    //lateinit var etAcceleration: EditText
    lateinit var etShootingMode: Spinner
    //lateinit var sharedPrefs: PreferenceManager
    lateinit var settingsPrefs: SettingsPreferences
    lateinit var viewModel: FragmentViewModel
    lateinit var oldSet: Settings
    var page: Int = 0

    enum class ShootingMode {
        inter, PingP, seria, nonST
    }

    val shootingModemap: HashMap<String, Int> = hashMapOf("inter" to 0, "nonST" to 1)

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.preset_edit)

        // Get the Intent that started this activity and extract the fragment number
        page = intent.getIntExtra("page", 0)

        //sharedPrefs = PreferenceManager(applicationContext)
        settingsPrefs = SettingsPreferences(applicationContext)
        val myToolbar = findViewById<View>(R.id.preset_edit_toolbar) as Toolbar
        setSupportActionBar(myToolbar)
        // Get a support ActionBar corresponding to this toolbar
        val ab = supportActionBar

        // Enable the Up button
        ab!!.setDisplayHomeAsUpEnabled(true)

        etName = findViewById<View>(R.id.etName) as EditText
        etFrame = findViewById<View>(R.id.etFrame) as EditText
        etDelay = findViewById<View>(R.id.etDelay) as EditText
        etSpeed = findViewById<View>(R.id.etSpeed) as EditText
        //etAcceleration = findViewById<View>(R.id.etAcceleration) as EditText
        etShootingMode = findViewById<View>(R.id.etShootingMode) as Spinner

        // Create an ArrayAdapter using the string array and a default spinner layout
        val adapter = ArrayAdapter.createFromResource(this,
                R.array.shootingmode_array, android.R.layout.simple_spinner_item)
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        // Apply the adapter to the spinner
        etShootingMode.adapter = adapter

        //viewModel = ViewModelProviders.of(this).get(FragmentViewModel::class.java)
        viewModel = ViewModelProviders.of(this, CustomViewModelFactory(this.application, page)).
                get(FragmentViewModel::class.java)

        viewModel.getPreset().
                observe(this, Observer<Settings> { set ->
                    displaySettings(set!!)
                })

/*        when (page) {
            0 -> viewModel.getPreset1().
                    observe(this, object: Observer<String> {
                        override fun onChanged(jss: String?) {
                            var settings = Settings(jss!!)
                            displaySettings(settings)
                        }
                    })
            1 -> viewModel.getPreset2().
                    observe(this, object: Observer<String> {
                        override fun onChanged(jss: String?) {
                            var settings = Settings(jss!!)
                            displaySettings(settings)
                        }
                    })
            2 -> viewModel.getPreset3().
                    observe(this, object: Observer<String> {
                        override fun onChanged(jss: String?) {
                            var settings = Settings(jss!!)
                            displaySettings(settings)
                        }
                    })
            3 -> viewModel.getPreset4().
                    observe(this, object: Observer<String> {
                        override fun onChanged(jss: String?) {
                            var settings = Settings(jss!!)
                            displaySettings(settings)
                        }
                    })
        }*/

        etShootingMode.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View,
                                        position: Int, id: Long) {
                Log.v("item", parent.getItemAtPosition(position) as String)
                when (position) {
                    0 -> {
                    }
                    1 -> {
                    }
                    2 -> {
                    }
                }// Whatever you want to happen when the first item gets selected
                // Whatever you want to happen when the second item gets selected
                // Whatever you want to happen when the thrid item gets selected
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // TODO Auto-generated method stub
            }
        }

        viewModel.initPreferencesRequest()
    }

    fun displaySettings (mSettings: Settings){

        etName.setText(mSettings.presetName)
        etFrame.setText(mSettings.frame.toString())
        etDelay.setText(mSettings.delay.toString())
        etSpeed.setText(mSettings.speed.toString())
        //etAcceleration.setText(mSettings.acceleration.toString())
        etShootingMode.setSelection( shootingModemap.get(mSettings.shootingMode)!!)
        //= sharedPrefs.shootingMode
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.pref_edit_menu, menu);
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        when (item.itemId) {
            R.id.action_save -> {
                oldSet = viewModel.getPreset().value!!
/*                when (page) {
                    0 -> oldSet = Settings(viewModel.getPreset1().value!!)
                    1 -> oldSet = Settings(viewModel.getPreset2().value!!)
                    2 -> oldSet = Settings(viewModel.getPreset3().value!!)
                    3 -> oldSet = Settings(viewModel.getPreset4().value!!)
                }*/
                oldSet.presetName = etName.text.toString()
                oldSet.frame = etFrame.text.toString().toInt()
                oldSet.framesLeft = etFrame.text.toString().toInt()
                oldSet.delay = etDelay.text.toString().toInt()
                oldSet.speed = etSpeed.text.toString().toInt()
                //oldSet.acceleration = etAcceleration.text.toString().toInt()
                oldSet.shootingMode = getKeyByValue(shootingModemap, etShootingMode.selectedItemPosition) ?: "inter"
                settingsPrefs.setChanges(page, oldSet)

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

    @RequiresApi(Build.VERSION_CODES.KITKAT)
    fun <T, E> getKeyByValue(map: Map<T, E>, value: E): T? {
        for ((key, value1) in map) {
            if (Objects.equals(value, value1)) {
                return key
            }
        }
        return null
    }
}