package com.example.webviewkit

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.webviewkit.base.BaseActivity
import com.example.webviewkit.databinding.ActivityMainBinding

class MainActivity : BaseActivity<ActivityMainBinding> (
    { ActivityMainBinding.inflate(it) }
) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }
}