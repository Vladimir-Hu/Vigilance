library("RMySQL");
library("RColorBrewer")
library("ggplot2")

conn = dbConnect(MySQL(),user='USERNAME',password='PASSWD',
                 dbname='vigilance',host='SERVER_IP');
trial.result = dbSendQuery(conn,"SELECT * FROM `TrialInfo` WHERE `isFullRec`=TRUE AND `ExtraInfo`!='test' ");
trial.info = fetch(trial.result);
trial.names = trial.info$TrialName;
rownames(trial.info) = trial.info$TrialName;
trial.info$TrialName = NULL;
for (name in trial.names){
  sql.single = paste("SELECT `StartTime`,`EndTime` FROM `",name,"` WHERE ActivityName='VIG'",sep = "");
  single.result = dbSendQuery(conn,sql.single);
  single.info = fetch(single.result);
  sql.misc = paste("SELECT `Duration` FROM `TrialInfo` WHERE TrialName='",name,"'",sep = "");
  misc.result = dbSendQuery(conn,sql.misc);
  misc.info = fetch(misc.result);
  trial.info[name,'VigilanceCount'] = nrow(single.info);
  trial.info[name,'VigilanceTime'] = sum(single.info$EndTime)-sum(single.info$StartTime);
  trial.info[name,'VigilancePerMin'] = 60*nrow(single.info)/misc.info$Duration;
  trial.info[name,'NormedTime'] = (sum(single.info$EndTime)-sum(single.info$StartTime))/misc.info$Duration;
  trial.info[name,'TimePerVigilance'] = trial.info[name,'VigilanceTime']/trial.info[name,'VigilanceCount'];
}

# Data used to do hypo tests
male.time = trial.info[trial.info$Gender=='M','NormedTime'];
male.count = trial.info[trial.info$Gender=='M','VigilanceCount'];
male.avgtime = trial.info[trial.info$Gender=='M','TimePerVigilance'];
female.time = trial.info[trial.info$Gender=='F','NormedTime'];
female.count = trial.info[trial.info$Gender=='F','VigilanceCount'];
female.avgtime = trial.info[trial.info$Gender=='F','TimePerVigilance'];

single.time = trial.info[trial.info$GroupSize==1,'NormedTime'];
single.count = trial.info[trial.info$GroupSize==1,'VigilanceCount'];
single.avgtime = trial.info[trial.info$GroupSize==1,'TimePerVigilance'];
group.time = trial.info[trial.info$GroupSize>1,'NormedTime'];
group.count = trial.info[trial.info$GroupSize>1,'VigilanceCount'];
group.avgtime = trial.info[trial.info$GroupSize>1,'TimePerVigilance'];
trial.info$isAlone = trial.info$GroupSize==1

trial.info$isUseEarphone = trial.info$isUseEarphone==1
earphone.time = trial.info[trial.info$isUseEarphone==TRUE,'NormedTime'];
earphone.count = trial.info[trial.info$isUseEarphone==TRUE,'VigilanceCount'];
earphone.avgtime = trial.info[trial.info$isUseEarphone==TRUE,'TimePerVigilance'];
noEarphone.time = trial.info[trial.info$isUseEarphone==FALSE,'NormedTime'];
noEarphone.count = trial.info[trial.info$isUseEarphone==FALSE,'VigilanceCount'];
noEarphone.avgtime = trial.info[trial.info$isUseEarphone==FALSE,'TimePerVigilance'];

# Hypo tests (Simple demo)
wilcox.test(male.time,female.time)
wilcox.test(male.count,female.count)
# Each properties should done following 3 tests
shapiro.test(male.avgtime)
shapiro.test(female.avgtime)
ks.test(male.avgtime,female.avgtime)
wilcox.test(male.avgtime,female.avgtime)


# Plot TimePerVigilance
mean.Male.TimePerVigilance = mean(male.avgtime,na.rm = TRUE);
mean.Female.TimePerVigilance = mean(female.avgtime,na.rm = TRUE);
ggplot(trial.info,aes(TimePerVigilance,fill = Gender,color = Gender))+
  geom_density(alpha=0.25)+
  geom_vline(aes(color = 'M',xintercept = mean.Male.TimePerVigilance),linetype="dashed")+
  geom_vline(aes(color= 'F',xintercept = mean.Female.TimePerVigilance),linetype="dashed")+
  scale_fill_brewer(palette="Set1")+
  scale_color_brewer(palette = "Set1")+
  xlab("Average Length of Each Vigilance")+
  ylab("Density")+
  ggsave(filename = "AvgLength.png",width = 8,height = 6);

# Plot VigilanceCount
mean.Male.VigilanceCount = mean(male.count,na.rm = TRUE);
mean.Female.VigilanceCount = mean(female.count,na.rm = TRUE);
ggplot(trial.info,aes(VigilanceCount,fill = Gender,color = Gender))+
  geom_density(alpha=0.25)+
  geom_vline(aes(color = 'M',xintercept = mean.Male.VigilanceCount),linetype="dashed")+
  geom_vline(aes(color= 'F',xintercept = mean.Female.VigilanceCount),linetype="dashed")+
  scale_fill_brewer(palette="Set1")+
  scale_color_brewer(palette = "Set1")+
  xlab("Average Vigilance Count")+
  ylab("Density")+
  ggsave(filename = "AvgCount.png",width = 8,height = 6);

# Plot VigilanceNormedTime
mean.Male.NormedTime = mean(male.time,na.rm = TRUE);
mean.Female.NormedTime = mean(female.time,na.rm = TRUE);
ggplot(trial.info,aes(NormedTime,fill = Gender,color = Gender))+
  geom_density(alpha=0.25)+
  geom_vline(aes(color='M',xintercept=mean.Male.NormedTime),linetype="dashed")+
  geom_vline(aes(color='F',xintercept=mean.Female.NormedTime),linetype="dashed")+
  scale_fill_brewer(palette="Set1")+
  scale_color_brewer(palette="Set1")+
  xlab("Ratio of Vigilance Time")+
  ylab("Density")+
  ggsave(filename = "AvgRatio.png",width = 8,height = 6);

# Plot of GroupSize
mean.Single.VigilanceCount = mean(single.count,na.rm = TRUE);
mean.Group.VigilanceCount = mean(group.count,na.rm = TRUE);
ggplot(trial.info,aes(VigilanceCount,group = isAlone,fill = isAlone,color = isAlone))+
  geom_density(alpha=0.25)+
  geom_vline(aes(color = 'TRUE',xintercept = mean.Single.VigilanceCount),linetype="dashed")+
  geom_vline(aes(color= 'FALSE',xintercept = mean.Group.VigilanceCount),linetype="dashed")+
  scale_fill_brewer(palette="Set1")+
  scale_color_brewer(palette="Set1")+
  xlab("Average Vigilance Count")+
  ylab("Density");
  ggsave(filename = "GroupAvgCount.png",width = 8,height = 6);
