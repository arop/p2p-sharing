package server;

import java.util.ArrayList;

import user.User;

public class SendPendingMsg extends Thread {
	ArrayList<String> msgs;
	User user;
	Server mainThread;
	
	public SendPendingMsg(ArrayList<String> msgs, User user, Server mainThread) {
		this.msgs = msgs;
		this.user = user;
		this.mainThread = mainThread;
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
			System.out.println(msg);
			String response = mainThread.sendMessage(msg+"\r\n\r\n", user.getIp(), user.getPort(), 0);
			
			if(response.contains("OK"))
				msgs.remove(msg);
		}
	}	
}
