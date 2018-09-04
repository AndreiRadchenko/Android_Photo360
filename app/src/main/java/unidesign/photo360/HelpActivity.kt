package unidesign.photo360

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.webkit.WebView
import android.webkit.WebViewClient

import unidesign.photo360.R


class HelpActivity : AppCompatActivity() {

    lateinit internal var helpWebView: WebView

    override fun onCreate(savedInstanceState: Bundle?) {


        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_help)
        this.setTitle(R.string.action_help)
        val myToolbar = findViewById(R.id.help_toolbar) as Toolbar
        setSupportActionBar(myToolbar)
        // Get a support ActionBar corresponding to this toolbar
        val ab = supportActionBar
        ab!!.setHomeAsUpIndicator(R.drawable.ic_close)
        //ab.setIcon(R.drawable.ic_close);
        // Enable the Up button
        ab!!.setDisplayHomeAsUpEnabled(true)

        helpWebView = WebView(this)
        // initiate a web view
        helpWebView = findViewById(R.id.HelpWebView)
        helpWebView.webViewClient = MyWebViewClient()


        val webSettings = helpWebView.settings
        //webSettings.setJavaScriptEnabled(true);
        webSettings.builtInZoomControls = true
        webSettings.displayZoomControls = true
        //webSettings.setSupportZoom(true);

        // specify the url of the web page in loadUrl function
        val help_index_path = "file:///android_asset/" + resources.getString(R.string.help_locale) + "/index.html"
        helpWebView.loadUrl(help_index_path)

    }



    override fun onBackPressed() {
        if (helpWebView.canGoBack()) {
            helpWebView.goBack()
        } else {
            setResult(Activity.RESULT_OK, Intent())
            super.onBackPressed()
        }
    }

    companion object {
        val HELP_REQUEST = 2
    }
}
