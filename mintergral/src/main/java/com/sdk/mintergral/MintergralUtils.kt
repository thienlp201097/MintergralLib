package com.sdk.mintergral

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.FrameLayout
import android.widget.LinearLayout
import com.airbnb.lottie.LottieAnimationView
import com.facebook.shimmer.ShimmerFrameLayout
import com.mbridge.msdk.MBridgeSDK
import com.mbridge.msdk.mbbid.out.BannerBidRequestParams
import com.mbridge.msdk.mbbid.out.BidListennning
import com.mbridge.msdk.mbbid.out.BidManager
import com.mbridge.msdk.mbbid.out.BidResponsed
import com.mbridge.msdk.mbbid.out.SplashBidRequestParams
import com.mbridge.msdk.newinterstitial.out.MBBidNewInterstitialHandler
import com.mbridge.msdk.newinterstitial.out.MBNewInterstitialHandler
import com.mbridge.msdk.newinterstitial.out.NewInterstitialListener
import com.mbridge.msdk.out.AutoPlayMode
import com.mbridge.msdk.out.BannerAdListener
import com.mbridge.msdk.out.BannerSize
import com.mbridge.msdk.out.MBBannerView
import com.mbridge.msdk.out.MBBidRewardVideoHandler
import com.mbridge.msdk.out.MBNativeAdvancedHandler
import com.mbridge.msdk.out.MBRewardVideoHandler
import com.mbridge.msdk.out.MBSplashHandler
import com.mbridge.msdk.out.MBSplashLoadListener
import com.mbridge.msdk.out.MBSplashShowListener
import com.mbridge.msdk.out.MBridgeIds
import com.mbridge.msdk.out.MBridgeSDKFactory
import com.mbridge.msdk.out.NativeAdvancedAdListener
import com.mbridge.msdk.out.RewardInfo
import com.mbridge.msdk.out.RewardVideoListener
import com.mbridge.msdk.out.SDKInitStatusListener
import com.sdk.mintergral.callback.AOAListening
import com.sdk.mintergral.callback.BannerListener
import com.sdk.mintergral.callback.InitStatusListener
import com.sdk.mintergral.callback.InterstitialListener
import com.sdk.mintergral.callback.NativeListener
import com.sdk.mintergral.callback.RewardListener
import org.json.JSONObject


object MintergralUtils {

    private var dialogFullScreen: Dialog? = null
    const val TAG = "==MintergralUtils=="
    private var isTestAds = false

    @JvmStatic
    fun isInternetAvailable(context: Context): Boolean {
        val result: Boolean
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkCapabilities = connectivityManager.activeNetwork ?: return false
        val actNw = connectivityManager.getNetworkCapabilities(networkCapabilities) ?: return false
        result = when {
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
            else -> false
        }
        return result
    }

    @JvmStatic
    fun initMintergralSdk(
        context: Context,
        appId: String,
        apiKey: String,isTestAds : Boolean,
        initStatusListener: InitStatusListener
    ) {
        this.isTestAds = isTestAds
        var appIdSdk = appId
        var apiKeySdk = apiKey
        if (isTestAds){
            appIdSdk = "144002"
            apiKeySdk = "7c22942b749fe6a6e361b675e96b3ee9"
        }
        val sdk: MBridgeSDK = MBridgeSDKFactory.getMBridgeSDK()
        val map = sdk.getMBConfigurationMap(appIdSdk, apiKeySdk)
        sdk.init(map, context, object : SDKInitStatusListener {
            override fun onInitSuccess() {
                Log.e("SDKInitStatus", "onInitSuccess")
                initStatusListener.onInitSuccess()
            }

            override fun onInitFail(errorMsg: String) {
                Log.e("SDKInitStatusInitFail", errorMsg)
                initStatusListener.onInitFail()
            }
        })
    }

    @SuppressLint("InflateParams")
    @JvmStatic
    fun showBanner(
        context: Activity,
        viewGroup: ViewGroup,
        adsModel: AdsModel,
        bannerListener: BannerListener
    ) {
        var adsModelSdk = adsModel
        if (isTestAds){
            adsModelSdk =  AdsModel("1010694", "2677210", false)
        }
        val mbBannerView = MBBannerView(context)
        mbBannerView.layoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        )
        mbBannerView.init(
            BannerSize(BannerSize.DEV_SET_TYPE, 720, 50),
            adsModelSdk.placementId,
            adsModelSdk.unitId
        )
        val tagView = context.layoutInflater.inflate(R.layout.layoutbanner_loading, null, false)
        try {
            viewGroup.removeAllViews()
            viewGroup.addView(tagView)
        }catch (_: Exception){

        }
        val shimmerFrameLayout : ShimmerFrameLayout? = tagView.findViewById(R.id.shimmer_view_container)
        shimmerFrameLayout?.startShimmer()

        mbBannerView.setBannerAdListener(object : BannerAdListener {
            override fun onLoadFailed(p0: MBridgeIds?, p1: String?) {
                Log.d(TAG, "onLoadFailed: ")
                p1?.let { bannerListener.loadFailed(it) }
            }

            override fun onLoadSuccessed(p0: MBridgeIds?) {
                Log.d(TAG, "onLoadSuccessed: ")
                bannerListener.loadSuccessed()
                shimmerFrameLayout?.stopShimmer()
                viewGroup.removeAllViews()
                viewGroup.addView(mbBannerView)
            }

            override fun onLogImpression(p0: MBridgeIds?) {

            }

            override fun onClick(p0: MBridgeIds?) {

            }

            override fun onLeaveApp(p0: MBridgeIds?) {

            }

            override fun showFullScreen(p0: MBridgeIds?) {

            }

            override fun closeFullScreen(p0: MBridgeIds?) {

            }

            override fun onCloseBanner(p0: MBridgeIds?) {

            }

        })
        if (adsModelSdk.isBid) {
            var mBidToken: String

            val manager =
                BidManager(BannerBidRequestParams(adsModelSdk.placementId, adsModelSdk.unitId, 320, 50))
            manager.setBidListener(object : BidListennning {
                override fun onFailed(msg: String) {
                    // bid failed
                    Log.d(TAG, "Bid onFailed: $msg")
                    bannerListener.loadFailed(msg)
                }

                override fun onSuccessed(bidResponsed: BidResponsed) {
                    // bid successful
                    mBidToken = bidResponsed.bidToken
                    mbBannerView.loadFromBid(mBidToken)
                }
            })
            manager.bid()
        } else {
            mbBannerView.load()
        }
    }

    private fun dpToPx(dp: Float, context: Context): Int {
        return (dp * context.resources.displayMetrics.density).toInt()
    }

    @SuppressLint("InflateParams")
    @JvmStatic
    fun loadAndShowNative(context: Activity, viewGroup: ViewGroup, adsModel: AdsModel,size : NativeSize,nativeListener: NativeListener) {
        val tagView: View = if (size === NativeSize.UNIFIED_MEDIUM) {
            context.layoutInflater.inflate(R.layout.layoutnative_loading_medium, null, false)
        } else {
            context.layoutInflater.inflate(R.layout.layoutnative_loading_small, null, false)
        }
        try {
            viewGroup.removeAllViews()
            viewGroup.addView(tagView)
        }catch (_ : Exception){

        }
        val shimmerFrameLayout : ShimmerFrameLayout? = tagView.findViewById(R.id.shimmer_view_container)
        shimmerFrameLayout?.startShimmer()

        var adsModelSdk = adsModel
        if (isTestAds){
            adsModelSdk =  AdsModel("328917", "1542077", false)
        }

        val mbNativeAdvancedHandler =
            MBNativeAdvancedHandler(context, adsModelSdk.placementId, adsModelSdk.unitId)
        mbNativeAdvancedHandler.setNativeViewSize(dpToPx(400f, context), dpToPx(250f, context))
        mbNativeAdvancedHandler.autoLoopPlay(AutoPlayMode.PLAY_WHEN_NETWORK_IS_AVAILABLE)

        val style = "{\n" +
                "    \"list\": [{\n" +
                "        \"target\": \"title\",\n" +
                "        \"values\": {\n" +
                "            \"paddingLeft\": 15,\n" +
                "            \"backgroundColor\": \"white\",\n" +
                "            \"fontSize\": 15,\n" +
                "            \"fontFamily\": \"Microsoft YaHei\",\n" +
                "            \"color\": \"red\"\n" +
                "        }\n" +
                "    }, {\n" +
                "        \"target\": \"mediaContent\",\n" +
                "        \"values\": {\n" +
                "            \"paddingTop\": 10,\n" +
                "            \"paddingRight\": 10,\n" +
                "            \"paddingBottom\": 10,\n" +
                "            \"paddingLeft\": 10\n" +
                "        }\n" +
                "    }]\n" +
                "}\n"

        val jsonObject = JSONObject(style)
        mbNativeAdvancedHandler.setViewElementStyle(jsonObject)
        mbNativeAdvancedHandler.setAdListener(object : NativeAdvancedAdListener {
            override fun onLoadFailed(p0: MBridgeIds?, p1: String?) {
                Log.d(TAG, "onLoadFailed: $p1")
                p1?.let { nativeListener.loadFailed(it) }
            }

            override fun onLoadSuccessed(p0: MBridgeIds?) {
                Log.d(TAG, "onLoadSuccessed: Native")
                val mAdvancedNativeView = mbNativeAdvancedHandler.adViewGroup
                shimmerFrameLayout?.stopShimmer()
                viewGroup.removeAllViews()
                viewGroup.addView(mAdvancedNativeView)
                nativeListener.loadSuccessed()
            }

            override fun onLogImpression(p0: MBridgeIds?) {

            }

            override fun onClick(p0: MBridgeIds?) {

            }

            override fun onLeaveApp(p0: MBridgeIds?) {

            }

            override fun showFullScreen(p0: MBridgeIds?) {

            }

            override fun closeFullScreen(p0: MBridgeIds?) {

            }

            override fun onClose(p0: MBridgeIds?) {

            }

        })
        if (adsModelSdk.isBid) {
            var mBidToken: String

            val manager =
                BidManager(BannerBidRequestParams(adsModelSdk.placementId, adsModelSdk.unitId, 320, 50))
            manager.setBidListener(object : BidListennning {
                override fun onFailed(msg: String) {
                    // bid failed
                    Log.d(TAG, "Bid onFailed: $msg")
                    nativeListener.loadFailed(msg)
                }

                override fun onSuccessed(bidResponsed: BidResponsed) {
                    // bid successful
                    mBidToken = bidResponsed.bidToken
                    mbNativeAdvancedHandler.loadByToken(mBidToken)
                }
            })
            manager.bid()
        } else {
            mbNativeAdvancedHandler.load()
        }
    }

    @JvmStatic
    fun loadAndShoeInterstitial(
        context: Context,
        adsModel: AdsModel,isDialog : Boolean,
        interstitialListener: InterstitialListener
    ) {

        var adsModelSdk = adsModel
        if (isTestAds){
            adsModelSdk =  AdsModel("290653", "462374", false)
        }
        if (isDialog){
            dialogLoading(context)
        }
        val mMBInterstitalVideoHandler = MBNewInterstitialHandler(context, adsModelSdk.placementId, adsModelSdk.unitId)
        val mMBBidNewInterstitialHandler = MBBidNewInterstitialHandler(context, adsModelSdk.placementId, adsModelSdk.unitId)

        val newInterstitialListener = object :
            NewInterstitialListener {
            override fun onLoadCampaignSuccess(p0: MBridgeIds?) {

            }

            override fun onResourceLoadSuccess(p0: MBridgeIds?) {
                dismissAdDialog()
                if (adsModelSdk.isBid){
                    if (mMBBidNewInterstitialHandler.isBidReady) {
                        // Use this method to determine if the video asset is ready for playback. It is recommended to display the ad only when it's playable.
                        mMBBidNewInterstitialHandler.showFromBid() //show ad
                    } else {
                        interstitialListener.onLoadFailed("Inter Bid not Ready")
                    }
                }else{
                    if (mMBInterstitalVideoHandler.isReady) {
                        // Use this method to determine if the video asset is ready for playback. It is recommended to display the ad only when it's playable.
                        mMBInterstitalVideoHandler.show() //show ad
                    } else {
                        interstitialListener.onLoadFailed("Inter not Ready")
                    }
                }
            }

            override fun onResourceLoadFail(p0: MBridgeIds?, p1: String?) {
                p1?.let { interstitialListener.onLoadFailed(it) }
                dismissAdDialog()
            }

            override fun onAdShow(p0: MBridgeIds?) {
                interstitialListener.onShowSuccessed()
            }

            override fun onAdClose(p0: MBridgeIds?, p1: RewardInfo?) {
                interstitialListener.onAdClose()
                dismissAdDialog()
            }

            override fun onShowFail(p0: MBridgeIds?, p1: String?) {
                p1?.let { interstitialListener.onShowFailed(it) }
                dismissAdDialog()
            }

            override fun onAdClicked(p0: MBridgeIds?) {

            }

            override fun onVideoComplete(p0: MBridgeIds?) {

            }

            override fun onAdCloseWithNIReward(p0: MBridgeIds?, p1: RewardInfo?) {

            }

            override fun onEndcardShow(p0: MBridgeIds?) {

            }
        }
        if (!adsModelSdk.isBid) {
            mMBInterstitalVideoHandler.setInterstitialVideoListener(newInterstitialListener)
            mMBInterstitalVideoHandler.load()
        } else {
            mMBBidNewInterstitialHandler.setInterstitialVideoListener(newInterstitialListener)
            var mBidToken: String

            val manager = BidManager(adsModelSdk.placementId, adsModelSdk.unitId)
            manager.setBidListener(object : BidListennning {
                override fun onFailed(msg: String) {
                    // bid failed
                    Log.d(TAG, "Bid onFailed: $msg")
                    interstitialListener.onLoadFailed(msg)
                }

                override fun onSuccessed(bidResponsed: BidResponsed) {
                    // bid successful
                    mBidToken = bidResponsed.bidToken
                    mMBBidNewInterstitialHandler.loadFromBid(mBidToken)
                }
            })
            manager.bid()
        }
    }

    @JvmStatic
    fun loadAndShowAOA(context: Activity,timeOut : Long, adsModel: AdsModel,isDialog: Boolean, aoaListening: AOAListening){
        var adsModelSdk = adsModel
        if (isTestAds){
            adsModelSdk =  AdsModel("328916","1542060",false)
        }
        var mBidToken: String? = null
        if (isDialog){
            dialogLoading(context)
        }
        val mMBSplashHandler = MBSplashHandler(context, adsModelSdk.placementId,adsModelSdk.unitId)
        mMBSplashHandler.setLoadTimeOut(timeOut)

        mMBSplashHandler.setSplashLoadListener(object : MBSplashLoadListener {
            override fun onLoadSuccessed(ids: MBridgeIds, reqType: Int) {
                /**
                 * Ad loaded successfully
                 */
                aoaListening.loadSuccessed()
                Handler(Looper.getMainLooper()).postDelayed({
                    dismissAdDialog()
                    if (!adsModelSdk.isBid){
                        if (mMBSplashHandler.isReady) {
                            mMBSplashHandler.show(context) // container
                        }
                    }else{
                        if (mMBSplashHandler.isReady(mBidToken)) {
                            mMBSplashHandler.show(context, mBidToken)
                        }
                    }
                },1000)
                Log.i(TAG, "onLoadSuccessed: $reqType $ids")
            }

            override fun onLoadFailed(ids: MBridgeIds, msg: String, reqType: Int) {
                /**
                 * Ad failed to load
                 */
                aoaListening.loadFailed(msg)
                Log.e(TAG, "onLoadFailed: $msg  $reqType $ids")
            }

            override fun isSupportZoomOut(ids: MBridgeIds, support: Boolean) {
                /**
                 * Whether zoom out is supported
                 */
                Log.i(TAG, "isSupportZoomOut: $support ids$ids")
            }
        })

        mMBSplashHandler.setSplashShowListener(object : MBSplashShowListener {
            override fun onShowSuccessed(ids: MBridgeIds) {
                /**
                 * Ad shown successfully
                 */
                aoaListening.onShowSuccessed()
                Log.i(TAG, "onShowSuccessed: $ids")
            }

            override fun onShowFailed(ids: MBridgeIds, msg: String) {
                /**
                 * Ad failed to show
                 */
                aoaListening.onShowFailed(msg)
                Log.e(TAG, "onShowFailed: $msg $ids")
            }

            override fun onAdClicked(ids: MBridgeIds) {
                /**
                 * Ad clicked
                 */
                Log.i(TAG, "onAdClicked: $ids")
            }

            override fun onDismiss(ids: MBridgeIds, type: Int) {
                /**
                 * App Open Ad closed
                 * @param type: 1 for user skipped; 2 for countdown finished; 3 for clicking the ad to exit the app
                 */
                aoaListening.onAdClose()
                Log.i(TAG, "onDismiss: $type $ids")
            }

            override fun onAdTick(ids: MBridgeIds, millisUntilFinished: Long) {
                /**
                 * Countdown callback
                 * @param millisUntilFinished: Time remaining until countdown finishes in milliseconds
                 */
                Log.i(TAG, "onAdTick: $millisUntilFinished $ids")
            }

            override fun onZoomOutPlayStart(ids: MBridgeIds) {
                // Notify the SDK when the developer adds the float view to the appropriate position
                Log.i(TAG, "onZoomOutPlayStart: $ids")
            }

            override fun onZoomOutPlayFinish(ids: MBridgeIds) {
                // Notify the SDK when the developer removes the float view from the parent layout
                Log.e(TAG, "onZoomOutPlayFinish: $ids")
            }
        })

        if (!adsModelSdk.isBid){
            mMBSplashHandler.preLoad()
        }else{
            val manager = BidManager(SplashBidRequestParams(adsModelSdk.placementId, adsModelSdk.unitId, true, Configuration.ORIENTATION_PORTRAIT, 156, 156))
            manager.setBidListener(object : BidListennning {
                override fun onFailed(msg: String) {
                    // bid failed
                    aoaListening.loadFailed(msg)
                }

                override fun onSuccessed(bidResponsed: BidResponsed) {
                    // bid successful
                    mBidToken = bidResponsed.bidToken
                    mMBSplashHandler.preLoadByToken(mBidToken)
                }
            })
            manager.bid()
        }
    }

    fun loadAndShowReward(context: Activity,adsModel: AdsModel,isDialog : Boolean,rewardListener: RewardListener){
        var adsModelSdk = adsModel
        if (isTestAds){
            adsModelSdk =  AdsModel("290651", "462372", false)
        }
        if (isDialog){
            dialogLoading(context)
        }

        var mBidToken: String

        val mMBRewardVideoHandler = MBRewardVideoHandler(context, adsModelSdk.placementId, adsModelSdk.unitId)
        val mMBBidRewardVideoHandler = MBBidRewardVideoHandler(context, adsModelSdk.placementId, adsModelSdk.unitId)

        val rewardVideoListener = object : RewardVideoListener {
            override fun onVideoLoadSuccess(p0: MBridgeIds?) {

            }

            override fun onLoadSuccess(p0: MBridgeIds?) {
                dismissAdDialog()
                if (!adsModelSdk.isBid){
                    if (mMBRewardVideoHandler.isReady) {
                        rewardListener.onLoadSuccessed()
                        mMBRewardVideoHandler.show() // Client reward callback without passing parameters
                    }else{
                        rewardListener.onLoadFailed("")
                    }
                }else{
                    if (mMBBidRewardVideoHandler.isBidReady) {
                        rewardListener.onLoadSuccessed()
                        mMBBidRewardVideoHandler.showFromBid() // Client reward callback without passing parameters
                    }else{
                        rewardListener.onLoadFailed("")
                    }
                }
            }

            override fun onVideoLoadFail(p0: MBridgeIds?, p1: String?) {
                dismissAdDialog()
                rewardListener.onLoadFailed(p1.toString())
            }

            override fun onAdShow(p0: MBridgeIds?) {
                rewardListener.onShowSuccessed()
            }

            override fun onAdClose(p0: MBridgeIds?, rewardInfo: RewardInfo?) {
                // If 'rewardInfo.isCompleteView()' returns true, it means that the user can be rewarded.
                var isCompleteView = false
                rewardInfo?.let {
                    Log.d(TAG, "onAdClose: ${it.isCompleteView}")
                    isCompleteView = it.isCompleteView
                }
                rewardListener.onAdClose(isCompleteView)

            }

            override fun onShowFail(p0: MBridgeIds?, p1: String?) {
                dismissAdDialog()
                rewardListener.onShowFailed(p1.toString())
            }

            override fun onVideoAdClicked(p0: MBridgeIds?) {

            }

            override fun onVideoComplete(p0: MBridgeIds?) {

            }

            override fun onEndcardShow(p0: MBridgeIds?) {

            }
        }
        if (!adsModelSdk.isBid){
            mMBRewardVideoHandler.setRewardVideoListener(rewardVideoListener)
            mMBRewardVideoHandler.load()
        }else{
            mMBBidRewardVideoHandler.setRewardVideoListener(rewardVideoListener)
            val manager = BidManager(adsModelSdk.placementId, adsModelSdk.unitId)
            manager.setBidListener(object : BidListennning {
                override fun onFailed(msg: String) {
                    // bid failed
                    println("Bid failed: $msg")
                }

                override fun onSuccessed(bidResponse: BidResponsed?) {
                    mBidToken = bidResponse?.bidToken.toString()
                    println("Bid succeeded, token: $mBidToken")
                    mMBBidRewardVideoHandler.loadFromBid(mBidToken)
                }
            })
            manager.bid()
        }
    }

    private fun dialogLoading(context: Context) {
        dialogFullScreen = Dialog(context)
        dialogFullScreen?.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialogFullScreen?.setContentView(R.layout.dialog_full_screen)
        dialogFullScreen?.setCancelable(false)
        dialogFullScreen?.window!!.setBackgroundDrawable(ColorDrawable(Color.WHITE))
        dialogFullScreen?.window!!.setLayout(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT
        )
        val img = dialogFullScreen?.findViewById<LottieAnimationView>(R.id.imageView3)
        img?.setAnimation(R.raw.gifloading)
        try {
            if (dialogFullScreen != null && dialogFullScreen?.isShowing == false) {
                dialogFullScreen?.show()
            }
        } catch (ignored: Exception) {
        }

    }

    @JvmStatic
    fun dismissAdDialog() {
        try {
            if (dialogFullScreen != null && dialogFullScreen?.isShowing == true) {
                dialogFullScreen?.dismiss()
            }
        }catch (_: Exception){
            Log.d(TAG, "dismissAdDialog: Error")
        }
    }
}