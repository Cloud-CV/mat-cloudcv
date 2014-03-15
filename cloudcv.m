classdef cloudcv < handle
    %Class for connecting to CloudCV
    properties
        params;
        upload_obj;
        socket_obj;
    end
    
    methods(Access = private)
        function obj = startUpload(obj)
            obj.upload_obj=javaObject('UploadData', obj.params);
            t = javaObject('java.lang.Thread', obj.upload_obj);
            javaMethod('start', t);
           
        end
    end
    
    methods
        function obj = cloudcv()
            obj.params=NaN;
            obj.upload_obj=NaN;
        end
        
        function obj = init(obj, configFile, imageDir, resultDir, execName)
    
            if ~exist('imageDir','var') || isempty(imageDir)
                imageDir = '';
            end
            
            if ~exist('resultDir','var') || isempty(resultDir)
                resultDir = '';
            end
            
            if ~exist('execName','var') || isempty(execName)
                execName = '';
            end

            obj.params = javaObject('ConfigParser',configFile);
            javaMethod('readConfigFile', obj.params);
            val = javaMethod('parseArguments', obj.params, imageDir, resultDir, execName);
            
            if(val==1)
                javaMethod('getParams',obj.params);
            end
    
        end
    
        function obj = disconnect(obj)
            if(strcmp(class(obj.socket_obj),'SocketConnection'))
                javaMethod('socket_disconnect',obj.socket_obj);
                disp('Disconnecting Redis Server for Socket Connection');
            end
        end
        
        
        function obj = run(obj)
            obj = startUpload(obj);
            
            if(~strcmp(class(obj.socket_obj),'SocketConnection'))
                disp('Socket Server created');
                obj.socket_obj = javaObject('SocketConnection', obj.params.executable_name, obj.params.output_path);
                javaMethod('socketIOConnection',obj.socket_obj);
            
            else
                disp('Socket Connection is already established');
                javaMethod('updateParameters',obj.socket_obj, obj.params.executable_name, obj.params.output_path);
                javaMethod('socketIOConnection', obj.socket_obj);
            end
        end
        
    end
end



