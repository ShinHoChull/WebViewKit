package com.example.webviewkit.view.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.ColorFilter
import android.net.Uri
import android.net.http.SslError
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.*
import com.example.webviewkit.BuildConfig
import com.example.webviewkit.MainActivity
import com.example.webviewkit.R
import com.example.webviewkit.base.BaseFragment
import com.example.webviewkit.common.Defines
import com.example.webviewkit.databinding.FragmentMainBinding
import com.example.webviewkit.databinding.FragmentSplashBinding
import com.example.webviewkit.utils.ChromeClient

class MainFragment : BaseFragment<FragmentMainBinding> (
    FragmentMainBinding::inflate
) {

    public lateinit var mWebView : WebView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Defines.log("MainFragment-> onViewCreated")

        mBinding.progressBar.progressTintList = ColorStateList
            .valueOf(Color.parseColor("#E5D85C"))
        mWebView = mBinding.webView

        setUpListener()
        setUpWebView()
    }

    private fun setUpListener() {
        val parentActivity = activity as MainActivity
        parentActivity.setOnClickListener(object : MainActivity.OnClickListener{
            override fun backButtonClick() {
                if (mWebView.canGoBack()) {
                    mWebView.goBack()
                }
            }
        })

    }

    override fun onResume() {
        super.onResume()
    }


    private fun setUpWebView() {

        mBinding.webView.apply {

            webViewClient = webViewClientCustom()
            webChromeClient = ChromeClient(
                requireContext()
                , mBinding.progressBar)

            settings.apply {
                javaScriptEnabled = true
                loadsImagesAutomatically= true // 이미지 리소스 자동 로드
                useWideViewPort = true // html컨텐츠가 웹뷰에 맞게 출력
                javaScriptCanOpenWindowsAutomatically = false // window.open() 동작 허용
                loadsImagesAutomatically = true // 웹뷰에서 앱에 등록되어있는 이미지 리소스를 사용해야 할 경우 자동으로 로드 여부
                cacheMode = WebSettings.LOAD_CACHE_ELSE_NETWORK // LOAD_NO_CACHE -> 캐시 사용 x 네트워크로만 호출,
                // LOAD_NORMAL -> 기본적인 모드로 캐시 사용,
                // LOAD_DEFAULT  -> 평소엔 LOAD_NORAML 캐시가 만료된 경우 네트워크를 사용,
                // LOAD_CACHE_ONLY -> 캐시만 사용,
                // LOAD_CACHE_ELSE_NETWORK 캐시가 없을 경우 네트워크 사용

                domStorageEnabled = true // 로컬 스토리지 사용하여 dom 가져올 수 있도록 함
                allowFileAccess = true // 웹뷰 내에서 파일 액세스 활성화 여부

                val userAgent = java.lang.String.format(
                    "%s [%s/%s]",
                    userAgentString,
                    "App/Android",
                    BuildConfig.VERSION_NAME
                )
                Defines.log("agent->${userAgent}")

                userAgentString = userAgent// 웹에서 해당속성을 통해 앱으로 인지 하도록
            }

            loadUrl(Defines.baseUrl)
        }
    }

    inner class webViewClientCustom : WebViewClient() {

        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
            super.onPageStarted(view, url, favicon)
        }

        override fun shouldOverrideUrlLoading(
            view: WebView?,
            request: WebResourceRequest?
        ): Boolean {
            Defines.log("should->${request?.url.toString()}")

            if (request?.url.toString().contains("intent")) {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(request?.url.toString()))
                startActivity(intent)
                return true
            }
            //view?.loadUrl(request?.url.toString())
            return false
        }

        override fun onPageFinished(view: WebView?, url: String?) {
            Defines.log("Finis->${url.toString()}")
            super.onPageFinished(view, url)
        }

        override fun onLoadResource(view: WebView?, url: String?) {
            super.onLoadResource(view, url)
        }

        override fun onReceivedError(
            view: WebView?,
            request: WebResourceRequest?,
            error: WebResourceError?
        ) {
            Defines.log("error->${error.toString()}")
            super.onReceivedError(view, request, error)

        }

        override fun onReceivedSslError(
            view: WebView?,
            handler: SslErrorHandler?,
            error: SslError?
        ) {
            //super.onReceivedSslError(view, handler, error)
            handler?.proceed()
        }
    }




}