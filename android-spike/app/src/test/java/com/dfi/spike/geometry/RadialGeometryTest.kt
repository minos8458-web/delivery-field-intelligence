package com.dfi.spike.geometry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class RadialGeometryTest {

    // A realistic usable area after status bar / cutout (top) and gesture
    // nav (bottom) insets on a 1080x2340 display.
    private val safe = Bounds(left = 0f, top = 96f, right = 1080f, bottom = 2200f)
    private val actionCount = 6
    private val ringRadius = 300f
    private val hitRadius = 96f

    private fun layoutAt(x: Float, y: Float): RadialLayout =
        RadialGeometry.layout(safe, Pt(x, y), actionCount, ringRadius, hitRadius)

    private fun assertAllTargetsInside(l: RadialLayout) {
        assertEquals(actionCount, l.slots.size)
        for (s in l.slots) {
            assertTrue(
                safe.containsCircle(s.center, l.hitRadius),
                "slot ${s.index} at ${s.center} (hitR=${l.hitRadius}) escapes safe bounds $safe",
            )
        }
    }

    private fun assertSlotsDistinct(l: RadialLayout, minSeparation: Float = 40f) {
        for (a in l.slots) for (b in l.slots) {
            if (a.index < b.index) {
                assertTrue(
                    dist(a.center, b.center) >= minSeparation,
                    "slots ${a.index} and ${b.index} collapsed: ${a.center} / ${b.center}",
                )
            }
        }
    }

    @Test fun leftEdgeKeepsAllTargetsInside() {
        val l = layoutAt(60f, 1100f)
        assertAllTargetsInside(l)
        assertSlotsDistinct(l)
        // Expansion must be inward: every slot to the right of the anchor side.
        assertTrue(l.slots.all { it.center.x >= 0f + l.hitRadius })
    }

    @Test fun rightEdgeKeepsAllTargetsInside() {
        val l = layoutAt(1020f, 1100f)
        assertAllTargetsInside(l)
        assertSlotsDistinct(l)
    }

    @Test fun topEdgeKeepsAllTargetsInside() {
        val l = layoutAt(540f, 150f)
        assertAllTargetsInside(l)
        assertSlotsDistinct(l)
        // Top edge: actions must expand downward of the unsafe strip.
        assertTrue(l.slots.all { it.center.y >= safe.top + l.hitRadius })
    }

    @Test fun bottomEdgeKeepsAllTargetsInside() {
        val l = layoutAt(540f, 2150f)
        assertAllTargetsInside(l)
        assertSlotsDistinct(l)
    }

    @Test fun topLeftCornerKeepsAllTargetsInside() {
        val l = layoutAt(60f, 150f)
        assertAllTargetsInside(l)
        assertSlotsDistinct(l)
    }

    @Test fun topRightCornerKeepsAllTargetsInside() {
        val l = layoutAt(1020f, 150f)
        assertAllTargetsInside(l)
        assertSlotsDistinct(l)
    }

    @Test fun bottomLeftCornerKeepsAllTargetsInside() {
        val l = layoutAt(60f, 2150f)
        assertAllTargetsInside(l)
        assertSlotsDistinct(l)
    }

    @Test fun bottomRightCornerKeepsAllTargetsInside() {
        val l = layoutAt(1020f, 2150f)
        assertAllTargetsInside(l)
        assertSlotsDistinct(l)
    }

    @Test fun centerKeepsAllTargetsInsideWithoutDegradation() {
        val l = layoutAt(540f, 1150f)
        assertAllTargetsInside(l)
        assertSlotsDistinct(l)
        assertTrue(!l.degraded, "center layout should not degrade")
        assertEquals(ringRadius, l.ringRadius, "center layout should keep preferred radius")
    }

    @Test fun deterministicForIdenticalInput() {
        for (p in listOf(
            Pt(60f, 1100f), Pt(1020f, 150f), Pt(540f, 1150f), Pt(60f, 2150f),
        )) {
            val a = RadialGeometry.layout(safe, p, actionCount, ringRadius, hitRadius)
            val b = RadialGeometry.layout(safe, p, actionCount, ringRadius, hitRadius)
            assertEquals(a, b, "layout not deterministic at $p")
        }
    }

    @Test fun exclusionCircleIsAvoidedWhenSpaceAllows() {
        val exclusion = Circle(Pt(540f, 2100f), 140f)
        val l = RadialGeometry.layout(
            safe, Pt(540f, 1800f), actionCount, ringRadius, hitRadius,
            exclusions = listOf(exclusion),
        )
        assertAllTargetsInside(l)
        if (!l.degraded) {
            for (s in l.slots) {
                assertTrue(
                    !exclusion.intersectsCircle(s.center, l.hitRadius),
                    "slot ${s.index} intersects exclusion",
                )
            }
        }
    }

    @Test fun tinyBoundsFallbackStillKeepsTargetsInside() {
        val tiny = Bounds(0f, 0f, 420f, 420f)
        val l = RadialGeometry.layout(tiny, Pt(210f, 210f), actionCount, ringRadius, 60f)
        assertEquals(actionCount, l.slots.size)
        for (s in l.slots) {
            assertTrue(tiny.containsCircle(s.center, l.hitRadius), "slot escaped tiny bounds")
        }
        assertTrue(l.degraded, "tiny bounds layout must be marked degraded")
    }

    @Test fun nearestSlotIsNullInsideNeutralZone() {
        val l = layoutAt(540f, 1150f)
        assertNull(RadialGeometry.nearestSlot(Pt(545f, 1145f), l, neutralRadius = 90f, maxSelectDist = 160f))
    }

    @Test fun nearestSlotSelectsClosestTarget() {
        val l = layoutAt(540f, 1150f)
        val target = l.slots[2]
        val touch = Pt(target.center.x + 10f, target.center.y - 10f)
        val idx = RadialGeometry.nearestSlot(touch, l, neutralRadius = 90f, maxSelectDist = 160f)
        assertNotNull(idx)
        assertEquals(2, idx)
    }

    @Test fun nearestSlotIsNullFarOutsideRing() {
        val l = layoutAt(540f, 1150f)
        assertNull(RadialGeometry.nearestSlot(Pt(540f, 2190f), l, neutralRadius = 90f, maxSelectDist = 160f))
    }
}
