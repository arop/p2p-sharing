package friends;

import java.util.ArrayList;
import java.util.List;

public class FriendCircle {
	
	String name; //UNIQUE 
	ArrayList<Integer> memberIds = new ArrayList<Integer>(); //ids of friends in this circle
	
	public FriendCircle(String name, List<Friend> friends){
		this.name = name;
		for(Friend f : friends){
			memberIds.add(f.getId());
		}
	}
	
	public void addFriend(Friend f){
		this.memberIds.add(f.getId());
	}
	
	public void addFriends(List<Friend> friends){
		for(Friend f : friends){
			memberIds.add(f.getId());
		}
	}
	
	public void removeFriend(Friend f){
		this.memberIds.remove(f.getId());
	}

	public String getName() {
		return name;
	}
	
	
	
}
