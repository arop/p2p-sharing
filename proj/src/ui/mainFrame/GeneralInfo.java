package ui.mainFrame;

import java.awt.Dimension;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import peer.PeerNew;

public class GeneralInfo extends JPanel {
	
	private static final long serialVersionUID = 1L;
	
	private PeerNew mainThread;
	
	JLabel numBackupFiles;
	JLabel totalSize;
	JLabel numFriends;
	JLabel numFriendsOn;
	JLabel serverOn;
	
	JButton refreshButton;

	public GeneralInfo(int width, int height,PeerNew peer){
		super();
		mainThread = peer;
		this.setPreferredSize(new Dimension(width, height));
		//this.setLayout(new GridLayout(0,1));
		
		numBackupFiles = new JLabel("number of files backed up: "+mainThread.getBackupList().size());
		totalSize = new JLabel("total size backed up: 1203MB");
		numFriends = new JLabel("number of friends: 15");
		numFriendsOn = new JLabel("number of friends online: 3");
		serverOn = new JLabel("server online: YES");
		
		this.add(numBackupFiles);
		this.add(totalSize);
		this.add(numFriends);
		this.add(numFriendsOn);
		this.add(serverOn);
		
		int buttonWidth = width;    	
    	int buttonHeight = 90;
		
		refreshButton = new JButton("Refresh");
    	refreshButton.setPreferredSize(new Dimension(buttonWidth, buttonHeight));
    	this.add(refreshButton);
	}
	
	public void setNumBackupFiles(int num) {
		numBackupFiles.setText("number of files backed up: " + mainThread.getBackupList().size());
	}

	public JButton getRefreshButton() {
		return refreshButton;
	}
}
