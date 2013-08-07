
import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.Timer;
import java.util.regex.Pattern;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import org.idevlab.rjc.RedisNode;
import org.idevlab.rjc.RedisOperations;
import org.idevlab.rjc.SingleRedisOperations;
import org.idevlab.rjc.ds.DataSource;
import org.idevlab.rjc.ds.SimpleDataSource;
import org.idevlab.rjc.message.MessageListener;
import org.idevlab.rjc.message.RedisNodeSubscriber;
import org.idevlab.rjc.message.SubscribeListener;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.regex.*;

import java.io.PrintWriter;
import java.net.*;


import javax.net.ssl.SSLContext;
import javax.print.StreamPrintService;

import io.socket.*;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;

class SocketCallback implements IOCallback
{
	SocketIO socket;
	String socketid;
	
	RedisOperations redis;
	
	public SocketCallback(SocketIO socket)
	{
		this.socket=socket;
		socketid=new String();
		redis = new RedisNode(new SimpleDataSource("localhost"));
	}
	
    @Override
    public void onMessage(JSONObject json, IOAcknowledge ack) {
        try {
            System.out.println("Server said:" + json.toString(2));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onMessage(String data, IOAcknowledge ack) {
        System.out.println("Server said: " + data);
    }

    @Override
    public void onError(io.socket.SocketIOException socketIOException) {
        System.out.println("an Error occured");
        socketIOException.printStackTrace();
    }

    @Override
    public void onDisconnect() {
        System.out.println("Connection terminated.");
    }

    @Override
    public void onConnect() {
        System.out.println("Connection established");
    }

    @Override
    public void on(String event, IOAcknowledge ack, Object... args) {
       // System.out.println("Server triggered event '" + event + "'  : "+args[0].toString() );
        JSONObject jobj;
        
		try {
			jobj = new JSONObject(args[0].toString());
			System.out.println(jobj.length());
			
			Iterator<String> itr=jobj.keys();
			while(itr.hasNext())
			{
				String key=itr.next();
				//System.out.println(key);
				if(key.equals("visit"))
				{
					System.out.println("visit");
				}
				else if(key.equals("socketid"))
				{
					socketid=jobj.getString("socketid");
					
					socketid= "{socketid:" + socketid + "}";
					
					this.redis.publish("c2", socketid);
					
					System.out.println("****************");
				}
				else if(key.equals("name"))
				{
					System.out.println("****************");
					socket.emit("send_message", "Hi!!");
					//this.socket.disconnect();
				}
				else if(key.equals("data"))
				{
					String str=jobj.getString("data");
					System.out.println("Data:" + str);
				}
				else if(key.equals("picture"))
				{
					String str=jobj.getString("picture");
					System.out.println("Picture: " + str);
					str= "{picture: \"" + str + "\"}";
					System.out.println("*********"+str.charAt(14));
					this.redis.publish("c2", str);
				}
			}
			
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
        //JsonObject obj=new JsonObject();
    }
     
}


class SocketConnection implements SubscribeListener, MessageListener
{

	RedisNodeSubscriber subscriber;
	RedisOperations redis;
	SocketIO socket;
     
	public void onSubscribe(String channel, long subscribedChannels) {
        System.out.println("s: " + channel + ":" + subscribedChannels);
    }

    public void onUnsubscribe(String channel, long subscribedChannels) {
        System.out.println("us: " + channel + ":" + subscribedChannels);
    }

    public void onPSubscribe(String pattern, long subscribedChannels) {
        System.out.println("ps: " + pattern + ":" + subscribedChannels);
    }

    public void onPUnsubscribe(String pattern, long subscribedChannels) {
        System.out.println("pus: " + pattern + ":" + subscribedChannels);
    }
    
    @Override
	public void onMessage(String channel, String message) 
    {
		// TODO Auto-generated method stub
    	System.out.println(message);
    	if(message.startsWith("us"))
    	{
    		socket.disconnect();
    		subscriber.unsubscribe();
    		subscriber.close();   		
    		
	
    		System.out.println("Connections Closed");
    		
    	}
    	
	}
   
	public SocketConnection()
	{

		System.out.println("Inside Constructor");
		
		redis = new RedisNode(new SimpleDataSource("localhost"));
		
		subscriber = new RedisNodeSubscriber();
	    subscriber.setDataSource(new SimpleDataSource("localhost"));
	    
	    subscriber.setSubscribeListener(this);
	    
	    subscriber.subscribe("c1");
	    
	    subscriber.setMessageListener(this);
	    
	    Thread t = new Thread(new Runnable() {
            public void run() {
                subscriber.runSubscription();
            }
        });
        t.start();
	    
        
		System.out.println("Coming Out");
	}
	
	public SocketIO socketIOConnection() throws Exception
	{
		SocketIO.setDefaultSSLSocketFactory(SSLContext.getDefault());
		
		socket = new SocketIO("http://godel.ece.vt.edu:8000/");
		
		try {
			socket.connect(new SocketCallback(socket));
			
			//String result=this.CloudCVPostRequest();
			// This line is cached until the connection is establisched.
			//socket.send("Hello Server!");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//redis.publish("c2", socket);
		
		return socket;
	}	
}

public class Sockets_CCV {
	
	public String imagepath;
	public String savepath;
	public String socketid;
	public String sessionid;
	public SocketIO _socket;
	
	public void socketConnection()
	{
		   try {
			   	Socket echoSocket = null;
			   	PrintWriter out = null;
			    BufferedReader in = null;

			    try {
			    	
			        echoSocket = new Socket("128.173.88.252", 4444);
			        out = new PrintWriter(echoSocket.getOutputStream(), true);
			        
			        in = new BufferedReader(new InputStreamReader(
			                                    echoSocket.getInputStream()));
			        String line=new String();
			        
			        while((line=in.readLine())!=null)
			        {
			        	System.out.println(line);
			        }
			    } catch (UnknownHostException e) {
			        System.err.println("Don't know about host: cloudcv.");
			        System.exit(1);
			    } catch (IOException e) {
			        System.err.println("Couldn't get I/O for "
			                           + "the connection to: taranis.");
			        System.exit(1);
			    }
			    
			BufferedReader stdIn = new BufferedReader(
			                               new InputStreamReader(System.in));
			String userInput;

			while ((userInput = stdIn.readLine()) != null) {
			    out.println(userInput);
			    System.out.println("echo: " + in.readLine());
			}

			out.close();
			in.close();
			stdIn.close();
			echoSocket.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
	}
	
	
	public Sockets_CCV(String imagepath, String savepath) {
		// TODO Auto-generated constructor stub
		this.imagepath=imagepath;
		this.savepath=savepath;
		socketid=new String();
		sessionid=new String();
	}
	
	public void getImageAndSave(String imagepath)
	{
		try {
		HttpClient hc= new DefaultHttpClient();
		HttpGet get = new HttpGet("http://godel.ece.vt.edu"+imagepath);
		
			HttpResponse response = hc.execute(get);
			InputStream is = response.getEntity().getContent();
			FileOutputStream out = new FileOutputStream(this.savepath);
			
			int data=is.read();
		
			//System.out.println("\n\n\n\n");
			while(data!=-1) {
			  //do something with data...
				
				out.write(data);
				data=is.read();
			}
			is.close();
			out.close();
			//System.out.println("Yayee! Done. Check.");
			
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public String parseJson(JSONArray array)
	{
		String result=new String();
		String image=new String();
		try {
			JSONObject text= array.getJSONObject(array.length()-2);
			JSONObject resultImage= array.getJSONObject(array.length()-1);
			result=text.getString("text");
			image=resultImage.getString("result");
			getImageAndSave(image);
		} catch (JSONException e) {
			// TODO Auto-generated catch block:
			e.printStackTrace();
		}
		return result;
	}
	
	public String CloudCVPostRequest() throws UnsupportedEncodingException
	{
		HttpClient hc= new DefaultHttpClient();
		
		/*Get request to create a unique id*/
		HttpGet get = new HttpGet("http://godel.ece.vt.edu/cloudcv/matlab/");
		HttpResponse getResponse;
		
		String token = new String();
		try {
			getResponse = hc.execute(get);
			HttpEntity getEntity=getResponse.getEntity();
			InputStream getResult=getEntity.getContent();
			BufferedReader getbr=new BufferedReader(new InputStreamReader(getResult));
			String getLine=new String();
			
			Pattern csrf_token_pat=Pattern.compile("^(META:\\{'CSRF_COOKIE': ')((\\w)+)(',)$");
			while((getLine=getbr.readLine())!=null)
			{
				Matcher matcher=csrf_token_pat.matcher(getLine);
			
				if(matcher.matches())
				{
					//System.out.print("****************");
					token=matcher.group(2);
					
				}
				//System.out.println(getLine);
			}
		} catch (ClientProtocolException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		
		/*End*/
		
		HttpPost post = new HttpPost("http://godel.ece.vt.edu/cloudcv/matlab/");
		
		MultipartEntity reqentity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
		
		final Pattern pat= Pattern.compile("([^\\s]+(\\.(jpg|png|gif|bmp))$)", Pattern.CASE_INSENSITIVE);
		
		File dirList = new File(this.imagepath);
		//System.out.println(dirList.isDirectory());
		File[] imageList= dirList.listFiles(new FileFilter() {
			
			@Override
			public boolean accept(File file) {
				// TODO Auto-generated method stub
				pat.matcher(file.getName()).matches();
				//System.out.println(file.getName());
				return true;
			}
		});
		
		int imagecount = imageList.length;
		
		reqentity.addPart("count", new StringBody(Integer.toString(imagecount)));
		reqentity.addPart("token", new StringBody(token));
		while(socketid.equals(""))
		{
			try {
				Thread.sleep(2000);
				System.out.println("Pausing for socketid");
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		System.out.println(socketid);
		reqentity.addPart("socketid", new StringBody(this.socketid));
		
		for(int i=0;i<imagecount;i++)
		{
			FileBody fileBody = new FileBody(imageList[i], "application/octet-stream");
			
			try{
				reqentity.addPart("file", fileBody);
			}catch(Exception e)
			{
				System.out.println(e);
			
			}
		}
		
		post.setEntity(reqentity);
		String result = new String("");

		try {
			System.out.println("Uploading Images, Waiting for response");
			HttpResponse response = hc.execute(post);
			HttpEntity entity=response.getEntity();
			char []cbuf = null;

			if(entity!=null)
			{
				//System.out.println("Uploading Images, Waiting for response");
				
				InputStream instream = entity.getContent();
				InputStreamReader isr = new InputStreamReader(instream);
				BufferedReader br= new BufferedReader(isr);
				String line;
				
				while((line=br.readLine())!=null)
				{
					//System.out.println(line);	
					
					try{
						if(line.startsWith("[")&&line.endsWith("]"))
						{
							JSONArray array= new JSONArray(line);
						//	System.out.println(array.length());
							if(array.length()>0)
							{
								result=this.parseJson(array);
								break;
							}
							
						}
				
					}catch(Exception e)
					{
						System.out.println(e);
					}		
				}
				
				instream.close();
			}
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
	}
	
	
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		
		Sockets_CCV testObj=new Sockets_CCV("/home/dexter/Pictures/test_download/1/","/home/dexter/Pictures/test_download/result.jpg");
		try 
		{
			UploadData udobj =new UploadData(testObj.imagepath, testObj.savepath);
			
			SocketConnection sock = new SocketConnection();
			sock.socketIOConnection();
		
			RedisOperations redis = new RedisNode(new SimpleDataSource("localhost"));
		    redis.publish("c1", "message");  
		    
		    new Thread(udobj).start();
		} 
		catch (Exception e) 
		{
			
			e.printStackTrace();
		}
	
	}
	
}
