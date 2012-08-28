#version 120
attribute vec3 position;
attribute vec2 texcoord;

varying vec2 vertTexcoord;

void main()
{
	vertTexcoord = texcoord;

	//normal
	gl_Position = gl_ModelViewProjectionMatrix * vec4(position.x, position.y, position.z, 1.0f);
}