package com.example.ui.components

import android.os.Build
import android.view.HapticFeedbackConstants
import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView

class TrailHaptics(
    private val view: View,
    private val composeHaptics: HapticFeedback
) {
    /** Crisp tactile tap for navigation tabs, chips, map buttons, and list items. */
    fun tap() {
        val handled = view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
        if (!handled) {
            composeHaptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        }
    }

    /** Firm tactile confirmation for starting GPS navigation/recording, bookmarking, or offline downloads. */
    fun confirm() {
        val code = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            HapticFeedbackConstants.CONFIRM
        } else {
            HapticFeedbackConstants.CONTEXT_CLICK
        }
        val handled = view.performHapticFeedback(code)
        if (!handled) {
            composeHaptics.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }

    /** Distinct pulse for stopping recording, discarding, or clearing cached data. */
    fun heavyAction() {
        val code = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            HapticFeedbackConstants.REJECT
        } else {
            HapticFeedbackConstants.LONG_PRESS
        }
        val handled = view.performHapticFeedback(code)
        if (!handled) {
            composeHaptics.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }
}

@Composable
fun rememberTrailHaptics(): TrailHaptics {
    val view = LocalView.current
    val composeHaptics = LocalHapticFeedback.current
    return remember(view, composeHaptics) {
        TrailHaptics(view = view, composeHaptics = composeHaptics)
    }
}
