package com.sdk.mintergral.callback

interface InterstitialListener {
    fun onLoadSuccessed()
    fun onLoadFailed(errorMsg: String)
    fun onShowSuccessed()
    fun onShowFailed(errorMsg: String)
    fun onAdClose()

}