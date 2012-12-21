package com.github.CubieX.TimedRanks;

import java.util.logging.Logger;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import net.milkbowl.vault.permission.Permission;

import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class TimedRanks extends JavaPlugin
{
    private TimedRanksConfigHandler cHandler = null;
    private TimedRanksEntityListener eListener = null;
    private TimedRanksCommandHandler comHandler = null;

    private TimedRanks plugin;
    private static final Logger log = Logger.getLogger("Minecraft");
    static final String logPrefix = "[TimedRanks] "; // Prefix to go in front of all log entries
    static Economy econ = null;
    static Permission perm = null;
    
    //************************************************
    static String usedConfigVersion = "1"; // Update this every time the config file version changes, so the plugin knows, if there is a suiting config present
    //************************************************
    
    @Override
    public void onEnable()
    {     
        this.plugin = this; 
        
        cHandler = new TimedRanksConfigHandler(this, log);
        
        if(!checkConfigFileVersion())
        {
            log.severe(logPrefix + "Outdated or corrupted config file. Please delete your current config file, so " + getDescription().getName() + " can create a new one!");
            log.severe(logPrefix + "will be disabled now. Config file is outdated or corrupted.");
            disablePlugin();
            return;
        }        
                
        if (!hookToPermissionSystem())
        {
            log.info("logPrefix - Disabled due to no superperms compatible permission system found!");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        
        if (!setupPermissions())
        {
            log.info("logPrefix - Disabled because could not hook Vault to permission system!");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        if (!setupEconomy())
        {
            log.info("logPrefix - Disabled because could not hook Vault to economy system!");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        
        log.info(getDescription().getName() + " version " + getDescription().getVersion() + " is enabled!");
               
        comHandler = new TimedRanksCommandHandler(this, log, cHandler, perm);
        getCommand("tr").setExecutor(comHandler);
        eListener = new TimedRanksEntityListener(this,log, econ, perm);  
    }

    private boolean checkConfigFileVersion()
    {
        boolean res = false;

        if(this.getConfig().isSet("config_version"))
        {
            String configVersion = this.getConfig().getString("config_version");

            if(configVersion.equals(usedConfigVersion))
            {
                res = true;
            }  
        }

        return (res);
    }
    
    private boolean hookToPermissionSystem()
    {
        if ((getServer().getPluginManager().getPlugin("PermissionsEx") == null) &&
        (getServer().getPluginManager().getPlugin("bPermissions") == null) &&
        (getServer().getPluginManager().getPlugin("zPermissions") == null) &&
        (getServer().getPluginManager().getPlugin("PermissionsBukkit") == null))
        {
            return false;
        }
        else
        {
            return true;
        }
    }

    private boolean setupEconomy() 
    {
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp != null)
        {
            econ = rsp.getProvider();
        }
        
        return (econ != null);
    }
    
    private boolean setupPermissions()
    {  
        RegisteredServiceProvider<Permission> permissionProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.permission.Permission.class);
        if (permissionProvider != null)
        {
            perm = permissionProvider.getProvider();
        }
        return (perm != null);
    }

    void disablePlugin()
    {
        getServer().getPluginManager().disablePlugin(this);        
    }
    
    //TODO löschen von ganzen keys mit Unterpunkten: plugin.getConfig().getConfigurationSection("Warps").set(args[0], null) + dann config saven!
    @Override
    public void onDisable()
    {       
        cHandler = null;       
        comHandler = null;
        eListener = null;
        econ = null;
        perm = null;
        log.info(getDescription().getName() + " version " + getDescription().getVersion() + " is disabled!");
    }   
}
