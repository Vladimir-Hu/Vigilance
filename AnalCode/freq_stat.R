library("RMySQL");
library("RColorBrewer")
library("ggplot2")
library("lubridate")

# Clear workspace before use (rbind is used)
conn = dbConnect(MySQL(),user='USERNAME',password='PASSWD',
                 dbname='vigilance',host='SERVER_IP');
trial.result = dbSendQuery(conn,"SELECT * FROM `TrialInfo` WHERE `isFullRec`=TRUE AND `ExtraInfo`!='test'");
trial.info = fetch(trial.result);
trial.names = trial.info$TrialName;
rownames(trial.info) = trial.info$TrialName;
trial.info$TrialName = NULL;
vig.info = data.frame(TrialName=character(0),Gender=character(0),vStart=POSIXct(0),vEnd=POSIXct(0),
                      tStart=POSIXct(0),tEnd=POSIXct(0),
                      stringsAsFactors=FALSE);
for (name in trial.names){
  sql.single = paste("SELECT `StartTime`,`EndTime` FROM `",name,"` WHERE ActivityName='VIG'",sep = "");
  single.result = dbSendQuery(conn,sql.single);
  single.info = fetch(single.result);
  sql.dura = paste("SELECT `Duration` FROM `TrialInfo` WHERE TrialName='",name,"'",sep = "");
  dura.result = dbSendQuery(conn,sql.dura);
  dura.info = fetch(dura.result);
  sql.sex = paste("SELECT `Gender` FROM `TrialInfo` WHERE TrialName='",name,"'",sep = "");
  sex.result = dbSendQuery(conn,sql.sex);
  sex.info = fetch(sex.result);
  if (nrow(single.info) > 0){
    for (i in 1:nrow(single.info)){
      start = single.info[i,1];
      end = single.info[i,2];
      if (start>0 && end>0){
        trialStrat = ymd_hms(name,tz="Asia/Shanghai");
        trialEnd = trialStrat+dura.info$Duration;
        trialGender = sex.info$Gender;
        vigStart = trialStrat+start;
        vigEnd = trialEnd+end;
        vig.info = rbind(vig.info,data.frame(TrialName=name,Gender=trialGender,vStart=vigStart,vEnd=vigEnd,
                                             tStart=trialStrat,tEnd=trialEnd),stringsAsFactors=FALSE);
      }
    }
  }
}

# Filter workday data (wd)
int.wd1 = interval(ymd(20201009,tz="Asia/Shanghai"),ymd(20201010,tz="Asia/Shanghai"));
int.wd2 = interval(ymd(20201012,tz="Asia/Shanghai"),ymd(20201014,tz="Asia/Shanghai"));
wd.info = rbind(vig.info[vig.info$tStart %within% int.wd1,],
                vig.info[vig.info$tStart %within% int.wd2,]);
wd.trialname = as.vector(unique(wd.info$TrialName));

lunch.hour = 12;
lunch.freq = data.frame(TrialName=character(0),Time=numeric(0),Freq=numeric(0),Gender=character(0));
for (name in wd.trialname){
  temp.info = wd.info[(wd.info$TrialName==name & hour(wd.info$vStart)==lunch.hour),];
  for (lunch.min in seq(0,30,10)){
    vigcount = nrow(temp.info[minute(round_date(temp.info$vStart,"10 mins"))==lunch.min,]);
    if (nrow(temp.info)>0){
      if (hour(temp.info$tEnd[1]) == lunch.hour && minute(temp.info$tEnd[1]) >= (lunch.min+10)){
        freq = vigcount/10;
      }
      else if (hour(temp.info$tEnd[1]) == lunch.hour && minute(temp.info$tEnd[1]) < (lunch.min+10)
               && minute(temp.info$tEnd[1]) > lunch.min){
        length = minute(temp.info$tEnd[1])-lunch.min;
        freq = vigcount/length;
      }
      else{
        freq = 0;
      }
      lunch.freq = rbind(lunch.freq,data.frame(TrialName=name,Time=lunch.min
                                               ,Freq=freq,Gender=temp.info$Gender[1]));
    }
  }
}

avg.lunch.freq = data.frame(Time=POSIXct(0),Freq=numeric(0));
for (i in seq(0,30,10)){
  avg.lunch.freq = rbind(avg.lunch.freq,data.frame(Class=i,Freq=mean(lunch.freq[lunch.freq$Class==i,'Freq'])));
}

ggplot(lunch.freq,aes(Time,Freq))+
  ylim(0,2)+
  geom_boxplot(aes(group=Time))+
  stat_summary(fun="mean",geom="line",color="red",size=1.25)+
  ggsave("Time.png",width = 8,height = 6);




