package com.github.CubieX.TimedRanks;

import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import ru.tehkode.permissions.PermissionUser;
import ru.tehkode.permissions.bukkit.PermissionsEx;

public class TimedRanksCommandHandler implements CommandExecutor
{
    private final TimedRanks plugin;
    private final TimedRanksConfigHandler cHandler;
    private final Logger log;

    public TimedRanksCommandHandler(TimedRanks plugin, Logger log, TimedRanksConfigHandler cHandler)
    {
        this.plugin = plugin;
        this.cHandler = cHandler;
        this.log = log;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
    {
        Player player = null;
        if (sender instanceof Player) 
        {
            player = (Player) sender;
        }

        if (cmd.getName().equalsIgnoreCase("tr"))
        { // If the player typed /tr then do the following... (can be run from console also)
            if (args.length == 0)
            { //no arguments, so help will be displayed
                return false;
            }
            if (args.length==1)
            {
                if (args[0].equalsIgnoreCase("version")) // show the current version of the plugin
                {            
                    sender.sendMessage(ChatColor.GREEN + "This server is running " + plugin.getDescription().getName() + " " + plugin.getDescription().getVersion());
                }    

                if (args[0].equalsIgnoreCase("reload")) // reload the plugins config and playerfile
                {            
                    if(sender.hasPermission("timedranks.admin"))
                    {
                        cHandler.reloadConfig(sender);                        
                    }
                    else
                    {
                        sender.sendMessage(ChatColor.RED + "You du not have sufficient permission to reload " + plugin.getDescription().getName() + "!");
                    }
                }

                if (args[0].equalsIgnoreCase("status")) // no name given, so show status of player that issued the command
                {            
                    if(sender.hasPermission("timedranks.status.own"))
                    {
                        // TODO show own status  

                    }
                    else
                    {
                        sender.sendMessage(ChatColor.RED + "You du not have sufficient permission to see your status!");
                    }
                }
                return true;
            }

            if (args.length==2)
            {
                if (args[0].equalsIgnoreCase("status")) // show the status of the given player
                {            
                    if(sender.hasPermission("timedranks.status.other"))
                    {
                        // TODO show status of given player  

                    }
                    else
                    {
                        sender.sendMessage(ChatColor.RED + "You du not have sufficient permission to see the status of " + plugin.getDescription().getName() + "!");
                    }
                }

                if (args[0].equalsIgnoreCase("pause")) // pause the promoted rank immediately until it gets manually resumed
                {            
                    if(sender.hasPermission("timedranks.manage"))
                    {
                        // TODO                        
                    }
                    else
                    {
                        sender.sendMessage(ChatColor.RED + "You du not have sufficient permission to manage ranks!");
                    }
                }                

                if (args[0].equalsIgnoreCase("resume")) // resume the promoted rank, adding the suspended time to the end timestamp
                {            
                    if(sender.hasPermission("timedranks.manage"))
                    {
                        // TODO                       
                    }
                    else
                    {
                        sender.sendMessage(ChatColor.RED + "You du not have sufficient permission to manage ranks!");
                    }                    
                }
            }

            if (args.length==3)
            {
                if (args[0].equalsIgnoreCase("sub") || args[0].equalsIgnoreCase("nimm")) // take away days for the promoted rank for the player
                {                   
                    if(sender.hasPermission("timedranks.manage"))
                    {
                        // TODO                            
                    }
                    else
                    {
                        sender.sendMessage(ChatColor.RED + "You du not have sufficient permission to manage ranks!");
                    }                   
                }
                return true;
            }

            if (args.length==4)
            {
                if (args[0].equalsIgnoreCase("add") || args[0].equalsIgnoreCase("gib")) // promote a player for the given time and rank
                {            
                    if(sender.hasPermission("timedranks.manage"))
                    {
                        // TODO    
                    }
                    else
                    {
                        sender.sendMessage(ChatColor.RED + "You du not have sufficient permission to manage ranks!");
                    }                    
                }
                return true;
            }
            else
            {
                sender.sendMessage(ChatColor.YELLOW + plugin.logPrefix + "Falsche Anzahl an Parametern.");
            }  
        }         
        return false; // No valid parameter count. If false is returned, the help for the command stated in the plugin.yml will be displayed to the player
    }
}
