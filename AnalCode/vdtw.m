% Get data from online database
conn = database('vigilance','USERNAME','PASSWD','com.mysql.jdbc.Driver','jdbc:mysql://SERVER_IP:3306/vigilance');
cursor = exec(conn,"SELECT * FROM `TrialInfo` WHERE `isFullRec`=TRUE AND `ExtraInfo`!='test'");
cursor = fetch(cursor);
info = cursor.Data;
[trialCount,~] = size(info);
tsArray = cell(1,trialCount);
isUsePhone = zeros(trialCount,1);
isVigilance = zeros(trialCount,1);
infoTable = cell2table(info,'VariableNames',{'TrialName' 'Duration' 'Gender' 'GroupSize' 'isFullRec' 'isUseEarphone' 'FoodType' 'ExtraInfo'});
% Original Data used for DTW Analysis
for i=1:trialCount
    sql = strcat("SELECT `StartTime`,`EndTime` FROM `",info{i,1},"` WHERE ActivityName='VIG'");
    cursorSingle = exec(conn,sql);
    cursorSingle = fetch(cursorSingle);
    singleInfo = cursorSingle.Data;
    singleTS = zeros(1,info{i,2});                  % Time series of a single trial
    [rowsingle,colsingle] = size(singleInfo);
    if (rowsingle ~= 1 && colsingle ~= 1)           % Trial contains VIG
        isVigilance(i,1) = true;
        for j=1:rowsingle
            singleTS(singleInfo{j,1}:singleInfo{j,2}) = 1;
        end
    else
        isVigilance(i,1) = false;
    end
    tsArray{1,i} = singleTS;
    % Phone Use Stat
    sqlP = strcat("SELECT `StartTime`,`EndTime` FROM `",info{i,1},"` WHERE ActivityName='PHO'");
    cursorSinglePhone = exec(conn,sqlP);
    cursorSinglePhone = fetch(cursorSinglePhone);
    singlePhoneInfo = cursorSinglePhone.Data;
    [phoneRow,phoneCol] = size(singlePhoneInfo);
    if(phoneRow >= 0 && phoneCol==2)
        isUsePhone(i,1) = true;
    else
        isUsePhone(i,1) = false;
    end
end
infoTable.isVigilance = isVigilance;
infoTable.isUsePhone = isUsePhone;
[rowcell,colcell] = size(tsArray);
dist = zeros(colcell);
for i=1:colcell
    for j=1:colcell
        dist(i,j) = dtw(tsArray{1,i},tsArray{1,j});
    end
end

% To choose which cluster method matches
z1=linkage(dist);
z2=linkage(dist,'complete');
z3=linkage(dist,'average');
z4=linkage(dist,'centroid');
z5=linkage(dist,'ward');
R=[cophenet(z1,dist),cophenet(z2,dist),cophenet(z3,dist),cophenet(z4,dist),cophenet(z5,dist)];
disp(R);
z = z2;
% To choose how many clusters should be devided
clusterNum = 7;
tabulate(cluster(z,'MaxClust',clusterNum))
clustResult = cluster(z2,clusterNum);
infoTable.ClusterInfo = clustResult;

% Plot Dendrogram
leafNum = 24;
leaforder = optimalleaforder(z,dist);
figure(1);
[~,T,denOrder] = dendrogram(z,leafNum,'Reorder',leaforder,'Orientation','left','ColorThreshold',0.5*max(z2(:,3)));
stdTS = zeros(leafNum,100);
stdLabel = zeros(1,leafNum);
for n = flip(denOrder)
    repSet = find(T==n);
    rep = repSet(randi(numel(repSet),1));
    tsLength = length(tsArray{rep});
    scale = tsLength/100;
    stdLabel(n) = clustResult(rep);
    for m = 1:tsLength
        if(tsArray{rep}(m) == 1)
            corr = ceil(m/scale);
            stdTS(n,corr) = stdTS(n,corr) + 1;
        end
    end
end
figure(2);
heatmap(stdTS);

for i=1:clusterNum
    figure(i+2);
    res = find(clustResult==i);
    for j = 1:min(6,length(res))
        subplot(6,1,j);
        plot(tsArray{res(j)});
    end
end

writetable(infoTable,'info.csv','Encoding','UTF-8');