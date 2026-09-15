// Copyright 2026, compose-miuix-ui contributors
// SPDX-License-Identifier: Apache-2.0

package top.yukonga.miuix.kmp.glass

import androidx.compose.runtime.mutableFloatStateOf
import kotlin.test.Test
import kotlin.test.assertEquals

class GlassTransformPopupTest {

    @Test
    fun closingRowsCannotReopenSubmenuBeforeOrDuringExit() {
        assertEquals(false, isTransformPopupInteractive(show = false, stacked = false))
        assertEquals(false, isTransformPopupInteractive(show = false, stacked = true))
    }

    @Test
    fun openingRowsCanActivateSubmenuWithoutWaitingForAnimation() {
        assertEquals(true, isTransformPopupInteractive(show = true, stacked = false))
    }

    @Test
    fun predictiveBackScalesPopupProgressTowardItsAnchor() {
        assertEquals(1f, popupFractionWithBack(1f, 0f))
        assertEquals(0.5f, popupFractionWithBack(1f, 0.5f))
        assertEquals(0f, popupFractionWithBack(1f, 1f))
    }

    @Test
    fun dropdownAnchorValueReturnsWithPredictiveBackProgress() {
        val progress = mutableFloatStateOf(0.4f)
        val anchor = GlassPopupAnchor().apply { dropdownBackProgressState = progress }
        assertEquals(0.4f, anchor.dropdownValueAlpha)

        progress.floatValue = 1.2f
        assertEquals(1f, anchor.dropdownValueAlpha)
    }

    @Test
    fun stackedPrimaryRowsRemainNonInteractive() {
        assertEquals(false, isTransformPopupInteractive(show = true, stacked = true))
    }

    @Test
    fun sharedAnchorOpacityTracksAnimationWithoutRepublishingSurface() {
        val progress = mutableFloatStateOf(1f)
        val anchor = GlassPopupAnchor().apply {
            surfaceProgress = progress
            surfaceOpacity = 0.5f
        }
        assertEquals(0.5f, anchor.surfaceAlpha)

        // A frame updates the animation state without recomposing the button's surface publisher.
        progress.floatValue = 0.25f
        assertEquals(0.125f, anchor.surfaceAlpha)
        assertEquals(
            anchor.surfaceAlpha,
            transformPanelAlpha(
                visualAlpha = 0.5f,
                floating = true,
                anchorAlpha = anchor.surfaceAlpha / 0.5f,
                geometryProgress = 0f,
                iconMaterial = 0f,
            ),
        )
        progress.floatValue = 0f
        assertEquals(0f, anchor.surfaceAlpha)
    }

    @Test
    fun transformSpringsUseFolmeValueTargetThreshold() {
        assertEquals(GlassMotion.TRANSFORM_VISIBILITY_THRESHOLD, GlassMotion.transformBounds(true).visibilityThreshold)
        assertEquals(GlassMotion.TRANSFORM_VISIBILITY_THRESHOLD, GlassMotion.transformBounds(false).visibilityThreshold)
        assertEquals(GlassMotion.TRANSFORM_VISIBILITY_THRESHOLD, GlassMotion.transformCenter(true).visibilityThreshold)
        assertEquals(GlassMotion.TRANSFORM_VISIBILITY_THRESHOLD, GlassMotion.transformCenter(false).visibilityThreshold)
    }

    @Test
    fun simplifiedAnchorContentReturnsDuringDismissal() {
        assertEquals(false, shouldRenderAnchorContent(simplified = true, show = true))
        assertEquals(true, shouldRenderAnchorContent(simplified = true, show = false))
        assertEquals(true, shouldRenderAnchorContent(simplified = false, show = true))
    }

    @Test
    fun nonFloatingPanelFadesWithIconMaterial() {
        assertEquals(
            0f,
            transformPanelAlpha(
                visualAlpha = 1f,
                floating = false,
                anchorAlpha = 1f,
                geometryProgress = 0.5f,
                iconMaterial = 0f,
            ),
        )
        assertEquals(
            0.4f,
            transformPanelAlpha(
                visualAlpha = 0.8f,
                floating = false,
                anchorAlpha = 1f,
                geometryProgress = 0.5f,
                iconMaterial = 0.5f,
            ),
        )
    }

    @Test
    fun floatingPanelReturnsToCapturedAnchorBackground() {
        assertEquals(
            1f,
            transformPanelAlpha(
                visualAlpha = 1f,
                floating = true,
                anchorAlpha = 1f,
                geometryProgress = 0f,
                iconMaterial = 0f,
            ),
        )
        assertEquals(
            0f,
            transformPanelAlpha(
                visualAlpha = 1f,
                floating = true,
                anchorAlpha = 0f,
                geometryProgress = 0f,
                iconMaterial = 0f,
            ),
        )
        assertEquals(
            0.5f,
            transformPanelAlpha(
                visualAlpha = 1f,
                floating = true,
                anchorAlpha = 0f,
                geometryProgress = 0.5f,
                iconMaterial = 0f,
            ),
        )
    }
}
