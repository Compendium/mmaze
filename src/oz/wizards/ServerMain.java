package oz.wizards;

import java.net.InetAddress;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;
import java.util.Vector;

import oz.wizards.net.Network;
import oz.wizards.net.NetworkManager;
import oz.wizards.net.Packer;
import oz.wizards.net.Unpacker;
import oz.wizards.net.Package;

//server
public class ServerMain {
	public static class Client {
		int id; //client-specific id
		
		InetAddress address;
		int port;
	}
	
	
	public static void main(String[] args) {
		List<Client> clients = new Vector<Client>();
		Network nw = new Network();
		nw.create(4182);
		System.out.println("bound local server to " + nw.getAddress().getHostAddress() + ":" +  nw.getPort());
		
		MazeGenerator mg = new MazeGenerator(16, 16);
		System.out.println("generated new map, width: " + mg.getWidth() + ", height: " + mg.getHeight());
		
		while(true) {
			nw.tick();
			Package p = nw.lastReceivedPackage;
			String ident = Unpacker.unpackString(p);
			if(ident.equals("MAZE")) {
				byte type = Unpacker.unpackByte(p);
				
				if(type == NetworkManager.TYPE_REGISTER) {
					int clientId = clients.size();
					Client newClient = new Client();
					newClient.id = clientId;
					newClient.address = p.address;
					newClient.port = p.port;
					clients.add(newClient);
					System.out.println("added client " + clientId + " @ " + p.address.toString() + ":" + p.port);
					
					Package ap = new Package();
					ap.fillHeader();
					Packer.packByte(ap, NetworkManager.TYPE_ACKNOWLEDGE);
					Packer.packInt(ap, clientId);
					Packer.packInt(ap, (int)Math.round(Math.random()* 1000));
					ap.address = p.address;
					ap.port = p.port;
					nw.send(ap);
				} else if(type == NetworkManager.TYPE_UNREGISTER) {
					//clients.set(Unpacker.unpackInt(p.p), null);
					//clients.remove(Unpacker.unpackInt(p));
				} else {
					int clientId = Unpacker.unpackInt(p);
					
					if(type == NetworkManager.TYPE_MOVEMENT) {
						float x = Unpacker.unpackFloat(p);
						float y = Unpacker.unpackFloat(p);
						float z = Unpacker.unpackFloat(p);
						float rx = Unpacker.unpackFloat(p);
						float ry = Unpacker.unpackFloat(p);
						float rz = Unpacker.unpackFloat(p);
						
						
						for(int i = 0; i < clients.size(); i++) {
							Client c = clients.get(i);
							if(c.id != clientId) {
								Package np = new Package();
								np.fillHeader();
								Packer.packByte(np, NetworkManager.TYPE_MOVEMENT);
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
					} else if(type == NetworkManager.TYPE_CHAT) {
						
					} else if(type == NetworkManager.TYPE_MAPREQUEST) {
						int numParts = Unpacker.unpackInt(p);
						int [] missing = null;
						if(numParts != -1) {
							missing = new int[numParts];
							for(int i = 0; i < numParts; i++) {
								missing[i] = Unpacker.unpackInt(p);
							}
						}
						
						int packetCount = (mg.bytemap[0].length * mg.bytemap.length) / 512 + 1;
						int overallSize = (mg.bytemap[0].length * mg.bytemap.length);
						
						byte [] mapdata = new byte[overallSize];
						for(int row = 0; row < mg.bytemap.length; row++){
							for(int col = 0; col < mg.bytemap[0].length; col++) {
								mapdata[col + row*mg.bytemap[0].length] = mg.bytemap[col][row];
							}
						}
						
						for(int i = 0; i < mg.bytemap[0].length; i++) {
							System.out.print(mg.bytemap[0][i]);
						}
						System.out.print("\n");

						if (numParts == -1) {// send all data packages
							System.out.println("sending all " + packetCount + " mapdata packages");
							for (int n = 0; n < packetCount; n++) {
								int transmissionSize = ((n + 1) * 512 > overallSize ? overallSize % 512
										: 512);

								Package data = new Package();
								data.fillHeader();
								Packer.packByte(data, NetworkManager.TYPE_MAPDATA);
								Packer.packInt(data, n);
								Packer.packInt(data, packetCount);//c
								Packer.packInt(data, overallSize);
								System.out.println("payload size = "
										+ transmissionSize);
								Packer.packByteArray(
										data,
										Arrays.copyOfRange(mapdata, n * 512, n
												* 512 + transmissionSize));
								data.address = p.address;
								data.port = p.port;
								nw.send(data);
							}
						} else { //send only specific packages
							System.out.println("sending only " + numParts + " specific mapdata packages");
							for(int j = 0; j < numParts; j++) {
								int n = missing[j];
								int transmissionSize = ((n + 1) * 512 > overallSize ? overallSize % 512 : 512);
								
								Package data = new Package();
								data.fillHeader();
								Packer.packByte(data, NetworkManager.TYPE_MAPDATA);
								Packer.packInt(data, n);
								Packer.packInt(data, packetCount);//c
								Packer.packInt(data, overallSize);
								System.out.println("payload size = "
										+ transmissionSize);
								Packer.packByteArray(
										data,
										Arrays.copyOfRange(mapdata, n * 512, n
												* 512 + transmissionSize));
								data.address = p.address;
								data.port = p.port;
								nw.send(data);
							}
						}
					}
				}
			}
		}
	}

}
