package receive;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.Arrays;
import java.util.Random;

import main.Chunk;
import main.Peer;
import extra.Tools;

/**
 * Class used to receive the messages in the MC channel
 * @author André Pires, Filipe Gama
 *
 */
public class ReceiveThread_MC extends ReceiveThread {
	private int numberOfConfirmations;

	public ReceiveThread_MC(Peer mainPeer) {
		super(mainPeer, mainPeer.getMC_IP(), mainPeer.getMC_Port());
	}

	@Override
	public void run() {
		MulticastSocket mSocket = null;
		InetAddress addr;
		DatagramPacket packet;

		try {
			mSocket = new MulticastSocket(port);

			// Join the Multicast Group
			addr = InetAddress.getByName(IP);
			mSocket.joinGroup(addr);

			while(true) {	
				byte[] buf = new byte[64500];
				packet = new DatagramPacket(buf, buf.length);

				// receive the packets
				mSocket.receive(packet);
				if(main.getPeerPort() != packet.getPort() || !Tools.getPeerAddress().equals(packet.getAddress().toString().substring(1))) {

					byte[] receivedData = Arrays.copyOfRange(packet.getData(), 0, packet.getLength());
					String received = new String(receivedData);
					String fileId = received.split(" +")[2].trim();

					if(received.contains("exit")) break;

					if(Tools.isDebug())
						System.out.println("RECEIVED: " + received.substring(0,50));

					switch(received.split(" +")[0]) {
					case "GETCHUNK":
						//GETCHUNK <Version> <FileId> <ChunkNo> <CRLF><CRLF>
						int chunkNo = Integer.parseInt(received.split(" +")[3].trim());
						Chunk c = null;
						if(main.hasChunk(fileId, chunkNo)) {
							c = main.searchChunk(fileId,chunkNo);

							Random r = new Random();
							Thread.sleep(r.nextInt(401));

							main.getSnd_MC().sendMessage(Tools.generateMessage("CHUNK", c),main.getMDR_IP(),main.getMDR_Port());
						}
						break;

					case "DELETE":
						//DELETE <Version> <FileId> <CRLF><CRLF>
						main.deleteChunksOfFile(received.split(" +")[2].trim());
						main.getDegMonitor().removeFromStoredMap(received.split(" +")[2].trim());
						break;

					case "REMOVED":
						//REMOVED <Version> <FileId> <ChunkNo> <CRLF><CRLF>
						main.setHasReceivedPutchunk(true);
						main.getDegMonitor().decConfirmations(fileId, Integer.parseInt(received.split(" +")[3].trim()),main.getDegMonitor().getSentMap());
						main.getDegMonitor().decConfirmations(fileId, Integer.parseInt(received.split(" +")[3].trim()),main.getDegMonitor().getStoredMap());
						main.compensate(received.split(" +")[2].trim(),Integer.parseInt(received.split(" +")[3].trim()));
						break;

					case "STORED":
						//STORED <Version> <FileId> <ChunkNo> <CRLF><CRLF>
						int chunkNo2 = Integer.parseInt(received.split(" +")[3].trim());
						
						Chunk ficticio = new Chunk(fileId,Integer.parseInt(received.split(" +")[3].trim()),0);
						main.addStoredChunk(ficticio);
						
						if(main.addPortToList(packet.getPort(), packet.getAddress().toString().substring(1))) { 
							if(main.getDegMonitor().hasSentFile(fileId))
								main.getDegMonitor().incConfirmations(fileId,chunkNo2,main.getDegMonitor().getSentMap());
						}
						//adicionou logo era novo, portanto incrementa
						main.getDegMonitor().incConfirmations(fileId,chunkNo2,main.getDegMonitor().getStoredMap());
						break;

					default:
						break;				
					}
				}
			}

			mSocket.leaveGroup(addr);
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
			mSocket.close();
		}

		mSocket.close();
	}

	public void resetConfirmations() { 
		this.numberOfConfirmations = 0;
	}

	int getConfirmations() {
		return this.numberOfConfirmations;
	}
}
