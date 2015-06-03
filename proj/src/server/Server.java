package server;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.security.KeyStore;
import java.util.ArrayList;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

import com.google.gson.Gson;

import user.User;
import database.UserDatabase;
import extra.Tools;

public class Server {

	UserDatabase user_db;

	public Server(){
		user_db = new UserDatabase();		
	}

	public boolean registerUser(String username, String password, String email, String ip, int port){
		String registerResponse;
		if (!(registerResponse = user_db.registerUser(username, email, password, ip, port)).equals("success")){
			System.out.println(registerResponse);
			return false;
		}
		return true;
	}

	public User login(String email, String password){
		User user;
		if ((user = user_db.login(email, password)) == null){
			return null;
		}
		System.out.println("Login successful");
		return user;
	}

	public User loginFacebook(String username, long fb_id, String address, int port){
		User user;
		if ((user = user_db.getUserFacebookByFacebookId(fb_id)) == null){
			user_db.registerUserFacebook(username, fb_id, address, port);
			if ((user = user_db.getUserFacebookByFacebookId(fb_id)) == null){
				return null;
			}
			return user;
		}
		System.out.println("Login successful");
		return user;
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
	
	public void updateUserPort(int user_id, int port){
		user_db.updateUserPort(user_id, port);		
	}

	/**
	 * Sends a message
	 * @param msg
	 * @param ip_dest
	 * @param port_dest
	 * @return response from other peer/server
	 */
	public String sendMessage(String msg, String ip_dest, int port_dest, int connection_try_number){
		System.out.println("Sending message: " + Tools.getHead(msg));

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

	public SSLServerSocket getServerSocket(int socket_port) {
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

	public UserDatabase getdb() {
		return user_db;
	}

	public static void main(String[] args){
		Server server = new Server();

		CheckOnlineThread checkOnlineThread = new CheckOnlineThread(server);
		ConnectionListenerServer con_listener = new ConnectionListenerServer(16500, server,checkOnlineThread);
		checkOnlineThread.start();
		con_listener.start();
	}
}
