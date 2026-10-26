#version 300 es
precision mediump float;

uniform sampler2D fromTexture;
uniform sampler2D toTexture;
uniform float progress;

out vec4 outColor;

void main() {
    vec2 uv = gl_FragCoord.xy / vec2(textureSize(fromTexture, 0));
    float p = clamp(progress, 0.0, 1.0);
    vec4 fromColor = texture(fromTexture, uv - vec2(p, 0.0));
    vec4 toColor = texture(toTexture, uv + vec2(1.0 - p, 0.0));
    outColor = uv.x < p ? toColor : fromColor;
}
