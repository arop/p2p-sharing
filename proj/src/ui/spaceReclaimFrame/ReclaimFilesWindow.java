package ui.spaceReclaimFrame;

import java.awt.Dimension;
import java.util.ArrayList;

import javax.swing.JFrame;

import main.Chunk;
import peer.PeerNew;

public class ReclaimFilesWindow extends JFrame {

	private static final long serialVersionUID = 1L;

	public ReclaimFilesWindow(PeerNew mainThread) {
		super();

		setTitle("Reclaim Space");
		this.setMinimumSize(new Dimension(300,100));
		setLocationRelativeTo(null);
		setResizable(false);

		ArrayList<Chunk> chunksSaved = mainThread.getChunklist(); 

		if (chunksSaved.isEmpty())
			System.out.println("lista é nula");

		this.add(new ReclaimListPanel(this,mainThread));

		this.pack();
		this.setVisible(true);
	}
}
