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
        { // If the player typed /plague then do the following... (can be run from console also)
            if (args.length == 0)
            { //no arguments, so help will be displayed
                return false;
            }
            if (args.length==1)
            {
                if (args[0].equalsIgnoreCase("version")) // argument 0 is given and correct
                {            
                    sender.sendMessage(ChatColor.GREEN + "This server is running " + plugin.getDescription().getName() + " " + plugin.getDescription().getVersion());                    
                    return true;
                }    
                if (args[0].equalsIgnoreCase("reload")) // argument 0 is given and correct
                {            
                    if(sender.hasPermission("timedranks.admin"))
                    {
                        cHandler.reloadConfig(sender);                        
                        return true;
                    }
                    else
                    {
                        sender.sendMessage(ChatColor.RED + "You du not have sufficient permission to reload " + plugin.getDescription().getName() + "!");
                    }
                }

            }
            if (args.length==3)
            {
                if (args[0].equalsIgnoreCase("promote")) // argument 0 is given and correct
                {   //TODO Befehl wie "promote NAME set TIME" und auch "promote NAME add TIME" sollte möglich sein. (Zeit in Tagen erstmal nur)      
                    if(sender.hasPermission("timedranks.promote")) //TODO schlüssel so machen, das es sich die Perm aus der Config zusammenstellt je nach den dort anegegebenen Gruppen
                        // TODO evt. mehrere PFade oder Gruppenränge in Config und Permissions definieren um z.B. zu setzen welche Ränge promotebar sind.
                        // TODO Muster: baseGroup1:Member promoteGroup1:VIP baseGroup2:Moderator promoteGroup2:VIPModerator oder so ähnlich
                        //TODO und die Permission dann so: timedranks.promote.member timedranks.promote.moderator um aus Membern VIPs und Mods VIPMods zu machen
                    {
                        String playerToPromote = args[1];
                        String promotionTime = args[2];
                        
                        String[] promoteMemberToGroup = {"VIP"}; //TODO das hier anfangs aus der Config lesen!! evt. Hashmap...
                        
                        try
                        {
                            PermissionUser permPlayer  = PermissionsEx.getUser(playerToPromote);
                            // TODO Achtung: Wenn der Spieler nicht in der permissions.yml existiert, wird er automatisch angelegt!
                            // TODO deswegen vorher die Existenz prüfen und dann Fehler ausgeben!!
                            permPlayer.setGroups(promoteMemberToGroup);
                            sender.sendMessage(ChatColor.GREEN + "Spieler: " + ChatColor.YELLOW + playerToPromote + ChatColor.GREEN + " wird fuer " + promotionTime + " Tage in den Rang: " + ChatColor.YELLOW + promoteMemberToGroup[0] + ChatColor.GREEN +" gesetzt.");
                        }
                        catch (Exception ex)
                        {
                            sender.sendMessage(ChatColor.YELLOW + "Fehler beim promoten dieses Spielers!");
                            log.severe(ex.getMessage());
                        }

                        return true;
                    }
                    else
                    {
                        sender.sendMessage(ChatColor.RED + "Du hast keine Berechtigung fuer diese Ernennung.");
                    }
                }
            }
            else
            {
                sender.sendMessage(ChatColor.YELLOW + "Falsche Anzahl an Parametern.");
            }                

        }         
        return false; // if false is returned, the help for the command stated in the plugin.yml will be displayed to the player
    }
}
