package extra;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JFileChooser;

import com.google.gson.Gson;

import user.User;
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

	public static String generateNotRespondMessage(String type, String fileId, User u) {
		switch(type) {
		case "DELETE":
			return "NOTRESPOND DELETE " + Tools.getVersion() + " " + fileId + " " + u.getId() + "\r\n\r\n";
		}
		return null;
	}
	
	
	public static String generateNotRespondMessage(String type, String fileId, int chunkNo) {
		switch(type) {
		case "GETCHUNK":
			return "NOTRESPOND GETCHUNK " + Tools.getVersion() + " " + fileId + " " + chunkNo + "\r\n\r\n";
		}
		return null;
	}
	
	
	
	public static String sendGetChunkServer(String type, String fileId, User u) {
		switch(type) {
		case "DELETE":
			return "NOTRESPOND DELETE " + Tools.getVersion() + " " + fileId + " " + u.getId() + "\r\n\r\n";
		}
		return null;
	}
	
	/**
	 * 
	 * @param in
	 * @param messagesWithBody
	 * @return
	 * @throws IOException
	 */
	public static String stateMachine(BufferedReader in,ArrayList<String> messagesWithBody ) throws IOException {
		String response;
		int state = 0;
		boolean firstTime = true;
		
		response = "";
		char nextChar = 0;

		while(true) {		

			nextChar = (char) in.read();
			response += nextChar;

			if(nextChar == '\r' && (state == 0 || state == 2)) {
				state++;
			}
			else if(nextChar == '\n' && state == 1) {
				state++;
			}
			else if(nextChar == '\n' && state == 3) {
				if(messagesWithBody.contains(response.substring(0, response.indexOf(" "))) && firstTime) {
					firstTime = false;
					state = 0;
				}
				else break;
			}
			else state = 0;
		}

		firstTime = true;
		return response;
	}

	
	/**
	 * 
	 * @param type
	 * @param chunk
	 * @return
	 */
	public static String generateMessage(String type, Chunk chunk) {
		Gson gson = new Gson();
		String message = null;
		switch(type) {
		case "PUTCHUNK":
			//message = "PUTCHUNK " + Tools.getVersion() + " " + chunk.getFileId() +  " "  + chunk.getChunkNo() + " " + chunk.getReplicationDeg() 
			//+ "\r\n\r\n" + (new String(chunk.getByteArray(),StandardCharsets.ISO_8859_1)) + "\r\n\r\n";
			
			message = "PUTCHUNK " + Tools.getVersion() + " " + chunk.getFileId() +  " "  + chunk.getChunkNo() + " " + chunk.getReplicationDeg() 
					+ "\r\n\r\n" + gson.toJson(chunk) + "\r\n\r\n";
			break;
		case "STORED":
			message = "STORED " +  Tools.getVersion() + " " +  chunk.getFileId() +  " "  + chunk.getChunkNo() + "\r\n\r\n"; 
			break;	
		case "CHUNK": 
			//message =  "CHUNK "  + Tools.getVersion() + " " +  chunk.getFileId() +  " "  + chunk.getChunkNo() + "\r\n\r\n" +
				//	(new String(chunk.getByteArray(),StandardCharsets.ISO_8859_1))+ "\r\n\r\n";
			message =  "CHUNK "  + Tools.getVersion() + " " +  chunk.getFileId() +  " "  + chunk.getChunkNo() + "\r\n\r\n" +
					gson.toJson(chunk)+ "\r\n\r\n";
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
	public static String generateDeleteMessage(String type, String fileId) {
		return "DELETE " + Tools.getVersion() + " " + fileId + "\r\n\r\n";
	}

	/**
	 * GETCHUNK <Version> <FileId> <ChunkNo> <CRLF><CRLF>
	 * @param type
	 * @param fileId
	 * @param chunkNo
	 * @return
	 */
	public static String generateGetChunkMessage(String type, String fileId, int chunkNo) {
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
		case "GETONLINEUSERS":
			message = "GETONLINEUSERS " + Tools.getVersion() + "\r\n\r\n";
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
		String message = type + " " + Tools.getVersion() + "\r\n\r\n" + json_body + "\r\n\r\n" ;
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
		String message = type + " " + Tools.getVersion() + " " + user_id + "\r\n\r\n" + json_body + "\r\n\r\n" ;
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
		
		int index2 = msg.substring(index).indexOf("\r\n\r\n");
		
		if(index2 < 0)
			return msg.substring(index);
		
		return msg.substring(index,index2+index);
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
	 * #proj2 
	 * Launches file selection window. 
	 * @return Selected file path
	 */
	public static String selectFileFrame(){
		JFileChooser chooser = new JFileChooser();
	    /*FileNameExtensionFilter filter = new FileNameExtensionFilter(
	        "JPG & GIF Images", "jpg", "gif");
	    chooser.setFileFilter(filter);*/
	    int returnVal = chooser.showOpenDialog(null);
	    if(returnVal == JFileChooser.APPROVE_OPTION) {
	       System.out.println("You chose to open this file: " +
	            chooser.getSelectedFile().getName());
	       return chooser.getSelectedFile().getPath();
	    }
	    return null;	
	}
	
	
	/**
	 * 
	 * @param fileName
	 * @return array with 2 elements. First is file name, second is file extension
	 */
	public static String[] splitFileExtension(String fileName){
		return fileName.split("\\.(?=[^\\.]+$)");
	}
	
	
}
