package user;

public class User {

		
	private int user_id;
	private String username;
	private String email;
	private String password_hash;
	private String ip; //last known ip
	
	public User(int id, String un, String em, String hash, String i){
		user_id = id;
		username = un;
		email = em;
		password_hash = hash;
		ip = i;
	}

	public String getUsername() {
		return username;
	}

	public int getId() {
		return user_id;
	}
	
	public String getIp(){
		return ip;
	}

	public String getEmail() {
		return email;
	}
	
	public String getPasswordHash(){
		return password_hash;
	}



}
