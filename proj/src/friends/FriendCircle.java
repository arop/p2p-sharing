package friends;

import java.util.ArrayList;
import java.util.List;

import user.User;

public class FriendCircle {
	
	String name; //UNIQUE 
	ArrayList<Integer> memberIds = new ArrayList<Integer>(); //ids of friends in this circle
	
	public FriendCircle(String name, List<User> friends){
		this.name = name;
		for(User f : friends){
			memberIds.add(f.getId());
		}
	}
	
	public void addFriend(User f){
		this.memberIds.add(f.getId());
	}
	
	public void addFriends(List<User> friends){
		for(User f : friends){
			memberIds.add(f.getId());
		}
	}
	
	public void removeFriend(User f){
		this.memberIds.remove(f.getId());
	}

	public String getName() {
		return name;
	}
	
	
	
}
