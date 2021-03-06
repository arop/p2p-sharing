package ui.deleteFileFrame;

import java.awt.Dimension;
import java.util.ArrayList;

import javax.swing.JFrame;

import peer.PeerNew;

public class DeleteFilesWindow extends JFrame {

	private static final long serialVersionUID = 1L;

	public DeleteFilesWindow(PeerNew mainThread) {
		super();

		setTitle("Delete File");
		this.setMinimumSize(new Dimension(300,500));
		setLocationRelativeTo(null);
		setResizable(false);

		ArrayList<String> myList = mainThread.getBackedUpFiles(); 

		if (myList.isEmpty())
			System.out.println("lista � nula");

		this.add(new BackupListPanel(myList,this,mainThread));

		this.pack();
		this.setVisible(true);
	}
}
