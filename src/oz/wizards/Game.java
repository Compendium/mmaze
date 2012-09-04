package oz.wizards;

import java.io.IOException;
import java.lang.reflect.Array;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import org.lwjgl.LWJGLException;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;
import org.lwjgl.opengl.GL20;
import org.lwjgl.util.glu.GLU;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;

import oz.wizards.gfx.OBJFile;
import oz.wizards.gfx.Shader;
import oz.wizards.gfx.Texture;
import oz.wizards.gfx.VertexBatch;
import oz.wizards.net.Network;
import oz.wizards.net.NetworkManager;
import oz.wizards.net.Package;
import oz.wizards.net.Packer;
import oz.wizards.net.Unpacker;
import static org.lwjgl.opengl.GL11.*;

public class Game implements Runnable {
	final static byte TYPE_REGISTER = 8; //new client wants to register itself
	final static byte TYPE_UNREGISTER = 9; //clients wants to unregister itself/close connection
	final static byte TYPE_ACKNOWLEDGE = 10; //ack the client that just connected
	final static byte TYPE_RESEND = 11; //client wants the server to re-send a specific packet
	final static byte TYPE_MOVEMENT = 12; //client wants to inform of movement, unimportant
	final static byte TYPE_CHAT = 13; //client sent a chat message, important!
	final static byte TYPE_MAPREQUEST = 64;
	final static byte TYPE_MAPDATA = 65;	
	
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
	
	MazeGenerator mg;
	
	InetAddress serverAddr;
	int serverPort;
	int clientId;
	
	class Player {
		Vector3f position;
		Vector3f orientation;
	}
	
	HashMap<Integer, Player> players = new HashMap<Integer, Game.Player>();
	long timestapmPositionSend = 0;

	public void update() {
		while(Mouse.next()) {
			if(Mouse.getEventButtonState() == true) {
				if(Mouse.getEventButton() == 0) {
					Mouse.setGrabbed(true);
				}
			} else {
				if(Mouse.getEventButton() == 0) {
					Mouse.setGrabbed(false);
				}
			}
		}
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
		
		Vector3f currentTranslation = new Vector3f(translation);
		Vector3f nextTranslation = new Vector3f(translation);

		//forward
		if (Keyboard.isKeyDown(Keyboard.KEY_COMMA)) {
			float xrad = (float) (rotation.x / 180.0 * Math.PI);
			float yrad = (float) (rotation.y / 180.0 * Math.PI);

			nextTranslation.x += (float) Math.sin(yrad);
			nextTranslation.z -= (float) Math.cos(yrad);
			//nextTranslation.y -= (float)Math.sin(xrad);
		}
		//backward
		if (Keyboard.isKeyDown(Keyboard.KEY_O)) {
			float xrad = (float) (rotation.x / 180.0 * Math.PI);
			float yrad = (float) (rotation.y / 180.0 * Math.PI);

			nextTranslation.x -= (float) Math.sin(yrad);
			nextTranslation.z += (float) Math.cos(yrad);
			//nextTranslation.y += (float)Math.sin(xrad);
		}
		//left
		if (Keyboard.isKeyDown(Keyboard.KEY_A)) {
			float yrad = (float) (rotation.y / 180.0 * Math.PI);
			nextTranslation.x -= (float) Math.cos(yrad);
			nextTranslation.z -= (float) Math.sin(yrad);
		}
		//right
		if (Keyboard.isKeyDown(Keyboard.KEY_E)) {
			float yrad = (float) (rotation.y / 180.0 * Math.PI);
			nextTranslation.x += (float) Math.cos(yrad);
			nextTranslation.z += (float) Math.sin(yrad);
		}
		
		/*System.out.printf("Position (%f|%f), bm(%d|%d), v=%d\n", translation.x, translation.z,
				(int)(translation.x / 10.f), (int)(translation.z / 10.f) + 1,
				mg.bytemap[(int)(translation.x / 10.f)][(int)(translation.z / 10.f)+1]);*/
		
		
		
		if (Keyboard.isKeyDown(Keyboard.KEY_K)) {
			translation = nextTranslation;
		} else {
			int indexx, indexy;
		
			indexy = (int)((nextTranslation.z) / 10.f) + 1;
			indexx = (int)((nextTranslation.x + 2.f) / 10.f);
			//moved right
			if (nextTranslation.x > currentTranslation.x) {
				if (mg.bytemap[indexx][indexy] == 1) {
					System.out.println("collide right");
					//nextTranslation.x = currentTranslation.x;
					//is this working? TODO
					nextTranslation.x = (float) (Math.ceil(currentTranslation.x / 10.f) * 10 - 2.0); 
				}
			}
			
			indexx = (int)((nextTranslation.x - 2.f) / 10.f);
			//moved left
			if (nextTranslation.x < currentTranslation.x) {
				if (mg.bytemap[indexx][indexy] == 1) {
					//is this working? TODO
					nextTranslation.x = (float) (Math.floor(currentTranslation.x / 10.f) * 10 + 2.0); 
				}
			}
			
			indexx = (int)((nextTranslation.x) / 10.f);
			indexy = (int)((nextTranslation.z - 2.f) / 10.f) + 1;
			//moved forward
			if (nextTranslation.z < currentTranslation.z) {
				if (mg.bytemap[indexx][indexy] == 1) {
					//is this working? TODO
					nextTranslation.z = (float) (Math.floor(currentTranslation.z / 10.f) * 10 + 2.0); 
				}
			}
			indexy = (int)((nextTranslation.z + 2.f) / 10.f) + 1;
			//moved backward
			if (nextTranslation.z > currentTranslation.z) {
				if (mg.bytemap[indexx][indexy] == 1) {
					//is this working? TODO
					nextTranslation.z = (float) (Math.ceil(currentTranslation.z / 10.f) * 10 - 2.0); 
				}
			}

			translation = nextTranslation;
		}
		while(Main.networkManager.receivedPackages.size() != 0) {
			Package recv = Main.networkManager.receivedPackages.pop();
			Unpacker.unpackString(recv);
			Unpacker.unpackLong(recv);
			byte tag = Unpacker.unpackByte(recv);
			if(tag == TYPE_MOVEMENT) {
				int cid = Unpacker.unpackInt(recv);
				float x = Unpacker.unpackFloat(recv);
				float y = Unpacker.unpackFloat(recv);
				float z = Unpacker.unpackFloat(recv);
				
				if(players.containsKey(cid)){
					players.get(cid).position.x = x;
					players.get(cid).position.y = y;
					players.get(cid).position.z = z;
				} else {
					Player p = new Player();
					p.orientation = new Vector3f(0,0,0);
					p.position = new Vector3f(x,y,z);
					players.put(cid, p);
				}
			}
		}
		
		if(timestapmPositionSend < System.nanoTime()) {
			timestapmPositionSend = System.nanoTime() + 100000000;
			Package p = new Package();
			p.fillHeader();
			Packer.packByte(p, TYPE_MOVEMENT);
			Packer.packInt(p, clientId);
			Packer.packFloat(p, translation.x);
			Packer.packFloat(p, translation.y);
			Packer.packFloat(p, translation.z);
			
			p.address = serverAddr;
			p.port = serverPort;
			Main.networkManager.queue(p);
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
		glRotatef(rotation.x, 1, 0, 0);
		glRotatef(rotation.y, 0, 1, 0);
		glTranslatef(-translation.x, -translation.y, -translation.z);
		
		shader1.enable();
		glPushMatrix();
		glScalef(10.f, 10.f, 10.f);
		//vb.putOBJ(skyboxTex, skybox);
		//vb.render();
		glPopMatrix();
		shader1.disable();
		
		shaderFog.enable();
		
		for(Map.Entry<Integer, Player> entry : players.entrySet()) {
			Integer key = entry.getKey();
			Player val = entry.getValue();
			glPushMatrix();
			glTranslatef(val.position.x, 5, val.position.z);
			vb.putOBJ(objTex[0], obj[0]);
			vb.render();
			glPopMatrix();
		}
		mg.meshify(vb, terrainTex);
		vb.render();
		
		glPushMatrix();
		glTranslatef(0.f, -50.f, 0.f);
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
		Package registerPackage = new Package();
		registerPackage.fillHeader();
		Packer.packByte(registerPackage, TYPE_REGISTER);
		
		try {
			serverAddr = InetAddress.getByName("127.0.0.1");
		} catch (UnknownHostException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		serverPort = 4182;
		
		registerPackage.address = serverAddr;
		registerPackage.port = serverPort;
		Main.networkManager.getNetwork().send(registerPackage);
		
		try {
			Display.setDisplayMode(new DisplayMode(1024, 768));
			Display.create();
			Display.setTitle("テスト");
			Mouse.setGrabbed(false);

			glMatrixMode(GL_PROJECTION);
			glLoadIdentity();
			glViewport(0, 0, 1024, 768);
			float ratio = (float) (1024.0 / 768.0);
			//glFrustum(-ratio, +ratio, -1, +1, 1.0, 1000.0);
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
			//glClearColor(k * 0x80, k * 0xa6, k * 0xa9, 1.0f);
			glClearColor(0,0,0, 1.0f);
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

		mg = new MazeGenerator(16, 16);
		rotation = new Vector3f(0, 0, 0);
		translation = new Vector3f(0, 5, 0);
		boolean startFound = false;
		for(int x = 0; !startFound; x++) {
			for(int z = 0; !startFound; z++) {
				if(mg.bytemap[x][z] == 1) {
					System.out.println("found!");
					startFound = true;
					translation.x = x * mg.scale + .5f * mg.scale + 1 * mg.scale;
					translation.z = z * mg.scale + .5f * mg.scale;
				}
			}
		}

		obj = new OBJFile[3];
		objTex = new Texture[3];

		try {
			objTex[0] = new Texture("res/textures/cubetex.png");
			//objTex[1] = new Texture("res/textures/monkey.png");
			//objTex[2] = new Texture("res/textures/hito.png");

			obj[0] = new OBJFile("res/models/cube.obj");
			//obj[1] = new OBJFile("res/models/monkey.obj");
			//obj[2] = new OBJFile("res/models/hito.obj");
			
			skybox = new OBJFile("res/models/skybox.obj");
			skyboxTex = new Texture("res/textures/skybox.png");
			
			terrain = new OBJFile("res/models/terrain.obj");
			terrainTex = new Texture("res/textures/terrain.png");
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		System.out.println("waiting for ack-packet");
		
		NetworkManager nm = Main.networkManager;
		while(nm.receivedPackages.size() == 0) {
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		Package rp = nm.receivedPackages.pop();
		Unpacker.unpackString(rp);
		Unpacker.unpackLong(rp);
		System.out.println("recvd: " + Unpacker.unpackByte(rp));
		clientId = Unpacker.unpackInt(rp);
		System.out.println("my id is " + clientId);
		
		System.out.println("requesting map...");
		Package req = new Package();
		req.fillHeader();
		Packer.packByte(req, TYPE_MAPREQUEST);
		Packer.packInt(req, clientId);
		
		req.address = serverAddr;
		req.port = serverPort;
		nm.getNetwork().send(req);
		
		while(nm.receivedPackages.size() == 0) {
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		Package nn = nm.receivedPackages.pop();
		Unpacker.unpackString(nn);
		Unpacker.unpackLong(nn);
		byte t = Unpacker.unpackByte(nn);
		if(t == TYPE_MAPREQUEST) {
			System.out.println("success");
		} else {
			eject(-128);
		}
		
		int mapWidth = Unpacker.unpackInt(nn);
		int mapHeight = Unpacker.unpackInt(nn);
		int packetCount = Unpacker.unpackInt(nn);
		int overallSize = Unpacker.unpackInt(nn);
		System.out.println("w " + mapWidth + ", h " + mapHeight);
		
		class MapPart {
			int n, payloadSize;
			byte[] data;
		}
		Vector<MapPart> parts = new Vector<MapPart> ();
		
		System.out.println("downloading...");
		int j = 0;
		boolean haveAllLevelPackets = false;
		while(haveAllLevelPackets == false) {
			while(nm.receivedPackages.size() == 0) {
				try {
					Thread.sleep(10);
				} catch (Exception e) {}
			}
			Package recvd = nm.receivedPackages.pop();
			Unpacker.unpackString(recvd);
			Unpacker.unpackLong(recvd);
			int type = Unpacker.unpackByte(recvd);
			if(type != TYPE_MAPDATA) {
				System.out.println("discarding");
				continue; //garbled package?!? Just ignore it~
			}
			int n = Unpacker.unpackInt(recvd);
			int payloadSize = Unpacker.unpackInt(recvd); //size of payload
			System.out.println("payload: " + payloadSize);
			
			MapPart mp = new MapPart();
			mp.n = n;
			mp.payloadSize = payloadSize;
			mp.data = Unpacker.unpackByteArray(recvd, payloadSize);
			parts.add(mp);
			j++;
			System.out.println("added, n = " + n + ", packetCount = " + packetCount);
			if(j >= packetCount) {
				haveAllLevelPackets = true;
				System.out.println("finished");
			}
		}
		
		System.out.println("parsing");
		byte[] wmap = new byte[overallSize];
		int wmapptr = 0;
		for(int i = 0; i < packetCount; i++) {
			for(int k = 0; k < parts.get(i).payloadSize; k++) {
				wmap[wmapptr++] = parts.get(i).data[k];
			}
			System.out.println("putted " + parts.get(i).n);
		}
		byte[][] map = new byte[mapWidth][mapHeight];
		for(int row = 0; row < mapHeight; row++) {
			for(int col = 0; col < mapWidth; col++) {
				map[col][row] = wmap[col+row*mapWidth];
			}
		}
		
		System.out.print('\n');
		mg.bytemap = map;
		mg.print();
		
		System.out.println("ready.");
	}

	public void destruct() {
		Display.destroy();
		Package p = new Package();
		p.fillHeader();
		Packer.packByte(p, TYPE_UNREGISTER);
		Packer.packInt(p, clientId);
		Main.networkManager.keepRunning = false;
		
		p.address = serverAddr;
		p.port = serverPort;
		Main.networkManager.getNetwork().send(p);
		Main.networkManager.getNetwork().destroy();
	}

	public void eject(int ecode) {
		System.out
				.println("I'm sorry Dave. I can't let you do that. ERROR CODE "
						+ ecode);
		System.exit(ecode);
	}
}
