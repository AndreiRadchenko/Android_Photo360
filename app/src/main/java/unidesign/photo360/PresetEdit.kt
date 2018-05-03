package unidesign.photo360

import android.content.ContentValues
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
    lateinit var etAcceleration: EditText
    lateinit var etShootingMode: Spinner
    lateinit var sharedPrefs: PreferenceManager

    enum class ShootingMode {
        inter, PingP, seria, nonST
    }

    val shootingModemap: HashMap<String, Int> = hashMapOf("inter" to 0, "PingP" to 1, "seria" to 2, "nonST" to 3)

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.preset_edit)
        sharedPrefs = PreferenceManager(applicationContext)
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
        etAcceleration = findViewById<View>(R.id.etAcceleration) as EditText
        etShootingMode = findViewById<View>(R.id.etShootingMode) as Spinner

        // Create an ArrayAdapter using the string array and a default spinner layout
        val adapter = ArrayAdapter.createFromResource(this,
                R.array.shootingmode_array, android.R.layout.simple_spinner_item)
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        // Apply the adapter to the spinner
        etShootingMode.adapter = adapter

        etName.setText(sharedPrefs.presetName);
        etFrame.setText(sharedPrefs.frame.toString())
        etDelay.setText(sharedPrefs.delay.toString())
        etSpeed.setText(sharedPrefs.speed.toString())
        etAcceleration.setText(sharedPrefs.acceleration.toString())
        etShootingMode.setSelection( shootingModemap.get(sharedPrefs.shootingMode)!!)
        //= sharedPrefs.shootingMode

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
        // создаем объект для создания и управления версиями БД
        //dbHelper = new TemplatesDataSource(this);

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

        // создаем объект для данных
        val values = ContentValues()

        //получаем данные из полей ввода
        //String name = etName.getText().toString();
        //String comment = etComment.getText().toString();
        //String template = etTemplate.getText().toString();

        when (item.itemId) {
            R.id.action_save -> {
                sharedPrefs.presetName = etName.text.toString()
                sharedPrefs.frame = etFrame.text.toString().toInt()
                sharedPrefs.delay = etDelay.text.toString().toInt()
                sharedPrefs.speed = etSpeed.text.toString().toInt()
                sharedPrefs.acceleration = etAcceleration.text.toString().toInt()
                sharedPrefs.shootingMode = getKeyByValue(shootingModemap, etShootingMode.selectedItemPosition) ?: "inter"
                Log.d("action_save", sharedPrefs.shootingMode)
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