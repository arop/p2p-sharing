package receive;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.Arrays;

import main.Chunk;
import main.Peer;
import extra.FileManagement;
import extra.Tools;

/**
 * Class used to receive the messages in the MDR channel
 * @author André Pires, Filipe Gama
 *
 */
public class ReceiveThread_MDR extends ReceiveThread {

	public ReceiveThread_MDR(Peer mainPeer) {
		super(mainPeer, mainPeer.getMDR_IP(), mainPeer.getMDR_Port());
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

			outerLoop:
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
					case "CHUNK":
						//CHUNK <Version> <FileId> <ChunkNo> <CRLF><CRLF><Body>
						Chunk receivedChunk = new Chunk(getBody(receivedData, index+1));

						splitMessage(receivedChunk, receivedData, index);

						//so guarda o chunk se ainda nao o tiver
						if(!main.alreadyExists(receivedChunk.getFileId(), receivedChunk.getChunkNo())) {
							main.addToReceivedChunks(receivedChunk);
						}
						// se ja tem os chunks todos, restora o ficheiro
						if(main.getChunksToReceive() == main.getReceivedChunks().size()) {
							FileManagement.recover_file(main.getFileToRecover(), main.getReceivedChunks());
							main.clearReceivedChunks();
							break outerLoop;
						}
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
		}
		mSocket.close();
	}
}
