package oz.wizards.screen;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.List;
import java.util.Stack;
import java.util.Vector;

import org.lwjgl.LWJGLException;
import org.lwjgl.Sys;
import org.lwjgl.opengl.Display;
import org.lwjgl.util.glu.GLU;

import oz.wizards.Main;
import oz.wizards.MazeGenerator;
import oz.wizards.net.NetworkManager;
import oz.wizards.net.Package;
import oz.wizards.net.Packer;
import oz.wizards.net.Unpacker;

import static org.lwjgl.opengl.GL11.*;

public class LoadingScreen extends Screen implements Runnable {
	boolean isMultiplayer = true;
	boolean isFinished = false;
	List<Package> packages = new Vector<Package>();

	@Override
	public void run() {
		create();
		while (!isFinished) {
			update();
			draw();
		}
		destruct();
	}

	long sentRegisterPackage = -1;
	boolean registrationAcknowledged = false;
	long sentMapRequestPackage = -1;
	boolean allDataReceived = false;
	long receivedMapDataPackage = -1;

	@Override
	public void create() {
		try {
			Display.makeCurrent();
		} catch (LWJGLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			serverAddr = InetAddress.getByName("localhost");
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		serverPort = 4182;

		Package register = new Package();
		register.fillHeader();
		Packer.packByte(register, NetworkManager.TYPE_REGISTER);
		register.address = serverAddr;
		register.port = serverPort;
		Main.networkManager.send(register);
		sentRegisterPackage = System.nanoTime();
	}

	InetAddress serverAddr;
	int serverPort;
	int clientId = -1;
	int sclientId = -1;

	class MapPart {
		int n, c, bs;
		byte[] data;

		public MapPart(int n, int c, int bs, byte[] data) {
			this.n = n;
			this.c = c;
			this.bs = bs;
			this.data = data;
		}
	}

	Vector<MapPart> parts = new Vector<MapPart>();

	@Override
	public void update() {
		if (!isMultiplayer) {
			Main.game.mg = new MazeGenerator(16, 16);
			isFinished = true;
		} else {
			NetworkManager nm = Main.networkManager;
			while (nm.receivedPackages.size() > 0) {
				Package rp = nm.receivedPackages.pop();
				packages.add(rp);
			}

			for (int i = 0; i < packages.size(); i++) {
				Package p = packages.get(i);
				Unpacker.unpackString(p);
				byte b = Unpacker.unpackByte(p);
				if (b == NetworkManager.TYPE_ACKNOWLEDGE) {
					registrationAcknowledged = true;
					clientId = Unpacker.unpackInt(p);
					sclientId = Unpacker.unpackInt(p);
					System.out.println("received ack, my clientId is " + clientId);

					Package request = new Package();
					request.fillHeader();
					Packer.packByte(request, NetworkManager.TYPE_MAPREQUEST);
					Packer.packInt(request, clientId);
					Packer.packInt(request, -1);
					request.address = serverAddr;
					request.port = serverPort;
					Main.networkManager.send(request);
					sentMapRequestPackage = System.nanoTime();
					receivedMapDataPackage = System.nanoTime();

					packages.remove(i);
				} else if (b == NetworkManager.TYPE_MAPDATA) {
					receivedMapDataPackage = System.nanoTime();
					int n = Unpacker.unpackInt(p);
					int c = Unpacker.unpackInt(p);
					int bs = Unpacker.unpackInt(p);
					int transmissionSize = ((n + 1) * 512 > bs ? bs % 512
							: 512);
					System.out.println("received mapdata! [n = " + n + ", c = " + c + ", bs = " + bs + ", ts = " + transmissionSize + "]");
					byte[] data = Unpacker.unpackByteArray(p, transmissionSize);
					MapPart mp = new MapPart(n, c, bs, data);
					parts.add(mp);

					// find out if we still need some data packages
					List<Integer> missing = new Vector<Integer>();
					for (int h = 0; h < c; h++) {
						boolean hasH = false;
						for (int k = 0; k < parts.size(); k++) {
							if (parts.get(k).n == h)
								hasH = true;
						}
						if (hasH == false)
							missing.add(h);
					}

					if (missing.size() == 0) {// have all the MapData
												// packages
						allDataReceived = true;
						processMapData(parts);
					}
					packages.remove(i);
				}

				p.rewind();
			}

			// handle re-sends
			{
				if (registrationAcknowledged == false
						&& sentRegisterPackage + 1000000000L < System
								.nanoTime()) {
					System.out.println("had to re-send registration package!");
					Package register = new Package();
					register.fillHeader();
					Packer.packByte(register, NetworkManager.TYPE_REGISTER);
					register.address = serverAddr;
					register.port = serverPort;
					Main.networkManager.send(register);
					sentRegisterPackage = System.nanoTime();

				} else if (allDataReceived == false
						&& sentMapRequestPackage != -1
						&& receivedMapDataPackage + 1000000000L < System
								.nanoTime()) {
					System.out.println("RESEND!!! MAPDATA");
					int c = parts.get(0).c;

					// find out if we still need some data packages
					List<Integer> missing = new Vector<Integer>();
					for (int h = 0; h < c; h++) {
						boolean hasH = false;
						for (int k = 0; k < parts.size(); k++) {
							if (parts.get(k).n == h)
								hasH = true;
						}
						if (hasH == false)
							missing.add(h);
					}

					if (missing.size() == 0) {// have all the MapData
												// packages
						allDataReceived = true;
						processMapData(parts);
					} else {// still need one or more MapData packages
						Package request = new Package();
						request.fillHeader();
						Packer.packByte(request, NetworkManager.TYPE_MAPREQUEST);
						Packer.packInt(request, clientId);
						Packer.packInt(request, missing.size());
						System.out.print("re-requesting " + missing.size() + " packages ");
						for (int j = 0; j < missing.size(); j++) {
							Packer.packInt(request, missing.get(j));
							System.out.print(missing.get(j) + ", ");
						}
						System.out.print("\n");
						request.address = serverAddr;
						request.port = serverPort;
						Main.networkManager.send(request);
						sentMapRequestPackage = System.nanoTime();
						receivedMapDataPackage = System.nanoTime();
					}
				}
			}
		}
	}

	private void processMapData(Vector<MapPart> p) {
		int mapWidth = 16*2+1;
		int mapHeight = mapWidth;
		int packetCount = p.get(0).n;
		int overallSize = p.get(0).bs;
		
		//sort p
		{
			int inner, outer;
			for(outer = 1; outer < p.size(); outer++) {
				MapPart temp = p.get(outer);
				inner = outer;
				while (inner > 0 && p.get(inner-1).n >= temp.n) {
					p.set(inner, p.get(inner-1));
					inner--;
				}
				p.set(inner, temp);
			}

		}
		System.out.print("unfiltered:\t");
		for(int i = 0; i < p.size(); i++) {
			System.out.print(p.get(i).n + ", ");
		}
		System.out.print("\n");
		for(int i = 0; i < p.size(); i++) {
			MapPart sample = p.get(i);
			for(int k = i+1; k < p.size() && p.get(k).n == sample.n; k++) {
				p.remove(k);
			}
		}
		System.out.print("filtered:\t");
		for(int i = 0; i < p.size(); i++) {
			System.out.print(p.get(i).n + ", ");
		}
		System.out.print("\n");
		System.out.println("exciting! got " + p.size() + " parts, should have gotten " + p.get(0).c + " parts");
		
		byte [] buffer = new byte[overallSize];
		int wbufptr = 0;
		for(int i = 0; i < packetCount+1; i++) {
			for(int j = 0; j < parts.get(i).data.length; j++) {
				buffer[wbufptr++] = parts.get(i).data[j];
			}
		}
		System.out.println("wbufptr = " + wbufptr + ", should be " + buffer.length);
		byte [][] map = new byte[mapWidth][mapHeight];
		for(int row = 0; row < mapHeight; row++) {
			for(int col = 0; col < mapWidth; col++) {
				map[col][row] = buffer[col+row*mapWidth];
			}
		}
		
		Main.game = new GameScreen();
			
		Main.game.mg = new MazeGenerator(16, 16);
		Main.game.mg.bytemap = map;
		Main.game.clientId = clientId;
		
		
		Main.sm.setNextScreen(Main.game);
		this.active = false;
	}

	@Override
	public void draw() {
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
		glMatrixMode(GL_MODELVIEW);
		glLoadIdentity();
		glMatrixMode(GL_PROJECTION);
		glLoadIdentity();
		float ratio = (float) Display.getWidth() / (float) Display.getHeight();
		GLU.gluOrtho2D(-ratio, +ratio, -1, +1);

		Display.update();
	}

	@Override
	public void destruct() {
		try {
			Display.releaseContext();
		} catch (LWJGLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
