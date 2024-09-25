package com.sdk.mintergral.callback

interface AOAListening {
    fun loadSuccessed()
    fun loadFailed(msg: String)
    fun onShowSuccessed()
    fun onShowFailed(msg: String)
    fun onAdClose()

}