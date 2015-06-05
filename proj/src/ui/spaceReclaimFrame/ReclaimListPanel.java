package ui.spaceReclaimFrame;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;

import main.Chunk;
import peer.PeerNew;

public class ReclaimListPanel extends JPanel {

	private static final long serialVersionUID = 1L;
	public JScrollPane listScroller;
	private PeerNew mainThread;

	public ReclaimListPanel(JFrame frame, PeerNew mainThread){
		super();
		this.mainThread = mainThread;

		this.setLayout(new BorderLayout());

		JLabel spinnerLabel = new JLabel("Replication Degree");
		
		SpinnerNumberModel spinnerModel = new SpinnerNumberModel(64000, 0, 1E10, 10);  
		JSpinner spinner = new JSpinner(spinnerModel);
		
		this.add(spinnerLabel,BorderLayout.WEST);
		this.add(spinner,BorderLayout.EAST);

		//BOTTOM BUTTONS
		JPanel bottomButtons = new JPanel();
		bottomButtons.setLayout(new BorderLayout());

		//RECLAIM FILES BTN
		JButton reclaimFilesButton = new JButton("<html><center>Reclaim</center></html>");		
		bottomButtons.add(reclaimFilesButton);
		reclaimFilesButton.addActionListener(new ReclaimFileButtonListener(frame,this.mainThread,(Double) spinner.getValue()));

		this.add(bottomButtons, BorderLayout.SOUTH);
	}
}
