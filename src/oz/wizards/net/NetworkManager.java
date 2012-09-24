package oz.wizards.net;

import java.util.Stack;

import oz.wizards.Main;
import oz.wizards.net.Network.Packet;

public class NetworkManager implements Runnable {
	public final static byte TYPE_REGISTER = 8; //new client wants to register itself
	public final static byte TYPE_UNREGISTER = 9; //clients wants to unregister itself/close connection
	public final static byte TYPE_ACKNOWLEDGE = 10; //ack the client that just connected
	public final static byte TYPE_MOVEMENT = 12; //client wants to inform of movement, unimportant
	public final static byte TYPE_CHAT = 13; //client sent a chat message, important!

	public final static byte TYPE_MAPREQUEST = 32;
	public final static byte TYPE_MAPDATA = 33;

	private Network nw;
	public Stack<Package> receivedPackages;
	Thread receiveThread;
	public boolean keepRunning = true;

	public NetworkManager() {
		receivedPackages = new Stack<Package>();

		nw = new Network();
		nw.create(0);
	}

	@Override
	public void run() {
		while (keepRunning) {
			nw.tick();
			receivedPackages.add(nw.lastReceivedPackage);
		}

		System.out.println("[nw] bye");
	}

	public void send(Package p) {
		nw.send(p);
	}

	public Network getNetwork() {
		return nw;
	}

}
