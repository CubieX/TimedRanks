package com.github.CubieX.TimedRanks;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class TimedRanksFileLogger
{
   private final TimedRanks plugin;
   private File dataFolder = null;
   private File saveTo = null;
   private FileWriter fw = null;
   private PrintWriter pw = null;

   // constructor
   TimedRanksFileLogger(TimedRanks plugin)
   {
      this.plugin = plugin;

      initTransactionFileLogger();
   }

   void initTransactionFileLogger()
   {
      try
      {
         dataFolder = plugin.getDataFolder();

         if(!dataFolder.exists())
         {
            dataFolder.mkdir();
         }

         saveTo = new File(plugin.getDataFolder(), "transactionLog.txt");

         if (!saveTo.exists())
         {
            saveTo.createNewFile();
         }

         fw = new FileWriter(saveTo, true);
         pw = new PrintWriter(fw);

      }
      catch (IOException e)
      {
         e.printStackTrace();
      }
   }

   public void logTransaction(String message)
   {
      try
      {
         pw.println(message);
         pw.flush();
         pw.close();
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }
   }
}

