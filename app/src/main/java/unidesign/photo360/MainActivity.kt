package unidesign.photo360

import android.content.ContentValues
import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main.*
import android.content.Context.WINDOW_SERVICE
import android.os.Build
import android.support.annotation.RequiresApi
import android.support.design.widget.Snackbar
import android.support.v7.widget.Toolbar
import android.view.WindowManager
import android.util.DisplayMetrics
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast


class MainActivity : AppCompatActivity() {

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val myToolbar = findViewById(R.id.main_activity_toolbar) as Toolbar
        setSupportActionBar(myToolbar)
        // Get a support ActionBar corresponding to this toolbar
        val ab = supportActionBar

        val pageAdapter = PageAdapter(supportFragmentManager)
        var displayMetrics: DisplayMetrics? = null

        // create fragments from 0 to 9
        for (i in 0..4) {
            pageAdapter.add(PageFragment.newInstance(i), "Tab$i")
        }

        view_pager.adapter = pageAdapter
//        tabs.setupWithViewPager(view_pager)

//        displayMetrics = DisplayMetrics()
//        val windowmanager = applicationContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager
//        windowmanager.defaultDisplay.getMetrics(displayMetrics)
//        val tabwidth = Math.round(displayMetrics.widthPixels / displayMetrics.density/3)

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main_activity_appbar_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {


        when (item.itemId) {
            R.id.action_connect -> {
                // User chose the "Settings" item, show the app settings UI...
                /*                Snackbar.make(findViewById(R.id.ussd_toolbar), "Replace done with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();*/

//                if (name.length == 0 && template.length == 0) {
//                    Snackbar.make(findViewById(R.id.ussd_toolbar), R.string.snackbar_fill_form, Snackbar.LENGTH_LONG)
//                            .setAction("Action", null).show()
//                    return false
//                }
                Toast.makeText(application, "Connect to turntable", Toast.LENGTH_LONG).show()

                return true
            }

            R.id.action_settings -> {

                return true
            }

            else ->
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item)
        }
    }
}
