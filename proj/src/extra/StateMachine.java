package extra;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

public class StateMachine {
	
//	final ArrayList<String> messagesWithBody = new ArrayList<String>(
//		    Arrays.asList("ONLINEUSERS","FRIENDS","PUTCHUNK", "CHUNK", "GETUSER","LOGIN","LOGINFACEBOOK","USERS","OK","REGISTER","ADDFRIENDS","BACKUPFILE"));

	public String stateMachine(BufferedReader in) throws IOException {
		String response;
		int state = 0;
		boolean firstTime = true;

		response = "";
		char nextChar = 0;

		System.out.println("ENCONTRO-mE NO EXTERIOR");
		
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
				int size = 0;
				String[] parts = response.split(" +");
				
				System.out.println("Partes0 : " + parts[0]);
				
				if(parts[0].equals("NOTRESPOND")) size = Integer.parseInt(parts[2].trim());
				else size =  Integer.parseInt(parts[1].trim());
				response += readNumberChars(size,in);
				in.readLine();
				in.readLine();
				break;
			}
			else state = 0;
		}

		//firstTime = true;
		return response;
	}

	public String readNumberChars(int size,BufferedReader in) throws IOException{
		char nextChar = 0;
		String response = "";

		for(int i = 0; i < size; i++) {
			nextChar = (char) in.read();
			response += nextChar;
		}
		return response;
		
	}





}
