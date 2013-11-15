%Setup.m: Used to initialize the variable
function [output] = setup(currDir)
try
    addpath(strcat(currDir,'/urlreadpost'), strcat(currDir,'/urlread2'));
catch exception
    if exception
        output=0;
    else error('problem adding path. No error caught');
    end
end

% check if redis-server is up
[status,result] = system('redis-cli ping');
if (status ~= 0) && (status~=1)
    error('redis-client not present. Need redis for communicating with CloudCV servers. Perhaps redis-cli is not in path? (See help setenv)');
end
if ~isequal(result,'PONG')
    [status, result] = system('redis-server &');
    if (status ~= 0) 
        disp(result);
        error('redis-server not present. Need redis for communicating with CloudCV servers');
    end
    disp('Starting redis-server');
end


javaaddpath(strcat(currDir,'/jcloudcv/lib/commons-codec-1.6.jar'))
javaaddpath(strcat(currDir,'/jcloudcv/lib/commons-logging-1.1.1.jar'))
javaaddpath(strcat(currDir,'/jcloudcv/lib/commons-math-2.2.jar'))
javaaddpath(strcat(currDir,'/jcloudcv/lib/commons-pool-1.3.jar'))
javaaddpath(strcat(currDir,'/jcloudcv/lib/fluent-hc-4.2.5.jar'))
javaaddpath(strcat(currDir,'/jcloudcv/lib/gson-2.2.4.jar'))
javaaddpath(strcat(currDir,'/jcloudcv/lib/httpclient-4.2.5.jar'))
javaaddpath(strcat(currDir,'/jcloudcv/lib/httpclient-cache-4.2.5.jar'))
javaaddpath(strcat(currDir,'/jcloudcv/lib/httpcore-4.2.4.jar'))
javaaddpath(strcat(currDir,'/jcloudcv/lib/httpmime-4.2.5.jar'))
javaaddpath(strcat(currDir,'/jcloudcv/lib/json-20090211.jar'))
javaaddpath(strcat(currDir,'/jcloudcv/bin/jcloudcv.jar'))
javaaddpath(strcat(currDir,'/jcloudcv/lib/java_websocket.jar'))
javaaddpath(strcat(currDir,'/jcloudcv/lib/json-org.jar'))
javaaddpath(strcat(currDir,'/jcloudcv/lib/jedis-2.1.0.jar'))
javaaddpath(strcat(currDir,'/jcloudcv/lib/slf4j-simple-1.6.2.jar'))
javaaddpath(strcat(currDir,'/jcloudcv/lib/slf4j-api-1.6.1.jar'))
javaaddpath(strcat(currDir,'/jcloudcv/lib/WebSocket.jar'))
javaaddpath(strcat(currDir,'/jcloudcv/lib/WebSocket1.jar'))
import Sockets_CCV.*
import io.socket.SocketIO.*