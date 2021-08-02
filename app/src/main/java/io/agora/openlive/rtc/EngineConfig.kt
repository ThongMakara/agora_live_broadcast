package io.agora.openlive.rtc

import io.agora.openlive.Constants

class EngineConfig {
    // private static final int DEFAULT_UID = 0;
    // private int mUid = DEFAULT_UID;
    var channelName: String? = null
    private var mShowVideoStats = false
    var videoDimenIndex = Constants.DEFAULT_PROFILE_IDX
    var mirrorLocalIndex = 0
    var mirrorRemoteIndex = 0
    var mirrorEncodeIndex = 0

    fun ifShowVideoStats(): Boolean {
        return mShowVideoStats
    }

    fun setIfShowVideoStats(show: Boolean) {
        mShowVideoStats = show
    }

}