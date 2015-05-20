package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import user.BCrypt;
import user.User;

public class UserDatabase {
	
	private Connection con;
	
	public UserDatabase(){
		con = null;
	    try {
	      Class.forName("org.sqlite.JDBC");
	      con = DriverManager.getConnection("jdbc:sqlite:..\\database\\user_db.db");
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
		  
		  if (rs.isClosed()) //no users with this id
			  return null;
		  
		  user = this.getUserFromResultSet(rs, true);
	      rs.close();
	      stmt.close();
	      //con.close();
	    } catch ( Exception e ) {
	      System.err.println( e.getClass().getName() + ": " + e.getMessage() );
	      System.exit(0);
	    }
		return user;
	}
	
	private User getUserFromResultSet(ResultSet rs, boolean complete){
		try {
			int id = rs.getInt("id");
			String  username = rs.getString("username");
			String  email = rs.getString("email");
			
			if (!complete)
				return new User(id, username, email, null, null, -1);
			
			String  ip = rs.getString("last_ip");
			String  password_hash = rs.getString("password_hash");
			int port = rs.getInt("port");	
			return new User(id, username, email, password_hash, ip, port);
		} catch (SQLException e) {
			System.err.println( e.getClass().getName() + ": " + e.getMessage() );
		    System.exit(0);
		}
		return null;
		
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
		  user = getUserFromResultSet(rs, true);
	      rs.close();
	      stmt.close();
	      //con.close();
	    } catch ( Exception e ) {
	      System.err.println( e.getClass().getName() + ": " + e.getMessage() );
	      System.exit(0);
	    }
		return user;
	}
	
	public String registerUser(String username, String email, String password, String ip, int port){
		if (getUserByEmail(email) != null)
			return "Email already registered...";
		
		String hashed = BCrypt.hashpw(password, BCrypt.gensalt(12));
		
		Statement stmt = null;
	    try {
	    	Class.forName("org.sqlite.JDBC");	
	      con.setAutoCommit(false);

	      stmt = con.createStatement();
	      String sql = "INSERT INTO User (username,email,password_hash,last_ip, port) " +
	                   "VALUES ('"+username+"', '"+email+"', '"+hashed+"', '"+ip+"', "+port+");"; 
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
	    	Class.forName("org.sqlite.JDBC");	
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
	
	public boolean checkIfFriend(int id1, int id2){
		Statement stmt = null;
	    boolean result = false;
	    try {
		  Class.forName("org.sqlite.JDBC");
		  con.setAutoCommit(false);
		
		  stmt = con.createStatement();
		  ResultSet rs = stmt.executeQuery( "SELECT * FROM Friend "
		  		+ "WHERE id1="+id1+" AND id2="+id2+";");
		  if (!rs.isClosed()) //FRIENDSHIP exists
			  result = true;

	      rs.close();
	      stmt.close();
	    } catch ( Exception e ) {
	      System.err.println( e.getClass().getName() + ": " + e.getMessage() );
	      return false;
	    }
		
		return result;
	}
	
	public String login(String email, String password){
		
		User user = getUserByEmail(email);
		if (user == null)
			return "Email doesn't exist!";
		if (BCrypt.checkpw(password, user.getPasswordHash()))
			return "success";
		return "Wrong password...";
	}

	/**
	 * 
	 * @param complete if true, all info from Users will be retrieved. If false, only id, username and email will be retrieved.
	 * @return
	 */
	public ArrayList<User> getAllUsers(boolean complete) {

	    Statement stmt = null;
	    ArrayList<User> users = new ArrayList<User>();
	    
	    try {
		  Class.forName("org.sqlite.JDBC");		
		  stmt = con.createStatement();
		  ResultSet rs = stmt.executeQuery( "SELECT * FROM User;" );
		  
		  if (rs.isClosed()) //no users with this email
			  return users;
		  
		  while(rs.next()){
			  User user;
			  if ((user = getUserFromResultSet(rs, complete)) != null)
				  users.add(user);
		  }
		  
	      rs.close();
	      stmt.close();
	      //con.close();
	    } catch ( Exception e ) {
	      System.err.println( e.getClass().getName() + ": " + e.getMessage() );
	      System.exit(0);
	    }
		return users;
	}

	/**
	 * 
	 * @param user_id
	 * @param i
	 */
	public String addFriend(int user_id, int other_user) {
		
		if (checkIfFriend(user_id, other_user))
			return "success";
		
		Statement stmt = null;
	    try {
	    	Class.forName("org.sqlite.JDBC");	
		    con.setAutoCommit(false);
	
		    stmt = con.createStatement();
		    String sql = "INSERT INTO Friend (id1,id2) " +
		                 "VALUES ("+user_id+", "+other_user+");"; 
		    stmt.executeUpdate(sql);
		    stmt.close();
		    con.commit();
	    } catch ( Exception e ) {
	    	System.err.println( e.getClass().getName() + ": " + e.getMessage() );
	    	return "error";
	    }
	    
	    return "success";
		
	}
	
	public ArrayList<User> getFriends(int user_id) {

	    Statement stmt = null;
	    ArrayList<User> users = new ArrayList<User>();
	    
	    try {
		  Class.forName("org.sqlite.JDBC");		
		  stmt = con.createStatement();
		  ResultSet rs = stmt.executeQuery( "SELECT * FROM Friend WHERE id1 = "+user_id+";" );
		  
		  if (rs.isClosed()) //no users with this email
			  return users;
		  
		  //iterate friends ids
		  while(rs.next()){
			  int id =  rs.getInt("id2");
			  
			  //get user by id
			  Statement stmt2 = con.createStatement();
			  ResultSet rs2 = stmt2.executeQuery( "SELECT * FROM User WHERE id = "+id+";" );
			  User user;
			  if ((user = getUserFromResultSet(rs2, false)) != null)
				  users.add(user);
			  //close connections
			  rs2.close();
			  stmt2.close();
		  }
		  
		  
	      rs.close();
	      stmt.close();
	      //con.close();
	    } catch ( Exception e ) {
	      System.err.println( e.getClass().getName() + ": " + e.getMessage() );
	      System.exit(0);
	    }
		return users;
	}
	
}

