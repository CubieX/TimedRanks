package com.github.CubieX.TimedRanks;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class TimedRanksConfigHandler
{
   private final TimedRanks plugin;
   private FileConfiguration promoPlayersCfg = null;
   private File promotedPlayersConfigFile = null;
   private final String promotedPlayersFileName = "promotedPlayers.yml";

   //Constructor
   public TimedRanksConfigHandler(TimedRanks plugin)
   {        
      this.plugin = plugin;

      initConfig();
   }

   private void initConfig()
   {
      // plugin config
      plugin.saveDefaultConfig(); //creates a copy of the provided config.yml in the plugins data folder, if it does not exist
      plugin.getConfig(); //re-reads config out of memory. (Reads the config from file only, when invoked the first time!)

      // promoted players list
      reloadPromotedPlayersFile(); // load file from disk and create objects      
      savePromotedPlayersDefaultFile(); // creates a copy of the provided promotedPlayers.yml
      promoPlayersCfg = getPromotedPlayersFile(); // re-reads promotedPlayers file from mem or disk 
   }

   //reloads the config from disc (used if user made manual changes to the config.yml file)
   public void reloadConfig(CommandSender sender)
   {
      plugin.reloadConfig();
      plugin.getConfig(); // new assignment necessary when returned value is assigned to a variable or static field(!)
      
      reloadPromotedPlayersFile();
      
      plugin.readConfigValues();

      sender.sendMessage(ChatColor.GREEN + plugin.getDescription().getName() + " " + plugin.getDescription().getVersion() + " reloaded!");       
   }

   // =====================================
   // promotedPlayers config handling
   // =====================================

   // reload from disk
   public void reloadPromotedPlayersFile()
   {
      if (promotedPlayersConfigFile == null)
      {
         promotedPlayersConfigFile = new File(plugin.getDataFolder(), promotedPlayersFileName);
      }
      promoPlayersCfg = YamlConfiguration.loadConfiguration(promotedPlayersConfigFile);

      // Look for defaults in the jar
      InputStream defConfigStream = plugin.getResource(promotedPlayersFileName);
      if (defConfigStream != null)
      {
         YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
         promoPlayersCfg.setDefaults(defConfig);
      }
   }

   // reload config and return it
   public FileConfiguration getPromotedPlayersFile()
   {
      if (promoPlayersCfg == null)
      {
         this.reloadPromotedPlayersFile();
      }
      return promoPlayersCfg;
   }

   //save config
   public void savePromotedPlayersFile()
   {
      if (promoPlayersCfg == null || promotedPlayersConfigFile == null)
      {
         return;
      }
      try 
      {
         getPromotedPlayersFile().save(promotedPlayersConfigFile);
      }
      catch (IOException ex)
      {
         TimedRanks.log.severe("Could not save config to " + promotedPlayersConfigFile);
         TimedRanks.log.severe(ex.getMessage());
      }
   }

   // safe a default config if there is no file present
   public void savePromotedPlayersDefaultFile()
   {
      if (!promotedPlayersConfigFile.exists())
      {            
         plugin.saveResource(promotedPlayersFileName, false);
      }
   }   
}
