package peer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.reflect.Type;
import java.security.Security;
import java.util.ArrayList;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import ui.mainFrame.GUI;
import user.User;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.sun.net.ssl.internal.ssl.Provider;

import extra.Tools;

public class PeerNew {

	/**
	 * 
	 * @param msg
	 * @param ip_dest
	 * @param port_dest
	 * @return response from other peer/server
	 */
	public String sendMessage(String msg, String ip_dest, int port_dest){
		{
			// Registering the JSSE provider
			Security.addProvider(new Provider());
		}
		
		SSLSocketFactory sslsocketfactory = (SSLSocketFactory)SSLSocketFactory.getDefault();
		SSLSocket sslSocket;
		PrintWriter out = null;
        BufferedReader in = null;
        String response = null;
        
		try {
			sslSocket = (SSLSocket)sslsocketfactory.createSocket(ip_dest,port_dest);
			
			
			// Initializing the streams for Communication with the Server
         	out = new PrintWriter(sslSocket.getOutputStream(), true);
         	in = new BufferedReader(new InputStreamReader(sslSocket.getInputStream()));

			//SEND MESSAGE
         	out.println(msg);
			
         	//GET RESPONSE 
			response = in.readLine();
			response += in.readLine(); //TODO (isto está assim hardcoded pq o 
			response += "\r\n\r\n"+in.readLine(); //readLine lê até ao \r\n apenas)
			
			//PARSE RESPONSE
			String origin_ip = sslSocket.getInetAddress().getHostAddress();
			//System.out.println("response: " + response + "#" + origin_ip);

			// Closing the Streams and the Socket
			out.close();
			in.close();
			sslSocket.close();		
			
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		
		return response;
	}
	
	public ArrayList<User> getAllUsersFromServer(){
		String response = this.sendMessage(Tools.generateMessage("GETALLUSERS"), "localhost", 16400);
		Gson gson = new Gson();
		String type = response.split(" +")[0];
		if (!type.equals("USERS"))
			return null;
		
		String list_json = Tools.getBody(response);
		Type listOfUsersType = new TypeToken<ArrayList<User>>(){}.getType();
		return gson.fromJson(list_json, listOfUsersType);
		
	}
	
	
	public static void main(String[] args){
		PeerNew peer = new PeerNew();
		//peer.sendMessage("Teste SSL backups!", "localhost", 16400);
		
		GUI gui = new GUI(peer);
		gui.setVisible(true);
		
	}
}
