package peer;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Base64;

import javax.net.ssl.SSLSocket;

import org.apache.commons.lang.ArrayUtils;

import user.User;

public class FileShareThreadSend extends Thread{

	private User friend;
	private String filePath;
	private PeerNew mainThread;
	private SSLSocket socket;

	public FileShareThreadSend(User friend, String filePath, int friendPortForShare, PeerNew mainThread) throws Exception {
		this.friend = friend;
		this.filePath = filePath;
		this.mainThread = mainThread;

		this.socket = mainThread.getSocketConnection(friend.getIp(), friendPortForShare);
		if (this.socket == null)
			throw new Exception ("Erro creating socket to share file with friend. Addr: "+friend.getIp() + " / Port: " + friendPortForShare);
	}

	@Override
	public void run() {
		PrintWriter out = null;
		BufferedReader in = null;
		String response = null;

		try {
			socket.setSoTimeout(5000);

			out = new PrintWriter(socket.getOutputStream(), true);
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

			InputStream br = new FileInputStream(this.filePath);

			int maxBufferSize = 200000;
			byte[] buffer = new byte[maxBufferSize];
			int numChars;
			Base64.Encoder enc = Base64.getEncoder();
			boolean fail = false;

			while ((numChars = br.read(buffer, 0, maxBufferSize)) > 0 ) { //200KB for each iteration
				String encodedLine = enc.encodeToString(ArrayUtils.subarray(buffer, 0, numChars));
				//SEND MESSAGE
				out.println("LINE#"+encodedLine);

				//GET RESPONSE 
				response = in.readLine();
				if (!response.equals("OK")){
					fail = true;
					break;
				}
			}

			if (!fail){
				//sucesso
				out.println("END#");
				response = in.readLine();
				if (!response.equals("OK")){
					//friend não recebeu correctamente o ficheiro
					System.out.println("Friend hasn't received file correctly (file sizes don't match)");
					System.out.println("	file name: "+this.filePath);
					System.out.println("	friend name: "+this.friend.getUsername());
				}
				else{
					System.out.println("File successfully shared with "+this.friend.getUsername());
				}
			}
			else{
				//algo falhou
				System.out.println("Friend hasn't received file correctly.");
				System.out.println("	file name: "+this.filePath);
				System.out.println("	friend name: "+this.friend.getUsername());
			}

			// Closing the Streams and the Socket
			br.close();
			out.close();
			in.close();
			socket.close();		
		} 
		catch (Exception e) {
			System.out.println("#Unexpected exception sharing file with friend...");
			System.out.println("Exception message: "+e.getMessage());
			e.printStackTrace();
		}
	}
}
