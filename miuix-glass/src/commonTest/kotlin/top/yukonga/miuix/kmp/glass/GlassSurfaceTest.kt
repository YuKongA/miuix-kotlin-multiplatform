// Copyright 2026, compose-miuix-ui contributors
// SPDX-License-Identifier: Apache-2.0

package top.yukonga.miuix.kmp.glass

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

class GlassSurfaceTest {

    @Test
    fun roundedHostShapeDrivesGlassSilhouette() {
        val hostShape = RoundedCornerShape(
            topStart = 4.dp,
            topEnd = 8.dp,
            bottomEnd = 12.dp,
            bottomStart = 16.dp,
        )

        assertEquals(
            GlassShape(
                topStart = 4.dp,
                topEnd = 8.dp,
                bottomEnd = 12.dp,
                bottomStart = 16.dp,
            ),
            resolveGlassSurfaceShape(hostShape, fallbackCornerRadius = 24.dp),
        )
    }

    @Test
    fun existingGlassShapeKeepsItsSmoothing() {
        val hostShape = GlassShape(16.dp, smoothing = 0.35f)

        assertSame(hostShape, resolveGlassSurfaceShape(hostShape, fallbackCornerRadius = 24.dp))
    }

    @Test
    fun unsupportedShapeUsesExplicitFallbackRadius() {
        assertEquals(
            GlassShape(20.dp),
            resolveGlassSurfaceShape(RectangleShape, fallbackCornerRadius = 20.dp),
        )
    }
}
