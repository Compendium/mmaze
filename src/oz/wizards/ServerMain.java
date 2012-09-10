package oz.wizards;

import java.net.InetAddress;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;
import java.util.Vector;

import oz.wizards.net.Network;
import oz.wizards.net.Network.Packet;
import oz.wizards.net.Packer;
import oz.wizards.net.Unpacker;
import oz.wizards.net.Package;

//server
public class ServerMain {
	public static class Client {
		int id; //client-specific id
		long lastPacketTimeClient;
		long lastPacketTimeLocal;
		byte lastPacketTypeReceived;
		byte lastPacketTypeSent;
		Stack<Package> packageCache = new Stack<Package>();//cache of packets sent to the client
		boolean hasMap = false;
		
		InetAddress address;
		int port;
		
		public void setHasMap (boolean b) {
			this.hasMap = b;
		}
	}
	
	final static byte TYPE_REGISTER = 8; //new client wants to register itself
	final static byte TYPE_UNREGISTER = 9; //clients wants to unregister itself/close connection
	final static byte TYPE_ACKNOWLEDGE = 10; //ack the client that just connected
	final static byte TYPE_RESEND = 11; //client wants the server to re-send a specific packet
	final static byte TYPE_MOVEMENT = 12; //client wants to inform of movement, unimportant
	final static byte TYPE_CHAT = 13; //client sent a chat message, important!
	final static byte TYPE_MAPREQUEST = 64; //client requested the map
	final static byte TYPE_MAPDATA = 65;	
	
	/* STRUCTURE OF A MMAZE PACKET SENT TO THE SERVER
	 * string: "MAZE"
	 * long: PACKET_TIME client specific time the package was sent
	 * long: LAST_PACKET_TIME client specific time of the last sent packet (this matters only with 'important' packets)
	 * byte: PACKET_TYPE
	 * byte: LAST_PACKET_TYPE
	 * [byte: CLIENT_ID] only if the client already registered
	 * PAYLOAD
	 */
	
	/* STRUCTURE OF A MMAZE PACKET SENT TO THE CLIENT
	 * string: "MAZE"
	 * long: PACKET_TIME server specific time the package was sent
	 * long: LAST_PACKET_TIME server specific time of the last sent packet (this matters only with 'important' packets)
	 * byte: PACKET_TYPE
	 * byte: LAST_PACKET_TYPE
	 * PAYLOAD
	 */
	
	
	public static void main(String[] args) {
		List<Client> clients = new Vector<Client>();
		Network nw = new Network();
		nw.create(4182);
		
		MazeGenerator mg = new MazeGenerator(64, 64);
		mg.print();
		
		while(true) {
			nw.tick();
			Package p = nw.lastReceivedPackage;
			String ident = Unpacker.unpackString(p);
			if(ident.equals("MAZE")) {
				long clientTime = Unpacker.unpackLong(p);
				byte type = Unpacker.unpackByte(p);
				
				if(type == TYPE_REGISTER) {
					int clientId = clients.size();
					Client newClient = new Client();
					newClient.id = clientId;
					newClient.lastPacketTimeClient = clientTime;
					newClient.lastPacketTimeLocal = System.currentTimeMillis();
					newClient.lastPacketTypeReceived = TYPE_REGISTER;
					newClient.address = p.address;
					newClient.port = p.port;
					clients.add(newClient);
					System.out.println("added client " + clientId + " @ " + p.address.toString());
					
					Package ap = new Package();
					ap.fillHeader();
					Packer.packByte(ap, TYPE_ACKNOWLEDGE);
					Packer.packInt(ap, clientId);
					ap.address = p.address;
					ap.port = p.port;
					nw.send(ap);
				} else if(type == TYPE_UNREGISTER) {
					//clients.set(Unpacker.unpackInt(p.p), null);
					//clients.remove(Unpacker.unpackInt(p));
				} else {
					int clientId = Unpacker.unpackInt(p);
					
					if(type == TYPE_MOVEMENT) {
						float x = Unpacker.unpackFloat(p);
						float y = Unpacker.unpackFloat(p);
						float z = Unpacker.unpackFloat(p);
						float rx = Unpacker.unpackFloat(p);
						float ry = Unpacker.unpackFloat(p);
						float rz = Unpacker.unpackFloat(p);
						for(int i = 0; i < clients.size(); i++) {
							Client c = clients.get(i);
							if(c.id != clientId && c.hasMap) {
								Package np = new Package();
								np.fillHeader();
								Packer.packByte(np, TYPE_MOVEMENT);
								Packer.packInt(np, clientId);
								Packer.packFloat(np, x);
								Packer.packFloat(np, y);
								Packer.packFloat(np, z);
								Packer.packFloat(np, rx);
								Packer.packFloat(np, ry);
								Packer.packFloat(np, rz);
								np.address = c.address;
								np.port = c.port;
								nw.send(np);
							}
						}
					} else if(type == TYPE_CHAT) {
						
					} else if(type == TYPE_MAPREQUEST) {
						int packetCount = (mg.bytemap[0].length * mg.bytemap.length) / 512 + 1;
						int overallSize = (mg.bytemap[0].length * mg.bytemap.length);
						
						Package dimPackage = new Package();
						dimPackage.fillHeader();
						Packer.packByte(dimPackage, TYPE_MAPREQUEST);
						Packer.packInt(dimPackage, mg.bytemap[0].length);
						Packer.packInt(dimPackage, mg.bytemap.length);
						Packer.packInt(dimPackage, packetCount);
						Packer.packInt(dimPackage, overallSize);
						dimPackage.address = p.address;
						dimPackage.port = p.port;
						nw.send(dimPackage);
						
						try {
							Thread.sleep(100);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						byte [] mapdata = new byte[overallSize];
						for(int row = 0; row < mg.bytemap.length; row++){
							for(int col = 0; col < mg.bytemap[0].length; col++) {
								mapdata[col + row*mg.bytemap[0].length] = mg.bytemap[col][row];
							}
						}
						
						for(int c = 0; c < packetCount; c++) {
							int transmissionSize = ((c+1)*512 > overallSize ? overallSize % 512 : 512);
							
							Package data = new Package();
							data.fillHeader();
							Packer.packByte(data, TYPE_MAPDATA);
							Packer.packInt(data, c);
							Packer.packInt(data, transmissionSize);
							System.out.println("pl size = " + transmissionSize);
							Packer.packByteArray(data, Arrays.copyOfRange(mapdata, c*512, c*512 + transmissionSize));
							data.address = p.address;
							data.port = p.port;
							nw.send(data);
							try {
								Thread.sleep(100);
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
						clients.get(clientId).hasMap = true;
						if(clients.get(clientId).hasMap == false) {
							System.err.println("failed");
						}
					}
				}
			}
		}
	}

}
