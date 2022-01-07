package com.example.webviewkit.utils

import android.app.ActionBar
import android.app.Activity
import android.content.Context
import android.net.Uri
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.webkit.*
import android.widget.FrameLayout
import android.widget.ProgressBar
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.example.webviewkit.common.Defines
import android.webkit.WebChromeClient
import android.webkit.ValueCallback
import android.os.Message
import com.example.webviewkit.view.ui.MainFragment


class ChromeClient(
    private val context: Context
    , private val progressbar: ProgressBar
    , private val activity: MainFragment
    , private val parent: ViewGroup
    , private val webViewClient: WebViewClient
    ): WebChromeClient() {

    private var chromeView: View? = null
    private var fullScreenContainer: FullScreenHolder? = null
    private var customViewCallback: CustomViewCallback? = null
    private var originalOrientation: Int = (context as Activity).requestedOrientation

    public var childWebViews = mutableListOf<WebView>()

    companion object {
        private val COVER_SCREEN_PARAMS = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
    }


    override fun onProgressChanged(view: WebView?, newProgress: Int) {
        super.onProgressChanged(view, newProgress)

        progressbar.visibility = View.VISIBLE;
        progressbar.progress = newProgress

        if(newProgress >= 100){
            progressbar.visibility = View.GONE;
        }
    }

    override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
        Log.i("JS Console.log", "${consoleMessage?.message()} , ${consoleMessage?.messageLevel()} , ${consoleMessage?.sourceId()}")
        return super.onConsoleMessage(consoleMessage)
    }

    override fun onShowCustomView(view: View?, callback: CustomViewCallback?) {
        if(chromeView != null) {
            callback?.onCustomViewHidden()
            return
        }

        val decor = (context as Activity).window.decorView as FrameLayout
        fullScreenContainer = FullScreenHolder(context)
        fullScreenContainer?.addView(view, COVER_SCREEN_PARAMS)
        decor.addView(fullScreenContainer, COVER_SCREEN_PARAMS)
        chromeView = view
        setFullScreen(true)
        customViewCallback = callback

        super.onShowCustomView(view, callback)
    }

    override fun onHideCustomView() {
        if(chromeView == null) {
            return;
        }

        setFullScreen(false)
        val decor = (context as Activity).window.decorView as FrameLayout
        decor.removeView(fullScreenContainer)
        fullScreenContainer = null
        chromeView = null
        customViewCallback?.onCustomViewHidden()
        context.requestedOrientation = originalOrientation
    }

    private fun setFullScreen(enable: Boolean) {
        val window = (context as Activity).window
        val winParams = window.attributes
        val bit = WindowManager.LayoutParams.FLAG_FULLSCREEN
        if(enable) {
            winParams.flags = winParams.flags or bit
        } else {
            winParams.flags = winParams.flags and bit.inv()
            if (chromeView != null) {
                chromeView?.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
            }
        }
    }

    override fun onCreateWindow(
        view: WebView?,
        isDialog: Boolean,
        isUserGesture: Boolean,
        resultMsg: Message?
    ): Boolean {

        // 팝업을 위한 웹뷰를 만든다.
        val targetWebView = WebView(context)
        targetWebView.settings.javaScriptEnabled = true
        targetWebView.layoutParams =
            ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ActionBar.LayoutParams.MATCH_PARENT
            ) // 팝업 웹뷰 layout 설정. parent 와 동일해야 함.

        // 팝업을 위한 웹뷰를
        parent.addView(targetWebView)
        childWebViews.add(targetWebView)

        // WebViewTransport 를 통하여 팝업용 웹뷰 전달.
        val transport = resultMsg!!.obj as WebView.WebViewTransport
        transport.webView = targetWebView
        resultMsg.sendToTarget()

        // 팝업용 웹뷰 설정
        targetWebView.webViewClient = webViewClient // 부모 웹뷰와 같은 WebViewClient 를 사용. (URL 처리는 동일하기 때문)
        // window.close() 가 호출될 때 parent view group 에서 삭제하고 childWebView 리스트에서 삭제하여야 함.
        targetWebView.webChromeClient = object : WebChromeClient() {
            override fun onCloseWindow(window: WebView?) {
                super.onCloseWindow(window)
                parent.removeView(targetWebView)
                childWebViews.remove(targetWebView)
            }
        }
        return true
    }

    override fun onJsConfirm(
        view: WebView?,
        url: String?,
        message: String?,
        result: JsResult?
    ): Boolean {
        AlertDialog.Builder(context)
            .setTitle("")
            .setMessage(message)
            .setPositiveButton(android.R.string.ok) { dialog, which -> result?.confirm() }
            .setNegativeButton(android.R.string.cancel) { dialog, which -> result?.cancel()  }
            .create()
            .show()
        return true
    }

    override fun onJsAlert(
        view: WebView?,
        url: String?,
        message: String?,
        result: JsResult?
    ): Boolean {
        AlertDialog.Builder(context)
            .setTitle("")
            .setMessage(message)
            .setPositiveButton(android.R.string.ok) { dialog, which -> result?.confirm() }
            .create()
            .show()
        return true
    }

    class FullScreenHolder(context: Context) : FrameLayout(context) {
        init {
            setBackgroundColor(ContextCompat.getColor(context, android.R.color.white))
        }

        override fun onTouchEvent(event: MotionEvent?): Boolean {
            return true
        }
    }

    var filePathCallbackLollipop: ValueCallback<Array<Uri>>? = null

    override fun onShowFileChooser(
        webView: WebView?,
        filePathCallback: ValueCallback<Array<Uri>>?,
        fileChooserParams: FileChooserParams?
    ): Boolean {

        Defines.log("call File Chooser")

        if (filePathCallbackLollipop != null) {
            filePathCallbackLollipop?.onReceiveValue(null)
            filePathCallbackLollipop = null
        }
        filePathCallbackLollipop = filePathCallback

        val isCapture = fileChooserParams?.isCaptureEnabled

        activity.takePicture(filePathCallbackLollipop)

        filePathCallbackLollipop = null
        return true
    }




}