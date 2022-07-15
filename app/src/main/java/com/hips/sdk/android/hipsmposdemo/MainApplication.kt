package com.hips.sdk.android.hipsmposdemo

import android.app.Application
import androidx.core.content.ContextCompat
import com.hips.sdk.hips.ui.HipsUi
import com.hips.sdk.hips.ui.HipsUiBuilder

lateinit var hipsUi: HipsUi

class MainApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        hipsUi = HipsUiBuilder()
            .appContext(this)
            .build()

        /**
         * apply custom UI elements to Hips SDK such as colors and icons
         * uncomment the method call to test
         */
        //customizeSDK()
    }


    /**
     * As for colors, pass only resolved colors (ColorInt): ContextCompat.getColor() or Color objects
     */
    private fun customizeSDK() {
        val customTheme = hipsUi.themeOptions
        customTheme.toolbarColor = ContextCompat.getColor(this, R.color.custom_toolbar_bg)
        customTheme.toolbarTextColor = ContextCompat.getColor(this, R.color.custom_accent)
        customTheme.toolbarLogoDrawable = ContextCompat.getDrawable(this, R.drawable.logo_hips_white)
        customTheme.backgroundColor = ContextCompat.getColor(this, R.color.custom_main_bg)
        customTheme.paymentStatusTextColor = ContextCompat.getColor(this, R.color.custom_accent)
        customTheme.paymentSummaryTextColor = ContextCompat.getColor(this, R.color.custom_accent)
        customTheme.paymentStatusProgressColor = ContextCompat.getColor(this, R.color.custom_accent)
        customTheme.paymentStatusDebugLogTextColor = ContextCompat.getColor(this, R.color.custom_accent)
    }
}