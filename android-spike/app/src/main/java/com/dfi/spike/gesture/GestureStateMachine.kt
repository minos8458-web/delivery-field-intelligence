package com.dfi.spike.gesture

import com.dfi.spike.gesture.Effect as E
import com.dfi.spike.gesture.GestureEvent as Ev
import com.dfi.spike.gesture.GestureState as S
import kotlin.math.hypot

/**
 * Deterministic gesture state machine for the floating control.
 * Pure Kotlin: the host feeds raw touch events and timer events, and
 * executes the returned effects (WindowManager moves, haptics, logging,
 * SpeechRecognizer start, overlay teardown).
 */
class GestureStateMachine(
    private val touchSlopPx: Float,
    private val longPressMs: Long,
    private val hits: HitTesters,
) {
    var state: S = S.Idle
        private set

    fun on(event: Ev): List<E> {
        // Global stop wins from every state.
        if (event is Ev.StopRequested) {
            val cleanup = listOf(
                E.CancelLongPressTimer, E.HideCloseTarget, E.CloseRadial,
            )
            state = S.Stopped
            return cleanup
        }
        return when (val s = state) {
            is S.Idle -> onIdle(event)
            is S.Pressed -> onPressed(s, event)
            is S.MovingButton -> onMoving(s, event)
            is S.RadialActive -> onRadial(s, event)
            is S.VoiceCapture -> onVoice(event)
            is S.Stopped -> emptyList()
        }
    }

    private fun onIdle(e: Ev): List<E> = when (e) {
        is Ev.Down -> {
            state = S.Pressed(e.x, e.y, e.t)
            listOf(E.StartLongPressTimer)
        }
        else -> emptyList()
    }

    private fun onPressed(s: S.Pressed, e: Ev): List<E> = when (e) {
        is Ev.Move -> {
            if (hypot(e.x - s.downX, e.y - s.downY) > touchSlopPx) {
                state = S.MovingButton(closeArmed = false, startedAt = e.t)
                listOf(
                    E.CancelLongPressTimer,
                    E.LogEvent(SpikeEvents.BUTTON_MOVE_STARTED),
                    E.ShowCloseTarget,
                    E.MoveButtonTo(e.x, e.y),
                )
            } else emptyList()
        }
        is Ev.Up -> {
            // Short tap: released within slop before the long-press timer.
            state = S.VoiceCapture
            listOf(
                E.CancelLongPressTimer,
                E.LogEvent(
                    SpikeEvents.SHORT_TAP,
                    mapOf("pressMs" to (e.t - s.downAt).toString()),
                ),
                E.StartVoice,
            )
        }
        is Ev.LongPressElapsed -> {
            state = S.RadialActive(highlighted = null, openedAt = e.t, targetChanges = 0)
            listOf(
                E.LogEvent(
                    SpikeEvents.LONG_PRESS_STARTED,
                    mapOf("pressMs" to (e.t - s.downAt).toString()),
                ),
                E.OpenRadial(s.downX, s.downY),
                E.LogEvent(SpikeEvents.RADIAL_OPENED),
                E.Haptic(HapticKind.RADIAL_OPEN),
            )
        }
        else -> emptyList()
    }

    private fun onMoving(s: S.MovingButton, e: Ev): List<E> = when (e) {
        is Ev.Move -> {
            val armed = hits.closeTarget(e.x, e.y)
            val effects = mutableListOf<E>(E.MoveButtonTo(e.x, e.y))
            if (armed && !s.closeArmed) {
                effects += E.LogEvent(SpikeEvents.CLOSE_TARGET_ENTERED)
                effects += E.Haptic(HapticKind.CLOSE_ARMED)
            } else if (!armed && s.closeArmed) {
                effects += E.LogEvent(SpikeEvents.CLOSE_CANCELLED)
            }
            state = s.copy(closeArmed = armed)
            effects
        }
        is Ev.Up -> {
            if (s.closeArmed) {
                state = S.Stopped
                listOf(
                    E.LogEvent(SpikeEvents.OVERLAY_CLOSED_BY_DRAG),
                    E.HideCloseTarget,
                    E.CloseOverlay,
                )
            } else {
                state = S.Idle
                listOf(
                    E.HideCloseTarget,
                    E.SettleButton(e.x, e.y),
                    E.LogEvent(
                        SpikeEvents.BUTTON_MOVE_ENDED,
                        mapOf("moveMs" to (e.t - s.startedAt).toString()),
                    ),
                )
            }
        }
        // Stale long-press timer after the finger already moved: ignore.
        is Ev.LongPressElapsed -> emptyList()
        else -> emptyList()
    }

    private fun onRadial(s: S.RadialActive, e: Ev): List<E> = when (e) {
        is Ev.Move -> {
            val idx = hits.radialTarget(e.x, e.y)
            if (idx == s.highlighted) {
                emptyList()
            } else {
                val effects = mutableListOf<E>(E.HighlightTarget(idx))
                var changes = s.targetChanges
                if (idx != null) {
                    changes += 1
                    effects += E.LogEvent(
                        SpikeEvents.RADIAL_TARGET_ENTERED,
                        mapOf("index" to idx.toString()),
                    )
                    effects += E.Haptic(HapticKind.TARGET_TICK)
                }
                state = s.copy(highlighted = idx, targetChanges = changes)
                effects
            }
        }
        is Ev.Up -> {
            state = S.Idle
            val idx = s.highlighted
            if (idx != null) {
                listOf(
                    E.LogEvent(
                        SpikeEvents.RADIAL_SELECTION_CONFIRMED,
                        mapOf(
                            "index" to idx.toString(),
                            "openToSelectMs" to (e.t - s.openedAt).toString(),
                            "targetChanges" to s.targetChanges.toString(),
                        ),
                    ),
                    E.Haptic(HapticKind.CONFIRM),
                    E.CloseRadial,
                    E.SelectAction(idx),
                )
            } else {
                listOf(
                    E.LogEvent(
                        SpikeEvents.RADIAL_SELECTION_CANCELLED,
                        mapOf(
                            "openToCancelMs" to (e.t - s.openedAt).toString(),
                            "targetChanges" to s.targetChanges.toString(),
                        ),
                    ),
                    E.CloseRadial,
                )
            }
        }
        is Ev.LongPressElapsed -> emptyList()
        else -> emptyList()
    }

    private fun onVoice(e: Ev): List<E> = when (e) {
        is Ev.VoiceEnded -> {
            state = S.Idle
            emptyList()
        }
        // Touches during voice capture are intentionally ignored in the spike.
        else -> emptyList()
    }
}
