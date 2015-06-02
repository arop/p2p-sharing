package ui.deleteFileFrame;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;

import peer.PeerNew;

public class BackupListPanel extends JPanel {

	private static final long serialVersionUID = 1L;
	public JScrollPane listScroller;
	private PeerNew mainThread;

	public BackupListPanel(List<String> backedUpFiles, JFrame frame, PeerNew mainThread){
		super();
		this.mainThread = mainThread;

		this.setLayout(new BorderLayout());

		//TABLE OF FILES
		JTable jTable1 = new javax.swing.JTable();
		jTable1.setModel(new FinalTableModelBackupList(backedUpFiles));
		jTable1.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		jTable1.getColumnModel().getColumn(0).setPreferredWidth(100);

		//add scroll
		listScroller = new JScrollPane(jTable1);
		Dimension d = new Dimension(200,450);
		listScroller.setPreferredSize(d);
		this.add(listScroller, BorderLayout.NORTH);

		//BOTTOM BUTTONS
		JPanel bottomButtons = new JPanel();
		bottomButtons.setLayout(new BorderLayout());

		//DELETE FILES BTN
		JButton deleteFilesButton = new JButton("<html><center>Delete Selected<br>File</center></html>");		
		bottomButtons.add(deleteFilesButton);
		deleteFilesButton.addActionListener(new DeleteFileButtonListener(frame, jTable1, this.mainThread));

		this.add(bottomButtons, BorderLayout.SOUTH);
	}
}
