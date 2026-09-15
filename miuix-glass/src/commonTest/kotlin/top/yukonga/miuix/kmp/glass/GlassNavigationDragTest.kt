// Copyright 2026, compose-miuix-ui contributors
// SPDX-License-Identifier: Apache-2.0

package top.yukonga.miuix.kmp.glass

import androidx.compose.animation.core.TargetBasedAnimation
import androidx.compose.animation.core.VectorConverter
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GlassNavigationDragTest {
    @Test
    fun rightwardDragStretchesOnlyTheTrailingEdge() {
        val target = navigationDragTarget(100f, 80f, 320f, 5f, false)
        assertEquals(80f, target.left)
        assertEquals(180f, target.right)
        assertEquals(GlassMotion.navDragFollow(), target.leftSpring)
        assertEquals(target.leftSpring, target.rightSpring)
    }

    @Test
    fun reversalMovesTheTrailToTheOtherEdge() {
        val target = navigationDragTarget(100f, 80f, 320f, -5f, false)
        assertEquals(100f, target.left)
        assertEquals(200f, target.right)
    }

    @Test
    fun trailIsCappedAtSixtyPixels() {
        val target = navigationDragTarget(100f, 80f, 320f, 100f, false)
        assertEquals(40f, target.left)
        assertEquals(180f, target.right)
    }

    @Test
    fun leadingBoundarySuppressesStretch() {
        val right = navigationDragTarget(300f, 80f, 320f, 20f, false)
        assertEquals(240f, right.left)
        assertEquals(320f, right.right)
        val left = navigationDragTarget(-10f, 80f, 320f, -20f, false)
        assertEquals(0f, left.left)
        assertEquals(80f, left.right)
    }

    @Test
    fun rawTrailCanExceedContainerButRenderedEdgeIsLimited() {
        val target = navigationDragTarget(10f, 80f, 320f, 10f, false)
        assertEquals(-30f, target.left)
        assertEquals(90f, target.right)
        val bounds = navigationIndicatorBounds(target.left, target.right, 320f, 3f)
        assertEquals(3f, bounds.x)
        assertEquals(90f, bounds.y)
    }

    @Test
    fun firstToFourthAndBackNeverDrawOutsideEndItems() {
        val barWidth = 344f
        val inset = 3f
        val slot = (barWidth - 16f) / 4f
        val chipWidth = slot + 10f
        for (forward in listOf(true, false)) {
            val from = if (forward) inset else inset + 3f * slot
            val to = if (forward) inset + 3f * slot else inset
            val left = TargetBasedAnimation(
                GlassMotion.edgeSpring<Float>(!forward),
                Float.VectorConverter,
                from,
                to,
            )
            val right = TargetBasedAnimation(
                GlassMotion.edgeSpring<Float>(forward),
                Float.VectorConverter,
                from + chipWidth,
                to + chipWidth,
            )
            var overshot = false
            for (frame in 0..120) {
                val time = frame * 8_000_000L
                val rawLeft = left.getValueFromNanos(time)
                val rawRight = right.getValueFromNanos(time)
                overshot = overshot || rawLeft < inset || rawRight > barWidth - inset
                val bounds = navigationIndicatorBounds(rawLeft, rawRight, barWidth, inset)
                assertTrue(bounds.x >= inset)
                assertTrue(bounds.y <= barWidth - inset)
                assertTrue(bounds.y >= bounds.x)
            }
            assertTrue(overshot, "The regression must exercise actual spring overshoot")
        }
    }

    @Test
    fun interiorStretchIsUnchanged() {
        val bounds = navigationIndicatorBounds(80f, 200f, 344f, 3f)
        assertEquals(80f, bounds.x)
        assertEquals(200f, bounds.y)
    }

    @Test
    fun crossingItemsUsesDirectionalSpringsWithoutExtraTrail() {
        val right = navigationDragTarget(100f, 80f, 320f, 10f, true)
        assertEquals(100f, right.left)
        assertEquals(180f, right.right)
        assertEquals(GlassMotion.navIndicatorTrail(), right.leftSpring)
        assertEquals(GlassMotion.navIndicator(), right.rightSpring)
        val left = navigationDragTarget(100f, 80f, 320f, -10f, true)
        assertEquals(GlassMotion.navIndicator(), left.leftSpring)
        assertEquals(GlassMotion.navIndicatorTrail(), left.rightSpring)
    }

    @Test
    fun zeroDeltaRestoresNominalTargetWidth() {
        val target = navigationDragTarget(100f, 80f, 320f, 0f, false)
        assertEquals(100f, target.left)
        assertEquals(180f, target.right)
    }

    @Test
    fun followSpringLagsThenSettlesWithoutOvershoot() {
        val animation = TargetBasedAnimation(
            animationSpec = GlassMotion.navDragFollow(),
            typeConverter = Float.VectorConverter,
            initialValue = 0f,
            targetValue = 100f,
        )
        val firstFrame = animation.getValueFromNanos(16_000_000L)
        assertTrue(firstFrame > 0f && firstFrame < 100f)
        var previous = 0f
        for (frame in 1..60) {
            val value = animation.getValueFromNanos(frame * 16_000_000L)
            assertTrue(value >= previous && value <= 100f)
            previous = value
        }
        assertEquals(100f, previous)
    }
}
