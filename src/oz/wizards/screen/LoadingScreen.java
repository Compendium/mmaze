package oz.wizards.screen;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Stack;
import java.util.Vector;

import org.lwjgl.LWJGLException;
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
		Main.networkManager.getNetwork().send(register);
		sentRegisterPackage = System.nanoTime();
	}
	
	long sentRegisterPackage = -1;
	boolean receivedAckPackage = false;
	
	long sentMapReqPackage = -1;
	boolean receivedMapReqPackage = false;
	
	long sentMapRAckPackage = -1;
	long sentNAck = -1;
	
	long lastReceivedMapDataPackage = -1;
	
	InetAddress serverAddr;
	int serverPort;
	int clientId = -1;
	
	class MapPart {
		int n, payloadSize;
		byte [] data;
	}
	Vector<MapPart> parts = new Vector<MapPart>();
	

	@Override
	public void update() {
		if (!isMultiplayer) {
			Main.game.mg = new MazeGenerator(32, 32);
			isFinished = true;
		} else {
			NetworkManager nm = Main.networkManager;
			while(nm.receivedPackages.size() > 0) {
				Package rp = nm.receivedPackages.pop();
				packages.add(rp);
				
				for(int i = 0; i < packages.size(); i++) {
					Package p = packages.get(i);
					Unpacker.unpackString(p);
					Unpacker.unpackLong(p);
					byte b = Unpacker.unpackByte(p);
					if(b == NetworkManager.TYPE_ACKNOWLEDGE) {
						packages.remove(i);
						sentRegisterPackage = -1;
						receivedAckPackage = true;
						clientId = Unpacker.unpackInt(p);
						
						Package mapreq = new Package();
						mapreq.fillHeader();
						Packer.packByte(mapreq, NetworkManager.TYPE_MAPREQUEST);
						Packer.packInt(mapreq, clientId);
						mapreq.address = serverAddr;
						mapreq.port = serverPort;
						nm.getNetwork().send(mapreq);
						
						sentMapReqPackage = System.nanoTime();
					} else if (b == NetworkManager.TYPE_MAPDATA) {
						packages.remove(i);
						sentMapReqPackage = -1;
						sentNAck = -1;
						lastReceivedMapDataPackage = System.nanoTime();
						
						//TODO
					} else if(b == NetworkManager.TYPE_COMPLETE) {
						lastReceivedMapDataPackage = -1;
						packages.remove(i);
						boolean complete = true;
						Stack<Integer> missing = new Stack<Integer>();
						
						for(int n = 0; n < parts.size(); n++) {
							if(parts.get(n) == null) {
								complete = false;
								missing.push(n);
							}
						}
						if (complete) {
							Package ack = new Package();
							ack.fillHeader();
							Packer.packByte(ack,
									NetworkManager.TYPE_ACK);
							Packer.packInt(ack, clientId);
							ack.address = serverAddr;
							ack.port = serverPort;
							nm.getNetwork().send(ack);
						} else {
							Package nack = new Package();
							nack.fillHeader();
							Packer.packByte(nack,
									NetworkManager.TYPE_NACK);
							Packer.packInt(nack, clientId);
							for(int k = 0; k < missing.size(); k++) 
								Packer.packInt(nack, missing.pop());
							nack.address = serverAddr;
							nack.port = serverPort;
							nm.getNetwork().send(nack);
							sentNAck = System.nanoTime();
						}
					}
					p.rewind();
				}
			}
		}
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
