package com.example.webviewkit.utils

import android.content.Context
import android.util.AttributeSet
import android.webkit.WebView
import android.webkit.WebSettings

import android.os.Build
import android.webkit.CookieManager

import androidx.annotation.RequiresApi
import com.example.webviewkit.common.Defines


class WebViewC : WebView {

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    init {
        setUpView()
    }

    private fun setUpView() {

        this.api21Code()
    }


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    fun api21Code() {
        this.settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        val cookieManager: CookieManager = CookieManager.getInstance()
        cookieManager.setAcceptCookie(true)
        cookieManager.setAcceptThirdPartyCookies(this, true)
    }



}