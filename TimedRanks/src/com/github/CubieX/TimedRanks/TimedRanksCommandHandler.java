package com.github.CubieX.TimedRanks;

import java.util.ArrayList;
import java.util.Set;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

public class TimedRanksCommandHandler implements CommandExecutor
{
   private final TimedRanks plugin;
   private final TimedRanksConfigHandler cHandler;
   private final Permission perm;
   private final TimedRanksFileLogger fileLogger;
   private final int contentLinesPerPage = 10;

   public TimedRanksCommandHandler(TimedRanks plugin, TimedRanksConfigHandler cHandler, Permission perm, TimedRanksFileLogger fileLogger)
   {
      this.plugin = plugin;
      this.cHandler = cHandler;
      this.perm = perm;
      this.fileLogger = fileLogger;
   }

   @Override
   public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
   {
      if (cmd.getName().equalsIgnoreCase("tr"))
      {
         if (args.length == 0)
         { //no arguments, so help will be displayed
            return false;
         }

         if (args.length == 1)
         {
            // SHOW PLUGIN VERSION =======================================================================
            if (args[0].equalsIgnoreCase("version")) // show the current version of the plugin
            {            
               sender.sendMessage(ChatColor.GREEN + "This server is running " + plugin.getDescription().getName() + " " + plugin.getDescription().getVersion());
               return true;
            }    

            // RELOAD CONFIG =======================================================================
            if (args[0].equalsIgnoreCase("reload")) // reload the plugins config and playerfile
            {            
               if(sender.isOp() || sender.hasPermission("timedranks.admin"))
               {
                  cHandler.reloadConfig(sender);                        
               }
               else
               {
                  sender.sendMessage(ChatColor.RED + "Du hast keine Berechtigung um " + plugin.getDescription().getName() + " neu zu laden!");
               }
               return true;
            }

            // SHOW OWN PROMOTION STATUS =======================================================================
            if ((args[0].equalsIgnoreCase("status")) || (args[0].equalsIgnoreCase("info"))) // no name given, so show status of player that issued the command
            {
               if(sender.isOp() || sender.hasPermission("timedranks.status.own"))
               {
                  if(sender instanceof Player)
                  {
                     if(plugin.playerIsOnPromotionList(sender.getName()))
                     {
                        String promoteGroup = plugin.getPromoteGroup(sender.getName());

                        if(plugin.promotionIsActive(sender.getName()))
                        {
                           sender.sendMessage(ChatColor.WHITE + "Deine Ernennung zum " + ChatColor.GREEN + promoteGroup + ChatColor.WHITE + " endet " + plugin.getPromotionEndTimeMessage(sender.getName()));
                        }
                        else
                        {
                           sender.sendMessage(ChatColor.WHITE + "Deine Ernennung zum " + ChatColor.GREEN + promoteGroup + " ist zur Zeit pausiert.\n" +
                                 ChatColor.WHITE + "Nach Fortsetzung endet die Ernennung " + plugin.getPromotionEndTimeMessage(sender.getName()));

                           if(plugin.playerIsInPayedGroup(sender.getName()))
                           {
                              sender.sendMessage(ChatColor.WHITE + "Naechste Auszahlung " + ChatColor.GREEN + plugin.getNextPaymentTime(sender.getName()));
                           }
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

            // SHOW A LIST OF ALL CURRENTLY PROMOTED PLAYERS - Page 1 (further pages are in "2 Parameters" section!) ===================
            if (args[0].equalsIgnoreCase("list") || args[0].equalsIgnoreCase("liste"))
            { 
               if(sender.isOp() || sender.hasPermission("timedranks.status.other"))
               {
                  int countAll = 0;
                  int countActive = 0;
                  int countPaused = 0;
                  String status = "READ ERROR";
                  String daysLeft = "READ_ERROR";                     

                  ConfigurationSection confSec = cHandler.getPromotedPlayersFile().getConfigurationSection("players");
                  Set<String> promotedPlayersList;
                  ArrayList <String> lineList = new ArrayList<String>();

                  if(null != confSec)
                  {
                     promotedPlayersList = confSec.getKeys(false);

                     for (String name : promotedPlayersList)
                     {
                        if(plugin.promotionIsActive(name))
                        {
                           status = ChatColor.GREEN + "active";
                           countActive++;
                           countAll++;
                        }
                        else
                        {
                           status = ChatColor.RED + "paused";
                           countPaused++;
                           countAll++;
                        }

                        daysLeft = plugin.getPromotionEndTimeMessage(name);

                        if(sender instanceof Player)
                        {                        
                           lineList.add(ChatColor.GREEN + name + ChatColor.WHITE + ": Laeuft ab " + daysLeft + " Status: " + status);
                        }
                        else
                        {
                           lineList.add(name + ": Laeuft ab " + daysLeft + " Status: " + status);
                        }
                     }
                  }

                  // send list paginated
                  paginateList(sender, lineList, 1, countAll, countActive, countPaused);                  
               }
               else
               {
                  sender.sendMessage(ChatColor.RED + "Du hast keine Berechtigung um die Liste anzuzeigen!");
               }

               return true;
            }

            // DISPLAY HELP Page 1 (Page 2 is in "2 Parameters" section!) =======================================================================
            if ((args[0].equalsIgnoreCase("help")) || (args[0].equalsIgnoreCase("hilfe")))
            {
               String[] messages = {
                     ChatColor.GREEN + "------------------------\n" + 
                           "TimedRanks - Hilfe\n" + 
                           "------------------------\n" + 
                           "/vip version - Version anzeigen.\n" + 
                           ChatColor.RED + "/vip reload - Configs neu laden\n" + 
                           ChatColor.GREEN + "/vip status|info" + ChatColor.RED + " [SPIELER]" + ChatColor.GREEN + " - Ernennungsstatus anzeigen.\n" + 
                           ChatColor.RED + "/vip list|liste - Liste - Liste aller VIPs.\n" + 
                           "/vip promote SPIELER TAGE - Spieler zum VIP machen\n" + 
                           "/vip demote SPIELER - Spieler in alte Gruppe zurueckstufen\n" + 
                           "/vip add|gib SPIELER TAGE - Tage im VIP-Rank hinzufuegen\n" + 
                           ChatColor.YELLOW + "/vip hilfe 2" + ChatColor.GRAY + " fuer 2. Seite."
               };

               sender.sendMessage(messages);

               return true;
            }
         }

         if (args.length == 2)
         {
            // DISPLAY HELP Page 2 (Page 1 is in "1 Parameters" section!) =======================================================================
            if ((args[0].equalsIgnoreCase("help")) || (args[0].equalsIgnoreCase("hilfe")))
            {
               if((null != args[1]) &&
                     (args[1].equals("2")))
               {
                  String[] messages = {
                        ChatColor.GREEN + "-------------------------------\n" + 
                              "TimedRanks - Hilfe - Seite 2\n" + 
                              "-------------------------------\n" + 
                              ChatColor.RED + "/vip sub|nimm SPIELER TAGE - Tage im VIP-Rang abziehen\n" + 
                              "/vip pause SPIELER - VIP-Ernennung pausieren\n" + 
                              "/vip resume|weiter SPIELER - VIP-Ernennung weiterlaufen lassen\n" + 
                              "/vip pay|zahle SPIELER - Spieler sofort auszahlen\n" + 
                              "/vip del|delete|loesche SPIELER - Spieler aus Liste loeschen"
                  };

                  sender.sendMessage(messages);

                  return true;
               }
            }

            // SHOW A LIST OF ALL CURRENTLY PROMOTED PLAYERS - Page 2 to X ======================================================================= 
            if (args[0].equalsIgnoreCase("list") || args[0].equalsIgnoreCase("liste"))
            {
               if(sender.isOp() || sender.hasPermission("timedranks.status.other"))
               {
                  if(TimedRanksUtils.isPositiveInteger(args[1]))
                  {
                     int countAll = 0;
                     int countActive = 0;
                     int countPaused = 0;
                     String status = "READ ERROR";
                     String daysLeft = "READ_ERROR";                     
                     Set<String> promotedPlayersList = cHandler.getPromotedPlayersFile().getConfigurationSection("players").getKeys(false);
                     ArrayList <String> lineList = new ArrayList<String>();

                     for (String name : promotedPlayersList)
                     {  
                        if(plugin.promotionIsActive(name))
                        {
                           status = ChatColor.GREEN + "active";
                           countActive++;
                           countAll++;
                        }
                        else
                        {
                           status = ChatColor.RED + "paused";
                           countPaused++;
                           countAll++;
                        }

                        daysLeft = plugin.getPromotionEndTimeMessage(name);

                        if(sender instanceof Player)
                        {                        
                           lineList.add(ChatColor.GREEN + name + ChatColor.WHITE + ": Laeuft ab " + daysLeft + " Status: " + status);
                        }
                        else
                        {
                           lineList.add(name + ": Laeuft ab " + daysLeft + " Status: " + status);
                        }
                     }

                     // send list paginated
                     paginateList(sender, lineList, Integer.parseInt(args[1]), countAll, countActive, countPaused);
                  }
                  else
                  {
                     sender.sendMessage(ChatColor.RED + "Der zweite Parameter (Seite) muss eine positive Zahl sein.");
                  }
               }
               else
               {
                  sender.sendMessage(ChatColor.RED + "Du hast keine Berechtigung um die Liste anzuzeigen!");
               }

               return true;
            }

            // SHOW OTHER PLAYERS PROMOTION STATUS =======================================================================
            if ((args[0].equalsIgnoreCase("status")) || (args[0].equalsIgnoreCase("info"))) // show the status of the given player
            {
               if(sender.isOp() || sender.hasPermission("timedranks.status.other"))
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
                        String promoteGroup = plugin.getPromoteGroup(playerNameToShowStatus);

                        if(plugin.promotionIsActive(playerNameToShowStatus))
                        {
                           if(sender instanceof Player)
                           {                           
                              sender.sendMessage(ChatColor.WHITE + "Die Ernennung von " + playerNameToShowStatus + " zum " + ChatColor.GREEN + promoteGroup + ChatColor.WHITE + " endet " + plugin.getPromotionEndTimeMessage(playerNameToShowStatus));

                              if(plugin.playerIsInPayedGroup(playerNameToShowStatus))
                              {
                                 sender.sendMessage(ChatColor.WHITE + "Naechste Auszahlung " + plugin.getNextPaymentTime(playerNameToShowStatus));
                              }
                           }
                           else
                           {
                              sender.sendMessage("Ernennung von " + playerNameToShowStatus + " als " + promoteGroup + " endet " + plugin.getPromotionEndTimeMessage(playerNameToShowStatus));

                              if(plugin.playerIsInPayedGroup(playerNameToShowStatus))
                              {
                                 sender.sendMessage("Naechste Auszahlung " + plugin.getNextPaymentTime(playerNameToShowStatus));
                              }
                           }
                        }
                        else
                        {
                           if(sender instanceof Player)
                           {
                              sender.sendMessage(TimedRanks.logPrefix + ChatColor.WHITE + "Die Ernennung dieses Spielers zum " + ChatColor.GREEN + promoteGroup + ChatColor.WHITE + " ist zur Zeit pausiert.\n" +
                                    ChatColor.WHITE + "Nach Fortsetzung endet die Ernennung " + plugin.getPromotionEndTimeMessage(playerNameToShowStatus));
                           }
                           else
                           {
                              sender.sendMessage(TimedRanks.logPrefix + "Die Ernennung dieses Spielers zum " + promoteGroup + " ist zur Zeit pausiert.");
                              sender.sendMessage(TimedRanks.logPrefix + "Nach Fortsetzung endet die Ernennung " + plugin.getPromotionEndTimeMessage(playerNameToShowStatus));                             
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

            // PAUSE PROMOTION OF A PLAYER =======================================================================
            if (args[0].equalsIgnoreCase("pause")) // pause the promoted rank immediately until it gets manually resumed
            {
               if(sender.isOp() || sender.hasPermission("timedranks.manage"))
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
                        String promoteGroup = plugin.getPromoteGroup(playersNameToPause);

                        if(plugin.promotionIsActive(playersNameToPause))
                        {
                           if(plugin.pausePromotion(playersNameToPause))
                           {
                              if(sender instanceof Player)
                              {
                                 sender.sendMessage(TimedRanks.logPrefix + ChatColor.WHITE + "Die Ernennung dieses Spielers zum " + ChatColor.GREEN + promoteGroup + ChatColor.WHITE + " ist jetzt pausiert.");                                 
                              }
                              else
                              {
                                 sender.sendMessage(TimedRanks.logPrefix + "Promotion of this players to rank " + promoteGroup + " is now paused.");
                              }
                              
                              fileLogger.logPromotionStatusChange(sender.getName() + " PAUSED promotion of player '" + playersNameToPause + "'.");
                           }
                           else
                           {
                              if(sender instanceof Player)
                              {
                                 sender.sendMessage(TimedRanks.logPrefix + ChatColor.RED + "Fehler beim Pausieren der Ernennung!");
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
                              sender.sendMessage(TimedRanks.logPrefix + ChatColor.YELLOW + "Die Ernennung dieses Spielers zum " + ChatColor.GREEN + promoteGroup + ChatColor.YELLOW + " ist bereits pausiert.");
                           }
                           else
                           {
                              sender.sendMessage(TimedRanks.logPrefix + "Promotion of this player to rank " + promoteGroup + " has already been paused.");
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

            // RESUME PROMOTION OF A PLAYER =======================================================================
            if (args[0].equalsIgnoreCase("resume") || args[0].equalsIgnoreCase("weiter")) // resume the promoted rank, adding the suspended time to the end timestamp and next payment time
            {            
               if(sender.isOp() || sender.hasPermission("timedranks.manage"))
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
                        String promoteGroup = plugin.getPromoteGroup(playersNameToResume);

                        if(!plugin.promotionIsActive(playersNameToResume))
                        {
                           if(plugin.resumePromotion(playersNameToResume))
                           {
                              if(sender instanceof Player)
                              {
                                 sender.sendMessage(TimedRanks.logPrefix + ChatColor.WHITE + "Die Ernennung dieses Spielers zum " + ChatColor.GREEN + promoteGroup + ChatColor.WHITE + " wurde wieder aktiviert.");
                              }
                              else
                              {
                                 sender.sendMessage(TimedRanks.logPrefix + "Promotion of this players to rank " + promoteGroup + " has been resumed.");
                              }
                              
                              fileLogger.logPromotionStatusChange(sender.getName() + " RESUMED promotion of player '" + playersNameToResume + "'.");
                           }
                           else
                           {
                              if(sender instanceof Player)
                              {
                                 sender.sendMessage(TimedRanks.logPrefix + ChatColor.RED + "Fehler beim Reaktivieren der Ernennung!");
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
                              sender.sendMessage(TimedRanks.logPrefix + ChatColor.YELLOW + "Die Ernennung dieses Spielers zum " + ChatColor.GREEN + promoteGroup + ChatColor.WHITE + " ist schon aktiv.");
                           }
                           else
                           {
                              sender.sendMessage(TimedRanks.logPrefix + "Promotion of this player to rank " + promoteGroup + " is already active.");
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

            // DEMOTE A PROMOTED PLAYER =======================================================================
            if (args[0].equalsIgnoreCase("demote"))
            {                 
               if(sender.isOp() || sender.hasPermission("timedranks.manage"))
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
                              fileLogger.logPromotionStatusChange(sender.getName() + " DEMOTED player '" + playersNameToDemote + "'.");
                              
                              if(sender instanceof Player)
                              {
                                 sender.sendMessage(TimedRanks.logPrefix + ChatColor.YELLOW + playersNameToDemote + ChatColor.WHITE + " wurde vom " + ChatColor.RED + promoteGroup + ChatColor.WHITE + " zum " + ChatColor.GREEN + baseGroup + ChatColor.WHITE + " zurueckgestuft.");                                 
                              }

                              try
                              {
                                 if(plugin.getServer().getPlayer(playersNameToDemote).isOnline())
                                 {
                                    // TODO hier die Messages aus der Config einfuegen!  Auch bei den Fehlermeldungen!                      
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
                                 sender.sendMessage(TimedRanks.logPrefix + ChatColor.YELLOW + "Fehler beim Zurueckstufen dieses Spielers!");
                              }
                              else
                              {
                                 TimedRanks.log.severe(TimedRanks.logPrefix + "Error on demoting this player!");
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
                        World nullWorld = null;
                        String primaryGroup = perm.getPrimaryGroup(nullWorld, playersNameToDemote);

                        if(sender instanceof Player)
                        {
                           sender.sendMessage(TimedRanks.logPrefix + "Dieser Spieler ist " + ChatColor.GREEN + primaryGroup + ChatColor.WHITE + " und momentan nicht ueber " + plugin.getDescription().getName() + " in einen hoeheren Rang ernannt.");
                        }
                        else
                        {
                           sender.sendMessage(TimedRanks.logPrefix + "This Player is " + primaryGroup + " and currently not promoted in a higher rank by " + plugin.getDescription().getName() + ".");
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

            // PAY PLAYER BEFORE DUE DATE (this will postpone the next payment accordingly by one payment interval!) =======================
            if ((args[0].equalsIgnoreCase("pay")) || (args[0].equalsIgnoreCase("zahle")))
            {
               if(sender.isOp() || sender.hasPermission("timedranks.admin"))
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
                              fileLogger.logTransaction("The last transaction was manually triggered from " + sender.getName() + " for player '" + playersNameToPay + "'"); //log to file
                              
                              if(plugin.scheduleNextPayment(playersNameToPay, plugin.getPromoteGroup(playersNameToPay)))
                              {
                                 // everything went OK. Error handling is inside the called methods.
                                 if(sender instanceof Player)
                                 {
                                    sender.sendMessage(TimedRanks.logPrefix + ChatColor.GREEN + "Spieler wurde ausgezahlt. Die naechste Zahlung wurde entsprechend verschoben.");
                                 }
                                 else
                                 {
                                    TimedRanks.log.info(TimedRanks.logPrefix + "Player has been payed. Next pay time has been postponed accordingly.");                                    
                                 }

                                 try
                                 {
                                    if(plugin.getServer().getPlayer(playersNameToPay).isOnline())
                                    {
                                       // TODO hier die Messages aus der Config einfuegen!  Auch bei den Fehlermeldungen!                      
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
                        World nullWorld = null;
                        String primaryGroup = perm.getPrimaryGroup(nullWorld, playersNameToPay);

                        if(sender instanceof Player)
                        {
                           sender.sendMessage(TimedRanks.logPrefix + "Dieser Spieler ist " + ChatColor.GREEN + primaryGroup + ChatColor.WHITE + " und momentan nicht ueber " + plugin.getDescription().getName() + " in einen hoeheren Rang ernannt.");
                        }
                        else
                        {
                           sender.sendMessage(TimedRanks.logPrefix + "This Player is " + primaryGroup + " and currently not promoted in a higher rank by " + plugin.getDescription().getName() + ".");
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

            // DELETE A PLAYER FROM PROMOTION LIST WITHOUT ANY CHECKS OR RANK MODIFICATIONS ==========================================
            // This is only necessary if the players rank was modified without using TR
            if ((args[0].equalsIgnoreCase("del")) || (args[0].equalsIgnoreCase("delete")) || args[0].equalsIgnoreCase("loeschen"))
            {                 
               if(sender.isOp() || sender.hasPermission("timedranks.admin"))
               {
                  OfflinePlayer offlinePlayer = plugin.getServer().getOfflinePlayer(args[1]); // Caution. This always returns a valid object!
                  String playersNameToDelete = null;

                  if((null != offlinePlayer) &&
                        (offlinePlayer.hasPlayedBefore())) // checks if this player is a valid one
                  {
                     playersNameToDelete = offlinePlayer.getName();   
                  }

                  if(null != playersNameToDelete)
                  {
                     plugin.deletePlayerFromPromotionList(playersNameToDelete);                     
                     TimedRanks.log.info(TimedRanks.logPrefix + playersNameToDelete + " has been deleted from the promotion list!");                   

                     if(sender instanceof Player)
                     {
                        sender.sendMessage(TimedRanks.logPrefix + ChatColor.RED + playersNameToDelete + ChatColor.WHITE + " wurde von der Ernennungsliste geloescht!");
                     }                    
                     
                     fileLogger.logPromotionStatusChange(sender.getName() + " DELETED player '" + playersNameToDelete + "' from promotion list.");
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
            // PROMOTE A PLAYER FOR A GIVEN TIME =======================================================================
            if (args[0].equalsIgnoreCase("promote"))
            {
               if(sender.isOp() || sender.hasPermission("timedranks.manage"))
               {
                  int daysToPromote = 0;

                  OfflinePlayer offlinePlayer = plugin.getServer().getOfflinePlayer(args[1]); // Caution. This always returns a valid object!
                  String playersNameToPromote = null;

                  if((null != offlinePlayer) &&
                        (offlinePlayer.hasPlayedBefore())) // checks if this player is a valid one
                  {
                     playersNameToPromote = offlinePlayer.getName();   
                  }

                  if(TimedRanksUtils.isInteger(args[2]))
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
                                 // They will every time get payed. Currently, this will not be implemented! Perharps with additional "pay" parameter like /tr promote PLAYER pay
                                 TimedRanks.log.info(TimedRanks.logPrefix + playersNameToPromote + " was promoted to rank: " + promoteGroup + " from " + sender.getName() + " for " + daysToPromote + " days.");

                                 if(sender instanceof Player)
                                 {
                                    sender.sendMessage(TimedRanks.logPrefix + ChatColor.YELLOW + playersNameToPromote + ChatColor.WHITE + " wurde fuer " + ChatColor.GREEN + daysToPromote + ChatColor.WHITE + " Tage zum " + ChatColor.GREEN + promoteGroup + ChatColor.WHITE + " ernannt.");                                    
                                 }
                                 
                                 fileLogger.logPromotionStatusChange(sender.getName() + " PROMOTED player '" + playersNameToPromote + "' for " + daysToPromote + " days.");

                                 try
                                 {
                                    if(plugin.getServer().getPlayer(playersNameToPromote).isOnline())
                                    {
                                       // TODO hier die Messages aus der Config einfuegen!  Auch bei den Fehlermeldungen!   
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
                           sender.sendMessage(TimedRanks.logPrefix + "Bitte den Ernennungs-Status des Spielers mit /tr status SPIELER pruefen.");
                        }
                        else
                        {
                           sender.sendMessage(TimedRanks.logPrefix + "This player is " + perm.getPrimaryGroup(nWorld, playersNameToPromote) + " and cannot be promoted further by " + plugin.getDescription().getName() + ".");
                           sender.sendMessage(TimedRanks.logPrefix + "Please check players promotion status using /tr status PLAYER.");
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

            // ADD TIME TO A PLAYERS CURRENTLY ACTIVE PROMOTION =======================================================================
            if (args[0].equalsIgnoreCase("add") || args[0].equalsIgnoreCase("gib"))
            {            
               if(sender.isOp() || sender.hasPermission("timedranks.manage"))
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

                     if(TimedRanksUtils.isInteger(args[2]))
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
                              
                              fileLogger.logPromotionStatusChange(sender.getName() + " ADDED " + daysToAdd + " days to promotion of player '" + playersNameToAddTime + "'.");
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

            // SUBTRACT TIME FROM A PLAYERS CURRENTLY ACTIVE PROMOTION =======================================================================
            if (args[0].equalsIgnoreCase("sub") || args[0].equalsIgnoreCase("nimm"))
            {                   
               if(sender.isOp() || sender.hasPermission("timedranks.manage"))
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

                     if(TimedRanksUtils.isInteger(args[2]))
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
                              
                              fileLogger.logPromotionStatusChange(sender.getName() + " SUBTRACTED " + daysToSubtract + " days from promotion of player '" + playersNameToSubtractTime + "'.");
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
            sender.sendMessage(ChatColor.YELLOW + TimedRanks.logPrefix + "Ungueltiger Befehl. Bitte alle Parameter angeben!");            
         }
      }

      return false; // No valid parameter count. If false is returned, the help for the command stated in the plugin.yml will be displayed to the player
   }

   // ##################################################################################################################

   /**
    * Paginates a string list to display it in chat page-by-page
    * 
    * @param Pass the first parameter as the sender.
    * @param The second parameter as the list with all entries.
    * @param The third as the page number to display.
    * @param The fifth as the count of all promoted players
    * @param The sixth as the count of all active promotions
    * @param The sixth as the count of all paused promotions
    */
   public void paginateList(CommandSender sender, ArrayList<String> list, int page, int countAll, int countActive, int countPaused)
   {

      int totalPageCount = 1;

      if((list.size() % contentLinesPerPage) == 0)
      {
         totalPageCount = list.size() / contentLinesPerPage;
      }
      else
      {
         totalPageCount = (list.size() / contentLinesPerPage) + 1;
      }

      if(sender instanceof Player)
      {
         sender.sendMessage(ChatColor.WHITE + "----------------------------------------------");
         sender.sendMessage(ChatColor.GREEN + "Liste ernannter Spieler - Seite (" + String.valueOf(page) + " von " + totalPageCount + ")");
         sender.sendMessage(ChatColor.WHITE + "----------------------------------------------");
      }
      else
      {
         sender.sendMessage("--------------------------------------------------------");
         sender.sendMessage("Liste ernannter Spieler - Seite (" + String.valueOf(page) + " von " + totalPageCount + ")");
         sender.sendMessage("--------------------------------------------------------");
      }

      if(list.isEmpty())
      {
         sender.sendMessage(ChatColor.WHITE + "Keine Eintraege.");
      }
      else
      {
         int i = 0, k = 0;
         page--;

         for (String entry : list)
         {
            k++;
            if ((((page * contentLinesPerPage) + i + 1) == k) && (k != ((page * contentLinesPerPage) + contentLinesPerPage + 1)))
            {
               i++;
               sender.sendMessage(entry);
            }
         }
      }

      if(sender instanceof Player)
      {
         sender.sendMessage(ChatColor.WHITE + "----------------------------------------------");
         sender.sendMessage(ChatColor.WHITE + "Ernannt Gesamt: " + ChatColor.YELLOW + countAll + ChatColor.WHITE + " | Aktiv: " + ChatColor.GREEN + countActive + ChatColor.WHITE + " | Pausiert: " + ChatColor.RED + countPaused);
         sender.sendMessage(ChatColor.WHITE + "----------------------------------------------");
      }
      else
      {
         sender.sendMessage("--------------------------------------------------------");
         sender.sendMessage("Ernannt Gesamt: " + countAll + " | Aktiv: " + countActive + " | Pausiert: " + countPaused);
         sender.sendMessage("--------------------------------------------------------");
      }
   }
}
