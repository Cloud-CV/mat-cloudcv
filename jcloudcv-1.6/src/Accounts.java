import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

public class Accounts {
	public static String CONFIG_PATH = System.getProperty("user.dir")
			+ "//user.cnf";

	String random_key = UUID.randomUUID().toString();
	static boolean googleAuthentication = false;
	static boolean dropboxAuthentication = false;

	/*
	 *  flag: Type- Boolean,
	 *  This flag switches between writing google account details or dropbox account details into config file.
	 */
	public void writeFile(JSONObject o, boolean flag)
			throws FileNotFoundException, IOException, JSONException,
			ClassNotFoundException {

		JSONObject ob = null;
		
		// System.out.println(o);
		
		if (flag) {
			ob = readFile();
			ObjectOutputStream os = new ObjectOutputStream(
					new FileOutputStream(CONFIG_PATH));
			ob.put("uid", o.getString("uid"));
			ob.put("token", o.getString("token"));
			os.writeObject(ob.toString());
			os.flush();
			os.close();
		} else {
			ObjectOutputStream os = new ObjectOutputStream(
					new FileOutputStream(CONFIG_PATH));
			os.writeObject(o.toString());
			os.flush();
			os.close();
		}

	}

	public JSONObject readFile() throws FileNotFoundException,
			IOException, JSONException, ClassNotFoundException {
		ObjectInputStream is = new ObjectInputStream(new FileInputStream(
				CONFIG_PATH));

		String str = (String) is.readObject();
		// System.out.println("out put os the file is : " + str);
		
		JSONObject o = new JSONObject(str);
		if (o.has("token"))
			this.dbaccount.access_token = o.getString("token");
		if (o.has("uid"))
			this.dbaccount.userid = o.getString("uid");
		if (o.has("id"))
			this.gaccount.userid = o.getString("id");
		if (o.has("email"))
			this.gaccount.emailid = o.getString("email");
		
		is.close();
		return o;
	}

	public void dropboxAuthenticate() throws JSONException, IOException,
			ClassNotFoundException {
		HttpResponse response = null;

		System.out.println("DB : google authentication is : "
				+ googleAuthentication);

		if (googleAuthentication) {
			File f = new File(CONFIG_PATH);
			String result = null;
			HttpClient client = new DefaultHttpClient();

			if (f.exists() && readFile().has("uid")) {
				try {

					System.out.println("DB : authenticated by google and db ");
					HttpGet post = new HttpGet(
							"http://cloudcv.org/cloudcv/auth/dropbox/?type=api&state="
									+ random_key + "&userid="
									+ readFile().get("id") + "&dbuser="
									+ readFile().getString("uid"));

					response = client.execute(post);
					result = convertStreamToString(response.getEntity()
							.getContent());
					
					//System.out
					//		.println("DB : When already authenticated respose is : "
					//				+ result);

				} catch (IOException e) {
					e.printStackTrace();
				}

			} else {
				try {
					HttpGet get = new HttpGet(
							"http://cloudcv.org/cloudcv/auth/dropbox/?userid="
									+ readFile().get("id") + "&state="
									+ random_key + "&type=api");
					//System.out
					//		.println("DB : authenticated by google but not dropbox ");

					response = client.execute(get);
					result = convertStreamToString(response.getEntity()
							.getContent());
					//System.out.println("DB : result when file not existed :"
					//		+ result);

				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			// read the response
			JSONObject mobj = new JSONObject(result);
			Iterator<String> itr = mobj.keys(); // check iterator type

			while (itr.hasNext()) {
				String key = itr.next();

				//System.out.println("DB : key in the response is :" + key);

				if (key.equals("redirect")
						&& mobj.getString("redirect").equals("True")) {
					
					System.out.println("DB : opening browser.. !");

					String url = mobj.getString("url");

					if (Desktop.isDesktopSupported()) {
						Desktop desktop = Desktop.getDesktop();
						try {
							desktop.browse(new URI(url));
						} catch (IOException e) {

							e.printStackTrace();
						} catch (URISyntaxException e) {

							e.printStackTrace();
						}
					} else {
						Runtime runtime = Runtime.getRuntime();
						try {
							runtime.exec("xdg-open " + url);
						} catch (IOException e) {

							e.printStackTrace();
						}
					}
				}
				if (key.equals("isValid")
						&& mobj.getString("isValid").equals("True")) {
					
					System.out.println("Dropbox Authentication Successfull");
					
					this.dbaccount.access_token = mobj.getString("token");
					
					dropboxAuthentication = true;
				}

			}
			//System.out.println("DB : exiting dropbox authentication. ");

		} else {
			authenticate();
			while ( Accounts.getObject().googleAuthentication == false)
			{
				try {
					Thread.sleep(5000);
					// System.out.println("Sleeping while waiting for google Authentication");
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			dropboxAuthenticate();
		}
	}

	public void authenticate() throws JSONException, IOException,
			ClassNotFoundException {

		String result = null;

		
		File f = new File(CONFIG_PATH);
		HttpClient client = new DefaultHttpClient();

		HttpResponse response = null;
		
		System.out.println("auth : Authentication begins !");
		
		if (f.exists() && !f.isDirectory()) {
			// System.out.println("auth : config file already exists ");
			
			try {
				
				//System.out
				//		.println("auth : reading and sending google id from the file.");
				HttpGet post = new HttpGet(
								"http://cloudcv.org/cloudcv/auth/google/?type=api&state="
								+ random_key + "&userid="
								+ readFile().getString("id"));
				
				response = client.execute(post);
				
				result = convertStreamToString(response.getEntity()
								.getContent());
				
				// System.out.println("auth : reasponse after registration : " + result);

			} catch (IOException e) {
				e.printStackTrace();
			}

		} else {
			
			//System.out.println("auth : file does not exits."
			//		+ "Unregisterd or deleted config file");
			
			// System.out.println("auth : Creating New Account !");

			try {
				HttpGet post = new HttpGet(
						"http://cloudcv.org/cloudcv/auth/google/?type=api&state="
								+ random_key);
				List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(
						1);

				nameValuePairs.add(new BasicNameValuePair("type", "api"));
				nameValuePairs.add(new BasicNameValuePair("state", random_key));

				response = client.execute(post);
				result = convertStreamToString(response.getEntity()
						.getContent());
				// System.out.println(result);

			} catch (IOException e) {
				e.printStackTrace();
			}

		}
		
		JSONObject mobj = new JSONObject(result);
		Iterator<String> itr = mobj.keys(); // check iterator type
		//System.out.println("auth : iterator has next :" + itr.hasNext());
		int i = 0;
		
		
		while (itr.hasNext()) 
		{
			//System.out.println("auth : iterating....." + i);
			i++;
			String key = itr.next();
			if (key.equals("redirect")
					&& mobj.getString("redirect").equals("True")) {
				System.out.println("auth : opening browser !");
				
				// open web browser with url in response
				String url = mobj.getString("url");

				if (Desktop.isDesktopSupported()) {
					Desktop desktop = Desktop.getDesktop();
					try {
						desktop.browse(new URI(url));
					} catch (IOException e) {

						e.printStackTrace();
					} catch (URISyntaxException e) {

						e.printStackTrace();
					}
				} else {
					Runtime runtime = Runtime.getRuntime();
					System.out.println("opening browser .....");
					try {
						runtime.exec("xdg-open " + url);
					} catch (IOException e) {

						e.printStackTrace();
					}
				}
			}
			if (key.equals("isValid")
					&& mobj.getString("isValid").equals("True")) {
				// Dropbox authentication successful
				// System.out.println("Google authentication is true !");
				googleAuthentication = true;
			}

		}
		// System.out.println("auth : exiting goog auth ");

	}

	public static String convertStreamToString(InputStream is) 
	{
		
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		StringBuilder sb = new StringBuilder();

		String line = null;
		
		try {
			while ((line = reader.readLine()) != null) {
				sb.append(line + "\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return sb.toString();
	}

	public void printAccountInfo()
	{
		System.out.println("Google UserID: " + this.gaccount.userid );
		System.out.println("Google EmailID: " + this.gaccount.emailid);
		System.out.println("Dropbox UserID: " +  this.dbaccount.userid);
		System.out.println("Dropbox Access Token: " + this.dbaccount.access_token);
	}
	public static void main(String... args) throws JSONException, IOException,
			ClassNotFoundException {
		
		Accounts test = new Accounts();
		test.dropboxAuthenticate();
	}
	
	GoogleAccount gaccount = new GoogleAccount();
	DropboxAccount dbaccount = new DropboxAccount();
	
	private static Accounts accounts = null;
	
	public static Accounts getObject(){
		
		if (accounts == null) {
			accounts = new Accounts();
		}
		return accounts;
	}

}

class GoogleAccount{
	String userid;
	String emailid;
	public GoogleAccount()
	{
		this.userid = "";
		this.emailid = "";
	}
	
	public GoogleAccount(String userid) {
		this.userid=userid;
	}
}

class DropboxAccount{
	String access_token;
	String userid;
	public DropboxAccount()
	{
		this.access_token = "";
		this.userid = "";
	}
	public DropboxAccount(String userid, String access_token) {
		this.access_token = access_token;
		this.userid = userid;
	}

}