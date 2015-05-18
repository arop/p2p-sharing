package ui.mainFrame;

import java.awt.Dimension;

import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;

import friends.Friend;

public class MessageLogPanel extends JPanel {

	private static final long serialVersionUID = 1L;
	
	private JList<Friend> list;
	
	public MessageLogPanel(){
		
		super();
		this.setMinimumSize(new Dimension(150,10));
		
		list = new JList<Friend>(); //data has type Object[]
		list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		list.setLayoutOrientation(JList.VERTICAL);
		list.setVisibleRowCount(-1);

		//list.setCellRenderer(new FriendCellRenderer());
		
		JScrollPane listScroller = new JScrollPane(list);
		//listScroller.setPreferredSize(new Dimension(250, 80));
		
		//Dimension d = list.getPreferredSize();
		Dimension d = new Dimension(200,500);
		//d.width = 200;
		listScroller.setPreferredSize(d);
		
		this.add(listScroller);
	}
}