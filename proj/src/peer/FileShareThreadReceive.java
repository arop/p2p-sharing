package peer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.security.PrivilegedActionException;
import java.util.ArrayList;
import java.util.Base64;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLSocket;

import extra.Tools;
import user.User;

public class FileShareThreadReceive extends Thread {

	private User friend;
	private int port;

	private String fileName;
	private PeerNew mainThread;
	private long expectedFileSize;

	public FileShareThreadReceive(int friend_id, String fileName, long fileSize, PeerNew mainThread) throws IOException {
		super();

		this.mainThread = mainThread;
		this.port = -1;
		this.expectedFileSize = fileSize;

		ArrayList<User> friends = this.mainThread.getFriends();
		for (User u : friends)
			if (u.getId() == friend_id) {
				this.friend = u;
				break;
			}

		if (this.friend == null) {
			System.out.println("User who is not my friend tried to send file.");
		}

		String friendFolderPath = "files\\shared-with-me\\" + friend.getUsername() + "-" + friend.getId();
		File friendFolder = new File(friendFolderPath);
		if (!friendFolder.exists())
			friendFolder.mkdir();

		String newFilePath = friendFolderPath + "\\" + fileName;
		File newFile = new File(newFilePath);

		int i = 0;
		String[] splitFileName = Tools.splitFileExtension(fileName); 

		while (true){
			if (!newFile.exists()){
				newFile.createNewFile();
				this.fileName = newFilePath;
				break;
			}
			else{
				i++;
				newFilePath = friendFolderPath + "\\" + splitFileName[0] + "-" + i + "." + splitFileName[1];
				newFile = new File(newFilePath);
			}
		}
	}

	@Override
	public void run() {
		SSLServerSocket sslServerSocket = null;
		SSLSocket sslSocket = null;

		try {
			sslServerSocket = mainThread.getServerSocket(0);

			if (sslServerSocket == null) {
				throw new Exception("Failed to create socket to receive file");
			} else {

				this.setPort(sslServerSocket.getLocalPort());

				sslSocket = (SSLSocket) sslServerSocket.accept();

				// Create Input / Output Streams for communication with the
				PrintWriter out = new PrintWriter(sslSocket.getOutputStream(), true); // vai responder por aqui
				BufferedReader in = new BufferedReader(new InputStreamReader(sslSocket.getInputStream())); // lê daqui

				OutputStream writer = new FileOutputStream(this.fileName);
				Base64.Decoder dec = Base64.getDecoder();
				boolean fail = false;

				while(!fail){
					String inputLine = in.readLine();

					int index = inputLine.indexOf("#");
					if (index < 0){
						System.out.println("Invalid line received from "+ friend.getUsername());
						fail = true;
					}
					else {
						String lineType = inputLine.substring(0, index);
						if(lineType.equals("LINE")){
							byte[] lineDecoded = dec.decode(inputLine.substring(index+1, inputLine.length()));
							writer.write(lineDecoded);
							out.println("OK");
						}
						else if(lineType.equals("END")){
							System.out.println("Received file! Shared by: " + friend.getUsername());

							break;
						}
						else{
							System.out.println("Invalid line type received from "+ friend.getUsername() + ": " + lineType);
							fail = true;
						}
					}
				}

				writer.close();

				if (fail == true){
					//TODO abrir pop up erro. comunicar falha.
					out.println("FAIL"); 
				}
				else{
					//Check if file size is correct.
					File f = new File(this.fileName);
					if (this.expectedFileSize == f.length()){
						//TODO abrir pop up sucesso
						out.println("OK"); //confirms to other user that file was successfully received;
					}
					else{
						out.println("FAIL"); 
					}						
				}

				// Close the streams and the socket
				out.close();
				in.close();
				// sslSocket.close();
				sslServerSocket.close();
			}
		} catch (Exception exp) {
			PrivilegedActionException priexp = new PrivilegedActionException(
					exp);
			System.out.println(" Priv exp --- " + priexp.getMessage());
			System.out.println(" Exception occurred .... " + exp);
			exp.printStackTrace();
		}
	}

	public synchronized int getPort() {
		return port;
	}

	public synchronized void setPort(int port) {
		this.port = port;
	}

}
