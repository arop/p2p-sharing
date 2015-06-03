package server;

import java.util.ArrayList;

import user.User;
import extra.Tools;

public class CheckOnlineThread extends Thread {
	Server server;
	ArrayList<User> onlineUsers;

	public CheckOnlineThread(Server server) {
		this.server = server;
		onlineUsers = new ArrayList<User>();
	}

	public ArrayList<User> getOnlineUsers() {
		//this.refreshOnlineUsers();
		return onlineUsers;
	}

	/**
	 * Contacts each user individually (by communicating with 
	 * its last known ip/address), to check if it's online.
	 */
	public void refreshOnlineUsers(){
		ArrayList<User> users = server.getdb().getAllUsers(false);

		//clear online users
		onlineUsers.clear();

		for (User user : users) {
			String msg = Tools.generateMessage("ISONLINE", user.getId());
			String answer = server.sendMessage(msg, user.getIp(), user.getPort(),3); //dont try more than 1 time
			if (answer != null)
				if (Tools.getType(answer).equals("OK")) {
					onlineUsers.add(user);
				}
		}		
	}

	/**
	 * 
	 */
	@Override
	public void run() {
		while(true) {
			try {
				Thread.sleep(10000);
				refreshOnlineUsers();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

}
