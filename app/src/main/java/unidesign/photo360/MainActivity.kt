package unidesign.photo360

import android.app.ProgressDialog
import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main.*
import android.os.Build
import android.support.annotation.RequiresApi
import android.support.design.R.id.visible
import android.support.v7.widget.Toolbar
import android.util.DisplayMetrics
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
//import com.sun.org.apache.xml.internal.serializer.utils.Utils.messages
import org.java_websocket.WebSocket

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import java.net.URI
import java.net.URISyntaxException
import org.java_websocket.drafts.Draft_17
import org.java_websocket.drafts.Draft_6455
import org.java_websocket.exceptions.WebsocketNotConnectedException


class MainActivity : AppCompatActivity() {

    private var mWebSocketClient: WebSocketClient? = null
    private var menu: Menu? = null
    lateinit var mprogresBar: ProgressBar
    //public val sharedPrefs = PreferenceManager(applicationContext)
    //public val sharedPrefs = PreferenceManager(applicationContext)

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val sharedPrefs = PreferenceManager(applicationContext)

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
        mprogresBar = findViewById(R.id.progressBar)
        val btnRunCW: Button = findViewById(R.id.button_run_cw)
        val btnRunCCW: Button = findViewById(R.id.button_run_ccw)
        val btnSTOP: Button = findViewById(R.id.button_stop)

        btnRunCW.setOnClickListener(View.OnClickListener {
            sharedPrefs.direction = 1
            sharedPrefs.state = "start"
            try {
                mWebSocketClient!!.send(sharedPrefs.getJSON().toString())
            }
            catch (e: Exception) {
                Toast.makeText(application, "Turnable not connected", Toast.LENGTH_LONG).show()
            }
        })
        btnRunCCW.setOnClickListener(View.OnClickListener {
            sharedPrefs.direction = 0
            sharedPrefs.state = "start"
            try {
                mWebSocketClient!!.send(sharedPrefs.getJSON().toString())
            }
            catch (e: Exception) {
                Toast.makeText(application, "Turnable not connected", Toast.LENGTH_LONG).show()
            }
        })
        btnSTOP.setOnClickListener(View.OnClickListener {
            sharedPrefs.state = "stop"
            try {
                mWebSocketClient!!.send(sharedPrefs.getJSON().toString())
            }
            catch (e: Exception) {
                Toast.makeText(application, "Turnable not connected", Toast.LENGTH_LONG).show()
            }
        })

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main_activity_appbar_menu, menu)
        this.menu = menu;
        return true
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onOptionsItemSelected(item: MenuItem): Boolean {


        when (item.itemId) {
            R.id.action_connect -> {
                // User chose the "Settings" item, show the app settings UI...
                /*                Snackbar.make(findViewById(R.id.ussd_toolbar), "Replace done with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();

                if (name.length == 0 && template.length == 0) {
                    Snackbar.make(findViewById(R.id.ussd_toolbar), R.string.snackbar_fill_form, Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show()
                    return false
                }*/
                Toast.makeText(application, "Connect to turntable", Toast.LENGTH_SHORT).show()
                menu?.getItem(0)?.setIcon(applicationContext.getDrawable(R.drawable.ic_action_connected));
                mprogresBar.visibility = View.VISIBLE
                connectWebSocket()
                   // menu?.getItem(0)?.setIcon(getDrawable(R.drawable.ic_action_connect))

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

    private fun connectWebSocket(): Boolean {
        val uri: URI
        try {
            uri = URI("ws://192.168.4.1:8000/")
        } catch (e: URISyntaxException) {
            e.printStackTrace()
            return false
        }

        mWebSocketClient = object : WebSocketClient(uri, Draft_6455()) {
            @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
            //var
            override fun onOpen(serverHandshake: ServerHandshake) {
                Log.i("Websocket", "Opened")
                runOnUiThread {
                    mprogresBar.visibility = View.INVISIBLE
                    Toast.makeText(application, "Turntable connected", Toast.LENGTH_LONG).show()
                }
            }

            override fun onMessage(s: String) {
                runOnUiThread {
//                    val textView = findViewById(R.id.messages) as TextView
//                    textView.text = textView.text.toString() + "\n" + s
                }
            }

            @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
            override fun onClose(i: Int, s: String, b: Boolean) {
                Log.i("Websocket", "Closed $s")
                if (getReadyState() != WebSocket.READYSTATE.NOT_YET_CONNECTED)
                runOnUiThread {
                    mprogresBar.visibility = View.INVISIBLE
                    menu?.getItem(0)?.setIcon(getDrawable(R.drawable.ic_action_connect))
                    Toast.makeText(application, "Turntable disconnected", Toast.LENGTH_LONG).show()
                }
            }

            @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
            override fun onError(e: Exception) {
                Log.i("Websocket", "Error " + e.message)
                runOnUiThread {
                    mprogresBar.visibility = View.INVISIBLE
                    menu?.getItem(0)?.setIcon(getDrawable(R.drawable.ic_action_connect))
                    Toast.makeText(application, "Turntable not found", Toast.LENGTH_LONG).show()
                }
            }
        }
        mWebSocketClient?.connect()
        return true
    }

    fun sendMessage(view: View) {
        val editText = findViewById<View>(R.id.message) as EditText
        mWebSocketClient!!.send(editText.text.toString())
        editText.setText("")
    }
}
