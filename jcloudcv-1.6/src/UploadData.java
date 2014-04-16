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
	String _source_path;
	String _output_path;
	String _socketid;
	String _executable_name;
	String sessionid;
	SocketIO _socket;
	RedisOperations _redis;
	RedisNodeSubscriber _subscriber;
	String _params;
	Thread _thread_redis;
	
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
					_socketid = jobj.getString("socketid");
				}
				
				if(key.equals("unsubscribe"))
				{
					_redis.publish("intercomm", "unsubscribe");
					
					if(_subscriber.isConnected()){
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

                    File theDir = new File(this._output_path, jobid);
                    Job.resultpath = theDir.getPath();
                    Job.executable = this._executable_name;
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
					this.getImageAndSave(str, "results"+this._socketid+".txt");
					
				}
                if(key.equals("error"))
                {
                    String str = new String();
                    str = jobj.getString("error");
                    this.getImageAndSave(str, "results"+this._socketid+".txt");

                }
			}
    	
    	} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	public UploadData(ConfigParser parser)
	{
		_source_path = parser.source_path;

        Job.imagepath = _source_path;

        _output_path = parser.output_path;
		_executable_name = parser.executable_name;
		_params = parser.params.toString();
		
		_socketid = new String();
		
		_redis = new RedisNode(new SimpleDataSource("localhost"));
		
		// Creating Redis Subscriber. Subscribes to intercomm2 channel.
		_subscriber = new RedisNodeSubscriber();
	    _subscriber.setDataSource(new SimpleDataSource("localhost"));
	    _subscriber.setSubscribeListener(this);
	    _subscriber.subscribe("intercomm2");
	    _subscriber.setMessageListener(this);
        // Blocking API, hence it runs it its own thread.
	    _thread_redis = new Thread(new Runnable() 
	    {
            public void run() 
            {
                _subscriber.runSubscription();
            }
        });
	    
      _thread_redis.start();

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
		
		final Pattern pat= Pattern.compile("([^\\s]+(\\.(jpg|png|gif|bmp))$)", Pattern.CASE_INSENSITIVE);
		
		File dirList = new File(this._source_path);
		
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
	
	public String CloudCVPostRequest() throws UnsupportedEncodingException, InterruptedException
	{
	
		String token = getToken();
		File[] imagelist = getImageList();
		int imagecount = imagelist.length;
		
		HttpClient hc= new DefaultHttpClient();
		HttpPost post = new HttpPost("http://godel.ece.vt.edu/cloudcv/matlab/");
		
		MultipartEntity reqentity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);

		reqentity.addPart("count", new StringBody(Integer.toString(imagecount)));
		reqentity.addPart("token", new StringBody(token));
		reqentity.addPart("executable",new StringBody(this._executable_name));
		reqentity.addPart("exec_params", new StringBody(this._params));
		int i =0;
		while(_socketid.equals(""))
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
		reqentity.addPart("socketid", new StringBody(this._socketid));
		
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
		if(this._subscriber.isConnected())
		{
			this._subscriber.unsubscribe();
			this._subscriber.close();
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
            {   //This will return when the redis thread ends after unsubscribe.
				_thread_redis.join();
                System.out.println("Redis Thread Ended after unsubscribe");
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
	}

}
