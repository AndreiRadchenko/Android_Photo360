package unidesign.photo360

import android.Manifest
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
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
import android.os.Build
import android.os.Handler
import android.support.annotation.RequiresApi
import android.support.design.widget.NavigationView
import android.support.design.widget.TabLayout
import android.support.v4.view.ViewPager
import android.support.v4.app.ActivityCompat
import android.support.v4.app.DialogFragment
import android.support.v4.content.ContextCompat
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.support.v4.widget.DrawerLayout.LOCK_MODE_LOCKED_CLOSED
import android.support.v4.widget.DrawerLayout.LOCK_MODE_UNLOCKED
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.widget.Toolbar
import android.util.DisplayMetrics
import android.util.Log
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import kotlinx.coroutines.*
//import com.sun.org.apache.xml.internal.serializer.utils.Utils.messages
import org.java_websocket.WebSocket

import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import java.net.URI
import java.net.URISyntaxException
import org.java_websocket.drafts.Draft_6455
import org.json.JSONObject
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.android.UI
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import unidesign.photo360.BackupRestore.BackupDialog
import unidesign.photo360.BackupRestore.BackupTask
import java.lang.Runnable

//import unidesign.photo360.save_restore.BackupDialog

import java.util.*


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
    lateinit var btnRunCW: ImageButton
    lateinit var btnRunCCW: ImageButton
    lateinit var btnSTOP: ImageButton
    lateinit var framesLeftTxt: TextView
    lateinit var turntableView: TurntableView
    lateinit var mViewPager: ViewPager
    lateinit var mToolbar: Toolbar
    lateinit var pageAdapter: PageAdapter
    lateinit var TurntableConectionJob: Job

    private lateinit var wifiScanReceiver: BroadcastReceiver
    //represents a common pool of shared threads as the coroutine dispatcher
    private val bgContext: CoroutineContext = CommonPool
    internal val PERMISSION_WRITE_SD = 1
    val PERMISSION_READ_SD = 2
    val PERMISSION_WIFI = 3
    //private var runningFragmentId: Int = NO_FRAGMENT_RUNNING
    //var postSettings: Settings = Settings()
    //public val sharedPrefs = PreferenceManager(applicationContext)
    //public val sharedPrefs = PreferenceManager(applicationContext)
    companion object {
        //var sharedPrefs: PreferenceManager? = null
        val NO_FRAGMENT_RUNNING = 100
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
        lateinit var drawer: DrawerLayout

    }

    lateinit var viewModel: ActivityViewModel
    lateinit var settingsPrefs: SettingsPreferences

    //todo  NoticeDialogListener interface
    override fun onDialogPositiveClick(dialog: DialogFragment, name: String, comment: String) {
        // User touched the dialog's positive button
        var backupDialog = dialog as BackupDialog
        if (name != "") {
            val AsyncBackup = BackupTask(this)
            AsyncBackup.execute(name, comment)
            backupDialog.dismiss()
            drawer.closeDrawer(Gravity.LEFT, false)
        } else {
            backupDialog.setMessage(getString(R.string.enter_backup_name))
            val h = Handler()
            h.postDelayed(Runnable {
                //Log.d("post delayed handler", "run dialog.resetMessage()")
                try {
                    backupDialog.resetMessage()
                } catch (e: Exception) {
                    Log.d("post delayed exeption", e.toString())
                }
            }, 3000)
            //Toast.makeText(application, getString(R.string.Enter_file_name), Toast.LENGTH_SHORT).show()
        }

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

        val myToolbar: Toolbar = findViewById(R.id.main_activity_toolbar)
        setSupportActionBar(myToolbar)
        // Get a support ActionBar corresponding to this toolbar
        val ab = supportActionBar

        val navigationView: NavigationView = findViewById(R.id.nav_view)
        navigationView.setNavigationItemSelectedListener(this)

        drawer = findViewById(R.id.drawer_layout)
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

        //runningFragmentId = settingsPrefs.runningFragmentId
        Log.d("MainActivity.OnCreate", "runningFragmentId = $runningFragmentId")
        // create fragments from 0 to 9
        for (i in 0..3) {
            pageAdapter.add(PageFragment.newInstance(i), "Tab$i")
            Log.d("MainActivity.OnCreate", "PageFragment.newInstance($i)")
        }

        mViewPager = findViewById(R.id.view_pager)
        mViewPager.adapter = pageAdapter

        var tabLayout: TabLayout = findViewById(R.id.tabs)
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
                    if (set?.state == "waiting" || set?.state == "stop") {
                        buttonStartState()
                        drawer.setDrawerLockMode(LOCK_MODE_UNLOCKED)
                    }
                    else if (set?.state == "pause") {
                        buttonPauseState()
                        drawer.setDrawerLockMode(LOCK_MODE_LOCKED_CLOSED)
                    }
                    else {
                        buttonStopState()
                        drawer.setDrawerLockMode(LOCK_MODE_LOCKED_CLOSED)
                    }
                }
                })

        btnRunCW.setOnClickListener(View.OnClickListener {

            if (postSettings.state == "pause") {
                Log.d("MainActivity.btnRunCW", "postSettings.state = " + postSettings.state)
                postSettings.state = "started"
            }
            else {
                runningFragmentId = mViewPager.currentItem
                //settingsPrefs.runningFragmentId = mViewPager.currentItem
                Log.d("OnClickListener()", "runningFragmentId: " + runningFragmentId)
                //runningFragmentId = mViewPager.currentItem
                var currentFragment = pageAdapter.getItem(runningFragmentId) as PageFragment
                postSettings = currentFragment.viewModel.getPreset().value ?: Settings()

                postSettings.direction = 0
                postSettings.state = "start"
                //postSettings.presetFragment = currentFragmentId
                viewModel.setSettings(postSettings)
            }

            try {
                mWebSocketClient!!.send(postSettings.getJSON().toString())
//                drawer.setDrawerLockMode(LOCK_MODE_LOCKED_CLOSED)
            }
            catch (e: Exception) {
                Toast.makeText(application, "Turnable not connected", Toast.LENGTH_SHORT).show()
                buttonStartState()
            }
        })
        btnRunCCW.setOnClickListener(View.OnClickListener {

            if (postSettings.state == "pause") {
                postSettings.state = "started"
            }
            else {
                runningFragmentId = mViewPager.currentItem
                var currentFragment = pageAdapter.getItem(runningFragmentId) as PageFragment
                postSettings = currentFragment.viewModel.getPreset().value ?: Settings()

                postSettings.direction = 1
                postSettings.state = "start"
                viewModel.setSettings(postSettings)
            }
            try {
                mWebSocketClient!!.send(postSettings.getJSON().toString())
            }
            catch (e: Exception) {
                Toast.makeText(application, "Turnable not connected", Toast.LENGTH_SHORT).show()
                buttonStartState()
            }
        })
        btnSTOP.setOnClickListener(View.OnClickListener {
            if (postSettings.state == "started") {
                Log.d("MainActivity.btnSTOP", "postSettings.state = " + postSettings.state)
                postSettings.state = "pause"

            }
            //sharedPrefs?.state = "stop"
            //currentFragmentId = mViewPager.currentItem
            //var currentFragment = pageAdapter.getItem(currentFragmentId) as PageFragment
            //postSettings = Settings(currentFragment.viewModel.getPreset(currentFragmentId).value ?:
            //                                Settings().getJSON().toString())
            else if (postSettings.state == "pause"){
                Log.d("MainActivity.btnSTOP", "postSettings.state = " + postSettings.state)
                if (runningFragmentId == NO_FRAGMENT_RUNNING)
                    runningFragmentId = mViewPager.currentItem
                var currentFragment = pageAdapter.getItem(runningFragmentId) as PageFragment
                if (currentFragment != null)
                    currentFragment.viewModel.initPreferencesRequest()
                postSettings.state = "stop"
                //postSettings.framesLeft = postSettings.frame
                viewModel.setSettings(postSettings)
                //viewModel.setTtRun(false)
                //buttonStartState()
            }
            try {
                mWebSocketClient!!.send(postSettings.getJSON().toString())
            }
            catch (e: Exception) {
                Toast.makeText(application, "Turnable not connected", Toast.LENGTH_SHORT).show()
            }
//            runningFragmentId = NO_FRAGMENT_RUNNING
        })

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            if(checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            {
                requestPermissions(Array<String>(1){Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_WIFI);
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public fun onMessage(event: wsMessage){

//        Log.d("AcViewModel onMessage()", Settings.FRAMES_LEFT + ": " + postSettings.value?.framesLeft)
//        Log.d("AcViewModel onMessage()", Settings.STATE + ": " + postSettings.value?.state)

        postSettings.framesLeft = event.framesLeft
        postSettings.state = event.state
        viewModel.setSettings(postSettings)
    }

    override fun onBackPressed() {
        val drawer: DrawerLayout = findViewById(R.id.drawer_layout)
        if (drawer.isDrawerOpen(GravityCompat.START)) run {
                drawer.closeDrawer(GravityCompat.START)
            //mTutorialHandler.cleanUp();
        }
        else
        super.onBackPressed()
    }

    override fun onResume() {

        Log.d("onResume()", "runningFragmentId: " + runningFragmentId)
        if (wsConnected)
        {
            menu?.getItem(0)?.setIcon(applicationContext.getDrawable(R.drawable.ic_action_connected))

/*            if (runningFragmentId != NO_FRAGMENT_RUNNING) {
                //postSettings = Settings(settingsPrefs.presetArray[currentFragmentId].get())
                //postSettings.state = "started"
                disableButton()
                Handler().postDelayed(Runnable() {
                    //do something
                    viewModel.setSettings(postSettings)
                }, 2000//time in milisecond
                )
            }
            else*/
            viewModel.setSettings(postSettings)
        }

        if (runningFragmentId == NO_FRAGMENT_RUNNING)
            mViewPager.currentItem = currentFragmentId
        else
            mViewPager.currentItem = runningFragmentId

        super.onResume()
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
        Log.d("MainActivity.onStop()", "MainActivity.onStop()")
        super.onStop()
        //disconnectWebSocket()
    }

    override fun onPause() {
        currentFragmentId = mViewPager.currentItem
        //runningFragmentId = runningFragmentId
        Log.d("MainActivity.onPause()", "currentFragmentId = $currentFragmentId")
        super.onPause()
    }

    override fun onDestroy() {
        //settingsPrefs.runningFragmentId = runningFragmentId
        Log.d("MainActivity.onDestroy", "runningFragmentId = $runningFragmentId")
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
                        arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), PERMISSION_WRITE_SD)
            }
        } else if (id == R.id.nav_restore) {

            if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {

                startActivity(Intent("intent.action.restore_photo360"))
                //drawer.closeDrawer(Gravity.LEFT, false)

            } else {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), PERMISSION_READ_SD)
            }
        } else if (id == R.id.nav_help) {

            startActivity(Intent("intent.action.photo360_help"))
        }

        //        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        //drawer.closeDrawer(GravityCompat.START);
        return true
    }

    private fun disableButton() {
        //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        btnRunCW.isEnabled = false
        btnRunCCW.isEnabled = false
        btnSTOP.isEnabled = false
        btnRunCW.setImageResource(R.drawable.clockwise_disable)
        btnRunCCW.setImageResource(R.drawable.anticlockwise_disable)
        btnSTOP.setImageResource(R.drawable.pause_disable)
    }

    private fun enableButton() {
        //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        btnRunCW.isEnabled = true
        btnRunCCW.isEnabled = true
        btnSTOP.isEnabled = true
        btnRunCW.setImageResource(R.drawable.clockwise)
        btnRunCCW.setImageResource(R.drawable.anticlockwise)
        btnSTOP.setImageResource(R.drawable.redbutton)
    }

    private fun buttonStartState() {
        //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        btnRunCW.isEnabled = true
        btnRunCCW.isEnabled = true
        btnSTOP.isEnabled = false
        runningFragmentId = NO_FRAGMENT_RUNNING
        btnRunCW.setImageResource(R.drawable.clockwise)
        btnRunCCW.setImageResource(R.drawable.anticlockwise)
        btnSTOP.setImageResource(R.drawable.pause_disable)
    }

    private fun buttonStopState() {
        //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        btnRunCW.isEnabled = false
        btnRunCCW.isEnabled = false
        btnSTOP.isEnabled = true
        //runningFragmentId = NO_FRAGMENT_RUNNING
        btnRunCW.setImageResource(R.drawable.clockwise_disable)
        btnRunCCW.setImageResource(R.drawable.anticlockwise_disable)
        btnSTOP.setImageResource(R.drawable.pause)
    }

    private fun buttonPauseState() {
        //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        if (postSettings.direction == 0) {
            btnRunCW.isEnabled = true
            btnRunCCW.isEnabled = false
            btnSTOP.isEnabled = true
            btnRunCW.setImageResource(R.drawable.clockwise)
            btnRunCCW.setImageResource(R.drawable.anticlockwise_disable)
            btnSTOP.setImageResource(R.drawable.redbutton)
        }
        else {
            btnRunCW.isEnabled = false
            btnRunCCW.isEnabled = true
            btnSTOP.isEnabled = true
            btnRunCW.setImageResource(R.drawable.clockwise_disable)
            btnRunCCW.setImageResource(R.drawable.anticlockwise)
            btnSTOP.setImageResource(R.drawable.redbutton)
        }

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
/*                if (btnRunCW.isEnabled )
                    disableButton()
                else
                    enableButton()*/
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
                    Toast.makeText(application, getString(R.string.turntable_connected), Toast.LENGTH_SHORT).show()
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
                    //Log.d("onMessage()", Settings.FRAMES_LEFT + ": " + frameleft)
                    //Log.d("onMessage()", "allFrames: " + allSteps)
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
                    Toast.makeText(application, getString(R.string.turntable_disconnected), Toast.LENGTH_SHORT).show()
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
                    Toast.makeText(application, getString(R.string.turntable_not_found), Toast.LENGTH_SHORT).show()
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
                    Toast.makeText(application, getString(R.string.turntable_not_found), Toast.LENGTH_SHORT).show()
                    menu?.getItem(0)?.setIcon(applicationContext.getDrawable(R.drawable.ic_action_connect))
                    mprogresBar.visibility = View.INVISIBLE
                }
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            PERMISSION_WRITE_SD -> {
                // If request is cancelled, the result arrays are empty.
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.

                    Timer().schedule(object : TimerTask() {
                        override fun run() {
                            // EXECUTE ACTIONS (LIKE FRAGMENT TRANSACTION ETC.)
                            val newFragment = BackupDialog()
                            newFragment.show(supportFragmentManager, "backup_dialog")
                        }
                    }, 0)

                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(application, getString(R.string.storage_denied), Toast.LENGTH_SHORT).show()
                }
                return
            }
            PERMISSION_READ_SD -> {
                if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    startActivity(Intent("intent.action.restore_photo360"))
                }
                else {
                    //                    Snackbar.make(tabLayout, getResources().getString(R.string.ext_stor_perm_denied),
                    //                            Snackbar.LENGTH_LONG).show();
                    Toast.makeText(this, R.string.ext_stor_perm_denied, Toast.LENGTH_LONG).show()
                }}
            PERMISSION_WIFI -> {
                if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {

                }
                else {
                    //                    Snackbar.make(tabLayout, getResources().getString(R.string.ext_stor_perm_denied),
                    //                            Snackbar.LENGTH_LONG).show();
                    Toast.makeText(this, "Permission to scan WiFi denied", Toast.LENGTH_LONG).show()
                }}

            // Add other 'when' lines to check for other
            // permissions this app might request.
            else -> {
                // Ignore all other requests.
            }
        }
    }
}


