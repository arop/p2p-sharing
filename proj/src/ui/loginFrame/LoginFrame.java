package ui.loginFrame;

import java.awt.BorderLayout;
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
import javax.swing.JSpinner;
import javax.swing.JTextArea;

import peer.PeerNew;
import ui.registerFrame.RegisterFrame;


public class LoginFrame extends JFrame {

	private static final long serialVersionUID = 1L;

	private String stateF = "running";
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

		JButton loginFacebookButton = new JButton("Login with Facebook");
		JSpinner portFacebook = new JSpinner();
		portFacebook.setPreferredSize(new Dimension(60,25));
		portFacebook.setValue(5599);

		loginButton.addActionListener(new ActionListener()
		{
			@Override	
			public void actionPerformed(ActionEvent e){
				if(username.getText()!= null && password.getPassword()!= null) {
					if(mainThread.login(username.getText(),new String(password.getPassword()))) {
						setState("success"); 
					}
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

		loginFacebookButton.addActionListener(new ActionListener()
		{
			@Override	
			public void actionPerformed(ActionEvent e){
				setState("facebook-"+portFacebook.getValue()); 
			}
		});

		JPanel loginForm = new JPanel();

		GridLayout grid = new GridLayout(7,1);

		loginForm.setLayout(grid);		

		loginForm.add(usernameLabel);
		loginForm.add(username);
		loginForm.add(passwordLabel);
		loginForm.add(password);
		loginForm.add(loginButton);
		loginForm.add(registerButton);

		JPanel panelFacebook = new JPanel();
		panelFacebook.add(loginFacebookButton, BorderLayout.WEST);
		panelFacebook.add(portFacebook, BorderLayout.EAST);

		loginForm.add(panelFacebook);

		this.add(loginForm);

		this.pack();
	}

	public synchronized void setState(String b){
		stateF = b;
	}

	public synchronized String getState1() {
		return this.stateF;
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
