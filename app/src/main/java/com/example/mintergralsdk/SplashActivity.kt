package com.example.mintergralsdk

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.sdk.mintergral.AdsModel
import com.sdk.mintergral.MintergralUtils
import com.sdk.mintergral.callback.AOAListening
import com.sdk.mintergral.callback.InitStatusListener

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {
    val aoa = AdsModel("328916","1542060",false)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_splash)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        MintergralUtils.initMintergralSdk(this, "144002", "7c22942b749fe6a6e361b675e96b3ee9",true, object :
            InitStatusListener {
            override fun onInitSuccess() {
                MintergralUtils.loadAndShowAOA(this@SplashActivity,20000,aoa,true,object :AOAListening{
                    override fun loadSuccessed() {
                        
                    }

                    override fun loadFailed(msg: String) {
                        startActivity(Intent(this@SplashActivity,MainActivity::class.java))
                    }

                    override fun onShowSuccessed() {
                        
                    }

                    override fun onShowFailed(msg: String) {
                        startActivity(Intent(this@SplashActivity,MainActivity::class.java))
                    }

                    override fun onAdClose() {
                        startActivity(Intent(this@SplashActivity,MainActivity::class.java))
                    }

                })
            }

            override fun onInitFail() {
                startActivity(Intent(this@SplashActivity,MainActivity::class.java))
            }
        })
    }
}