import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

public class SimpleServer extends NanoHTTPD implements Runnable{

	public SimpleServer() {
		super(8000);
	}


	@Override
	public Response serve(String uri, Method method, Map<String, String> headers, Map<String, String> parms,
			Map<String, String> files) throws JSONException  {
		String code = null ; 
		String state = null ; 
		int i=0 ; 
		//System.out.println(uri);
		Set<String> keys = parms.keySet(); 

		for (String key : keys)  
		{  
			if(key.equals("state")) {
				//System.out.println("state is : "+ key );
				//System.out.println("value is : "+ parms.get(key) );
				state = parms.get(key) ;

			}
			else if (key.equals("code") ){

				//System.out.println("code is : "+ key );
				//System.out.println("value is : "+ parms.get(key) );
				code = parms.get(key) ; 
			}


		}  

		if(uri.startsWith("/dropbox_callback")) {

			//Dropbox calling....

			//send 'POST' to CCV
			HttpClient client = new DefaultHttpClient();
			HttpPost post = new HttpPost("http://cloudcv.org/cloudcv/callback/dropbox/");

			try {

				List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);


				nameValuePairs.add(new BasicNameValuePair("code", code )) ;
				nameValuePairs.add(new BasicNameValuePair("state", state )) ;
				nameValuePairs.add(new BasicNameValuePair("userid", Sockets_CCV.acc.gaccount.userid )); 

				post.setEntity(new UrlEncodedFormEntity(nameValuePairs));
				HttpResponse response = client.execute(post);
				String result = Accounts.convertStreamToString(response.getEntity().getContent());
				JSONObject mobj = new JSONObject(result);

				// here I want mob.getString("uid") and mob.getString("token")
				
				//System.out.println("dropbox callback result is  :"+result);
				
				Sockets_CCV.acc.dbaccount.access_token = mobj.getString("token");
				Sockets_CCV.acc.dbaccount.userid = mobj.getString("uid");
				
				Sockets_CCV.acc.writeFile(mobj,true ) ;
				Accounts.dropboxAuthentication = true;
				
				return new NanoHTTPD.Response(response.toString());

			}catch (IOException e) {
				e.printStackTrace();
			} 
			catch (ClassNotFoundException e ) {
				e.printStackTrace();
			}


		} 
		else {
			if (uri.startsWith("/callback")) {

				HttpClient client = new DefaultHttpClient();
				HttpPost post = new HttpPost("http://cloudcv.org/cloudcv/callback/google/");

				try {

					List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);

					//System.out.println(code);
					//System.out.println(state);
					
					nameValuePairs.add(new BasicNameValuePair("code",code));
					nameValuePairs.add(new BasicNameValuePair("state",state));
					code = null ; state= null ; 
					post.setEntity(new UrlEncodedFormEntity(nameValuePairs));
					HttpResponse response = client.execute(post);
					
					//System.out.println("request sent to the server !");
					//read the response 
					
					String result = Accounts.convertStreamToString(response.getEntity().getContent());
					
					//System.out.println("result after google callback is :"+result);
					
					JSONObject mobj = new JSONObject(result);
					
					Sockets_CCV.acc.gaccount.emailid = mobj.getString("email");
					Sockets_CCV.acc.gaccount.userid = mobj.getString("id");
					
					try{

						Sockets_CCV.acc.writeFile(mobj,false) ;

					}catch(ClassNotFoundException e ){e.printStackTrace();}

					Accounts.getObject().googleAuthentication = true;
					Accounts.googleAuthentication = true;

					return new NanoHTTPD.Response(result);

				}catch(IOException e){e.printStackTrace(); }

			}

		}

		return new NanoHTTPD.Response("CloudCV User Authentication");
	}

/*
	public static void main(String[] args) 
	{
		ServerRunner.run(SimpleServer.class, true);
	}

*/
	
	
	@Override
	public void run() {
		ServerRunner.run(SimpleServer.class, true);
	}
	
/*
	public class ServerRunnerThread implements Runnable{
		Class serverClass;
		boolean flag;
		
		public ServerRunnerThread(Class serverClass, boolean flag)
		{
			this.serverClass = serverClass;
			this.flag = flag;
		}
		@Override
		public void run() {
			try {
				executeInstance((NanoHTTPD) serverClass.newInstance(), flag);
			} catch (Exception e) {
				e.printStackTrace();
			}	
		}

		public  void executeInstance(NanoHTTPD server, boolean flag) {
			if (flag) {
				try {

					server.start();
				} catch (IOException ioe) {
					System.err.println("Couldn't start server:\n" + ioe);
					System.exit(-1);
				}

				
				System.out.println("Server started, Hit Enter to stop.\n");

				try {
					System.in.read();
				} catch (Throwable ignored) {
				}

				server.stop();
				System.out.println("Server stopped.\n");
				 
			} else {
				server.stop();

			}

		}
	}

*/
	
	
	public static class ServerRunner {
		public static boolean stopServer = true;
		public static void run(Class serverClass, boolean flag) {
			try {
				executeInstance((NanoHTTPD) serverClass.newInstance(), flag);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	
		public static void executeInstance(NanoHTTPD server, boolean flag) {
			if (flag) {
				try {
	
					server.start();
				} catch (IOException ioe) {
					System.err.println("Couldn't start server:\n" + ioe);
					System.exit(-1);
				}
					while(!stopServer){
						
					}
					/*
					System.out.println("Server started, Hit Enter to stop.\n");
	
					try {
						System.in.read();
					} catch (Throwable ignored) {
					}
					*/
					//server.stop();
					//System.out.println("Server stopped.\n");
				 
			} else {
				server.stop();
	
			}
	
		}
	}
}
