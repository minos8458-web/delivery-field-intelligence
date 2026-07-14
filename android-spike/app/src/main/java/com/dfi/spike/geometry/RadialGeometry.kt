package com.dfi.spike.geometry

import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

/**
 * Safe-area-aware adaptive radial menu geometry.
 *
 * Pure and deterministic: same input -> same output. No Android imports.
 *
 * Algorithm (per DFI_FLOATING_INTERACTION_FIELD_ASSISTANCE_MODEL_V1):
 *  1. Sample candidate angles around the anchor at the current ring radius.
 *  2. An angle is allowed when the full action HIT circle fits inside the
 *     supplied safe bounds and avoids exclusion circles.
 *  3. Find the longest contiguous allowed arc (circular, wrap-aware).
 *  4. Center the action fan on that arc's bisector; compress the angular gap
 *     down to a minimum before giving up on this radius.
 *  5. If no radius admits the fan, shrink the radius stepwise and retry.
 *  6. Final invariant: every returned hit-target circle is clamped inside the
 *     safe bounds. A layout that needed clamping or radius fallback is marked
 *     `degraded` for logging.
 *
 * This is intentionally not a nine-zone lookup: it works from the actual
 * safe-bounds rectangle supplied by the caller.
 */

data class Slot(val index: Int, val angleDeg: Float, val center: Pt)

data class RadialLayout(
    val anchor: Pt,
    val ringRadius: Float,
    val hitRadius: Float,
    val slots: List<Slot>,
    val degraded: Boolean,
)

object RadialGeometry {

    private const val STEP_DEG = 2
    private const val SAMPLES = 360 / STEP_DEG

    /**
     * Radius reflow schedule, tried in order. Growing comes before shrinking:
     * near edges and corners the available arc is angularly bounded (a corner
     * exposes at most ~90 degrees at ANY radius), so the only way to keep hit
     * targets physically separated inside a narrow fan is a LARGER ring.
     * Shrinking is the tail case for very small safe bounds. The trade-off
     * (longer thumb travel from corners) is a C4 field-measurement question.
     */
    private val RADIUS_SCHEDULE = floatArrayOf(1f, 1.3f, 1.6f, 2f, 2.5f, 0.85f, 0.7f, 0.55f)

    fun layout(
        safe: Bounds,
        anchor: Pt,
        actionCount: Int,
        preferredRadius: Float,
        hitRadius: Float,
        preferredGapDeg: Float = 45f,
        exclusions: List<Circle> = emptyList(),
        /** Minimum center-to-center distance between adjacent hit targets.
         *  Default: tangent hit circles (no overlap). */
        minSeparation: Float = 2f * hitRadius,
    ): RadialLayout {
        require(actionCount > 0) { "actionCount must be > 0" }

        for (mult in RADIUS_SCHEDULE) {
            val radius = preferredRadius * mult
            val allowed = BooleanArray(SAMPLES) { i ->
                val p = pointAt(anchor, radius, (i * STEP_DEG).toFloat())
                safe.containsCircle(p, hitRadius) &&
                    exclusions.none { it.intersectsCircle(p, hitRadius) }
            }
            val run = longestCircularRun(allowed) ?: continue
            val (startIdx, len) = run

            // The angular gap that guarantees minSeparation at this radius.
            val minGapDeg = Math.toDegrees((minSeparation / radius).toDouble()).toFloat()

            if (len == SAMPLES) {
                // Full circle available (e.g. anchor near screen center):
                // broader radial layout, fan centered pointing up.
                val gap = min(preferredGapDeg, 360f / actionCount)
                if (gap < minGapDeg) continue
                val span = gap * (actionCount - 1)
                return finalize(safe, anchor, radius, hitRadius, actionCount,
                    startDeg = -90f - span / 2f, gapDeg = gap, degradedIn = false)
            }

            val runSpan = ((len - 1) * STEP_DEG).toFloat()
            if (actionCount == 1 || runSpan >= (actionCount - 1) * minGapDeg) {
                val gap = if (actionCount > 1) {
                    min(preferredGapDeg, runSpan / (actionCount - 1))
                        .coerceAtLeast(minGapDeg)
                } else 0f
                val span = gap * (actionCount - 1)
                val runStartDeg = (startIdx * STEP_DEG).toFloat()
                return finalize(safe, anchor, radius, hitRadius, actionCount,
                    startDeg = runStartDeg + (runSpan - span) / 2f, gapDeg = gap,
                    degradedIn = false)
            }
        }

        // Last-resort fallback for pathologically small safe bounds:
        // even spacing, every target clamped inside bounds, marked degraded.
        val gap = 360f / actionCount
        return finalize(safe, anchor, preferredRadius, hitRadius, actionCount,
            startDeg = -90f - gap * (actionCount - 1) / 2f, gapDeg = gap,
            degradedIn = true)
    }

    /** Nearest selectable slot for a touch point, or null.
     *  Null inside the neutral radius around the anchor (cancel zone),
     *  and null when the touch is too far from every slot. */
    fun nearestSlot(
        touch: Pt,
        layout: RadialLayout,
        neutralRadius: Float,
        maxSelectDist: Float,
    ): Int? {
        if (dist(touch, layout.anchor) <= neutralRadius) return null
        var best: Int? = null
        var bestD = Float.MAX_VALUE
        for (s in layout.slots) {
            val d = dist(touch, s.center)
            if (d < bestD) { bestD = d; best = s.index }
        }
        return if (bestD <= maxSelectDist) best else null
    }

    // ---- internals ----

    private fun pointAt(anchor: Pt, radius: Float, angleDeg: Float): Pt {
        val rad = Math.toRadians(angleDeg.toDouble())
        return Pt(
            anchor.x + radius * cos(rad).toFloat(),
            anchor.y + radius * sin(rad).toFloat(),
        )
    }

    /** Longest circular run of `true`; returns (startIndex, length) or null.
     *  Deterministic: the first maximal run found when scanning from index 0. */
    private fun longestCircularRun(allowed: BooleanArray): Pair<Int, Int>? {
        val n = allowed.size
        if (allowed.all { it }) return 0 to n
        if (allowed.none { it }) return null
        var bestStart = -1
        var bestLen = 0
        var runStart = -1
        var runLen = 0
        // Scan doubled array to handle wrap-around runs.
        for (i in 0 until 2 * n) {
            if (allowed[i % n]) {
                if (runLen == 0) runStart = i
                runLen++
                // Cap: a run can never exceed n (all-true handled above).
                if (runLen > n) runLen = n
                if (runLen > bestLen) { bestLen = runLen; bestStart = runStart }
            } else {
                runLen = 0
            }
        }
        return (bestStart % n) to bestLen
    }

    private fun finalize(
        safe: Bounds,
        anchor: Pt,
        radius: Float,
        hitRadius: Float,
        actionCount: Int,
        startDeg: Float,
        gapDeg: Float,
        degradedIn: Boolean,
    ): RadialLayout {
        var degraded = degradedIn
        val slots = (0 until actionCount).map { i ->
            val angle = startDeg + gapDeg * i
            val raw = pointAt(anchor, radius, angle)
            val clamped = safe.clampCircle(raw, hitRadius)
            if (dist(raw, clamped) > 0.5f) degraded = true
            Slot(index = i, angleDeg = angle, center = clamped)
        }
        return RadialLayout(anchor, radius, hitRadius, slots, degraded)
    }
}
