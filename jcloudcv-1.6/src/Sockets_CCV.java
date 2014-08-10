
import java.io.IOException;

import org.apache.http.impl.conn.tsccm.WaitingThread;
import org.idevlab.rjc.RedisNode;
import org.idevlab.rjc.RedisOperations;
import org.idevlab.rjc.ds.SimpleDataSource;
import org.json.JSONException;

import sun.awt.windows.ThemeReader;
import io.socket.*;

public class Sockets_CCV {
	public static Accounts acc = new Accounts();
	public Sockets_CCV() 
	{	
	}
	
	public static void main(String[] args) throws IOException, InterruptedException 
	{
		SimpleServer ss = new SimpleServer();
		Thread t_sserver = new Thread(ss);
		t_sserver.start();
		
		System.out.println("config path is " + Accounts.CONFIG_PATH);
		
		System.out.println(acc.CONFIG_PATH);

		try {
			acc.dropboxAuthenticate();
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		while(!Accounts.dropboxAuthentication)
		{
			
		}
		acc.printAccountInfo();
		SimpleServer.ServerRunner.stopServer = true;
		
		

        ConfigParser cp = new ConfigParser("/home/dexter/workspace/mcloudcv/config.json");
		cp.readConfigFile();
		
		int val = cp.parseArguments("","","ImageStitch");
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
                Thread.sleep(3000);
                sock.socket_disconnect();

//			    TEST CODE TO CHECK IF RESTART CODE WORKS PROPERLY
				//sock.updateParameters(cp.executable_name, cp.output_path);
				//sock.socketIOConnection();
//				udobj = new UploadData(cp);
//				t = new Thread(udobj);
//				t.start();
//                t.join();
//
				sock.socket_disconnect();
				
			} 
			catch (Exception e) 
			{
			
				e.printStackTrace();
			}

		}
		
	}
	
}
