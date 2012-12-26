package com.github.CubieX.TimedRanks;

import java.util.List;
import java.util.logging.Logger;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;

import org.bukkit.configuration.Configuration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class TimedRanksEntityListener implements Listener
{
   private final TimedRanks plugin;
   private final Logger log;
   private final Economy econ; 
   private final Permission perm;

   //Constructor
   public TimedRanksEntityListener(TimedRanks plugin, Logger log, Economy econ, Permission perm)
   {
      this.plugin = plugin;
      this.log = log;
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

   }


}
