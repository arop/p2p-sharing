package ui.mainFrame;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import peer.PeerNew;
import ui.deleteFileFrame.DeleteFilesWindow;
import ui.restoreFrame.RestoreFilesWindow;
import user.User;
import extra.Tools;

public class GUI extends JFrame {
	private static final long serialVersionUID = 1L;

	private PeerNew mainThread;

	private GeneralInfo generalInfo;

	public GUI(PeerNew peer) {
		mainThread = peer;
		initUI();
	}

	public List<User> friendsList;

	private void initUI() {

		setTitle("Distributed Backup System");
		this.setMinimumSize(new Dimension(600,500));
		setLocationRelativeTo(null);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setResizable(false);

		JPanel friendshipsPanel = new JPanel();
		friendshipsPanel.setLayout(new BorderLayout());

		FriendsPanel friendsPanel = new FriendsPanel(mainThread);
		friendshipsPanel.add(friendsPanel, BorderLayout.WEST);

		this.add(generateButtonsPanel(150, 500), BorderLayout.WEST);
		this.add(friendshipsPanel, BorderLayout.EAST);
		generalInfo = new GeneralInfo(100,500, mainThread);

		generalInfo.getRefreshButton().addActionListener(new ActionListener()
		{
			@Override	
			public void actionPerformed(ActionEvent e){
				generalInfo.setNumBackupFiles(mainThread.getBackupList().size());
				generalInfo.setNumberOfFriends(mainThread.getFriends().size());
				generalInfo.setSizeBackedUp(mainThread.getFriends().size());
				generalInfo.setNumberOnlineFriends(mainThread.getNumberOfFriendsOnline());
			}
		});

		this.add(generalInfo, BorderLayout.CENTER);

		this.pack();
	}

	private JPanel generateButtonsPanel(int panelWidth, int panelHeight){
		JPanel panel = new JPanel();
		panel.setPreferredSize(new Dimension(panelWidth, panelHeight));

		int buttonWidth = panelWidth;    	
		int buttonHeight = 90;
		
		JPanel repDegreePanel = new JPanel();
		
		JLabel spinnerLabel = new JLabel("Replication Degree");
		
		SpinnerNumberModel spinnerModel = new SpinnerNumberModel(1, 1, 5, 1);  
		JSpinner spinner = new JSpinner(spinnerModel);
		
		repDegreePanel.add(spinnerLabel,BorderLayout.WEST);
		repDegreePanel.add(spinner,BorderLayout.EAST);
		
		panel.add(repDegreePanel);

		JButton button = new JButton("Backup File");
		button.setPreferredSize(new Dimension(buttonWidth, buttonHeight));
		panel.add(button);
		button.addActionListener(new ActionListener()
		{
			@Override	
			public void actionPerformed(ActionEvent e){
				String filePath;
				if( (filePath = Tools.selectFileFrame()) != null){
					System.out.println("This file was selected: "+filePath);
					try {
						mainThread.startRegularBackupProtocol(filePath, (int) spinner.getValue());
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				}
			}
		});

		button = new JButton("<html><center>Restore<br>File</center></html>");
		button.setPreferredSize(new Dimension(buttonWidth, buttonHeight));
		panel.add(button);

		button.addActionListener(new ActionListener()
		{
			@Override	
			public void actionPerformed(ActionEvent e){
				new RestoreFilesWindow(mainThread);
			}
		});

		button = new JButton("Delete File");
		button.setPreferredSize(new Dimension(buttonWidth, buttonHeight));
		panel.add(button);
		button.addActionListener(new ActionListener()
		{
			@Override	
			public void actionPerformed(ActionEvent e){
				new DeleteFilesWindow(mainThread);
			}
		});

		button = new JButton("<html><center>Reclaim<br>Space<center></html>");
		button.setPreferredSize(new Dimension(buttonWidth, buttonHeight));
		panel.add(button);

		return panel;
	}

	public static void main(String[] args) {

		EventQueue.invokeLater(new Runnable() {

			@Override
			public void run() {
				GUI ex = new GUI(null);
				ex.setVisible(true);
			}
		});
	}

	public GeneralInfo getGeneralInfo() {
		return generalInfo;
	}

	public void setGeneralInfo(GeneralInfo generalInfo) {
		this.generalInfo = generalInfo;
	}
}