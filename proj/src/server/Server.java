package server;

import java.util.ArrayList;

import com.google.gson.Gson;

import user.User;
import database.UserDatabase;

public class Server {
	
	UserDatabase user_db;
	
	public Server(){
		user_db = new UserDatabase();
	}
	
	public void registerUser(String username, String password, String email, String ip, int port){
		
		String registerResponse;
		if (!(registerResponse = user_db.registerUser(username, email, password, ip, port)).equals("success")){
			System.out.println(registerResponse);
		}
	}
	
	public boolean login(String email, String password){
		String response;
		if (!(response = user_db.login(email, password)).equals("success")){
			System.out.println(response);
			return false;
		}
		System.out.println("Login successful");
		return true;
	}
	
	
	public boolean checkOnlineUsers(){
		
		ArrayList<User> users = user_db.getAllUsers(true);
		
		return false;		
	}
	
	public String getAllUsersEssencials(){
		ArrayList<User> users = user_db.getAllUsers(false);
		Gson gson = new Gson();
		String listOfUsers = gson.toJson(users);
		return listOfUsers;
	}
	
	
	public boolean sendPacket(String msg, String ip_dest, String port_dest){
		
		return false;
	}
	
	public static void main(String[] args){
		Server server = new Server();
		
		server.registerUser("norim_13", "a", "norim_13@hotmail.com", "", 4444);
		server.registerUser("norim_14", "a", "norim_14@hotmail.com", "", 4444);

		server.login("norim_13@hotmail.com", "a"); //success
		
		server.login("norim@hotmail.com", "a"); //wrong email
		
		server.login("norim_13@hotmail.com", "b"); //wrong password
		
		ConnectionListenerServer con_listener = new ConnectionListenerServer(16400, server);
		con_listener.start();
		
	}
	
}
