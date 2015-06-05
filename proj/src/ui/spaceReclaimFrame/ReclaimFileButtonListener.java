package ui.spaceReclaimFrame;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JTable;

import main.Chunk;
import peer.PeerNew;

public class ReclaimFileButtonListener implements ActionListener {
	private JTable jtable;
	private PeerNew mainThread;
	private JFrame frame;
	private Double sizeToRemove;

	public ReclaimFileButtonListener(JFrame frame, PeerNew mainThread,Double sizeToRemove){
		this.mainThread = mainThread;
		this.frame = frame;
		this.sizeToRemove = sizeToRemove;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		mainThread.reclaimSpace(sizeToRemove);	
		frame.dispose();
	}
}
