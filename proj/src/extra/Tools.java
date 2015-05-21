package extra;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.security.Security;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import com.sun.net.ssl.internal.ssl.Provider;

import main.Chunk;

/**
 * 
 * @author André Pires, Filipe Gama
 *
 */
public abstract class Tools {
	static int packetSize = 64000;	
	static long folderSize = 10000000L;
	static boolean debug = true;
	static String version = "1.0";	

	/**
	 * Checks if the input ips are valid
	 * @param ip
	 * @return
	 */
	public static Boolean validIP(String ip) {
		String pattern = "(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)."
				+ "(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)."
				+ "(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)."
				+ "(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)";

		Pattern r = Pattern.compile(pattern);
		Matcher m = r.matcher(ip);

		return m.matches();
	}

	/**
	 * 
	 * @param type
	 * @param chunk
	 * @return
	 */
	public static String generateMessage(String type, Chunk chunk) {
		String message = null;
		switch(type) {
		case "PUTCHUNK":
			message = "PUTCHUNK " + Tools.getVersion() + " " + chunk.getFileId() +  " "  + chunk.getChunkNo() + " " + chunk.getReplicationDeg() 
			+ "\r\n\r\n" + (new String(chunk.getByteArray(),StandardCharsets.ISO_8859_1));
			break;
		case "STORED":
			message = "STORED " +  Tools.getVersion() + " " +  chunk.getFileId() +  " "  + chunk.getChunkNo() + "\r\n\r\n"; 
			break;	
		case "CHUNK": 
			message =  "CHUNK "  + Tools.getVersion() + " " +  chunk.getFileId() +  " "  + chunk.getChunkNo() + "\r\n\r\n" +
					(new String(chunk.getByteArray(),StandardCharsets.ISO_8859_1)); 
			break;		
		case "REMOVED":
			message = "REMOVED " +  Tools.getVersion() + " " +  chunk.getFileId() +  " "  + chunk.getChunkNo() + "\r\n\r\n"; 
			break;	
		default:
			System.err.println("Invalid Message!");
			break;
		}
		return message;
	}


	/**
	 * DELETE <Version> <FileId> <CRLF><CRLF>
	 * @param type
	 * @param fileId
	 * @return
	 */
	public static String generateMessage(String type, String fileId) {
		return "DELETE " + Tools.getVersion() + " " + fileId + "\r\n\r\n";
	}

	/**
	 * GETCHUNK <Version> <FileId> <ChunkNo> <CRLF><CRLF>
	 * @param type
	 * @param fileId
	 * @param chunkNo
	 * @return
	 */
	public static String generateMessage(String type, String fileId, int chunkNo) {
		return "GETCHUNK " + Tools.getVersion() + " " + fileId + " " + chunkNo + "\r\n\r\n";
	}

	/**
	 * #proj2
	 * GETALLUSERS <Version> <CRLF><CRLF>
	 * OK <Version> <CRLF><CRLF>
	 * NOTOK <Version> <CRLF><CRLF>
	 * @param type
	 * @return
	 */
	public static String generateMessage(String type){
		String message = null;
		switch(type) {
		case "GETALLUSERS":
			message = "GETALLUSERS " + Tools.getVersion() + "\r\n\r\n";
			break;
		case "OK":
			message = "OK " + Tools.getVersion() + "\r\n\r\n";
			break;
		case "NOTOK":
			message = "NOTOK " + Tools.getVersion() + "\r\n\r\n";
			break;

		default:
			System.err.println("Invalid Message!");
			break;
		}
		return message;
	}

	/**
	 * #proj2
	 * USERS <Version> <CRLF><CRLF> Json body with users data
	 * @param type
	 * @param json_body
	 * @return
	 */
	public static String generateJsonMessage(String type, String json_body){
		/*String message = null;
		switch(type) {
		case "USERS":
			message = "USERS " + Tools.getVersion() + "\r\n\r\n" + json_body ;
			break;
		default:
			System.err.println("Invalid Message!");
			break;
		}*/
		String message = type + " " + Tools.getVersion() + "\r\n\r\n" + json_body ;
		return message;
	}

	/**
	 *  #proj2
	 *  ADDFRIENDS <Version> <User id> <CRLF><CRLF> JSON of int[] users ids
	 *  
	 * @param type message type
	 * @param user_id Local user id
	 * @param json_body message body with json data
	 * @return generated message
	 */
	public static String generateJsonMessage(String type, int user_id, String json_body){
		String message = type + " " + Tools.getVersion() + " " + user_id + "\r\n\r\n" + json_body ;
		return message;
	}

	/**
	 *  #proj2
	 *  
	 * @param type message type
	 * @param user_id Local user id
	 * @param json_body message body with json data
	 * @return generated message
	 */
	public static String generateMessage(String type, int user_id){
		String message = type + " " + Tools.getVersion() + " " + user_id + "\r\n\r\n" ;
		return message;
	}


	public static String getPeerAddress() throws UnknownHostException {
		return InetAddress.getLocalHost().getHostAddress();
	}

	/**
	 * Gets folder size by sum of files length, copied from:
	 * http://stackoverflow.com/questions/2149785/get-size-of-folder-or-file
	 * 
	 * @param directory
	 * @return size of folder
	 */
	public static long folderSize(File directory) {
		long length = 0;

		if(!directory.isDirectory())
			return 0;

		for (File file : directory.listFiles()) {
			if (file.isFile())
				length += file.length();
			else
				length += folderSize(file);
		}
		return length;
	}

	public static int getPacketSize() {
		return packetSize;
	}

	public static void setPacketSize(int packetSizeIn) {
		packetSize = packetSizeIn;
	}

	public static void setFolderSize(int folderSizeIn) {
		folderSize = folderSizeIn;
	}

	public static long getFolderSize() {
		return folderSize;
	}

	/**
	 * @return the debug
	 */
	public static boolean isDebug() {
		return debug;
	}

	/**
	 * @param debug the debug to set
	 */
	public static void setDebug(boolean debugIn) {
		debug = debugIn;
	}

	public static String getVersion() {
		return version;
	}

	public static void setVersion(String version) {
		Tools.version = version;
	}

	public static int getFreePort(){
		int port = -1;
		try {
			ServerSocket  ss = new ServerSocket(0);
			port = ss.getLocalPort();
			ss.close();
		} catch (IOException e) {
			return -1;
		}
		return port;
	}

	/**
	 * 
	 * @param msg - message received 
	 * @return head of the message
	 */
	public static String getHead(String msg) {
		int index = msg.indexOf("\r\n\r\n");
		if (index < 0)
			return null;
		return msg.substring(0,index);
	}

	/**
	 * 
	 * @param msg - message received 
	 * @return body of the message
	 */
	public static String getBody(String msg) {
		int index = msg.indexOf("\r\n\r\n") + 4;
		if (index < 0)
			return null;
		return msg.substring(index);
	}

	/**
	 * 
	 * @param msg - message received 
	 * @return type of the message
	 */
	public static String getType(String msg) {
		return msg.split(" +")[0];

	}

	/**
	 * 
	 * @param msg
	 * @param ip_dest
	 * @param port_dest
	 * @return response from other peer/server
	 */
	public static String sendMessage(String msg, String ip_dest, int port_dest, int connection_try_number){
		{
			// Registering the JSSE provider
			Security.addProvider(new Provider());
		}

		int timeout = 5000; //timeout in miliseconds

		SSLSocketFactory sslsocketfactory = (SSLSocketFactory)SSLSocketFactory.getDefault();
		SSLSocket sslSocket;

		PrintWriter out = null;
		BufferedReader in = null;
		String response = null;

		try {
			sslSocket = (SSLSocket)sslsocketfactory.createSocket(ip_dest,port_dest);
			sslSocket.setSoTimeout(timeout);

			// Initializing the streams for Communication with the Server
			out = new PrintWriter(sslSocket.getOutputStream(), true);
			in = new BufferedReader(new InputStreamReader(sslSocket.getInputStream()));

			//SEND MESSAGE
			out.println(msg);

			//GET RESPONSE 
			response = in.readLine();
			response += in.readLine(); 				//TODO (isto está assim hardcoded pq o 
			response += "\r\n\r\n"+in.readLine(); 	//readLine lê até ao \r\n apenas)

			//PARSE RESPONSE
			//String origin_ip = sslSocket.getInetAddress().getHostAddress();
			//System.out.println("response: " + response + "#" + origin_ip);

			// Closing the Streams and the Socket
			out.close();
			in.close();
			sslSocket.close();		
		} 
		catch (SocketTimeoutException e){
			System.out.println("timeout");
			if (connection_try_number > 3){
				//e.printStackTrace();
				return null;
			}
			System.out.println("try: "+connection_try_number);
			return Tools.sendMessage(msg, ip_dest, port_dest,connection_try_number+1);
		}
		catch (ConnectException e){
			if (connection_try_number > 3){
				//e.printStackTrace();
				return null;
			}
			System.out.println("try: "+connection_try_number);
			return Tools.sendMessage(msg, ip_dest, port_dest,connection_try_number+1);
		}
		catch (IOException e) {
			e.printStackTrace();
			return null;
		}

		return response;
	}
}
