package ui.mainFrame;

import java.awt.Dimension;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class GeneralInfo extends JPanel {
	
	private static final long serialVersionUID = 1L;

	public GeneralInfo(int width, int height){
		super();
		this.setPreferredSize(new Dimension(width, height));
		//this.setLayout(new GridLayout(0,1));
		this.add(new JLabel("number of files backed up: 3"));
		this.add(new JLabel("total size backed up: 1203MB"));
		this.add(new JLabel("number of friends: 15"));
		this.add(new JLabel("number of friends online: 3"));
		this.add(new JLabel("server online: YES"));
		
	}

}
