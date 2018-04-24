package unidesign.photo360

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main.*
import android.os.Build
import android.support.annotation.RequiresApi
import android.support.v7.widget.Toolbar
import android.util.DisplayMetrics
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import android.widget.EditText
//import com.sun.org.apache.xml.internal.serializer.utils.Utils.messages
import android.widget.TextView

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import java.net.URI
import java.net.URISyntaxException
import org.java_websocket.drafts.Draft_17
import org.java_websocket.drafts.Draft_6455


class MainActivity : AppCompatActivity() {

    private var mWebSocketClient: WebSocketClient? = null

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
                connectWebSocket()
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

    private fun connectWebSocket() {
        val uri: URI
        try {
            uri = URI("ws://192.168.4.1:8000/")
        } catch (e: URISyntaxException) {
            e.printStackTrace()
            return
        }

        mWebSocketClient = object : WebSocketClient(uri, Draft_6455()) {
            override fun onOpen(serverHandshake: ServerHandshake) {
                Log.i("Websocket", "Opened")
                //mWebSocketClient?.send("Hello from " + Build.MANUFACTURER + " " + Build.MODEL)
            }

            override fun onMessage(s: String) {
                runOnUiThread {
//                    val textView = findViewById(R.id.messages) as TextView
//                    textView.text = textView.text.toString() + "\n" + s
                }
            }

            override fun onClose(i: Int, s: String, b: Boolean) {
                Log.i("Websocket", "Closed $s")
            }

            override fun onError(e: Exception) {
                Log.i("Websocket", "Error " + e.message)
            }
        }
        mWebSocketClient!!.connect()
    }

    fun sendMessage(view: View) {
        val editText = findViewById<View>(R.id.message) as EditText
        mWebSocketClient!!.send(editText.text.toString())
        editText.setText("")
    }
}
