package dev.anilbeesetti.nextplayer.feature.player.state

import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.app.MultiWindowModeChangedInfo
import androidx.core.util.Consumer

/** Whether the activity is in split-screen or freeform mode. Picture-in-picture is not counted. */
@Composable
fun rememberIsInMultiWindowMode(): Boolean {
    val activity = LocalActivity.current as? ComponentActivity ?: return false
    var isInMultiWindowMode by remember(activity) {
        mutableStateOf(activity.isInMultiWindowMode && !activity.isInPictureInPictureMode)
    }
    DisposableEffect(activity) {
        val listener = Consumer<MultiWindowModeChangedInfo> { info ->
            isInMultiWindowMode = info.isInMultiWindowMode && !activity.isInPictureInPictureMode
        }
        activity.addOnMultiWindowModeChangedListener(listener)
        onDispose { activity.removeOnMultiWindowModeChangedListener(listener) }
    }
    return isInMultiWindowMode
}
