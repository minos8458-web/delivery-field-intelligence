package com.dfi.spike

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import com.dfi.spike.log.SpikeEventLog
import com.dfi.spike.overlay.OverlayService
import com.dfi.spike.speech.VoiceCaptureController

/**
 * C4 spike launcher activity. Plain programmatic views, no androidx.
 *
 * Responsibilities:
 *  - explain and request the three required permissions
 *    (overlay, microphone, notifications on 33+)
 *  - start/stop the overlay foreground service explicitly
 *  - show and clear the local spike event log
 *
 * No permission request loops: each request is user-initiated by button.
 */
class MainActivity : Activity() {

    private lateinit var status: TextView
    private lateinit var logView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            val pad = (16 * resources.displayMetrics.density).toInt()
            setPadding(pad, pad, pad, pad)
        }

        status = TextView(this)
        root.addView(status)

        root.addView(
            button("1. 오버레이 권한 (다른 앱 위에 표시)") {
                // Rationale: the floating capture button must draw over the
                // carrier app during delivery. Without this, DFI cannot exist
                // as a passive field-assistance layer.
                if (!Settings.canDrawOverlays(this)) {
                    startActivity(
                        Intent(
                            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                            Uri.parse("package:$packageName"),
                        ),
                    )
                } else refresh()
            },
        )
        root.addView(
            button("2. 마이크 권한 (짧은 탭 음성 캡처)") {
                if (checkSelfPermission(Manifest.permission.RECORD_AUDIO)
                    != PackageManager.PERMISSION_GRANTED
                ) {
                    requestPermissions(arrayOf(Manifest.permission.RECORD_AUDIO), 10)
                } else refresh()
            },
        )
        root.addView(
            button("3. 알림 권한 (세션 상태 표시)") {
                if (Build.VERSION.SDK_INT >= 33 &&
                    checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED
                ) {
                    requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 11)
                } else refresh()
            },
        )
        root.addView(
            button("세션 시작 (플로팅 버튼 표시)") {
                // Technically required before startForegroundService:
                //   • SYSTEM_ALERT_WINDOW: service adds TYPE_APPLICATION_OVERLAY window immediately.
                //   • RECORD_AUDIO: Android 14+ (API 34) throws SecurityException if a
                //     FOREGROUND_SERVICE_TYPE_MICROPHONE service is started without this grant.
                //     Below API 34 the service starts but voice capture fails at SpeechRecognizer;
                //     we block it here on all API levels to avoid a broken session.
                // NOT required to start the service:
                //   • POST_NOTIFICATIONS: denial only silences the notification; the service itself
                //     is unaffected. The tester is warned in the status text but not blocked.
                val canStart = canStartSession()
                if (canStart == null) {
                    startForegroundService(Intent(this, OverlayService::class.java))
                } else {
                    android.widget.Toast.makeText(this, canStart, android.widget.Toast.LENGTH_LONG)
                        .show()
                }
                refresh()
            },
        )
        root.addView(
            button("세션 종료") {
                stopService(Intent(this, OverlayService::class.java))
                refresh()
            },
        )
        root.addView(button("이벤트 로그 새로고침") { refresh() })
        root.addView(
            button("이벤트 로그 삭제") {
                SpikeEventLog.clear(this)
                refresh()
            },
        )

        logView = TextView(this).apply { textSize = 11f }
        val scroll = ScrollView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f,
            )
            addView(logView)
        }
        root.addView(scroll)

        setContentView(root)
    }

    override fun onResume() {
        super.onResume()
        refresh()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        refresh() // Single-shot: denial is shown in status, never re-looped.
    }

    /**
     * Returns null when all technically required permissions are satisfied and
     * session start is allowed, or a human-readable Korean reason string when
     * blocked.
     *
     * Technically required (missing → session start is blocked):
     *   SYSTEM_ALERT_WINDOW  — window is added immediately on service start
     *   RECORD_AUDIO         — FOREGROUND_SERVICE_TYPE_MICROPHONE on API 34+ throws
     *                          SecurityException without this; below 34 voice capture fails
     *
     * Strongly recommended but NOT technically required:
     *   POST_NOTIFICATIONS   — denial only silences the status notification; service runs fine
     */
    private fun canStartSession(): String? {
        if (!Settings.canDrawOverlays(this))
            return "세션을 시작하려면 먼저 오버레이 권한(1번)을 허용해 주세요."
        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED
        )
            return "세션을 시작하려면 마이크 권한(2번)이 필요합니다." +
                if (Build.VERSION.SDK_INT >= 34) " (Android 14+: 권한 없이 시작하면 시스템 오류 발생)" else ""
        return null // all required permissions granted
    }

    private fun refresh() {
        val overlay = Settings.canDrawOverlays(this)
        val mic = checkSelfPermission(Manifest.permission.RECORD_AUDIO) ==
            PackageManager.PERMISSION_GRANTED
        val notifGranted = Build.VERSION.SDK_INT < 33 ||
            checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED
        val speech = VoiceCaptureController(this, SpikeEventLog(this)).describeAvailability()
        val blockReason = canStartSession()

        status.text = buildString {
            appendLine("DFI C4 스파이크")
            appendLine("오버레이 권한 [필수]: ${if (overlay) "허용" else "필요 — 버튼 1번"}")
            appendLine("마이크 권한 [필수]: ${if (mic) "허용" else "필요 — 버튼 2번"}")
            if (Build.VERSION.SDK_INT >= 33) {
                appendLine(
                    "알림 권한 [권장]: ${if (notifGranted) "허용" else "미허용 — 세션 알림이 표시되지 않음 (시작은 가능)"}",
                )
            }
            appendLine("음성 인식: $speech")
            appendLine("세션: ${if (OverlayService.running) "실행 중" else "중지됨"}")
            if (blockReason != null) appendLine("⛔ $blockReason")
        }
        logView.text = SpikeEventLog.read(this).takeLast(6000)
    }

    private fun button(text: String, onClick: () -> Unit): Button =
        Button(this).apply {
            this.text = text
            isAllCaps = false
            setOnClickListener { onClick() }
        }
}
