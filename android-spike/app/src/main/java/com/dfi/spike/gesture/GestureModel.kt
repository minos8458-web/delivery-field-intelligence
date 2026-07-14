package com.dfi.spike.gesture

/**
 * Explicit gesture state model for the DFI floating control.
 * Pure (no Android imports): all coordinates are raw screen px supplied by
 * the host; time is epoch/uptime ms supplied by the host.
 *
 * State diagram:
 *
 *   IDLE --Down--> PRESSED
 *   PRESSED --Move beyond slop--> MOVING_BUTTON        (button move mode)
 *   PRESSED --Up before long-press--> VOICE_CAPTURE    (short tap -> voice)
 *   PRESSED --LongPressElapsed--> RADIAL_ACTIVE        (finger stays down)
 *   MOVING_BUTTON --Move into close hit--> MOVING_BUTTON(closeArmed=true)
 *   MOVING_BUTTON(closeArmed) --Up--> STOPPED          (drag-to-close)
 *   MOVING_BUTTON --Up--> IDLE                          (settle + clamp)
 *   RADIAL_ACTIVE --Move--> RADIAL_ACTIVE(highlighted)  (nearest target)
 *   RADIAL_ACTIVE --Up over target--> IDLE              (select)
 *   RADIAL_ACTIVE --Up neutral/outside--> IDLE          (cancel)
 *   VOICE_CAPTURE --VoiceEnded--> IDLE
 *   any --StopRequested--> STOPPED
 *
 * RADIAL_TARGET_HIGHLIGHTED and CLOSE_TARGET_ACTIVE from the task spec are
 * modeled as data on RADIAL_ACTIVE / MOVING_BUTTON rather than separate
 * states, which keeps transitions total and testable.
 */

sealed interface GestureState {
    data object Idle : GestureState
    data class Pressed(val downX: Float, val downY: Float, val downAt: Long) : GestureState
    data class MovingButton(val closeArmed: Boolean, val startedAt: Long) : GestureState
    data class RadialActive(
        val highlighted: Int?,
        val openedAt: Long,
        val targetChanges: Int,
    ) : GestureState
    data object VoiceCapture : GestureState
    data object Stopped : GestureState
}

sealed interface GestureEvent {
    data class Down(val x: Float, val y: Float, val t: Long) : GestureEvent
    data class Move(val x: Float, val y: Float, val t: Long) : GestureEvent
    data class Up(val x: Float, val y: Float, val t: Long) : GestureEvent
    /** Fired by the host's long-press timer. Ignored unless still PRESSED. */
    data class LongPressElapsed(val t: Long) : GestureEvent
    data class VoiceEnded(val t: Long) : GestureEvent
    data class StopRequested(val t: Long) : GestureEvent
}

enum class HapticKind { RADIAL_OPEN, TARGET_TICK, CONFIRM, CLOSE_ARMED }

sealed interface Effect {
    data object StartLongPressTimer : Effect
    data object CancelLongPressTimer : Effect
    data class MoveButtonTo(val x: Float, val y: Float) : Effect
    data class SettleButton(val x: Float, val y: Float) : Effect
    data object ShowCloseTarget : Effect
    data object HideCloseTarget : Effect
    data class OpenRadial(val anchorX: Float, val anchorY: Float) : Effect
    data object CloseRadial : Effect
    data class HighlightTarget(val index: Int?) : Effect
    data class SelectAction(val index: Int) : Effect
    data object CloseOverlay : Effect
    data object StartVoice : Effect
    data class Haptic(val kind: HapticKind) : Effect
    data class LogEvent(val name: String, val fields: Map<String, String> = emptyMap()) : Effect
}

/** Canonical spike event names (accidental-activation measurement). */
object SpikeEvents {
    const val SHORT_TAP = "SHORT_TAP"
    const val LONG_PRESS_STARTED = "LONG_PRESS_STARTED"
    const val RADIAL_OPENED = "RADIAL_OPENED"
    const val RADIAL_TARGET_ENTERED = "RADIAL_TARGET_ENTERED"
    const val RADIAL_SELECTION_CONFIRMED = "RADIAL_SELECTION_CONFIRMED"
    const val RADIAL_SELECTION_CANCELLED = "RADIAL_SELECTION_CANCELLED"
    const val BUTTON_MOVE_STARTED = "BUTTON_MOVE_STARTED"
    const val BUTTON_MOVE_ENDED = "BUTTON_MOVE_ENDED"
    const val CLOSE_TARGET_ENTERED = "CLOSE_TARGET_ENTERED"
    const val CLOSE_CANCELLED = "CLOSE_CANCELLED"
    const val OVERLAY_CLOSED_BY_DRAG = "OVERLAY_CLOSED_BY_DRAG"
    const val VOICE_CAPTURE_STARTED = "VOICE_CAPTURE_STARTED"
    const val VOICE_RESULT = "VOICE_RESULT"
    const val VOICE_ERROR = "VOICE_ERROR"
    const val SESSION_STARTED = "SESSION_STARTED"
    const val SESSION_STOPPED = "SESSION_STOPPED"
    const val SERVICE_DESTROYED = "SERVICE_DESTROYED"
}

/** Host-supplied hit testing. The state machine never touches geometry
 *  directly; the host wires this to RadialGeometry with real safe bounds. */
interface HitTesters {
    /** Index of the highlighted radial slot for a raw screen point, or null. */
    fun radialTarget(x: Float, y: Float): Int?
    /** True when the raw screen point is inside the close target hit area. */
    fun closeTarget(x: Float, y: Float): Boolean
}
