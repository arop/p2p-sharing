package ui.restoreFrame;

import java.awt.Dimension;
import java.util.ArrayList;

import javax.swing.JFrame;

import peer.PeerNew;

public class RestoreFilesWindow extends JFrame {

	private static final long serialVersionUID = 1L;

	public RestoreFilesWindow(PeerNew mainThread) {
		super();

		setTitle("Restore File");
		this.setMinimumSize(new Dimension(300,500));
		setLocationRelativeTo(null);
		setResizable(false);

		ArrayList<String> myList = mainThread.getBackedUpFiles(); 

		if (myList.isEmpty())
			System.out.println("lista é nula");

		this.add(new RestoreListPanel(myList,this,mainThread));

		this.pack();
		this.setVisible(true);
	}
}
