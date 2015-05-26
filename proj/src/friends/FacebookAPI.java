package friends;

import com.restfb.DefaultFacebookClient;
import com.restfb.FacebookClient;
import com.restfb.Version;

public class FacebookAPI {

	private final String appSecret = "e0b8e660ebde5e5d5f1a61e7086ffbe7"; 
    private static final String appID = "901910593200831";  
    private static final String client_id = "MYCLIENTID";  

    // set this to your servlet URL for the authentication servlet/filter
    private static final String redirect_uri = "http://www.onmydoorstep.com.au/fbauth"; 
    /// set this to the list of extended permissions you want
    private static final String[] perms = new String[] {"publish_stream", "email"};
	
	public static void main(String[] args) {
		new FacebookAPI();
	}
	
/*	FacebookAPI() {
		//DefaultFacebookClient(String accessToken, String appSecret, Version apiVersion)
		FacebookClient.AccessToken fbat = new FacebookClient.AccessToken();
		fbat.fromQueryString("me?fields=id,name,about,address,age_range,bio,birthday,email,first_name,gender,hometown");
		
		FacebookClient facebookClient = new DefaultFacebookClient(fbat.getAccessToken(),appSecret,Version.VERSION_2_3);
		
	}
	*/
}
