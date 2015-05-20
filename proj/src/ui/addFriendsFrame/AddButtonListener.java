package ui.addFriendsFrame;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import peer.PeerNew;

public class AddButtonListener implements ActionListener{

	private UsersPanel panel;
	private JTable jtable;
	private JScrollPane jscroller;
	private PeerNew mainThread;
	private JButton jButton;
	
	public AddButtonListener(UsersPanel panel, JTable jtable, JScrollPane jscroller, JButton button, PeerNew mainThread){
		this.panel = panel;
		this.jscroller = jscroller;
		this.jtable = jtable;
		this.mainThread = mainThread;
		this.jButton = button;
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		//new AddFriendsWindow(mainThread);
		int[] selection = jtable.getSelectedRows();
		int[] user_ids = new int[selection.length];
		for (int i = 0; i < selection.length; i++){
			user_ids[i] = (int) jtable.getModel().getValueAt(selection[i], 2);
		}
		String message ="";
		if (mainThread.addFriends(user_ids))
			message = "Success! Please close this window and refresh your friends list.";
		else message = "An error occured... Please try again later.";		
		
		
		panel.remove(jscroller);
		panel.repaint();
		
		JLabel label = new JLabel(message);
		panel.add(label, BorderLayout.NORTH);
		panel.validate();
		
		this.jButton.setEnabled(false);
		
		
	}

}
