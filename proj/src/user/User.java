package user;

public class User {

		
	private int user_id;
	private String username;
	private String email;
	private String password_hash;
	private String ip; //last known ip
	private int port; //port listening to messages
	private boolean online; //true if user is online - "volatile". should always be re-checked after some time
	
	
	public User(int id, String un, String em, String hash, String i, int p){
		user_id = id;
		username = un;
		email = em;
		password_hash = hash;
		ip = i;
		port = p;
		online = false;
	}
	
	public User(int id, String un){
		user_id = id;
		username = un;
		email = null;
		password_hash = null;
		ip = null;
		port = -1;
		online = true;
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

	public int getPort(){
		return port;
	}
	
	public boolean isOnline(){
		return online;
	}
	
	@Override
	public String toString() {
	   return "User [user_id=" + user_id + ", username=" + username + ", email="+ email + ", password_hash=" + password_hash + ", ip=" + ip + ", port="+port+"]";
	}


}
