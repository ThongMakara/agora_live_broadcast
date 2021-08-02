package io.agora.openlive.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.RelativeLayout
import io.agora.openlive.R
import io.agora.openlive.activities.LiveActivity
import io.agora.rtc.Constants

class RoleActivity : BaseActivity() {
    companion object {
        var role = ""
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_choose_role)
    }

    override fun onGlobalLayoutCompleted() {
        var layout = findViewById<RelativeLayout>(R.id.role_title_layout)
        var params = layout.layoutParams as RelativeLayout.LayoutParams
        params.height += mStatusBarHeight
        layout.layoutParams = params
        layout = findViewById(R.id.role_content_layout)
        params = layout.layoutParams as RelativeLayout.LayoutParams
        params.topMargin = (mDisplayMetrics.heightPixels -
                layout.measuredHeight) * 3 / 7
        layout.layoutParams = params
    }

    fun onJoinAsBroadcaster(view: View?) {
        gotoLiveActivity(Constants.CLIENT_ROLE_BROADCASTER)
        role = "Broadcaster"
    }

    fun onJoinAsAudience(view: View?) {
        gotoLiveActivity(Constants.CLIENT_ROLE_AUDIENCE)
        role = "Audience"
    }

    private fun gotoLiveActivity(role: Int) {
        val intent = Intent(intent)
        intent.putExtra(io.agora.openlive.Constants.KEY_CLIENT_ROLE, 1)
        intent.setClass(applicationContext, LiveActivity::class.java)
        startActivity(intent)
    }

    fun onBackArrowPressed(view: View?) {
        finish()
    }
}