package server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.reflect.Type;
import java.security.PrivilegedActionException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLSocket;

import user.User;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import extra.FileManagement;
import extra.StateMachine;
import extra.Tools;

public class ConnectionListenerServer extends Thread{
	private int port; // Port where the SSL Server needs to listen for new requests from the client
	private Server mainThread;
	private CheckOnlineThread checkOnlineThread;

	private Map<Integer,ArrayList<String>> pendingMessages;

	public ConnectionListenerServer(int port, Server server, CheckOnlineThread cot){
		this.port = port;
		this.mainThread = server;
		this.checkOnlineThread = cot;

		pendingMessages = new HashMap<Integer,ArrayList<String>>();
		loadNotRespondMapFile();
	}

	@Override
	public void run() {
		SSLServerSocket sslServerSocket = null;
		SSLSocket sslSocket = null ;

		while(true){
			try {
				sslServerSocket = mainThread.getServerSocket(this.port);
				if (sslServerSocket == null){
					System.out.println("Failed to create socket. Port: "+this.port);
				}
				else{
					sslSocket = (SSLSocket)sslServerSocket.accept();

					// Create Input / Output Streams for communication with the client
					PrintWriter out = new PrintWriter(sslSocket.getOutputStream(), true); //vai responder por aqui
					BufferedReader in = new BufferedReader(new InputStreamReader(sslSocket.getInputStream())); //lê daqui

//					String inputLine = in.readLine();
//					inputLine += in.readLine();
//					inputLine+= "\r\n\r\n";
//					inputLine += in.readLine();
					
					//GET RESPONSE 
//					ArrayList<String> messages = new ArrayList<String>();
//					messages.add("ADDFRIENDS");
//					messages.add("REGISTER");
//					messages.add("LOGIN");
//					messages.add("GETUSER");
					
					String inputLine = new StateMachine().stateMachine(in);

					
				
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
					sslServerSocket.close();
				} catch (IOException e) {
					System.out.println("erro no while read server");
				}
			}
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
		Gson gson = new Gson();
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
			ArrayList<User> userList1 = checkOnlineThread.getOnlineUsers();
			return Tools.generateJsonMessage("ONLINEUSERS", gson.toJson(userList1, arrayListOfUsers));

		case "LOGIN":
			String[] loginparts = Tools.getBody(message).split(" ");

			System.out.println("Username: " + loginparts[0]);
			System.out.println("Password: " + loginparts[1]);

			User user = null;
			if( (user = mainThread.login(loginparts[0],loginparts[1])) != null){
				mainThread.updateUserAddress(user.getId(), sourceAddress);

				sendPendingMessages(user);

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

		case "GETUSER":
			User u = mainThread.user_db.getUserById(Integer.parseInt(Tools.getBody(message)));
			String userJson = gson.toJson(u);
			return Tools.generateJsonMessage("USER", userJson);

		case "NOTRESPOND":
			saveNotRespondMessage(Tools.getHead(message));
			break;

		default:
			break;				
		}
		return null;
	}

	private void sendPendingMessages(User user) {
		ArrayList<String> msgs = pendingMessages.get(user.getId());

		if(msgs != null)
			new SendPendingMsg(msgs,user,mainThread,this).start();	
	}

	private void saveNotRespondMessage(String string) {
		//"NOTRESPOND DELETE " + Tools.getVersion() + " " + fileId + " " + u.getId() + "\r\n\r\n";
		String msg = string.substring(11); //remove NOTRESPOND
		int userID = Integer.parseInt(msg.split(" +")[3]);
		if(pendingMessages.containsKey(userID))
			pendingMessages.get(userID).add(msg);
		else {
			ArrayList<String> temp = new ArrayList<String>();
			temp.add(msg);
			pendingMessages.put(userID, temp);
		}
		refreshNotRespondMapFile();
	}

	private void refreshNotRespondMapFile() {
		PrintWriter f0;
		try {
			f0 = new PrintWriter(new FileWriter("..\\pending_messages\\messages.txt"));
			for(Entry<Integer,ArrayList<String>> e: pendingMessages.entrySet()) {
				for(String msg : e.getValue())
					f0.println(msg);
			}
			f0.close();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}

	public void loadNotRespondMapFile() {
		String file = "..\\pending_messages\\messages.txt";
		try {
			if(!FileManagement.fileExists(file))
				(new File(file)).createNewFile();
			else {
				try(BufferedReader br = new BufferedReader(new FileReader(new File(file)))) {
					for(String line; (line = br.readLine()) != null; ) {
						if(!line.startsWith("DELETE")) break;
						int userID = Integer.parseInt(line.split(" +")[3]);

						if(pendingMessages.containsKey(userID))
							pendingMessages.get(userID).add(line);
						else {
							ArrayList<String> temp = new ArrayList<String>();
							temp.add(line);
							pendingMessages.put(userID, temp);
						}
					}
					br.close();
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void removePendingMsg(User u, ArrayList<String> msg) {
		pendingMessages.put(u.getId(),msg);
		refreshNotRespondMapFile();
	}
}