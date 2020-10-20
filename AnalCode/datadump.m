% Get data from online database
conn = database('vigilance','USERNAME','PASSWD','com.mysql.jdbc.Driver','jdbc:mysql://SERVER_IP:3306/vigilance');
cursor = exec(conn,"SELECT * FROM `TrialInfo` WHERE `isFullRec`=TRUE AND `ExtraInfo`!='test'");
cursor = fetch(cursor);
info = cursor.Data;
[trialCount,~] = size(info);
infoTable = cell2table(info,'VariableNames',{'TrialName' 'Duration' 'Gender' 'GroupSize' 'isFullRec' 'isUseEarphone' 'FoodType' 'ExtraInfo'});
writetable(infoTable,'TrialInfo.csv');
% Dump Individual Data
for i=1:trialCount
    sql = strcat("SELECT * FROM `",info{i,1},"`");
    cursorSingle = exec(conn,sql);
    cursorSingle = fetch(cursorSingle);
    singleInfo = cursorSingle.Data;
    [eventCount,~] = size(singleInfo);
    filename = strcat(info{i,1},".csv");
    if eventCount > 1
        singleTable = cell2table(singleInfo,'VariableNames',{'ActivityName' 'StartTime' 'EndTime' 'MiscInfo'});
        writetable(singleTable,filename);
    else
        fp=fopen(filename,'a');
    end
end