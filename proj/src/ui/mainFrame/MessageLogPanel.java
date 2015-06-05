package ui.mainFrame;

import java.awt.Dimension;

import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;

import user.User;

public class MessageLogPanel extends JPanel {

	private static final long serialVersionUID = 1L;

	private JList<User> list;

	public MessageLogPanel(){

		super();
		this.setMinimumSize(new Dimension(150,10));

		list = new JList<User>(); //data has type Object[]
		list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		list.setLayoutOrientation(JList.VERTICAL);
		list.setVisibleRowCount(-1);

		JScrollPane listScroller = new JScrollPane(list);

		Dimension d = new Dimension(200,500);
		listScroller.setPreferredSize(d);

		this.add(listScroller);
	}
}