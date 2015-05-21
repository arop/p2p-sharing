package peer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.security.PrivilegedActionException;
import java.security.Security;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;

import com.sun.net.ssl.internal.ssl.Provider;

import extra.Tools;

public class ConnectionListenerPeer extends Thread{
	private PeerNew mainThread;

	public ConnectionListenerPeer(PeerNew peer){
		this.mainThread = peer;
	}

	@Override
	public void run() {
		{
			// Registering the JSSE provider
			Security.addProvider(new Provider());

			//Specifying the Keystore details
			System.setProperty("javax.net.ssl.keyStore","..\\certificates\\peer\\keystore");
			System.setProperty("javax.net.ssl.keyStorePassword","serverkey");

			// Enable debugging to view the handshake and communication which happens between the SSLClient and the SSLServer
			// System.setProperty("javax.net.debug","all");
		}

		SSLServerSocket sslServerSocket = null;
		SSLSocket sslSocket = null ;

		while(true){
			try {

				// Initialize the Server Socket
				SSLServerSocketFactory sslServerSocketfactory = (SSLServerSocketFactory)SSLServerSocketFactory.getDefault();
				sslServerSocket = (SSLServerSocket)sslServerSocketfactory.createServerSocket(this.mainThread.getLocalUser().getPort());
				sslSocket = (SSLSocket)sslServerSocket.accept();

				PrintWriter out = new PrintWriter(sslSocket.getOutputStream(), true); //vai responder por aqui
				BufferedReader in = new BufferedReader(new InputStreamReader(sslSocket.getInputStream())); //lê daqui

				String inputLine = in.readLine();
				inputLine += in.readLine();
				inputLine+= "\r\n\r\n";
				inputLine += in.readLine();

				System.out.println("message received: \n	"+inputLine);
				String origin_ip = sslSocket.getInetAddress().getHostAddress();
				//PROCESS RECEIVED MESSAGE
				System.out.println("	from: "+origin_ip);
				String responseMessage = this.parseReceivedMessage(inputLine);
				//SEND RESPONSE
				out.println(responseMessage);
				System.out.println("Answer sent!");
				// Close the streams and the socket
				out.close();
				in.close();
				sslSocket.close();
				sslServerSocket.close();	

			}
			catch(Exception exp)
			{
				PrivilegedActionException priexp = new PrivilegedActionException(exp);
				System.out.println(" Priv exp --- " + priexp.getMessage());
				System.out.println(" Exception occurred .... " +exp);
				//exp.printStackTrace();

				try {
					sslSocket.close();
					sslServerSocket.close();
				} catch (IOException e) {
					System.out.println("erro no while read server");
					//e.printStackTrace();
				}	        
			}
		}
	}

	/**
	 * parses received message, executes action, and generates response.
	 * @param message
	 * @return Response to received message. Null if not applicable.
	 */
	public String parseReceivedMessage(String message){
		String[] messageHeadParts = Tools.getHead(message).split(" +");
		//int user_id;
		//Gson gson = new Gson();;

		switch(messageHeadParts[0]) {
		case "ISONLINE":
			return Tools.generateMessage("OK");

		default:
			break;				
		}
		return null;
	}	
}
