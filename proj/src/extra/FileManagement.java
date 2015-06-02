package extra;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Map;

import main.Chunk;
/**
 * Class used to manage file operations
 * @author André Pires, Filipe Gama
 */
public abstract class FileManagement {	
	/**
	 * Mostra as informações do ficheiro para gerar um ID unico com SHA256
	 * @param filePath
	 * @return 
	 * @throws NoSuchAlgorithmException
	 * @throws UnsupportedEncodingException 
	 */
	public static String getFileId(String filePath) throws NoSuchAlgorithmException, UnsupportedEncodingException {
		File file = new File(filePath);
		SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");

		/* Store and display metadata that is going to be used to generate the bit string */
		String filename = file.getName();
		String date = sdf.format(file.lastModified());
		String path = file.getAbsolutePath();

		/* generate unique id */
		String temp = filename+date+path;
		MessageDigest digest = MessageDigest.getInstance("SHA-256");
		byte[] hash = digest.digest(temp.getBytes("UTF-16"));

		String res = bytesToHex(hash);
		return res;
	}

	/**
	 * Split file in chunks - based on code from http://www.javabeat.net/java-split-merge-files/
	 * @param filename
	 * @throws FileNotFoundException 
	 * @throws NoSuchAlgorithmException 
	 */
	public static ArrayList<Chunk> splitFile(String filename, int repDegree) throws FileNotFoundException, NoSuchAlgorithmException {
		if(!FileManagement.fileExists(filename))
			throw new FileNotFoundException();

		ArrayList<Chunk> arrayfinal = new ArrayList<Chunk>();

		File file = new File(filename);  

		int fileRemaining = (int) file.length();

		boolean isMultiple = ((fileRemaining % Tools.getPacketSize()) == 0);

		int processed = 0, toRead = Tools.getPacketSize(), nChunk = 0;
		byte[] chunk;

		FileInputStream stream;

		try {
			stream = new FileInputStream(filename);
			String fId = FileManagement.getFileId(filename);

			while(fileRemaining > 0) {
				if(fileRemaining < Tools.getPacketSize()) {
					toRead = fileRemaining;
				}

				chunk = new byte[toRead];
				processed = stream.read(chunk, 0, toRead);

				fileRemaining -= toRead;
				assert (processed == chunk.length);

				Chunk newChunk = new Chunk(chunk);
				newChunk.setChunkNo(nChunk);
				newChunk.setFileId(fId);
				newChunk.setReplicationDeg(repDegree);

				arrayfinal.add(newChunk);				
				nChunk++;
			}

			if(isMultiple) {
				Chunk newChunk = new Chunk(new byte[0]);
				newChunk.setChunkNo(nChunk);
				newChunk.setFileId(fId);
				newChunk.setReplicationDeg(repDegree);

				arrayfinal.add(newChunk);
				nChunk++;
			}

			stream.close();
		} catch (IOException exception) {
			exception.printStackTrace();
		}
		return arrayfinal;
	}

	/**
	 * Creates a physical chunk
	 * @param chunk
	 * @throws IOException
	 */
	public static void materializeChunk(Chunk chunk) throws IOException {
		FileOutputStream out;
		String tempname = chunk.getFileId() + "_chunk_" + chunk.getChunkNo();
		out = new FileOutputStream(new File("files\\backups\\" + tempname));
		out.write(chunk.getByteArray());
		out.flush();
		out.close();
	}
	
	/**
	 * Add file to the backup list
	 * @param string
	 * @param fId
	 * @param numberOfChunks
	 * @param usersID 
	 */
	public static void addToBackupListFile(String string, String fId,int numberOfChunks, ArrayList<Integer> usersID) {
		try {
			PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter("files\\lists\\backup_list.txt")));
			out.print(string + "?" +  fId + "?" + numberOfChunks);
			for(Integer id: usersID)
				out.print("?" + id);
			out.println();
			out.close();
		} catch (IOException e) {
			e.getMessage();
		}		
	}

	/**
	 * Reads a physical chunk, and creates a chunk with it
	 * @param fileId
	 * @param chunkNo
	 * @return
	 * @throws IOException
	 */
	public static Chunk readChunk(String fileId, int chunkNo) throws IOException {
		File file = new File("files\\backups\\" + fileId +"_chunk_" + chunkNo);
		FileInputStream in_stream = new FileInputStream(file);
		byte[] fileBytes = new byte[(int) file.length()];
		in_stream.read(fileBytes, 0,(int)  file.length());	
		in_stream.close();
		return new Chunk(fileBytes,chunkNo, 1, fileId);
	}

	/**
	 * Join chunks and get file - based on code from http://www.javabeat.net/java-split-merge-files/
	 * @param filename
	 */
	public static void recover_file(String filename, ArrayList<Chunk> chunks) { 
		File ofile = new File("files\\restores\\backup_" + filename);
		FileOutputStream out_stream;

		try {
			out_stream = new FileOutputStream(ofile,true);
			for (Chunk chunk : chunks) {
				out_stream.write(chunk.getByteArray());
				out_stream.flush();
			}
			out_stream.close();
			out_stream = null;		
		}catch (Exception exception){
			exception.printStackTrace();
		}
	}
	
	/**
	 * Saves map data to a file
	 * @param m
	 * @param file
	 * @throws IOException
	 */
	public static void saveMapToFile(Map<Chunk,Integer> m, String file) throws IOException {
		PrintWriter f0 = new PrintWriter(new FileWriter(file));

		for (Map.Entry<Chunk, Integer> entry : m.entrySet()) {
			f0.println(entry.getKey().getFileId() + "?" + entry.getKey().getChunkNo() + "?" + entry.getKey().getReplicationDeg() + "?" + entry.getValue());
		}
		f0.close();
	}
	
	/** 
	 * Print hash in hexadecimal, code copied from:
	 * 
	 * http://stackoverflow.com/questions/9655181/convert-from-byte-array-to-hex-string-in-java
	 *  
	 */
	final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();

	static String bytesToHex(byte[] bytes) {
		char[] hexChars = new char[bytes.length * 2];
		for ( int j = 0; j < bytes.length; j++ ) {
			int v = bytes[j] & 0xFF;
			hexChars[j * 2] = hexArray[v >>> 4];
			hexChars[j * 2 + 1] = hexArray[v & 0x0F];
		}
		return new String(hexChars);
	}

	/**
	 * Checks if filePath is a file
	 * @param filePath
	 * @return boolean
	 */
	public static boolean fileExists(String filePath) {
		return (new File(filePath)).isFile();
	}
}

