package unidesign.photo360

import android.Manifest
import android.app.ProgressDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.wifi.ScanResult
import android.net.wifi.WifiConfiguration
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main.*
import android.os.Build
import android.os.Handler
import android.provider.Contacts
import android.support.annotation.RequiresApi
import android.support.annotation.UiThread
import android.support.design.R.id.visible
import android.support.v4.view.ViewPager
import android.support.v7.widget.Toolbar
import android.util.DisplayMetrics
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import kotlinx.android.synthetic.main.fragment_page.*
import kotlinx.coroutines.experimental.*
//import com.sun.org.apache.xml.internal.serializer.utils.Utils.messages
import org.java_websocket.WebSocket

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import java.net.URI
import java.net.URISyntaxException
import org.java_websocket.drafts.Draft_17
import org.java_websocket.drafts.Draft_6455
import org.java_websocket.exceptions.WebsocketNotConnectedException
import org.json.JSONObject
import kotlin.coroutines.experimental.CoroutineContext
import kotlinx.coroutines.experimental.android.UI
import java.util.concurrent.TimeUnit


class MainActivity : AppCompatActivity() {

    private var mWebSocketClient: WebSocketClient? = null
    private var menu: Menu? = null
    lateinit var mprogresBar: ProgressBar
//    var networkSSID = "test"
//    var networkPass = "pass"
    lateinit var wifiConf: WifiConfiguration
//    public lateinit var wifiManager: WifiManager
    //public lateinit var sharedPrefs: PreferenceManager
    var homeWiFiInfo: WifiInfo? = null
    lateinit var btnRunCW: Button
    lateinit var btnRunCCW: Button
    lateinit var btnSTOP: Button
    lateinit var framesLeftTxt: TextView
    lateinit var turntableView: TurntableView
    lateinit var mViewPager: ViewPager
    lateinit var pageAdapter: PageAdapter
    lateinit var TurntableConectionJob: Job
    private lateinit var wifiScanReceiver: BroadcastReceiver
    //represents a common pool of shared threads as the coroutine dispatcher
    private val bgContext: CoroutineContext = CommonPool
    //public val sharedPrefs = PreferenceManager(applicationContext)
    //public val sharedPrefs = PreferenceManager(applicationContext)
    companion object {
        var sharedPrefs: PreferenceManager? = null
        var currentFragmentId: Int = 0
        lateinit var wifiManager: WifiManager
        var fireWiFiScan = false
    }

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

        pageAdapter = PageAdapter(supportFragmentManager)
        var displayMetrics: DisplayMetrics? = null

        // create fragments from 0 to 9
        for (i in 0..4) {
            pageAdapter.add(PageFragment.newInstance(i), "Tab$i")
        }

        view_pager.adapter = pageAdapter
        mViewPager = findViewById(R.id.view_pager)

        mprogresBar = findViewById(R.id.progressBar)
        btnRunCW = findViewById(R.id.button_run_cw)
        btnRunCCW = findViewById(R.id.button_run_ccw)
        btnSTOP = findViewById(R.id.button_stop)
        disableButton()

        btnRunCW.setOnClickListener(View.OnClickListener {
            sharedPrefs?.direction = 1
            sharedPrefs?.state = "start"

            currentFragmentId = mViewPager.currentItem
            var currentFragment = pageAdapter.getItem(currentFragmentId)
            framesLeftTxt = currentFragment.frames_left_txt

            try {
                mWebSocketClient!!.send(sharedPrefs?.getJSON().toString())
            }
            catch (e: Exception) {
                Toast.makeText(application, "Turnable not connected", Toast.LENGTH_SHORT).show()
            }
        })
        btnRunCCW.setOnClickListener(View.OnClickListener {
            sharedPrefs?.direction = 0
            sharedPrefs?.state = "start"

            currentFragmentId = mViewPager.currentItem
            var currentFragment = pageAdapter.getItem(currentFragmentId)
            framesLeftTxt = currentFragment.frames_left_txt

            try {
                mWebSocketClient!!.send(sharedPrefs?.getJSON().toString())
            }
            catch (e: Exception) {
                Toast.makeText(application, "Turnable not connected", Toast.LENGTH_SHORT).show()
            }
        })
        btnSTOP.setOnClickListener(View.OnClickListener {
            sharedPrefs?.state = "stop"
            try {
                mWebSocketClient!!.send(sharedPrefs?.getJSON().toString())
            }
            catch (e: Exception) {
                Toast.makeText(application, "Turnable not connected", Toast.LENGTH_SHORT).show()
            }
        })

    }

    override fun onResume() {
        super.onResume()
//        var currentFragment = pageAdapter.getItem(mViewPager.currentItem)
//        framesLeftTxt = currentFragment.frames_left_txt
//        framesLeftTxt.setText(sharedPrefs.frame)
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            if(checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            {
                requestPermissions(Array<String>(1){Manifest.permission.ACCESS_COARSE_LOCATION}, 87);
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onStart() {
        super.onStart()
        TurntableConectionJob = Job()
        wifiScanReceiver = WifiScanReceiver()
        registerReceiver(wifiScanReceiver, IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION))
        connectWebSocket()
    }

    override fun onStop() {
        super.onStop()
        unregisterReceiver(wifiScanReceiver)
        disconnectWebSocket()
    }

    override fun onDestroy() {
        super.onDestroy()
        TurntableConectionJob.cancel()
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
                    //if no WiFi connection allowed
                    if (!wifiManager.isWifiEnabled){
                        Toast.makeText(application, "Please, enable WiFi connection", Toast.LENGTH_SHORT).show()
                    }
                    else {
                        menu?.getItem(0)?.setIcon(applicationContext.getDrawable(R.drawable.ic_action_connected))
                        mprogresBar.visibility = View.VISIBLE
                        wifiManager.startScan()
                        fireWiFiScan = true
                    }

                    //connect2Turntable()
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
    fun connect2Turntable(){
        TurntableConectionJob = launch(UI){
            try {
                val result1 = withContext(bgContext) { connect2Photo360WiFi(sharedPrefs?.wifiSsid, sharedPrefs?.wifiPassword) }

                when (result1){
                    "wifi connected" -> {
                        Log.d("connect2Photo360WiFi", "[${Thread.currentThread().name}] " +"wifi connected")
                        async(bgContext) {connectWebSocket()}
                        //mprogresBar.visibility = View.INVISIBLE
                        //Toast.makeText(application, "Please, enable WiFi connection", Toast.LENGTH_SHORT).show()
                    }
                    "wifi just connected" -> {
                        Log.d("connect2Photo360WiFi", "[${Thread.currentThread().name}] " +"wifi just connected")
                        async(bgContext) {
                            delay(4000)
                            connectWebSocket()}
                        //Toast.makeText(application, "Please, enable WiFi connection", Toast.LENGTH_SHORT).show()
                    }
                    "wifi not added" -> {
                        Log.d("connect2Photo360WiFi", "[${Thread.currentThread().name}] " +"wifi not found")
                        Toast.makeText(application, "Turntable WiFi not added", Toast.LENGTH_SHORT).show()
                        menu?.getItem(0)?.setIcon(applicationContext.getDrawable(R.drawable.ic_action_connect))
                        mprogresBar.visibility = View.INVISIBLE
                    }
                }

            } catch (exception: Exception) {
                exception.printStackTrace()
                mprogresBar.visibility = View.INVISIBLE
            }

            finally {
                //Turn off busy indicator.

            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private suspend fun connect2Photo360WiFi(networkSSID: String?, networkPass: String?): String {
        //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        homeWiFiInfo = wifiManager.connectionInfo
        wifiConf.SSID = "\"" + networkSSID + "\""
        //wifiConf.preSharedKey = "\"" + networkPass + "\""
        wifiConf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
        wifiManager.addNetwork(wifiConf)

        if (wifiManager.connectionInfo.ssid.equals(wifiConf.SSID))
            return "wifi connected"
        else {
            //Toast.makeText(application, "Connect to turntable", Toast.LENGTH_SHORT).show()
            var list: List<WifiConfiguration> = wifiManager.configuredNetworks
            var scannedWiFi = wifiManager.scanResults
            for (i: WifiConfiguration in list) {
                        if (i.SSID != null && i.SSID.equals("\"" + networkSSID + "\"")) {
                            wifiManager.disconnect()
                            wifiManager.enableNetwork(i.networkId, true)
                            wifiManager.reconnect()
                            //Log.d("connect2Photo360WiFi", "[${Thread.currentThread().name}] " +"wifiManager.reconnect(): true")
                            return "wifi just connected"
                            break;
                        }
            }
            return "wifi not added"
        }
//        Handler().postDelayed(Runnable {
//            // do something...
//            connectWebSocket()
//        }, 4000)
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
            if(i.SSID != null && i.SSID.equals(homeWiFiInfo?.ssid)) {
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
                    menu?.getItem(0)?.setIcon(applicationContext.getDrawable(R.drawable.ic_action_connected))
                    mprogresBar.visibility = View.INVISIBLE
                    Toast.makeText(application, "Turntable connected", Toast.LENGTH_SHORT).show()
                    enableButton()
                }
            }

            override fun onMessage(s: String) {
                runOnUiThread {

                    var esp32answer = JSONObject(s)
                    var frameleft = esp32answer.getInt(PreferenceManager.FRAMES_LEFT)
                    Log.d("onMessage()", PreferenceManager.FRAMES_LEFT + ": " + frameleft)
                    sharedPrefs?.framesLeft = frameleft

                    var currentFragment = pageAdapter.getItem(currentFragmentId)
                    if (currentFragment.isVisible) {
                        framesLeftTxt = currentFragment.frames_left_txt
                        framesLeftTxt.setText(frameleft.toString())
                        framesLeftTxt.invalidate()
                    }
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
        Log.d("connectWebSocket", "[${Thread.currentThread().name}] " +"connect to web socket")
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

    inner class WifiScanReceiver: BroadcastReceiver() {
        @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
        override fun onReceive(context: Context, intent: Intent) {
            if(intent.getAction().equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION) && fireWiFiScan) {
                fireWiFiScan = false
                var scanResults = MainActivity.wifiManager.scanResults
                var TurntableFound = false
                for (i:ScanResult in scanResults){
                    Log.d("WifiScanReceiver", "ScanResult: " + i.SSID)
                    if (i.SSID.equals(sharedPrefs?.wifiSsid)) {
                        connect2Turntable()
                        TurntableFound = true
                        break
                    }
                }
                if (!TurntableFound){
                    Toast.makeText(application, "Turntable WiFi not found", Toast.LENGTH_SHORT).show()
                    menu?.getItem(0)?.setIcon(applicationContext.getDrawable(R.drawable.ic_action_connect))
                    mprogresBar.visibility = View.INVISIBLE
                }
            }
        }
    }
}


