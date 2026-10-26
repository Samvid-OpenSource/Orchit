#version 300 es
precision mediump float;
uniform sampler2D fromTexture;
uniform sampler2D toTexture;
uniform float progress;
out vec4 outColor;
void main() {
    vec2 uv = gl_FragCoord.xy;
    outColor = mix(texture(fromTexture, uv), texture(toTexture, uv), clamp(progress, 0.0, 1.0));
}
