#version 300 es
precision mediump float;

uniform sampler2D fromTexture;
uniform sampler2D toTexture;
uniform float progress;

out vec4 outColor;

void main() {
    vec2 uv = gl_FragCoord.xy / vec2(textureSize(fromTexture, 0));
    float p = clamp(progress, 0.0, 1.0);
    float angle = (1.0 - p) * 3.14159265;
    vec2 centered = uv - 0.5;
    float c = cos(angle);
    float s = sin(angle);
    vec2 rotated = vec2(centered.x * c - centered.y * s, centered.x * s + centered.y * c);
    vec2 toUv = clamp(rotated + 0.5, 0.0, 1.0);
    vec4 fromColor = texture(fromTexture, uv);
    vec4 toColor = texture(toTexture, toUv);
    outColor = mix(fromColor, toColor, p);
}
