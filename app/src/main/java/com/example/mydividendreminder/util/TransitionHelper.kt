package com.example.mydividendreminder.util

import android.app.Activity
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi

/**
 * Utility class to manage custom activity transitions that exclude the AppBar
 * from being included in the transition animation.
 */
object TransitionHelper {
    
    /**
     * Sets up custom transitions for the current activity.
     * Call this method in onCreate() BEFORE setContent() to properly configure transitions.
     */
    fun setupCustomTransitions(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            try {
                // Request feature before adding content
                activity.window.requestFeature(android.view.Window.FEATURE_ACTIVITY_TRANSITIONS)
                
                // Set fade transitions
                activity.window.enterTransition = android.transition.TransitionInflater.from(activity)
                    .inflateTransition(android.R.transition.fade)
                activity.window.exitTransition = android.transition.TransitionInflater.from(activity)
                    .inflateTransition(android.R.transition.fade)
                
                // Allow overlap to prevent AppBar from being included
                activity.window.allowEnterTransitionOverlap = true
                activity.window.allowReturnTransitionOverlap = true
            } catch (e: Exception) {
                // Log error but don't crash
                android.util.Log.w("TransitionHelper", "Failed to setup custom transitions: ${e.message}")
            }
        }
    }
    
    /**
     * Sets up slide transitions for the current activity.
     * Call this method in onCreate() BEFORE setContent() to properly configure transitions.
     */
    fun setupSlideTransitions(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            try {
                // Request feature before adding content
                activity.window.requestFeature(android.view.Window.FEATURE_ACTIVITY_TRANSITIONS)
                
                // Set slide transitions
                activity.window.enterTransition = android.transition.TransitionInflater.from(activity)
                    .inflateTransition(android.R.transition.slide_top)
                activity.window.exitTransition = android.transition.TransitionInflater.from(activity)
                    .inflateTransition(android.R.transition.slide_bottom)
                
                // Allow overlap to prevent AppBar from being included
                activity.window.allowEnterTransitionOverlap = true
                activity.window.allowReturnTransitionOverlap = true
            } catch (e: Exception) {
                // Log error but don't crash
                android.util.Log.w("TransitionHelper", "Failed to setup slide transitions: ${e.message}")
            }
        }
    }
    
    /**
     * Starts an activity with custom transitions that exclude the AppBar
     * Note: This method should be called BEFORE setContent() in the target activity
     */
    fun startActivityWithCustomTransition(
        activity: Activity,
        intent: Intent,
        enterTransition: Int = android.R.transition.fade,
        exitTransition: Int = android.R.transition.fade
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // Set custom transitions without requesting features
            try {
                activity.window.enterTransition = android.transition.TransitionInflater.from(activity)
                    .inflateTransition(enterTransition)
                activity.window.exitTransition = android.transition.TransitionInflater.from(activity)
                    .inflateTransition(exitTransition)
                
                // Allow overlap to prevent AppBar from being included
                activity.window.allowEnterTransitionOverlap = true
                activity.window.allowReturnTransitionOverlap = true
            } catch (e: Exception) {
                // Fallback to standard navigation if transitions fail
                activity.startActivity(intent)
                return
            }
        }
        
        activity.startActivity(intent)
    }
    
    /**
     * Starts an activity with fade transition (recommended for excluding AppBar)
     */
    fun startActivityWithFadeTransition(activity: Activity, intent: Intent) {
        startActivityWithCustomTransition(
            activity = activity,
            intent = intent,
            enterTransition = android.R.transition.fade,
            exitTransition = android.R.transition.fade
        )
    }
    
    /**
     * Starts an activity with slide transition (alternative option)
     */
    fun startActivityWithSlideTransition(activity: Activity, intent: Intent) {
        startActivityWithCustomTransition(
            activity = activity,
            intent = intent,
            enterTransition = android.R.transition.slide_top,
            exitTransition = android.R.transition.slide_bottom
        )
    }
    
    /**
     * Starts an activity with no transitions (instant switch)
     */
    fun startActivityWithNoTransition(activity: Activity, intent: Intent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            try {
                // Disable all transitions
                activity.window.enterTransition = null
                activity.window.exitTransition = null
                activity.window.allowEnterTransitionOverlap = false
                activity.window.allowReturnTransitionOverlap = false
            } catch (e: Exception) {
                // Fallback to standard navigation if transitions fail
                activity.startActivity(intent)
                return
            }
        }
        
        activity.startActivity(intent)
    }
    
    /**
     * Finishes an activity with custom exit transition
     */
    fun finishWithCustomTransition(
        activity: Activity,
        exitTransition: Int = android.R.transition.fade
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            try {
                activity.window.exitTransition = android.transition.TransitionInflater.from(activity)
                    .inflateTransition(exitTransition)
            } catch (e: Exception) {
                // Fallback to standard finish if transition fails
                activity.finish()
                return
            }
        }
        activity.finish()
    }
    
    /**
     * Finishes an activity with no transition
     */
    fun finishWithNoTransition(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            try {
                activity.window.exitTransition = null
            } catch (e: Exception) {
                // Fallback to standard finish if transition fails
                activity.finish()
                return
            }
        }
        activity.finish()
    }
    
    /**
     * Alternative method: Use overridePendingTransition for better compatibility
     */
    fun startActivityWithOverrideTransition(activity: Activity, intent: Intent) {
        activity.startActivity(intent)
        
        // Use overridePendingTransition for better compatibility
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ECLAIR) {
            activity.overridePendingTransition(
                android.R.anim.fade_in,
                android.R.anim.fade_out
            )
        }
    }
}
