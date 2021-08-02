package io.agora.openlive

import android.app.Application
import io.agora.openlive.rtc.AgoraEventHandler
import io.agora.openlive.rtc.EngineConfig
import io.agora.openlive.rtc.EventHandler
import io.agora.openlive.stats.StatsManager
import io.agora.openlive.utils.FileUtil.initializeLogFile
import io.agora.openlive.utils.PrefManager.getPreferences
import io.agora.rtc.RtcEngine

class AgoraApplication : Application() {
    private var mRtcEngine: RtcEngine? = null
    private val mGlobalConfig = EngineConfig()
    private val mHandler = AgoraEventHandler()
    private val mStatsManager = StatsManager()
    override fun onCreate() {
        super.onCreate()
        try {
            mRtcEngine = RtcEngine.create(applicationContext, getString(R.string.private_app_id), mHandler)
            // Sets the channel profile of the Agora RtcEngine.
            // The Agora RtcEngine differentiates channel profiles and applies different optimization algorithms accordingly. For example, it prioritizes smoothness and low latency for a video call, and prioritizes video quality for a video broadcast.
            mRtcEngine!!.setChannelProfile(io.agora.rtc.Constants.CHANNEL_PROFILE_LIVE_BROADCASTING)
            mRtcEngine!!.enableVideo()
            mRtcEngine!!.setLogFile(initializeLogFile(this))
        } catch (e: Exception) {
            e.printStackTrace()
        }
        initConfig()
    }

    private fun initConfig() {
        val pref = getPreferences(applicationContext)
        mGlobalConfig.videoDimenIndex = pref.getInt(
                Constants.PREF_RESOLUTION_IDX, Constants.DEFAULT_PROFILE_IDX)
        val showStats = pref.getBoolean(Constants.PREF_ENABLE_STATS, false)
        mGlobalConfig.setIfShowVideoStats(showStats)
        mStatsManager.enableStats(showStats)
        mGlobalConfig.mirrorLocalIndex = pref.getInt(Constants.PREF_MIRROR_LOCAL, 0)
        mGlobalConfig.mirrorRemoteIndex = pref.getInt(Constants.PREF_MIRROR_REMOTE, 0)
        mGlobalConfig.mirrorEncodeIndex = pref.getInt(Constants.PREF_MIRROR_ENCODE, 0)
    }

    fun engineConfig(): EngineConfig {
        return mGlobalConfig
    }

    fun rtcEngine(): RtcEngine? {
        return mRtcEngine
    }

    fun statsManager(): StatsManager {
        return mStatsManager
    }

    fun registerEventHandler(handler: EventHandler?) {
        mHandler.addHandler(handler!!)
    }

    fun removeEventHandler(handler: EventHandler?) {
        mHandler.removeHandler(handler!!)
    }

    override fun onTerminate() {
        super.onTerminate()
        RtcEngine.destroy()
    }
}