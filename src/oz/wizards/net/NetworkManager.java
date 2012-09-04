package oz.wizards.net;

import java.util.Stack;

import oz.wizards.Main;
import oz.wizards.net.Network.Packet;

public class NetworkManager implements Runnable {
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
