package com.github.CubieX.TimedRanks;

import net.milkbowl.vault.permission.Permission;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TimedRanksCommandHandler implements CommandExecutor
{
   private final TimedRanks plugin;
   private final TimedRanksConfigHandler cHandler;
   private final Permission perm;

   public TimedRanksCommandHandler(TimedRanks plugin, TimedRanksConfigHandler cHandler, Permission perm)
   {
      this.plugin = plugin;
      this.cHandler = cHandler;
      this.perm = perm;
   }

   @Override
   public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
   {
      Player sendingPlayer = null;

      if (sender instanceof Player) 
      {
         sendingPlayer = (Player) sender;
      }

      if (cmd.getName().equalsIgnoreCase("tr"))
      { // If the player typed /tr then do the following... (can be run from console also)
         if (args.length == 0)
         { //no arguments, so help will be displayed
            return false;
         }

         if (args.length == 1)
         {
            // SHOW PLUGIN VERSION
            if (args[0].equalsIgnoreCase("version")) // show the current version of the plugin
            {            
               sender.sendMessage(ChatColor.GREEN + "This server is running " + plugin.getDescription().getName() + " " + plugin.getDescription().getVersion());
               return true;
            }    

            // RELOAD CONFIG
            if (args[0].equalsIgnoreCase("reload")) // reload the plugins config and playerfile
            {            
               if(sender.hasPermission("timedranks.admin"))
               {
                  cHandler.reloadConfig(sender);                        
               }
               else
               {
                  sender.sendMessage(ChatColor.RED + "Du hast keine berechtigung um " + plugin.getDescription().getName() + " neu zu laden!");
               }
               return true;
            }

            // SHOW OWN PROMOTION STATUS
            if (args[0].equalsIgnoreCase("status")) // no name given, so show status of player that issued the command
            { 
               if(sender.hasPermission("timedranks.status.own"))
               {
                  if(sender instanceof Player)
                  {
                     if(plugin.playerIsOnPromotionList(sender.getName()))
                     {
                        if(plugin.promotionIsActive(sender.getName()))
                        {
                           String promoteGroup = plugin.getPromoteGroup(sender.getName());
                           sender.sendMessage(ChatColor.WHITE + "Deine Ernennung zum " + ChatColor.GREEN + promoteGroup + ChatColor.WHITE + " endet in " + ChatColor.GREEN + plugin.getPromotionEndTime(sender.getName()) + ChatColor.WHITE + " Tagen.");
                        }
                        else
                        {
                           sender.sendMessage(ChatColor.WHITE + "Deine Ernennung ist zur Zeit pausiert.");
                        }
                     }
                     else
                     {
                        sender.sendMessage(ChatColor.WHITE + "Du bist momentan nicht in einen hoeheren Rang ernannt.");
                     }
                  }
                  else
                  {
                     sender.sendMessage(TimedRanks.logPrefix + "This command can only be used by a player.");
                  }                  
               }
               else
               {
                  sender.sendMessage(ChatColor.RED + "Du hast keine Berechtigung um deinen Status abzufragen!");
               }

               return true;
            }
         }

         if (args.length == 2)
         {
            // SHOW OTHER PLAYERS PROMOTION STATUS
            if (args[0].equalsIgnoreCase("status")) // show the status of the given player
            {
               if(sender.hasPermission("timedranks.status.other"))
               {
                  OfflinePlayer offlinePlayer = plugin.getServer().getOfflinePlayer(args[1]); // Caution. This always returns a valid object!
                  String playerNameToShowStatus = null;

                  if((null != offlinePlayer) &&
                        (offlinePlayer.hasPlayedBefore())) // checks if this player is a valid one
                  {
                     playerNameToShowStatus = offlinePlayer.getName();
                  }

                  if(null != playerNameToShowStatus)
                  {
                     if(plugin.playerIsOnPromotionList(playerNameToShowStatus))
                     {
                        if(plugin.promotionIsActive(playerNameToShowStatus))
                        {
                           String promoteGroup = plugin.getPromoteGroup(playerNameToShowStatus);

                           if(sender instanceof Player)
                           {                           
                              sender.sendMessage(ChatColor.WHITE + "Die Ernennung von " + playerNameToShowStatus + " zum " + ChatColor.GREEN + promoteGroup + ChatColor.WHITE + " endet in " + ChatColor.GREEN + plugin.getPromotionEndTime(playerNameToShowStatus) + ChatColor.WHITE + " Tagen.");

                              if(plugin.playerIsInPayedGroup(playerNameToShowStatus))
                              {
                                 sender.sendMessage(ChatColor.WHITE + "Naechste Auszahlung in " + ChatColor.GREEN + plugin.getNextPaymentTime(playerNameToShowStatus) + ChatColor.WHITE + " Tagen.");
                              }
                           }
                           else
                           {
                              sender.sendMessage("Promotion of " + playerNameToShowStatus + " as " + promoteGroup + " ends in " + plugin.getPromotionEndTime(playerNameToShowStatus) + " days.");

                              if(plugin.playerIsInPayedGroup(playerNameToShowStatus))
                              {
                                 sender.sendMessage("Next payment in " + plugin.getNextPaymentTime(playerNameToShowStatus) + " days.");
                              }
                           }
                        }
                        else
                        {
                           if(sender instanceof Player)
                           {
                              sender.sendMessage(TimedRanks.logPrefix + "Die Ernennung dieses Spielers ist zur Zeit pausiert.");
                           }
                           else
                           {
                              sender.sendMessage(TimedRanks.logPrefix + "This players promotion is currently paused.");
                           }
                        }
                     }
                     else
                     {
                        if(sender instanceof Player)
                        {
                           sender.sendMessage(TimedRanks.logPrefix + "Dieser Spieler ist momentan nicht ueber " + plugin.getDescription().getName() + " in einen hoeheren Rang ernannt.");
                        }
                        else
                        {
                           sender.sendMessage(TimedRanks.logPrefix + "This Player is currently not promoted in a higher rank by " + plugin.getDescription().getName() + ".");
                        }
                     }
                  }
                  else
                  {
                     if(sender instanceof Player)
                     {
                        sender.sendMessage(TimedRanks.logPrefix + "Spieler wurde nicht gefunden. Bitte Name pruefen!");
                     }
                     else
                     {
                        sender.sendMessage(TimedRanks.logPrefix + "Player not found. Please check the name!");
                     }
                  }
               }
               else
               {
                  sender.sendMessage(ChatColor.RED + "Du hast keine Berechtigung um den Status anderer Spieler abzufragen!");
               }
               return true;
            }

            // PAUSE PROMOTION OF A PLAYER
            if (args[0].equalsIgnoreCase("pause")) // pause the promoted rank immediately until it gets manually resumed
            {            
               if(sender.hasPermission("timedranks.manage"))
               {
                  OfflinePlayer offlinePlayer = plugin.getServer().getOfflinePlayer(args[1]); // Caution. This always returns a valid object!
                  String playersNameToPause = null;

                  if((null != offlinePlayer) &&
                        (offlinePlayer.hasPlayedBefore())) // checks if this player is a valid one
                  {
                     playersNameToPause = offlinePlayer.getName();                                
                  }

                  if(null != playersNameToPause)
                  {
                     if(plugin.playerIsOnPromotionList(playersNameToPause))
                     {
                        if(plugin.promotionIsActive(playersNameToPause))
                        {
                           if(plugin.pausePromotion(playersNameToPause))
                           {
                              if(sender instanceof Player)
                              {
                                 sender.sendMessage(TimedRanks.logPrefix + ChatColor.WHITE + "Die Ernennung dieses Spielers wurde pausiert.");
                              }
                              else
                              {
                                 sender.sendMessage(TimedRanks.logPrefix + "Promotion of this players has been paused.");
                              }
                           }
                           else
                           {
                              if(sender instanceof Player)
                              {
                                 sender.sendMessage(TimedRanks.logPrefix + ChatColor.RED + "Fehler beim pausieren der Ernennung!");
                              }
                              else
                              {
                                 TimedRanks.log.severe(TimedRanks.logPrefix + "Error on pausing " + playersNameToPause + "'s promotion!");
                              }
                           }
                        }
                        else
                        {
                           if(sender instanceof Player)
                           {
                              sender.sendMessage(TimedRanks.logPrefix + ChatColor.YELLOW + "Die Ernennung dieses Spielers ist bereits pausiert.");
                           }
                           else
                           {
                              sender.sendMessage(TimedRanks.logPrefix + "Promotion of this player has already been paused.");
                           }
                        }                        
                     }
                     else
                     {
                        if(sender instanceof Player)
                        {
                           sender.sendMessage(TimedRanks.logPrefix + ChatColor.WHITE + "Dieser Spieler ist momentan nicht ueber " + plugin.getDescription().getName() + " in einen hoeheren Rang ernannt.");
                        }
                        else
                        {
                           sender.sendMessage(TimedRanks.logPrefix + "This Player is currently not promoted in a higher rank by " + plugin.getDescription().getName() + ".");
                        }
                     }
                  }
               }
               else
               {
                  sender.sendMessage(ChatColor.RED + "Du hast keine Berechtigung um Raenge zu managen!");
               }
               return true;
            }                

            // RESUME PROMOTION OF A PLAYER
            if (args[0].equalsIgnoreCase("resume")) // resume the promoted rank, adding the suspended time to the end timestamp and next payment time
            {            
               if(sender.hasPermission("timedranks.manage"))
               {
                  OfflinePlayer offlinePlayer = plugin.getServer().getOfflinePlayer(args[1]); // Caution. This always returns a valid object!
                  String playersNameToResume = null;

                  if((null != offlinePlayer) &&
                        (offlinePlayer.hasPlayedBefore())) // checks if this player is a valid one
                  {
                     playersNameToResume = offlinePlayer.getName();                               
                  }

                  if(null != playersNameToResume)
                  {
                     if(plugin.playerIsOnPromotionList(playersNameToResume))
                     {
                        if(!plugin.promotionIsActive(playersNameToResume))
                        {
                           if(plugin.resumePromotion(playersNameToResume))
                           {
                              if(sender instanceof Player)
                              {
                                 sender.sendMessage(TimedRanks.logPrefix + ChatColor.WHITE + "Die Ernennung dieses Spielers wurde wieder aktiviert.");
                              }
                              else
                              {
                                 sender.sendMessage(TimedRanks.logPrefix + "Promotion of this players has been reactivated.");
                              }
                           }
                           else
                           {
                              if(sender instanceof Player)
                              {
                                 sender.sendMessage(TimedRanks.logPrefix + ChatColor.RED + "Fehler beim reaktivieren der Ernennung!");
                              }
                              else
                              {
                                 TimedRanks.log.severe(TimedRanks.logPrefix + "Error on reactivating " + playersNameToResume + "'s promotion!");
                              }
                           }
                        }
                        else
                        {
                           if(sender instanceof Player)
                           {
                              sender.sendMessage(TimedRanks.logPrefix + ChatColor.YELLOW + "Die Ernennung dieses Spielers ist schon aktiv.");
                           }
                           else
                           {
                              sender.sendMessage(TimedRanks.logPrefix + "Promotion of this players is already active.");
                           }
                        }                        
                     }
                     else
                     {
                        if(sender instanceof Player)
                        {
                           sender.sendMessage(TimedRanks.logPrefix + ChatColor.WHITE + "Dieser Spieler ist momentan nicht ueber " + plugin.getDescription().getName() + " in einen hoeheren Rang ernannt.");
                        }
                        else
                        {
                           sender.sendMessage(TimedRanks.logPrefix + "This Player is currently not promoted in a higher rank by " + plugin.getDescription().getName() + ".");
                        }
                     }
                  }

               }
               else
               {
                  sender.sendMessage(ChatColor.RED + "Du hast keine Berechtigung um um Raenge zu managen!");
               }
               return true;
            }

            // DEMOTE A PROMOTED PLAYER
            if (args[0].equalsIgnoreCase("demote"))
            {                 
               if(sender.hasPermission("timedranks.manage"))
               {
                  OfflinePlayer offlinePlayer = plugin.getServer().getOfflinePlayer(args[1]); // Caution. This always returns a valid object!
                  String playersNameToDemote = null;

                  if((null != offlinePlayer) &&
                        (offlinePlayer.hasPlayedBefore())) // checks if this player is a valid one
                  {
                     playersNameToDemote = offlinePlayer.getName();   
                  }

                  if(null != playersNameToDemote)
                  {
                     if(plugin.playerIsDemotable(playersNameToDemote))
                     {
                        String baseGroup = plugin.getBaseGroup(playersNameToDemote);
                        String promoteGroup = plugin.getPromoteGroup(playersNameToDemote);

                        if((null != baseGroup) &&
                              (null != promoteGroup)) // given player is valid and demotable and his groups are found
                        {
                           // Perform promotion of player                    
                           World nullWorld = null;

                           if((perm.playerAddGroup(nullWorld, playersNameToDemote, baseGroup)) && // add player to baseGroup
                                 (perm.playerRemoveGroup(nullWorld, playersNameToDemote, promoteGroup))) //remove player from current promoteGroup
                           {
                              plugin.deletePlayerFromPromotionList(playersNameToDemote);
                              TimedRanks.log.info(TimedRanks.logPrefix + playersNameToDemote + " was demoted to rank: " + baseGroup + " from " + sender.getName());

                              if(sender instanceof Player)
                              {
                                 sender.sendMessage(TimedRanks.logPrefix + ChatColor.YELLOW + playersNameToDemote + ChatColor.WHITE + " wurde vom " + ChatColor.RED + promoteGroup + ChatColor.WHITE + " zum " + ChatColor.GREEN + baseGroup + ChatColor.WHITE + " zurueckgestuft.");                                 
                              }

                              try
                              {
                                 if(plugin.getServer().getPlayer(playersNameToDemote).isOnline())
                                 {
                                    // TODO hier die Messages aus der Config einfügen!  Auch bei den Fehlermeldungen!                      
                                    plugin.getServer().getPlayer(playersNameToDemote).sendMessage(TimedRanks.logPrefix + ChatColor.WHITE + "Du wurdest vom " + ChatColor.RED + promoteGroup + ChatColor.WHITE + " zum " + ChatColor.GREEN + baseGroup + ChatColor.WHITE + " zurueckgestuft.");
                                 }
                              }
                              catch (Exception ex)
                              {
                                 // player is not online
                              }                           
                           }
                           else
                           {
                              if(sender instanceof Player)
                              {
                                 sender.sendMessage(TimedRanks.logPrefix + ChatColor.YELLOW + "Fehler beim Ernennen dieses Spielers!");
                              }
                              else
                              {
                                 TimedRanks.log.severe(TimedRanks.logPrefix + "Error on promoting this player!");
                              }                           
                           }
                        }
                        else
                        {
                           if(sender instanceof Player)
                           {
                              sender.sendMessage(TimedRanks.logPrefix + ChatColor.YELLOW + "Fehler in der Config-File! Gruppenzuordnung nicht erkannt.");
                           }
                           else
                           {
                              TimedRanks.log.severe(TimedRanks.logPrefix + "Error in cofnig file. Group matching not recognizable.");
                           }                        
                        }
                     }
                     else
                     {
                        if(sender instanceof Player)
                        {
                           sender.sendMessage(TimedRanks.logPrefix + "Dieser Spieler ist momentan nicht ueber " + plugin.getDescription().getName() + " in einen hoeheren Rang ernannt.");
                        }
                        else
                        {
                           sender.sendMessage(TimedRanks.logPrefix + "This Player is currently not promoted in a higher rank by " + plugin.getDescription().getName() + ".");
                        }
                     }
                  }
                  else
                  {
                     if(sender instanceof Player)
                     {
                        sender.sendMessage(TimedRanks.logPrefix + "Spieler wurde nicht gefunden. Bitte Name pruefen!");
                     }
                     else
                     {
                        sender.sendMessage(TimedRanks.logPrefix + "Player not found. Please check the name!");
                     }
                  }                  
               }
               else
               {
                  sender.sendMessage(ChatColor.RED + "Du hast keine Berechtigung um Raenge zu managen!");
               }

               return true;
            }

            // PAY PLAYER BEFORE DUE DATE (this will postpone the next payment accordingly by one payment interval!)
            if (args[0].equalsIgnoreCase("pay"))
            {                 
               if(sender.hasPermission("timedranks.admin"))
               {
                  OfflinePlayer offlinePlayer = plugin.getServer().getOfflinePlayer(args[1]); // Caution. This always returns a valid object!
                  String playersNameToPay = null;

                  if((null != offlinePlayer) &&
                        (offlinePlayer.hasPlayedBefore())) // checks if this player is a valid one
                  {
                     playersNameToPay = offlinePlayer.getName();   
                  }

                  if(null != playersNameToPay)
                  {
                     if(plugin.playerIsInPayedGroup(playersNameToPay))
                     {                        
                        String promoteGroup = plugin.getPromoteGroup(playersNameToPay);

                        if(null != promoteGroup)
                        {
                           if(plugin.payPlayer(playersNameToPay))
                           {
                              if(plugin.scheduleNextPayment(playersNameToPay, plugin.getPromoteGroup(playersNameToPay)))
                              {
                                 // everything went OK. Error handling is inside the called methods.
                                 if(sender instanceof Player)
                                 {
                                    sender.sendMessage(TimedRanks.logPrefix + ChatColor.GREEN + "Spieler wurde ausgezahlt. Die naechste Zahlung wurde entsprechend verschoben.");
                                 }
                                 else
                                 {
                                    TimedRanks.log.info(TimedRanks.logPrefix + "Player has been payed. Next pay time has been postponed acordingly.");
                                 }
                                 
                                 try
                                 {
                                    if(plugin.getServer().getPlayer(playersNameToPay).isOnline())
                                    {
                                       // TODO hier die Messages aus der Config einfügen!  Auch bei den Fehlermeldungen!                      
                                       plugin.getServer().getPlayer(playersNameToPay).sendMessage(TimedRanks.logPrefix + ChatColor.YELLOW + "Du hast diese Ueberweisung vorzeitig erhalten.");
                                       plugin.getServer().getPlayer(playersNameToPay).sendMessage(TimedRanks.logPrefix + ChatColor.YELLOW + "Die naechste wird entsprechend verschoben.");
                                    }
                                 }
                                 catch (Exception ex)
                                 {
                                    // player is not online
                                 }
                              }
                           }
                        }
                        else
                        {
                           if(sender instanceof Player)
                           {
                              sender.sendMessage(TimedRanks.logPrefix + ChatColor.YELLOW + "Fehler in der Config-File! Gruppenzuordnung nicht erkannt.");
                           }
                           else
                           {
                              TimedRanks.log.severe(TimedRanks.logPrefix + "Error in cofnig file. Group matching not recognizable.");
                           }                        
                        }
                     }
                     else
                     {
                        if(sender instanceof Player)
                        {
                           sender.sendMessage(TimedRanks.logPrefix + "Dieser Spieler ist momentan nicht ueber " + plugin.getDescription().getName() + " in einen hoeheren Rang ernannt.");
                        }
                        else
                        {
                           sender.sendMessage(TimedRanks.logPrefix + "This Player is currently not promoted in a higher rank by " + plugin.getDescription().getName() + ".");
                        }
                     }
                  }
                  else
                  {
                     if(sender instanceof Player)
                     {
                        sender.sendMessage(TimedRanks.logPrefix + "Spieler wurde nicht gefunden. Bitte Name pruefen!");
                     }
                     else
                     {
                        sender.sendMessage(TimedRanks.logPrefix + "Player not found. Please check the name!");
                     }
                  }                  
               }
               else
               {
                  sender.sendMessage(ChatColor.RED + "Du hast keine Berechtigung um Raenge zu managen!");
               }

               return true;
            }
         }

         if (args.length == 3)
         {
            // PROMOTE A PLAYER FOR A GIVEN TIME
            if (args[0].equalsIgnoreCase("promote"))
            {
               if(sender.hasPermission("timedranks.manage"))
               {
                  int daysToPromote = 0;

                  OfflinePlayer offlinePlayer = plugin.getServer().getOfflinePlayer(args[1]); // Caution. This always returns a valid object!
                  String playersNameToPromote = null;

                  if((null != offlinePlayer) &&
                        (offlinePlayer.hasPlayedBefore())) // checks if this player is a valid one
                  {
                     playersNameToPromote = offlinePlayer.getName();   
                  }

                  if(TimedRanksUtils.tryParseInt(args[2]))
                  {
                     daysToPromote = Integer.parseInt(args[2]);
                  }

                  if(null != playersNameToPromote)
                  {
                     if(plugin.playerIsPromotable(playersNameToPromote))
                     {
                        String baseGroup = plugin.getBaseGroup(playersNameToPromote);
                        String promoteGroup = plugin.getPromoteGroup(playersNameToPromote);

                        if((null != baseGroup) &&
                              (null != promoteGroup)) // given player is valid and promotable and his groups are found
                        {
                           // perform promotion of player
                           World nullWorld = null;

                           if((daysToPromote > 0) && // will be 0 if parsing failed or sender used invalid value
                                 ((daysToPromote < 10000))) // prevent unrealistic values
                           {
                              if((perm.playerAddGroup(nullWorld, playersNameToPromote, promoteGroup)) && // add player to promoteGroup                     
                                    (perm.playerRemoveGroup(nullWorld, playersNameToPromote, baseGroup))) //remove player from current baseGroup
                              {
                                 plugin.addPlayerToPromotionList(playersNameToPromote, daysToPromote, promoteGroup);
                                 // TODO Pay a player immediately for the first time after promotion? -> Could be dangerous when players are promoted in short succession...
                                 // They will every time get payed. Currently, this will not be implemented!
                                 TimedRanks.log.info(TimedRanks.logPrefix + playersNameToPromote + " was promoted to rank: " + promoteGroup + " from " + sender.getName() + " for " + daysToPromote + " days.");

                                 if(sender instanceof Player)
                                 {
                                    sender.sendMessage(TimedRanks.logPrefix + ChatColor.YELLOW + playersNameToPromote + ChatColor.WHITE + " wurde fuer " + ChatColor.GREEN + daysToPromote + ChatColor.WHITE + " Tage zum " + ChatColor.GREEN + promoteGroup + ChatColor.WHITE + " ernannt.");                                    
                                 }

                                 try
                                 {
                                    if(plugin.getServer().getPlayer(playersNameToPromote).isOnline())
                                    {
                                       // TODO hier die Messages aus der Config einfügen!  Auch bei den Fehlermeldungen!   
                                       plugin.getServer().getPlayer(playersNameToPromote).sendMessage(TimedRanks.logPrefix + "Du wurdest fuer " + ChatColor.GREEN + daysToPromote + ChatColor.WHITE + " Tage zum " + ChatColor.GREEN + promoteGroup + ChatColor.WHITE + " ernannt.");
                                    }
                                 }
                                 catch (Exception ex)
                                 {
                                    // player is not online
                                 }                                 
                              }
                              else
                              {
                                 if(sender instanceof Player)
                                 {
                                    sender.sendMessage(TimedRanks.logPrefix + ChatColor.YELLOW + "Fehler beim Ernennen dieses Spielers!");
                                 }
                                 else
                                 {
                                    TimedRanks.log.severe(TimedRanks.logPrefix + "Error on promoting this player!");
                                 }                           
                              }
                           }
                           else
                           {
                              if(sender instanceof Player)
                              {
                                 sender.sendMessage(TimedRanks.logPrefix + ChatColor.YELLOW + "Ungueltiger Befehl. Format: " + ChatColor.WHITE + "/tr promote <SPIELER> <TAGE>");
                              }
                              else
                              {
                                 TimedRanks.log.severe(TimedRanks.logPrefix + "Invalid command. Use Format: /tr promote <PLAYER> <DAYS>");
                              }
                           }
                        }
                        else
                        {
                           if(sender instanceof Player)
                           {
                              sender.sendMessage(TimedRanks.logPrefix + ChatColor.YELLOW + "Fehler in der Config-File! Gruppenzuordnung nicht erkannt.");
                           }
                           else
                           {
                              TimedRanks.log.severe(TimedRanks.logPrefix + "Error in cofnig file. Group matching not recognizable.");
                           }                        
                        }
                     }
                     else
                     {
                        World nWorld = null;

                        if(sender instanceof Player)
                        {
                           sender.sendMessage(TimedRanks.logPrefix + "Dieser Spieler ist " + ChatColor.YELLOW + perm.getPrimaryGroup(nWorld, playersNameToPromote) + ChatColor.WHITE + " und kann nicht weiter mit " + plugin.getDescription().getName() + " befoerdert werden.");
                        }
                        else
                        {
                           sender.sendMessage(TimedRanks.logPrefix + "This player is " + perm.getPrimaryGroup(nWorld, playersNameToPromote) + " and cannot be promoted further by " + plugin.getDescription().getName() + ".");
                        }
                     }
                  }
                  else
                  {
                     if(sender instanceof Player)
                     {
                        sender.sendMessage(TimedRanks.logPrefix + "Spieler wurde nicht gefunden. Bitte Name pruefen!");
                     }
                     else
                     {
                        sender.sendMessage(TimedRanks.logPrefix + "Player not found. Please check the name!");
                     }
                  }
               }
               else
               {
                  sender.sendMessage(ChatColor.RED + "Du hast keine Berechtigung um Raenge zu managen!");
               }

               return true;
            }

            // ADD TIME TO A PLAYERS CURRENTLY ACTIVE PROMOTION
            if (args[0].equalsIgnoreCase("add") || args[0].equalsIgnoreCase("gib"))
            {            
               if(sender.hasPermission("timedranks.manage"))
               {
                  OfflinePlayer offlinePlayer = plugin.getServer().getOfflinePlayer(args[1]); // Caution. This always returns a valid object!
                  String playersNameToAddTime = null;

                  if((null != offlinePlayer) &&
                        (offlinePlayer.hasPlayedBefore())) // checks if this player is a valid one
                  {
                     playersNameToAddTime = offlinePlayer.getName();   
                  }

                  if(null != playersNameToAddTime)
                  {
                     int daysToAdd = 0;                  

                     if(TimedRanksUtils.tryParseInt(args[2]))
                     {
                        daysToAdd = Integer.parseInt(args[2]);
                     }

                     if(plugin.playerIsOnPromotionList(playersNameToAddTime))
                     {                     
                        if(0 < daysToAdd)
                        {
                           if(plugin.addPromotionTime(playersNameToAddTime, daysToAdd))
                           {
                              if(sender instanceof Player)
                              {
                                 sender.sendMessage(TimedRanks.logPrefix + ChatColor.GREEN + playersNameToAddTime + ChatColor.WHITE + " wurden " + ChatColor.GREEN + daysToAdd + ChatColor.WHITE + " Tage hinzugefuegt.");
                              }
                              else
                              {
                                 TimedRanks.log.info(TimedRanks.logPrefix + daysToAdd + " days have been added to " + playersNameToAddTime + "'s promotion.");
                              }
                           }
                           else
                           {
                              if(sender instanceof Player)
                              {
                                 sender.sendMessage(TimedRanks.logPrefix + ChatColor.YELLOW + "Zeit konnte nicht hinzugefuegt werden. Format: /tr add|gib <SPIELER> <TAGE>");
                              }
                              else
                              {
                                 TimedRanks.log.info(TimedRanks.logPrefix + "Time could not be added. Format: /tr add <PLAYER> <DAYS>");
                              }
                           }
                        }
                        else
                        {
                           if(sender instanceof Player)
                           {
                              sender.sendMessage(TimedRanks.logPrefix + ChatColor.YELLOW + "Bitte positiven Wert fuer die Tage angeben! Format: /tr sub|nimm <SPIELER> <TAGE>");
                           }
                           else
                           {
                              TimedRanks.log.info(TimedRanks.logPrefix + "Please use a positive value for the days! Format: /tr sub <PLAYER> <DAYS>");
                           }
                        }
                     }
                     else
                     {
                        if(sender instanceof Player)
                        {
                           sender.sendMessage(TimedRanks.logPrefix + "Dieser Spieler ist momentan nicht ueber " + plugin.getDescription().getName() + " in einen hoeheren Rang ernannt.");
                        }
                        else
                        {
                           sender.sendMessage(TimedRanks.logPrefix + "This Player is currently not promoted in a higher rank by " + plugin.getDescription().getName() + ".");
                        }
                     }
                  }
                  else
                  {
                     if(sender instanceof Player)
                     {
                        sender.sendMessage(TimedRanks.logPrefix + "Spieler wurde nicht gefunden. Bitte Name pruefen!");
                     }
                     else
                     {
                        sender.sendMessage(TimedRanks.logPrefix + "Player not found. Please check the name!");
                     }
                  }

               }
               else
               {
                  sender.sendMessage(ChatColor.RED + "Du hast keine Berechtigung um Raenge zu managen!");
               }
               return true;
            }

            // SUBTRACT TIME FROM A PLAYERS CURRENTLY ACTIVE PROMOTION
            if (args[0].equalsIgnoreCase("sub") || args[0].equalsIgnoreCase("nimm"))
            {                   
               if(sender.hasPermission("timedranks.manage"))
               {
                  OfflinePlayer offlinePlayer = plugin.getServer().getOfflinePlayer(args[1]); // Caution. This always returns a valid object!
                  String playersNameToSubtractTime = null;

                  if((null != offlinePlayer) &&
                        (offlinePlayer.hasPlayedBefore())) // checks if this player is a valid one
                  {
                     playersNameToSubtractTime = offlinePlayer.getName();   
                  }

                  if(null != playersNameToSubtractTime)
                  {
                     int daysToSubtract = 0;

                     if(TimedRanksUtils.tryParseInt(args[2]))
                     {
                        daysToSubtract = Integer.parseInt(args[2]);
                     }

                     if(plugin.playerIsOnPromotionList(playersNameToSubtractTime))
                     {
                        if(0 < daysToSubtract)
                        {
                           if(plugin.substractPromotionTime(playersNameToSubtractTime, daysToSubtract))
                           {
                              if(sender instanceof Player)
                              {
                                 sender.sendMessage(TimedRanks.logPrefix + ChatColor.GREEN + playersNameToSubtractTime + ChatColor.WHITE + " wurden " + ChatColor.GREEN + daysToSubtract + ChatColor.WHITE + " Tage abgezogen.");
                              }
                              else
                              {
                                 TimedRanks.log.info(TimedRanks.logPrefix + daysToSubtract + " days have been subtracted from " + playersNameToSubtractTime + "'s promotion.");
                              }
                           }
                           else
                           {
                              if(sender instanceof Player)
                              {
                                 sender.sendMessage(TimedRanks.logPrefix + ChatColor.YELLOW + "Zeit konnte nicht abgezogen werden. Format: /tr add|gib <SPIELER> <TAGE>.");
                              }
                              else
                              {
                                 TimedRanks.log.info(TimedRanks.logPrefix + "Time could not be subtracted. Format: /tr add <SPIELER> <TAGE>.");
                              }
                           }
                        }
                        else
                        {
                           if(sender instanceof Player)
                           {
                              sender.sendMessage(TimedRanks.logPrefix + ChatColor.YELLOW + "Bitte positiven Wert fuer die Tage angeben! Format: /tr sub|nimm <SPIELER> <TAGE>");
                           }
                           else
                           {
                              TimedRanks.log.info(TimedRanks.logPrefix + "Please use a positive value for the days! Format: /tr sub <PLAYER> <DAYS>");
                           }                           
                        }
                     }
                     else
                     {
                        if(sender instanceof Player)
                        {
                           sender.sendMessage(TimedRanks.logPrefix + "Dieser Spieler ist momentan nicht ueber " + plugin.getDescription().getName() + " in einen hoeheren Rang ernannt.");
                        }
                        else
                        {
                           sender.sendMessage(TimedRanks.logPrefix + "This Player is currently not promoted in a higher rank by " + plugin.getDescription().getName() + ".");
                        }
                     }
                  }
                  else
                  {
                     if(sender instanceof Player)
                     {
                        sender.sendMessage(TimedRanks.logPrefix + "Spieler wurde nicht gefunden. Bitte Name pruefen!");
                     }
                     else
                     {
                        sender.sendMessage(TimedRanks.logPrefix + "Player not found. Please check the name!");
                     }
                  }

               }
               else
               {
                  sender.sendMessage(ChatColor.RED + "Du hast keine Berechtigung um Raenge zu managen!");
               }
               return true;
            }            
         }         
         else
         {
            sender.sendMessage(ChatColor.YELLOW + TimedRanks.logPrefix + "Falsche Anzahl an Parametern.");            
         }
      }

      return false; // No valid parameter count. If false is returned, the help for the command stated in the plugin.yml will be displayed to the player
   }   
}
