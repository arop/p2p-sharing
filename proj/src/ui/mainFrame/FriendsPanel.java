package ui.mainFrame;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;

import friends.FriendCircle;
import peer.PeerNew;
import ui.addFriendsFrame.AddFriendsWindow;
import user.User;

public class FriendsPanel extends JPanel {

	private static final long serialVersionUID = 1L;

	private PeerNew mainThread;
	
	public void  updateTable(JTable jTable1){
		jTable1.setModel(new FinalTableModelFriendsList(mainThread.getFriends()));
		jTable1.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		jTable1.getColumnModel().getColumn(0).setMaxWidth(20);
		jTable1.getColumnModel().getColumn(1).setPreferredWidth(100);
		
		//hide id column (although it's removed, the data remains there)
		jTable1.removeColumn(jTable1.getColumnModel().getColumn(2));
	}
	
	public FriendsPanel(PeerNew mainThread){
		super();
		this.mainThread = mainThread;
		
		this.setLayout(new BorderLayout());
		
		//TABLE OF FRIENDS
		JTable jTable1 = new javax.swing.JTable();
		updateTable(jTable1);
		
		//add scroll
		JScrollPane listScroller = new JScrollPane(jTable1);
		Dimension d = new Dimension(200,450);
		listScroller.setPreferredSize(d);
		this.add(listScroller, BorderLayout.NORTH);
		
		
		
		//right click event listener
		jTable1.addMouseListener(new MouseAdapter() {
		    @Override
		    public void mouseReleased(MouseEvent e) {
		        int r = jTable1.rowAtPoint(e.getPoint());
		        if (r >= 0 && r < jTable1.getRowCount()) {
		            jTable1.setRowSelectionInterval(r, r);
		        } else {
		            jTable1.clearSelection();
		        }

		        int rowindex = jTable1.getSelectedRow();
		        if (rowindex < 0)
		            return;
		        if (e.isPopupTrigger() && e.getComponent() instanceof JTable ) {
		            JPopupMenu popup = new FriendRightClickPopUp((int) jTable1.getModel().getValueAt(r, 2));
		            popup.show(e.getComponent(), e.getX(), e.getY());
		        }
		    }
		});
			
		
		
		
		//BOTTOM BUTTONS
		JPanel bottomButtons = new JPanel();
		bottomButtons.setLayout(new BorderLayout());
				
			//ADD FRIENDS
			JButton addFriendsButton = new JButton("<html><center>Add<br>Friends</center></html>");
			addFriendsButton.setPreferredSize(new Dimension(100, 50));		
			bottomButtons.add(addFriendsButton, BorderLayout.WEST);
			addFriendsButton.addActionListener(new ActionListener()
				{
					@Override	
					public void actionPerformed(ActionEvent e){
						new AddFriendsWindow(mainThread);
					}
				});
		
			//REFRESH FRIENDS' LIST
			JButton refreshListButton = new JButton("<html><center>Refresh<br>Friends List</center></html>");
			refreshListButton.setPreferredSize(new Dimension(100, 50));		
			bottomButtons.add(refreshListButton, BorderLayout.EAST);
			refreshListButton.addActionListener(new ActionListener()
			{
				@Override	
				public void actionPerformed(ActionEvent e){
					mainThread.getFriendsFromServer();
					updateTable(jTable1);
				}
			});
			
			
			
		this.add(bottomButtons, BorderLayout.SOUTH);
			

	}
}
