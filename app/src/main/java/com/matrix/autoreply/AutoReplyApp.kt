/*
 * AutoReply - AI-Powered Smart Auto Reply App
 * Copyright (c) 2024 Prasoon Kumar
 * 
 * This file is part of AutoReply, licensed under GPL v3.
 * Commercial distribution on app stores requires explicit permission.
 * 
 * Contact: prasoonkumar008@gmail.com
 * GitHub: https://github.com/it5prasoon/Auto-Reply-Android
 * 
 * WARNING: Unauthorized commercial distribution will result in DMCA takedown requests.
 */

package com.matrix.autoreply

import android.app.Application
import android.content.Context
import android.os.Build
import androidx.appcompat.app.AppCompatDelegate
import com.google.android.material.color.DynamicColors
import com.matrix.autoreply.preferences.PreferencesManager

class AutoReplyApp : Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        // Initialize PreferencesManager early to ensure it's available for all services
        PreferencesManager.initialize(this)
        
        // Apply saved theme preference
        applyTheme()
    }
    
    /**
     * Apply the user's saved theme preference
     */
    private fun applyTheme() {
        val preferencesManager = PreferencesManager.getPreferencesInstance(this)
        val theme = preferencesManager?.appTheme ?: "system"
        
        when (theme) {
            "light" -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
            "dark" -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            }
            "system" -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            }
            "dynamic" -> {
                // Dynamic colors (Material You) for Android 12+
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    if (DynamicColors.isDynamicColorAvailable()) {
                        // Apply dynamic colors
                        DynamicColors.applyToActivitiesIfAvailable(this)
                    }
                }
                // Use system theme as base
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            }
            else -> {
                // Default to system theme if unknown preference
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            }
        }
    }
    
    companion object {
        private lateinit var instance: AutoReplyApp
        
        val appContext: Context
            get() = instance.applicationContext
    }
    
    init {
        instance = this
    }
}
