function DrawBoxes( bounding_boxes )

for i=1:size(bounding_boxes,1)

    point1x=bounding_boxes(i,1);
    point1y=bounding_boxes(i,2);
    
    point2x=bounding_boxes(i,3);
    point2y=bounding_boxes(i,2);
    
    point3x=bounding_boxes(i,3);
    point3y=bounding_boxes(i,4);
    
    point4x=bounding_boxes(i,1);
    point4y=bounding_boxes(i,4);
    
    x=[point1x point2x point3x point4x point1x];
    y=[point1y point2y point3y point4y point1y];    

    plot(x,y,'Color','y');
    
end

