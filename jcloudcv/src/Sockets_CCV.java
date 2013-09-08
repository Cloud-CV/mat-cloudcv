
import java.io.IOException;

import org.idevlab.rjc.RedisNode;
import org.idevlab.rjc.RedisOperations;
import org.idevlab.rjc.ds.SimpleDataSource;

import io.socket.*;

public class Sockets_CCV {
	
	public String imagepath;
	public String savepath;
	public String socketid;
	public String sessionid;
	public SocketIO _socket;
	
	public Sockets_CCV(String imagepath, String savepath) {
		// TODO Auto-generated constructor stub
		this.imagepath=imagepath;
		this.savepath=savepath;
		socketid=new String();
		sessionid=new String();
	}

	public static void main(String[] args) throws IOException {
		ConfigParser cp = new ConfigParser("/home/dexter/projects/vt/pcloudcv/config.json");
		cp.readConfigFile();
		cp.parseArguments(args);
		cp.getParams();
		
		
		Sockets_CCV testObj=new Sockets_CCV(cp.executable_name,cp.output_path);
		
		try 
		{
			UploadData udobj =new UploadData(cp);
			
			SocketConnection sock = new SocketConnection(cp.executable_name, cp.output_path);
			sock.socketIOConnection();
		
			RedisOperations redis = new RedisNode(new SimpleDataSource("localhost"));
		    redis.publish("intercomm", "message");  
		    
		    new Thread(udobj).start();
		} 
		catch (Exception e) 
		{
			
			e.printStackTrace();
		}
		
	
	}
	
}
