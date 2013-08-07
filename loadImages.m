function [output] = loadImages(dirname)
dirData = dir(dirname);
dirIndex = [dirData.isdir];
dirFiles={dirData(~dirIndex).name};
output=[];
for i=1:length(dirFiles)
    if(~isempty(regexpi(char(dirFiles(i)),'\w*.jpg','match')))
        output=[output, dirFiles(i)];
    end
end

        