package ui.mainFrame;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

public class FriendRightClickPopUp extends JPopupMenu{

	private static final long serialVersionUID = 1L;
	
	public FriendRightClickPopUp(int friendId){
		super();
		JMenuItem menuItem = new JMenuItem("Backup file");
	    this.add(menuItem);
	    menuItem = new JMenuItem("Remove from friends");
	    this.add(menuItem);
	}

}
