package peer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.reflect.Type;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.Security;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.Map.Entry;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

import main.Chunk;
import ui.loginFrame.LoginFrame;
import ui.mainFrame.GUI;
import user.User;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.sun.net.ssl.internal.ssl.Provider;

import extra.FileManagement;
import extra.Tools;

public class PeerNew {
	private User localUser;
	private String serverAddress = "localhost";
	private int serverPort = 16500; 

	//private DegreeMonitorThread degMonitor;
	private Map <Chunk, Integer> degreeListSent;


	ArrayList<User> friends;

	private Map <String, String> backupList;
	private Map <String, Integer> backupList2;

	private ArrayList<Chunk> chunklist;
	private ArrayList<Chunk> chunksReceived;

	public PeerNew() throws IOException {		
		chunklist = new ArrayList<Chunk>();
		backupList = new HashMap<String, String>();
		backupList2 = new HashMap<String, Integer>();

		//degMonitor = new DegreeMonitorThread();

		loadChunkList();
		loadBackupList();

		degreeListSent = new HashMap<Chunk, Integer>();
		
		if(degreeListSent.isEmpty()) {
			readDegreeList("files\\lists\\degreeListSent.txt");
	}


		//degMonitor.start();
	}

	private LoginFrame loginFrame;
	//private GUI mainFrame;

	public ArrayList<User> getAllUsersFromServer(){
		String response = this.sendMessage(Tools.generateMessage("GETALLUSERS"), serverAddress, serverPort,0);
		Gson gson = new Gson();
		String type = response.split(" +")[0];
		if (!type.equals("USERS"))
			return null;

		String list_json = Tools.getBody(response);
		Type listOfUsersType = new TypeToken<ArrayList<User>>(){}.getType();
		return gson.fromJson(list_json, listOfUsersType);
	}

	public void getFriendsFromServer(){
		String response = this.sendMessage(Tools.generateMessage("GETFRIENDS", this.localUser.getId()), serverAddress, serverPort,0);
		Gson gson = new Gson();
		String type = Tools.getType(response);
		if (!type.equals("FRIENDS")){
			return;
		}			

		String list_json = Tools.getBody(response);
		Type listOfUsersType = new TypeToken<ArrayList<User>>(){}.getType();
		this.friends = gson.fromJson(list_json, listOfUsersType);
	}

	public ArrayList<User> getOnlineUsersFromServer(){
		String response = this.sendMessage(Tools.generateMessage("GETONLINEUSERS"), serverAddress, serverPort,0);
		Gson gson = new Gson();
		String type = Tools.getType(response);
		if (!type.equals("ONLINEUSERS")){
			return null;
		}			

		String list_json = Tools.getBody(response);
		Type listOfUsersType = new TypeToken<ArrayList<User>>(){}.getType();
		return gson.fromJson(list_json, listOfUsersType);
	}

	/**
	 * 
	 * @param user_ids array of ids if the users to add as friends.
	 */
	public boolean addFriends(int[] user_ids) {

		/*if (user_ids == null)
			System.out.println("nulosss");
		else System.out.println(user_ids[0]);*/

		Gson gson = new Gson();
		String json_data = gson.toJson(user_ids);
		String response = this.sendMessage(Tools.generateJsonMessage("ADDFRIENDS", localUser.getId(), json_data), serverAddress, serverPort,0);

		return Tools.getType(response).equals("OK");
	}

	public void startPeer(){
		loginFrame = new LoginFrame(this);
		while(!loginFrame.isSuccess()) {	
			//System.out.println(loginFrame.isSuccess());
		}
		loginFrame.dispose();

		this.friends = new ArrayList<User>();//initialize list

		this.getFriendsFromServer(); //update list with values from server

		GUI gui = new GUI(this);
		gui.setVisible(true);

		//connection listener -> thread always reading in user's port.
		ConnectionListenerPeer con_listener = new ConnectionListenerPeer(this);
		con_listener.start();
	}

	public User getLocalUser() {
		return localUser;
	}

	public void setLocalUser(User localUser) {
		this.localUser = localUser;
	}

	public ArrayList<User> getFriends(){
		return this.friends;
	}

	public boolean login(String username, String password) {
		String messagebody = username + " " + password;
		String response = this.sendMessage(Tools.generateJsonMessage("LOGIN",messagebody), serverAddress, serverPort,0);
		if(!Tools.getType(response).equals("OK"))
			return false;

		Gson g = new Gson();
		User u = g.fromJson(Tools.getBody(response), User.class);
		setLocalUser(u);
		return true;
	}

	public boolean register(String username, String email, String password1, String password2, int desiredPort) {
		if(password1.equals(password2)) {
			String messagebody = username + " " + email + " " + password1 + " " + desiredPort;
			String response = this.sendMessage(Tools.generateJsonMessage("REGISTER",messagebody), serverAddress, serverPort,0);
			return Tools.getType(response).equals("OK");
		}

		return false;
	}

	public static void main(String[] args) throws InterruptedException, IOException{
		generateFolders();

		PeerNew peer = new PeerNew();

		peer.startPeer();	
	}

	public void startRegularBackupProtocol(String filePath, int repDegree) throws IOException {
		//1-> split file into chunks
		ArrayList<Chunk> chunks = null; 
		try {
			chunks = FileManagement.splitFile(filePath, repDegree);
		} catch (FileNotFoundException | NoSuchAlgorithmException e) {
			e.printStackTrace();
		}

		//2 -> get online users IPs and ports from server
		ArrayList<User> onlineUsers = this.getOnlineUsersFromServer();
		
		if (onlineUsers == null)
			System.out.println("Null online users");
		else {
		
			for(User user : new ArrayList<User> (onlineUsers)){
				if(user.getId() == localUser.getId()) onlineUsers.remove(user);
				System.out.println("User: "+user.getIp()+":"+user.getPort());
			}

		}

		//3 -> send each chunk for repDegree random users
		if(onlineUsers.size() < repDegree) {
			System.err.println("ERROR: Impossible rep degree at this point");
			return;
		}

		ArrayList<Integer> usedIndexes = new ArrayList<Integer>();
		for (Chunk chunk : chunks) {
			usedIndexes.clear();
			String msg = Tools.generateMessage("PUTCHUNK", chunk);

			System.out.println("inside for");

			int tempRepDegree = repDegree;
			Random r = new Random();

			while(tempRepDegree > 0) {
				int index = r.nextInt(onlineUsers.size());
				
				while(usedIndexes.contains(index))
					index = r.nextInt(onlineUsers.size());

				usedIndexes.add(index);

				User temp = onlineUsers.get(index);
				System.out.println("sending message");
				this.sendMessage(msg, temp.getIp(), temp.getPort(), 0);
				tempRepDegree--;
			}

			String[] cenas = filePath.split("\\\\");

			addToBackupList(cenas[cenas.length-1],chunks.get(0).getFileId(),chunks.size());
			
			refreshBackupList();

			for (Chunk chunk2 : chunks) {
				addToMap("send", chunk2);
		}	
	}
	}

	
	public boolean shareFileWithFriend(int friend_id){
		
		User friend;
		if ( (friend= this.getUserFromServer(friend_id)) == null)
			return false;
		
		String filePath = Tools.selectFileFrame();
		File f = new File(filePath);
		
		String answer = sendMessage(Tools.generateJsonMessage("BACKUPFILE", this.localUser.getId(), f.getName()+"#"+f.length()), 
				friend.getIp(), friend.getPort(), 0);
		
		if (answer == null)
			return false;
		
		if (!Tools.getType(answer).equals("OK")) 
			return false;
				
		int friendPortForShare = Integer.parseInt(Tools.getBody(answer));
		
		//create thread to send file
		FileShareThreadSend t;
		try {
			t = new FileShareThreadSend(friend, filePath, friendPortForShare, this);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			return false;
		}
		
		t.start();	
		
		return true;
	}
	
	/**
	 * Starts thread where peer will receive file shared by friend.
	 * @param friend_id
	 * @param fileName
	 * @param fileSize
	 * @return port where ssl socket is open. negative in case of error.
	 */
	public int startFileShareReceiveThread(int friend_id, String fileName, long fileSize){
		int port = -1;
		
		try {
			FileShareThreadReceive t = new FileShareThreadReceive(friend_id, fileName, fileSize, this);
			t.start();
			while(true){
				port = t.getPort();
				if (port > 0)
					break;
			}
		} catch (IOException e) {
			System.out.println("Unable to create file...");
		}
		catch (Exception e){
			System.out.println("Unexpected exception creating file share receive thread...");
			System.out.println("Exception message: "+e.getMessage());
			e.printStackTrace();
		}
		
		return port;
	}
	
	public User getUserFromServer(int user_id){
		User u = null;
		String answer = sendMessage(Tools.generateJsonMessage("GETUSER", String.valueOf(user_id)), this.serverAddress, this.serverPort, 0);
		if (answer == null)
			return null;
		
		if (!Tools.getType(answer).equals("USER"))
			return null;
		
		Gson gson = new Gson();
		u = (User) gson.fromJson(Tools.getBody(answer), User.class);
		return u;
	}
	
	
	/**
	 * @param msg
	 * @param ip_dest
	 * @param port_dest
	 * @return response from other peer/server
	 */
	public String sendMessage(String msg, String ip_dest, int port_dest, int connection_try_number){
		System.out.println("Sending message: "+Tools.getHead(msg));
		{
			// Registering the JSSE provider
			Security.addProvider(new Provider());
		}

		int timeout = 60000; //timeout in miliseconds

		//SSLSocketFactory sslsocketfactory = (SSLSocketFactory)SSLSocketFactory.getDefault();
		SSLSocket sslSocket;

		PrintWriter out = null;
		BufferedReader in = null;
		String response = null;

		try {
			sslSocket = getSocketConnection(ip_dest, port_dest);
			//sslSocket = (SSLSocket)sslsocketfactory.createSocket(ip_dest,port_dest);
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
		catch (Exception e) {
			if (connection_try_number > 3){
				//e.printStackTrace();
				return null;
			}
			System.out.println("try: "+connection_try_number);
			return this.sendMessage(msg, ip_dest, port_dest,connection_try_number+1);
		}
		System.out.println("	answer: "+Tools.getHead(response));
		return response;
	}


	private SSLSocket getSocketConnection(String host, int port) {
		try {
			/* Create keystore */
			KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
			keyStore.load(new FileInputStream("..\\certificates\\server\\keystore"), "peerkey".toCharArray());

			/* Get factory for the given keystore */
			TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
			tmf.init(keyStore);
			SSLContext ctx = SSLContext.getInstance("SSL");
			ctx.init(null, tmf.getTrustManagers(), null);
			SSLSocketFactory factory = ctx.getSocketFactory();

			
			return (SSLSocket) factory.createSocket(host, port);
		} catch (Exception e) {
			System.out.println("Problem starting auth server: "+ e.getMessage()+"\n"+e.getCause());
			return null;
		}
	}
	
	
	public SSLServerSocket getServerSocket(int socket_port) {
	    try {
	    	
	        /* Create keystore */
	        KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
	        keyStore.load(new FileInputStream("..\\certificates\\server\\keystore"), "peerkey".toCharArray());

	        
	        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
	    	kmf.init(keyStore, "peerkey".toCharArray()); // That's the key's password, if different.
	        
	        
	        /* Get factory for the given keystore */
	        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
	        tmf.init(keyStore);
	        
	        SSLContext ctx = SSLContext.getInstance("SSL");
	        //ctx.init(null, tmf.getTrustManagers(), null);
	        ctx.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
	        
	        SSLServerSocketFactory factory = ctx.getServerSocketFactory();

	        return (SSLServerSocket) factory.createServerSocket(socket_port);
	    } catch (Exception e) {
	       System.out.println("Problem creating SSL Server Socket: "+ e.getMessage()+"\n"+e.getCause());
	       return null;
	    }
	    
	}

	/**
	 * Create folders
	 * @throws InterruptedException 
	 */
	private static void generateFolders() throws InterruptedException {

		File dir1 = new File("files");
		if(!dir1.exists())
			dir1.mkdir();

		File dir2 = new File("files\\lists");
		if(!dir2.exists())
			dir2.mkdir();

		File dir3 = new File("files\\restores");
		if(!dir3.exists())
			dir3.mkdir();

		File dir4 = new File("files\\backups");
		if(!dir4.exists())
			dir4.mkdir();
		
		File dir5 = new File("files\\shared-with-me");
		if(!dir5.exists())
			dir5.mkdir();
	}

	public void addToBackupList(String string, String fId,int numberOfChunks) {
		if(!isBackedUp(string)) {
			backupList.put(string, fId);
			backupList2.put(string, numberOfChunks);
		}
	}

	public void refreshBackupList() throws IOException {
		if(backupList.isEmpty()) {
			PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter("files\\lists\\backup_list.txt")));
			out.close();
		}
		else for (Map.Entry<String, String> entry : backupList.entrySet()) {
			FileManagement.addToBackupListFile(entry.getKey(), entry.getValue(), backupList2.get(entry.getKey()));
		}
	}

	/**
	 * Load list of backed up files
	 * @throws IOException
	 */
	public void loadBackupList() throws IOException {
		if(!FileManagement.fileExists("files\\lists\\backup_list.txt"))
			(new File("files\\lists\\backup_list.txt")).createNewFile();
		else {
			try(BufferedReader br = new BufferedReader(new FileReader(new File("files\\lists\\backup_list.txt")))) {
				for(String line; (line = br.readLine()) != null; ) {
					String[] piecesOfLine = line.split(" +");
					backupList.put(piecesOfLine[0],piecesOfLine[1]);
					backupList2.put(piecesOfLine[0],Integer.parseInt(piecesOfLine[2]));
				}
				br.close();
			}
		}
	}

	/**
	 * Checks the existence of chunks in folder, and saves it in the array
	 * @throws IOException 
	 * 
	 */
	public void loadChunkList() throws IOException {
		File folder = new File("files\\backups");
		if(!folder.exists()) {
			folder.mkdir();
		}

		if(!folder.isDirectory()) {
			throw new FileNotFoundException();
		}

		ArrayList<File> files = parseFiles(new ArrayList<File>(Arrays.asList(folder.listFiles())));

		for (File file : files) {
			String fName = file.getName();
			String[] chunkName = fName.split("_");
			String fileId = chunkName[0];
			int chunkNo = Integer.parseInt(chunkName[2]);
			chunklist.add(FileManagement.readChunk(fileId, chunkNo));
		}
	}

	/**
	 * Select only the files that are chunks
	 * @param files
	 * @return
	 */
	private ArrayList<File> parseFiles(ArrayList<File> files) {
		ArrayList<File> toReturn = new ArrayList<File>();

		for (File file : files) {
			if(file.getName().contains("_chunk_"))
				toReturn.add(file);
		}
		return toReturn;
	}

	/**
	 * Deletes all chunk files with that fileId
	 * @param fileId
	 * @throws IOException
	 */
	public void deleteChunksOfFile(String fileId) throws IOException {
		for (Chunk chunk : chunklist) {
			if(chunk.getFileId().equals(fileId)) {
				String filename = fileId + "_chunk_" + chunk.getChunkNo();

				File f = new File("files\\backups\\"+filename);
				System.gc();
				f.delete();
			}
		}

		/* reloads chunk list */
		chunklist.clear();
		loadChunkList();
	}

	/**
	 * Deletes file from lists
	 * @param fileId
	 * @throws IOException 
	 */
	public void deleteFromSentLists(String fileId) throws IOException {
		String filename = getFilename(fileId);
		backupList.remove(filename);
		backupList2.remove(filename);

		removeFromSentMap(fileId);
	}

	/**
	 * Deletes Chunk with maximum replication degree ratio
	 * @throws IOException
	 */
//	public void deleteWorstChunk() throws IOException {
//		//Chunk chunk = mostRedundant();
//
//		if(chunk != null) {
//			String filename = chunk.getFileId() + "_chunk_" + chunk.getChunkNo();
//
//			File f = new File("files\\backups\\"+filename);
//			System.gc();
//			f.delete(); 
//
//			//removeFromStoredMap(chunk);
//
//			//this.sendMessage(Tools.generateMessage("REMOVED", chunk),MC_IP,MC_Port);
//		}
//
//		//FileManagement.saveMapToFile(getStoredMap(),"files\\lists\\degreeListStored.txt");
//
//		/* reloads chunk list */
//		chunklist.clear();
//		loadChunkList();
//	}

	/**
	 * Checks if peer has chunk
	 * @param fileId 
	 * @param chunkNo
	 * @return
	 */
	public boolean hasChunk(String fileId, int chunkNo) {
		for (Chunk chunk : chunklist) {
			if(chunk.getFileId().equals(fileId) && chunk.getChunkNo() == chunkNo)
				return true;
		}
		return false;
	}

	/**
	 * Returns chunk with fileId and chunkNo
	 * @param fileId
	 * @param chunkNo
	 * @return
	 */
	public Chunk searchChunk(String fileId, int chunkNo) {
		Chunk toSend = null;
		for (Chunk chunk : chunklist) {
			if(chunk.getFileId().equals(fileId) && chunk.getChunkNo() == chunkNo)
				toSend = chunk;
		}
		return toSend;
	}

	/**
	 * Checks if it has already received that chunk (used when restoring a file)
	 * @param fileId
	 * @param chunkNo
	 * @return
	 */
	public boolean alreadyExists(String fileId, int chunkNo) {
		for (Chunk chunk : chunksReceived) {
			if(chunk.getFileId().equals(fileId) && chunk.getChunkNo() == chunkNo) 
				return true;
		}
		return false;
	}

	public String getFilename(String fileId) {
		for(Map.Entry<String, String> filenames: backupList.entrySet()) {
			if(filenames.getValue().equals(fileId)) {
				return filenames.getKey();
			}
		}
		return null;
	}
	public Integer getFileNumberChunks(String string) {
		return this.backupList2.get(string);
	}

	public Boolean isBackedUp(String filename) {
		return this.backupList.containsKey(filename);
	}

	public Map<String, String> getBackupList() {
		return backupList;
	}

	public Map<String, Integer> getBackupList2() {
		return backupList2;
	}

	public void clearChunkList() {
		chunklist.clear();
}
	
	/* ============== OBRAS ============== */
	
	public void readDegreeList(String file) throws IOException {
		if(!FileManagement.fileExists(file))
			(new File(file)).createNewFile();
		else {
			try(BufferedReader br = new BufferedReader(new FileReader(new File(file)))) {
				for(String line; (line = br.readLine()) != null; ) {
					String[] piecesOfLine = line.split(" +");

					Chunk temp = new Chunk(piecesOfLine[0],Integer.parseInt(piecesOfLine[1]),Integer.parseInt(piecesOfLine[2]));
						degreeListSent.put(temp,Integer.parseInt(piecesOfLine[3]));
				}
				br.close();
			}
		}
	}
	
	public void addToMap(String whatmap, Chunk chunk) throws IOException {
		if(whatmap.equals("send")) degreeListSent.put(chunk,0);
		else { 
			System.err.println("invalid map type");
		}
		
		FileManagement.saveMapToFile(degreeListSent, "files\\lists\\degreeListSent.txt");

	}
	
	public void removeFromSentMap(String fileId) throws IOException {		
		for(Iterator<Map.Entry<Chunk, Integer>> it = degreeListSent.entrySet().iterator(); it.hasNext(); ) {
			Entry<Chunk, Integer> entry = it.next();
			if(entry.getKey().getFileId().equals(fileId)) {
				it.remove();
			}
		}
		
		FileManagement.saveMapToFile(degreeListSent, "files\\lists\\degreeListSent.txt");

	}
	
	public Map<Chunk, Integer> getSentMap() {
		return degreeListSent;
	}
	
	public void incConfirmations(String fileId2, int chunkNo2, Map<Chunk,Integer> m) throws IOException {
		for (Map.Entry<Chunk, Integer> entry : m.entrySet()) {
			if(entry.getKey().getFileId().equals(fileId2) && entry.getKey().getChunkNo() == chunkNo2) {
				m.put(entry.getKey(),entry.getValue() + 1);
			}
		}
		
		FileManagement.saveMapToFile(degreeListSent, "files\\lists\\degreeListSent.txt");

	}

}
