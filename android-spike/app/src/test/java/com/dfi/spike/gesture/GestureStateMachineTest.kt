package com.dfi.spike.gesture

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class GestureStateMachineTest {

    /** Programmable stub hit-tester: radial index by x band, close by flag. */
    private class StubHits : HitTesters {
        var radial: Int? = null
        var close: Boolean = false
        override fun radialTarget(x: Float, y: Float): Int? = radial
        override fun closeTarget(x: Float, y: Float): Boolean = close
    }

    private fun machine(hits: StubHits = StubHits()): Pair<GestureStateMachine, StubHits> =
        GestureStateMachine(touchSlopPx = 16f, longPressMs = 400, hits = hits) to hits

    private fun List<Effect>.logNames(): List<String> =
        filterIsInstance<Effect.LogEvent>().map { it.name }

    private fun List<Effect>.log(name: String): Effect.LogEvent =
        filterIsInstance<Effect.LogEvent>().first { it.name == name }

    @Test fun shortTapStartsVoiceCapture() {
        val (m, _) = machine()
        m.on(GestureEvent.Down(100f, 100f, 1000))
        val fx = m.on(GestureEvent.Up(102f, 101f, 1120))
        assertTrue(fx.contains(Effect.StartVoice))
        assertEquals("120", fx.log(SpikeEvents.SHORT_TAP).fields["pressMs"])
        assertIs<GestureState.VoiceCapture>(m.state)
        // Voice end returns to idle.
        m.on(GestureEvent.VoiceEnded(3000))
        assertIs<GestureState.Idle>(m.state)
    }

    @Test fun touchesDuringVoiceCaptureAreIgnored() {
        val (m, _) = machine()
        m.on(GestureEvent.Down(100f, 100f, 1000))
        m.on(GestureEvent.Up(100f, 100f, 1100))
        val fx = m.on(GestureEvent.Down(300f, 300f, 1500))
        assertTrue(fx.isEmpty())
        assertIs<GestureState.VoiceCapture>(m.state)
    }

    @Test fun longPressOpensRadialWhileFingerDown() {
        val (m, _) = machine()
        m.on(GestureEvent.Down(200f, 900f, 1000))
        val fx = m.on(GestureEvent.LongPressElapsed(1400))
        assertTrue(fx.any { it is Effect.OpenRadial })
        assertEquals(
            listOf(SpikeEvents.LONG_PRESS_STARTED, SpikeEvents.RADIAL_OPENED),
            fx.logNames(),
        )
        assertIs<GestureState.RadialActive>(m.state)
    }

    @Test fun dragToTargetAndReleaseSelectsWithoutSecondTap() {
        val (m, hits) = machine()
        m.on(GestureEvent.Down(200f, 900f, 1000))
        m.on(GestureEvent.LongPressElapsed(1400))
        hits.radial = 2
        val moveFx = m.on(GestureEvent.Move(320f, 800f, 1600))
        assertTrue(moveFx.contains(Effect.HighlightTarget(2)))
        assertTrue(moveFx.any { it is Effect.Haptic && it.kind == HapticKind.TARGET_TICK })
        val upFx = m.on(GestureEvent.Up(320f, 800f, 1800))
        assertTrue(upFx.contains(Effect.SelectAction(2)))
        val confirmed = upFx.log(SpikeEvents.RADIAL_SELECTION_CONFIRMED)
        assertEquals("2", confirmed.fields["index"])
        assertEquals("400", confirmed.fields["openToSelectMs"])
        assertEquals("1", confirmed.fields["targetChanges"])
        assertIs<GestureState.Idle>(m.state)
    }

    @Test fun returningToNeutralCancelsSelection() {
        val (m, hits) = machine()
        m.on(GestureEvent.Down(200f, 900f, 1000))
        m.on(GestureEvent.LongPressElapsed(1400))
        hits.radial = 1
        m.on(GestureEvent.Move(320f, 800f, 1500))
        hits.radial = null // moved back into the neutral zone
        val neutralFx = m.on(GestureEvent.Move(205f, 902f, 1600))
        assertTrue(neutralFx.contains(Effect.HighlightTarget(null)))
        val upFx = m.on(GestureEvent.Up(205f, 902f, 1700))
        assertTrue(upFx.none { it is Effect.SelectAction })
        assertEquals("1", upFx.log(SpikeEvents.RADIAL_SELECTION_CANCELLED).fields["targetChanges"])
        assertIs<GestureState.Idle>(m.state)
    }

    @Test fun releaseOutsideValidTargetsCancelsSafely() {
        val (m, hits) = machine()
        m.on(GestureEvent.Down(200f, 900f, 1000))
        m.on(GestureEvent.LongPressElapsed(1400))
        hits.radial = null
        m.on(GestureEvent.Move(900f, 200f, 1500))
        val upFx = m.on(GestureEvent.Up(900f, 200f, 1600))
        assertTrue(upFx.none { it is Effect.SelectAction })
        assertTrue(upFx.logNames().contains(SpikeEvents.RADIAL_SELECTION_CANCELLED))
        assertIs<GestureState.Idle>(m.state)
    }

    @Test fun targetChangesAreCounted() {
        val (m, hits) = machine()
        m.on(GestureEvent.Down(200f, 900f, 1000))
        m.on(GestureEvent.LongPressElapsed(1400))
        hits.radial = 0; m.on(GestureEvent.Move(1f, 1f, 1500))
        hits.radial = 1; m.on(GestureEvent.Move(2f, 2f, 1550))
        hits.radial = 0; m.on(GestureEvent.Move(3f, 3f, 1600))
        val upFx = m.on(GestureEvent.Up(3f, 3f, 1700))
        assertEquals("3", upFx.log(SpikeEvents.RADIAL_SELECTION_CONFIRMED).fields["targetChanges"])
    }

    @Test fun moveBeyondSlopEntersButtonMoveAndCancelsLongPress() {
        val (m, _) = machine()
        m.on(GestureEvent.Down(200f, 900f, 1000))
        val fx = m.on(GestureEvent.Move(260f, 940f, 1100))
        assertTrue(fx.contains(Effect.CancelLongPressTimer))
        assertTrue(fx.contains(Effect.ShowCloseTarget))
        assertTrue(fx.logNames().contains(SpikeEvents.BUTTON_MOVE_STARTED))
        assertIs<GestureState.MovingButton>(m.state)
        // A stale long-press timer that fires later must be ignored.
        val staleFx = m.on(GestureEvent.LongPressElapsed(1400))
        assertTrue(staleFx.isEmpty())
        assertIs<GestureState.MovingButton>(m.state)
    }

    @Test fun buttonMoveEndsWithSettleAndClampEffect() {
        val (m, _) = machine()
        m.on(GestureEvent.Down(200f, 900f, 1000))
        m.on(GestureEvent.Move(260f, 940f, 1100))
        val upFx = m.on(GestureEvent.Up(400f, 1200f, 1500))
        assertTrue(upFx.contains(Effect.SettleButton(400f, 1200f)))
        assertTrue(upFx.contains(Effect.HideCloseTarget))
        assertEquals("400", upFx.log(SpikeEvents.BUTTON_MOVE_ENDED).fields["moveMs"])
        assertIs<GestureState.Idle>(m.state)
    }

    @Test fun dragToCloseTargetAndReleaseClosesOverlay() {
        val (m, hits) = machine()
        m.on(GestureEvent.Down(200f, 900f, 1000))
        m.on(GestureEvent.Move(260f, 940f, 1100))
        hits.close = true
        val enterFx = m.on(GestureEvent.Move(540f, 2100f, 1300))
        assertTrue(enterFx.logNames().contains(SpikeEvents.CLOSE_TARGET_ENTERED))
        assertTrue(enterFx.any { it is Effect.Haptic && it.kind == HapticKind.CLOSE_ARMED })
        val upFx = m.on(GestureEvent.Up(540f, 2100f, 1400))
        assertTrue(upFx.contains(Effect.CloseOverlay))
        assertTrue(upFx.logNames().contains(SpikeEvents.OVERLAY_CLOSED_BY_DRAG))
        assertIs<GestureState.Stopped>(m.state)
    }

    @Test fun leavingCloseTargetCancelsCloseWithoutClosing() {
        val (m, hits) = machine()
        m.on(GestureEvent.Down(200f, 900f, 1000))
        m.on(GestureEvent.Move(260f, 940f, 1100))
        hits.close = true
        m.on(GestureEvent.Move(540f, 2100f, 1300))
        hits.close = false
        val exitFx = m.on(GestureEvent.Move(540f, 1500f, 1400))
        assertTrue(exitFx.logNames().contains(SpikeEvents.CLOSE_CANCELLED))
        val upFx = m.on(GestureEvent.Up(540f, 1500f, 1500))
        assertTrue(upFx.none { it is Effect.CloseOverlay })
        assertIs<GestureState.Idle>(m.state)
    }

    @Test fun stopRequestedWinsFromAnyStateAndAbsorbsFurtherEvents() {
        val (m, _) = machine()
        m.on(GestureEvent.Down(200f, 900f, 1000))
        m.on(GestureEvent.LongPressElapsed(1400))
        val fx = m.on(GestureEvent.StopRequested(2000))
        assertTrue(fx.contains(Effect.CloseRadial))
        assertTrue(fx.contains(Effect.CancelLongPressTimer))
        assertIs<GestureState.Stopped>(m.state)
        assertTrue(m.on(GestureEvent.Down(1f, 1f, 3000)).isEmpty())
        assertIs<GestureState.Stopped>(m.state)
    }
}
