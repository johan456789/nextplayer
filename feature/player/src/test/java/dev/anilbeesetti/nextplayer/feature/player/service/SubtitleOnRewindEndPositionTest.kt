package dev.anilbeesetti.nextplayer.feature.player.service

import org.junit.Assert.assertEquals
import org.junit.Test

class SubtitleOnRewindEndPositionTest {

    @Test
    fun shortSkipBackEndsWherePlaybackWasBefore() {
        assertEquals(
            100_000L,
            subtitleOnRewindEndPositionMs(
                fromPositionMs = 100_000L,
                toPositionMs = 90_000L,
            ),
        )
    }

    @Test
    fun longSkipBackIsCappedPastLandingSpot() {
        assertEquals(
            40_000L,
            subtitleOnRewindEndPositionMs(
                fromPositionMs = 100_000L,
                toPositionMs = 10_000L,
            ),
        )
    }

    @Test
    fun secondSkipBackKeepsLaterOriginalEnd() {
        assertEquals(
            100_000L,
            subtitleOnRewindEndPositionMs(
                fromPositionMs = 95_000L,
                toPositionMs = 85_000L,
                currentEndPositionMs = 100_000L,
            ),
        )
    }

    @Test
    fun secondSkipBackFromBeyondEndExtendsToNewStart() {
        assertEquals(
            105_000L,
            subtitleOnRewindEndPositionMs(
                fromPositionMs = 105_000L,
                toPositionMs = 95_000L,
                currentEndPositionMs = 100_000L,
            ),
        )
    }

    @Test
    fun secondSkipBackIsCappedPastNewLandingSpot() {
        assertEquals(
            50_000L,
            subtitleOnRewindEndPositionMs(
                fromPositionMs = 95_000L,
                toPositionMs = 20_000L,
                currentEndPositionMs = 100_000L,
            ),
        )
    }
}
