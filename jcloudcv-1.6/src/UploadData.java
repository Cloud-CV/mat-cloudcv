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
import java.util.List;
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
	String source_path;
	String output_path;
	String socketid;
	String executable_name;
	String sessionid;
	SocketIO socketIO_obj;
	RedisOperations redis_obj;
	RedisNodeSubscriber redis_subs;
	String params;
	Thread thread_redis_obj;
	
	public void onSubscribe(String channel, long subscribedChannels) {
        //System.out.println("s: " + channel + ":" + subscribedChannels);
    }

    public void onUnsubscribe(String channel, long subscribedChannels) {
        //System.out.println("unsubscribe: " + channel + ":" + subscribedChannels);
    }

    public void onPSubscribe(String pattern, long subscribedChannels) {
        //System.out.println("ps: " + pattern + ":" + subscribedChannels);
    }

    public void onPUnsubscribe(String pattern, long subscribedChannels) {
        //System.out.println("pus: " + pattern + ":" + subscribedChannels);
    }

    @Override
	public void onMessage(String channel, String message) {
    	try
    	{
			JSONObject jobj = new JSONObject(message);
			Iterator<String> itr=jobj.keys();
			while(itr.hasNext())
			{
				String key=itr.next();
				if(key.equals("socketid"))
				{
					socketid = jobj.getString("socketid");
				}
				
				if(key.equals("unsubscribe"))
				{
					redis_obj.publish("intercomm", "unsubscribe");
					
					if(redis_subs.isConnected()){
						this.redis_unsubscribe();
						System.out.println("Redis Subscriber for Uploading Data Disconnected");
					}
					else
					{
						System.out.println("Redis Subscriber for Uploading Data Already Disconnected");
					}
				}
				if(key.equals("picture"))
				{
					String url = new String();
					url = jobj.getString("picture");
                    String jobid = new String();
                    jobid = jobj.getString("jobid");
                    Job.jobid = jobid;

                    File theDir = new File(this.output_path, jobid);
                    Job.resultpath = theDir.getPath();
                    Job.executable = this.executable_name;
                    if(!theDir.exists())
                    {
                        theDir.mkdir();
                    }
                    String fileName = url.substring( url.lastIndexOf('/')+1, url.length());
                    File theFile = new File(theDir, fileName);
					this.getImageAndSave(url,theFile.getPath());
                    Job.addFiles(theFile.getPath());
				}
				
				if(key.equals("mat"))
				{
					String str = new String();
					str = jobj.getString("mat");
					this.getImageAndSave(str, "results"+this.socketid+".txt");
					
				}
                if(key.equals("error"))
                {
                    String str = new String();
                    str = jobj.getString("error");
                    this.getImageAndSave(str, "results"+this.socketid+".txt");

                }
			}
    	
    	} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	public UploadData(ConfigParser parser)
	{
		source_path = parser.source_path;

        Job.imagepath = source_path;

        output_path = parser.output_path;
		executable_name = parser.executable_name;
		params = parser.params.toString();
		
		socketid = new String();
		
		redis_obj = new RedisNode(new SimpleDataSource("localhost"));
		
		// Creating Redis Subscriber. Subscribes to intercomm2 channel.
		redis_subs = new RedisNodeSubscriber();
	    redis_subs.setDataSource(new SimpleDataSource("localhost"));
	    redis_subs.setSubscribeListener(this);
	    redis_subs.subscribe("intercomm2");
	    redis_subs.setMessageListener(this);
        // Blocking API, hence it runs it its own thread.
	    thread_redis_obj = new Thread(new Runnable() 
	    {
            public void run() 
            {
                redis_subs.runSubscription();
            }
        });
	    
      thread_redis_obj.start();

	}
	
	public void getImageAndSave(String imagepath, String filename)
	{
        int i = 0;
        while(i < 5)
        {
            try {
                HttpClient hc= new DefaultHttpClient();
                HttpGet get = new HttpGet(imagepath);

                HttpResponse response = hc.execute(get);
                InputStream is = response.getEntity().getContent();
                FileOutputStream out = new FileOutputStream(filename);

                int data=is.read();
                while(data!=-1) {

                    out.write(data);
                    data=is.read();
                }
                is.close();
                out.close();
                System.out.println("File Saved: " + filename);
                break;
            } catch (ClientProtocolException e) {
                System.out.println("Retrying to connect");
                i++;
            } catch (IOException e) {
                System.out.println("Retrying to connect");
                i++;
            }
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
		} catch (JSONException e) {
			// TODO Auto-generated catch block:
			e.printStackTrace();
		}
		return result;
	}

	public String getToken()
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
			getResult.close();
			getbr.close();
			
		} catch (ClientProtocolException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		return token;
	}
	
	public File[] getImageList()
	{
		
		final Pattern pat= Pattern.compile("([^\\s]+(\\.(jpg|png|gif|bmp|jpeg))$)", Pattern.CASE_INSENSITIVE);
		
		File dirList = new File(this.source_path);
		
		File[] imageList= dirList.listFiles(new FileFilter() {
			
			@Override
			public boolean accept(File file) {
				if(file.isFile())
				{
					pat.matcher(file.getName()).matches();

					return true;
				}
				else return false;
				
			}
		});
		
		return imageList;

	}
	
	private String[] identifySourcePath() throws Exception {
		String[] source_path_tokens = this.source_path.split(":");
		if ( source_path_tokens.length == 2 ){
			source_path_tokens[0] = source_path_tokens[0].replaceAll("\\s+","");
			source_path_tokens[1] = source_path_tokens[1].replaceAll("\\s+","");
			return source_path_tokens;
			
		}
		else {
			throw new Exception();
		}
		//System.out.println("Source Path: " + this.source_path + "\n Split: " + source_path_tokens);
	}
	
	private void addAccountParameters(MultipartEntity requentity) {
		
		//requentity.addPart("");
	}
	
	public String CloudCVPostRequest() throws UnsupportedEncodingException, InterruptedException
	{
	
		MultipartEntity reqentity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
		
		String token = getToken();		
		
		//this.addAccountParameters();
		
		String[] source_path_tokens;
		/*
		try {
			source_path_tokens = this.identifySourcePath();
			System.out.println(source_path_tokens[0] + "\n" + source_path_tokens[1]);
			
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		*/
		
		File[] imagelist = getImageList();
		int imagecount = imagelist.length;
		
		HttpClient hc= new DefaultHttpClient();
		
		// Previous URL was http://godel.ece.vt.edu/cloudcv/matlab/
		// Converted into a unique URL for both python and matlab api
		HttpPost post = new HttpPost("http://godel.ece.vt.edu/cloudcv/api/");
		
		reqentity.addPart("count", new StringBody(Integer.toString(imagecount)));
		reqentity.addPart("token", new StringBody(token));
		reqentity.addPart("executable",new StringBody(this.executable_name));
		reqentity.addPart("exec_params", new StringBody(this.params));
		
		/**
		 * Loop so that it waits for the socket connection to establish before it can
		 * communicate with the cloud servers
		 */
		int i =0;
		while(socketid.equals(""))
		{
			try
            {
                System.out.println("Waiting for socket connection ");
                if(i<3){
				    Thread.sleep(3000);
                    i++;
                }
                else
                {
                    throw new InterruptedException("Maximum retry over. Exit.");
                }
			} catch (InterruptedException e)
            {
                throw e;
			}
		}
		
		reqentity.addPart("socketid", new StringBody(this.socketid));
		
		/*
		 * Adding Files in the post request.
		 */
		for(i=0;i<imagecount;i++)
		{
			FileBody fileBody = new FileBody(imagelist[i], "application/octet-stream");
			
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
				InputStream instream = entity.getContent();
				InputStreamReader isr = new InputStreamReader(instream);
				BufferedReader br= new BufferedReader(isr);
				String line;
				
				while((line=br.readLine())!=null)
				{
					System.out.println(line);
				}
				isr.close();
				br.close();
				instream.close();
				
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}

	public void redis_unsubscribe()
	{
		if(this.redis_subs.isConnected())
		{
			this.redis_subs.unsubscribe();
			this.redis_subs.close();
		}
	}
	@Override
	public void run()
    {

			try
            {
				this.CloudCVPostRequest();
				
			} catch (Exception e) {
				
				e.printStackTrace();
			}
			try
            {   
				thread_redis_obj.join();
                System.out.println("Redis Thread Ended after unsubscribe");
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
	}

}
