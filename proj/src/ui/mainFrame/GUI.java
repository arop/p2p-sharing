package ui.mainFrame;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

import peer.PeerNew;
import ui.deleteFileFrame.DeleteFilesWindow;
import user.User;
import extra.Tools;
import friends.FriendCircle;

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
		this.setMinimumSize(new Dimension(700,500));
		//setSize(300, 200);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setResizable(false);

		List<FriendCircle> circles = new ArrayList<FriendCircle>();
		/*circles.add(new FriendCircle("Circle 1", friendsList.subList(2, 6)));
        circles.add(new FriendCircle("Circle 2", friendsList.subList(13, 17)));
        circles.add(new FriendCircle("Circle 3", friendsList.subList(4, 7)));*/

		JPanel friendshipsPanel = new JPanel();
		friendshipsPanel.setLayout(new BorderLayout());

		FriendsPanel friendsPanel = new FriendsPanel(mainThread);
		friendshipsPanel.add(friendsPanel, BorderLayout.WEST);

		CirclesPanel circlesPanel = new CirclesPanel(circles);
		friendshipsPanel.add(circlesPanel, BorderLayout.EAST);


		this.add(generateButtonsPanel(100, 500), BorderLayout.WEST);
		this.add(friendshipsPanel, BorderLayout.EAST);
		generalInfo = new GeneralInfo(100,500, mainThread);

		generalInfo.getRefreshButton().addActionListener(new ActionListener()
		{
			@Override	
			public void actionPerformed(ActionEvent e){
				generalInfo.setNumBackupFiles(mainThread.getBackupList().size());
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
						mainThread.startRegularBackupProtocol(filePath, 1);
					} catch (IOException e1) {
						e1.printStackTrace();
					} //TODO replication degree hardcoded
				}
			}
		});

		button = new JButton("<html><center>Restore<br>File</center></html>");
		button.setPreferredSize(new Dimension(buttonWidth, buttonHeight));
		panel.add(button);
		
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