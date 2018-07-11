package unidesign.photo360

import android.Manifest
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.Uri
import android.net.wifi.ScanResult
import android.net.wifi.WifiConfiguration
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main.*
import android.os.Build
import android.os.Handler
import android.os.Message
import android.support.annotation.RequiresApi
import android.support.annotation.UiThread
import android.support.design.R.id.visible
import android.support.design.widget.NavigationView
import android.support.design.widget.TabLayout
import android.support.v4.app.ActivityCompat
import android.support.v4.app.DialogFragment
import android.support.v4.content.ContextCompat
import android.support.v4.view.GravityCompat
import android.support.v4.view.ViewPager
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.widget.Toolbar
import android.util.DisplayMetrics
import android.util.Log
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import kotlinx.android.synthetic.main.fragment_page.*
import kotlinx.coroutines.experimental.*
//import com.sun.org.apache.xml.internal.serializer.utils.Utils.messages
import org.java_websocket.WebSocket

import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import java.net.URI
import java.net.URISyntaxException
import org.java_websocket.drafts.Draft_6455
import org.java_websocket.exceptions.WebsocketNotConnectedException
import org.json.JSONObject
import kotlin.coroutines.experimental.CoroutineContext
import kotlinx.coroutines.experimental.android.UI
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import unidesign.photo360.BackupRestore.BackupDialog
import unidesign.photo360.R.drawable.settings

//import unidesign.photo360.save_restore.BackupDialog

import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit


class MainActivity : AppCompatActivity(),  NavigationView.OnNavigationItemSelectedListener,
                            BackupDialog.NoticeDialogListener
{


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
    lateinit var mToolbar: Toolbar
    lateinit var pageAdapter: PageAdapter
    lateinit var TurntableConectionJob: Job
    lateinit var drawer: DrawerLayout
    private lateinit var wifiScanReceiver: BroadcastReceiver
    //represents a common pool of shared threads as the coroutine dispatcher
    private val bgContext: CoroutineContext = CommonPool
    internal val PERMISSION_REQUEST_CODE = 1
    //private var runningFragmentId: Int = NO_FRAGMENT_RUNNING
    //var postSettings: Settings = Settings()
    //public val sharedPrefs = PreferenceManager(applicationContext)
    //public val sharedPrefs = PreferenceManager(applicationContext)
    companion object {
        //var sharedPrefs: PreferenceManager? = null
        val NO_FRAGMENT_RUNNING = -1
        var currentFragmentId: Int = 0
        var runningFragmentId: Int = NO_FRAGMENT_RUNNING
        var mstate: String = "waiting"
        var mframeleft: Int = 36
        var postSettings: Settings = Settings()
        lateinit var wifiManager: WifiManager
        var fireWiFiScan = false
        var wsConnected = false
        var mWebSocketClient: WebSocketClient? = null
        var menu: Menu? = null

    }

    lateinit var viewModel: ActivityViewModel
    lateinit var settingsPrefs: SettingsPreferences

    //todo  NoticeDialogListener interface
    override fun onDialogPositiveClick(dialog: DialogFragment, name: String, comment: String) {
        // User touched the dialog's positive button
        //val AsyncBackup = BackupTask(this)
        //AsyncBackup.execute(name, comment)
        drawer.closeDrawer(Gravity.LEFT, false)
    }

    override fun onDialogNegativeClick(dialog: DialogFragment) {

    }

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_drawer)

        settingsPrefs = SettingsPreferences(applicationContext)
        //sharedPrefs = PreferenceManager(applicationContext)
        wifiConf =  WifiConfiguration()
        wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

        val myToolbar = findViewById(R.id.main_activity_toolbar) as Toolbar
        setSupportActionBar(myToolbar)
        // Get a support ActionBar corresponding to this toolbar
        val ab = supportActionBar
        drawer = findViewById(R.id.drawer_layout) as DrawerLayout

        val navigationView = findViewById(R.id.nav_view) as NavigationView
        navigationView.setNavigationItemSelectedListener(this)

        drawer = findViewById(R.id.drawer_layout) as DrawerLayout
        val toggle = object : ActionBarDrawerToggle(
                this, drawer, myToolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close) {
            override fun onDrawerOpened(drawerView: View) {
                super.onDrawerOpened(drawerView)
            }

            override fun onDrawerClosed(drawerView: View) {
                super.onDrawerClosed(drawerView)

            }
        }

        drawer.addDrawerListener(toggle)

        toggle.syncState()

        pageAdapter = PageAdapter(supportFragmentManager)
        var displayMetrics: DisplayMetrics? = null

        // create fragments from 0 to 9
        for (i in 0..3) {
            pageAdapter.add(PageFragment.newInstance(i), "Tab$i")
        }

        mViewPager = findViewById(R.id.view_pager)
        mViewPager.adapter = pageAdapter

        var tabLayout = findViewById(R.id.tabs) as TabLayout
        tabLayout.setupWithViewPager(mViewPager)

        mprogresBar = findViewById(R.id.progressBar)
        btnRunCW = findViewById(R.id.button_run_cw)
        btnRunCCW = findViewById(R.id.button_run_ccw)
        btnSTOP = findViewById(R.id.button_stop)
        disableButton()

        viewModel = ViewModelProviders.of(this).get(ActivityViewModel::class.java)

        viewModel.getSettings().
                observe(this, Observer<Settings> { set -> run {
                    Log.d("App ViewModel Observ", "set?.state = " + set?.state)
                    if (set?.state == "waiting" || set?.state == "stop")
                        buttonStartState()
                    else
                        buttonStopState()}
                })

        postSettings = Settings(settingsPrefs.presetArray[0].get() ?: Settings().getJSON().toString())

        btnRunCW.setOnClickListener(View.OnClickListener {
            runningFragmentId = mViewPager.currentItem
            Log.d("OnClickListener()", "runningFragmentId: " + runningFragmentId)
            //runningFragmentId = mViewPager.currentItem
            var currentFragment = pageAdapter.getItem(runningFragmentId) as PageFragment
            postSettings = currentFragment.viewModel.getPreset().value ?: Settings()

            postSettings.direction = 1
            postSettings.state = "start"
            //postSettings.presetFragment = currentFragmentId
            viewModel.setSettings(postSettings)
            //viewModel.setRunningFragment(currentFragmentId)
            //viewModel.setTtRun(true)
//            buttonStopState()
//            sharedPrefs?.direction = 1
//            sharedPrefs?.state = "start"
            try {
                mWebSocketClient!!.send(postSettings.getJSON().toString())
            }
            catch (e: Exception) {
                Toast.makeText(application, "Turnable not connected", Toast.LENGTH_SHORT).show()
                buttonStartState()
            }
        })
        btnRunCCW.setOnClickListener(View.OnClickListener {
            runningFragmentId = mViewPager.currentItem
            //runningFragmentId = mViewPager.currentItem
            var currentFragment = pageAdapter.getItem(runningFragmentId) as PageFragment
            postSettings = currentFragment.viewModel.getPreset().value ?: Settings()

            postSettings.direction = 0
            postSettings.state = "start"
            viewModel.setSettings(postSettings)
            //viewModel.setTtRun(true)
//            buttonStopState()
//            sharedPrefs?.direction = 0
//            sharedPrefs?.state = "start"
            try {
                mWebSocketClient!!.send(postSettings.getJSON().toString())
            }
            catch (e: Exception) {
                Toast.makeText(application, "Turnable not connected", Toast.LENGTH_SHORT).show()
                buttonStartState()
            }
        })
        btnSTOP.setOnClickListener(View.OnClickListener {
            //sharedPrefs?.state = "stop"
            //currentFragmentId = mViewPager.currentItem
            //var currentFragment = pageAdapter.getItem(currentFragmentId) as PageFragment
            //postSettings = Settings(currentFragment.viewModel.getPreset(currentFragmentId).value ?:
            //                                Settings().getJSON().toString())
            if (runningFragmentId == NO_FRAGMENT_RUNNING)
                runningFragmentId = mViewPager.currentItem
            var currentFragment = pageAdapter.getItem(runningFragmentId) as PageFragment
            if (currentFragment != null)
                currentFragment.viewModel.initPreferencesRequest(runningFragmentId)
            postSettings.state = "stop"
            //postSettings.framesLeft = postSettings.frame
            viewModel.setSettings(postSettings)
            //viewModel.setTtRun(false)
            //buttonStartState()
            try {
                mWebSocketClient!!.send(postSettings.getJSON().toString())
            }
            catch (e: Exception) {
                Toast.makeText(application, "Turnable not connected", Toast.LENGTH_SHORT).show()
            }
//            runningFragmentId = NO_FRAGMENT_RUNNING
        })

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public fun onMessage(event: wsMessage){
/*
        Log.d("AcViewModel onMessage()", Settings.FRAMES_LEFT + ": " + postSettings.value?.framesLeft)
        Log.d("AcViewModel onMessage()", Settings.STATE + ": " + postSettings.value?.state)
*/
        postSettings.framesLeft = event.framesLeft
        postSettings.state = event.state
        viewModel.setSettings(postSettings)
    }

/*    override fun onSaveInstanceState(savedInstanceState: Bundle)
    {
        super.onSaveInstanceState(savedInstanceState)
        savedInstanceState.putInt("FragmentId", currentFragmentId)
        savedInstanceState.putInt("runFragmentId", runningFragmentId)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle)
    {
        super.onRestoreInstanceState(savedInstanceState)
        currentFragmentId = savedInstanceState.getInt("FragmentId")
        runningFragmentId = savedInstanceState.getInt("runFragmentId")
        Log.d("onRestoreInstanceState", "runningFragmentId: " + runningFragmentId)
        Log.d("onRestoreInstanceState", "currentFragmentId: " + currentFragmentId)

    }*/

    override fun onBackPressed() {
        val drawer = findViewById(R.id.drawer_layout) as DrawerLayout
        if (drawer.isDrawerOpen(GravityCompat.START)) run {
                drawer.closeDrawer(GravityCompat.START)
            //mTutorialHandler.cleanUp();
        }
        else
        super.onBackPressed()
    }

    override fun onResume() {
        super.onResume()

        if (wsConnected)
        {
            menu?.getItem(0)?.setIcon(applicationContext.getDrawable(R.drawable.ic_action_connected))
            viewModel.setSettings(postSettings)
        }

        if (runningFragmentId == NO_FRAGMENT_RUNNING)
            mViewPager.currentItem = currentFragmentId
        else
/*        Log.d("onResume()", "runningFragmentId: " + runningFragmentId)
        Log.d("onResume()", "currentFragmentId: " + currentFragmentId)*/
            mViewPager.currentItem = runningFragmentId

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
        EventBus.getDefault().register(this)
        TurntableConectionJob = Job()
        wifiScanReceiver = WifiScanReceiver()
        registerReceiver(wifiScanReceiver, IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION))
        //connectWebSocket()
    }

    override fun onStop() {
        EventBus.getDefault().unregister(this)
        unregisterReceiver(wifiScanReceiver)
        TurntableConectionJob.cancel()
        super.onStop()
        //disconnectWebSocket()
    }

    override fun onPause() {
        currentFragmentId = mViewPager.currentItem
        //runningFragmentId = mViewPager.currentItem
        super.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        val id = item.itemId

        if (id == R.id.nav_settings) {

            //startActivity(Intent("intent.action.import_templates"))
            startActivity(Intent("intent.action.settingsedit"))

        } else if (id == R.id.nav_save) {

            if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {

                val newFragment = BackupDialog()
                newFragment.show(supportFragmentManager, "backup_dialog")

/*                BackupTask AsyncBackup = new BackupTask(this);
                AsyncBackup.execute();*/
            } else {
                ActivityCompat.requestPermissions(this,
                        arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), PERMISSION_REQUEST_CODE)
            }
        } else if (id == R.id.nav_restore) {

/*            if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {

                startActivity(Intent("intent.action.restore_templates"))
            } else {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), PERMISSION_READ_SD)
            }*/
        } else if (id == R.id.nav_help) {

/*            val settings_intent = Intent(this, SettingsPrefActivity::class.java)
            startActivity(settings_intent)
            //startActivity(new Intent("intent.action.settings"));*/
        }

        //        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        //drawer.closeDrawer(GravityCompat.START);
        return true
    }

/*    @Subscribe(threadMode = ThreadMode.MAIN)
    public fun onMessage(event: wsMessage){
        var currentFragment = pageAdapter.getItem(currentFragmentId) as PageFragment
        if (currentFragment.isVisible) {
            //var currentFragment = pageAdapter.getItem(currentFragmentId)

            framesLeftTxt = currentFragment.view!!.findViewById(R.id.frames_left_txt)
            framesLeftTxt.setText(event.framesLeft)
            framesLeftTxt.invalidate()
        }
    //mytextview.setText(event.message);
    }*/

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

    private fun buttonStartState() {
        //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        btnRunCW.isEnabled = true
        btnRunCCW.isEnabled = true
        btnSTOP.isEnabled = false
        runningFragmentId = NO_FRAGMENT_RUNNING
    }

    private fun buttonStopState() {
        //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        btnRunCW.isEnabled = false
        btnRunCCW.isEnabled = false
        btnSTOP.isEnabled = true
        //runningFragmentId = NO_FRAGMENT_RUNNING
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main_activity_appbar_menu, menu)
        Companion.menu = menu;
        if (wsConnected)
            Companion.menu?.getItem(0)?.setIcon(applicationContext.getDrawable(R.drawable.ic_action_connected))
        return true
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_connect -> {
                if (!wsConnected) {
                    //if no WiFi connection allowed
                    if (!wifiManager.isWifiEnabled){
                        Toast.makeText(application, "Please, enable WiFi connection", Toast.LENGTH_SHORT).show()
                    }
                    else {
                        var wifiset = Settings(settingsPrefs.presetArray[0].get() ?: Settings().getJSON().toString())
                        postSettings.wifiSsid = wifiset.wifiSsid
                        postSettings.wifiPassword = wifiset.wifiPassword
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
                    menu?.getItem(0)?.setIcon(applicationContext.getDrawable(R.drawable.ic_action_connect))
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
                val result1 = withContext(bgContext) { connect2Photo360WiFi( postSettings.wifiSsid, postSettings.wifiPassword) }

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
        wifiConf.preSharedKey = "\"" + networkPass + "\""
        wifiConf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
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
                    wsConnected = true
                    menu?.getItem(0)?.setIcon(applicationContext.getDrawable(R.drawable.ic_action_connected))
                    mprogresBar.visibility = View.INVISIBLE
                    Toast.makeText(application, "Turntable connected", Toast.LENGTH_SHORT).show()
                    buttonStartState()
                }
            }

            override fun onMessage(s: String) {
                var esp32answer = JSONObject(s)
                var frameleft = esp32answer.getInt(Settings.FRAMES_LEFT)
                var state = esp32answer.getString(Settings.STATE)
                var allSteps = esp32answer.getInt(Settings.ALL_STEPS)
                EventBus.getDefault().post(wsMessage(frameleft, state, allSteps))
                runOnUiThread {
                    Log.d("onMessage()", Settings.FRAMES_LEFT + ": " + frameleft)
                    Log.d("onMessage()", "allFrames: " + allSteps)
                    MainActivity.postSettings.state = state
                    MainActivity.postSettings.framesLeft = frameleft

/*                    var currentFragment = pageAdapter.getItem(runningFragmentId) as PageFragment
                    if (currentFragment != null) {
                        currentFragment.viewModel.setChanges2View(runningFragmentId, wsMessage(frameleft, state))
                    }*/
                    //sharedPrefs?.framesLeft = frameleft
                }
            }

            @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
            override fun onClose(i: Int, s: String, b: Boolean) {
                Log.i("Websocket", "Closed $s")
                if (getReadyState() != WebSocket.READYSTATE.NOT_YET_CONNECTED)
                runOnUiThread {
                    wsConnected = false
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
                    wsConnected = false
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

    inner class WifiScanReceiver: BroadcastReceiver() {
        @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
        override fun onReceive(context: Context, intent: Intent) {
            if(intent.getAction().equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION) && fireWiFiScan) {
                fireWiFiScan = false
                var scanResults = MainActivity.wifiManager.scanResults
                var TurntableFound = false
                for (i:ScanResult in scanResults){
                    Log.d("WifiScanReceiver", "ScanResult: " + i.SSID)
                    if (i.SSID.equals(postSettings.wifiSsid)) {
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


