package com.example.webviewkit

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.view.children
import androidx.core.view.get
import androidx.fragment.app.Fragment
import androidx.fragment.app.findFragment
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import com.example.webviewkit.base.BaseActivity
import com.example.webviewkit.common.Defines
import com.example.webviewkit.databinding.ActivityMainBinding
import com.example.webviewkit.view.ui.MainFragment
import android.R
import androidx.navigation.get


class MainActivity : BaseActivity<ActivityMainBinding> (
    { ActivityMainBinding.inflate(it) }
) {
    private var onClickListener : OnClickListener? = null

    interface OnClickListener {
        fun backButtonClick()
    }

    public fun setOnClickListener(listener: OnClickListener) {
        this.onClickListener = listener
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }


    override fun onBackPressed() {
        //super.onBackPressed()
        if (mBinding.navHostFragment.childCount > 0) {
            if (mBinding.navHostFragment.findNavController().currentDestination?.label
                == "MainFragment") {
                onClickListener?.backButtonClick()
            }
        }


    }



}