package oz.wizards;

import java.io.IOException;

import org.lwjgl.LWJGLException;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;
import org.lwjgl.opengl.GL20;
import org.lwjgl.util.glu.GLU;
import org.lwjgl.util.vector.Vector3f;

import oz.wizards.gfx.OBJFile;
import oz.wizards.gfx.Shader;
import oz.wizards.gfx.Texture;
import oz.wizards.gfx.VertexBatch;
import static org.lwjgl.opengl.GL11.*;

public class Game implements Runnable {
	public static int drawCalls = 0;
	
	public boolean isRunning = false;
	public boolean isCloseRequested = false;

	private Shader shader1;
	Shader shaderFog;
	int uniformCameraPosition = -1;
	int uniformDelta = -1;
	private VertexBatch vb;

	OBJFile skybox;
	Texture skyboxTex;
	
	OBJFile terrain;
	Texture terrainTex;
	
	OBJFile obj[];
	Texture objTex[];

	Vector3f rotation;
	Vector3f translation;
	float anglex = 0;
	float angley = 0;
	float zoom = 1.f;

	int model = 0;
	
	float k = 0.0f;

	public void update() {
		float mx = Mouse.getDX();
		float my = Mouse.getDY();
		if (Mouse.isButtonDown(0)) {
			rotation.y += (float)mx * 0.1f;
			rotation.x -= (float)my * 0.1f;
		}

		// translation.x = (float) Math.sin(anglex) * 10 * zoom;
		// translation.z = (float) Math.cos(anglex) * 10 * zoom;
		// translation.y = (float) Math.sin(angley) * 10 * zoom;

		while (Keyboard.next()) {
			if (Keyboard.getEventKeyState() == true) {
				if (Keyboard.getEventKey() == Keyboard.KEY_RETURN) {
					if (++model == 3)
						model = 0;
				}
				if(Keyboard.getEventKey() == Keyboard.KEY_ESCAPE) {
					this.isCloseRequested = true;
				}
			}
		}

		if (Keyboard.isKeyDown(Keyboard.KEY_COMMA)) {
			float xrad = (float) (rotation.x / 180.0 * Math.PI);
			float yrad = (float) (rotation.y / 180.0 * Math.PI);

			translation.x += (float) Math.sin(yrad);
			translation.z -= (float) Math.cos(yrad);
			translation.y -= (float)Math.sin(xrad);
		}
		if (Keyboard.isKeyDown(Keyboard.KEY_O)) {
			float xrad = (float) (rotation.x / 180.0 * Math.PI);
			float yrad = (float) (rotation.y / 180.0 * Math.PI);

			translation.x -= (float) Math.sin(yrad);
			translation.z += (float) Math.cos(yrad);
			translation.y += (float)Math.sin(xrad);
		}
		if (Keyboard.isKeyDown(Keyboard.KEY_A)) {
			float yrad = (float) (rotation.y / 180.0 * Math.PI);
			translation.x -= (float) Math.cos(yrad);
			translation.z -= (float) Math.sin(yrad);
		}
		if (Keyboard.isKeyDown(Keyboard.KEY_E)) {
			float yrad = (float) (rotation.y / 180.0 * Math.PI);
			translation.x += (float) Math.cos(yrad);
			translation.z += (float) Math.sin(yrad);
		}
		
		if(Keyboard.isKeyDown(Keyboard.KEY_K)) {
			k += .1f;
		}
	}

	long tPrint = 0;
	public void draw() {
		drawCalls = 0;
		long timestamp = System.nanoTime();
		
		GL20.glUniform3f(uniformCameraPosition, 0, translation.y, translation.z);
		GL20.glUniform1f(uniformDelta, translation.x);
		
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER,
				GL11.GL_NEAREST);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER,
				GL11.GL_NEAREST);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S,
				GL11.GL_REPEAT);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T,
				GL11.GL_REPEAT);
		
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
		glMatrixMode(GL_MODELVIEW);
		glLoadIdentity();
		// glTranslatef(0, 0, -15.f);
		glRotatef(rotation.x, 1, 0, 0);
		glRotatef(rotation.y, 0, 1, 0);
		glTranslatef(-translation.x, -translation.y, -translation.z);
		// glRotatef(rotation.x, 1.f, 0.f, 0.f);
		// glRotatef(rotation.y, 0.f, 1.f, 0.f);
		// glTranslatef(-translation.x, -translation.y, -translation.z);
		// GLU.gluLookAt(-translation.x, -translation.y, -translation.z, 0, 0,
		// 0,
		// 0, 1, 0);
		shader1.enable();
		glPushMatrix();
		glScalef(10.f, 10.f, 10.f);
		vb.putOBJ(skyboxTex, skybox);
		vb.render();
		glPopMatrix();
		shader1.disable();
		
		shaderFog.enable();
		
		vb.putOBJ(objTex[model], obj[model]);
		vb.render();
		
		glPushMatrix();
		glTranslatef(0.f, -10.f, 0.f);
		vb.putOBJ(terrainTex, terrain);
		vb.render();
		glPopMatrix();
		shaderFog.disable();
		Display.update();
		
		long frametime = System.nanoTime() - timestamp;
		if(tPrint < System.nanoTime()) {
			tPrint = System.nanoTime() + 1000000000;
			System.out.println("draw calls = " + drawCalls);
			System.out.println("frame-time = " + frametime);
			System.out.println("k = " + k);
			System.out.println("~");
		}
	}

	@Override
	public void run() {
		isRunning = true;
		create();
		while (isCloseRequested == false) {
			if(Display.isCloseRequested())
				isCloseRequested = true;
			update();
			draw();
		}
		destruct();
		isRunning = false;
	}

	public void create() {
		try {
			Display.setDisplayMode(new DisplayMode(1024, 768));
			Display.create();
			Display.setTitle("テスト");
			Mouse.setGrabbed(true);

			glMatrixMode(GL_PROJECTION);
			glLoadIdentity();
			glViewport(0, 0, 1024, 768);
			float ratio = (float) (1024.0 / 768.0);
			// glFrustum(-ratio, +ratio, -1, +1, 1.0, 1000.0);
			GLU.gluPerspective(45, ratio, 1.f, 10000.f);

			glMatrixMode(GL_MODELVIEW);
			glLoadIdentity();

			glEnable(GL_TEXTURE_2D);
			glDisable(GL_SMOOTH);
			glDisable(GL_CULL_FACE);
			glEnable(GL_CULL_FACE);
			// glFrontFace(GL_CCW);
			glEnable(GL_DEPTH_TEST);
			
			glEnable(GL_BLEND);
			glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

			float k = 1.f / 255.f;
			glClearColor(k * 0x80, k * 0xa6, k * 0xa9, 1.0f);
			glClearDepth(1.0f);
			glClear(GL_COLOR_BUFFER_BIT);
			Display.update();
		} catch (LWJGLException e) {
			e.printStackTrace();
			eject(-1);
		}

		shader1 = new Shader("res/shaders/shader");
		shaderFog = new Shader("res/shaders/fog");
		
		uniformDelta = shaderFog.getUniformLocation("deltaTime");
		uniformCameraPosition = shaderFog.getUniformLocation("cameraPosition");
		System.out.println(uniformCameraPosition + ", campos");
		System.out.println(uniformDelta + ", delta");
		
		vb = new VertexBatch(shader1);

		rotation = new Vector3f(0, 0, 0);
		translation = new Vector3f(0, 0, 0);

		obj = new OBJFile[3];
		objTex = new Texture[3];

		try {
			objTex[0] = new Texture("res/textures/cubetex.png");
			objTex[1] = new Texture("res/textures/monkey.png");
			objTex[2] = new Texture("res/textures/hito.png");

			obj[0] = new OBJFile("res/models/cube.obj");
			obj[1] = new OBJFile("res/models/monkey.obj");
			obj[2] = new OBJFile("res/models/hito.obj");
			
			skybox = new OBJFile("res/models/skybox.obj");
			skyboxTex = new Texture("res/textures/skybox.png");
			
			terrain = new OBJFile("res/models/terrain.obj");
			terrainTex = new Texture("res/textures/terrain.png");
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void destruct() {
		Display.destroy();
	}

	public void eject(int ecode) {
		System.out
				.println("I'm sorry Dave. I can't let you do that. ERROR CODE "
						+ ecode);
		System.exit(ecode);
	}
}
