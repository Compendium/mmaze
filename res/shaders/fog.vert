#version 120
uniform vec3 cameraPosition;
uniform float deltaTime;

attribute vec3 position;
attribute vec2 texcoord;

varying vec2 vertTexcoord;
varying vec3 vertPosition;

varying vec3 varCameraPosition;

void main()
{
	vertTexcoord = texcoord;
	vertPosition = (gl_ModelViewProjectionMatrix * vec4(position, 1.0)).xyz;
	varCameraPosition = cameraPosition.xyz;

	//normal
	vec4 pos = vec4(position, 1.0f);
	pos = gl_ModelViewProjectionMatrix * pos;
	//pos.y = pos.y + deltaTime;
	gl_Position = pos;
}