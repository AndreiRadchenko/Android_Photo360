package unidesign.photo360

import android.app.Activity
import android.content.ContentValues
import android.content.SharedPreferences
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.ActionBar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import unidesign.photo360.MainActivity

class PresetEdit : AppCompatActivity() {

//    internal val etName: EditText = findViewById<View>(R.id.etName) as EditText
//    internal val etFrame: EditText = findViewById<View>(R.id.etFrame) as EditText
//    internal val etDelay: EditText = findViewById<View>(R.id.etDelay) as EditText
//    internal val etSpeed: EditText = findViewById<View>(R.id.etSpeed) as EditText
//    internal val etAcceleration: EditText = findViewById<View>(R.id.etAcceleration) as EditText
//    internal val etShootingMode: Spinner = findViewById<View>(R.id.etShootingMode) as Spinner

    override fun onCreate(savedInstanceState: Bundle?) {

        // Use the chosen theme
        //        SharedPreferences sharedPrefs = android.preference.PreferenceManager.getDefaultSharedPreferences(this);
        //        boolean useDarkTheme = sharedPrefs.getBoolean(pref_items.pref_DarkTheme, false);
        //
        //        if(useDarkTheme) {
        //            setTheme(R.style.Theme_Dark);
        //        }

        super.onCreate(savedInstanceState)
        setContentView(R.layout.preset_edit)
        val sharedPrefs = PreferenceManager(applicationContext)
        val myToolbar = findViewById<View>(R.id.preset_edit_toolbar) as Toolbar
        setSupportActionBar(myToolbar)
        // Get a support ActionBar corresponding to this toolbar
        val ab = supportActionBar

        // Enable the Up button
        ab!!.setDisplayHomeAsUpEnabled(true)


//        etName = findViewById<View>(R.id.etName) as EditText
//        etFrame = findViewById<View>(R.id.etFrame) as EditText
//        etDelay = findViewById<View>(R.id.etDelay) as EditText
//        etSpeed = findViewById<View>(R.id.etSpeed) as EditText
//        etAcceleration = findViewById<View>(R.id.etAcceleration) as EditText
         val etName: EditText = findViewById<View>(R.id.etName) as EditText
         val etFrame: EditText = findViewById<View>(R.id.etFrame) as EditText
         val etDelay: EditText = findViewById<View>(R.id.etDelay) as EditText
         val etSpeed: EditText = findViewById<View>(R.id.etSpeed) as EditText
         val etAcceleration: EditText = findViewById<View>(R.id.etAcceleration) as EditText
         val etShootingMode: Spinner = findViewById<View>(R.id.etShootingMode) as Spinner
//        etShootingMode = findViewById<View>(R.id.etShootingMode) as Spinner
        // Create an ArrayAdapter using the string array and a default spinner layout
        val adapter = ArrayAdapter.createFromResource(this,
                R.array.shootingmode_array, android.R.layout.simple_spinner_item)
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        // Apply the adapter to the spinner
        etShootingMode.adapter = adapter

        //etName.setText(MainActivity.sharedPrefs.);
//        etFrame.setText(sharedPrefs.frame)
//        etDelay.setText(sharedPrefs.delay)
//        etSpeed.setText(sharedPrefs.speed)
//        etAcceleration.setText(sharedPrefs.acceleration)
        //etShootingMode.setText(sharedPrefs.getFrame());

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
        //getMenuInflater().inflate(R.menu.ussd_appbar_menu, menu);
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        // создаем объект для данных
        val values = ContentValues()

        // получаем данные из полей ввода
        //String name = etName.getText().toString();
        //String comment = etComment.getText().toString();
        //String template = etTemplate.getText().toString();

        when (item.itemId) {
        //            case R.id.action_done:
        //                //Toast.makeText(getApplication(), R.string.template_saved, Toast.LENGTH_LONG).show();
        //                finish();
        //                return true;
        //
        //            case R.id.action_copy:
        //                finish();
        //                return true;
        //
            else ->
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item)
        }
    }

}