package io.agora.openlive.activities

import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory
import io.agora.openlive.Constants
import io.agora.openlive.R
import io.agora.openlive.stats.LocalStatsData
import io.agora.openlive.stats.RemoteStatsData
import io.agora.openlive.ui.VideoGridContainer
import io.agora.rtc.IRtcEngineEventHandler.*
import io.agora.rtc.video.VideoEncoderConfiguration.VideoDimensions
import kotlinx.android.synthetic.main.activity_live_room.*

class LiveActivity : RtcBaseActivity() {
    private var mVideoGridContainer: VideoGridContainer? = null
    private var mMuteAudioBtn: ImageView? = null
    private var mMuteVideoBtn: ImageView? = null
    private var mVideoDimension: VideoDimensions? = null
    private var countUser: Int = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_live_room)
        initUI()
        initData()

    }

    private fun initUI() {
        val roomName = findViewById<TextView>(R.id.live_room_name)
        roomName.text = config().channelName
        roomName.isSelected = true
        initUserIcon()
        val role = intent.getIntExtra(
                Constants.KEY_CLIENT_ROLE,
                io.agora.rtc.Constants.CLIENT_ROLE_AUDIENCE)
        val isBroadcaster = role == io.agora.rtc.Constants.CLIENT_ROLE_BROADCASTER
        mMuteVideoBtn = findViewById(R.id.live_btn_mute_video)
        mMuteVideoBtn!!.setActivated(isBroadcaster)
        mMuteAudioBtn = findViewById(R.id.live_btn_mute_audio)
        mMuteAudioBtn!!.setActivated(isBroadcaster)
        val beautyBtn = findViewById<ImageView>(R.id.live_btn_beautification)
        beautyBtn.isActivated = true
        rtcEngine()!!.setBeautyEffectOptions(beautyBtn.isActivated,
                Constants.DEFAULT_BEAUTY_OPTIONS)
        mVideoGridContainer = findViewById(R.id.live_video_grid_layout)
        mVideoGridContainer?.setStatsManager(statsManager())
        rtcEngine()!!.setClientRole(role)
        if (isBroadcaster) startBroadcast()
    }

    private fun initUserIcon() {
        val origin = BitmapFactory.decodeResource(resources, R.drawable.mango_byte_logo)
        val drawable = RoundedBitmapDrawableFactory.create(resources, origin)
        drawable.isCircular = true
        val iconView = findViewById<ImageView>(R.id.live_name_board_icon)
        iconView.setImageDrawable(drawable)
    }

    private fun initData() {
        mVideoDimension = Constants.VIDEO_DIMENSIONS[config().videoDimenIndex]
    }

    override fun onGlobalLayoutCompleted() {
        val topLayout = findViewById<RelativeLayout>(R.id.live_room_top_layout)
        val params = topLayout.layoutParams as RelativeLayout.LayoutParams
        params.height = mStatusBarHeight + topLayout.measuredHeight
        topLayout.layoutParams = params
        topLayout.setPadding(0, mStatusBarHeight, 0, 0)
    }

    private fun startBroadcast() {
        rtcEngine()!!.setClientRole(io.agora.rtc.Constants.CLIENT_ROLE_BROADCASTER)
        val surface = prepareRtcVideo(0, true)
        mVideoGridContainer!!.addUserVideoSurface(0, surface, true)
        mMuteAudioBtn!!.isActivated = true
    }

    private fun stopBroadcast() {
        rtcEngine()!!.setClientRole(io.agora.rtc.Constants.CLIENT_ROLE_AUDIENCE)
        removeRtcVideo(0, true)
        mVideoGridContainer!!.removeUserVideo(0, true)
        mMuteAudioBtn!!.isActivated = false
    }

    override fun onJoinChannelSuccess(channel: String?, uid: Int, elapsed: Int) {

    }

    override fun onUserJoined(uid: Int, elapsed: Int) {

//        fav_icon.text = VideoGridContainer.mUidList.size.toString()
    }

    override fun onUserOffline(uid: Int, reason: Int) {
        runOnUiThread {
            removeRemoteUser(uid)

        }
    }

    override fun onFirstRemoteVideoDecoded(uid: Int, width: Int, height: Int, elapsed: Int) {
        runOnUiThread {
            renderRemoteUser(uid)

        }
    }

    private fun renderRemoteUser(uid: Int) {
        val surface = prepareRtcVideo(uid, false)
        mVideoGridContainer!!.addUserVideoSurface(uid, surface, false)
    }

    private fun removeRemoteUser(uid: Int) {
        removeRtcVideo(uid, false)

        mVideoGridContainer!!.removeUserVideo(uid, false)
    }

    override fun onLocalVideoStats(stats: LocalVideoStats?) {
        if (!statsManager().isEnabled) return
        val data = statsManager().getStatsData(0) as LocalStatsData ?: return
        data.width = mVideoDimension!!.width
        data.height = mVideoDimension!!.height
        data.framerate = stats!!.sentFrameRate
    }

    override fun onRtcStats(stats: RtcStats?) {
        if (!statsManager().isEnabled) return
        val data = statsManager().getStatsData(0) as LocalStatsData ?: return
        data.lastMileDelay = stats!!.lastmileDelay
        data.videoSendBitrate = stats!!.txVideoKBitRate
        data.videoRecvBitrate = stats!!.rxVideoKBitRate
        data.audioSendBitrate = stats!!.txAudioKBitRate
        data.audioRecvBitrate = stats!!.rxAudioKBitRate
        data.cpuApp = stats!!.cpuAppUsage
        data.cpuTotal = stats!!.cpuAppUsage
        data.sendLoss = stats!!.txPacketLossRate
        data.recvLoss = stats!!.rxPacketLossRate
    }

    override fun onNetworkQuality(uid: Int, txQuality: Int, rxQuality: Int) {
        if (!statsManager().isEnabled) return
        val data = statsManager().getStatsData(uid) ?: return
        data.sendQuality = statsManager().qualityToString(txQuality)
        data.recvQuality = statsManager().qualityToString(rxQuality)
    }

    override fun onRemoteVideoStats(stats: RemoteVideoStats?) {
        if (!statsManager().isEnabled) return
        val data = statsManager().getStatsData(stats!!.uid) as RemoteStatsData ?: return
        data.width = stats!!.width
        data.height = stats!!.height
        data.framerate = stats!!.rendererOutputFrameRate
        data.videoDelay = stats!!.delay
    }

    override fun onRemoteAudioStats(stats: RemoteAudioStats?) {
        if (!statsManager().isEnabled) return
        val data = statsManager().getStatsData(stats!!.uid) as RemoteStatsData ?: return
        data.audioNetDelay = stats!!.networkTransportDelay
        data.audioNetJitter = stats!!.jitterBufferDelay
        data.audioLoss = stats!!.audioLossRate
        data.audioQuality = statsManager().qualityToString(stats!!.quality)
    }

    override fun finish() {
        super.finish()
        statsManager().clearAllData()
    }

    fun onLeaveClicked(view: View?) {
        finish()
    }

    fun onSwitchCameraClicked(view: View?) {
        rtcEngine()!!.switchCamera()
    }

    fun onBeautyClicked(view: View) {
        view.isActivated = !view.isActivated
        rtcEngine()!!.setBeautyEffectOptions(view.isActivated,
                Constants.DEFAULT_BEAUTY_OPTIONS)
    }

    fun onMoreClicked(view: View?) {
        // Do nothing at the moment
    }

    fun onPushStreamClicked(view: View?) {
        // Do nothing at the moment
    }

    fun onMuteAudioClicked(view: View) {
        if (!mMuteVideoBtn!!.isActivated) return
        rtcEngine()!!.muteLocalAudioStream(view.isActivated)
        view.isActivated = !view.isActivated
    }

    fun onMuteVideoClicked(view: View) {
        if (view.isActivated) {
            stopBroadcast()
        } else {
            startBroadcast()
        }
        view.isActivated = !view.isActivated
    }

    companion object {
        private val TAG = LiveActivity::class.java.simpleName
    }
}