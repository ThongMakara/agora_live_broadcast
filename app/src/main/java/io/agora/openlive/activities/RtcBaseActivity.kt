package io.agora.openlive.activities

import android.os.Bundle
import android.text.TextUtils
import android.view.SurfaceView
import io.agora.openlive.Constants
import io.agora.openlive.R
import io.agora.openlive.rtc.EventHandler
import io.agora.rtc.RtcEngine
import io.agora.rtc.video.VideoCanvas
import io.agora.rtc.video.VideoEncoderConfiguration

abstract class RtcBaseActivity : BaseActivity(), EventHandler {
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        registerRtcEventHandler(this)
//        configVideo()
//        joinChannel()
//    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        registerRtcEventHandler(this)
        configVideo()
        joinChannel()
    }

    private fun configVideo() {
        val configuration = VideoEncoderConfiguration(
                Constants.VIDEO_DIMENSIONS[config().videoDimenIndex],
                VideoEncoderConfiguration.FRAME_RATE.FRAME_RATE_FPS_15,
                VideoEncoderConfiguration.STANDARD_BITRATE,
                VideoEncoderConfiguration.ORIENTATION_MODE.ORIENTATION_MODE_FIXED_PORTRAIT
        )
        configuration.mirrorMode = Constants.VIDEO_MIRROR_MODES[config().mirrorEncodeIndex]
        rtcEngine()!!.setVideoEncoderConfiguration(configuration)
    }

    private fun joinChannel() {
        var token: String? = getString(R.string.agora_access_token)
//        if (TextUtils.isEmpty(token) || TextUtils.equals(token, "#YOUR ACCESS TOKEN#")) {
//            token = null // default, no token
//        }
        rtcEngine()!!.joinChannel(null, "LiveStream", "", 0)
    }

    override fun onUserJoined(uid: Int, elapsed: Int) {
        super.onUserJoined(uid, elapsed)

    }
    protected fun prepareRtcVideo(uid: Int, local: Boolean): SurfaceView {
        // Render local/remote video on a SurfaceView
        val surface = RtcEngine.CreateRendererView(applicationContext)
        if (local) {
            rtcEngine()!!.setupLocalVideo(
                    VideoCanvas(
                            surface,
                            VideoCanvas.RENDER_MODE_HIDDEN,
                            0,
                            Constants.VIDEO_MIRROR_MODES[config().mirrorLocalIndex]
                    )
            )
        } else {
            rtcEngine()!!.setupRemoteVideo(
                    VideoCanvas(
                            surface,
                            VideoCanvas.RENDER_MODE_HIDDEN,
                            uid,
                            Constants.VIDEO_MIRROR_MODES[config().mirrorRemoteIndex]
                    )
            )
        }
        return surface
    }

    protected fun removeRtcVideo(uid: Int, local: Boolean) {
        if (local) {
            rtcEngine()!!.setupLocalVideo(null)
        } else {
            rtcEngine()!!.setupRemoteVideo(VideoCanvas(null, VideoCanvas.RENDER_MODE_HIDDEN, uid))
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        removeRtcEventHandler(this)
        rtcEngine()!!.leaveChannel()
    }
}