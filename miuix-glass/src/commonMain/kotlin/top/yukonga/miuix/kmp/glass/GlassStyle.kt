// Copyright 2026, compose-miuix-ui contributors
// SPDX-License-Identifier: Apache-2.0

package top.yukonga.miuix.kmp.glass

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

/**
 * A complete glass material description.
 *
 * @property blend Luminance curve, saturation, brightness and shadow compression applied to the
 *   refracted backdrop.
 * @property inner Tint, inner glow and the final gamma of the glass body.
 * @property edge The rim geometry — how wide the lit band is and how the surface curves inside it.
 * @property reflect How much of the reflection ray mixes into the refraction ray.
 * @property light The two directional lights that shade the rim.
 * @property refract The index of refraction of the glass body.
 * @property background Treatment of the wide-blur backdrop that supplies the low-frequency colour.
 * @property blur The two backdrop blur radii the material samples.
 */
@Immutable
data class GlassStyle(
    val blend: GlassBlend,
    val inner: GlassInner,
    val edge: GlassEdge,
    val reflect: GlassReflect,
    val light: GlassLight,
    val refract: GlassRefract,
    val background: GlassBackground,
    val blur: GlassBlur,
)

/**
 * Colour treatment of the refracted backdrop.
 *
 * @property curveA Cubic term of the luminance curve.
 * @property curveB Quadratic term of the luminance curve.
 * @property curveC Linear term of the luminance curve.
 * @property curveD Constant term of the luminance curve.
 * @property amount How far the curve result mixes in. 0 leaves luminance unchanged.
 * @property saturation Saturation multiplier. 1 leaves saturation unchanged.
 * @property brightness Additive brightness. 0 leaves brightness unchanged.
 * @property darker Strength of the deep-shadow compression toward a near-black violet.
 * @property darkerStart Luminance at which the compression starts.
 * @property darkerEnd Luminance at which the compression reaches [darker].
 */
@Immutable
data class GlassBlend(
    val curveA: Float,
    val curveB: Float,
    val curveC: Float,
    val curveD: Float,
    val amount: Float,
    val saturation: Float,
    val brightness: Float,
    val darker: Float,
    val darkerStart: Float,
    val darkerEnd: Float,
)

/**
 * The glass body itself.
 *
 * @property bottom Strength of the additive glow along the bottom edge.
 * @property tint The colour of the glass. Its own alpha is ignored — [tintStrength] carries it,
 *   because the source values go above 1.
 * @property tintStrength How far the backdrop colour moves toward [tint]. Values above 1 push past
 *   the tint and are legal.
 * @property colorWhite How far the wide-blur backdrop moves toward white before it tints the glass.
 * @property colorMix How far the finished glass colour replaces the refracted backdrop.
 * @property colorPow Final gamma applied to the material. Above 1 darkens, below 1 lifts.
 * @property alpha Base opacity of the material.
 */
@Immutable
data class GlassInner(
    val bottom: Float,
    val tint: Color,
    val tintStrength: Float,
    val colorWhite: Float,
    val colorMix: Float,
    val colorPow: Float,
    val alpha: Float,
)

/**
 * The rim geometry, in source pixels.
 *
 * @property width Width of the rim band.
 * @property pow Curvature exponent of the rim profile. 1 is a soft dome; higher values keep the
 *   surface flat for longer and then turn sharply at the silhouette.
 * @property thickness Glass thickness. It scales how far the refraction ray moves.
 * @property reflectOffset How far the reflection ray moves. Large values pull in distant colour.
 */
@Immutable
data class GlassEdge(
    val width: Float,
    val pow: Float,
    val thickness: Float,
    val reflectOffset: Float,
)

/**
 * The reflection ray.
 *
 * @property lighten Additive strength of the reflection on top of the refraction.
 * @property strength How far the reflection mixes into the refraction at the silhouette.
 */
@Immutable
data class GlassReflect(
    val lighten: Float,
    val strength: Float,
)

/**
 * The two directional lights that shade the rim. The second light points the opposite way in X
 * and Y, so one preset lights both sides of the shape.
 *
 * @property directionX X component of the main light direction.
 * @property directionY Y component of the main light direction.
 * @property directionZ Z component of the main light direction.
 * @property intensity Strength of the main light.
 * @property oppositeIntensity Strength of the opposite light.
 * @property angleRange Angular falloff range as a multiple of pi.
 */
@Immutable
data class GlassLight(
    val directionX: Float,
    val directionY: Float,
    val directionZ: Float,
    val intensity: Float,
    val oppositeIntensity: Float,
    val angleRange: Float,
)

/**
 *
 * @property ior Index of refraction. 1 bends nothing; 1.5 is window glass.
 */
@Immutable
data class GlassRefract(
    val ior: Float,
)

/**
 * Treatment of the wide-blur backdrop, which supplies the low-frequency colour of the glass.
 *
 * @property saturation Saturation multiplier of the wide-blur backdrop. 0 makes it grey.
 * @property brightness Additive brightness of the wide-blur backdrop.
 * @property burn Exponent of the colour-burn ratio. Values below 0.5 are raised to 0.5.
 * @property unShade How far the material collapses to a flat [GlassInner.tint]. 1 removes the
 *   material entirely and leaves a plain tinted shape.
 */
@Immutable
data class GlassBackground(
    val saturation: Float,
    val brightness: Float,
    val burn: Float,
    val unShade: Float,
)

/**
 * The two backdrop blur radii, in pixels at [GlassDefaults.SourceDensity].
 *
 * @property small Radius of the refraction source. It keeps structure, so the rim shows a
 *   recognisable bent image of the backdrop.
 * @property big Radius of the colour source. It only supplies a low-frequency tint.
 */
@Immutable
data class GlassBlur(
    val small: Float,
    val big: Float,
)
