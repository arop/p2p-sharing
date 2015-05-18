package cli;

import java.io.File;
import java.util.ArrayList;
import java.util.Scanner;

import extra.FileManagement;
import extra.Tools;

/**
 * Class used for functions not directly related to the (sub)protocols
 * @author André Pires, Filipe Gama
 *
 */
public abstract class Interface {

	/**
	 * Prints usage
	 */
	public static void printUsage() {
		System.out.println("Usage: <MC_IP> <MC_Port> <MDB_IP> <MDB_PORT> <MDR_IP> <MDR_Port>");
		System.out.println("	<MC_IP>  <MC_Port>:  IP multicast address and port of control channel");
		System.out.println("	<MDB_IP> <MDB_PORT>: IP multicast address and port of data backup channel");
		System.out.println("	<MDR_IP> <MDR_Port>: IP multicast address and port of data restore channel");
	}

	/**
	 * Main menu
	 * @return
	 */
	public static ArrayList<String> menu() {
		System.out.println("Backup service: ");
		System.out.println("1. Backup file");
		System.out.println("2. Restore file");
		System.out.println("3. Delete file");
		System.out.println("4. Reclaim space");
		System.out.println("5. Change debug mode");
		System.out.println("6. Exit");

		@SuppressWarnings("resource")
		Scanner in = new Scanner(System.in);

		System.out.println("Choose a service: ");
		String s = in.nextLine();	

		return menu1( Integer.parseInt(s));
	}

	/**
	 * Sub menu
	 * @param choice
	 * @return
	 */
	static ArrayList<String> menu1(int choice) {
		@SuppressWarnings("resource")
		Scanner in = new Scanner(System.in);
		String s;
		ArrayList<String> res = new ArrayList<String>();

		switch(choice) {
		case 1:
			System.out.println("Backup file");
			System.out.println("Enter a file: ");
			s = in.nextLine();
			System.out.println("Enter desired replication degree:");
			String s1 = in.nextLine();

			if(FileManagement.fileExists(s)) {
				res.add("backup");
				res.add(s);
				res.add(s1);
			}
			else {
				res.add("backup");
				res.add("failed");
			}

			break;
		case 2:
			System.out.println("Restore file");
			System.out.println("Enter a file: ");
			s = in.nextLine();
			
			res.add("restore");
			res.add(s);

			break;
		case 3:
			System.out.println("Delete file");
			System.out.println("Enter a file: ");
			s = in.nextLine();

			res.add("delete");
			res.add(s);

			break;
		case 4:
			System.out.println("Reclaim space");
			System.out.println("Maximum size: " + Tools.getFolderSize());
			System.out.println("Current size: " + Tools.folderSize(new File("files\\backups")));
			System.out.println("Enter maximum size: ");
			s = in.nextLine();
			
			res.add("reclaim");
			res.add(s);

			break;
		case 5:
			System.out.println("Current debug mode: " + Tools.isDebug());
			System.out.println("New mode: " + !Tools.isDebug());
			
			res.add("debug");
			res.add("debug");
			break;
		case 6:
			res.add("exit");
			res.add("exit");
			break;
		default:
			System.out.println("Invalid choice!\n");
			break;
		}

		return res;
	}
}
