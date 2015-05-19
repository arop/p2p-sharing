package main;

import database.UserDatabase;

public class Server {
	
	UserDatabase user_db;
	
	public Server(){
		user_db = new UserDatabase();
	}
	
	public void registerUser(String username, String password, String email, String ip){
		
		String registerResponse;
		if (!(registerResponse = user_db.registerUser(username, email, password, ip)).equals("success")){
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
	
	
	public static void main(String[] args){
		Server server = new Server();
		
		server.registerUser("norim_13", "a", "norim_13@hotmail.com", "");

		server.login("norim_13@hotmail.com", "a");
		
		server.login("norim@hotmail.com", "a");
		
		server.login("norim_13@hotmail.com", "b");
		
	}
	
}
