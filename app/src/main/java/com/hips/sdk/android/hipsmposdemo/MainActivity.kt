package com.hips.sdk.android.hipsmposdemo

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContentView(R.layout.activity_main)

        val controller = WindowCompat.getInsetsController(window, window.decorView)
        controller.isAppearanceLightStatusBars = true
        controller.isAppearanceLightNavigationBars = true

        // Apply safe padding to your root container
        findViewById<View>(R.id.container)?.let { container ->
            ViewCompat.setOnApplyWindowInsetsListener(container) { v, insets ->
                val sb = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.updatePadding(top = sb.top, bottom = sb.bottom)
                insets
            }
            // Request insets so the listener runs immediately
            ViewCompat.requestApplyInsets(container)
        }

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, MainFragment.newInstance())
                .commitNow()
        }
    }
}
