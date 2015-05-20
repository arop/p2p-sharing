package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.reflect.Type;
import java.security.PrivilegedActionException;
import java.security.Security;
import java.util.ArrayList;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;





import user.User;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
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
		
		SSLServerSocket sslServerSocket = null;
		SSLSocket sslSocket = null ;
		
		while(true){
			try {
				
				// Initialize the Server Socket
				SSLServerSocketFactory sslServerSocketfactory = (SSLServerSocketFactory)SSLServerSocketFactory.getDefault();
				sslServerSocket = (SSLServerSocket)sslServerSocketfactory.createServerSocket(this.port);
				sslSocket = (SSLSocket)sslServerSocket.accept();

				
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
					// TODO Auto-generated catch block
					System.out.println("erro no while read server");
					//e.printStackTrace();
				}
		        
			}
		}
		
	}
	
	/**
	 * parses received message, executes action, and generates response.
	 * @param message
	 * @return Response to received message. Null if not applicable.
	 */
	public String parseReceivedMessage(String message){
		String[] messageHeadParts = Tools.getHead(message).split(" +");
		int user_id;
		Gson gson = new Gson();;
		
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
			
			Type arrayListOfUsers = new TypeToken<ArrayList<User>>(){}.getType();
			String json_data = gson.toJson(userList, arrayListOfUsers);
			
			return Tools.generateJsonMessage("FRIENDS", json_data);
	
		default:
			break;				
		}
		return null;
	}
	
	
	
	
}
