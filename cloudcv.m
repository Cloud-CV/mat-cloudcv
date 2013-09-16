function output = cloudcv
    output.setParam = @setParam;
    output.startSocket=@startSocket;
    output.startUpload=@startUpload;
    output.reconnect = @reconnect;
end

function r = setParam(configFile, imageDir, resultDir, execName)
    
%     obj=javaObject('Sockets_CCV',imageDir,resultDir);
    cp = javaObject('ConfigParser',configFile);
    
    javaMethod('readConfigFile', cp);
    val = javaMethod('parseArguments', cp, imageDir, resultDir, execName);
    if(val==1)
        
        javaMethod('getParams',cp);
        r=cp;
    end
    
end

function r = startUpload(cp)
    obj1=javaObject('UploadData', cp);
    t = javaObject('java.lang.Thread', obj1);
    javaMethod('start', t);
    r = obj1;
end

function r = startSocket(cp)
    obj2=javaObject('SocketConnection', cp.executable_name, cp.output_path);
    javaMethod('socketIOConnection',obj2);
    r=obj2;
    
end

function reconnect(cp, obj)
    javaMethod('updateParameters',obj, cp.executable_name, cp.output_path);
    javaMethod('startRedis', obj);
end



