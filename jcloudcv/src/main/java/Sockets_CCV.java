
import java.io.IOException;

import org.apache.http.impl.conn.tsccm.WaitingThread;
import org.idevlab.rjc.RedisNode;
import org.idevlab.rjc.RedisOperations;
import org.idevlab.rjc.ds.SimpleDataSource;

import sun.awt.windows.ThemeReader;

import io.socket.*;

public class Sockets_CCV {
	public Sockets_CCV() 
	{	
	}
	
	public static void main(String[] args) throws IOException 
	{
		ConfigParser cp = new ConfigParser("/home/dexter/workspace/mcloudcv/config.json");
		cp.readConfigFile();
		int val = cp.parseArguments("","","");
		cp.getParams();
		
		if(val==1)
		{
			try 
			{
				UploadData udobj =new UploadData(cp);
			
				SocketConnection sock = new SocketConnection(cp.executable_name, cp.output_path);
				sock.socketIOConnection();
		
				Thread t = new Thread(udobj);
				t.start();
				t.join();

			/* TEST CODE TO CHECK IF RESTART CODE WORKS PROPERLY*/
				sock.updateParameters(cp.executable_name, cp.output_path);
				sock.socketIOConnection();
				udobj = new UploadData(cp);
				t = new Thread(udobj);
				t.start();
                t.join();
				
				sock.socket_disconnect();
				
			} 
			catch (Exception e) 
			{
			
				e.printStackTrace();
			}

		}
		
	}
	
}
