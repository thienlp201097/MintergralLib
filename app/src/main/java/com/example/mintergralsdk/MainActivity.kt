package com.example.mintergralsdk

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.mintergralsdk.databinding.ActivityMainBinding
import com.sdk.mintergral.AdsModel
import com.sdk.mintergral.MintergralUtils
import com.sdk.mintergral.NativeSize
import com.sdk.mintergral.callback.BannerListener
import com.sdk.mintergral.callback.InterstitialListener
import com.sdk.mintergral.callback.NativeListener
import com.sdk.mintergral.callback.RewardListener

class MainActivity : AppCompatActivity() {

    val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    val bannerUnit = AdsModel("1010694", "2677210", false)
    val nativeUnit = AdsModel("328917", "1542077", false)
    val interstitialUnit = AdsModel("290653", "462374", false)
    val reward = AdsModel("290651", "462372", false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.btnShowReward.setOnClickListener { 
            MintergralUtils.loadAndShowReward(this, reward,true, object : RewardListener {
                override fun onLoadSuccessed() {
                    
                }

                override fun onLoadFailed(errorMsg: String) {
                    
                }

                override fun onShowSuccessed() {
                    
                }

                override fun onShowFailed(errorMsg: String) {
                    
                }

                override fun onAdClose(isCompleted: Boolean) {
                    
                }
            })
        }

        binding.btnShowInter.setOnClickListener {
            MintergralUtils.loadAndShoeInterstitial(
                this,
                interstitialUnit, true,
                object : InterstitialListener {
                    override fun onLoadSuccessed() {

                    }

                    override fun onLoadFailed(errorMsg: String) {

                    }

                    override fun onShowSuccessed() {

                    }

                    override fun onShowFailed(errorMsg: String) {

                    }

                    override fun onAdClose() {

                    }
                })
        }
        MintergralUtils.showBanner(this, binding.flBanner, bannerUnit,
            object :
                BannerListener {
                override fun loadFailed(msg: String) {

                }

                override fun loadSuccessed() {

                }
            })

        MintergralUtils.loadAndShowNative(
            this,
            binding.flNative,
            nativeUnit,
            NativeSize.UNIFIED_MEDIUM,
            object : NativeListener {
                override fun loadFailed(msg: String) {

                }

                override fun loadSuccessed() {

                }
            })
    }


}