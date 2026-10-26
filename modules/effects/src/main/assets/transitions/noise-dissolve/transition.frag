#version 300 es
precision mediump float;

uniform sampler2D fromTexture;
uniform sampler2D toTexture;
uniform float progress;

out vec4 outColor;

float hash(vec2 value) {
    return fract(sin(dot(value, vec2(12.9898, 78.233))) * 43758.5453);
}

void main() {
    vec2 uv = gl_FragCoord.xy / vec2(textureSize(fromTexture, 0));
    float p = clamp(progress, 0.0, 1.0);
    float threshold = hash(floor(gl_FragCoord.xy / 3.0));
    float reveal = smoothstep(threshold, threshold + 0.05, p);
    reveal = max(reveal, smoothstep(0.95, 1.0, p));
    vec4 fromColor = texture(fromTexture, uv);
    vec4 toColor = texture(toTexture, uv);
    outColor = mix(fromColor, toColor, reveal);
}
