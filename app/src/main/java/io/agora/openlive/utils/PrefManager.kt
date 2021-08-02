package io.agora.openlive.utils

import android.content.Context
import android.content.SharedPreferences
import io.agora.openlive.Constants

object PrefManager {
    @JvmStatic
    fun getPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE)
    }
}