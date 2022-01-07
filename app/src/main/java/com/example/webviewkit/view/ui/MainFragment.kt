package com.example.webviewkit.view.ui


import android.app.Activity
import android.app.DownloadManager
import android.content.ContentValues
import android.content.Context.DOWNLOAD_SERVICE
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.net.http.SslError
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Environment.getExternalStorageDirectory
import android.provider.MediaStore

import android.view.View
import android.view.ViewGroup
import android.webkit.*
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.net.toUri
import com.example.webviewkit.MainActivity
import com.example.webviewkit.base.BaseFragment
import com.example.webviewkit.common.Defines
import com.example.webviewkit.databinding.FragmentMainBinding
import com.example.webviewkit.utils.ChromeClient

import com.google.firebase.encoders.json.BuildConfig
import com.nguyenhoanglam.imagepicker.model.Config
import com.nguyenhoanglam.imagepicker.ui.imagepicker.ImagePicker

import java.io.*
import java.lang.Exception
import java.net.URLDecoder


interface IImageHandler {
    fun takePicture(callBack: ValueCallback<Array<Uri>>?)
    fun uploadImageOnPage(resultCode: Int, intent: Intent?)
}

class MainFragment : BaseFragment<FragmentMainBinding>(
    FragmentMainBinding::inflate
), IImageHandler {

    private val CAPTURE_CAMERA_RESULT = 3089
    private var filePathCallbackLollipop: ValueCallback<Array<Uri>>? = null

    public lateinit var mWebView: WebView
    private lateinit var mChromeClient: ChromeClient

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            CAPTURE_CAMERA_RESULT -> {
                onCaptureImageResult(data)
            }

            Config.RC_PICK_IMAGES -> {
                if (data != null) {

                    val images: ArrayList<com.nguyenhoanglam.imagepicker.model.Image> =
                        data.getParcelableArrayListExtra(Config.EXTRA_IMAGES)!!

                    val intent = Intent().apply {
                        this.data = getImageContentUri(images[0].path)
                    }

                    uploadImageOnPage(resultCode, intent)
                } else {
                    /** * 만약 사진촬영이나 선택을 하던중 취소할경우 filePathCallbackLollipop 을 null 로 해줘야
                     * 웹에서 사진첨부를 눌렀을때 이벤트를 다시 받을 수 있다. */

                    filePathCallbackLollipop?.onReceiveValue(null)
                    filePathCallbackLollipop = null
                }
            }
        }
    }


    private fun getImageContentUri(path: String): Uri? {

        val cursor = context?.contentResolver?.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            arrayOf(MediaStore.Images.Media._ID),
            MediaStore.Images.Media.DATA + "=? ",
            arrayOf(path),
            null
        )

        if (cursor != null && cursor.moveToFirst()) {
            val idx = cursor.getColumnIndex(MediaStore.MediaColumns._ID)
            val id = cursor.getInt(idx)
            return Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id.toString())

        } else if (path.isNotEmpty()) {

            val values = ContentValues()
            values.put(MediaStore.Images.Media.DATA, path)
            return context?.contentResolver?.insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values
            )
        }
        return null
    }


    override fun uploadImageOnPage(resultCode: Int, intent: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            if (intent != null) {
                filePathCallbackLollipop?.onReceiveValue(
                    WebChromeClient.FileChooserParams.parseResult(Activity.RESULT_OK, intent)
                )
                filePathCallbackLollipop = null
            }
        } else {
            /** * 만약 사진촬영이나 선택을 하던중 취소할경우 filePathCallbackLollipop 을
            null 로 해줘야 * 웹에서 사진첨부를 눌렀을때 이벤트를 다시 받을 수 있다. */
            filePathCallbackLollipop?.onReceiveValue(null)
            filePathCallbackLollipop = null
        }
    }

    /** * 카메라 작동후 전달된 인텐트를 받는다. */
    private fun onCaptureImageResult(data: Intent?) {
        if (data == null) {
            /** * 만약 사진촬영이나 선택을 하던중 취소할경우 filePathCallbackLollipop 을 null 로 해줘야
             * 웹에서 사진첨부를 눌렀을때 이벤트를 다시 받을 수 있다. */

            filePathCallbackLollipop?.onReceiveValue(null)
            filePathCallbackLollipop = null
            return
        }
        val thumbnail = data.extras!!.get("data") as Bitmap
        saveImage(thumbnail)
    }

    /** * 비트맵을 로컬에 물리적 이미지 파일로 저장시킨다. */
    private fun saveImage(bitmap: Bitmap) {
        val bytes = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
        // create a directory if it doesn't already exist
        val photoDirectory = File(getExternalStorageDirectory().absolutePath + "/cameraphoto/")
        if (!photoDirectory.exists()) {
            photoDirectory.mkdirs()
        }

        val imgFile = File(photoDirectory, "${System.currentTimeMillis()}.jpg")
        val fo: FileOutputStream
        try {
            imgFile.createNewFile()
            fo = FileOutputStream(imgFile)
            fo.write(bytes.toByteArray())
            fo.close()
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        uploadImageOnPage(Activity.RESULT_OK, Intent().apply { data = imgFile.toUri() })
    }

    override fun takePicture(callBack: ValueCallback<Array<Uri>>?) {
        filePathCallbackLollipop = callBack
        showSelectCameraOrImage()
    }

    /** * 카메라 / 갤러리 선택 팝업을 표시한다. */
    private fun showSelectCameraOrImage() {

        galleryIntent()

//        CameraOrImageSelectDialog(object: CameraOrImageSelectDialog.OnClickSelectListener {
//            override fun onClickCamera() {
//                cameraIntent()
//            }
//            override fun onClickImage() {
//                galleryIntent()
//            }
//        }).show(requireActivity().supportFragmentManager, "CameraOrImageSelectDialog")
    }

    /** * 이미지 선택 */
    private fun galleryIntent() {

        ImagePicker.with(this).run {
            setToolbarColor("#FFFFFF")
            setStatusBarColor("#FFFFFF")
            setToolbarTextColor("#000000")
            setToolbarIconColor("#000000")
            setProgressBarColor("#FFC300")
            setBackgroundColor("#FFFFFF")
            setCameraOnly(false)
            setMultipleMode(false)
            setFolderMode(true)
            setShowCamera(false)
            setFolderTitle("MY PHOTO")
            setDoneTitle("OK")
            setKeepScreenOn(true)
            start()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mBinding.progressBar.progressTintList = ColorStateList
            .valueOf(Color.parseColor("#E5D85C"))

        mWebView = mBinding.webView

        setUpListener()
        setUpWebView()
    }

    private fun setUpListener() {

        val parentActivity = activity as MainActivity
        parentActivity.setOnClickListener(object : MainActivity.OnClickListener {
            override fun backButtonClick() {

                //팝업 이 열려있을경우.
                mChromeClient.childWebViews.firstOrNull {
                    if (it.canGoBack()) {
                        it.goBack()
                        return
                    }
                    mBinding.parentLayout.removeView(it)
                    mChromeClient.childWebViews.remove(it)
                    return
                }


                if (mWebView.canGoBack()) {
                    mWebView.goBack()
                    return
                }
                parentActivity.finish()
            }
        })
    }

    override fun onResume() {
        super.onResume()
    }


    private fun setUpWebView() {

        mBinding.webView.apply {

            webViewClient = webViewClientCustom()

            mChromeClient = ChromeClient(
                requireContext()
                , mBinding.progressBar
                , this@MainFragment
                , mBinding.parentLayout as ViewGroup
                , webViewClientCustom()
            ).apply {
                webChromeClient = this
            }

            settings.apply {
                javaScriptEnabled = true //렌더링한 웹에서 자바스크립트 사용.
                loadsImagesAutomatically = true // 이미지 리소스 자동 로드
                useWideViewPort = true // html 컨텐츠가 웹뷰에 맞게 출력
                javaScriptCanOpenWindowsAutomatically = false // window.open() 동작 허용
                loadsImagesAutomatically = true // 웹뷰에서 앱에 등록되어있는 이미지 리소스를 사용해야 할 경우 자동으로 로드 여부
                setSupportMultipleWindows(true) //Popup 관련 .
                cacheMode =
                    WebSettings.LOAD_CACHE_ELSE_NETWORK // LOAD_NO_CACHE -> 캐시 사용 x 네트워크로만 호출,
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


            setDownloadListener { url, userAgent, contentDisposition, mimetype, contentLength ->

                val request = DownloadManager.Request(Uri.parse(url))
                val filename = URLUtil.guessFileName(url, contentDisposition, mimetype)

                val cookies = CookieManager.getInstance().getCookie(url)
                request.addRequestHeader("cookie", cookies)
                request.addRequestHeader("User-Agent", userAgent)
                request.setDescription("Downloading file..")
                request.setTitle(URLDecoder.decode(filename, "UTF-8"))
                request.allowScanningByMediaScanner()
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                request.setDestinationInExternalPublicDir(
                    Environment.DIRECTORY_DOWNLOADS,
                    URLDecoder.decode(filename, "UTF-8")
                )

                val dManager =
                    requireContext().getSystemService(DOWNLOAD_SERVICE) as DownloadManager
                dManager.enqueue(request)

                Toast.makeText(requireContext(), "다운로드가 완료되었습니다.", Toast.LENGTH_SHORT).show()
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
            if (arrayOf("http", "https").contains(request?.url?.scheme)) return false // http, https 주소는 웹뷰가 직접 load 한다.

            Defines.log("should->${request?.url.toString()}")

            val intent = Intent.parseUri(
                request?.url?.toString(),
                Intent.URI_INTENT_SCHEME
            )

            if (intent.resolveActivity(requireActivity().packageManager) != null) {
                startActivity(intent)
                return true
            }

            // 실행 가능한 앱이 없으면 인텐트에 fallback  url 값이 있는지 체크한다.
            if (!resortToFallbackUrl(view!!, intent)) {

                if (!resortToToPackage(intent)) {
                    Toast.makeText(requireContext(), "URI Intent 에 해당하는 애플리케이션을 찾을 수 없습니다.", Toast.LENGTH_LONG)
                        .show() // fallback url 이 없는 경우, market 으로 보내지 않고 유저에게 실행불가능 설명.
                }
            }

            return true
        }

        override fun onReceivedError(
            view: WebView?,
            request: WebResourceRequest?,
            error: WebResourceError?
        ) {
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

    /**
     * 브라우저에서 들어온 intent 에서 browser_fallback_url 을 찾아서 실행시키는 메소드
     * fallback url validation 은 따로하지 않는데, 필요하면 해야함.
     *
     * @return true if webView loaded fallback url, false otherwise
     */
    private fun resortToFallbackUrl(webView: WebView, intent: Intent): Boolean {
        val fallbackUrl = intent.getStringExtra("browser_fallback_url")
        if (fallbackUrl != null && fallbackUrl != "null") {
            webView.loadUrl(fallbackUrl)
            return true
        }
        return false
    }

    /**
     *browser_fallback_url 을 가져오지 못할경우 package를 찾아 실행 시킴.
     */
    private fun resortToToPackage( intent : Intent ) : Boolean {
        val packageStr = intent.`package`

        if (packageStr != null && packageStr != "") {
            val viewIntent = Intent().apply {
                addCategory(Intent.CATEGORY_DEFAULT)
                this.data = Uri.parse("market://details?id=$packageStr")
            }
            startActivity(viewIntent)
            return true
        }
        return false
    }


}