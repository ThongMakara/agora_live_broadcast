package io.agora.openlive.test

import android.content.Context
import io.agora.rtc.IRtcEngineEventHandler
import io.agora.rtc.RtcEngine
import org.junit.Assert
import org.junit.Test
import org.mockito.Mock

class RtcEngineTest {
    @Mock
    var mockContext: Context? = null

    @Test
    fun RtcEngineCreateTest() {
        // fake app id, not valid for joining a channel
        // but enough to create a rtc engine.
        val appId = "85ace02fa13321f4cb06b61a0e109080"
        var success = true
        try {
            RtcEngine.create(mockContext, appId, object : IRtcEngineEventHandler() {})
        } catch (e: Exception) {
            success = false
            e.printStackTrace()
        }
        Assert.assertTrue(success)
    }
}