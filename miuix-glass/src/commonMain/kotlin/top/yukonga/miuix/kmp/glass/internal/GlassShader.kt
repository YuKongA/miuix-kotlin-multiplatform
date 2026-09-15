// Copyright 2026, compose-miuix-ui contributors
// SPDX-License-Identifier: Apache-2.0

package top.yukonga.miuix.kmp.glass.internal

/** Cache key for the compiled glass shader. */
internal const val GLASS_SHADER_KEY = "MiuixGlass"

/** Cache key for the compiled silhouette mask shader. */
internal const val GLASS_MASK_SHADER_KEY = "MiuixGlassMask"

/**
 * The silhouette, shared by the material shader and the mask shader so the two can never disagree
 * about where the surface ends.
 */
private val GLASS_SDF_SOURCE: String = """
uniform float2 in_size;         // component size
uniform float4 in_radii;        // topLeft, topRight, bottomRight, bottomLeft
uniform float in_smoothing;     // 0 = circular corner, 1 = continuous corner

float roundedBoxSdf(float2 p, float2 b, float r) {
    float2 d = p - b + r;
    return min(max(d.x, d.y), 0.0) + length(max(d, float2(0.0))) - r;
}

float supercircleSdf(float2 off, float tile) {
    float2 q = max(float2(0.0), (float2(tile) + off) / tile);
    float hi = max(q.x, q.y);
    float ratio = (hi == 0.0) ? 0.0 : clamp(min(q.x, q.y) / hi, 0.0, 1.0);
    float fit = (((($FIT_4 * ratio + $FIT_3) * ratio + $FIT_2) * ratio + $FIT_1) * ratio + $FIT_0);
    float len = length(q);
    float distBase = (len + 1.0) - 1.0 / (1.0 - ratio * ratio * clamp(len, 0.0, 1.0) * fit);
    return min(max(tile + off.x, tile + off.y), 0.0) + tile * (distBase - 1.0);
}

float pickRadius(float2 local, float2 b) {
    float top = (local.x < b.x) ? in_radii.x : in_radii.y;
    float bottom = (local.x < b.x) ? in_radii.w : in_radii.z;
    return min((local.y < b.y) ? top : bottom, min(b.x, b.y));
}

float sdfShape(float2 p, float2 b, float r) {
    float box = roundedBoxSdf(p, b, r);
    float minHalf = min(b.x, b.y);
    if (in_smoothing <= 0.001 || r < $MIN_SUPERCIRCLE_RADIUS || (minHalf - r) <= 1.0) {
        return box;
    }
    float tile = min($TILE_SCALE * r, minHalf);
    return mix(box, supercircleSdf(p - b, tile), in_smoothing);
}

// Distance to the silhouette from a point in component space. Negative inside.
float silhouetteSdf(float2 local) {
    float2 b = in_size * 0.5;
    return sdfShape(abs(local - b), b, pickRadius(local, b));
}
"""

/** The rim: how the surface curves near the silhouette, and how the two directional lights catch it. */
private val GLASS_RIM_SOURCE: String = """
const float3 kLumaWeights = float3(0.2126, 0.7152, 0.0722);

float luma(float3 color) {
    return dot(color, kLumaWeights);
}

float smooth5Map(float t) {
    t = clamp(mix(0.5, 1.0, t), 0.0, 1.0);
    t = t * t * t * (t * (t * 6.0 - 15.0) + 10.0);
    return (t - 0.5) * 2.0;
}

// Depth 0 at the silhouette, 1 once the surface is flat. `p` shapes how fast it gets there.
float edgeCurve(float x, float p) {
    if (x >= 0.85) return 1.0;
    float c = smooth5Map(sqrt(clamp(x, 0.0, 1.0)));
    return 1.0 - pow(1.0 - c, p);
}

// Slope of `edgeCurve` in `x`.
//
// The source effect differentiates the rim profile with a one-*display*-pixel step, which against
// a sixty-pixel band is as good as exact. The material pass runs on the downscaled backdrop layer,
// where the smallest honest step is several display pixels — a sixth of the band — and a step that
// coarse does not measure the turn, it averages it away: the tilt it reports far inside the band
// is seven times the real one, and the rim reads as a dome instead of an edge. Differentiating the
// curve in closed form removes the step, and with it the resolution the answer depends on.
float edgeCurveSlope(float x, float p) {
    if (x >= 0.85) return 0.0;
    float t = sqrt(clamp(x, 0.000001, 1.0));
    float u = 0.5 + 0.5 * t;
    float um = u - 1.0;
    float c = smooth5Map(t);
    // d(smooth5Map(sqrt x))/dx, the quintic's derivative carried through the square root.
    float slope = 15.0 * u * u * um * um / t;
    return p * pow(max(1.0 - c, 0.0), p - 1.0) * slope;
}

// Direction the distance field grows in: a unit vector pointing out of the shape. The unshaped
// field is locally a plane, so a half-pixel difference reads it exactly.
float2 sdfGradient(float2 local) {
    float dx = silhouetteSdf(local + float2(0.5, 0.0)) - silhouetteSdf(local - float2(0.5, 0.0));
    float dy = silhouetteSdf(local + float2(0.0, 0.5)) - silhouetteSdf(local - float2(0.0, 0.5));
    float2 gradient = float2(dx, dy);
    float length2d = length(gradient);
    return (length2d < 0.0001) ? float2(0.0) : gradient / length2d;
}

// Lifts a colour toward white without clipping: the gain is applied around a luminance-dependent
// pivot, so bright pixels move less than dark ones.
float3 liftToward(float3 color, float amount) {
    float v = luma(color);
    float w = smoothstep(0.0, 0.5, v);
    float k = mix(1.0 - v, v, w);
    float gain = 1.0 + smoothstep(0.0, 1.0, amount) * mix(0.75, 0.4, w);
    return (color + k) * gain - k;
}

float dynamicAdd(float3 color) {
    float toWhite = smoothstep(0.2, 1.0, distance(float3(1.0), color));
    return mix(0.8, mix(0.2, 1.0, toWhite), luma(color));
}

// acos is not worth its cost here; this is the usual sqrt-based polynomial fit.
float lightFalloff(float3 normal, float3 direction, float intensity, float angleRange) {
    float dp = dot(normal, direction);
    float d = clamp(dp, -1.0, 1.0);
    float u = 1.0 - d;
    float positive = sqrt(u) * (u * (u * 0.04587603 + 0.10884186) + 1.41502593);
    float angle = (d < 0.0) ? (3.14159265 - positive) : positive;
    return max(dp, 0.0) * max(intensity * (1.0 - angle / max(angleRange, 0.0001)), 0.0);
}

// How much light the rim carries where neither directional light reaches. Calibrated so the sides
// between the two lit arcs come out about a dozen levels above the surface, as the source does.
const float kEdgeAmbient = 0.24;

// How much of the rim band the light rides, measured from the silhouette inward. The band itself
// is what bends the refraction ray; the highlight the source draws on it is a hairline.
const float kRimBandFraction = 0.33;

// How far the two lights lift a surface of `color`, in `[0, 1]`. The second light points the
// opposite way in x and y, so one direction lights both sides of the shape.
float rimLighten(float3 normal, float4 lightDir, float2 amounts, float3 color) {
    float3 lit = float3(normal.x, -normal.y, normal.z);
    float primary = lightFalloff(lit, lightDir.xyz, amounts.x, lightDir.w);
    float opposite = lightFalloff(lit, lightDir.xyz * float3(-1.0, -1.0, 1.0), amounts.y, lightDir.w);
    // Two opposed lights alone leave two lit arcs and two dark ones. The source rim is continuous:
    // measured round a 44dp button, the two arcs the lights own come out about thirty levels above
    // the surface and the sides between them still ten to fifteen — lit, not dark. An ambient term
    // proportional to how far the rim has turned away from the viewer is what joins them up, and
    // the compositor's own shader carries one under that name.
    float edgeFactor = clamp(length(lit.xy), 0.0, 1.0);
    float raw = clamp(primary + opposite + kEdgeAmbient * edgeFactor, 0.0, 1.0) * dynamicAdd(color);
    raw = pow(clamp(raw, 0.0, 1.0), 0.85);
    // A soft knee, tighter over a bright backdrop: full light lifts the rim about half way, never
    // to white. Without it the whole edge band clips and the hairline has nothing left to sit on.
    float knee = mix(1.0, 0.7, smoothstep(0.0, 0.5, luma(color)));
    return raw / (raw + knee);
}

// The steepest the bevel is allowed to get, as a rise over a run.
//
// The closed-form slope runs to the thousands at the silhouette, which stands the normal on end
// and hands both lights everything they can take. The source system never sees that: it reads its
// normal out of a cached corner image, and an image has a resolution, so the tilt it reports tops
// out. Measured against its own rim — peak twenty-nine levels above the surface, about two pixels
// wide — the cap it behaves as though it has is a little under sixty degrees.
const float kMaxBevelSlope = 1.7;
"""

/** The rim light, at full resolution. */
internal val GLASS_RIM_SHADER: String = """
$GLASS_SDF_SOURCE
$GLASS_RIM_SOURCE
uniform float4 in_rimEdge;      // edge width, edge pow, surface opacity, unused
uniform float4 in_lightDir;     // direction xyz, angle range * pi
uniform float4 in_lightAmt;     // intensity, opposite intensity, unused, unused
uniform float4 in_surface;      // the material's own colour, standing in for what is underneath

half4 main(float2 coord) {
    float d = silhouetteSdf(coord);
    // The light rides the outer third of the rim, not all of it. The whole band is what bends the
    // refraction ray — sixty source pixels of it — but the source's own highlight is a hairline
    // two or three pixels wide, and a light spread over the full band comes out five times that
    // and reads as a bevel rather than an edge.
    float edge = max(in_rimEdge.x * kRimBandFraction, 1.0);
    // Past 85% of the band the surface is flat and faces away from both lights.
    if (d > 0.5 || -d >= edge * 0.85) return half4(0.0);

    float2 gradient = sdfGradient(coord);
    if (length(gradient) < 0.5) return half4(0.0);

    float depth = clamp(-d / edge, 0.0, 1.0);
    float slope = min(edgeCurveSlope(depth, in_rimEdge.y), kMaxBevelSlope);
    float3 normal = normalize(float3(gradient * slope, 1.0));
    float lighten = rimLighten(normal, in_lightDir, in_lightAmt.xy, in_surface.rgb);
    float3 added = max(liftToward(in_surface.rgb, lighten) - in_surface.rgb, float3(0.0));

    float coverage = clamp(0.5 - d, 0.0, 1.0);
    float3 light = added * (in_rimEdge.z * coverage);
    // Carry an alpha equal to the light, not zero. A shader returns a *premultiplied* colour, and
    // a colour whose channels sit above its own alpha is not one — it is clamped away, which is
    // exactly what silently happened to this pass and to the bloom stroke: both asked for light
    // with no opacity, and both drew nothing at all. Under Plus the alpha adds too, but the
    // surface under the rim is already opaque, so it has nowhere to go.
    return half4(half3(light), half(clamp(max(max(light.r, light.g), light.b), 0.0, 1.0)));
}
"""

/** Cuts the surface to its silhouette, at full resolution. */
internal val GLASS_MASK_SHADER: String = """
$GLASS_SDF_SOURCE
half4 main(float2 coord) {
    // One pixel of coverage either side of the boundary is all the anti-aliasing an edge needs.
    return half4(half(clamp(0.5 - silhouetteSdf(coord), 0.0, 1.0)));
}
"""

/** The glass material, as one AGSL/SkSL pass over the blurred backdrop. */
internal val GLASS_SHADER: String = """
uniform shader child;

uniform float2 in_pad;          // component origin inside the padded layer
uniform float2 in_maxCoord;     // sampling clamp of the padded layer
uniform float4 in_alphaEdge;    // alpha, edge width, thickness, reflect offset
uniform float4 in_iorRefl;      // ior, reflect strength, reflect lighten, colour gamma
uniform float4 in_tint;         // tint rgb, tint strength
uniform float4 in_whiteMixBg;   // colour white, colour mix, backdrop saturation, backdrop brightness
uniform float4 in_darker;       // darker start, darker end, darker, inner bottom
uniform float4 in_lightDir;     // direction xyz, angle range * pi
uniform float4 in_lightAmt;     // intensity, opposite intensity, luminance amount, overspill
uniform float4 in_lumCurve;     // cubic coefficients A, B, C, D
uniform float4 in_satBri;       // saturation, brightness, burn, unshade
uniform float4 in_edgePow;      // edge pow, wide-sample radius

const float3 kCurveWeights = float3(0.2125, 0.7153, 0.0721);
const float3 kShadowTint = float3(0.07874, 0.02848, 0.09278);

// ------------------------------------------------- silhouette and edge profile
$GLASS_SDF_SOURCE
$GLASS_RIM_SOURCE
// ------------------------------------------------------------------ sampling

float4 tapLayer(float2 local) {
    return float4(child.eval(clamp(local + in_pad, float2(0.5), in_maxCoord)));
}

// The refraction and reflection rays reach past the silhouette, and the recorded layer only
// carries backdrop for as far as the blur padding. A tap that lands beyond it returns transparent
// black, which would ring the whole shape in a dark halo, so fall back to the nearest tap that is
// inside the surface itself.
float4 sampleBackdrop(float2 local) {
    float4 col = tapLayer(local);
    if (col.a < 0.02) {
        col = tapLayer(clamp(local, float2(0.5), in_size - 0.5));
    }
    if (col.a > 0.004) {
        col = float4(col.rgb / col.a, 1.0);
    }
    float t = smoothstep(in_darker.x, in_darker.y, luma(col.rgb)) * in_darker.z;
    col.rgb = mix(col.rgb, kShadowTint, t);
    return col;
}

// Stands in for the second, far wider blur the source effect samples.
//
// The taps sit on a Vogel spiral — radius grows as the square root of the index, and each step
// turns by the golden angle — which covers the disc evenly instead of leaving the middle empty.
// That evenness is the whole point: a sparse ring makes each tap a hard threshold, and a bright
// object behind the surface crosses one tap at a time, painting the rectangles a ring pattern
// leaves behind. Twenty-five taps put neighbouring samples closer together than the blur already
// on the layer, so the result is smooth wherever it is read.
//
// Taps are averaged by coverage, not by count: the padded layer runs out before the widest taps
// do, and weighting by alpha lets a tap that lands past the recorded backdrop contribute nothing
// rather than pull the colour toward black.
float4 sampleWide(float2 local) {
    float2 outer = in_edgePow.yz;
    if (outer.x <= 0.5 || outer.y <= 0.5) return sampleBackdrop(local);
    // The source effect's colour texture is blurred at a radius far larger than any surface it
    // sits on, so across a bar or a button it is very nearly one colour. `in_edgePow.w` says how
    // completely: at 1 the disc is centred on the surface and every pixel reads the same average,
    // which is what stops a photograph behind the glass from mottling it. Only a surface wider
    // than the blur reach keeps the sample under the pixel that reads it.
    float2 origin = mix(local, in_size * 0.5, in_edgePow.w);
    float3 sum = float3(0.0);
    float coverage = 0.0;
    for (int i = 0; i < 25; i++) {
        float index = float(i);
        float radius = sqrt((index + 0.5) * 0.04);
        float angle = index * 2.39996323;
        float4 tap = tapLayer(origin + float2(cos(angle), sin(angle)) * radius * outer);
        sum += tap.rgb;
        coverage += tap.a;
    }
    if (coverage < 0.0001) return sampleBackdrop(local);
    float3 color = sum / coverage;
    float t = smoothstep(in_darker.x, in_darker.y, luma(color)) * in_darker.z;
    return float4(mix(color, kShadowTint, t), 1.0);
}

// ------------------------------------------------------------------- grading

float4 adjustColor(float4 color, float saturation, float brightness) {
    float l = dot(color.rgb, kCurveWeights);
    return float4(mix(float3(l), color.rgb, saturation) + float3(brightness * color.a), color.a);
}

float4 luminanceCurve(float4 color) {
    float a = max(color.a, 0.0001);
    float3 straight = color.rgb / a;
    float l = clamp(dot(straight, kCurveWeights), 0.0, 1.0);
    float adjusted = ((in_lumCurve.x * l + in_lumCurve.y) * l + in_lumCurve.z) * l + in_lumCurve.w;
    adjusted = clamp(adjusted, 0.0, 1.0);
    float scale = adjusted / max(l, 0.01) * smoothstep(0.0, 0.1, l);
    return float4(mix(straight, straight * scale, in_lightAmt.z) * a, color.a);
}

float4 processColor(float4 color) {
    color = luminanceCurve(color);
    color.rgb = adjustColor(color, in_satBri.x, in_satBri.y).rgb;
    return color;
}

float4 processGlassColor(float4 material, float2 local) {
    float4 wide = adjustColor(sampleWide(local), in_whiteMixBg.z, in_whiteMixBg.w);
    wide.rgb = mix(wide.rgb, float3(1.0), in_whiteMixBg.x);

    float lumin = clamp(luma(wide.rgb), 0.0, 1.0);
    float burn = pow(lumin, max(in_satBri.z, 0.5)) - 0.5;
    float colorRatio = 0.8 * mix(lumin, 1.0, (1.587 * burn * burn * burn) + 0.5);

    float3 ratio = mix(float3(1.0), wide.rgb, colorRatio);
    float mean = (ratio.r + ratio.g + ratio.b) * 0.33333333;
    // Pull near-grey backdrops all the way to grey so the glass never picks up a false cast.
    ratio = mix(float3(mean), ratio, smoothstep(0.0, 0.4, distance(wide.rgb, float3(mean))));
    ratio = mix(ratio, in_tint.rgb, in_tint.a);

    material.rgb *= ratio;
    material.rgb = mix(material.rgb, ratio, in_whiteMixBg.y * colorRatio);
    return material;
}

float3 addLight(float3 color, float3 lightColor, float strength) {
    float toWhite = smoothstep(0.2, 1.0, distance(float3(1.0), color));
    return color + lightColor * strength * mix(0.3, 1.0, toWhite);
}

// ---------------------------------------------------------------------- main

// The material at one point, given how deep into the rim it is and which way the rim faces.
//
// `d` is passed in rather than measured: the caller walks it across a pixel, and over that
// distance the distance field is a plane, so stepping the value is both exact and free.
float4 shadeMaterial(float2 local, float d, float2 gradient, float edge) {
    float depth = clamp(-d / edge, 0.0, 1.0);
    float shaped = edgeCurve(depth, in_edgePow.x);
    // The reflection and the added light ride the same outer third of the band the rim light does.
    // Spread across the whole band they come out as a soft halo several times wider than the edge
    // the source draws, because this pass cannot resolve where the turn actually is.
    float nmlZ = edgeCurve(min(depth / kRimBandFraction, 1.0), 1.0);

    bool isFlat = shaped >= 1.0;
    float3 normal = isFlat
        ? float3(0.0, 0.0, 1.0)
        : normalize(float3(gradient * edgeCurveSlope(depth, in_edgePow.x), 1.0));

    float2 refractUv = local;
    if (!isFlat) {
        float3 bent = refract(float3(0.0, 0.0, -1.0), normal, 1.0 / max(in_iorRefl.x, 1.0));
        float thickness = in_alphaEdge.z;
        refractUv += bent.xy * mix((thickness - edge) * 2.0, thickness * 2.0, shaped);
    }
    float4 material = sampleBackdrop(refractUv);

    float rimZ = 1.0 - nmlZ;
    float4 mirrored = float4(0.0);
    if (rimZ > 0.000001) {
        float3 bounced = reflect(float3(0.0, 0.0, -1.0), normal);
        mirrored = sampleBackdrop(local + bounced.xy * (in_alphaEdge.w * (1.0 - shaped)));
        material = mix(material, mirrored, clamp(rimZ * in_iorRefl.y, 0.0, 1.0));
    }
    material.rgb = addLight(material.rgb, mirrored.rgb, (1.0 - shaped) * in_iorRefl.z);

    material = processColor(material);
    material = processGlassColor(material, local);

    // Additive glow along the bottom edge.
    float bottom = smoothstep(1.0, 0.0, 1.0 - local.y / max(in_size.y, 1.0));
    material.rgb += bottom * bottom * in_darker.w;


    material = pow(max(material, float4(0.0)), float4(in_iorRefl.w));
    material.rgb = mix(clamp(material.rgb, 0.0, 1.0), in_tint.rgb, in_satBri.w);
    return material;
}

// ---------------------------------------------------------------------- main

half4 main(float2 coord) {
    float2 local = coord - in_pad;
    float d = silhouetteSdf(local);
    // Overspill the silhouette: the mask pass trims the edge at full resolution, so this one only
    // has to reach past it.
    if (d >= in_lightAmt.w) return half4(0.0);

    // A fully unshaded style is a flat tinted shape; skip the whole material.
    if ((1.0 - in_satBri.w) <= 0.000001) {
        float flatAlpha = in_alphaEdge.x;
        return half4(half3(in_tint.rgb * flatAlpha), half(flatAlpha));
    }

    float edge = max(in_alphaEdge.y, 1.0);
    float2 gradient = sdfGradient(local);
    float4 material;

    // Now that the normal is exact, the lit part of the rim is a few display pixels wide — less
    // than one pixel of this layer. Read once per pixel it would alias into a staircase, so inside
    // the band the material is integrated across the pixel instead. One dimension is enough: the
    // rim varies along the distance field's gradient and is constant across it.
    if (-d < edge && length(gradient) > 0.5) {
        material = shadeMaterial(local - gradient * 0.375, d - 0.375, gradient, edge) * 0.25;
        material += shadeMaterial(local, d, gradient, edge) * 0.5;
        material += shadeMaterial(local + gradient * 0.375, d + 0.375, gradient, edge) * 0.25;
    } else {
        material = shadeMaterial(local, d, gradient, edge);
    }

    float alpha = material.a * in_alphaEdge.x;
    return half4(half3(material.rgb * alpha), half(alpha));
}
"""

/** Cache key for the compiled bloom stroke shader. */
internal const val GLASS_STROKE_SHADER_KEY = "MiuixGlassStroke"

/** The bloom stroke: the lit edge that traces the silhouette. */
internal val GLASS_STROKE_SHADER: String = """
$GLASS_SDF_SOURCE
uniform float2 in_halfViewFloor; // half size, floored, so the fold lands on a pixel boundary
uniform float2 in_strokeBand;    // stroke width, inner blur radius
uniform float4 in_strokeColor;   // stroke rgb, stroke opacity
uniform float in_strokeAlpha;    // surface opacity
uniform float4 in_light1;        // direction xyz, intensity
uniform float4 in_light1Color;   // colour rgb, unused
uniform float4 in_light2;        // direction xyz, intensity
uniform float4 in_light2Color;   // colour rgb, unused

// The bevel normal. Folded into one quadrant, built there, then unfolded by the sign of the
// offset — the rim is symmetric, so only a quarter of it has to be reasoned about.
float3 rimNormal(float2 coord, float2 half2d, float sdf, float radius, float inner) {
    float2 offset = coord - in_halfViewFloor;
    float2 folded = abs(offset);
    // Lift the point off the surface. Depth is measured in the rim band, height in pixels.
    float depth = smoothstep(-inner, 0.0, sdf);
    float3 point = float3(folded, -sqrt(max(inner * inner - depth * depth, 0.0)));

    float bevel = max(radius, inner);
    float2 reference = half2d - bevel;
    reference.x = min(reference.x, folded.x);
    reference.y = min(reference.y, folded.y);
    float2 toPoint = point.xy - reference;
    float len = length(toPoint);
    reference += ((len > 0.0001) ? toPoint / len : float2(0.0, 1.0)) * (bevel - inner);

    // Inside the reference the surface is flat and faces straight away from the lights.
    if (folded.x < reference.x || folded.y < reference.y) return float3(0.0, 0.0, -1.0);

    float3 normal = normalize(point - float3(reference, 0.0));
    normal.xy *= sign(offset);
    return normal;
}

// MiBloomStrokeFilter uses fixed vertical falloff axes, independent of the light positions.
// Keep the falloff signed: the native shader clamps the product, not either dot separately.
float3 rimLight(float3 normal, float axisY, float4 light, float3 color) {
    float falloff = axisY * normal.y;
    float lit = clamp(dot(normal, light.xyz) * falloff, 0.0, 1.0);
    return color * (lit * lit * light.w);
}

half4 main(float2 coord) {
    float2 half2d = in_size * 0.5;
    float2 folded = abs(coord - half2d);
    float radius = pickRadius(coord, half2d);
    float inner = max(in_strokeBand.y, 0.5);
    float bevel = max(radius, inner);

    // The interior has no rim to light, and it is most of the surface.
    if (folded.x < half2d.x - bevel && folded.y < half2d.y - bevel) return half4(0.0);

    float sdf = silhouetteSdf(coord);
    // A near-step band hugging the outer edge, squared the way the source composites it twice.
    float width = max(in_strokeBand.x, 0.75);
    float band = smoothstep(-width, -width + 1.0, sdf);
    float3 rgb = in_strokeColor.rgb * (in_strokeColor.a * band * band);

    float3 normal = rimNormal(coord, half2d, sdf, radius, inner);
    rgb += rimLight(normal, -1.0, in_light1, in_light1Color.rgb);
    rgb += rimLight(normal, 1.0, in_light2, in_light2Color.rgb);

    // Light only: alpha stays at zero so a transparent surface never gains opacity from the rim.
    float coverage = clamp(0.5 - sdf, 0.0, 1.0);
    float3 light = rgb * (in_strokeAlpha * coverage);
    // Premultiplied, so the alpha has to match the light — see the rim pass for why zero loses it.
    return half4(half3(light), half(clamp(max(max(light.r, light.g), light.b), 0.0, 1.0)));
}
"""

/** Cache key for the compiled shadow shader. */
internal const val GLASS_SHADOW_SHADER_KEY = "MiuixGlassShadow"

/** The shadow a surface casts, drawn from the same distance field as the surface itself. */
internal val GLASS_SHADOW_SHADER: String = """
$GLASS_SDF_SOURCE
uniform float2 in_shadowOffset;  // displacement in pixels
uniform float2 in_shadowShape;   // reach, dispersion
uniform float4 in_shadowColor;   // rgb, strength

half4 main(float2 coord) {
    float d = silhouetteSdf(coord - in_shadowOffset);
    float reach = max(in_shadowShape.x, 0.5);
    float t = clamp(1.0 - d / reach, 0.0, 1.0);
    // Dispersion reshapes the falloff: below a half it hugs the edge, above it spreads and thins.
    float falloff = pow(t, 1.0 / max(in_shadowShape.y, 0.05));
    float a = falloff * in_shadowColor.a;
    return half4(half3(in_shadowColor.rgb * a), half(a));
}
"""

internal const val GLASS_COLOR_BLEND_SHADER_KEY = "MiuixGlassColorBlend"

/** The colour treatment a glass surface lays over its blurred backdrop. */
internal val GLASS_COLOR_BLEND_SHADER: String = """
uniform shader child;

uniform float4 in_blend0;     // layer 0 colour, straight; w = amount
uniform float4 in_blend1;     // layer 1
uniform float4 in_blend2;     // layer 2
uniform float4 in_blendMode;  // mode of layers 0..2; w = how many layers are live

const float3 kLumWeights = float3(0.3, 0.59, 0.11);

float lumOf(float3 c) {
    return dot(c, kLumWeights);
}

// Pulls a colour back inside the unit cube without moving its luminance, which is what keeps a
// non-separable blend from clipping to a different tone than it computed.
float3 clipColor(float3 c) {
    float l = lumOf(c);
    float n = min(min(c.r, c.g), c.b);
    float x = max(max(c.r, c.g), c.b);
    if (n < 0.0) c = l + (c - l) * l / max(l - n, 0.0001);
    if (x > 1.0) c = l + (c - l) * (1.0 - l) / max(x - l, 0.0001);
    return c;
}

float3 setLum(float3 c, float l) {
    return clipColor(c + (l - lumOf(c)));
}

float softLightChannel(float d, float s) {
    float dd = (d <= 0.25) ? ((16.0 * d - 12.0) * d + 4.0) * d : sqrt(d);
    return (s <= 0.5)
        ? d - (1.0 - 2.0 * s) * d * (1.0 - d)
        : d + (2.0 * s - 1.0) * (dd - d);
}

float3 blendLayer(float3 d, float4 layer, float mode) {
    float3 s = layer.rgb;
    float a = layer.a;
    int m = int(mode + 0.5);
    float3 b;
    if (m == 1) {
        // plus darker: subtractive, so it floors the backdrop instead of greying it
        return clamp(d - a * (1.0 - s), 0.0, 1.0);
    } else if (m == 2) {
        // plus lighter
        return clamp(d + a * s, 0.0, 1.0);
    } else if (m == 3) {
        b = float3(
            softLightChannel(d.r, s.r),
            softLightChannel(d.g, s.g),
            softLightChannel(d.b, s.b)
        );
    } else if (m == 4) {
        // hard light: the layer decides
        b = mix(1.0 - 2.0 * (1.0 - s) * (1.0 - d), 2.0 * s * d, step(s, float3(0.5)));
    } else if (m == 5) {
        // overlay: the backdrop decides
        b = mix(1.0 - 2.0 * (1.0 - d) * (1.0 - s), 2.0 * d * s, step(d, float3(0.5)));
    } else if (m == 6) {
        b = setLum(d, lumOf(s));
    } else if (m == 7) {
        b = min(float3(1.0), d / max(1.0 - s, float3(0.0001)));
    } else if (m == 8) {
        b = 1.0 - min(float3(1.0), (1.0 - d) / max(s, float3(0.0001)));
    } else {
        b = s;
    }
    return clamp(mix(d, b, a), 0.0, 1.0);
}

half4 main(float2 coord) {
    half4 src = child.eval(coord);
    float a = float(src.a);
    if (a <= 0.0) return src;
    float3 d = float3(src.rgb) / a;
    d = blendLayer(d, in_blend0, in_blendMode.x);
    if (in_blendMode.w > 1.5) d = blendLayer(d, in_blend1, in_blendMode.y);
    if (in_blendMode.w > 2.5) d = blendLayer(d, in_blend2, in_blendMode.z);
    return half4(half3(d * a), src.a);
}
"""
