function [ outputImage ] = writeOnImage( inputImage, position, label, color )
% Takes an RGB image and writes text on top of it
%   Detailed explanation goes here

width = size(inputImage,2);
height = size(inputImage,1);
f = figure('position',[ 0, 0, width, height],'visible','off');
background = zeros(height,width);


imshow(inputImage,'border','tight');
axis off;
%position = position ./ [ width height];
text('Position',position,'color',color,'String',label,'FontSize',height/20);

set(f, 'PaperPositionMode', 'auto');
textData = hardcopy(f, '-Dzbuffer', '-r0');
close(f);

textDouble = rgb2gray(textData);
indices = (textDouble~=0);
indices = repmat(indices,[1 1 3]);
outputImage = inputImage;
outputImage(indices)=textData(indices);

end