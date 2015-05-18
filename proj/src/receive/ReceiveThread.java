package receive;

import java.util.Arrays;

import main.Chunk;
import main.Peer;
import extra.Tools;

/**
 * Class used to receive the messages in a channel
 * @author André Pires, Filipe Gama
 *
 */
public class ReceiveThread extends Thread {
	protected String IP;
	protected int port;
	protected Peer main;

	ReceiveThread(Peer mainPeer, String ip, int portN) {
		if(Tools.validIP(ip)) IP = ip;
		else System.err.println("Invalid IP");
		main = mainPeer;		
		port = portN;
	}

	void splitMessage(Chunk chunk, byte[] message, int index) {
		String[] temp = (new String(message,0,index)).split(" +");
		chunk.setFileId(temp[2]);
		chunk.setChunkNo(Integer.parseInt(temp[3].trim()));
	}

	protected int getHeader(byte[] msg) {
		int i;
		boolean foundFirstCRLF = false;

		for(i = 1; i < msg.length; i++) {
			if(foundFirstCRLF && msg[i-1] == 0xD && msg[i] == 0xA) // CRLF = 0xD 0xA
				break;
			if(msg[i-1] == 0xD && msg[i] == 0xA)
				foundFirstCRLF = true;
		}

		return i;
	}

	protected byte[] getBody(byte[] msg, int i) {
		if(msg.length == i) {
			return new byte[0];
		}

		return Arrays.copyOfRange(msg, i, msg.length);
	}
}