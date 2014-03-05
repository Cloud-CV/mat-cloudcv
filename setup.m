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
    error('redis-client not present. Need redis for communicating with CloudCV servers');
end
if ~isequal(result,'PONG')
    [status, result] = system('redis-server &');
    if (status ~= 0) 
        disp(result);
        error('redis-server not present. Need redis for communicating with CloudCV servers');
    end
    disp('Starting redis-server');
end

% javaaddpath(strcat(currDir,'/jcloudcv/lib/com.springsource.org.apache.commons.codec-1.4.0.jar'))
% javaaddpath(strcat(currDir,'/jcloudcv/lib/com.springsource.org.apache.commons.codec-1.6.0.jar'))
% javaaddpath(strcat(currDir,'/jcloudcv/lib/com.springsource.org.apache.commons.logging-1.1.1.jar'))
% javaaddpath(strcat(currDir,'/jcloudcv/lib/com.springsource.org.apache.httpcomponents.httpcore-4.2.1.jar'))
% javaaddpath(strcat(currDir,'/jcloudcv/lib/com.springsource.org.apache.httpcomponents.httpclient-4.2.1.jar'))
% javaaddpath(strcat(currDir,'/jcloudcv/lib/com.springsource.org.apache.httpcomponents.httpclient-cache-4.2.1.jar'))
% javaaddpath(strcat(currDir,'/jcloudcv/lib/com.springsource.org.apache.httpcomponents.httpmime-4.2.1.jar'))
% javaaddpath(strcat(currDir,'/jcloudcv/lib/Java-WebSocket-1.3.0.jar'))
% javaaddpath(strcat(currDir,'/jcloudcv/lib/commons-pool-1.6.jar'))
% javaaddpath(strcat(currDir,'/jcloudcv/lib/httpcore-4.2.1.jar'))
% javaaddpath(strcat(currDir,'/jcloudcv/lib/httpmime-4.2.1.jar'))
% javaaddpath(strcat(currDir,'/jcloudcv/lib/json-20131018.jar'))
% javaaddpath(strcat(currDir,'/jcloudcv/lib/log4j-1.2.14.jar'))
% javaaddpath(strcat(currDir,'/jcloudcv/lib/slf4j-api-1.6.0.jar'))
% javaaddpath(strcat(currDir,'/jcloudcv/lib/slf4j-log4j12-1.6.0.jar'))
javaaddpath(strcat(currDir,'/jcloudcv/bin/jcloudcv.jar'))


import io.socket.SocketIO.*
import UploadData.*
import ConfigParser.*
import SocketConnection.*
import Sockets_CCV.*