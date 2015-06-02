package ui.deleteFileFrame;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.JTable;

import peer.PeerNew;

public class DeleteFileButtonListener implements ActionListener {
	private JTable jtable;
	private PeerNew mainThread;
	private JFrame frame;

	public DeleteFileButtonListener(JFrame frame, JTable jtable, PeerNew mainThread){
		this.jtable = jtable;
		this.mainThread = mainThread;
		this.frame = frame;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		int[] selection = jtable.getSelectedRows();
		String[] filenames = new String[selection.length];
		for (int i = 0; i < selection.length; i++){
			filenames[i] = (String) jtable.getModel().getValueAt(selection[i], 0);
		}

		for(int i = 0; i < filenames.length; i++) {
			try {
				mainThread.startDeleteChunks(filenames[i]);
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
		frame.dispose();
	}
}
