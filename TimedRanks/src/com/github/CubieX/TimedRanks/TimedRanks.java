package com.github.CubieX.TimedRanks;

import java.util.logging.Logger;
import org.bukkit.plugin.java.JavaPlugin;

public class TimedRanks extends JavaPlugin
{
    private TimedRanksConfigHandler cHandler = null;
    private TimedRanksEntityListener eListener = null;
    private TimedRanksCommandHandler comHandler = null;

    private TimedRanks plugin;
    private static final Logger log = Logger.getLogger("Minecraft");
    public static String logPrefix = "[TimedRanks] "; // Prefix to go in front of all log entries

    @Override
    public void onEnable()
    {     
        this.plugin = this;       

        if (!hookToPermissionSystem() ) {
            log.info(String.format("[%s] - Disabled due to no permission system found!", getDescription().getName()));
            getServer().getPluginManager().disablePlugin(this);
            return;
        }       
        log.info(getDescription().getName() + " version " + getDescription().getVersion() + " is enabled!");

        cHandler = new TimedRanksConfigHandler(this, log);       
        comHandler = new TimedRanksCommandHandler(this, log, cHandler);
        getCommand("tr").setExecutor(comHandler);
        eListener = new TimedRanksEntityListener(this,log);        
    }

    private boolean hookToPermissionSystem() 
    {
        if (getServer().getPluginManager().getPlugin("PermissionsEx") == null)
        {
            return false;
        }
        else
        {
            return true;
        }
    }
    //TODO löschen von ganzen keys mit Unterpunkten: plugin.getConfig().getConfigurationSection("Warps").set(args[0], null) + dann config saven!
    @Override
    public void onDisable()
    {       
        cHandler = null;       
        comHandler = null;
        log.info(getDescription().getName() + " version " + getDescription().getVersion() + " is disabled!");
    }   
}
