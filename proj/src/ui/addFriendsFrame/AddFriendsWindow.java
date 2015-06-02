package ui.addFriendsFrame;

import java.awt.Dimension;
import java.util.ArrayList;

import javax.swing.JFrame;

import peer.PeerNew;
import user.User;

public class AddFriendsWindow extends JFrame{

	private static final long serialVersionUID = 1L;

	public AddFriendsWindow(PeerNew mainThread) {
		super();
		
		setTitle("Add Friends");
        this.setMinimumSize(new Dimension(300,500));
        //setSize(300, 200);
        setLocationRelativeTo(null);
        setResizable(false);
        
        ArrayList<User> list = mainThread.getAllUsersFromServer();
        if (list == null)
        	System.out.println("lista é nula");
        else{
        	 for(User u : list){
             	System.out.println(u.toString());
             }
        }
        
        this.add(new UsersPanel(list,mainThread));
        
        this.pack();
        this.setVisible(true);
	}
}
