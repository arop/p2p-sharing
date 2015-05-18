package extra;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
}
