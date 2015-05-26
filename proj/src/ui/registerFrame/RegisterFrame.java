package ui.registerFrame;

import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JSpinner;
import javax.swing.JTextArea;

import peer.PeerNew;


public class RegisterFrame extends JFrame {

	private static final long serialVersionUID = 1L;

	private PeerNew mainThread;

	public RegisterFrame(PeerNew peer) {
		mainThread = peer;
		initUI();
		setVisible(true);
	}

	private void initUI() {
		setTitle("Register");
		this.setMinimumSize(new Dimension(300,300));
		//setSize(300, 200);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setResizable(false);
		
		JTextArea username = new JTextArea();
		JTextArea email = new JTextArea();
		JPasswordField password = new JPasswordField();
		JPasswordField rePassword = new JPasswordField();
		JSpinner port = new JSpinner();
		
		
		JLabel usernameLabel = new JLabel("Desired username");
		JLabel emailLabel = new JLabel("Email");
		JLabel passwordLabel = new JLabel("Desired password");
		JLabel rePasswordLabel = new JLabel("Reenter your password");
		JLabel portLabel = new JLabel("Which port do you want to use");

		
		JButton registerButton = new JButton("Submit");

		registerButton.addActionListener(new ActionListener()
		{
			@Override	
			public void actionPerformed(ActionEvent e){
				if(mainThread.register(username.getText(),email.getText(),new String(password.getPassword()),new String(rePassword.getPassword()), (Integer) port.getValue() )) {
					dispose();
				}
			}
		});
		
	
	
		JPanel registerForm = new JPanel();
		
		GridLayout grid = new GridLayout(11,1);
		
		registerForm.setLayout(grid);		
		
		registerForm.add(usernameLabel);
		registerForm.add(username);
		registerForm.add(emailLabel);
		registerForm.add(email);
		registerForm.add(passwordLabel);
		registerForm.add(password);
		registerForm.add(rePasswordLabel);
		registerForm.add(rePassword);
		registerForm.add(portLabel);
		registerForm.add(port);
		registerForm.add(registerButton);
			
		this.add(registerForm);
		
		
		this.pack();
	}

	public static void main(String[] args) {

		EventQueue.invokeLater(new Runnable() {

			@Override
			public void run() {
				RegisterFrame ex = new RegisterFrame(null);
				ex.setVisible(true);
			}
		});
	}

}



