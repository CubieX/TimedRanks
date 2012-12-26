package com.github.CubieX.TimedRanks;

import java.util.List;
import java.util.logging.Logger;

import net.milkbowl.vault.permission.Permission;

import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TimedRanksCommandHandler implements CommandExecutor
{
   private final TimedRanks plugin;
   private final TimedRanksConfigHandler cHandler;
   private final Logger log;
   private final Permission perm;

   public TimedRanksCommandHandler(TimedRanks plugin, Logger log, TimedRanksConfigHandler cHandler, Permission perm)
   {
      this.plugin = plugin;
      this.cHandler = cHandler;
      this.log = log;
      this.perm = perm;
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

            if (args[0].equalsIgnoreCase("promote") &&
                  (null != player)) // TESTING! Promote issuing player to promoteGroup
            {   
               World nullWorld = null;
               String promoteGroup = getPromoteGroup(player);
               if(!perm.playerAddGroup(nullWorld, player.getName(), promoteGroup))
               {
                  sender.sendMessage("Promotion went wrong!");
               }
               if(!perm.playerRemoveGroup(nullWorld, player.getName(), perm.getPlayerGroups(player)[0])) //remove all groups except promoteGroup!
               {
                  sender.sendMessage("Promotion went wrong!");
               }
               else
               {
                  sender.sendMessage(ChatColor.GREEN + "You have been promoted from Group: " + perm.getPlayerGroups(player)[0] + " to Group: " + promoteGroup);
               }                                        
            }

            if (args[0].equalsIgnoreCase("demote") &&
                  (null != player)) // TEST!! demote issuing player to basegroup
            {      
               World nullWorld = null;
               String baseGroup = getBaseGroup(player);
               if(!perm.playerAddGroup(nullWorld, player.getName(), baseGroup))
               {
                  sender.sendMessage("Demotion went wrong!");
               }
               if(!perm.playerRemoveGroup(nullWorld, player.getName(), perm.getPlayerGroups(player)[0])) //remove all groups except promoteGroup!
               {
                  sender.sendMessage("Demotion went wrong!");
               }
               else
               {
                  sender.sendMessage(ChatColor.GREEN + "You have been demoted from Group: " + perm.getPlayerGroups(player)[0] + " to Group: " + baseGroup);    
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

   String getBaseGroup(Player player)
   {
      String baseGroup = "";

      // get count of BaseGroup entry to use for lookup of promoteGroup
      // this is only a quick tryout solution!!

      // das muss ins Config Init bzw. enable!
      List<String> baseGroupList = plugin.getConfig().getStringList("basegroups");
      List<String> promoteGroupList = plugin.getConfig().getStringList("promotegroups");

      for(int i = 0; i < promoteGroupList.size(); i++)
      {
         if(perm.playerInGroup(player, promoteGroupList.get(i)))
         {
            baseGroup = baseGroupList.get(i);
         }
      }        

      return (baseGroup);
   }

   String getPromoteGroup(Player player)
   {
      String promoteGroup = "";

      // get count of PromoteGroup entry to use for lookup of baseGroup
      // this is only a quick tryout solution!!

      // das muss ins Config Init bzw. enable!
      List<String> baseGroupList = plugin.getConfig().getStringList("basegroups");
      List<String> promoteGroupList = plugin.getConfig().getStringList("promotegroups");

      for(int i = 0; i < baseGroupList.size(); i++)
      {
         if(perm.playerInGroup(player, baseGroupList.get(i)))
         {
            promoteGroup = promoteGroupList.get(i);
         }
      }        

      return (promoteGroup);
   }
}
