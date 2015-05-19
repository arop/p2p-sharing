package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

import user.BCrypt;
import user.User;

public class UserDatabase {
	
	private Connection con;
	
	public UserDatabase(){
		con = null;
	    try {
	      Class.forName("org.sqlite.JDBC");
	      con = DriverManager.getConnection("jdbc:sqlite:database\\user_db.db");
	    } catch ( Exception e ) {
	      System.err.println( e.getClass().getName() + ": " + e.getMessage() );
	      System.exit(0);
	    }
	}
	
	public User getUserById(int id){

	    Statement stmt = null;
	    User user = null;
	    
	    try {
		  Class.forName("org.sqlite.JDBC");
		  con.setAutoCommit(false);
		
		  stmt = con.createStatement();
		  ResultSet rs = stmt.executeQuery( "SELECT * FROM User WHERE id = "+id+";" );
		  String  username = rs.getString("username");
		  String  email = rs.getString("email");
		  String  ip = rs.getString("last_ip");
		  String  password_hash = rs.getString("password_hash");
		  user = new User(id, username, email, password_hash, ip);
	      rs.close();
	      stmt.close();
	      //con.close();
	    } catch ( Exception e ) {
	      System.err.println( e.getClass().getName() + ": " + e.getMessage() );
	      System.exit(0);
	    }
		return user;
	}
	
	public User getUserByEmail(String email){

	    Statement stmt = null;
	    User user = null;
	    
	    try {
		  Class.forName("org.sqlite.JDBC");
		  con.setAutoCommit(false);
		
		  stmt = con.createStatement();
		  ResultSet rs = stmt.executeQuery( "SELECT * FROM User WHERE email = '"+email+"';" );
		  
		  if (rs.isClosed()) //no users with this email
			  return null;
		  
		  String  username = rs.getString("username");
		  int id = rs.getInt("id");
		  String  ip = rs.getString("last_ip");
		  String  password_hash = rs.getString("password_hash");
		  user = new User(id, username, email, password_hash, ip);
	      rs.close();
	      stmt.close();
	      //con.close();
	    } catch ( Exception e ) {
	      System.err.println( e.getClass().getName() + ": " + e.getMessage() );
	      System.exit(0);
	    }
		return user;
	}
	
	public String registerUser(String username, String email, String password, String ip){
		if (getUserByEmail(email) != null)
			return "Email already registered...";
		
		String hashed = BCrypt.hashpw(password, BCrypt.gensalt(12));
		
		Statement stmt = null;
	    try {
	      con.setAutoCommit(false);

	      stmt = con.createStatement();
	      String sql = "INSERT INTO User (username,email,password_hash,last_ip) " +
	                   "VALUES ('"+username+"', '"+email+"', '"+hashed+"', '"+ip+"');"; 
	      stmt.executeUpdate(sql);

	      stmt.close();
	      con.commit();
	      //c.close();
	    } catch ( Exception e ) {
	      System.err.println( e.getClass().getName() + ": " + e.getMessage() );
	      System.exit(0);
	    }
	    
	    return "success";
	}
	
	
	public void updateLastIp(int id, String ip){
	    Statement stmt = null;
	    try {
	      con.setAutoCommit(false);
	      stmt = con.createStatement();
	      String sql = "UPDATE User set last_ip = '"+ip+"' where id="+id+";";
	      stmt.executeUpdate(sql);
	      con.commit();
	      stmt.close();
	    } catch ( Exception e ) {
	      System.err.println( e.getClass().getName() + ": " + e.getMessage() );
	      System.exit(0);
	    }
	}
	
	
	public String login(String email, String password){
		
		User user = getUserByEmail(email);
		if (user == null)
			return "Email doesn't exist!";
		if (BCrypt.checkpw(password, user.getPasswordHash()))
			return "success";
		return "Wrong password...";
	}
	
}

