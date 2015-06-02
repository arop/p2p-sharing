package send;

import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import main.Chunk;
import main.Peer;
import cli.Interface;
import extra.FileManagement;
import extra.Tools;

/**
 * Class used to run the menu, and send the messages according to the selected option 
 * @author André Pires, Filipe Gama
 *
 */
public class SendThread extends Thread {
	private static Peer main;
	private MulticastSocket senderSocket;

	public SendThread(Peer mainPeer) {
		main = mainPeer;
		try {
			senderSocket = new MulticastSocket();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		while(true) {
			ArrayList<String> output = Interface.menu();

			if(!output.get(1).equals("failed")) {
				switch(output.get(0)) {
				case "backup":				
					ArrayList<Chunk> chunks = new ArrayList<Chunk>();
					String fId = null;

					int repDegree = Integer.parseInt(output.get(2));
					try {
						chunks = FileManagement.splitFile(output.get(1),repDegree);
						fId = FileManagement.getFileId(output.get(1));

						main.addToBackupList(output.get(1),fId,chunks.size());
						main.refreshBackupList();

						main.setBackuping(true);

						for (Chunk chunk : chunks) {
							main.getDegMonitor().addToMap("send", chunk);
						}

						for (Chunk chunk : chunks) {
							int i = 1;
							do {								
								sendMessage(Tools.generateMessage("PUTCHUNK", chunk), main.getMDB_IP(), main.getMDB_Port());
								//FileManagement.saveMapToFile(main.getDegMonitor().getSentMap(), "files\\lists\\degreeListSent.txt");
								Thread.sleep(1000);
								i++;
							} while (isWaitingForConfirmation(chunk.getFileId(), chunk.getChunkNo(),chunk.getReplicationDeg(),i) && i < 6);

							main.cleanPorts();

							if(i > 5) {
								if(Tools.isDebug())
									System.err.println("Timeout. Couldn't store the chunk in the required replication degree");
								break;
							}
						}

						main.setBackuping(false);
					}
					catch(Exception e) {
						e.getMessage();
					}
					break;

				case "restore":
					if(!main.isBackedUp(output.get(1))) {
						System.err.println("No file with that name ("+ output.get(1) + ") backed up!");
						break;
					}

					main.startNewRcv_MDR();
					main.clearReceivedChunks();
					sendGetChunks(output.get(1));
					break;

				case "delete":
					if(!main.isBackedUp(output.get(1))) {
						System.err.println("No file with that name ("+ output.get(1) + ") backed up!");
						break;
					}

					String fileId = main.getFileIdOf(output.get(1));
					sendMessage(Tools.generateMessage("DELETE", fileId), main.getMC_IP(), main.getMC_Port());
					try {
						main.deleteFromSentLists(fileId);
						main.refreshBackupList();
					} catch (IOException e1) {
						e1.printStackTrace();
					}
					break;

				case "reclaim":
					Tools.setFolderSize(Integer.parseInt(output.get(1)));
					try {
						while(Tools.getFolderSize() < Tools.folderSize(new File("files\\backups"))) {
							main.deleteWorstChunk();
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
					break;

				case "debug":
					Tools.setDebug(!Tools.isDebug());
					break;

				case "exit":
					try {
						main.refreshBackupList();
						FileManagement.saveMapToFile(main.getDegMonitor().getSentMap(), "files\\lists\\degreeListSent.txt");
						FileManagement.saveMapToFile(main.getDegMonitor().getStoredMap(), "files\\lists\\degreeListStored.txt");
					} catch (IOException e) {
						e.printStackTrace();
					}
					System.exit(0);

				default:
					System.err.println("Invalid command!");
					break;
				}
			}
			else {
				System.err.println("File does not exist!");
			}
		}
	}

	private boolean isWaitingForConfirmation(String fileId, int chunkNo, int replicationDegree, int i) throws InterruptedException {
		Thread.sleep(500*i);
		return main.getDegMonitor().getConfirmations(fileId, chunkNo, main.getDegMonitor().getSentMap()) < replicationDegree;
	}

	public MulticastSocket getSenderSocket() {
		return senderSocket;
	}

	/**
	 * Sends "getchunk" for all chunks of a file 
	 * @param filename
	 */
	public void sendGetChunks(String filename) {
		main.setChunksToReceive(main.getFileNumberChunks(filename));
		main.setFileToRecover(filename);

		for(int i = 0; i < main.getFileNumberChunks(filename); i++) {
			sendMessage(Tools.generateMessage("GETCHUNK",main.getFileIdOf(filename),i), main.getMC_IP(),main.getMC_Port());
		}
	}

	/**
	 * Sends a message to the desired channel
	 * @param msg
	 * @param ip
	 * @param port
	 */
	public void sendMessage(String msg, String ip, int port) {
		byte[] toSendB = msg.getBytes(StandardCharsets.ISO_8859_1);

		try {
			DatagramPacket packet;

			//send response
			InetAddress addr = InetAddress.getByName(ip);
			packet = new DatagramPacket(toSendB, toSendB.length, addr, port);
			senderSocket.send(packet);
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
}
