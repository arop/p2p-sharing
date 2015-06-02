package ui.deleteFileFrame;

import java.awt.Dimension;
import java.util.ArrayList;

import javax.swing.JFrame;

import peer.PeerNew;

public class DeleteFilesWindow extends JFrame {

	private static final long serialVersionUID = 1L;

	public DeleteFilesWindow(PeerNew mainThread) {
		super();

		setTitle("Delete Files");
		this.setMinimumSize(new Dimension(300,500));
		setLocationRelativeTo(null);
		setResizable(false);

		ArrayList<String> myList = mainThread.getBackedUpFiles(); 

		if (myList.isEmpty())
			System.out.println("lista é nula");
		else{
			for(Object u : myList){
				System.out.println(u);
			}
		}

		this.add(new BackupListPanel(myList,mainThread));

		this.pack();
		this.setVisible(true);
	}
}
