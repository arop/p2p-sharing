package friends;

public class Friend {
	
	private int user_id;
	private String username;
	private boolean online; //true if user is online
	
	public Friend(int id, String un){
		user_id = id;
		username = un;
		online = true;
	}

	public String getUsername() {
		return username;
	}

	public int getId() {
		return user_id;
	}
	
	public boolean isOnline(){
		return online;
	}

}
