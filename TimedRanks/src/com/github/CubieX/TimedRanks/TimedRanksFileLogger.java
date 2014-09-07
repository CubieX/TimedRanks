package com.github.CubieX.TimedRanks;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TimedRanksFileLogger
{
   private final TimedRanks plugin;
   private File dataFolder = null;
   private final String transactionLogFileName = "transactionLog.txt";
   private final String promotionStatusLogFileName = "promotionStatusLog.txt";
   private File transactionLogFile = null;
   private File promotionStatusLogFile = null;
   
   // constructor
   TimedRanksFileLogger(TimedRanks plugin)
   {
      this.plugin = plugin;
      this.dataFolder = plugin.getDataFolder();
   }

   public void logTransaction(String message)
   {
      try
      {
         String logTimeStamp = createLogTimeStamp();
         String msg = "[" + logTimeStamp + "] " + message;
         FileWriter fw;
         PrintWriter pw;
         
         dataFolder = plugin.getDataFolder();

         if(!dataFolder.exists())
         {
            dataFolder.mkdir();
         }

         transactionLogFile = new File(plugin.getDataFolder(), transactionLogFileName);

         if (!transactionLogFile.exists())
         {
            transactionLogFile.createNewFile();
         }

         fw = new FileWriter(transactionLogFile, true);
         pw = new PrintWriter(fw);
         
         pw.println(msg);
         pw.flush();
         pw.close(); // this disposes the stream object and saves the file
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }
   }
   
   /**
    * Log status changes of player promotions
    * */
   public void logPromotionStatusChange(String message)
   {
      try
      {
         String logTimeStamp = createLogTimeStamp();
         String msg = "[" + logTimeStamp + "] " + message;
         FileWriter fw;
         PrintWriter pw;
         dataFolder = plugin.getDataFolder();

         if(!dataFolder.exists())
         {
            dataFolder.mkdir();
         }

         promotionStatusLogFile = new File(plugin.getDataFolder(), promotionStatusLogFileName);

         if (!promotionStatusLogFile.exists())
         {
            promotionStatusLogFile.createNewFile();
         }

         fw = new FileWriter(promotionStatusLogFile, true);
         pw = new PrintWriter(fw);
         
         pw.println(msg);
         pw.flush();
         pw.close(); // this disposes the stream object and saves the file
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }
   }
   
   private String createLogTimeStamp()
   {
      // create current date in readable form
      long currTime = plugin.getCurrentTimeInMillis();
      final SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
      String logTime = sdf.format(new Date(currTime));
      
      return (logTime);
   }
}

