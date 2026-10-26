#version 300 es
precision mediump float;

uniform sampler2D fromTexture;
uniform sampler2D toTexture;
uniform float progress;

out vec4 outColor;

void main() {
    vec2 uv = gl_FragCoord.xy / vec2(textureSize(fromTexture, 0));
    float p = clamp(progress, 0.0, 1.0);
    float scale = mix(1.25, 1.0, p);
    vec2 toUv = clamp((uv - 0.5) / scale + 0.5, 0.0, 1.0);
    vec4 fromColor = texture(fromTexture, uv);
    vec4 toColor = texture(toTexture, toUv);
    outColor = mix(fromColor, toColor, p);
}
