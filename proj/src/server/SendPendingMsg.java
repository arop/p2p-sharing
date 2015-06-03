package server;

import java.util.ArrayList;

import user.User;

public class SendPendingMsg extends Thread {
	ArrayList<String> msgs;
	User user;
	Server mainThread;
	ConnectionListenerServer listener;
	
	public SendPendingMsg(ArrayList<String> msgs, User user, Server mainThread, ConnectionListenerServer listener) {
		this.msgs = msgs;
		this.user = user;
		this.mainThread = mainThread;
		this.listener = listener;
	}

	@Override
	public void run() {
		super.run();
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		for(String msg : new ArrayList<String>(msgs)) {
			String response = mainThread.sendMessage(msg+"\r\n\r\n", user.getIp(), user.getPort(), 0);
			
			if(response.contains("OK"))
				msgs.remove(msg);
		}
		
		listener.removePendingMsg(user,msgs);
	}	
}
