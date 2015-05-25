package ui.loginFrame;

import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextArea;

import peer.PeerNew;
import ui.registerFrame.RegisterFrame;


public class LoginFrame extends JFrame {

	private static final long serialVersionUID = 1L;

	private boolean success = false;
	private PeerNew mainThread;
	private RegisterFrame registerForm;
	

	public LoginFrame(PeerNew peer) {
		mainThread = peer;
		initUI();
		setVisible(true);
	}



	private void initUI() {

		setTitle("Login");
		this.setMinimumSize(new Dimension(200,200));
		//setSize(300, 200);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setResizable(false);

		
		JTextArea username = new JTextArea();
		JPasswordField password = new JPasswordField();
		
		JLabel usernameLabel = new JLabel("username");
		JLabel passwordLabel = new JLabel("password");

		JButton loginButton = new JButton("Login");
		
		JButton registerButton = new JButton("No account? Join us now!");

		
		loginButton.addActionListener(new ActionListener()
		{
			@Override	
			public void actionPerformed(ActionEvent e){
				if(mainThread.login(username.getText(),new String(password.getPassword()))) {
					setSuccess(true); 
				}
			}
		});
		
		registerButton.addActionListener(new ActionListener()
		{
			@Override	
			public void actionPerformed(ActionEvent e){
				registerForm = new RegisterFrame(mainThread);
				registerForm.setVisible(true);
			}
		});
	
		
	
		JPanel loginForm = new JPanel();
		
		GridLayout grid = new GridLayout(6,1);
		
		loginForm.setLayout(grid);		
		
		loginForm.add(usernameLabel);
		loginForm.add(username);
		loginForm.add(passwordLabel);
		loginForm.add(password);
		loginForm.add(loginButton);
		loginForm.add(registerButton);

			
		this.add(loginForm);
		
		this.pack();
	}


	public synchronized void setSuccess(boolean b){
		success = b;
	}

	public synchronized boolean isSuccess() {
		return success;
	}




	public static void main(String[] args) {

		EventQueue.invokeLater(new Runnable() {

			@Override
			public void run() {
				LoginFrame ex = new LoginFrame(null);
				ex.setVisible(true);
			}
		});
	}

}



