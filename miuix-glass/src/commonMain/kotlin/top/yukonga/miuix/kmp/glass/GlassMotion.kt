// Copyright 2026, compose-miuix-ui contributors
// SPDX-License-Identifier: Apache-2.0

package top.yukonga.miuix.kmp.glass

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.Color
import top.yukonga.miuix.kmp.anim.folmeSpring

/** The motion the source system animates its glass surfaces with. */
object GlassMotion {

    /**
     * Converts one of the source system's spring definitions into a Compose [SpringSpec].
     *
     * The conversion itself is [folmeSpring], which the library already carries. This adds only the
     * check that the response is usable.
     *
     * @param damping The damping ratio. 1 settles without overshoot; below 1 overshoots once.
     * @param response The period of the undamped oscillation, in seconds. Smaller is faster. Must
     *   be greater than zero — the stiffness is derived by dividing by it, and zero would hand the
     *   animation an infinite or undefined stiffness rather than failing where the mistake is.
     */
    @Stable
    fun <T> springOf(damping: Float, response: Float): SpringSpec<T> {
        require(response > 0f) { "A spring's response must be greater than zero, was $response" }
        return folmeSpring(damping, response)
    }

    /** A transform spring with Folme's `ValueTarget` settle threshold instead of Compose's. */
    @Stable
    private fun transformSpring(damping: Float, response: Float): SpringSpec<Float> = folmeSpring(
        damping = damping,
        response = response,
        visibilityThreshold = TRANSFORM_VISIBILITY_THRESHOLD,
    )

    /**
     * The spring the leading edge of the bottom bar's capsule travels on.
     *
     * The capsule does not travel rigidly. Its `OverlayView` puts the two edges on different
     * springs and swaps which edge gets which by the direction of travel, so the trailing edge is
     * always the slower of the two ([navIndicatorTrail]) and the capsule stretches on its way. That
     * stretch, and the overshoot this spring's damping ratio leaves, are the whole of what makes
     * the capsule read as elastic rather than as a box on rails.
     */
    @Stable
    fun <T> navIndicator(): SpringSpec<T> = springOf(0.7f, 0.4f)

    /** The spring the trailing edge of the capsule travels on. Slower, so the capsule stretches. */
    @Stable
    fun <T> navIndicatorTrail(): SpringSpec<T> = springOf(0.75f, 0.5f)

    /** The bottom indicator follows drag targets with a critically damped spring. */
    @Stable
    fun <T> navDragFollow(): SpringSpec<T> = springOf(1f, 0.15f)

    /** [navIndicator] for the edge that leads the travel, [navIndicatorTrail] for the one behind. */
    @Stable
    fun <T> edgeSpring(leading: Boolean): SpringSpec<T> = if (leading) navIndicator() else navIndicatorTrail()

    /** The capsule's fill arriving under a finger. Critically damped, and quick. */
    @Stable
    fun navPressEnter(): SpringSpec<Color> = springOf(1f, 0.2f)

    /** The capsule's fill leaving on release. Slower than the press, so the lift reads as a fade. */
    @Stable
    fun navPressExit(): SpringSpec<Color> = springOf(0.95f, 0.35f)

    /**
     * A menu row's fill leaving on release.
     *
     * Shorter than the bar's. A row is often gone, or behind a second menu, before the bar's fade
     * would finish, and the block that outlives its row reads as a stain on the panel under it.
     */
    @Stable
    fun popupPressExit(): SpringSpec<Color> = springOf(1f, 0.15f)

    /** The spring the bottom bar shows and hides on, from the same source class. */
    @Stable
    fun <T> navShowHide(): SpringSpec<T> = springOf(1f, 0.3f)

    /** The system's default spring, and the one its segmented tab indicator moves on. */
    @Stable
    fun <T> default(): SpringSpec<T> = springOf(0.95f, 0.35f)

    /** HyperPopupWindow's default opening spring and explicit secondary collapse spring. */
    @Stable
    fun secondaryPopup(expanding: Boolean): SpringSpec<Float> = if (expanding) {
        transformSpring(0.95f, 0.35f)
    } else {
        transformSpring(0.95f, 0.2f)
    }

    /** HyperPopupWindow's independent AUTO_ALPHA track, with ViewProperty's alpha threshold. */
    @Stable
    internal fun secondaryPopupMask(expanding: Boolean): SpringSpec<Float> = folmeSpring(
        damping = 0.95f,
        response = if (expanding) 0.35f else 0.2f,
        visibilityThreshold = POPUP_MASK_MIN_VISIBLE_CHANGE * 0.75f,
    )

    /** ViewProperty.AUTO_ALPHA hides the mask below one 8-bit alpha step. */
    internal const val POPUP_MASK_MIN_VISIBLE_CHANGE: Float = 1f / 256f

    /** A top bar expanding back to its large title. */
    @Stable
    fun <T> barExpand(): SpringSpec<T> = springOf(1f, 0.3f)

    /** A top bar collapsing to its compact title. Deliberately quicker than the expansion. */
    @Stable
    fun <T> barCollapse(): SpringSpec<T> = springOf(1f, 0.15f)

    /** A large title tracking a scroll. Slow, so it trails the content rather than snapping. */
    @Stable
    fun <T> barTrack(): SpringSpec<T> = springOf(1f, 0.6f)

    /** The linear material transition of a floating action-bar button. */
    @Stable
    fun topBarButtonFloat(): FiniteAnimationSpec<Float> = tween(
        durationMillis = 350,
        easing = LinearEasing,
    )

    /** ActionBarContainer's overlay mask follows a separate 100ms linear transition. */
    @Stable
    fun topBarMask(): FiniteAnimationSpec<Float> = tween(durationMillis = 100, easing = LinearEasing)

    /** A popup opening from its anchor. The only under-damped spring in the set — it overshoots. */
    @Stable
    fun <T> popupEnter(): SpringSpec<T> = springOf(0.8f, 0.28f)

    /** A popup closing. */
    @Stable
    fun <T> popupExit(): SpringSpec<T> = springOf(0.95f, 0.2f)

    /** A dialog entering. */
    @Stable
    fun <T> dialogEnter(): SpringSpec<T> = springOf(0.95f, 0.35f)

    /** A dialog leaving. */
    @Stable
    fun <T> dialogExit(): SpringSpec<T> = springOf(0.95f, 0.15f)

    /** A surface being pressed. */
    @Stable
    fun <T> pressDown(): SpringSpec<T> = springOf(0.99f, 0.15f)

    /** A surface released from a press. Slower than the press, so the lift reads as a rebound. */
    @Stable
    fun <T> pressUp(): SpringSpec<T> = springOf(0.99f, 0.3f)

    /** A surface appearing with a scale. */
    @Stable
    fun <T> scaleIn(): SpringSpec<T> = springOf(0.6f, 0.35f)

    /** A surface disappearing with a scale. */
    @Stable
    fun <T> scaleOut(): SpringSpec<T> = springOf(0.75f, 0.2f)

    /**
     * The curve the bottom bar grades its own contents on — the indicator's fill, and a
     * destination's tint as the selection passes over it.
     */
    @Stable
    fun navContent(): FiniteAnimationSpec<Color> = tween(
        durationMillis = 150,
        easing = CubicBezierEasing(0.33f, 0f, 0.67f, 1f),
    )

    /** [navContent], for anything animated as a plain number. */
    @Stable
    fun navContentFloat(): FiniteAnimationSpec<Float> = tween(
        durationMillis = 150,
        easing = CubicBezierEasing(0.33f, 0f, 0.67f, 1f),
    )

    /**
     * Opacity is not sprung. A popup fades in over this, faster than its scale settles, so the
     * surface is already legible while it is still moving.
     */
    @Stable
    fun fadeIn(): FiniteAnimationSpec<Float> = tween(durationMillis = 80, easing = LinearEasing)

    /** Opacity on the way out, slower than [fadeIn] so a dismissal does not read as a flicker. */
    @Stable
    fun fadeOut(): FiniteAnimationSpec<Float> = tween(durationMillis = 150, easing = LinearEasing)

    /**
     * The spring an anchored menu morphs on.
     *
     * The spring an anchored menu's geometry runs on.
     *
     * It carries the width and the aspect ratio together, not a uniform scale. The panel's right
     * and top edges never move; it opens leftward and downward, wide and flat at first and only
     * then letting its height out. Overshoot is about a percent at roughly 290ms, and the tail is
     * settled by 460ms — what a person reads is "open in 200ms" with a soft finish behind it.
     */
    @Stable
    fun popupMorph(): SpringSpec<Float> = springOf(0.82f, 0.33f)

    /**
     * The curve a menu's opacity and its blur correction run on.
     *
     * Linear over 200ms, and deliberately not the spring: by the time the geometry is still
     * settling the panel is already fully drawn, so the overshoot is felt and not seen. A
     * dismissal is quicker, at 150ms, so it does not read as a lag behind the finger.
     *
     * @param entering Whether the menu is opening.
     */
    @Stable
    fun popupMorphFade(entering: Boolean): FiniteAnimationSpec<Float> = tween(durationMillis = if (entering) 200 else 150, easing = LinearEasing)

    /**
     * The curve a menu blurs its own contents on.
     *
     * Its own channel, and 200ms linear in both directions, unlike the opacity beside it. The
     * blur is what carries the opening for a menu with no icon to hand over to: the rows arrive
     * unreadable and sharpen, rather than simply fading up.
     */
    @Stable
    fun popupMorphBlur(): FiniteAnimationSpec<Float> = tween(durationMillis = 200, easing = LinearEasing)

    /**
     * The spring the geometry of a menu grown out of its own control runs on.
     *
     * This is the other of the two openings the source system has. It applies when the control
     * itself takes part: the panel is not revealed beside the control, it *is* the control, grown
     * from the control's own rectangle and corner radius out to the panel's. The control's
     * contents travel with it and dissolve out while the panel's dissolve in.
     *
     * @param entering Whether the menu is opening.
     */
    @Stable
    fun transformBounds(entering: Boolean): SpringSpec<Float> = if (entering) {
        transformSpring(0.8f, 0.4f)
    } else {
        transformSpring(0.8f, 0.28f)
    }

    /**
     * The spring the *centre* of that panel travels on.
     *
     * Deliberately not [transformBounds]. The panel reaches where it belongs sooner than it
     * finishes growing to size, and that difference is what stops the travel reading as a slide.
     *
     * @param entering Whether the menu is opening.
     */
    @Stable
    fun transformCenter(entering: Boolean): SpringSpec<Float> = if (entering) {
        transformSpring(0.8f, 0.25f)
    } else {
        transformSpring(0.8f, 0.4f)
    }

    /**
     * The ramp the control's own contents dissolve out on as the panel takes over.
     *
     * The two dissolves are the same 80ms ramp with 50ms between them, and which of the two waits
     * swaps with the direction: opening, the control goes first and the panel's contents follow;
     * closing, the panel's contents go first.
     *
     * @param entering Whether the menu is opening.
     */
    @Stable
    fun transformIconMaterial(entering: Boolean): FiniteAnimationSpec<Float> = tween(
        durationMillis = TRANSFORM_MATERIAL_MS,
        delayMillis = if (entering) 0 else TRANSFORM_MATERIAL_DELAY_MS,
        easing = LinearEasing,
    )

    /** The ramp the panel's contents dissolve in on. The other half of [transformIconMaterial]. */
    @Stable
    fun transformContentMaterial(entering: Boolean): FiniteAnimationSpec<Float> = tween(
        durationMillis = TRANSFORM_MATERIAL_MS,
        delayMillis = if (entering) TRANSFORM_MATERIAL_DELAY_MS else 0,
        easing = LinearEasing,
    )

    /** How long either half of that dissolve takes, in milliseconds. */
    const val TRANSFORM_MATERIAL_MS: Int = 80

    /** How long the second half of the dissolve waits for the first, in milliseconds. */
    const val TRANSFORM_MATERIAL_DELAY_MS: Int = 50

    /**
     * How far the two halves of that dissolve blur, in pixels.
     *
     * Pixels, not dp, because the source hands this straight to the platform's blur and never
     * scales it by density. A denser screen therefore gets a proportionally softer dissolve, and
     * that is what the source looks like.
     */
    const val TRANSFORM_BLUR_PX: Float = 50f

    /** Folme `ValueTarget`: `0.002` minimum visible change times its `0.75` multiplier. */
    internal const val TRANSFORM_VISIBILITY_THRESHOLD: Float = 0.0015f

    /** Width a menu opens from, as a fraction of the width it settles at. */
    const val POPUP_START_WIDTH: Float = 0.15f

    /** Its height at that moment, as a fraction of its own width. Far flatter than it ends up. */
    const val POPUP_START_RATIO: Float = 0.2f

    /** Corner radius a menu opens from, in dp. It relaxes to the panel's own. */
    const val POPUP_START_CORNER_DP: Float = 4f

    /** How far a menu blurs its own contents at the start, in pixels rather than dp. */
    const val POPUP_MORPH_BLUR_PX: Float = 40f

    /**
     * The spring the *size* of a spinner's dropdown runs on.
     *
     * The third of the source system's openings, and the one a settings row's list of choices
     * gets. Unlike the other two it does not hold any edge still: the panel's size and its centre
     * run on two springs of different speeds, so the capsule reaches where it belongs before it has
     * finished growing and the panel's far edges pull in and rebound. That difference is the whole
     * of the arc.
     *
     * Opening, the centre is the quicker of the two. Closing, the two swap, so the panel collapses
     * to a capsule where it stands and only then travels back.
     *
     * @param entering Whether the dropdown is opening.
     */
    @Stable
    fun <T> arcBounds(entering: Boolean): SpringSpec<T> = if (entering) springOf(0.8f, 0.35f) else springOf(0.8f, 0.22f)

    /** The spring the *centre* of that dropdown travels on. The other half of [arcBounds]. */
    @Stable
    fun <T> arcPosition(entering: Boolean): SpringSpec<T> = if (entering) springOf(0.8f, 0.22f) else springOf(0.8f, 0.35f)

    /**
     * The ramp a spinner's dropdown fades on.
     *
     * 50ms in and 200ms out. The opening is brisk enough that the panel is already legible by the
     * time it becomes visible at all, and the closing is slow enough to carry the blur with it.
     *
     * @param entering Whether the dropdown is opening.
     */
    @Stable
    fun arcFade(entering: Boolean): FiniteAnimationSpec<Float> = tween(durationMillis = if (entering) 50 else 200, easing = LinearEasing)

    /**
     * The ramp that dropdown blurs itself on as it leaves.
     *
     * One-sided. The source blurs the panel only on the way out, so the opening has none of it at
     * all and the value snaps sharp rather than arriving out of focus.
     *
     * @param entering Whether the dropdown is opening.
     */
    @Stable
    fun arcBlur(entering: Boolean): FiniteAnimationSpec<Float> = if (entering) snap() else tween(durationMillis = 200, easing = LinearEasing)

    /** Width that dropdown opens from, as a fraction of the width it settles at. */
    const val ARC_START_WIDTH: Float = 0.69f

    /** Its height at that moment, as a fraction of its own width. */
    const val ARC_START_RATIO: Float = 0.2f

    /** How far that dropdown blurs its own contents on the way out, in dp. */
    const val ARC_EXIT_BLUR_DP: Float = 30f

    /**
     * How far the size has to travel before the panel is shown at all.
     *
     * Roughly 30ms in. By then the opacity is already at about 60%, so what appears is a legible
     * capsule rather than a tiny transparent speck.
     */
    const val ARC_VISIBLE_FRACTION: Float = 0.1f

    /** The scale a popup grows from, measured at its anchor corner. */
    const val POPUP_ENTER_SCALE: Float = 0.2f

    /** The scale a popup shrinks back to. It leaves larger than it arrived. */
    const val POPUP_EXIT_SCALE: Float = 0.5f

    /** The floor on the pressed scale, whatever the surface measures. */
    const val PRESS_SCALE_MIN: Float = 0.9f

    /** How much a press takes off a surface, across the whole of its smaller side, in dp. */
    const val PRESS_INSET_DP: Float = 10f

    /** The scale the bottom bar shrinks to as it leaves. */
    const val NAV_HIDE_SCALE: Float = 0.6f

    /** How far the bottom bar blurs itself at the end of its exit, in dp. */
    const val NAV_HIDE_BLUR_DP: Float = 18f

    /** How long the bottom bar waits before it comes back, in milliseconds. */
    const val NAV_SHOW_DELAY_MS: Long = 100
}
