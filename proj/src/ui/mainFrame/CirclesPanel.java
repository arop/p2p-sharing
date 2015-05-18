package ui.mainFrame;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import friends.FriendCircle;

public class CirclesPanel extends JPanel {

	private static final long serialVersionUID = 1L;
	
	public CirclesPanel(List<FriendCircle> friendsList){
		
		super();
		this.setLayout(new BorderLayout());
		
		//TABLE OF FRIENDS
		JTable jTable1 = new javax.swing.JTable();
		jTable1.setModel(new FinalTableModelCirclesList(friendsList));
		jTable1.getColumnModel().getColumn(0).setMaxWidth(200);
		
		JScrollPane listScroller = new JScrollPane(jTable1);
		Dimension d = new Dimension(200,500);
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
		            JPopupMenu popup = new CircleRightClickPopUp((String) jTable1.getModel().getValueAt(r, 0));
		            popup.show(e.getComponent(), e.getX(), e.getY());
		        }
		    }
		});
		
		
		//REFRESH FRIENDS' LIST
		/*JButton refreshList = new JButton("Refresh Friends List");
		refreshList.setPreferredSize(new Dimension(150, 50));
		this.add(refreshList, BorderLayout.SOUTH);*/

	}
}
