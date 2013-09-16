import io.socket.IOAcknowledge;
import io.socket.IOCallback;
import io.socket.SocketIO;

import java.util.Iterator;

import javax.net.ssl.SSLContext;

import org.idevlab.rjc.RedisNode;
import org.idevlab.rjc.RedisOperations;
import org.idevlab.rjc.ds.SimpleDataSource;
import org.idevlab.rjc.message.MessageListener;
import org.idevlab.rjc.message.RedisNodeSubscriber;
import org.idevlab.rjc.message.SubscribeListener;

import org.json.JSONException;
import org.json.JSONObject;

class SocketCallback implements IOCallback
{
	SocketIO socket;
	
	String socketid;
	String exec_name;
	RedisOperations redis;
	
	public SocketCallback(SocketIO socket, String exec_name)
	{
		this.socket=socket;
		this.exec_name=exec_name;
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
        //System.out.println("Server said: " + data);
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
			//System.out.println(jobj.length());
			
			Iterator<String> itr=jobj.keys();
			while(itr.hasNext())
			{
				String key=itr.next();
				if(key.equals("visit"))
				{
				}
				else if(key.equals("socketid"))
				{
					socketid=jobj.getString("socketid");
					
					System.out.println("SocketID: "+socketid);
					
					socketid= "{socketid:" + socketid + "}";
					
					this.redis.publish("intercomm2", socketid);

				}
				else if(key.equals("name"))
				{
					
					socket.emit("send_message", this.exec_name);
				}
				else if(key.equals("data"))
				{
					String str=jobj.getString("data");
					System.out.println("Data:" + str);
					this.redis.publish("intercomm2","{unsubscribe:\"\"}");
				}
				else if(key.equals("picture"))
				{
					String str=jobj.getString("picture");
					System.out.println("Picture: " + str);
					str= "{picture: \"" + str + "\"}";
					this.redis.publish("intercomm2", str);
				}
				else if(key.equals("mat"))
				{
					String str=jobj.getString("mat");
					System.out.println("Mat: " + str);
					str= "{mat: \"" + str + "\"}";
					this.redis.publish("intercomm2", str);
				}
				else if(key.equals("request_data"))
				{
					socket.emit("send_message", "data");
				}
				
			}
			
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
     
}


class SocketConnection implements SubscribeListener, MessageListener
{

	RedisNodeSubscriber _subscriber;
	RedisOperations _redis;
	
	SocketIO socket;
	
	String _output_path;
	String _exec_name;
     
	public void onSubscribe(String channel, long subscribedChannels) {
        //System.out.println("s: " + channel + ":" + subscribedChannels);
    }

    public void onUnsubscribe(String channel, long subscribedChannels) {
        //System.out.println("us: " + channel + ":" + subscribedChannels);
    }

    public void onPSubscribe(String pattern, long subscribedChannels) {
        //System.out.println("ps: " + pattern + ":" + subscribedChannels);
    }

    public void onPUnsubscribe(String pattern, long subscribedChannels) {
     //   System.out.println("pus: " + pattern + ":" + subscribedChannels);
    }
    
    @Override
	public void onMessage(String channel, String message) 
    {
		// TODO Auto-generated method stub
    	System.out.println("Message From Server:" + message);
    	
    	if(message.startsWith("unsubscribe"))
    	{
    	//	socket.disconnect();
    		if(socket.isConnected())
    			System.out.println("Still Connected");
    		else
    			System.out.println("Socket Disconnected");
    		
    		if(_subscriber.isConnected()){
    			_subscriber.unsubscribe();
    			_subscriber.close();
    			System.out.println("Redis Subscriber for Sockets Disconnected");
    		}
    		//System.out.println("Subscriber2 Closed");
    		
    	}
    	
	}
    public void startRedis()
    {
        this.socket.emit("send_message","socketid");
        
    	_subscriber = new RedisNodeSubscriber();
	    _subscriber.setDataSource(new SimpleDataSource("localhost"));
	    
	    _subscriber.setSubscribeListener(this);
	  
	    _subscriber.subscribe("intercomm");    
	    _subscriber.setMessageListener(this);
	    
	    
	    Thread t = new Thread(new Runnable() {
            public void run() {
                _subscriber.runSubscription();
            }
        });
        t.start();
        
        System.out.println("New Redis Connection Formed");
    }
    
    public void updateParameters(String exec_name, String output_path)
    {
    	this._exec_name = exec_name;
    	this._output_path = output_path;
    }
    
	public SocketConnection(String exec_name, String output_path)
	{
		
		this._exec_name = exec_name;
		this._output_path = output_path;

	}
	
	public void socketIOConnection() throws Exception
	{
		SocketIO.setDefaultSSLSocketFactory(SSLContext.getDefault());
		
		socket = new SocketIO("http://godel.ece.vt.edu:8000/");
		
		try {
			socket.connect(new SocketCallback(socket,this._exec_name));

		} catch (Exception e) {

			e.printStackTrace();
		}
		
		this.startRedis();	
	}
	
	public void unsubscribe()
	{
		this.socket.disconnect();
		if(_subscriber.isConnected())
		{
			_subscriber.unsubscribe();
			_subscriber.close();
		}
	}
}
