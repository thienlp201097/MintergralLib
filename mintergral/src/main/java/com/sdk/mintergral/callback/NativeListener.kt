package com.sdk.mintergral.callback

interface NativeListener {
    fun loadFailed(msg: String)
    fun loadSuccessed()
}