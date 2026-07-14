package com.dfi.spike.geometry

import kotlin.math.hypot

/** Pure geometry primitives. No Android imports: JVM-unit-testable. */

data class Pt(val x: Float, val y: Float)

data class Circle(val center: Pt, val r: Float) {
    fun contains(p: Pt): Boolean = dist(center, p) <= r
    fun intersectsCircle(otherCenter: Pt, otherR: Float): Boolean =
        dist(center, otherCenter) < r + otherR
}

data class Bounds(val left: Float, val top: Float, val right: Float, val bottom: Float) {
    val width: Float get() = right - left
    val height: Float get() = bottom - top
    val centerX: Float get() = (left + right) / 2f
    val centerY: Float get() = (top + bottom) / 2f

    fun containsCircle(c: Pt, r: Float): Boolean =
        c.x - r >= left && c.x + r <= right && c.y - r >= top && c.y + r <= bottom

    /** Clamp a circle center so the whole circle stays inside these bounds.
     *  If the bounds are smaller than the circle, clamp to the bounds center axis. */
    fun clampCircle(c: Pt, r: Float): Pt {
        val x = if (width >= 2 * r) c.x.coerceIn(left + r, right - r) else centerX
        val y = if (height >= 2 * r) c.y.coerceIn(top + r, bottom - r) else centerY
        return Pt(x, y)
    }
}

fun dist(a: Pt, b: Pt): Float = hypot(a.x - b.x, a.y - b.y)
