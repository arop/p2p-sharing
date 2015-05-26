package server;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.security.KeyStore;
import java.util.ArrayList;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

import com.google.gson.Gson;

import user.User;
import database.UserDatabase;
import extra.Tools;

public class Server {

	UserDatabase user_db;

	ArrayList<User> onlineUsers;


	public ArrayList<User> getOnlineUsers() {
		this.refreshOnlineUsers();
		return onlineUsers;
	}

	public Server(){
		user_db = new UserDatabase();
		onlineUsers = new ArrayList<User>();
	}

	public boolean registerUser(String username, String password, String email, String ip, int port){
		String registerResponse;
		if (!(registerResponse = user_db.registerUser(username, email, password, ip, port)).equals("success")){
			System.out.println(registerResponse);
			return false;
		}
		return true;
	}

	/**
	 * 
	 * @param email
	 * @param password
	 * @return user id
	 */
	public User login(String email, String password){
		User user;
		if ((user = user_db.login(email, password)) == null){
			return null;
		}
		System.out.println("Login successful");
		return user;
	}

	/**
	 * Contacts each user individually (by communicating with 
	 * its last known ip/address), to check if it's online.
	 */
	public void refreshOnlineUsers(){
		ArrayList<User> users = user_db.getAllUsers(false);

		//clear online users
		onlineUsers.clear();

		for (User user : users) {
			String msg = Tools.generateMessage("ISONLINE", user.getId());
			String answer = this.sendMessage(msg, user.getIp(), user.getPort(),0);
			if (answer != null)
				if (Tools.getType(answer).equals("OK")){
					onlineUsers.add(user);
				}
		}		
	}

	public String getAllUsersEssencials(){
		ArrayList<User> users = user_db.getAllUsers(false);
		Gson gson = new Gson();
		String listOfUsers = gson.toJson(users);
		return listOfUsers;
	}

	public boolean addFriendsToUser(int user_id, int[] futureFriends) {
		boolean success = true; 
		for (int i = 0; i < futureFriends.length; i++){
			String msg = user_db.addFriend(user_id, futureFriends[i]);
			if (!msg.equals("success"))
				success = false;
		}
		return success;
	}

	public ArrayList<User> getFriendsOfUser(int user_id) {
		return user_db.getFriends(user_id);
	}

	

	public void updateUserAddress(int user_id, String address){
		user_db.updateLastIp(user_id, address);		
	}
	
	
	/**
	 * 
	 * @param msg
	 * @param ip_dest
	 * @param port_dest
	 * @return response from other peer/server
	 */
	public String sendMessage(String msg, String ip_dest, int port_dest, int connection_try_number){
		System.out.println("Sending message: "+Tools.getHead(msg));
		
		int timeout = 3000; //timeout in miliseconds

		SSLSocket sslSocket;

		PrintWriter out = null;
		BufferedReader in = null;
		String response = null;

		try {
			//sslSocket = (SSLSocket)sslsocketfactory.createSocket(ip_dest,port_dest);
			sslSocket = getSocketConnection(ip_dest, port_dest); 
			sslSocket.setSoTimeout(timeout);

			// Initializing the streams for Communication with the Server
			out = new PrintWriter(sslSocket.getOutputStream(), true);
			in = new BufferedReader(new InputStreamReader(sslSocket.getInputStream()));

			//SEND MESSAGE
			out.println(msg);

			//GET RESPONSE 
			response = in.readLine();
			response += in.readLine(); 				//TODO (isto está assim hardcoded pq o 
			response += "\r\n\r\n"+in.readLine(); 	//readLine lê até ao \r\n apenas)


			// Closing the Streams and the Socket
			out.close();
			in.close();
			sslSocket.close();		
		} 
		catch (Exception e){
			if (connection_try_number > 3){
				//e.printStackTrace();
				return null;
			}
			System.out.println("try: "+connection_try_number);
			return this.sendMessage(msg, ip_dest, port_dest,connection_try_number+1);
		}
		
		System.out.println("	answer: "+Tools.getHead(response));
		return response;
	}
	
	private SSLSocket getSocketConnection(String host, int port) {
	    try {
	        /* Create keystore */
	        KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
	        keyStore.load(new FileInputStream("..\\certificates\\server\\keystore"), "peerkey".toCharArray());

	        /* Get factory for the given keystore */
	        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
	        tmf.init(keyStore);
	        SSLContext ctx = SSLContext.getInstance("SSL");
	        ctx.init(null, tmf.getTrustManagers(), null);
	        SSLSocketFactory factory = ctx.getSocketFactory();
	        
	        return (SSLSocket) factory.createSocket(host, port);
	    } catch (Exception e) {
	    	System.out.println("Problem starting auth server: "+ e.getMessage()+"\n"+e.getCause());
	    	return null;
	    }	
	}
	
	public static void main(String[] args){
		Server server = new Server();

		server.registerUser("norim_13", "a", "norim_13@hotmail.com", "localhost", 4444);
		server.registerUser("norim_14", "a", "norim_14@hotmail.com", "localhost", 4445);

		server.login("norim_13@hotmail.com", "a"); //success

		server.login("norim@hotmail.com", "a"); //wrong email

		server.login("norim_13@hotmail.com", "b"); //wrong password

		ConnectionListenerServer con_listener = new ConnectionListenerServer(16500, server);
		con_listener.start();		
	}
}
