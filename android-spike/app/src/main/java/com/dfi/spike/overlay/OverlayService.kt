package com.dfi.spike.overlay

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.PixelFormat
import android.os.BatteryManager
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.SystemClock
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.Gravity
import android.view.MotionEvent
import android.view.WindowManager
import android.widget.Toast
import com.dfi.spike.gesture.Effect
import com.dfi.spike.gesture.GestureEvent
import com.dfi.spike.gesture.GestureStateMachine
import com.dfi.spike.gesture.HapticKind
import com.dfi.spike.gesture.HitTesters
import com.dfi.spike.gesture.SpikeEvents
import com.dfi.spike.geometry.Circle
import com.dfi.spike.geometry.Bounds
import com.dfi.spike.geometry.Pt
import com.dfi.spike.geometry.RadialGeometry
import com.dfi.spike.geometry.RadialLayout
import com.dfi.spike.log.SpikeEventLog
import com.dfi.spike.speech.VoiceCaptureController

/**
 * C4 spike foreground service.
 *
 * Owns the floating control window (TYPE_APPLICATION_OVERLAY), the
 * full-screen non-touchable render layer, the gesture state machine host
 * wiring (long-press timer, hit testing, effect execution), the voice
 * capture controller, and battery/session logging.
 *
 * Start: startForegroundService(Intent(ctx, OverlayService::class))
 * Stop:  stopService(...) or the notification "세션 종료" action.
 */
class OverlayService : Service() {

    companion object {
        const val ACTION_STOP = "com.dfi.spike.action.STOP"
        private const val CHANNEL_ID = "dfi_spike"
        private const val NOTIF_ID = 1
        private const val LONG_PRESS_MS = 400L

        /** Prevents duplicate overlay windows across repeated start intents. */
        @Volatile
        var running = false
            private set
    }

    private lateinit var wm: WindowManager
    private lateinit var log: SpikeEventLog
    private lateinit var machine: GestureStateMachine
    private lateinit var voice: VoiceCaptureController
    private lateinit var safeBounds: SafeBoundsProvider
    private val handler = Handler(Looper.getMainLooper())

    private var button: FloatingControlView? = null
    private var buttonLp: WindowManager.LayoutParams? = null
    private var layer: InteractionLayerView? = null
    private var layerAttached = false

    private var radialLayout: RadialLayout? = null
    private var closeCircle: Circle? = null
    private var safe = Bounds(0f, 0f, 1080f, 1920f)

    private var sessionStartMs = 0L

    private val actionLabels = listOf("사진", "필드트레이스", "질문", "위험", "차량문제", "부상")
    private val actionCodes = listOf(
        "PHOTO", "FIELD_TRACE", "QUESTION", "RISK", "VEHICLE_PROBLEM", "PERSON_INJURED",
    )

    // Density-derived dimensions (px), computed in onCreate.
    private var density = 1f
    private var buttonWindowPx = 0
    private var ringRadiusPx = 0f
    private var hitRadiusPx = 0f
    private var closeRadiusPx = 0f
    private var closeBottomMarginPx = 0f

    private val longPressRunnable = Runnable {
        dispatch(GestureEvent.LongPressElapsed(SystemClock.uptimeMillis()))
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        wm = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        log = SpikeEventLog(this)
        safeBounds = SafeBoundsProvider(this)
        voice = VoiceCaptureController(this, log)

        density = resources.displayMetrics.density
        buttonWindowPx = (72f * density).toInt()
        ringRadiusPx = 132f * density
        hitRadiusPx = 44f * density
        closeRadiusPx = 48f * density
        closeBottomMarginPx = 32f * density

        machine = GestureStateMachine(
            touchSlopPx = 12f * density,
            longPressMs = LONG_PRESS_MS,
            hits = object : HitTesters {
                override fun radialTarget(x: Float, y: Float): Int? {
                    val layout = radialLayout ?: return null
                    return RadialGeometry.nearestSlot(
                        touch = Pt(x, y),
                        layout = layout,
                        neutralRadius = buttonWindowPx * 0.55f,
                        maxSelectDist = hitRadiusPx * 1.6f,
                    )
                }

                override fun closeTarget(x: Float, y: Float): Boolean {
                    val c = closeCircle ?: return false
                    val dx = x - c.center.x
                    val dy = y - c.center.y
                    val hit = c.r * 1.3f
                    return dx * dx + dy * dy <= hit * hit
                }
            },
        )
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            stopSelf()
            return START_NOT_STICKY
        }
        if (running) return START_NOT_STICKY
        running = true
        startInForeground()
        startSession()
        return START_NOT_STICKY
    }

    private fun startInForeground() {
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.createNotificationChannel(
            NotificationChannel(
                CHANNEL_ID, "DFI Spike Overlay", NotificationManager.IMPORTANCE_LOW,
            ).apply { description = "DFI C4 floating capture spike session" },
        )
        val stopPending = PendingIntent.getService(
            this, 0,
            Intent(this, OverlayService::class.java).setAction(ACTION_STOP),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        val n: Notification = Notification.Builder(this, CHANNEL_ID)
            .setContentTitle("DFI 스파이크 실행 중")
            .setContentText("플로팅 버튼 세션 활성 (짧게: 음성, 길게: 메뉴)")
            .setSmallIcon(android.R.drawable.ic_btn_speak_now)
            .addAction(Notification.Action.Builder(null, "세션 종료", stopPending).build())
            .setOngoing(true)
            .build()

        if (Build.VERSION.SDK_INT >= 29) {
            startForeground(NOTIF_ID, n, ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE)
        } else {
            startForeground(NOTIF_ID, n)
        }
    }

    private fun startSession() {
        sessionStartMs = SystemClock.elapsedRealtime()
        safe = safeBounds.current()
        log.log(
            SpikeEvents.SESSION_STARTED,
            mapOf(
                "batteryPct" to batteryPct().toString(),
                "safe" to "${safe.left},${safe.top},${safe.right},${safe.bottom}",
                "onDeviceSpeech" to voice.onDeviceAvailable().toString(),
            ),
        )
        addFloatingButton()
    }

    private fun addFloatingButton() {
        if (button != null) return
        val v = FloatingControlView(this) { action, rawX, rawY, eventTime ->
            onButtonTouch(action, rawX, rawY, eventTime)
        }
        val lp = WindowManager.LayoutParams(
            buttonWindowPx, buttonWindowPx,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT,
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = (safe.right - buttonWindowPx - 16f * density).toInt()
            y = ((safe.top + safe.bottom) / 2f - buttonWindowPx / 2f).toInt()
        }
        wm.addView(v, lp)
        button = v
        buttonLp = lp
    }

    private fun ensureLayer(): InteractionLayerView {
        val v = layer ?: InteractionLayerView(this).also { layer = it }
        if (!layerAttached) {
            val lp = WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                PixelFormat.TRANSLUCENT,
            )
            wm.addView(v, lp)
            layerAttached = true
        }
        return v
    }

    private fun removeLayerIfIdle() {
        val v = layer ?: return
        if (layerAttached && v.isIdle) {
            runCatching { wm.removeView(v) }
            layerAttached = false
        }
    }

    /** Center of the floating button in raw screen coordinates. */
    private fun buttonCenter(): Pt {
        val lp = buttonLp ?: return Pt(safe.centerX, safe.centerY)
        val half = buttonWindowPx / 2f
        return Pt(lp.x + half, lp.y + half)
    }

    private fun onButtonTouch(action: Int, rawX: Float, rawY: Float, eventTime: Long) {
        when (action) {
            MotionEvent.ACTION_DOWN -> dispatch(GestureEvent.Down(rawX, rawY, eventTime))
            MotionEvent.ACTION_MOVE -> dispatch(GestureEvent.Move(rawX, rawY, eventTime))
            MotionEvent.ACTION_UP,
            MotionEvent.ACTION_CANCEL,
            -> dispatch(GestureEvent.Up(rawX, rawY, eventTime))
        }
    }

    private fun dispatch(event: GestureEvent) {
        for (effect in machine.on(event)) execute(effect)
    }

    private fun execute(effect: Effect) {
        when (effect) {
            is Effect.StartLongPressTimer ->
                handler.postDelayed(longPressRunnable, LONG_PRESS_MS)

            is Effect.CancelLongPressTimer ->
                handler.removeCallbacks(longPressRunnable)

            is Effect.MoveButtonTo -> {
                val lp = buttonLp ?: return
                val half = buttonWindowPx / 2f
                lp.x = (effect.x - half).toInt()
                lp.y = (effect.y - half).toInt()
                button?.let { runCatching { wm.updateViewLayout(it, lp) } }
            }

            is Effect.SettleButton -> {
                val lp = buttonLp ?: return
                val half = buttonWindowPx / 2f
                val clamped = safe.clampCircle(Pt(effect.x, effect.y), half)
                lp.x = (clamped.x - half).toInt()
                lp.y = (clamped.y - half).toInt()
                button?.let { runCatching { wm.updateViewLayout(it, lp) } }
            }

            is Effect.ShowCloseTarget -> {
                safe = safeBounds.current()
                val c = Circle(
                    Pt(safe.centerX, safe.bottom - closeBottomMarginPx - closeRadiusPx),
                    closeRadiusPx,
                )
                closeCircle = c
                ensureLayer().showClose(c)
            }

            is Effect.HideCloseTarget -> {
                layer?.hideClose()
                closeCircle = null
                removeLayerIfIdle()
            }

            is Effect.OpenRadial -> {
                safe = safeBounds.current()
                val layout = RadialGeometry.layout(
                    safe = safe,
                    anchor = buttonCenter(),
                    actionCount = actionCodes.size,
                    preferredRadius = ringRadiusPx,
                    hitRadius = hitRadiusPx,
                )
                radialLayout = layout
                ensureLayer().showRadial(layout, actionLabels)
                if (layout.degraded) {
                    log.log("RADIAL_LAYOUT_DEGRADED")
                }
            }

            is Effect.CloseRadial -> {
                layer?.hideRadial()
                radialLayout = null
                removeLayerIfIdle()
            }

            is Effect.HighlightTarget -> layer?.highlight(effect.index)

            is Effect.SelectAction -> {
                val code = actionCodes.getOrNull(effect.index) ?: "UNKNOWN"
                log.log("RADIAL_ACTION_SELECTED", mapOf("action" to code))
                Toast.makeText(
                    this,
                    "선택: ${actionLabels.getOrNull(effect.index) ?: code}",
                    Toast.LENGTH_SHORT,
                ).show()
            }

            is Effect.CloseOverlay -> stopSelf()

            is Effect.StartVoice -> {
                ensureLayer().showBanner("음성 인식 중…")
                voice.start { text ->
                    handler.post {
                        layer?.let { l ->
                            if (text != null) l.showBanner("인식: $text") else Unit
                        }
                        removeLayerIfIdle()
                        dispatch(GestureEvent.VoiceEnded(SystemClock.uptimeMillis()))
                    }
                }
            }

            is Effect.Haptic -> haptic(effect.kind)

            is Effect.LogEvent -> {
                log.log(effect.name, effect.fields)
                // Drive close-target arming visuals off the canonical events so
                // rendering cannot drift from the state machine's decision.
                when (effect.name) {
                    SpikeEvents.CLOSE_TARGET_ENTERED -> layer?.setCloseArmed(true)
                    SpikeEvents.CLOSE_CANCELLED -> layer?.setCloseArmed(false)
                }
            }
        }
    }

    private fun haptic(kind: HapticKind) {
        val vib: Vibrator = if (Build.VERSION.SDK_INT >= 31) {
            (getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager).defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
        if (!vib.hasVibrator()) return
        if (Build.VERSION.SDK_INT >= 29) {
            val e = when (kind) {
                HapticKind.RADIAL_OPEN -> VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK)
                HapticKind.TARGET_TICK -> VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK)
                HapticKind.CONFIRM -> VibrationEffect.createPredefined(VibrationEffect.EFFECT_HEAVY_CLICK)
                HapticKind.CLOSE_ARMED -> VibrationEffect.createPredefined(VibrationEffect.EFFECT_DOUBLE_CLICK)
            }
            vib.vibrate(e)
        } else {
            vib.vibrate(VibrationEffect.createOneShot(20, VibrationEffect.DEFAULT_AMPLITUDE))
        }
    }

    private fun batteryPct(): Int {
        val bm = getSystemService(Context.BATTERY_SERVICE) as BatteryManager
        return bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
    }

    override fun onDestroy() {
        handler.removeCallbacks(longPressRunnable)
        // Route through the machine so cleanup effects run exactly once.
        dispatch(GestureEvent.StopRequested(SystemClock.uptimeMillis()))
        voice.destroy()

        button?.let { runCatching { wm.removeView(it) } }
        button = null
        buttonLp = null
        layer?.let { if (layerAttached) runCatching { wm.removeView(it) } }
        layer = null
        layerAttached = false

        if (sessionStartMs > 0) {
            log.log(
                SpikeEvents.SESSION_STOPPED,
                mapOf(
                    "batteryPct" to batteryPct().toString(),
                    "durMs" to (SystemClock.elapsedRealtime() - sessionStartMs).toString(),
                ),
            )
        }
        log.log(SpikeEvents.SERVICE_DESTROYED)
        running = false
        super.onDestroy()
    }
}
