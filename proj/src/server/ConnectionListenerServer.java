package server;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.reflect.Type;
import java.security.KeyStore;
import java.security.PrivilegedActionException;
import java.util.ArrayList;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;

import javax.net.ssl.TrustManagerFactory;

import user.User;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

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

		SSLServerSocket sslServerSocket = null;
		SSLSocket sslSocket = null ;

		while(true){
			try {
				
				sslServerSocket = getServerSocket(this.port);
				if (sslServerSocket == null){
					System.out.println("Failed to create socket. Port: "+this.port);
					//System.exit(0);
				}
				else{
					sslSocket = (SSLSocket)sslServerSocket.accept();
					
					
					// Create Input / Output Streams for communication with the client
					PrintWriter out = new PrintWriter(sslSocket.getOutputStream(), true); //vai responder por aqui
					BufferedReader in = new BufferedReader(new InputStreamReader(sslSocket.getInputStream())); //lê daqui

					String inputLine = in.readLine();
					inputLine += in.readLine();
					inputLine+= "\r\n\r\n";
					inputLine += in.readLine();

					System.out.println("message received: "+Tools.getHead(inputLine));
					String origin_ip = sslSocket.getInetAddress().getHostAddress();
					//PROCESS RECEIVED MESSAGE
					System.out.println("	from: "+origin_ip);
					String responseMessage = this.parseReceivedMessage(inputLine, origin_ip);
					
					//SEND RESPONSE
					out.println(responseMessage);
					//System.out.println("Answer sent!");
					// Close the streams and the socket
					out.close();
					in.close();
					//sslSocket.close();
					sslServerSocket.close();	
				}
				

			}
			catch(Exception exp)
			{
				PrivilegedActionException priexp = new PrivilegedActionException(exp);
				System.out.println(" Priv exp --- " + priexp.getMessage());
				System.out.println(" Exception occurred .... " +exp);
				exp.printStackTrace();

				try {
					//sslSocket.close();
					sslServerSocket.close();
				} catch (IOException e) {
					System.out.println("erro no while read server");
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
	 * @param sourceAddress Address where the message was sent from.
	 * @return Response to received message. Null if not applicable.
	 */
	public String parseReceivedMessage(String message, String sourceAddress){
		String[] messageHeadParts = Tools.getHead(message).split(" +");
		int user_id;
		Gson gson = new Gson();;
		Type arrayListOfUsers = new TypeToken<ArrayList<User>>(){}.getType();
		
		switch(messageHeadParts[0]) {
		case "GETALLUSERS":
			//GETALLUSERS <Version> <CRLF><CRLF>
			return Tools.generateJsonMessage("USERS", this.mainThread.getAllUsersEssencials());
		case "ADDFRIENDS": 
			//ADDFRIENDS <Version> <User id> <CRLF><CRLF> JSON of int[] users ids

			user_id = Integer.parseInt(messageHeadParts[2]);

			Type arrayOfIntsType = new TypeToken<int[]>(){}.getType();
			int[] users_ids = gson.fromJson(Tools.getBody(message), arrayOfIntsType);

			if (this.mainThread.addFriendsToUser(user_id, users_ids))
				return Tools.generateMessage("OK");
			return Tools.generateMessage("NOTOK");

		case "GETFRIENDS": 
			//ADDFRIENDS <Version> <User id> <CRLF><CRLF> JSON of int[] users ids
			user_id = Integer.parseInt(messageHeadParts[2]);

			ArrayList<User> userList = this.mainThread.getFriendsOfUser(user_id);

			String json_data = gson.toJson(userList, arrayListOfUsers);

			return Tools.generateJsonMessage("FRIENDS", json_data);
		
		case "GETONLINEUSERS": 
			//ADDFRIENDS <Version> <User id> <CRLF><CRLF> JSON of int[] users ids
			ArrayList<User> userList1 = this.mainThread.getOnlineUsers();
			return Tools.generateJsonMessage("ONLINEUSERS", gson.toJson(userList1, arrayListOfUsers));

		case "LOGIN":
			String[] loginparts = Tools.getBody(message).split(" ");
			
			System.out.println("Username: " + loginparts[0]);
			System.out.println("Password: " + loginparts[1]);
			
			User user = null;
			if( (user = mainThread.login(loginparts[0],loginparts[1])) != null){
				mainThread.updateUserAddress(user.getId(), sourceAddress);
				return Tools.generateJsonMessage("OK",gson.toJson(user));
			}
				
			return Tools.generateMessage("NOTOK");
		
			
		case "REGISTER":
			String[] registerParts = Tools.getBody(message).split(" ");

			System.out.println("Username: " + registerParts[0]);
			System.out.println("Email: " + registerParts[1]);
			System.out.println("Password: " + registerParts[2]);
			System.out.println("Port: " + registerParts[3]);

			
			int temp = Integer.parseInt(registerParts[3]);

			if(mainThread.registerUser(registerParts[0],registerParts[1],registerParts[2],sourceAddress,temp))
				return Tools.generateMessage("OK");
			return Tools.generateMessage("NOTOK");

		default:
			break;				
		}
		return null;
	}
}
