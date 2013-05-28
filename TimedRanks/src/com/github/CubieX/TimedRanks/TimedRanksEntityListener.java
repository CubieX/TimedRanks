package com.github.CubieX.TimedRanks;

import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class TimedRanksEntityListener implements Listener
{
   private final TimedRanks plugin; 
   private final Permission perm;

   //Constructor
   public TimedRanksEntityListener(TimedRanks plugin, Permission perm)
   {
      this.plugin = plugin;
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
            { // TODO Statt beim Login das ganze Zyklisch abpruefen alle paar Stunden? Damit die Liste immer aktuell ist?
               // checks if player is currently promoted via TR and if his promotion time has expired. If yes, he will be demoted.
               // this should always be called BEFORE calling checkPlayerPaymentStatus().
               checkPlayerPromotionStatus(joinedPlayer.getName());

               // checks if the player is in a payed group and if his next payment is due. If yes, he will be payed and the
               // next payment time will be scheduled.
               checkPlayerPaymentStatus(joinedPlayer.getName());

               // checks if a player who is managed by TR is currently in the permission group matching his base- or promoteGroup
               // if not, he has been set to another group by using the permission system before demoting or deleting him from the promotionList.
               // If this is the case, he and all OPs will be informed every time he loggs in, because
               // his group should be changed to his promoteGroup (if promotion is active), baseGroup (if promotion is paused) -> shown by /vip status PLAYER
               // or he should be demoted or deleted from the list
               checkPlayerGroupStatus(joinedPlayer);
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
                           // TODO hier die Messages aus der Config einfuegen!  Auch bei den Fehlermeldungen!                      
                           plugin.getServer().getPlayer(playerName).sendMessage(ChatColor.WHITE + TimedRanks.logPrefix + "Du wurdest vom " + ChatColor.RED + promoteGroup + ChatColor.WHITE + " zum " + ChatColor.GREEN + baseGroup + ChatColor.WHITE + " zurueckgestuft.");
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

   public void checkPlayerGroupStatus(Player joinedPlayer)
   {      
      if(!plugin.checkPlayerGroupStatus(joinedPlayer))
      {
         joinedPlayer.sendMessage(ChatColor.YELLOW + TimedRanks.logPrefix + "Du bist in einen hoeheren Rang ernannt, aber momentan nicht in der dazu passenden Gruppe.\n" + 
               ChatColor.YELLOW + "Bitte melde das einem Admin! (z.B. per Ticket oder im Forum)");

         for(Player operator : plugin.getServer().getOnlinePlayers())
         {
            if(operator.isOp())
            {
               operator.sendMessage(ChatColor.YELLOW + TimedRanks.logPrefix + joinedPlayer.getName() + " ist gemanaged ueber " + plugin.getDescription().getName() + ", aber ist momentan nicht in der dazu passenden Gruppe!");                              
            }
         }

         for(OfflinePlayer offlineOperator : plugin.getServer().getOperators())
         {
            if(offlineOperator.isOp())
            {
               // send all OPs a mail if Essentials plugin is present
               if(null != plugin.getServer().getPluginManager().getPlugin("Essentials"))
               {
                  plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), "mail send " + offlineOperator.getName() + " AUTOMESSAGE [TimedRanks]: " + joinedPlayer.getName() + " ist ueber TR gemanaged, aber momentan in einer falschen Gruppe. Bitte ueberpruefen!");
               }
            }
         }

         TimedRanks.log.info(TimedRanks.logPrefix + joinedPlayer.getName() + " is managed via " + plugin.getDescription().getName() + " but is currently NOT in the matching permission group!");
      }
   }
}
