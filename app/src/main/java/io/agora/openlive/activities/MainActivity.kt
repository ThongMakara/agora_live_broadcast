package io.agora.openlive.activities

import android.Manifest
import android.animation.Animator
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Rect
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.View
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import io.agora.openlive.R
import io.agora.openlive.activities.MainActivity

class MainActivity : BaseActivity() {
    private val PERMISSIONS = arrayOf(
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    )
    private val mVisibleRect = Rect()
    private var mLastVisibleHeight = 0
    private var mBodyLayout: RelativeLayout? = null
    private var mBodyDefaultMarginTop = 0
    private var mTopicEdit: EditText? = null
    private var mStartBtn: TextView? = null
    private var mLogo: ImageView? = null
    private val mLogoAnimListener: Animator.AnimatorListener = object : Animator.AnimatorListener {
        override fun onAnimationStart(animator: Animator) {
            // Do nothing
        }

        override fun onAnimationEnd(animator: Animator) {
            mLogo!!.visibility = View.VISIBLE
        }

        override fun onAnimationCancel(animator: Animator) {
            mLogo!!.visibility = View.VISIBLE
        }

        override fun onAnimationRepeat(animator: Animator) {
            // Do nothing
        }
    }
    private val mTextWatcher: TextWatcher = object : TextWatcher {
        override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
            // Do nothing
        }

        override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
            // Do nothing
        }

        override fun afterTextChanged(editable: Editable) {
            mStartBtn!!.isEnabled = !TextUtils.isEmpty(editable)
        }
    }
    private val mLayoutObserverListener = OnGlobalLayoutListener { checkInputMethodWindowState() }

    private fun checkInputMethodWindowState() {
        window.decorView.rootView.getWindowVisibleDisplayFrame(mVisibleRect)
        val visibleHeight = mVisibleRect.bottom - mVisibleRect.top
        if (visibleHeight == mLastVisibleHeight) return
        val inputShown = mDisplayMetrics.heightPixels - visibleHeight > MIN_INPUT_METHOD_HEIGHT
        mLastVisibleHeight = visibleHeight

        if (inputShown) {
            if (mLogo!!.visibility == View.VISIBLE) {
                mBodyLayout!!.animate().translationYBy(-mLogo!!.measuredHeight.toFloat())
                        .setDuration(ANIM_DURATION.toLong()).setListener(null).start()
                mLogo!!.visibility = View.INVISIBLE
            }
        } else if (mLogo!!.visibility != View.VISIBLE) {
            mBodyLayout!!.animate().translationYBy(mLogo!!.measuredHeight.toFloat())
                    .setDuration(ANIM_DURATION.toLong()).setListener(mLogoAnimListener).start()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initUI()
    }

    private fun initUI() {
        mBodyLayout = findViewById(R.id.middle_layout)
        mLogo = findViewById(R.id.main_logo)
        mTopicEdit = findViewById(R.id.topic_edit)
        mTopicEdit!!.addTextChangedListener(mTextWatcher)
        mStartBtn = findViewById(R.id.start_broadcast_button)
        if (TextUtils.isEmpty(mTopicEdit!!.getText())) mStartBtn!!.setEnabled(false)
    }

    override fun onGlobalLayoutCompleted() {
        adjustViewPositions()
    }

    private fun adjustViewPositions() {
        // Setting btn move downward away the status bar
        val settingBtn = findViewById<ImageView>(R.id.setting_button)
        var param = settingBtn.layoutParams as RelativeLayout.LayoutParams
        param.topMargin += mStatusBarHeight
        settingBtn.layoutParams = param

        // Logo is 0.48 times the screen width
        // ImageView logo = findViewById(R.id.main_logo);
        param = mLogo!!.layoutParams as RelativeLayout.LayoutParams
        val size = (mDisplayMetrics.widthPixels * 0.48).toInt()
        param.width = size
        param.height = size
        mLogo!!.layoutParams = param

        // Bottom margin of the main body should be two times it's top margin.
        param = mBodyLayout!!.layoutParams as RelativeLayout.LayoutParams
        param.topMargin = (mDisplayMetrics.heightPixels -
                mBodyLayout!!.measuredHeight - mStatusBarHeight) / 3
        mBodyLayout!!.layoutParams = param
        mBodyDefaultMarginTop = param.topMargin

        // The width of the start button is roughly 0.72
        // times the width of the screen
        mStartBtn = findViewById(R.id.start_broadcast_button)
        param = mStartBtn!!.getLayoutParams() as RelativeLayout.LayoutParams
        param.width = (mDisplayMetrics.widthPixels * 0.72).toInt()
        mStartBtn!!.setLayoutParams(param)
    }

    fun onSettingClicked(view: View?) {
        val i = Intent(this, SettingsActivity::class.java)
        startActivity(i)
    }

    fun onStartBroadcastClicked(view: View?) {
        checkPermission()
    }

    private fun checkPermission() {
        var granted = true
        for (per in PERMISSIONS) {
            if (!permissionGranted(per)) {
                granted = false
                break
            }
        }
        if (granted) {
            resetLayoutAndForward()
        } else {
            requestPermissions()
        }
    }

    private fun permissionGranted(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(
                this, permission) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_REQ_CODE)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == PERMISSION_REQ_CODE) {
            var granted = true
            for (result in grantResults) {
                granted = result == PackageManager.PERMISSION_GRANTED
                if (!granted) break
            }
            if (granted) {
                resetLayoutAndForward()
            } else {
                toastNeedPermissions()
            }
        }
    }

    private fun resetLayoutAndForward() {
        closeImeDialogIfNeeded()
        gotoRoleActivity()
    }

    private fun closeImeDialogIfNeeded() {
        val manager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        manager.hideSoftInputFromWindow(mTopicEdit!!.windowToken,
                InputMethodManager.HIDE_NOT_ALWAYS)
    }

    fun gotoRoleActivity() {
        val intent = Intent(this@MainActivity, RoleActivity::class.java)
        val room = mTopicEdit!!.text.toString()
        config().channelName = room
        startActivity(intent)
    }

    private fun toastNeedPermissions() {
        Toast.makeText(this, R.string.need_necessary_permissions, Toast.LENGTH_LONG).show()
    }

    override fun onResume() {
        super.onResume()
        resetUI()
        registerLayoutObserverForSoftKeyboard()
    }

    private fun resetUI() {
        resetLogo()
        closeImeDialogIfNeeded()
    }

    private fun resetLogo() {
        mLogo!!.visibility = View.VISIBLE
        mBodyLayout!!.y = mBodyDefaultMarginTop.toFloat()
    }

    private fun registerLayoutObserverForSoftKeyboard() {
        val view = window.decorView.rootView
        val observer = view.viewTreeObserver
        observer.addOnGlobalLayoutListener(mLayoutObserverListener)
    }

    override fun onPause() {
        super.onPause()
        removeLayoutObserverForSoftKeyboard()
    }

    private fun removeLayoutObserverForSoftKeyboard() {
        val view = window.decorView.rootView
        view.viewTreeObserver.removeOnGlobalLayoutListener(mLayoutObserverListener)
    }

    companion object {
        private val TAG = MainActivity::class.java.simpleName
        private const val MIN_INPUT_METHOD_HEIGHT = 200
        private const val ANIM_DURATION = 200

        // Permission request code of any integer value
        private const val PERMISSION_REQ_CODE = 1 shl 4
    }
}