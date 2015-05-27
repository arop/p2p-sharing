package user;

import java.awt.BorderLayout;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import chrriis.dj.nativeswing.swtimpl.NativeInterface;
import chrriis.dj.nativeswing.swtimpl.components.JWebBrowser;
import chrriis.dj.nativeswing.swtimpl.components.WebBrowserAdapter;
import chrriis.dj.nativeswing.swtimpl.components.WebBrowserNavigationEvent;

public class FacebookTest {

	private String user;
	
	public FacebookTest(){
		this.user = "not-finished";
	}
	
	public void communicateFacebook(){
		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {

				NativeInterface.open();
					
						final JFrame loginFrame = new JFrame();
						JPanel webBrowserPanel = new JPanel(new BorderLayout());
						// this is the JWebBrowser i mentioned earlier
						final JWebBrowser webBrowser = new JWebBrowser();
						
						
						// You can set this fields to false, or even let them
						// activated
						webBrowser.setMenuBarVisible(false);
						webBrowser.setButtonBarVisible(false);
						webBrowser.setLocationBarVisible(false);
						
						String appId = "901910593200831";
						String redirectUrl = "http://localhost/";
						String faceAppSecret = "e0b8e660ebde5e5d5f1a61e7086ffbe7";
						
						String fb_url = "https://www.facebook.com/dialog/oauth?client_id="+appId+"&redirect_uri="+redirectUrl;
						webBrowser.navigate(fb_url);

						// Here we add to our JWebBrowser an Adapter and
						// override the
						// locationChanging() method. Here we can check, if we
						// are
						// changing the location
						// in our case the String fb_url, then this JWebBrowser
						// can be
						// disposed.
						// The Timer is set for 2 seconds, so we can still see
						// if the
						// login was successfull or not.
						webBrowser.addWebBrowserListener(new WebBrowserAdapter() {
							@Override
							public void locationChanging(
									WebBrowserNavigationEvent e) {
								super.locationChanging(e);
								String newLocation = e
										.getNewResourceLocation();
	
								// System.out.println(newLocation);
	
								if (newLocation.indexOf("http://localhost/") == 0) {
									int index = newLocation.indexOf("code=");
									if (index < 0) {
										System.out.println("Failed...");
									} else {
										String accessCode = newLocation.substring(index + 5, newLocation.length()-4);
										
										//System.out.println(newLocation.substring(index+5));
										//System.out.println("Access code is: "+ accessCode);
																						
										String newUrl = "https://graph.facebook.com/oauth/access_token?client_id="+ appId
												+ "&redirect_uri="+ redirectUrl+ "&client_secret="+ faceAppSecret+ "&code=" + accessCode;
										
										try {
											String resp = readURL(new URL(newUrl));
											//System.out.println(resp);
											
											resp = readURL(new URL("https://graph.facebook.com/me?"+resp));
											//System.out.println(resp);
											if (resp != null)
												setUser(resp);
										} catch (MalformedURLException e1) {
											e1.printStackTrace();
										}
									}
									loginFrame.dispose();
								}
							}
						});
						webBrowserPanel.add(webBrowser, BorderLayout.CENTER);
						loginFrame.add(webBrowserPanel);
						loginFrame.setSize(400, 500);
						loginFrame.setVisible(true);
					}
				});

	}
	
	public synchronized void setUser(String resp){
		this.user = resp;
	}
	
	public synchronized String getUser(){
		return user;
	}
	
	public String getJsonUser(){
		this.communicateFacebook();
		while(this.getUser().equals("not-finished")){}
		return this.getUser();
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		FacebookTest fb = new FacebookTest();
		System.out.println(fb.getJsonUser());
	}

	/**
	 * FUNCTION CURRENTLY READING ONLY *ONE* RESPONSE *LINE*
	 * @param url
	 * @return
	 */
	private static String readURL(URL url) {
		
		String inputLine;
		try{
			URLConnection yc = url.openConnection();
	        BufferedReader in = new BufferedReader(
	                                new InputStreamReader(
	                                yc.getInputStream()));
	        

	        while ((inputLine = in.readLine()) != null) 
	            //System.out.println("#"+inputLine);
	        	return inputLine;
	        in.close();
		}
		catch(IOException e){
			e.printStackTrace();
			return null;
		}
        
        return inputLine;
	}

}
