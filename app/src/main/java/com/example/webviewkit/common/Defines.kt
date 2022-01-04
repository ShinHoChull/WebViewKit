package com.example.webviewkit.common

import android.annotation.SuppressLint
import android.util.Log
import java.text.SimpleDateFormat
import java.util.*

class Defines {

    companion object {
        private const val baseUrl = "https://www.naver.com"
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
    }
}