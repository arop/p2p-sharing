package ui.restoreFrame;

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

public class RestoreListPanel extends JPanel {

	private static final long serialVersionUID = 1L;
	public JScrollPane listScroller;
	private PeerNew mainThread;

	public RestoreListPanel(List<String> backedUpFiles, JFrame frame, PeerNew mainThread){
		super();
		this.mainThread = mainThread;

		this.setLayout(new BorderLayout());

		//TABLE OF FILES
		JTable jTable1 = new javax.swing.JTable();
		jTable1.setModel(new FinalTableModelRestoreList(backedUpFiles));
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

		//RESTORE FILES BTN
		JButton restoreFilesButton = new JButton("<html><center>Restore Selected<br>File</center></html>");		
		bottomButtons.add(restoreFilesButton);
		restoreFilesButton.addActionListener(new RestoreFileButtonListener(frame, jTable1, this.mainThread));

		this.add(bottomButtons, BorderLayout.SOUTH);
	}
}
