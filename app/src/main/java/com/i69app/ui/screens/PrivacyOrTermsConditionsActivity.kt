package com.i69app.ui.screens

import android.os.Bundle
import android.view.Window.FEATURE_NO_TITLE
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import com.i69app.data.config.Constants

class PrivacyOrTermsConditionsActivity : AppCompatActivity() {

    private lateinit var mWebView: WebView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.requestFeature(FEATURE_NO_TITLE)
        mWebView = WebView(this)
        mWebView.loadUrl(getUrl())
        mWebView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                view.loadUrl(url)
                return true
            }
        }

        this.setContentView(mWebView)
    }

    private fun getUrl() =
        if (intent.hasExtra("type") && intent.getStringExtra("type") == "privacy") com.i69app.data.config.Constants.URL_PRIVACY_POLICY else com.i69app.data.config.Constants.URL_TERMS_AND_CONDITION

}