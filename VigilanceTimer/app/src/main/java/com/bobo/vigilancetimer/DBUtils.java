package com.bobo.vigilancetimer;

import android.util.Log;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/************************************ Notice *****************************************
*  DO NOT connect database in this way, never, ever.                                  
*  Use a php script on the server to receive info that is sent from client,           
*  and do the miscellaneous job.                                                      
*                                                                                     
*  This class will not be runnable because the server IP address was removed.        
*  Set up a VPS and create a database to use this class. Though it is risky 
*  to leave the username and password in the client, which may harm your database.
*  (and of course, your data)
*  
*  If you insists, you need to create a table named 'TrialInfo' manually, in the
*  experiment we conducted, the creation of this table is something like:
*
    CREATE TABLE IF NOT EXISTS `TrialInfo`(
        `TrialName` CHAR(14) NOT NULL,
        `Duration` INT NOT NULL,
        `Gender` CHAR(1),
        `GroupSize` INT,
        `isFullRec` BOOLEAN,
        `isUseEarphone` BOOLEAN,
        `FoodType` VARCHAR(40),
        `ExtraInfo` VARCHAR(140),
        PRIMARY KEY ( `TrialName` )
    )ENGINE=InnoDB DEFAULT CHARSET=utf8;
*  
************************************ END *****************************************/

public class DBUtils {
    // Temp info assigned by UI actions
    public long trialStartInMs;
    public long trialEndInMs;
    public long vigilanceStartInMs;
    public long vigilanceEndInMs;
    public long phoneStartInMs;
    public long phoneEndInMs;
    public String focusDirection;
    // Trial Info
    public int groupSize;
    public boolean isFullRec;
    public boolean isUseEarphone;
    public String trialGender;
    public String foodType;
    public String extraInfo;
    // Private info
    private String trialTableName;      // Assigned in initTrial()
    // DB Info
    private static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
    private static final String DB_URL = "jdbc:mysql://SERVER_IP:3306/vigilance?useUnicode=yes&characterEncoding=utf8";
    private static final String user = "USERNAME";
    private static final String password = "USER_PWD";
    private static Connection getConn() {
        Connection connection = null;
        try {
            Class.forName(JDBC_DRIVER);
            connection = DriverManager.getConnection(DB_URL, user, password);
        }
        catch (Exception e){
            Log.e("SQL",Log.getStackTraceString(e));
        }
        return connection;
    }

    public void initTrial() {
        SimpleDateFormat timeStamp = new SimpleDateFormat("yyyyMMddHHmmss", Locale.CHINA);
        timeStamp.setTimeZone(TimeZone.getTimeZone("GMT+8"));
        Date date = new Date(trialStartInMs);
        trialTableName = timeStamp.format(date);
        try {
            Connection connectionInit = getConn();
            String createTrialTable = "CREATE TABLE IF NOT EXISTS `" + trialTableName +
                    "`(`ActivityName` VARCHAR(3),`StartTime` INT,`EndTime` INT,`MiscInfo` VARCHAR(30)," +
                    "PRIMARY KEY (`ActivityName`,`StartTime`))ENGINE=InnoDB DEFAULT CHARSET=utf8;";
            Statement newTrialTable = connectionInit.createStatement();
            newTrialTable.execute(createTrialTable);
            newTrialTable.close();
            connectionInit.close();
        } catch (Exception e) {
            Log.e("SQL",Log.getStackTraceString(e));
        }
    }

    public void insertVigilanceInfo() {
        int startTime = (int)(vigilanceStartInMs-trialStartInMs)/1000;
        int endTime = (int)(vigilanceEndInMs-trialStartInMs)/1000;
        try{
            Connection connectionVigilance = getConn();
            String insertActivity = "INSERT INTO `"+trialTableName+"` (ActivityName,StartTime,EndTime,MiscInfo) "+
                    "VALUES(?,?,?,?);";
            PreparedStatement ptmt = connectionVigilance.prepareStatement(insertActivity);
            ptmt.setString(1,"VIG");
            ptmt.setInt(2,startTime);
            ptmt.setInt(3,endTime);
            ptmt.setString(4,focusDirection);
            ptmt.execute();
            connectionVigilance.close();
        } catch (Exception e){
            Log.e("SQL",Log.getStackTraceString(e));
        }
        // Reset
        vigilanceStartInMs = vigilanceEndInMs = 0;
        focusDirection = "";
    }

    public void insertPhoneInfo() {
        int startTime = (int)(phoneStartInMs-trialStartInMs)/1000;
        int endTime = (int)(phoneEndInMs-trialStartInMs)/1000;
        try{
            Connection connectionPhone = getConn();
            String insertActivity = "INSERT INTO `"+trialTableName+"` (ActivityName,StartTime,EndTime,MiscInfo)"+
                    "values(?,?,?,?);";
            PreparedStatement ptmt = connectionPhone.prepareStatement(insertActivity);
            ptmt.setString(1,"PHO");
            ptmt.setInt(2,startTime);
            ptmt.setInt(3,endTime);
            ptmt.setString(4,"");
            ptmt.execute();
            connectionPhone.close();
        } catch (Exception e){
            Log.e("SQL",Log.getStackTraceString(e));
        }
        // Reset
        phoneStartInMs = phoneEndInMs = 0;
    }

    public void insertTrialInfo(){
        int duration = (int)(trialEndInMs-trialStartInMs)/1000;
        try{
            Connection connectionVigilance = getConn();
            String insertActivity = "INSERT INTO `TrialInfo` (TrialName,Duration,Gender,GroupSize,isFullRec,isUseEarphone,FoodType,ExtraInfo) "+
                    "VALUES(?,?,?,?,?,?,?,?);";
            PreparedStatement ptmt = connectionVigilance.prepareStatement(insertActivity);
            ptmt.setString(1,trialTableName);
            ptmt.setInt(2,duration);
            ptmt.setString(3,trialGender);
            ptmt.setInt(4,groupSize);
            ptmt.setBoolean(5,isFullRec);
            ptmt.setBoolean(6,isUseEarphone);
            ptmt.setString(7,foodType);
            ptmt.setString(8,extraInfo);
            ptmt.execute();
            connectionVigilance.close();
        } catch (Exception e){
            Log.e("SQL",Log.getStackTraceString(e));
        }
    }

    public void reset(){
        try {
            Connection connectionDrop = getConn();
            String createTrialTable = "DROP TABLE `" + trialTableName + "`;";
            Statement dropTrialTable = connectionDrop.createStatement();
            dropTrialTable.execute(createTrialTable);
            dropTrialTable.close();
            connectionDrop.close();
        } catch (Exception e) {
            Log.e("SQL",Log.getStackTraceString(e));
        }
    }
}
