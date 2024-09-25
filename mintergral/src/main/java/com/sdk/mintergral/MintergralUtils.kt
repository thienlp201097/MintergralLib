package com.sdk.mintergral

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.os.Handler
import android.os.Looper
import android.util.DisplayMetrics
import android.util.Log
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.FrameLayout
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
import com.mbridge.msdk.out.MBNativeAdvancedHandler
import com.mbridge.msdk.out.MBSplashHandler
import com.mbridge.msdk.out.MBSplashLoadListener
import com.mbridge.msdk.out.MBSplashShowListener
import com.mbridge.msdk.out.MBridgeIds
import com.mbridge.msdk.out.MBridgeSDKFactory
import com.mbridge.msdk.out.NativeAdvancedAdListener
import com.mbridge.msdk.out.RewardInfo
import com.mbridge.msdk.out.SDKInitStatusListener
import com.sdk.mintergral.callback.AOAListening
import com.sdk.mintergral.callback.BannerListener
import com.sdk.mintergral.callback.InitStatusListener
import com.sdk.mintergral.callback.InterstitialListener
import org.json.JSONObject


object MintergralUtils {
    val TAG = "==MintergralUtils=="
    fun initMintergralSdk(
        context: Context,
        appId: String,
        apiKey: String,
        initStatusListener: InitStatusListener
    ) {
        val sdk: MBridgeSDK = MBridgeSDKFactory.getMBridgeSDK()
        val map = sdk.getMBConfigurationMap(appId, apiKey)

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

    fun showBanner(
        context: Context,
        viewGroup: ViewGroup,
        adsModel: AdsModel,
        bannerListener: BannerListener
    ) {
        val mbBannerView = MBBannerView(context)
        mbBannerView.layoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        )
        mbBannerView.init(
            BannerSize(BannerSize.DEV_SET_TYPE, 720, 50),
            adsModel.placementId,
            adsModel.unitId
        )
        mbBannerView.setBannerAdListener(object : BannerAdListener {
            override fun onLoadFailed(p0: MBridgeIds?, p1: String?) {
                Log.d(TAG, "onLoadFailed: ")
                p1?.let { bannerListener.loadFailed(it) }
            }

            override fun onLoadSuccessed(p0: MBridgeIds?) {
                Log.d(TAG, "onLoadSuccessed: ")
                bannerListener.loadSuccessed()
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
        if (adsModel.isBid) {
            var mBidToken: String

            val manager =
                BidManager(BannerBidRequestParams(adsModel.placementId, adsModel.unitId, 320, 50))
            manager.setBidListener(object : BidListennning {
                override fun onFailed(msg: String) {
                    // bid failed
                    Log.d(TAG, "Bid onFailed: $msg")
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

    fun dpToPx(dp: Float, context: Context): Int {
        return (dp * context.resources.displayMetrics.density).toInt()
    }

    fun getScreenWidthInPx(context: Context): Int {
        val displayMetrics = DisplayMetrics()
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        return displayMetrics.widthPixels
    }

    fun loadAndShowNative(
        context: Activity,
        viewGroup: ViewGroup, adsModel: AdsModel
    ) {
        val mbNativeAdvancedHandler =
            MBNativeAdvancedHandler(context, adsModel.placementId, adsModel.unitId)
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
            }

            override fun onLoadSuccessed(p0: MBridgeIds?) {
                Log.d(TAG, "onLoadSuccessed: Native")
                val mAdvancedNativeView = mbNativeAdvancedHandler.adViewGroup
                viewGroup.addView(mAdvancedNativeView)
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
        if (adsModel.isBid) {
            var mBidToken: String

            val manager =
                BidManager(BannerBidRequestParams(adsModel.placementId, adsModel.unitId, 320, 50))
            manager.setBidListener(object : BidListennning {
                override fun onFailed(msg: String) {
                    // bid failed
                    Log.d(TAG, "Bid onFailed: $msg")
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

    fun loadAndShoeInterstitial(
        context: Context,
        adsModel: AdsModel,
        interstitialListener: InterstitialListener
    ) {
        if (!adsModel.isBid) {
            val mMBInterstitalVideoHandler =
                MBNewInterstitialHandler(context, adsModel.placementId, adsModel.unitId)
            mMBInterstitalVideoHandler.setInterstitialVideoListener(object :
                NewInterstitialListener {
                override fun onLoadCampaignSuccess(p0: MBridgeIds?) {

                }

                override fun onResourceLoadSuccess(p0: MBridgeIds?) {
                    if (mMBInterstitalVideoHandler.isReady) {
                        // Use this method to determine if the video asset is ready for playback. It is recommended to display the ad only when it's playable.
                        mMBInterstitalVideoHandler.show() //show ad
                    } else {
                        interstitialListener.onLoadFailed("Inter not Ready")
                    }
                }

                override fun onResourceLoadFail(p0: MBridgeIds?, p1: String?) {
                    p1?.let { interstitialListener.onLoadFailed(it) }
                }

                override fun onAdShow(p0: MBridgeIds?) {
                    interstitialListener.onShowSuccessed()
                }

                override fun onAdClose(p0: MBridgeIds?, p1: RewardInfo?) {
                    interstitialListener.onAdClose()
                }

                override fun onShowFail(p0: MBridgeIds?, p1: String?) {
                    p1?.let { interstitialListener.onShowFailed(it) }
                }

                override fun onAdClicked(p0: MBridgeIds?) {

                }

                override fun onVideoComplete(p0: MBridgeIds?) {

                }

                override fun onAdCloseWithNIReward(p0: MBridgeIds?, p1: RewardInfo?) {

                }

                override fun onEndcardShow(p0: MBridgeIds?) {

                }

            })
            mMBInterstitalVideoHandler.load()
        } else {
            val mMBBidNewInterstitialHandler =
                MBBidNewInterstitialHandler(context, adsModel.placementId, adsModel.unitId)
            mMBBidNewInterstitialHandler.setInterstitialVideoListener(object :
                NewInterstitialListener {
                override fun onLoadCampaignSuccess(p0: MBridgeIds?) {

                }

                override fun onResourceLoadSuccess(p0: MBridgeIds?) {
                    if (mMBBidNewInterstitialHandler.isBidReady) {
                        // Use this method to determine if the video asset is ready for playback. It is recommended to display the ad only when it's playable.
                        mMBBidNewInterstitialHandler.showFromBid() //show ad
                    } else {
                        interstitialListener.onLoadFailed("Inter Bid not Ready")
                    }
                }

                override fun onResourceLoadFail(p0: MBridgeIds?, p1: String?) {
                    p1?.let { interstitialListener.onLoadFailed(it) }
                }

                override fun onAdShow(p0: MBridgeIds?) {
                    interstitialListener.onShowSuccessed()
                }

                override fun onAdClose(p0: MBridgeIds?, p1: RewardInfo?) {
                    interstitialListener.onAdClose()
                }

                override fun onShowFail(p0: MBridgeIds?, p1: String?) {
                    p1?.let { interstitialListener.onShowFailed(it) }
                }

                override fun onAdClicked(p0: MBridgeIds?) {

                }

                override fun onVideoComplete(p0: MBridgeIds?) {

                }

                override fun onAdCloseWithNIReward(p0: MBridgeIds?, p1: RewardInfo?) {

                }

                override fun onEndcardShow(p0: MBridgeIds?) {

                }

            })
            var mBidToken: String

            val manager = BidManager(adsModel.placementId, adsModel.unitId)
            manager.setBidListener(object : BidListennning {
                override fun onFailed(msg: String) {
                    // bid failed
                    Log.d(TAG, "Bid onFailed: $msg")
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


    fun loadAndShowAOA(context: Activity,timeOut : Long, adsModel: AdsModel, aoaListening: AOAListening){
        var mBidToken: String? = null

        val mMBSplashHandler = MBSplashHandler(context, adsModel.placementId,adsModel.unitId)
        mMBSplashHandler.setLoadTimeOut(timeOut)

        mMBSplashHandler.setSplashLoadListener(object : MBSplashLoadListener {
            override fun onLoadSuccessed(ids: MBridgeIds, reqType: Int) {
                /**
                 * Ad loaded successfully
                 */
                aoaListening.loadSuccessed()
                Handler(Looper.getMainLooper()).postDelayed({
                    if (!adsModel.isBid){
                        if (mMBSplashHandler.isReady) {
                            mMBSplashHandler.show(context) // container
                        }
                    }else{
                        if (mMBSplashHandler.isReady(mBidToken)) {
                            mMBSplashHandler.show(context, mBidToken);
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

        if (!adsModel.isBid){
            mMBSplashHandler.preLoad()
        }else{
            val manager = BidManager(SplashBidRequestParams(adsModel.placementId, adsModel.unitId, true, Configuration.ORIENTATION_PORTRAIT, 156, 156))
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
}