package com.example.webviewkit.utils

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.webkit.*
import android.widget.FrameLayout
import android.widget.ProgressBar
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.webviewkit.R
import com.example.webviewkit.common.Defines
import java.io.ByteArrayOutputStream
import android.webkit.WebChromeClient

import android.webkit.ValueCallback
import android.webkit.WebChromeClient.FileChooserParams
import androidx.core.app.ActivityCompat.startActivityForResult

import android.content.DialogInterface
import android.os.Environment
import android.text.TextUtils
import androidx.core.content.FileProvider
import androidx.fragment.app.FragmentActivity
import com.example.webviewkit.view.ui.IImageHandler
import com.example.webviewkit.view.ui.MainFragment
import java.io.File
import java.io.IOException


class ChromeClient(
    private val context: Context
    , private val progressbar: ProgressBar
    , private val activity : MainFragment
    ): WebChromeClient() {


    private var chromeView: View? = null
    private var fullScreenContainer: FullScreenHolder? = null
    private var customViewCallback: CustomViewCallback? = null
    private var originalOrientation: Int = (context as Activity).requestedOrientation

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
        (context as Activity).requestedOrientation = originalOrientation

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