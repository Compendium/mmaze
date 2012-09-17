package oz.wizards.net;

import java.util.Stack;

import oz.wizards.Main;
import oz.wizards.net.Network.Packet;

public class NetworkManager implements Runnable {	
	public final static byte TYPE_REGISTER = 8; //new client wants to register itself
	public final static byte TYPE_UNREGISTER = 9; //clients wants to unregister itself/close connection
	public final static byte TYPE_ACKNOWLEDGE = 10; //ack the client that just connected
	public final static byte TYPE_RESEND = 11; //client wants the server to re-send a specific packet
	public final static byte TYPE_MOVEMENT = 12; //client wants to inform of movement, unimportant
	public final static byte TYPE_CHAT = 13; //client sent a chat message, important!
	
	public final static byte TYPE_MAPREQUEST = 64;
	public final static byte TYPE_MAPRACK = 65;
	public final static byte TYPE_MAPDATA = 66;	
	public final static byte TYPE_COMPLETE = 67;
	public final static byte TYPE_ACK = 32;
	public final static byte TYPE_NACK = 33;

	private Network nw;
	public Stack<Package> receivedPackages;
	public Stack<Package> sendQueue;
	Thread receiveThread;
	public boolean keepRunning = true;
	
	public NetworkManager() {
		receivedPackages = new Stack<Package>();
		sendQueue = new Stack<Package>();
		
		nw = new Network();
		nw.create(0);
	}
	
	@Override
	public void run() {
		receiveThread = new Thread(new Runnable() {
			public void run() {
				while(keepRunning) {
					nw.tick();
					receivedPackages.add(nw.lastReceivedPackage);
				}
			}
		});
		receiveThread.start();
		
		System.out.println("[nw] ready");
		while(keepRunning) {
			if(sendQueue.size() > 0) {
				int n = sendQueue.size();
				for(int i = 0; i < n; i++) {
					Package p = sendQueue.pop();
					nw.send(p);
				}
			} else {
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		
		System.out.println("[nw] bye");
	}
	
	public void queue (Package p) {
		sendQueue.push(p);
	}
	
	public Network getNetwork() {
		return nw;
	}
	
}
