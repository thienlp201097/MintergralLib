package com.sdk.mintergral.callback

import com.mbridge.msdk.out.RewardInfo

interface RewardListener {
    fun onLoadSuccessed()
    fun onLoadFailed(errorMsg: String)
    fun onShowSuccessed()
    fun onShowFailed(errorMsg: String)
    fun onAdClose(isCompleted: Boolean)

}