package ui.mainFrame;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

import peer.PeerNew;
import user.User;
import friends.FriendCircle;

public class GUI extends JFrame {


	private static final long serialVersionUID = 1L;

	private PeerNew mainThread;
	
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
       // GridLayout grid = new GridLayout(1,3,10,10);
       // this.setLayout(grid);
        
        //retrieve friends list from file? server?
        User[] friends = {new User(1, "norim"), new User(2,"joao"), new User(3,"filipe"), new User(4,"mira"), new User(5,"pires"),
        		new User(1, "norim"), new User(2,"joao"), new User(3,"filipe"), new User(4,"mira"), new User(5,"pires"),
        		new User(1, "norim"), new User(2,"joao"), new User(3,"filipe"), new User(4,"mira"), new User(5,"pires"),
        		new User(1, "norim"), new User(2,"joao"), new User(3,"filipe"), new User(4,"mira"), new User(5,"pires"),
        		new User(1, "norim"), new User(2,"joao"), new User(3,"filipe"), new User(4,"mira"), new User(5,"pires"),
        		new User(1, "norim"), new User(2,"joao"), new User(3,"filipe"), new User(4,"mira"), new User(5,"pires"),
        		new User(1, "norim"), new User(2,"joao"), new User(3,"filipe"), new User(4,"mira"), new User(5,"pires"),
        		new User(1, "norim"), new User(2,"joao"), new User(3,"filipe"), new User(4,"mira"), new User(5,"pires")};
        friendsList = new ArrayList<User>(Arrays.asList(friends));
        
        List<FriendCircle> circles = new ArrayList<FriendCircle>();
        circles.add(new FriendCircle("Circle 1", friendsList.subList(2, 6)));
        circles.add(new FriendCircle("Circle 2", friendsList.subList(13, 17)));
        circles.add(new FriendCircle("Circle 3", friendsList.subList(4, 7)));
                
        JPanel friendshipsPanel = new JPanel();
        friendshipsPanel.setLayout(new BorderLayout());
        
        FriendsPanel friendsPanel = new FriendsPanel(friendsList, mainThread);
        friendshipsPanel.add(friendsPanel, BorderLayout.WEST);
        
        CirclesPanel circlesPanel = new CirclesPanel(circles);
        friendshipsPanel.add(circlesPanel, BorderLayout.EAST);
        
       
        this.add(generateButtonsPanel(100, 500), BorderLayout.WEST);
        this.add(friendshipsPanel, BorderLayout.EAST);
        this.add(new GeneralInfo(100,500), BorderLayout.CENTER);
        
        this.pack();
    }

    private JPanel generateButtonsPanel(int panelWidth, int panelHeight){
    	JPanel panel = new JPanel();
    	panel.setPreferredSize(new Dimension(panelWidth, panelHeight));
    	//GridLayout grid = new GridLayout(0,1);
    	
    	//panel.setLayout(grid);
    	
    	int buttonWidth = panelWidth;
    	
    	int buttonHeight = 90;
    	
    	
    	JButton button = new JButton("Backup File");
    	button.setPreferredSize(new Dimension(buttonWidth, buttonHeight));
    	panel.add(button);
    	
    	button = new JButton("<html><center>Restore<br>File</center></html>");
    	button.setPreferredSize(new Dimension(buttonWidth, buttonHeight));
    	panel.add(button);
    	
    	button = new JButton("Delete File");
    	button.setPreferredSize(new Dimension(buttonWidth, buttonHeight));
    	panel.add(button);
    	
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
}