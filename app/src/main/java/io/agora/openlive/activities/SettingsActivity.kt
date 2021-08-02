package io.agora.openlive.activities

import android.app.AlertDialog
import android.content.SharedPreferences
import android.graphics.Rect
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration
import io.agora.openlive.Constants
import io.agora.openlive.R
import io.agora.openlive.ui.ResolutionAdapter
import io.agora.openlive.utils.PrefManager
import java.util.*

class SettingsActivity : BaseActivity() {
    private var mVideoStatCheck: TextView? = null
    private var mMirrorLocalText: TextView? = null
    private var mMirrorRemoteText: TextView? = null
    private var mMirrorEncodeText: TextView? = null
    private var mItemPadding = 0
    private var mResolutionAdapter: ResolutionAdapter? = null
    private val mItemDecoration: ItemDecoration = object : ItemDecoration() {
        override fun getItemOffsets(outRect: Rect, view: View,
                                    parent: RecyclerView, state: RecyclerView.State) {
            outRect.top = mItemPadding
            outRect.bottom = mItemPadding
            outRect.left = mItemPadding
            outRect.right = mItemPadding
            val pos = parent.getChildAdapterPosition(view)
            if (pos < DEFAULT_SPAN) {
                outRect.top = 0
            }
            if (pos % DEFAULT_SPAN == 0) outRect.left = 0 else if (pos % DEFAULT_SPAN == DEFAULT_SPAN - 1) outRect.right = 0
        }
    }
    private var mPref: SharedPreferences? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        mPref = PrefManager.getPreferences(applicationContext)
        initUI()
    }

    private fun initUI() {
        val resolutionList = findViewById<RecyclerView>(R.id.resolution_list)
        resolutionList.setHasFixedSize(true)
        val layoutManager: RecyclerView.LayoutManager = GridLayoutManager(this, DEFAULT_SPAN)
        resolutionList.layoutManager = layoutManager
        mResolutionAdapter = ResolutionAdapter(this, config().videoDimenIndex)
        resolutionList.adapter = mResolutionAdapter
        resolutionList.addItemDecoration(mItemDecoration)
        mItemPadding = resources.getDimensionPixelSize(R.dimen.setting_resolution_item_padding)
        mVideoStatCheck = findViewById(R.id.setting_stats_checkbox)
        mVideoStatCheck!!.setActivated(config().ifShowVideoStats())
        mMirrorLocalText = findViewById(R.id.setting_mirror_local_value)
        resetText(mMirrorLocalText, config().mirrorLocalIndex)
        mMirrorRemoteText = findViewById(R.id.setting_mirror_remote_value)
        resetText(mMirrorRemoteText, config().mirrorRemoteIndex)
        mMirrorEncodeText = findViewById(R.id.setting_mirror_encode_value)
        resetText(mMirrorEncodeText, config().mirrorEncodeIndex)
    }

    private fun resetText(view: TextView?, index: Int) {
        if (view == null) {
            return
        }
        val strings = resources.getStringArray(R.array.mirror_modes)
        view.text = strings[index]
    }

    override fun onGlobalLayoutCompleted() {
        // Adjust for status bar height
        val titleLayout = findViewById<RelativeLayout>(R.id.role_title_layout)
        val params = titleLayout.layoutParams as LinearLayout.LayoutParams
        params.height += mStatusBarHeight
        titleLayout.layoutParams = params
    }

    override fun onBackPressed() {
        onBackArrowPressed(null)
    }

    fun onBackArrowPressed(view: View?) {
        saveResolution()
        saveShowStats()
        finish()
    }

    private fun saveResolution() {
        val profileIndex = mResolutionAdapter!!.selected
        config().videoDimenIndex = profileIndex
        mPref!!.edit().putInt(Constants.PREF_RESOLUTION_IDX, profileIndex).apply()
    }

    private fun saveShowStats() {
        config().setIfShowVideoStats(mVideoStatCheck!!.isActivated)
        mPref!!.edit().putBoolean(Constants.PREF_ENABLE_STATS,
                mVideoStatCheck!!.isActivated).apply()
    }

    private fun saveVideoMirrorMode(key: String?, value: Int) {
        if (TextUtils.isEmpty(key)) return
        when (key) {
            Constants.PREF_MIRROR_LOCAL -> config().mirrorLocalIndex = value
            Constants.PREF_MIRROR_REMOTE -> config().mirrorRemoteIndex = value
            Constants.PREF_MIRROR_ENCODE -> config().mirrorEncodeIndex = value
        }
        mPref!!.edit().putInt(key, value).apply()
    }

    fun onStatsChecked(view: View) {
        view.isActivated = !view.isActivated
        statsManager().enableStats(view.isActivated)
    }

    fun onClick(view: View) {
        var key: String? = null
        var textView: TextView? = null
        when (view.id) {
            R.id.setting_mirror_local_view -> {
                key = Constants.PREF_MIRROR_LOCAL
                textView = mMirrorLocalText
            }
            R.id.setting_mirror_remote_view -> {
                key = Constants.PREF_MIRROR_REMOTE
                textView = mMirrorRemoteText
            }
            R.id.setting_mirror_encode_view -> {
                key = Constants.PREF_MIRROR_ENCODE
                textView = mMirrorEncodeText
            }
        }
        textView?.let { showDialog(key, it) }
    }

    private fun showDialog(key: String?, view: TextView) {
        val builder = AlertDialog.Builder(this)
        val strings = resources.getStringArray(R.array.mirror_modes)
        val checkedItem = Arrays.asList(*strings).indexOf(view.text.toString())
        builder.setSingleChoiceItems(strings, checkedItem) { dialog, which ->
            saveVideoMirrorMode(key, which)
            resetText(view, which)
            dialog.dismiss()
        }
        builder.create().show()
    }

    companion object {
        private const val DEFAULT_SPAN = 3
    }
}