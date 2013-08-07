import io.socket.SocketIO;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JOptionPane;

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
import org.idevlab.rjc.RedisNode;
import org.idevlab.rjc.RedisOperations;
import org.idevlab.rjc.ds.SimpleDataSource;
import org.idevlab.rjc.message.MessageListener;
import org.idevlab.rjc.message.RedisNodeSubscriber;
import org.idevlab.rjc.message.SubscribeListener;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class UploadData implements Runnable, SubscribeListener, MessageListener
{
	String imagepath;
	String savepath;
	String socketid;
	String sessionid;
	SocketIO _socket;
	RedisOperations redis;
	RedisNodeSubscriber subscriber;
	
	
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
	public void onMessage(String channel, String message) {
		// TODO Auto-generated method stub
    	try 
    	{
    		System.out.println(message);
    		
			JSONObject jobj = new JSONObject(message.toString());
			Iterator<String> itr=jobj.keys();
			
			while(itr.hasNext())
			{
				String key=itr.next();
				
				System.out.println(key);
				
				if(key.equals("socketid"))
				{
					socketid = jobj.getString("socketid");
				}
				
				if(key.equals("picture"))
				{
					String str = new String();
					str = jobj.getString("picture");
					System.out.println(str);
					this.getImageAndSave(str);
					
					JOptionPane.showMessageDialog(null, "Image Saved: "+ this.savepath);
					
					redis.publish("c1", "us");
					
					subscriber.unsubscribe();
					subscriber.close();
				}
			}
			
			//System.out.println(message);
    	
    	} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public UploadData(String imagepath, String savepath)
	{
		this.imagepath = imagepath;
		this.savepath = savepath;
		this.socketid = new String();
		
		
		System.out.println("Inside Constructor");
		
		redis = new RedisNode(new SimpleDataSource("localhost"));
		
		
		subscriber = new RedisNodeSubscriber();
	    subscriber.setDataSource(new SimpleDataSource("localhost"));
	    
	    subscriber.setSubscribeListener(this);
	    
	    subscriber.subscribe("c2");
	    
	    subscriber.setMessageListener(this);
	    
	    Thread t = new Thread(new Runnable() {
            public void run() {
                subscriber.runSubscription();
            }
        });
       t.start();
	    
        
		System.out.println("Coming Out");
		
	}
	public void getImageAndSave(String imagepath)
	{
		try {
		HttpClient hc= new DefaultHttpClient();
		HttpGet get = new HttpGet(imagepath);
		
			HttpResponse response = hc.execute(get);
			InputStream is = response.getEntity().getContent();
			FileOutputStream out = new FileOutputStream(this.savepath);
			
			int data=is.read();
		
			System.out.println("\n\n\n\n");
			while(data!=-1) {
			  //do something with data...
				
				out.write(data);
				data=is.read();
			}
			is.close();
			out.close();
			System.out.println("Yayee! Done. Check.");
			
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
			//image=resultImage.getString("result");
			//getImageAndSave(image);
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
					
					token=matcher.group(2);
					
				}
				
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
				
				System.out.println("Pausing for socketid");
				Thread.sleep(2000);
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

	@Override
	public void run() {
		// TODO Auto-generated method stub

			try {
				System.out.println("SocketID: " + socketid);
				this.CloudCVPostRequest();
				System.out.println("######################");
				
				//Thread.sleep(3000);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}

}
