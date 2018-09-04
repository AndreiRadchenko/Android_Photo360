package unidesign.photo360


import android.content.Intent
import android.util.Log
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient

class MyWebViewClient : WebViewClient() {

    override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {

        Log.d("MyWebViewClient", request.url.toString())
        if (request.url.toString().contains("android_asset")) {
            // This is my web site, so do not override; let my WebView load the page

            return false
        }
        // Otherwise, the link is not for a page on my site, so launch another Activity that handles URLs
        val intent = Intent(Intent.ACTION_VIEW, request.url)
        view.context.startActivity(intent)

        return true
    }
}