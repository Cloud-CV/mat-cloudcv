import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

class ArgumentException extends Exception
{
	int val;
	public ArgumentException(int val) {
		this.val = val;
	}
	@Override
	public String toString() {
		String str = new String();
		if(val==0)
			str = "Executable Not found. Possibly a typing error. Should be:\n1.) ImageStitch\n2.) VOCRelease5";
		if(val==1)
			str = "Path to the folder containing images is not defined.\n Define it through -I parameter\n";
		if(val==2)
			str = "Path to the output folder is not defined.\n Define it through -O parameter\n";
		return str;
	}
}
public class ConfigParser 
{
	String executable_name, source_path, output_path, file;
	JSONArray data;
	JSONObject json_data, params;
	
	public ConfigParser(String file) {
		executable_name = new String();
		data = new JSONArray();
		this.file = file;
		source_path = new String();
		output_path = new String();
		this.params = new JSONObject();
	}
	
	void readConfigFile()
	{
		try {
			BufferedReader br = new BufferedReader(new FileReader(file));
			String contents = new String();
			String line = new String();
			while((line = br.readLine()) != null)
			{
				contents+=line;
			}
			//System.out.println(contents);
			
			json_data = new JSONObject(contents);
			Iterator<String> it = json_data.keys();
			
			String keys = new String();
			while(it.hasNext())
			{	keys = it.next();
				if(keys.equals("config"))
				{
					data = (JSONArray)json_data.getJSONArray(keys);
				}
				else if(keys.equals("exec"))
				{
					executable_name=(String) json_data.get(keys);
				}
			}
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	void changePath()
	{
		try
		{
			int l = data.length();
			JSONObject config_data;
			
			for(int i=0;i<l;i++)
			{
				config_data = data.getJSONObject(i);
				
				if(config_data.getString("name").equals(executable_name))
				{
					this.source_path = config_data.getString("path");
					this.output_path = config_data.getString("output");
				}
			}
		}
		catch (Exception e) {
			
		}
	}
	void setParams()
	{
		try
		{
			int l = data.length();
			JSONObject config_data;
			
			for(int i=0;i<l;i++)
			{
				config_data = data.getJSONObject(i);
				
				if(config_data.getString("name").equals(executable_name))
				{
					this.params = config_data.getJSONObject("params");
				}
			}
		}
		catch (Exception e) {
			
		}	
	}
	void getParams()
	{
		System.out.println("Executable Name: "+this.executable_name);
		System.out.println("Output Path: "+this.output_path);
		System.out.println("Input Folder: "+this.source_path);
		System.out.println("Additional Parameters: "+this.params);
	}
	
	void parseArguments(String []list)
	{
		String sourcepath = new String();
		String resultapth = new String();
		String name = new String();
		
		if(list.length > 1 )
			sourcepath = list[1];
		if(list.length > 2)
			resultapth = list[2];
		if(list.length > 3)
			name = list[3];
		
		this.readConfigFile();
		this.changePath();
		this.setParams();
		
		try
		{
			if(!name.equals(""))
			{
				this.executable_name=name;
			}
			else if(executable_name.equals(""))
			{
				throw new ArgumentException(0);
			}
			
			if(!sourcepath.equals(""))
			{
				this.source_path = sourcepath;
			}
			else if(this.source_path.equals(""))
			{
				throw new ArgumentException(1);
			}
			
			if(!resultapth.equals(""))
			{
				this.output_path=resultapth;
			}
			else if(this.output_path.equals(""))
			{
				throw new ArgumentException(2);
			}
		}
		
		catch (ArgumentException e) {
			System.out.println(e.toString());
			System.exit(-1);
		}
	}
}
