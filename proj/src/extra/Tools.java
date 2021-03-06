package extra;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.UnknownHostException;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JFileChooser;

import user.User;
import main.Chunk;

/**
 * 
 * @author Andr� Pires, Filipe Gama
 *
 */
public abstract class Tools {
	static int packetSize = 14000;	
	static long folderSize = 10000000L;
	static boolean debug = true;

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
			return "NOTRESPOND DELETE " + "0" + " " + fileId + " " + u.getId() + "\r\n\r\n"+ "\r\n\r\n";
		}
		return null;
	}

	/**
	 * 
	 * @param type
	 * @param chunk
	 * @return
	 */
	public static String generateMessage(String type, Chunk chunk) {
		String message = null;
		String body = Tools.encode(chunk.getByteArray());
		switch(type) {
		case "PUTCHUNK":
			message = "PUTCHUNK " + String.valueOf(String.valueOf(body.length())) + " " + chunk.getFileId() +  " "  + chunk.getChunkNo() + " " + chunk.getReplicationDeg() 
			+ " " + chunk.getUserWhoSent() + " \r\n\r\n" + body + "\r\n\r\n";
			break;
		case "STORED":
			message = "STORED " + "0" + " " +  chunk.getFileId() +  " "  + chunk.getChunkNo() + " \r\n\r\n" + "\r\n\r\n"; 
			break;	
		case "CHUNK": 
			message =  "CHUNK "  +  String.valueOf(body.length()) + " " +  chunk.getFileId() +  " "  + chunk.getChunkNo() + " \r\n\r\n" +
					body+ "\r\n\r\n";
			break;		
		case "REMOVED":
			message = "REMOVED " +  "0"  + " " +  chunk.getFileId() +  " "  + chunk.getChunkNo() + " \r\n\r\n" + "\r\n\r\n"; 
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
		return "DELETE " + "0" + " " + fileId + " \r\n\r\n" + "\r\n\r\n";
	}

	/**
	 * GETCHUNK <Version> <FileId> <ChunkNo> <CRLF><CRLF>
	 * @param type
	 * @param fileId
	 * @param chunkNo
	 * @return
	 */
	public static String generateGetChunkMessage(String type, String fileId, int chunkNo) {
		return "GETCHUNK " + "0" + " " + fileId + " " + chunkNo + " \r\n\r\n" + "\r\n\r\n";
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
			message = "GETALLUSERS " + "0" + " \r\n\r\n" + "\r\n\r\n";
			break;
		case "GETONLINEUSERS":
			message = "GETONLINEUSERS " + "0" + " \r\n\r\n" + "\r\n\r\n";
			break;
		case "OK":
			message = "OK " + "0" + " \r\n\r\n" + "\r\n\r\n";
			break;
		case "NOTOK":
			message = "NOTOK " + "0" + " \r\n\r\n" + "\r\n\r\n";
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
		String message = type + " " + String.valueOf(json_body.length()) + " \r\n\r\n" + json_body + "\r\n\r\n" ;
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
		String message = type + " " + String.valueOf(json_body.length()) + " " + user_id + " \r\n\r\n" + json_body + "\r\n\r\n" ;
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
		String message = type + " " + "0" + " " + user_id + " \r\n\r\n" + "\r\n\r\n" ;
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
		int size = 0;
		String[] parts = msg.split(" +");

		if(parts[0].equals("NOTRESPOND")) size = Integer.parseInt(parts[2].trim());
		else size =  Integer.parseInt(parts[1].trim());

		int index = msg.indexOf("\r\n\r\n") + 4;
		if (index < 0)
			return null;

		int index2 = msg.substring(index).indexOf("\r\n\r\n");

		if(index2 < 0)
			return msg.substring(index);

		return msg.substring(index,size+index);
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

	@SuppressWarnings("restriction")
	public static String encode(byte[] bytes){
		
		//alternative 1 -> try encoding base64 (sun)
		//System.out.println("going to encode "+bytes.length+" bytes");
		sun.misc.BASE64Encoder enc = new sun.misc.BASE64Encoder();
		String ret = enc.encode(bytes);
		//System.out.println("	encoded to string with length "+ret.length());
		return ret;
		
		//alternative2 -> java8 encoder
		/*System.out.println("going to encode "+bytes.length+" bytes");
		Base64.Encoder enc2 = Base64.getEncoder().withoutPadding();
		String ret2 = enc2.encodeToString(bytes);
		System.out.println("	encoded to string with length "+ret2.length());
		return ret2; */
	}

	@SuppressWarnings("restriction")
	public static byte[] decode(String bytes){
			
		
		//alternative 1 -> try encoding base64 (sun)
		//System.out.println("going to decode string with size "+bytes.length());
		sun.misc.BASE64Decoder dec = new sun.misc.BASE64Decoder();
		try {
			byte[] ret = dec.decodeBuffer(bytes);
			//System.out.println("	decoded to "+ret.length+" bytes");
			return ret;
		} catch (IOException e) {
			System.out.println("error decoding string to byte[].");
			e.printStackTrace();
			return null;
		}
		
		
		//alternative2 -> java8 decoder
		/*System.out.println("going to decode string with size "+bytes.length());
		Base64.Decoder dec2 = Base64.getDecoder();
		byte[] ret2 = dec2.decode(bytes);
		System.out.println("	decoded to "+ret2.length+" bytes");
		return ret2; */
	}
}
