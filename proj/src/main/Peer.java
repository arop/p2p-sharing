package main;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import cli.Interface;
import extra.DegreeMonitorThread;
import extra.FileManagement;
import extra.Tools;
import receive.ReceiveThread_MC;
import receive.ReceiveThread_MDB;
import receive.ReceiveThread_MDR;
import send.SendThread;

/**
 * Main class, representing the peer itself
 * @author André Pires, Filipe Gama
 *
 */
public class Peer extends Thread {

	private String MC_IP, MDB_IP, MDR_IP;
	private int MC_Port, MDB_Port, MDR_Port;

	private DegreeMonitorThread degMonitor;
	private SendThread snd_MC;
	private ReceiveThread_MC rcv_MC;
	private ReceiveThread_MDB rcv_MDB;
	private ReceiveThread_MDR rcv_MDR;
	
	private ArrayList<Chunk> chunklist;
	
	private ArrayList<Integer> portList;
	private ArrayList<String> ipList;
	
	private Map <String, String> backupList;
	private Map <String, Integer> backupList2;

	private ArrayList<Chunk> chunksReceived;
	private int chunksToReceive; 
	private String fileToRecover;
	private boolean isBackuping;
	
	private ArrayList<Chunk> storedMessagesReceived;
	
	boolean hasReceivedPutchunk;
	
	Peer(int mC_Port2, int mDB_Port2, int mDR_Port2, String mc_ip, String mbd_ip,String mdr_ip) throws IOException, InterruptedException {
		MC_Port = mC_Port2;
		MDB_Port = mDB_Port2;
		MDR_Port = mDR_Port2;
		MC_IP = mc_ip;
		MDB_IP = mbd_ip;
		MDR_IP = mdr_ip;

		snd_MC = new SendThread(this);
		rcv_MC = new ReceiveThread_MC(this);
		rcv_MDB = new ReceiveThread_MDB(this);
		rcv_MDR = new ReceiveThread_MDR(this);

		//degMonitor = new DegreeMonitorThread(this);

		storedMessagesReceived = new ArrayList<Chunk>();
		
		chunksReceived = new ArrayList<Chunk>();
		portList = new ArrayList<Integer>();
		ipList = new ArrayList<String>();

		chunksToReceive = 0;
		hasReceivedPutchunk = false;

		chunklist = new ArrayList<Chunk>();
		backupList = new HashMap<String, String>();
		backupList2 = new HashMap<String, Integer>();

		loadChunkList();
		loadBackupList();

		this.start();

		//degMonitor.start();
		rcv_MC.start();
		rcv_MDB.start();

		snd_MC.start();
	}


	public static void main(String[] args) throws IOException, InterruptedException {
		if(args.length != 6) {
			Interface.printUsage();
			return;
		}

		String MC_IP = null, MDB_IP = null, MDR_IP = null;

		if(Tools.validIP(args[0])) MC_IP = args[0];
		else System.err.println("Invalid MC_IP");

		if(Tools.validIP(args[2])) MDB_IP = args[2];
		else System.err.println("Invalid MDB_IP");

		if(Tools.validIP(args[4])) MDR_IP = args[4];
		else System.err.println("Invalid MDR_IP");

		int MC_Port = Integer.parseInt(args[1]);
		int MDB_Port = Integer.parseInt(args[3]);
		int MDR_Port = Integer.parseInt(args[5]);

		generateFolders();

		new Peer(MC_Port,MDB_Port,MDR_Port,MC_IP,MDB_IP,MDR_IP);
	}

	public void setHasReceivedPutchunk(boolean hasReceivedPutchunk) {
		this.hasReceivedPutchunk = hasReceivedPutchunk;
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
	}

	public void addToBackupList(String string, String fId,int numberOfChunks) {
		if(!isBackedUp(string)) {
			backupList.put(string, fId);
			backupList2.put(string, numberOfChunks);
		}
	}

	public void refreshBackupList() throws IOException {
		/*if(backupList.isEmpty()) {
			PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter("files\\lists\\backup_list.txt")));
			out.close();
		}
		else for (Map.Entry<String, String> entry : backupList.entrySet()) {
			//FileManagement.addToBackupListFile(entry.getKey(), entry.getValue(), backupList2.get(entry.getKey()));
		}*/
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
	 */
	public void deleteFromSentLists(String fileId) {
		String filename = getFilename(fileId);
		backupList.remove(filename);
		backupList2.remove(filename);

		degMonitor.removeFromSentMap(fileId);
	}

	/**
	 * Deletes Chunk with maximum replication degree ratio
	 * @throws IOException
	 */
	public void deleteWorstChunk() throws IOException {
		Chunk chunk = degMonitor.mostRedundant();

		if(chunk != null) {
			String filename = chunk.getFileId() + "_chunk_" + chunk.getChunkNo();

			File f = new File("files\\backups\\"+filename);
			System.gc();
			f.delete(); 

			degMonitor.removeFromStoredMap(chunk);

			snd_MC.sendMessage(Tools.generateMessage("REMOVED", chunk),MC_IP,MC_Port);
		}

		//FileManagement.saveMapToFile(degMonitor.getStoredMap(),"files\\lists\\degreeListStored.txt");

		/* reloads chunk list */
		chunklist.clear();
		loadChunkList();
	}

	public int getNumberOfStoreds(Chunk temp) {
		return Collections.frequency(storedMessagesReceived,temp);
	}

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
	 * Checks if it has to send a putchunk message
	 * @param filename
	 * @param number
	 * @throws InterruptedException
	 */
	public void compensate(String filename, int number) throws InterruptedException {
		Chunk temp = searchChunk(filename, number);

		if(temp != null && degMonitor.getConfirmations(temp.getFileId(), temp.getChunkNo(), this.degMonitor.getStoredMap()) < temp.getReplicationDeg()) {
			Random r = new Random();
			Thread.sleep(r.nextInt(401));

			if(hasReceivedPutchunk) 
				snd_MC.sendMessage(Tools.generateMessage("PUTCHUNK", temp),MDB_IP,MDB_Port);
		}
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

	/**
	 * @param port
	 * @param ip 
	 * @return true if added, false if already in list
	 */
	public boolean addPortToList(int port, String ip) {
		for(int i = 0; i < portList.size(); i++) {
			if(port == portList.get(i) && ip.equals(ipList.get(i))) {
				return false;
			}
		}

		portList.add(port);
		ipList.add(ip);
		return true;
	}

	public void cleanPorts() {
		portList.clear();
	}
	
	public void startNewRcv_MDR() {
		rcv_MDR = new ReceiveThread_MDR(this);
		rcv_MDR.start();
	}

	/////////////////////////////////// GETTERS AND SETTERS //////////////////////////////////////////////////////////////

	public int getPeerPort() { 
		return snd_MC.getSenderSocket().getLocalPort();
	}
	
	public void setChunksToReceive(int i) {
		chunksToReceive = i;
	}

	public int getChunksToReceive() {
		return chunksToReceive;
	}

	public void addToReceivedChunks(Chunk c) {
		chunksReceived.add(c);
	}

	public void clearReceivedChunks() {
		chunksReceived.clear();
	}

	public void clearChunkList() {
		chunklist.clear();
	}

	public ArrayList<Chunk> getReceivedChunks() {
		return chunksReceived;
	}

	public String getFileIdOf(String string) {
		return this.backupList.get(string);
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

	public String getMC_IP() {
		return MC_IP;
	}

	public void setMC_IP(String mC_IP) {
		MC_IP = mC_IP;
	}

	public int getMC_Port() {
		return MC_Port;
	}

	public void setMC_Port(int mC_Port) {
		MC_Port = mC_Port;
	}

	public SendThread getSnd_MC() {
		return snd_MC;
	}

	public void setSnd_MC(SendThread snd_MC) {
		this.snd_MC = snd_MC;
	}

	public ReceiveThread_MC getRcv_MC() {
		return rcv_MC;
	}

	public void setRcv_MC(ReceiveThread_MC rcv_MC) {
		this.rcv_MC = rcv_MC;
	}

	public void setFileToRecover(String filename) {
		fileToRecover = filename;
	}

	public String getFileToRecover() {
		return fileToRecover;
	}

	public String getMDR_IP() {
		return MDR_IP;
	}

	public String getMDB_IP() {
		return MDB_IP;
	}

	public int getMDR_Port() {
		return MDR_Port;
	}

	public int getMDB_Port() {
		return MDB_Port;
	}

	public Map<String, String> getBackupList() {
		return backupList;
	}

	public Map<String, Integer> getBackupList2() {
		return backupList2;
	}

	public DegreeMonitorThread getDegMonitor() {
		return degMonitor;
	}

	public void setDegMonitor(DegreeMonitorThread degMonitor) {
		this.degMonitor = degMonitor;
	}

	public void addStoredChunk(Chunk ficticio) {
		storedMessagesReceived.add(ficticio);
	}
	
	public void removeChunkFromStoredArray(Chunk ficticio) {
		while(storedMessagesReceived.remove(ficticio)) { }
	}

	public boolean isBackuping() {
		return isBackuping;
	}

	public void setBackuping(boolean isBackuping) {
		this.isBackuping = isBackuping;
	}
	
}