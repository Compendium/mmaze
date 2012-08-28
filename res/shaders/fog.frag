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
	
	float dist = length(vertPosition - cameraPosition) / 80;
	//c.xyz = (1.0 / dist) * c.xyz;
	//c.xyz = exp(dist) * c.xyz;
	c.a = (1.0 / exp2(dist) * 10) * c.a;
	//if(c.a < .5)
		//discard;
	
	gl_FragColor = c;
}