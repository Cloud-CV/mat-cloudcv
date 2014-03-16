function visualize_detection(imdir,mat_file_dir, varargin)
imdir = char(imdir);
mat_file_dir = char(mat_file_dir);
imdir = strcat(imdir, '/');
mat_file_dir = strcat(mat_file_dir, '/');

imageNames={};
allowedExt={'png','gif','bmp','jpg','JPEG'};

count =1;
    for i=1:length(allowedExt)
        imagesStruct=dir(fullfile(imdir,strcat('*.',allowedExt{i})));
        for j=1:length(imagesStruct)
                imageNames{count}=imagesStruct(j).name;
                count=count+1;
        end
    end
    if(nargin<3)
	nTopBoxes=5;
    else
	nTopBoxes=varargin{1};
    end

%reuse count variable


	for i=1:length(imageNames)
		bboxes=[];
		scores=[];
		model_names=[];
		im_loc=[imdir imageNames{i}];
		%resize the image so that the max size is 500 pixels and aspect ratio is preserved
		im=imread(im_loc);
        %im=resizeImage(im);
		bbox_file_name=[mat_file_dir imageNames{i} '.mat'];
		load([bbox_file_name]);
        for j=1:length(bounding_boxes)
			model_name=bounding_boxes(j).model_name;
                        if(size(bounding_boxes(j).bboxes,1)~=0)
                            bboxes=[bboxes;[bounding_boxes(j).bboxes(:,1) bounding_boxes(j).bboxes(:,2) bounding_boxes(j).bboxes(:,3) bounding_boxes(j).bboxes(:,4)]];
                            scores=[scores;bounding_boxes(j).scores];
                            new_model_names=cell(length(bounding_boxes(j).scores),1);
                            for k=1:length(bounding_boxes(j).scores)
                             new_model_names{k}=model_name;
                            end
                            model_names=[model_names;new_model_names];				

                        end
        end
        
        if (length(scores)<nTopBoxes)
            nTopBoxes=length(scores);
        end
		[~,nTopValsInds]=sort(scores,'descend');
        label_class_names=cell(nTopBoxes,1);
        
        for jj=1:nTopBoxes
            model_names{nTopValsInds(jj)};
			label_class_names{jj}=[model_names{nTopValsInds(jj)}];
        end
        
        top_bboxes=bboxes(nTopValsInds(1:nTopBoxes),:);
        top_score_pos=[bboxes(nTopValsInds(1:nTopBoxes),1) bboxes(nTopValsInds(1:nTopBoxes),4)];
        top_label_pos=[bboxes(nTopValsInds(1:nTopBoxes),1) bboxes(nTopValsInds(1:nTopBoxes),2)];
        height=size(im,1);
        %imshow(im,'border','tight');
         %   axis off;
        for w=1:nTopBoxes
            label_score=['score: ' num2str(scores(nTopValsInds(w)),'%0.4f')];
            label_class=strrep(label_class_names{w},'_','\_');
            label_class=strrep(label_class,'imagenet','ImNet');
            im=writeOnImage(im,[top_score_pos(w,1)+height/100 top_score_pos(w,2)-4*height/100],label_score,[1,1,0]);
            im=writeOnImage(im,[top_label_pos(w,1)+height/100 top_label_pos(w,2)+4*height/100],label_class,[1,1,0]);
        end 
        figure
        imshow(im);
        hold on;
        for count =1:nTopBoxes
            DrawBoxes(top_bboxes(count,:));
        end
        hold off;
        pause;
        close;
	end
end

function [image]=resizeImage(image)

 max_size = 500;
 h = size(image,1)
 w = size(image,2)
 scale_f = min(max_size/h,max_size/w);
 if scale_f < 1
          image = imresize(image, scale_f);
 end
size(image)
end

