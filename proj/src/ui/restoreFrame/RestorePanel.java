//package ui.restoreFrame;
//
//import java.awt.*;
//import java.awt.event.*;
//import java.util.ArrayList;
//
//import javax.swing.*;
//
///*
// * Code based on:
// * https://docs.oracle.com/javase/tutorial/displayCode.html?code=https://docs.oracle.com/javase/tutorial/uiswing/examples/components/RadioButtonDemoProject/src/components/RadioButtonDemo.java
// */
//public class RestorePanel extends JPanel
//implements ActionListener {
//
//	/**
//	 * 
//	 */
//	private static final long serialVersionUID = 1L;
//	static String fileName = null;
//	static int numberOfFiles = 0;
//	static ButtonGroup group;
//	static JRadioButton tempButton;
//	static JPanel radioPanel;
//	
//	ArrayList<String> filenames;
//
//	public RestorePanel() {
//		super(new BorderLayout());
//		
//		Dimension d = new Dimension(300,300);
//		this.setPreferredSize(d);
//		
//		filenames = new ArrayList<String>();
//
//		//Create the radio buttons.
//		int i = 0;
//
//		if(numberOfFiles == 0) return;
//
//		radioPanel = new JPanel(new GridLayout(numberOfFiles, 0));
//
//		for(; i < numberOfFiles; i++) {
//
//			tempButton = new JRadioButton(filenames.get(i));
//			tempButton.setMnemonic(KeyEvent.VK_B);
//			tempButton.setActionCommand(filenames.get(i));
//			//tempButton.setSelected(true);
//			
//			//Group the radio buttons.
//			group = new ButtonGroup();
//			group.add(tempButton);
//			
//			//Register a listener for the radio buttons.
//			tempButton.addActionListener(this);
//			
//			//Put the radio buttons in a column in a panel.
//			radioPanel.add(tempButton);
//
//			add(radioPanel, BorderLayout.LINE_START);
//			setBorder(BorderFactory.createEmptyBorder(20,20,20,20));
//
//		}
//
//	}
//
//	public static int getNumberOfFiles() {
//		return numberOfFiles;
//	}
//
//	public static void setNumberOfFiles(int numberOfFiles) {
//		RestorePanel.numberOfFiles = numberOfFiles;
//	}
//
//	public ArrayList<String> getFilenames() {
//		return filenames;
//	}
//
//	public void setFilenames(ArrayList<String> filenames) {
//		this.filenames = filenames;
//	}
//
//	private static void createAndShowGUI() {
//		//Create and set up the window.
//		JFrame frame = new JFrame("RadioButtonDemo");
//		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//
//		//Create and set up the content pane.
//		JComponent newContentPane = new RestorePanel();
//		newContentPane.setOpaque(true); //content panes must be opaque
//		frame.setContentPane(newContentPane);
//
//		//Display the window.
//		frame.pack();
//		frame.setVisible(true);
//	}
//
//	public static void main(String[] args) {
//		//Schedule a job for the event-dispatching thread:
//		//creating and showing this application's GUI.
//		javax.swing.SwingUtilities.invokeLater(new Runnable() {
//			public void run() {
//				createAndShowGUI();
//			}
//		});
//	}
//
//
//	@Override
//	public void actionPerformed(ActionEvent arg0) {
//		// TODO Auto-generated method stub
//
//	}
//}