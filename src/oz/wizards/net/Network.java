package oz.wizards.net;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public class Network {
	public class Packet {
		public DatagramPacket d;
		public Package p;
		public Packet(DatagramPacket d, Package p) {
			this.d = d;
			this.p = p;
		}
	}
	public Packet lastReceivedPackage = null;
	DatagramSocket socket = null;
	
	public Network () {
	}
	
	public void create (int port) {
		try {
			if(port == 0) {
				socket = new DatagramSocket();
			} else {
				socket = new DatagramSocket(port);
			}
		} catch (SocketException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Manages receives, re-sends and re-send-requests of packages
	 */
	public void tick () {
		Package rp = new Package();
		DatagramPacket dp = new DatagramPacket(rp.packet, rp.packet.length);
		try {
			socket.receive(dp);
		} catch (IOException e) {
			//TODO is usually only called when we 'force' close the socket on exit, to unblock from the receive call
			e.printStackTrace();
		}
		rp.length = dp.getLength();
		lastReceivedPackage = new Packet(dp, rp);
	}
	
	public void send (InetAddress dest, int port, Package pkg) {
		try {
			socket.send(new DatagramPacket(pkg.getPacket(), pkg.pointer, dest, port));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void destroy () {
		socket.close();
	}
}
