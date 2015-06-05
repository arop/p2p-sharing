package main;

import java.util.ArrayList;
import java.util.Comparator;

/**
 * Class represents a chunk
 * @author André Pires, Filipe Gama
 *
 */
public class Chunk implements Comparator<Chunk>,Comparable<Chunk>{
	private byte[] chunk;
	private String fileId;
	private String fileName;
	private int chunkNo, replicationDeg = 1;
	private ArrayList<Integer> userIDs;

	private int userWhoSent;

	public Chunk() {}
	
	public Chunk(byte[] c) {
		chunk = c.clone();
		userIDs = new ArrayList<Integer>();
	}

	public Chunk(byte[] c, int number, int degree, String fileId) {
		chunk = c.clone();
		chunkNo = number;
		replicationDeg = degree;
		this.fileId = fileId;
		userIDs = new ArrayList<Integer>();
	}
	
	public Chunk(byte[] c, int number, int degree, String fileId, int userId) {
		chunk = c.clone();
		chunkNo = number;
		replicationDeg = degree;
		this.fileId = fileId;
		userIDs = new ArrayList<Integer>();
		userWhoSent = userId;
	}

	public Chunk(String fileId, int number, int degree) {
		chunkNo = number;
		replicationDeg = degree;
		this.fileId = fileId;
		userIDs = new ArrayList<Integer>();
	}

	public byte[] getByteArray() {
		return chunk;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + chunkNo;
		result = prime * result + ((fileId == null) ? 0 : fileId.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Chunk other = (Chunk) obj;
		if (chunkNo != other.chunkNo)
			return false;
		if (fileId == null) {
			if (other.fileId != null)
				return false;
		} else if (!fileId.equals(other.fileId))
			return false;
		return true;
	}

	public void setByteArray(byte[] chunk) {
		this.chunk = chunk.clone();
	}

	public String getFileId() {
		return fileId;
	}

	public void setFileId(String fileId) {
		this.fileId = fileId;
	}

	public int getChunkNo() {
		return chunkNo;
	}

	public void setChunkNo(int chunkNo) {
		this.chunkNo = chunkNo;
	}

	public int getReplicationDeg() {
		return replicationDeg;
	}

	public void setReplicationDeg(int replicationDeg) {
		this.replicationDeg = replicationDeg;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public void addUserID(int id) {
		userIDs.add(id);
	}
	
	public ArrayList<Integer> getUserIDs() {
		return userIDs;
	}

	public void setUserIDs(ArrayList<Integer> userIDs) {
		this.userIDs = userIDs;
	}

	@Override
	public int compare(Chunk arg0, Chunk arg1) {
		return arg0.chunkNo - arg1.chunkNo;
	}
	
	@Override
	public int compareTo(Chunk c) {
		return chunkNo - c.chunkNo;
	}
	
	public int getUserWhoSent() {
		return userWhoSent;
	}

	public void setUserWhoSent(int userWhoSent) {
		this.userWhoSent = userWhoSent;
	}

}
