// Copyright 2026, compose-miuix-ui contributors
// SPDX-License-Identifier: Apache-2.0

package top.yukonga.miuix.kmp.glass

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.LayoutDirection
import kotlin.test.Test
import kotlin.test.assertEquals

class GlassSecondaryPopupTest {
    private val page = Size(400f, 800f)
    private val row = Rect(120f, 200f, 320f, 260f)

    @Test
    fun ltrAlignsStartRatherThanEndWhenSecondaryIsWider() {
        val result = placeGlassSecondaryPopup(row, Size(240f, 300f), 12f, page, LayoutDirection.Ltr)
        assertEquals(Rect(120f, 200f, 360f, 500f), result)
    }

    @Test
    fun rtlAlignsTheOppositeStartEdge() {
        val result = placeGlassSecondaryPopup(row, Size(240f, 300f), 12f, page, LayoutDirection.Rtl)
        assertEquals(Rect(80f, 200f, 320f, 500f), result)
    }

    @Test
    fun bottomOverflowShiftsUpInsteadOfOpeningAboveAnchor() {
        val result = placeGlassSecondaryPopup(Rect(120f, 650f, 320f, 710f), Size(200f, 300f), 12f, page, LayoutDirection.Ltr)
        assertEquals(Rect(120f, 488f, 320f, 788f), result)
    }

    @Test
    fun oversizedMenuFitsTheSafeViewport() {
        val result = placeGlassSecondaryPopup(row, Size(500f, 900f), 12f, page, LayoutDirection.Ltr)
        assertEquals(Rect(12f, 12f, 388f, 788f), result)
    }

    @Test
    fun initialClipIsTheTriggerRowPlusPadding() {
        val end = Rect(120f, 200f, 360f, 500f)
        assertEquals(Rect(120f, 192f, 320f, 268f), secondaryPopupRect(row, end, 8f, 8f, 0f))
        assertEquals(end, secondaryPopupRect(row, end, 8f, 8f, 1f))
    }

    @Test
    fun allEdgesMoveWithoutScalingTheRows() {
        val end = Rect(100f, 100f, 340f, 400f)
        assertEquals(Rect(110f, 146f, 330f, 334f), secondaryPopupRect(row, end, 8f, 8f, 0.5f))
    }

    @Test
    fun collapseReturnsToThePaddedRowWithoutNegativeClip() {
        val end = Rect(120f, 200f, 360f, 500f)
        assertEquals(Rect(120f, 192f, 320f, 268f), secondaryPopupRect(row, end, 8f, 8f, -0.01f))
    }

    @Test
    fun geometryUsesNativeDefaultAndCollapseSprings() {
        val enter = GlassMotion.secondaryPopup(true)
        val exit = GlassMotion.secondaryPopup(false)
        assertEquals(0.95f, enter.dampingRatio)
        assertEquals(GlassMotion.springOf<Float>(0.95f, 0.35f).stiffness, enter.stiffness)
        assertEquals(GlassMotion.springOf<Float>(0.95f, 0.2f).stiffness, exit.stiffness)
        assertEquals(0.0015f, enter.visibilityThreshold)
    }
}
