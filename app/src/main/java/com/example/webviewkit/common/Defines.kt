package com.example.webviewkit.common

import android.annotation.SuppressLint
import android.util.Log
import java.text.SimpleDateFormat
import java.util.*

class Defines {

    companion object {

        const val baseUrl = "https://m.naver.com/"
        private const val pattern = "yyyy-MM-dd HH:mm:ss"

        @SuppressLint("SimpleDateFormat")
        private val simpleDateFormat = SimpleDateFormat(pattern)

        public fun log(
            log : String ,
            logName : String = "YONG"
        ) {
            Log.d(logName , "==================")
            Log.d(logName , log)
            Log.d("consumed time",simpleDateFormat.format(Date()))
            Log.d(logName , "=====================")
        }

        public fun logE(
            log : String ,
            logName : String = "YONG"
        ) {
            Log.e(logName , "==================")
            Log.e(logName , log)
            Log.e("consumed time",simpleDateFormat.format(Date()))
            Log.e(logName , "=====================")
        }

    }
}