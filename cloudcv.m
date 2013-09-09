function output = cloudcv
    output.start=@start;
    output.uploadData=@uploadData;
end

function [r1, r2 ,r3] = start(configFile, imageDir, resultDir, execName)
    
%     obj=javaObject('Sockets_CCV',imageDir,resultDir);
    cp = javaObject('ConfigParser',configFile);
    
    javaMethod('readConfigFile', cp);
    javaMethod('parseArguments', cp, imageDir, resultDir, execName);
    javaMethod('getParams',cp);
    obj1=javaObject('UploadData', cp);
    
%     t = javaObject('java.lang.Thread', obj1);
%     javaMethod('start', t);
    
    obj2=javaObject('SocketConnection', cp.executable_name, cp.output_path);
    
    t = javaObject('java.lang.Thread', obj1);
    javaMethod('socketIOConnection',obj2)
    javaMethod('start', t);
    
    r1=cp;
    r2=obj1;
    r3=obj2;
    
end


