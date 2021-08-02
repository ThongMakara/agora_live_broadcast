package io.agora.openlive.activities

import android.os.Bundle
import android.util.DisplayMetrics
import android.view.View
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.view.Window
import androidx.appcompat.app.AppCompatActivity
import io.agora.openlive.AgoraApplication
import io.agora.openlive.rtc.EngineConfig
import io.agora.openlive.rtc.EventHandler
import io.agora.openlive.stats.StatsManager
import io.agora.openlive.utils.WindowUtil
import io.agora.rtc.IRtcEngineEventHandler.*
import io.agora.rtc.RtcEngine

abstract class BaseActivity : AppCompatActivity(), EventHandler {
    @JvmField
    protected var mDisplayMetrics = DisplayMetrics()
    @JvmField
    protected var mStatusBarHeight = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowUtil.hideWindowStatusBar(window)
        setGlobalLayoutListener()
        displayMetrics
        initStatusBarHeight()
    }

    private fun setGlobalLayoutListener() {
        val layout = findViewById<View>(Window.ID_ANDROID_CONTENT)
        val observer = layout.viewTreeObserver
        observer.addOnGlobalLayoutListener(object : OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                layout.viewTreeObserver.removeOnGlobalLayoutListener(this)
                onGlobalLayoutCompleted()
            }
        })
    }


    protected open fun onGlobalLayoutCompleted() {}
    private val displayMetrics: Unit
        private get() {
            windowManager.defaultDisplay.getMetrics(mDisplayMetrics)
        }

    private fun initStatusBarHeight() {
        mStatusBarHeight = WindowUtil.getSystemStatusBarHeight(this)
    }

    protected fun application(): AgoraApplication {
        return application as AgoraApplication
    }

    protected fun rtcEngine(): RtcEngine? {
        return application().rtcEngine()
    }

    protected fun config(): EngineConfig {
        return application().engineConfig()
    }

    protected fun statsManager(): StatsManager {
        return application().statsManager()
    }

    protected fun registerRtcEventHandler(handler: EventHandler?) {
        application().registerEventHandler(handler)
    }

    protected fun removeRtcEventHandler(handler: EventHandler?) {
        application().removeEventHandler(handler)
    }


    override fun onFirstRemoteVideoDecoded(uid: Int, width: Int, height: Int, elapsed: Int) {}

    override fun onJoinChannelSuccess(channel: String?, uid: Int, elapsed: Int) {
    }

    override fun onLeaveChannel(stats: RtcStats?) {
        TODO("Not yet implemented")
    }

    override fun onUserOffline(uid: Int, reason: Int) {}


    override fun onUserJoined(uid: Int, elapsed: Int) {

    }
    override fun onLastmileQuality(quality: Int) {}

    override fun onLastmileProbeResult(result: LastmileProbeResult?) {

    }

    override fun onLocalVideoStats(stats: LocalVideoStats?) {

    }

    override fun onRtcStats(stats: RtcStats?) {

    }


    override fun onNetworkQuality(uid: Int, txQuality: Int, rxQuality: Int) {}

    override fun onRemoteVideoStats(stats: RemoteVideoStats?) {
        TODO("Not yet implemented")
    }

    override fun onRemoteAudioStats(stats: RemoteAudioStats?) {
        TODO("Not yet implemented")
    }
}