package peer;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.security.KeyStore;
import java.security.PrivilegedActionException;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManagerFactory;

import main.Chunk;
import extra.FileManagement;
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

					String inputLine = in.readLine();
					inputLine += in.readLine();
					inputLine+= "\r\n\r\n";
					inputLine += in.readLine();

					System.out.println("message received: \n	"+inputLine);
					String origin_ip = sslSocket.getInetAddress().getHostAddress();
					//PROCESS RECEIVED MESSAGE
					System.out.println("	from: "+origin_ip);
					String responseMessage = this.parseReceivedMessage(inputLine);
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
	 */
	public String parseReceivedMessage(String message) throws IOException{
		String[] messageHeadParts = Tools.getHead(message).split(" +");

		switch(messageHeadParts[0]) {
		case "ISONLINE":
			return Tools.generateMessage("OK");

		case "PUTCHUNK":
			Chunk temp = new Chunk(Tools.getBody(message).getBytes());
			
			splitMessage(temp,Tools.getHead(message));
			FileManagement.materializeChunk(temp);		
			
			return Tools.generateMessage("STORED", temp);

		default:
			break;				
		}
		return null;
	}
	
	void splitMessage(Chunk chunk, String message) {
		String[] temp = message.split(" +");
		chunk.setFileId(temp[2]);
		chunk.setChunkNo(Integer.parseInt(temp[3].trim()));
		chunk.setReplicationDeg(Integer.parseInt(temp[4].trim()));
	}
}
