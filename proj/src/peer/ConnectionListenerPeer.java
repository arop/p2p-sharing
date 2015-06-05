package peer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.security.PrivilegedActionException;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManagerFactory;

import com.google.gson.Gson;

import main.Chunk;
import extra.FileManagement;
import extra.StateMachine;
import extra.Tools;

public class ConnectionListenerPeer extends Thread {
	private PeerNew mainThread;

	public ConnectionListenerPeer(PeerNew peer){
		this.mainThread = peer;
	}

	@Override
	public void run() {
		SSLServerSocket sslServerSocket = null;
		SSLSocket sslSocket = null ;

		while(true){

			try {
				sslServerSocket = getServerSocket(this.mainThread.getLocalUser().getPort());
				if (sslServerSocket == null){
					System.out.println("Failed to open socket! Port: " + mainThread.getLocalUser().getPort());
				}
				else {
					sslSocket = (SSLSocket)sslServerSocket.accept();

					PrintWriter out = new PrintWriter(sslSocket.getOutputStream(), true); //vai responder por aqui
					BufferedReader in = new BufferedReader(new InputStreamReader(sslSocket.getInputStream())); //lê daqui

					String finalString = new StateMachine().stateMachine(in);

					System.out.println("message received: \n	" + Tools.getHead(finalString));
					String origin_ip = sslSocket.getInetAddress().getHostAddress();
					//PROCESS RECEIVED MESSAGE
					System.out.println("	from: "+origin_ip);
					String responseMessage = this.parseReceivedMessage(finalString);
					//SEND RESPONSE
					out.println(responseMessage);
					System.out.println("Answer sent!");
					// Close the streams and the socket
					out.close();
					in.close();
					sslSocket.close();
					sslServerSocket.close();	
				}
			}
			catch(Exception exp)
			{
				PrivilegedActionException priexp = new PrivilegedActionException(exp);
				System.out.println(" Priv exp --- " + priexp.getMessage());
				System.out.println(" Exception occurred .... " +exp);
				//exp.printStackTrace();

				try {
					sslSocket.close();
					sslServerSocket.close();
				} catch (IOException e) {
					System.out.println("erro no while read server");
					//e.printStackTrace();
				}	        
			}
		}
	}

	private SSLServerSocket getServerSocket(int socket_port) {
		try {    	
			/* Create keystore */
			KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
			keyStore.load(new FileInputStream("..\\certificates\\server\\keystore"), "peerkey".toCharArray());

			KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
			kmf.init(keyStore, "peerkey".toCharArray()); // That's the key's password, if different.

			/* Get factory for the given keystore */
			TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
			tmf.init(keyStore);

			SSLContext ctx = SSLContext.getInstance("SSL");
			//ctx.init(null, tmf.getTrustManagers(), null);
			ctx.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);

			SSLServerSocketFactory factory = ctx.getServerSocketFactory();

			return (SSLServerSocket) factory.createServerSocket(socket_port);
		} catch (Exception e) {
			System.out.println("Problem creating SSL Server Socket: "+ e.getMessage()+"\n"+e.getCause());
			return null;
		}	    
	}

	/**
	 * parses received message, executes action, and generates response.
	 * @param message
	 * @return Response to received message. Null if not applicable.
	 * @throws IOException 
	 * @throws InterruptedException 
	 */
	public String parseReceivedMessage(String message) throws IOException, InterruptedException{
		String[] messageHeadParts = Tools.getHead(message).split(" +");

		switch(messageHeadParts[0]) {
		case "ISONLINE":
			return Tools.generateMessage("OK");

		case "PUTCHUNK":
			Chunk temp = new Chunk(Tools.getBody(message).getBytes());
			splitMessage(temp,Tools.getHead(message));
			
			if(!mainThread.hasChunk(temp.getFileId(), temp.getChunkNo())) {
				if(Tools.folderSize(new File("files\\backups")) + temp.getByteArray().length-1 <= Tools.getFolderSize() ) {
					FileManagement.materializeChunk(temp);
					mainThread.clearChunkList();
					mainThread.loadChunkList();
					return Tools.generateMessage("STORED", temp);
				}
			}
			else return Tools.generateMessage("STORED", temp);

		case "DELETE":
			if(mainThread.deleteChunksOfFile(messageHeadParts[2]))
				return Tools.generateMessage("OK");
			else return Tools.generateMessage("NOTOK");

		case "BACKUPFILE":
			try{
				int friend_id = Integer.parseInt(messageHeadParts[2]);
				String[] bodySplit = Tools.getBody(message).split("#");

				int port = mainThread.startFileShareReceiveThread(friend_id, bodySplit[0], Long.parseLong(bodySplit[1]));
				return Tools.generateJsonMessage("OK", String.valueOf(port));
			}
			catch (ArrayIndexOutOfBoundsException e) {
				return Tools.generateMessage("NOTOK");
			}

		case "GETCHUNK":
			//GETCHUNK <Version> <FileId> <ChunkNo> <CRLF><CRLF>

			String fileId = messageHeadParts[2];
			int chunkNo = Integer.parseInt(messageHeadParts[3]);

			Chunk c = null;

			if(mainThread.hasChunk(fileId, chunkNo)) {
				c = mainThread.searchChunk(fileId,chunkNo);
				return Tools.generateMessage("CHUNK",c);
			}
		default:
			break;				
		}
		return null;
	}

	void splitMessage(Chunk chunk, String message) {
		String[] temp = message.split(" +");
		chunk.setFileId(temp[2]);
		chunk.setChunkNo(Integer.parseInt(temp[3].trim()));
		if(temp.length > 4) chunk.setReplicationDeg(Integer.parseInt(temp[4].trim()));
	}
}
