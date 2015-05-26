package extra;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import main.Chunk;

/**
 * Class used to check replication degrees
 * @author André Pires, Filipe Gama
 *
 */
public class DegreeMonitorThread extends Thread {
	private Map <Chunk, Integer> degreeListSent;
	private Map <Chunk, Integer> degreeListStored;

	public DegreeMonitorThread() throws IOException {
		degreeListSent = new HashMap<Chunk, Integer>();
		degreeListStored = new HashMap<Chunk, Integer>();

		if(degreeListSent.isEmpty()) {
			readDegreeList("files\\lists\\degreeListSent.txt");
		}

		if(degreeListStored.isEmpty()) {
			readDegreeList("files\\lists\\degreeListStored.txt");
		}
	}

	@Override
	public void run() {
		while(true) {
			try {
				Thread.sleep(10000);
				FileManagement.saveMapToFile(degreeListSent, "files\\lists\\degreeListSent.txt");
				FileManagement.saveMapToFile(degreeListStored, "files\\lists\\degreeListStored.txt");
				//analyzeDegrees();
			} catch (IOException | InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public boolean hasSent(Chunk c) {
		return degreeListSent.containsKey(c);
	}

	public void readDegreeList(String file) throws IOException {
		if(!FileManagement.fileExists(file))
			(new File(file)).createNewFile();
		else {
			try(BufferedReader br = new BufferedReader(new FileReader(new File(file)))) {
				for(String line; (line = br.readLine()) != null; ) {
					String[] piecesOfLine = line.split(" +");

					Chunk temp = new Chunk(piecesOfLine[0],Integer.parseInt(piecesOfLine[1]),Integer.parseInt(piecesOfLine[2]));
					if(file.contains("Sent"))
						degreeListSent.put(temp,Integer.parseInt(piecesOfLine[3]));
					else
						degreeListStored.put(temp,Integer.parseInt(piecesOfLine[3]));
				}
				br.close();
			}
		}
	}

	public void incConfirmations(String fileId2, int chunkNo2, Map<Chunk,Integer> m) {
		for (Map.Entry<Chunk, Integer> entry : m.entrySet()) {
			if(entry.getKey().getFileId().equals(fileId2) && entry.getKey().getChunkNo() == chunkNo2) {
				m.put(entry.getKey(),entry.getValue() + 1);
			}
		}
	}

	public void addToMap(String whatmap, Chunk chunk) {
		if(whatmap.equals("send")) degreeListSent.put(chunk,0);
		else if(whatmap.equals("stored")) degreeListStored.put(chunk,1);
		else { 
			System.err.println("invalid map type");
		}
	}

	public void decConfirmations(String fileId2, int chunkNo2, Map<Chunk,Integer> m) {
		for (Map.Entry<Chunk, Integer> entry : m.entrySet()) {
			if(entry.getKey().getFileId().equals(fileId2) && entry.getKey().getChunkNo() == chunkNo2) {
				m.put(entry.getKey(),entry.getValue()-1);
			}
		}
	}

	public int getConfirmations(String fileId2, int chunkNo2, Map<Chunk,Integer> m) {
		for (Map.Entry<Chunk, Integer> entry : m.entrySet()) {
			if(entry.getKey().getFileId().equals(fileId2) && entry.getKey().getChunkNo() == chunkNo2) {
				return entry.getValue();
			}
		}
		return -1;
	}

	public void removeFromStoredMap(Chunk chunk) {
		degreeListStored.remove(chunk);
	}   

	public void removeFromSentMap(String fileId) {		
		for(Iterator<Map.Entry<Chunk, Integer>> it = degreeListSent.entrySet().iterator(); it.hasNext(); ) {
			Entry<Chunk, Integer> entry = it.next();
			if(entry.getKey().getFileId().equals(fileId)) {
				it.remove();
			}
		}
	}

	public boolean hasSentFile(String fileId) {
		for (Map.Entry<Chunk, Integer> entry : degreeListSent.entrySet()) {
			if(entry.getKey().getFileId().equals(fileId)) {
				return true;
			}
		}
		return false;
	}

	public void removeFromStoredMap(String fileId) {		
		for(Iterator<Map.Entry<Chunk, Integer>> it = degreeListStored.entrySet().iterator(); it.hasNext(); ) {
			Entry<Chunk, Integer> entry = it.next();
			if(entry.getKey().getFileId().equals(fileId)) {
				it.remove();
			}
		}
	}	

	public Chunk mostRedundant() {
		Chunk res = null;
		int last = -9999;

		for (Map.Entry<Chunk, Integer> entry : degreeListStored.entrySet()) {
			if(degreeListStored.size() == 1) {
				res = entry.getKey();
			}

			if(entry.getValue() - entry.getKey().getReplicationDeg() > last) {
				last = entry.getValue()-entry.getKey().getReplicationDeg();
				res = entry.getKey();
			}
		}
		return res;
	}

	public Map<Chunk, Integer> getSentMap() {
		return degreeListSent;
	}

	public Map<Chunk, Integer> getStoredMap() {
		return degreeListStored;
	}

	/**
	 * Function analyzes the replication degrees of the sent chunks, if they are not met it is sent a new putchunk message for that chunk
	 * @throws FileNotFoundException
	 * @throws NoSuchAlgorithmException
	 * @throws InterruptedException
	 */
	/*public void analyzeDegrees() throws FileNotFoundException, NoSuchAlgorithmException, InterruptedException {
		if(!main.isBackuping()) {
			for (Map.Entry<Chunk, Integer> entry : degreeListSent.entrySet()) {
				if(entry.getValue() < entry.getKey().getReplicationDeg() ) {
					ArrayList<Chunk> temp = new ArrayList<Chunk>();

					if(main.getFilename( entry.getKey().getFileId()) != null) {
						temp = FileManagement.splitFile(main.getFilename( entry.getKey().getFileId()),entry.getKey().getReplicationDeg());
					}

					for(int i = 0; i < temp.size(); i++) {
						if(temp.get(i).equals(entry.getKey())){
							Thread.sleep(1000);
							main.cleanPorts();
							main.getSnd_MC().sendMessage(Tools.generateMessage("PUTCHUNK", temp.get(i)), main.getMDB_IP(), main.getMDB_Port());
						}
					}
				}
			}
		}
	}*/

}
