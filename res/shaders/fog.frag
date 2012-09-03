#version 120

varying vec2 vertTexcoord;
varying vec3 vertPosition;

uniform sampler2D texture;
uniform vec3 cameraPosition;
uniform float deltaTime;

void main() {
	//gl_FragColor = vertColor;
	vec4 c = texture2D(texture, vertTexcoord);
	//c *= vertColor;
	//vec4 c = vertColor;
	
	float dist = length(vertPosition - cameraPosition) / 12;
	//c.xyz = (1.0 / dist) * c.xyz;
	//c.xyz = exp(dist) * c.xyz;
	c.r = (1.0 / exp2(dist) * 2) * c.r * 0.85f;
	c.g = (1.0 / exp2(dist) * 2) * c.g * 0.85f;
	c.b = (1.0 / exp2(dist) * 2) * c.b * 0.85f;
	//if(c.a < .5)
		//discard;
	
	gl_FragColor = c;
}