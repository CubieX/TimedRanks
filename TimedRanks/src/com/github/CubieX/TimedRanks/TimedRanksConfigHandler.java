package com.github.CubieX.TimedRanks;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class TimedRanksConfigHandler
{
    private final TimedRanks plugin;
    private final Logger log;
    
    private FileConfiguration config;
    private FileConfiguration promoPlayersCfg = null;
    private File promotedPlayersConfigFile = null;
    private final String promotedPlayersFileName = "promotedPlayers.yml";

    //Constructor
    public TimedRanksConfigHandler(TimedRanks plugin, Logger log)
    {        
        this.plugin = plugin;
        this.log = log;

        initConfig();        
    }

    private void initConfig()
    {
        plugin.saveDefaultConfig(); //creates a copy of the provided config.yml in the plugins data folder, if it does not exist
        config = plugin.getConfig(); //re-reads config out of memory. (Reads the config from file only, when invoked the first time!)

        reloadPromotedPlayersConfig(); // load file from disk and create objects      
        savePromotedPlayersDefaultConfig(); // creates a copy of the provided promotedPlayers.yml
        promoPlayersCfg = getPromotedPlayersConfig(); // re-reads custom config from disk
    }

    private void saveConfig() //saves the config to disc (needed when entries have been altered via the plugin in-game)
    {
        // get and set values here!
        plugin.saveConfig();   
    }

    //reloads the config from disc (used if user made manual changes to the config.yml file)
    public void reloadConfig(CommandSender sender)
    {
        plugin.reloadConfig();
        config = plugin.getConfig(); // new assignment neccessary when returned value is assigned to a variable or static field(!)

        sender.sendMessage("[" + ChatColor.GREEN + "Info" + ChatColor.WHITE + "] " + ChatColor.GREEN + plugin.getDescription().getName() + " " + plugin.getDescription().getVersion() + " reloaded!");       
    }

    // =====================================
    // promotedPlayers config handling
    // =====================================

    // reload from disk
    public void reloadPromotedPlayersConfig()
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
    public FileConfiguration getPromotedPlayersConfig()
    {
        if (promoPlayersCfg == null)
        {
            this.reloadPromotedPlayersConfig();
        }
        return promoPlayersCfg;
    }

    //save config
    public void savePromotedPlayersConfig()
    {
        if (promoPlayersCfg == null || promotedPlayersConfigFile == null)
        {
            return;
        }
        try 
        {
            getPromotedPlayersConfig().save(promotedPlayersConfigFile);
        }
        catch (IOException ex)
        {
            log.log(Level.SEVERE, "Could not save config to " + promotedPlayersConfigFile, ex);
        }
    }

    // safe a default config if there is no file present
    public void savePromotedPlayersDefaultConfig()
    {
        if (!promotedPlayersConfigFile.exists())
        {            
           this.plugin.saveResource(promotedPlayersFileName, false);
        }
    }

}
