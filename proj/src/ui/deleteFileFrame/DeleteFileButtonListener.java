package ui.deleteFileFrame;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import peer.PeerNew;

public class DeleteFileButtonListener implements ActionListener {
	private BackupListPanel panel;
	private JTable jtable;
	private JScrollPane jscroller;
	private PeerNew mainThread;
	private JButton jButton;

	public DeleteFileButtonListener(BackupListPanel panel, JTable jtable, JScrollPane jscroller, JButton button, PeerNew mainThread){
		this.panel = panel;
		this.jscroller = jscroller;
		this.jtable = jtable;
		this.mainThread = mainThread;
		this.jButton = button;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		int[] selection = jtable.getSelectedRows();
		String[] filenames = new String[selection.length];
		for (int i = 0; i < selection.length; i++){
			filenames[i] = (String) jtable.getModel().getValueAt(selection[i], 2);
		}

		for(int i = 0; i < filenames.length; i++) {
			try {
				mainThread.startDeleteChunks(filenames[i]);
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}

		panel.remove(jscroller);
		panel.repaint();

		//JLabel label = new JLabel(message);
		//panel.add(label, BorderLayout.NORTH);
		panel.validate();

		this.jButton.setEnabled(false);
	}
}
