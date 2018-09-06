package unidesign.photo360


import android.annotation.TargetApi
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.util.Log
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient

class MyWebViewClient : WebViewClient() {

    @TargetApi(Build.VERSION_CODES.N)
    override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
        /*        Log.d("MyWebViewClient", request.url.toString())
        if (request.url.toString().contains("android_asset")) {
            // This is my web site, so do not override; let my WebView load the page

            return false
        }
        // Otherwise, the link is not for a page on my site, so launch another Activity that handles URLs
        val intent = Intent(Intent.ACTION_VIEW, request.url)
        view.context.startActivity(intent)

        return true*/
        val uri = request.url
        return handleUri(uri, view);
    }

    @SuppressWarnings("deprecation")
    override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
        val uri = Uri.parse(url)
        return handleUri(uri, view);
    }

    private fun handleUri(uri: Uri, view: WebView): Boolean {
        Log.d("MyWebViewClient", "Uri =" + uri);
        val host = uri.getHost();
        val scheme = uri.getScheme();
        // Based on some condition you need to determine if you are going to load the url
        // in your web view itself or in a browser.
        // You can use `host` or `scheme` or any part of the `uri` to decide.
        if (uri.toString().contains("android_asset")) {
            // Returning false means that you are going to load this url in the webView itself
            return false;
        } else {
            // Returning true means that you need to handle what to do with the url
            // e.g. open web page in a Browser
            val intent = Intent(Intent.ACTION_VIEW, uri)
            view.context.startActivity(intent)
            return true;
        }
    }

}