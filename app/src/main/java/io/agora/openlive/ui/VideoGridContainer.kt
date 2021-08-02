package io.agora.openlive.ui

import android.content.Context
import android.graphics.Color
import android.os.Handler
import android.util.AttributeSet
import android.util.SparseArray
import android.view.SurfaceView
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import io.agora.openlive.R
import io.agora.openlive.stats.StatsManager
import java.util.*

class VideoGridContainer : RelativeLayout, Runnable {
    private val mUserViewList = SparseArray<ViewGroup>(MAX_USER)
    private val mUidList: MutableList<Int> = ArrayList(MAX_USER)
    private var mStatsManager: StatsManager? = null
    private var mHandler: Handler? = null
    private var mStatMarginBottom = 0
//
    constructor(context: Context?) : super(context) {
        init()
    }
//
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init()
    }

    private fun init() {
        setBackgroundResource(R.drawable.live_room_bg)
        mStatMarginBottom = resources.getDimensionPixelSize(
                R.dimen.live_stat_margin_bottom)
        mHandler = Handler(context.mainLooper)
    }

    fun setStatsManager(manager: StatsManager?) {
        mStatsManager = manager
    }

    fun addUserVideoSurface(uid: Int, surface: SurfaceView?, isLocal: Boolean) {
        if (surface == null) {
            return
        }
        var id = -1
        if (isLocal) {
            if (mUidList.contains(0)) {
                mUidList.remove(0)
                mUserViewList.remove(0)
            }
            if (mUidList.size == MAX_USER) {
                mUidList.removeAt(0)
                mUserViewList.remove(0)
            }
            id = 0
        } else {
            if (mUidList.contains(uid)) {
                mUidList.remove(uid)
                mUserViewList.remove(uid)
            }
            if (mUidList.size < MAX_USER) {
                id = uid
            }
        }
        if (id == 0) mUidList.add(0, uid) else mUidList.add(uid)
        if (id != -1) {
            mUserViewList.append(uid, createVideoView(surface))
            if (mStatsManager != null) {
                mStatsManager!!.addUserStats(uid, isLocal)
                if (mStatsManager!!.isEnabled) {
                    mHandler!!.removeCallbacks(this)
                    mHandler!!.postDelayed(this, STATS_REFRESH_INTERVAL.toLong())
                }
            }
            requestGridLayout()
        }
    }

    private fun createVideoView(surface: SurfaceView): ViewGroup {
        val layout = RelativeLayout(context)
        layout.id = surface.hashCode()
        val videoLayoutParams = LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT)
        layout.addView(surface, videoLayoutParams)
        val text = TextView(context)
        text.id = layout.hashCode()
        val textParams = LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT)
        textParams.addRule(ALIGN_PARENT_BOTTOM, TRUE)
        textParams.bottomMargin = mStatMarginBottom
        textParams.leftMargin = STAT_LEFT_MARGIN
        text.setTextColor(Color.WHITE)
        text.textSize = STAT_TEXT_SIZE.toFloat()
        layout.addView(text, textParams)
        return layout
    }

    fun removeUserVideo(uid: Int, isLocal: Boolean) {
        if (isLocal && mUidList.contains(0)) {
            mUidList.remove(0)
            mUserViewList.remove(0)
        } else if (mUidList.contains(uid)) {
            mUidList.remove(uid)
            mUserViewList.remove(uid)
        }
        mStatsManager!!.removeUserStats(uid)
        requestGridLayout()
        if (childCount == 0) {
            mHandler!!.removeCallbacks(this)
        }
    }

    private fun requestGridLayout() {
        removeAllViews()
        layout(mUidList.size)
    }

    private fun layout(size: Int) {
        val params = getParams(size)
        for (i in 0 until size) {
            addView(mUserViewList[mUidList[i]], params[i])
        }
    }

    private fun getParams(size: Int): Array<LayoutParams?> {
        val width = measuredWidth
        val height = measuredHeight
        val array = arrayOfNulls<LayoutParams>(size)
        for (i in 0 until size) {
            if (i == 0) {
                array[0] = LayoutParams(
                        LayoutParams.MATCH_PARENT,
                        LayoutParams.MATCH_PARENT)
                array[0]!!.addRule(ALIGN_PARENT_TOP, TRUE)
                array[0]!!.addRule(ALIGN_PARENT_LEFT, TRUE)
            } else if (i == 1) {
                array[1] = LayoutParams(width, height / 2)
                array[0]!!.height = array[1]!!.height
                array[1]!!.addRule(BELOW, mUserViewList[mUidList[0]].id)
                array[1]!!.addRule(ALIGN_PARENT_LEFT, TRUE)
            } else if (i == 2) {
                array[i] = LayoutParams(width / 2, height / 2)
                array[i - 1]!!.width = array[i]!!.width
                array[i]!!.addRule(RIGHT_OF, mUserViewList[mUidList[i - 1]].id)
                array[i]!!.addRule(ALIGN_TOP, mUserViewList[mUidList[i - 1]].id)
            } else if (i == 3) {
                array[i] = LayoutParams(width / 2, height / 2)
                array[0]!!.width = width / 2
                array[1]!!.addRule(BELOW, 0)
                array[1]!!.addRule(ALIGN_PARENT_LEFT, 0)
                array[1]!!.addRule(RIGHT_OF, mUserViewList[mUidList[0]].id)
                array[1]!!.addRule(ALIGN_PARENT_TOP, TRUE)
                array[2]!!.addRule(ALIGN_PARENT_LEFT, TRUE)
                array[2]!!.addRule(RIGHT_OF, 0)
                array[2]!!.addRule(ALIGN_TOP, 0)
                array[2]!!.addRule(BELOW, mUserViewList[mUidList[0]].id)
                array[3]!!.addRule(BELOW, mUserViewList[mUidList[1]].id)
                array[3]!!.addRule(RIGHT_OF, mUserViewList[mUidList[2]].id)
            }
        }
        return array
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        clearAllVideo()
    }

    private fun clearAllVideo() {
        removeAllViews()
        mUserViewList.clear()
        mUidList.clear()
        mHandler!!.removeCallbacks(this)
    }

    override fun run() {
        if (mStatsManager != null && mStatsManager!!.isEnabled) {
            val count = childCount
            for (i in 0 until count) {
                val layout = getChildAt(i) as RelativeLayout
                val text = layout.findViewById<TextView>(layout.hashCode())
                if (text != null) {
                    val data = mStatsManager!!.getStatsData(mUidList[i])
                    val info = data?.toString()
                    if (info != null) text.text = info
                }
            }
            mHandler!!.postDelayed(this, STATS_REFRESH_INTERVAL.toLong())
        }
    }

    companion object {
        private const val MAX_USER = 4
        private const val STATS_REFRESH_INTERVAL = 2000
        private const val STAT_LEFT_MARGIN = 34
        private const val STAT_TEXT_SIZE = 10
    }
}