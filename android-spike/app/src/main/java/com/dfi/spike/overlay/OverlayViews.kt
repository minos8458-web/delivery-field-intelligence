package com.dfi.spike.overlay

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Build
import android.view.MotionEvent
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import com.dfi.spike.geometry.Bounds
import com.dfi.spike.geometry.Circle
import com.dfi.spike.geometry.RadialLayout

/** Usable safe bounds for overlay layout: display bounds minus status bar,
 *  navigation/gesture area, and display cutout insets. */
class SafeBoundsProvider(private val context: Context) {

    fun current(): Bounds {
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        if (Build.VERSION.SDK_INT >= 30) {
            val metrics = wm.currentWindowMetrics
            val insets = metrics.windowInsets.getInsetsIgnoringVisibility(
                WindowInsets.Type.statusBars()
                    or WindowInsets.Type.navigationBars()
                    or WindowInsets.Type.displayCutout(),
            )
            val b = metrics.bounds
            return Bounds(
                left = (b.left + insets.left).toFloat(),
                top = (b.top + insets.top).toFloat(),
                right = (b.right - insets.right).toFloat(),
                bottom = (b.bottom - insets.bottom).toFloat(),
            )
        }
        // API 26-29 fallback: real display size with conservative estimated
        // insets (exact cutout rects are not reliably available to overlay
        // windows here). Documented limitation for the spike.
        @Suppress("DEPRECATION")
        val display = wm.defaultDisplay
        val size = android.graphics.Point()
        @Suppress("DEPRECATION")
        display.getRealSize(size)
        val density = context.resources.displayMetrics.density
        return Bounds(
            left = 0f,
            top = 32f * density,
            right = size.x.toFloat(),
            bottom = size.y - 48f * density,
        )
    }
}

/** The visible floating control. Forwards raw touch events to the service;
 *  all interaction logic lives in the pure GestureStateMachine. */
class FloatingControlView(
    context: Context,
    private val onTouch: (action: Int, rawX: Float, rawY: Float, eventTime: Long) -> Unit,
) : View(context) {

    private val fill = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.argb(230, 30, 90, 200) }
    private val ring = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE; style = Paint.Style.STROKE; strokeWidth = 4f
    }
    private val label = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE; textAlign = Paint.Align.CENTER
        textSize = 14f * context.resources.displayMetrics.density
        isFakeBoldText = true
    }

    override fun onDraw(canvas: Canvas) {
        val cx = width / 2f
        val cy = height / 2f
        val r = minOf(cx, cy) - 4f
        canvas.drawCircle(cx, cy, r, fill)
        canvas.drawCircle(cx, cy, r, ring)
        canvas.drawText("DFI", cx, cy + label.textSize / 3f, label)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN,
            MotionEvent.ACTION_MOVE,
            MotionEvent.ACTION_UP,
            MotionEvent.ACTION_CANCEL,
            -> onTouch(event.actionMasked, event.rawX, event.rawY, event.eventTime)
        }
        return true
    }
}

/**
 * Full-screen render layer for radial targets, the close target, and the
 * recognized-voice banner. FLAG_NOT_TOUCHABLE: it never intercepts input;
 * the gesture stream stays on the floating-control window that received
 * ACTION_DOWN. All incoming coordinates are raw screen px and converted to
 * local px using the layer's on-screen offset.
 */
class InteractionLayerView(context: Context) : View(context) {

    var radial: RadialLayout? = null
        private set
    var labels: List<String> = emptyList()
    private var highlighted: Int? = null

    private var closeVisible = false
    private var closeArmed = false
    var closeCircle: Circle? = null
        private set

    private var banner: String? = null

    private val offset = IntArray(2)

    private val slotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.argb(215, 40, 40, 48) }
    private val slotHiPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.argb(240, 30, 140, 90) }
    private val slotRing = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE; style = Paint.Style.STROKE; strokeWidth = 3f
    }
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE; textAlign = Paint.Align.CENTER
        textSize = 12f * context.resources.displayMetrics.density
    }
    private val closePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.argb(200, 150, 40, 40) }
    private val closeArmedPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.argb(255, 220, 40, 40) }
    private val bannerBg = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.argb(200, 0, 0, 0) }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        super.onLayout(changed, l, t, r, b)
        getLocationOnScreen(offset)
    }

    fun showRadial(layout: RadialLayout, actionLabels: List<String>) {
        radial = layout
        labels = actionLabels
        highlighted = null
        invalidate()
    }

    fun hideRadial() {
        radial = null
        highlighted = null
        invalidate()
    }

    fun highlight(index: Int?) {
        highlighted = index
        invalidate()
    }

    fun showClose(circle: Circle) {
        closeCircle = circle
        closeVisible = true
        closeArmed = false
        invalidate()
    }

    fun setCloseArmed(armed: Boolean) {
        closeArmed = armed
        invalidate()
    }

    fun hideClose() {
        closeVisible = false
        closeArmed = false
        invalidate()
    }

    fun showBanner(text: String) {
        banner = text
        invalidate()
        postDelayed({ banner = null; invalidate() }, 4000)
    }

    val isIdle: Boolean get() = radial == null && !closeVisible && banner == null

    override fun onDraw(canvas: Canvas) {
        val dx = -offset[0].toFloat()
        val dy = -offset[1].toFloat()

        radial?.let { l ->
            for (s in l.slots) {
                val hi = s.index == highlighted
                val r = if (hi) l.hitRadius * 0.72f else l.hitRadius * 0.58f
                val paint = if (hi) slotHiPaint else slotPaint
                canvas.drawCircle(s.center.x + dx, s.center.y + dy, r, paint)
                canvas.drawCircle(s.center.x + dx, s.center.y + dy, r, slotRing)
                val label = labels.getOrNull(s.index) ?: s.index.toString()
                canvas.drawText(label, s.center.x + dx, s.center.y + dy + textPaint.textSize / 3f, textPaint)
            }
        }

        if (closeVisible) closeCircle?.let { c ->
            val paint = if (closeArmed) closeArmedPaint else closePaint
            val r = if (closeArmed) c.r else c.r * 0.8f
            canvas.drawCircle(c.center.x + dx, c.center.y + dy, r, paint)
            canvas.drawCircle(c.center.x + dx, c.center.y + dy, r, slotRing)
            canvas.drawText("닫기 ✕", c.center.x + dx, c.center.y + dy + textPaint.textSize / 3f, textPaint)
        }

        banner?.let { text ->
            val cx = width / 2f
            val y = height * 0.18f
            val halfW = textPaint.measureText(text) / 2f + 24f
            canvas.drawRoundRect(cx - halfW, y - 44f, cx + halfW, y + 24f, 16f, 16f, bannerBg)
            canvas.drawText(text, cx, y, textPaint)
        }
    }
}
