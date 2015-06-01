package ui.mainFrame;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import peer.PeerNew;

public class FriendRightClickPopUp extends JPopupMenu{

	private static final long serialVersionUID = 1L;
	
	public FriendRightClickPopUp(int friendId, PeerNew mainThread){
		super();
		JMenuItem menuItem = new JMenuItem("Backup file");
		menuItem.addMouseListener(new MouseAdapter() {
		    @Override
		    public void mouseReleased(MouseEvent e) {
		        mainThread.shareFileWithFriend(friendId);
		    }
		});
	    this.add(menuItem);
	    menuItem = new JMenuItem("Remove from friends");
	    this.add(menuItem);
	}

}
