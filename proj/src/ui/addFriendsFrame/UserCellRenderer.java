package ui.addFriendsFrame;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JSeparator;
import javax.swing.ListCellRenderer;
import javax.swing.border.EmptyBorder;

import user.User;

class UserCellRenderer extends JLabel implements ListCellRenderer<Object> {
	

	private static final long serialVersionUID = 1L;
	JSeparator separator;
	private static final Color HIGHLIGHT_COLOR = new Color(0, 0, 128);

    final String SEPARATOR = "SEPARATOR";
	
	public UserCellRenderer() {    
		setOpaque(true);
		//setIconTextGap(12);
		setBorder(new EmptyBorder(1, 1, 1, 1));
	    separator = new JSeparator(JSeparator.HORIZONTAL);
	}
	
	public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
		String str = (value == null) ? "" : value.toString();
	    if (SEPARATOR.equals(str)) {
	    	return separator;
	    }
		
	    User entry = (User) value;
		setText(entry.getUsername());
		//setIcon(entry.getImage());
		if (isSelected) {
			setBackground(HIGHLIGHT_COLOR);
			setForeground(Color.white);
		} 
		else {
			setBackground(Color.white);
			setForeground(Color.black);
		}
		return this;
	}
}
