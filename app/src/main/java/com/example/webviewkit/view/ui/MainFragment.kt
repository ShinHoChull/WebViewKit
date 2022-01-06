package com.example.webviewkit.view.ui


import android.app.Activity
import android.app.DownloadManager
import android.content.ContentValues
import android.content.Context.DOWNLOAD_SERVICE
import android.content.Intent
import android.content.res.ColorStateList
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.Color
import android.media.Image
import android.net.Uri
import android.net.http.SslError
import android.os.Bundle
import android.os.Environment
import android.os.Environment.getExternalStorageDirectory
import android.os.Parcelable
import android.provider.MediaStore
import android.util.Log

import android.view.View
import android.webkit.*
import android.widget.Toast
import androidx.core.net.toUri
import com.example.webviewkit.MainActivity
import com.example.webviewkit.R
import com.example.webviewkit.base.BaseFragment
import com.example.webviewkit.common.Defines
import com.example.webviewkit.databinding.FragmentMainBinding
import com.example.webviewkit.utils.ChromeClient
import com.google.android.datatransport.BuildConfig.VERSION_NAME

import com.google.firebase.encoders.json.BuildConfig
import com.nguyenhoanglam.imagepicker.model.Config
import com.nguyenhoanglam.imagepicker.ui.imagepicker.ImageLoader
import com.nguyenhoanglam.imagepicker.ui.imagepicker.ImagePicker

import java.io.*
import java.net.URI
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            CAPTURE_CAMERA_RESULT -> {
                onCaptureImageResult(data)
            }

            Config.RC_PICK_IMAGES -> {
                if (data != null) {

                    val images: ArrayList<com.nguyenhoanglam.imagepicker.model.Image> = data.getParcelableArrayListExtra(Config.EXTRA_IMAGES)!!
                    var img = Intent().apply {

                         //images[0].path
                    }
                    uploadImageOnPage(resultCode, data)
                } else {
                    /** * 만약 사진촬영이나 선택을 하던중 취소할경우 filePathCallbackLollipop 을 null 로 해줘야
                     * 웹에서 사진첨부를 눌렀을때 이벤트를 다시 받을 수 있다. */

                    filePathCallbackLollipop?.onReceiveValue(null)
                    filePathCallbackLollipop = null
                }
            }
        }
    }

    fun getImageContentUri(path : String) : Uri {

        val cursor = context?.contentResolver?.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        ,  arrayOf(MediaStore.Images.Media._ID)
        , MediaStore.Images.Media.DATA+"=? "
        , arrayOf(path)
        , null
        )

        if (cursor != null && cursor.moveToFirst()) {

            //val id = cursor.getInt(cursor.getColumnIndex(MediaStore.MediaColumns._ID))

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
           setFolderTitle("Photo")
           setDoneTitle("OK")
           setKeepScreenOn(true)
           start()
        }
    }




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
        parentActivity.setOnClickListener(object : MainActivity.OnClickListener {
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
                requireContext(), mBinding.progressBar, this@MainFragment
            )

            settings.apply {
                javaScriptEnabled = true //렌더링한 웹에서 자바스크립트 사용.
                loadsImagesAutomatically = true // 이미지 리소스 자동 로드
                useWideViewPort = true // html 컨텐츠가 웹뷰에 맞게 출력
                javaScriptCanOpenWindowsAutomatically = false // window.open() 동작 허용
                loadsImagesAutomatically = true // 웹뷰에서 앱에 등록되어있는 이미지 리소스를 사용해야 할 경우 자동으로 로드 여부

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

    private fun startIntent(url: String) {

        var packageStr = ""

        val cutStr = url.split("package=")
        if (cutStr.isNotEmpty()) {
            packageStr = cutStr[1].split(";").let { it[0] }
        }

        var intent = requireActivity().packageManager.getLaunchIntentForPackage(packageStr)
        if (intent != null) {

        } else {
            intent = Intent(Intent.ACTION_VIEW).apply {
                addCategory(Intent.CATEGORY_DEFAULT)
                data = Uri.parse("market://details?id=$packageStr")
            }
        }

        startActivity(intent)
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
                startIntent(request?.url.toString())
                return true
            }

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