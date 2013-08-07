function output = cloudcv
    output.start=@start;
    output.startSocketConnection=@startSocketConnection;
    output.uploadData=@uploadData;
end

function [r1, r2 ,r3] = start(imageDir, resultDir)
    
    obj=javaObject('Sockets_CCV',imageDir,resultDir);
    
    obj1=javaObject('UploadData', obj.imagepath, obj.savepath);
    
    t = javaObject('java.lang.Thread', obj1);
    javaMethod('start', t);
    
    obj2=javaObject('SocketConnection');
    
    r1=obj;
    r2=obj1;
    r3=obj2;
    
end

function result = startSocketConnection(obj)
    obj1=javaObject('UploadData', obj.imagepath, obj.savepath);
    t = javaObject('java.lang.Thread', obj1);
    javaMethod('start', t);
    obj1=javaObject('SocketConnection')
    result=obj1;
end

function result = uploadData(obj)
    obj1=javaObject('SocketConnection')
    javaMethod('socketIOConnection',obj1)
    
end

