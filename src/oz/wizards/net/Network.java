package oz.wizards.net;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Arrays;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

public class Network {
	public class Packet {
		public DatagramPacket d;
		public Package p;
		public Packet(DatagramPacket d, Package p) {
			this.d = d;
			this.p = p;
		}
	}
	public Package lastReceivedPackage = null;
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
		rp.address = dp.getAddress();
		rp.port = dp.getPort();
		
		final Inflater decompressor = new Inflater();
		decompressor.setInput(rp.packet);
		final ByteArrayOutputStream baos = new ByteArrayOutputStream(rp.packet.length);
		final byte[] buf = new byte[1024];
		try {
			while(!decompressor.finished()) {
				int count = decompressor.inflate(buf);
				baos.write(buf, 0, count);
			}
		} catch (DataFormatException e) {
			e.printStackTrace();
		}
		
		try {
			baos.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		rp.packet = baos.toByteArray();
		
		lastReceivedPackage = rp;
	}
	
	public byte[] compress (byte [] source) {
		Deflater compressor = new Deflater();
		compressor.setLevel(Deflater.BEST_SPEED);
		compressor.setInput(source);
		compressor.finish();
		ByteArrayOutputStream bos = new ByteArrayOutputStream(source.length);
		byte [] buf = new byte [1024];
		while(!compressor.finished()) {
			int count = compressor.deflate(buf);
			bos.write(buf, 0, count);
		}
		try {
			bos.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return bos.toByteArray();
	}
	
	public void send (Package pkg) {
		try {
			byte [] buffer = compress(Arrays.copyOf(pkg.getPacket(), pkg.pointer));
			
			System.out.println("would have normally sent " + pkg.pointer + " bytes");
			System.out.println("but with compression " + buffer.length + " bytes");
			//socket.send(new DatagramPacket(pkg.getPacket(), pkg.pointer, pkg.address, pkg.port));
			socket.send(new DatagramPacket(buffer, buffer.length, pkg.address, pkg.port));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	public void destroy () {
		socket.close();
	}
	
	public InetAddress getAddress () {
		return socket.getLocalAddress();
	}
	
	public int getPort () {
		return socket.getLocalPort();
	}
}
