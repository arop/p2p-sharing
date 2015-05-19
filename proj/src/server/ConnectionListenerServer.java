package server;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.security.PrivilegedActionException;
import java.security.Security;
import java.util.Arrays;
import java.util.Random;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;

import main.Chunk;
import peer.PeerNew;

import com.sun.net.ssl.internal.ssl.Provider;

import extra.Tools;

public class ConnectionListenerServer extends Thread{
	private int port; // Port where the SSL Server needs to listen for new requests from the client
	private Server mainThread;
	
	public ConnectionListenerServer(int port, Server server){
		this.port = port;
		this.mainThread = server;
	}
	
	@Override
	public void run() {
		{
			// Registering the JSSE provider
			Security.addProvider(new Provider());
	
			//Specifying the Keystore details
			System.setProperty("javax.net.ssl.keyStore","..\\certificates\\keystore");
			System.setProperty("javax.net.ssl.keyStorePassword","sida123");
	
			// Enable debugging to view the handshake and communication which happens between the SSLClient and the SSLServer
			// System.setProperty("javax.net.debug","all");
		}
		
		while(true){
			try {
				
				// Initialize the Server Socket
				SSLServerSocketFactory sslServerSocketfactory = (SSLServerSocketFactory)SSLServerSocketFactory.getDefault();
				SSLServerSocket sslServerSocket = (SSLServerSocket)sslServerSocketfactory.createServerSocket(port);
				SSLSocket sslSocket = (SSLSocket)sslServerSocket.accept();
		
				// Create Input / Output Streams for communication with the client
					
				PrintWriter out = new PrintWriter(sslSocket.getOutputStream(), true); //vai responder por aqui
		        BufferedReader in = new BufferedReader(new InputStreamReader(sslSocket.getInputStream())); //lê daqui
		        
		        
		        /*String inputLine;
		         String message = "";
		        while ((inputLine = in.readLine()) != null) {
		            message += inputLine; 
		        	out.println(inputLine);
		            System.out.println("message received: \n	"+inputLine);
		        }*/
		        
		        String inputLine = in.readLine();
		        System.out.println("message received: \n	"+inputLine);
		        String origin_ip = sslSocket.getInetAddress().getHostAddress();

		        //PROCESS RECEIVED MESSAGE
		        System.out.println("	from: "+origin_ip);
		        String responseMessage = this.parseReceivedMessage(inputLine);
		        
		        //SEND RESPONSE
		        out.println(responseMessage);
		        
		        // Close the streams and the socket
		        out.close();
		        in.close();
		        sslSocket.close();
		        sslServerSocket.close();	
				
			}
			catch(Exception exp)
			{
				PrivilegedActionException priexp = new PrivilegedActionException(exp);
				System.out.println(" Priv exp --- " + priexp.getMessage());
				System.out.println(" Exception occurred .... " +exp);
				exp.printStackTrace();
			}
		}
		
	}
	
	/**
	 * 
	 * @param message
	 * @return Response to received message. Null if not applicable.
	 */
	public String parseReceivedMessage(String message){
		switch(message.split(" +")[0]) {
		case "GETALLUSERS":
			//GETALLUSERS <Version> <CRLF><CRLF>
			return Tools.generateJsonMessage("USERS", this.mainThread.getAllUsersEssencials());

		default:
			break;				
		}
		return null;
	}
	
	
	
	
}
