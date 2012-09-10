#version 120
attribute vec3 position;
attribute vec2 texcoord;
attribute vec3 color;

varying vec2 vertTexcoord;
varying vec3 vertColor;

void main()
{
	vertTexcoord = texcoord;
	vertColor = color;

	//normal
	gl_Position = gl_ModelViewProjectionMatrix * vec4(position.x, position.y, position.z, 1.0f);
}