package dev.anilbeesetti.nextplayer.feature.player.ui.controls

import androidx.annotation.OptIn
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.union
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.compose.state.ProgressStateWithTickInterval
import androidx.media3.ui.compose.state.rememberProgressStateWithTickInterval
import dev.anilbeesetti.nextplayer.core.ui.extensions.copy
import dev.anilbeesetti.nextplayer.core.ui.theme.NextPlayerTheme
import dev.anilbeesetti.nextplayer.feature.player.state.ChaptersState
import dev.anilbeesetti.nextplayer.feature.player.state.rememberChaptersState
import dev.anilbeesetti.nextplayer.feature.player.ui.preview.rememberPreviewPlayer

/** Single-line title shown instead of [ControlsTopView] when the player window is small. */
@Composable
fun CompactControlsTopView(title: String, modifier: Modifier = Modifier) {
    val systemBarsPadding = WindowInsets.systemBars.union(
        WindowInsets.displayCutout,
    ).asPaddingValues()
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        color = Color.White,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier
            .fillMaxWidth()
            .padding(systemBarsPadding.copy(bottom = 0.dp))
            .padding(
                horizontal = 16.dp,
                vertical = 12.dp,
            ),
    )
}

/** Time and seekbar shown instead of [ControlsBottomView] when the player window is small. */
@OptIn(UnstableApi::class)
@Composable
fun CompactControlsBottomView(
    progressState: ProgressStateWithTickInterval,
    chaptersState: ChaptersState,
    showRemainingTime: Boolean,
    onToggleTimeDisplay: () -> Unit,
    onSeek: (Long) -> Unit,
    onSeekEnd: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val systemBarsPadding = WindowInsets.systemBars.union(
        WindowInsets.displayCutout,
    ).asPaddingValues()
    Column(
        modifier = modifier
            .padding(systemBarsPadding.copy(top = 0.dp))
            .padding(horizontal = 16.dp)
            .padding(bottom = 8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        PlayerTimeButton(
            progressState = progressState,
            showRemainingTime = showRemainingTime,
            onClick = onToggleTimeDisplay,
        )
        PlayerSeekbar(
            position = progressState.currentPositionMs.toFloat(),
            duration = progressState.durationMs.toFloat(),
            chapters = chaptersState.chapters,
            onSeek = { onSeek(it.toLong()) },
            onSeekFinished = { onSeekEnd() },
        )
    }
}

@OptIn(UnstableApi::class)
@Preview
@Composable
private fun CompactControlsPreview() {
    val player = rememberPreviewPlayer()
    val progressState = rememberProgressStateWithTickInterval(player = player)
    NextPlayerTheme(darkTheme = true) {
        Surface {
            Column {
                CompactControlsTopView(title = "A long video title that does not fit on one line")
                CompactControlsBottomView(
                    progressState = progressState,
                    chaptersState = rememberChaptersState(
                        player = player,
                        progressState = progressState,
                    ),
                    showRemainingTime = false,
                    onToggleTimeDisplay = {},
                    onSeek = {},
                    onSeekEnd = {},
                )
            }
        }
    }
}
