package com.github.CubieX.TimedRanks;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class TimedRanksEntityListener implements Listener
{
   private final TimedRanks plugin;
   private final Economy econ; 
   private final Permission perm;

   //Constructor
   public TimedRanksEntityListener(TimedRanks plugin, Economy econ, Permission perm)
   {
      this.plugin = plugin;
      this.econ = econ;
      this.perm = perm;

      plugin.getServer().getPluginManager().registerEvents(this, plugin);
   }
   /*
   Event Priorities

There are six priorities in Bukkit

    EventPriority.HIGHEST
    EventPriority.HIGH
    EventPriority.NORMAL
    EventPriority.LOW
    EventPriority.LOWEST
    EventPriority.MONITOR 

They are called in the following order

    EventPriority.LOWEST 
    EventPriority.LOW
    EventPriority.NORMAL
    EventPriority.HIGH
    EventPriority.HIGHEST
    EventPriority.MONITOR 

    All Events can be cancelled. Plugins with a high prio for the event can cancel or uncancel earlier issued lower prio plugin actions.
    MONITOR level should only be used, if the outcome of an event is NOT altered from this plugin and if you want to have the final state of the event.
    If the outcome gets changed (i.e. event gets cancelled, uncancelled or actions taken that can lead to it), a prio from LOWEST to HIGHEST must be used!

    The option "ignoreCancelled" if set to "true" says, that the plugin will not get this event if it has been cancelled beforehand from another plugin.
    */

   //================================================================================================
   @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true) // event has MONITOR priority and will be skipped if it has been cancelled before
   public void onPlayerJoin(PlayerJoinEvent event)
   {
      final Player joinedPlayer = event.getPlayer();

      try
      {
         Bukkit.getScheduler().runTaskLater(plugin, new Runnable()
         {
            public void run()
            { // TODO Statt beim Login das ganze Zyklisch abprüfen alle paar Stunden? Damit die Liste immer aktuell ist?
               // checks if player is currently promoted via TR and if his promotion time has expired. If yes, he will be demoted.
               // this should always be called BEFORE calling checkPlayerPaymentStatus().
               checkPlayerPromotionStatus(joinedPlayer.getName());
               // checks if the player is in a payed group and if his next payment is due. If yes, he will be payed and the
               // next payment time will be scheduled.
               checkPlayerPaymentStatus(joinedPlayer.getName());
            }
         }, 20*5L); // 5 second delay to give BukkitPermissions time to register joined players permissions         
      }
      catch (Exception ex)
      {
         // player is probably no longer online
      }
   }

   //=======================================================================================================
   // 

   public void checkPlayerPromotionStatus(String playerName)
   {
      if(null != playerName)
      {
         if(plugin.playerIsDemotable(playerName))
         {
            if(plugin.promotionIsActive(playerName))
            {
               // Perform time check
               if(plugin.promotionTimeIsUp(playerName))
               {
                  // time is up. So demote player.
                  World nullWorld = null;
                  String baseGroup = plugin.getBaseGroup(playerName);
                  String promoteGroup = plugin.getPromoteGroup(playerName);

                  if((null != baseGroup) &&
                        (null != promoteGroup)) // given player is valid and demotable and his groups are found
                  {
                     if((perm.playerAddGroup(nullWorld, playerName, baseGroup)) && // add player to baseGroup
                           (perm.playerRemoveGroup(nullWorld, playerName, promoteGroup))) //remove player from current promoteGroup
                     {
                        plugin.deletePlayerFromPromotionList(playerName);
                        TimedRanks.log.info(TimedRanks.logPrefix + playerName + "'s promotion period has expired. Player was demoted to rank: " + baseGroup + ".");

                        if(plugin.getServer().getPlayer(playerName).isOnline())
                        {
                           // TODO hier die Messages aus der Config einfügen!  Auch bei den Fehlermeldungen!                      
                           plugin.getServer().getPlayer(playerName).sendMessage(TimedRanks.logPrefix + "Du wurdest vom " + ChatColor.RED + promoteGroup + ChatColor.GRAY + " zum " + ChatColor.GREEN + baseGroup + ChatColor.GRAY + " zurueckgestuft.");
                        }
                     }
                     else
                     {
                        TimedRanks.log.severe(TimedRanks.logPrefix + "Error on demoting " + playerName + "!");
                     }
                  }
                  else
                  {
                     TimedRanks.log.severe(TimedRanks.logPrefix + ChatColor.YELLOW + "Error in cofing file! Group assignment was nor recognizable.");
                  }
               }
            }
         }         
      }
      else
      {
         // something went wrong
      }
   }

   public void checkPlayerPaymentStatus(String playerName)
   {
      if (null != playerName)
      {
         if(plugin.playerIsInPayedGroup(playerName))
         {
            if(plugin.promotionIsActive(playerName))
            {
               if(plugin.nextPaymentIsDue(playerName))
               {
                  if(plugin.payPlayer(playerName))
                  {
                     if(plugin.scheduleNextPayment(playerName, plugin.getPromoteGroup(playerName)))
                     {
                        // everything went OK. Error handling is inside the called methods.
                     }
                  }
               }
            }
         }
      }
   }
}
