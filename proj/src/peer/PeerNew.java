package peer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.reflect.Type;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.security.Security;
import java.util.ArrayList;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import server.ConnectionListenerServer;
import ui.loginFrame.LoginFrame;
import ui.mainFrame.GUI;
import user.User;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.sun.net.ssl.internal.ssl.Provider;

import extra.Tools;

public class PeerNew {
	private User localUser;
	private String serverAddress = "localhost";
	private int serverPort = 16500; 
	
	ArrayList<User> friends;
	
	public PeerNew(){
		
		
	}
	
	
	private static int connection_try_number = 0;
	
	private LoginFrame loginFrame;
	private GUI mainFrame;
	
	/**
	 * 
	 * @param msg
	 * @param ip_dest
	 * @param port_dest
	 * @return response from other peer/server
	 */
	public String sendMessage(String msg, String ip_dest, int port_dest){
		{
			// Registering the JSSE provider
			Security.addProvider(new Provider());
		}
		
		int timeout = 5000; //timeout in miliseconds
		
		SSLSocketFactory sslsocketfactory = (SSLSocketFactory)SSLSocketFactory.getDefault();
		SSLSocket sslSocket;
		
		PrintWriter out = null;
        BufferedReader in = null;
        String response = null;
        
		try {
			sslSocket = (SSLSocket)sslsocketfactory.createSocket(ip_dest,port_dest);
			sslSocket.setSoTimeout(timeout);
			
			// Initializing the streams for Communication with the Server
         	out = new PrintWriter(sslSocket.getOutputStream(), true);
         	in = new BufferedReader(new InputStreamReader(sslSocket.getInputStream()));

			//SEND MESSAGE
         	out.println(msg);
			
         	//GET RESPONSE 
			response = in.readLine();
			response += in.readLine(); //TODO (isto está assim hardcoded pq o 
			response += "\r\n\r\n"+in.readLine(); //readLine lê até ao \r\n apenas)
	
			//PARSE RESPONSE
			String origin_ip = sslSocket.getInetAddress().getHostAddress();
			//System.out.println("response: " + response + "#" + origin_ip);

			// Closing the Streams and the Socket
			out.close();
			in.close();
			sslSocket.close();		
			
		} 
		catch (SocketTimeoutException e){
			System.out.println("timeout");
			if (++connection_try_number > 3){
				connection_try_number = 0;
				//e.printStackTrace();
				return null;
			}
			System.out.println("try: "+connection_try_number);
		}
		catch (ConnectException e){
			if (++connection_try_number > 3){
				connection_try_number = 0;
				//e.printStackTrace();
				return null;
			}
			System.out.println("try: "+connection_try_number);
			return sendMessage(msg, ip_dest, port_dest);
		}
		catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		
		return response;
	}
	
	public ArrayList<User> getAllUsersFromServer(){
		String response = this.sendMessage(Tools.generateMessage("GETALLUSERS"), serverAddress, serverPort);
		Gson gson = new Gson();
		String type = response.split(" +")[0];
		if (!type.equals("USERS"))
			return null;
		
		String list_json = Tools.getBody(response);
		Type listOfUsersType = new TypeToken<ArrayList<User>>(){}.getType();
		return gson.fromJson(list_json, listOfUsersType);
		
	}
	
	public void getFriendsFromServer(){
		String response = this.sendMessage(Tools.generateMessage("GETFRIENDS", this.localUser.getId()), serverAddress, serverPort);
		Gson gson = new Gson();
		String type = Tools.getType(response);
		if (!type.equals("FRIENDS")){
			return;
		}			
		
		String list_json = Tools.getBody(response);
		Type listOfUsersType = new TypeToken<ArrayList<User>>(){}.getType();
		this.friends = gson.fromJson(list_json, listOfUsersType);
		
	}
	
	/**
	 * 
	 * @param user_ids array of ids if the users to add as friends.
	 */
	public boolean addFriends(int[] user_ids) {

		/*if (user_ids == null)
			System.out.println("nulosss");
		else System.out.println(user_ids[0]);*/
		
		Gson gson = new Gson();
		String json_data = gson.toJson(user_ids);
		String response = this.sendMessage(Tools.generateJsonMessage("ADDFRIENDS", localUser.getId(), json_data), serverAddress, serverPort);
		
		return Tools.getType(response).equals("OK");
	}
	
	
	public static void main(String[] args){
		PeerNew peer = new PeerNew();
		peer.startPeer();
		
	}
	
	public void startPeer(){
		
		/*loginFrame = new LoginFrame(this);
		while(!loginFrame.isSuccess()) {	
			//System.out.println(loginFrame.isSuccess());
		}
		loginFrame.dispose();*/
		
		User local = new User(1, "norim_13", "norim_13@hotmail.com",null,null, 4444);
		this.setLocalUser(local);
		this.friends = new ArrayList<User>();//initialize list

		this.getFriendsFromServer(); //update list with values from server

		GUI gui = new GUI(this);
		gui.setVisible(true);
		
		//connection listener -> thread always reading in user's port.
		ConnectionListenerPeer con_listener = new ConnectionListenerPeer(this);
		con_listener.start();
		
		
		
	}
	

	public User getLocalUser() {
		return localUser;
	}

	public void setLocalUser(User localUser) {
		this.localUser = localUser;
	}
	
	public ArrayList<User> getFriends(){
		return this.friends;
	}

	
	public boolean login(String username, String password) {
		String messagebody = username + " " + password;
		String response = this.sendMessage(Tools.generateJsonMessage("LOGIN",messagebody), serverAddress, serverPort);
		return Tools.getType(response).equals("OK");
	}
	
}
