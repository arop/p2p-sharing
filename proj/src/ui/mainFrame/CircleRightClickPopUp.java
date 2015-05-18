package ui.mainFrame;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

public class CircleRightClickPopUp extends JPopupMenu{

	private static final long serialVersionUID = 1L;
	
	public CircleRightClickPopUp(String friendId){
		super();
		JMenuItem menuItem = new JMenuItem("Backup file");
	    this.add(menuItem);
	    menuItem = new JMenuItem("Edit");
	    this.add(menuItem);
	    menuItem = new JMenuItem("Delete");
	    this.add(menuItem);
	}

}
