package unidesign.photo360

import android.app.ProgressDialog
import android.content.Context
import android.net.wifi.WifiConfiguration
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main.*
import android.os.Build
import android.os.Handler
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
//    var networkSSID = "test"
//    var networkPass = "pass"
    lateinit var wifiConf: WifiConfiguration
    lateinit var wifiManager: WifiManager
    lateinit var sharedPrefs: PreferenceManager
    lateinit var homeWiFiInfo: WifiInfo
    lateinit var btnRunCW: Button
    lateinit var btnRunCCW: Button
    lateinit var btnSTOP: Button
    //public val sharedPrefs = PreferenceManager(applicationContext)
    //public val sharedPrefs = PreferenceManager(applicationContext)

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sharedPrefs = PreferenceManager(applicationContext)
        wifiConf =  WifiConfiguration()
        wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

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
        btnRunCW = findViewById(R.id.button_run_cw)
        btnRunCCW = findViewById(R.id.button_run_ccw)
        btnSTOP = findViewById(R.id.button_stop)
        disableButton()

        btnRunCW.setOnClickListener(View.OnClickListener {
            sharedPrefs.direction = 1
            sharedPrefs.state = "start"
            try {
                mWebSocketClient!!.send(sharedPrefs.getJSON().toString())
            }
            catch (e: Exception) {
                Toast.makeText(application, "Turnable not connected", Toast.LENGTH_SHORT).show()
            }
        })
        btnRunCCW.setOnClickListener(View.OnClickListener {
            sharedPrefs.direction = 0
            sharedPrefs.state = "start"
            try {
                mWebSocketClient!!.send(sharedPrefs.getJSON().toString())
            }
            catch (e: Exception) {
                Toast.makeText(application, "Turnable not connected", Toast.LENGTH_SHORT).show()
            }
        })
        btnSTOP.setOnClickListener(View.OnClickListener {
            sharedPrefs.state = "stop"
            try {
                mWebSocketClient!!.send(sharedPrefs.getJSON().toString())
            }
            catch (e: Exception) {
                Toast.makeText(application, "Turnable not connected", Toast.LENGTH_SHORT).show()
            }
        })

    }

    private fun disableButton() {
        //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        btnRunCW.isEnabled = false
        btnRunCCW.isEnabled = false
        btnSTOP.isEnabled = false
    }

    private fun enableButton() {
        //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        btnRunCW.isEnabled = true
        btnRunCCW.isEnabled = true
        btnSTOP.isEnabled = true
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
                if (!(mWebSocketClient?.isOpen ?: false)) {
                    /*                Snackbar.make(findViewById(R.id.ussd_toolbar), "Replace done with your own action", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();

                    if (name.length == 0 && template.length == 0) {
                        Snackbar.make(findViewById(R.id.ussd_toolbar), R.string.snackbar_fill_form, Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show()
                        return false
                    }*/
                    menu?.getItem(0)?.setIcon(applicationContext.getDrawable(R.drawable.ic_action_connected));
                    mprogresBar.visibility = View.VISIBLE
                    connect2Photo360WiFi(sharedPrefs.wifiSsid, sharedPrefs.wifiPassword)
//                    connectWebSocket()
                }
                else {
                    //Toast.makeText(application, "Disconnect from turntable", Toast.LENGTH_SHORT).show()
                    menu?.getItem(0)?.setIcon(applicationContext.getDrawable(R.drawable.ic_action_connect));
                    disconnectFromPhoto360WiFi()
                }

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

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun connect2Photo360WiFi(networkSSID: String, networkPass: String) {
        //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        homeWiFiInfo = wifiManager.connectionInfo
        wifiConf.SSID = "\"" + networkSSID + "\""
        //wifiConf.preSharedKey = "\"" + networkPass + "\""
        wifiConf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
        wifiManager.addNetwork(wifiConf)

        //if no WiFi connection allowed
        if (!wifiManager.isWifiEnabled) {
            Toast.makeText(application, "Please, enable WiFi connection", Toast.LENGTH_SHORT).show()
            mprogresBar.visibility = View.INVISIBLE
            menu?.getItem(0)?.setIcon(getDrawable(R.drawable.ic_action_connect))
            return
        }

        Toast.makeText(application, "Connect to turntable", Toast.LENGTH_SHORT).show()
        var list: List<WifiConfiguration> = wifiManager.getConfiguredNetworks()
        for(  i: WifiConfiguration in list ) {
             if(i.SSID != null && i.SSID.equals("\"" + networkSSID + "\"")) {
                 wifiManager.disconnect()
                 wifiManager.enableNetwork(i.networkId, true)
                 wifiManager.reconnect()
                 //wifiManager.connectionInfo.ssid
                 break;
            }
        }
        Handler().postDelayed(Runnable {
            // do something...
            connectWebSocket()
        }, 4000)
    }

    private fun disconnectFromPhoto360WiFi() {
        //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        //wifiConf.SSID = "\"" + homeWiFiInfo.ssid + "\""
        //wifiConf.preSharedKey = "\"" + homeWiFiInfo. + "\""
        //wifiConf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
        //wifiManager.addNetwork(wifiConf)
        disconnectWebSocket()

        var list: List<WifiConfiguration> = wifiManager.getConfiguredNetworks()
        for(  i: WifiConfiguration in list ) {
            if(i.SSID != null && i.SSID.equals(homeWiFiInfo.ssid)) {
                wifiManager.disconnect()
                wifiManager.enableNetwork(i.networkId, true)
                wifiManager.reconnect()
                //wifiManager.connectionInfo.ssid
                break;
            }
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
                    Toast.makeText(application, "Turntable connected", Toast.LENGTH_SHORT).show()
                    enableButton()
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
                    Toast.makeText(application, "Turntable disconnected", Toast.LENGTH_SHORT).show()
                    disableButton()
                }
            }

            @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
            override fun onError(e: Exception) {
                Log.i("Websocket", "Error " + e.message)
                runOnUiThread {
                    mprogresBar.visibility = View.INVISIBLE
                    menu?.getItem(0)?.setIcon(getDrawable(R.drawable.ic_action_connect))
                    Toast.makeText(application, "Turntable not found", Toast.LENGTH_SHORT).show()
                    disableButton()
                }
            }
        }
        mWebSocketClient?.connect()
        return true
    }

    private fun disconnectWebSocket() {
        mWebSocketClient?.close()
    }

    fun sendMessage(view: View) {
        val editText = findViewById<View>(R.id.message) as EditText
        mWebSocketClient!!.send(editText.text.toString())
        editText.setText("")
    }
}
