package peer;

import java.lang.reflect.Type;
import java.util.ArrayList;

import ui.loginFrame.LoginFrame;
import ui.mainFrame.GUI;
import user.User;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import extra.Tools;

public class PeerNew {
	private User localUser;
	private String serverAddress = "localhost";
	private int serverPort = 16500; 

	ArrayList<User> friends;

	public PeerNew() {}

	private LoginFrame loginFrame;
	private GUI mainFrame;

	public ArrayList<User> getAllUsersFromServer(){
		String response = Tools.sendMessage(Tools.generateMessage("GETALLUSERS"), serverAddress, serverPort,0);
		Gson gson = new Gson();
		String type = response.split(" +")[0];
		if (!type.equals("USERS"))
			return null;

		String list_json = Tools.getBody(response);
		Type listOfUsersType = new TypeToken<ArrayList<User>>(){}.getType();
		return gson.fromJson(list_json, listOfUsersType);
	}

	public void getFriendsFromServer(){
		String response = Tools.sendMessage(Tools.generateMessage("GETFRIENDS", this.localUser.getId()), serverAddress, serverPort,0);
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
		String response = Tools.sendMessage(Tools.generateJsonMessage("ADDFRIENDS", localUser.getId(), json_data), serverAddress, serverPort,0);

		return Tools.getType(response).equals("OK");
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
		String response = Tools.sendMessage(Tools.generateJsonMessage("LOGIN",messagebody), serverAddress, serverPort,0);
		return Tools.getType(response).equals("OK");
	}

	public static void main(String[] args){
		PeerNew peer = new PeerNew();
		peer.startPeer();
	}

	public void startRegularBackupProtocol(String filePath) {
		//1 -> get online users IPs and ports form server
		
		//2 -> send all chunks to each user (?)
		
		
		
	}
}
