package receive;

import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.Arrays;
import java.util.Random;

import main.Chunk;
import main.Peer;
import extra.FileManagement;
import extra.Tools;

/**
 * Class used to receive the messages in the MDB channel
 * @author André Pires, Filipe Gama
 *
 */
public class ReceiveThread_MDB extends ReceiveThread {

	public ReceiveThread_MDB(Peer mainPeer) {
		super(mainPeer, mainPeer.getMDB_IP(), mainPeer.getMDB_Port());
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

					if(received.contains("exit")) break;

					int index = getHeader(receivedData);

					if(Tools.isDebug())
						System.out.println("RECEIVED: " + received.substring(0,50));
					switch(received.split(" +")[0]) {
					case "PUTCHUNK":
						//PUTCHUNK <Version> <FileId> <ChunkNo> <ReplicationDeg> <CRLF><CRLF><Body>
						Chunk temp = new Chunk(getBody(receivedData, index+1));
						splitMessage(temp, receivedData, index);

						/* Enhancement 1 */
						Random r = new Random();
						Thread.sleep(r.nextInt(401));

						if(main.getNumberOfStoreds(temp) < temp.getReplicationDeg()) {

							main.setHasReceivedPutchunk(false);

							if(!main.hasChunk(temp.getFileId(), temp.getChunkNo())) {
								if(Tools.folderSize(new File("files\\backups")) + receivedData.length-index-1 <= Tools.getFolderSize() ) {
									FileManagement.materializeChunk(temp);
									main.clearChunkList();
									main.loadChunkList();
									main.getDegMonitor().addToMap("stored",temp);
									main.getSnd_MC().sendMessage(Tools.generateMessage("STORED", temp),main.getMC_IP(),main.getMC_Port());
								}
							}
							else {
								main.getSnd_MC().sendMessage(Tools.generateMessage("STORED",temp),main.getMC_IP(),main.getMC_Port());
							}
						}
						main.removeChunkFromStoredArray(temp);										
						break;

					default:
						break;				
					}
				}
			}

			mSocket.leaveGroup(addr);
		} catch (IOException e) {
			e.printStackTrace();
			mSocket.close();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		mSocket.close();
	}

	@Override
	void splitMessage(Chunk chunk, byte[] message, int index) {
		String[] temp = (new String(message,0,index)).split(" +");
		chunk.setFileId(temp[2]);
		chunk.setChunkNo(Integer.parseInt(temp[3].trim()));
		chunk.setReplicationDeg(Integer.parseInt(temp[4].trim()));
	}
}