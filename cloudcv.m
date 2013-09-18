function output = cloudcv
    output.initialize = @initialize;
    output.start=@start;
    output.reconnect = @reconnect;
end

function obj = initialize(configFile, imageDir, resultDir, execName)
    
    if ~exist('imageDir','var') || isempty(imageDir)
        imageDir = '';
    end
    if ~exist('resultDir','var') || isempty(resultDir)
        resultDir = '';
    end
    if ~exist('execName','var') || isempty(execName)
        execName = '';
    end

    cp = javaObject('ConfigParser',configFile);
    javaMethod('readConfigFile', cp);
    val = javaMethod('parseArguments', cp, imageDir, resultDir, execName);
    if(val==1)
        javaMethod('getParams',cp);
        obj=cp;
    end
    
end

function r = startUpload(cp)
    obj1=javaObject('UploadData', cp);
    t = javaObject('java.lang.Thread', obj1);
    javaMethod('start', t);
    r = obj1;
end

function [r1,r2] = reconnect(cp, obj, imageDir, resultDir, execName)
    val = javaMethod('parseArguments', cp, imageDir, resultDir, execName);
    if(val==1)
        javaMethod('getParams',cp);
    end
    
    r1 = startUpload(cp);
    
    javaMethod('updateParameters',obj, cp.executable_name, cp.output_path);
    javaMethod('startRedis', obj); 
    r2=obj;
end


function [r1,r2] = start(cp)
    r1 = startUpload(cp);   
    obj2=javaObject('SocketConnection', cp.executable_name, cp.output_path);
    javaMethod('socketIOConnection',obj2);
    r2=obj2;    
end


