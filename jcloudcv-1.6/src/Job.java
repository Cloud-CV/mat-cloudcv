import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
/**
 * Created by dexter on 3/15/14.
 */
public class Job
{
    public static String imagepath = new String();
    public static String resultpath = new String();
    public static String output = new String();
    public static String executable = new String();
    public static String jobid = new String();
    public static List<String> files = new Vector<String>();

    public static void addFiles(String path)
    {
        files.add(path);
    }

    public static String getExecutable(){
        return executable;
    }

}
