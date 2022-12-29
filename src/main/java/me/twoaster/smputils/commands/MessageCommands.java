package me.twoaster.smputils.commands;

import me.twoaster.smputils.SMPUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.*;

public class MessageCommands implements CommandExecutor, TabCompleter {

    private final SMPUtils main;
    private final CommandManager commandManager;

    private final Map<String, CommandSender> lastMessage;

    public MessageCommands(SMPUtils main, CommandManager commandManager) {
        this.main = main;
        this.commandManager = commandManager;

        lastMessage = new HashMap<>();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!commandManager.enableMessaging) {
            main.sendMessage(sender, "§c/message is currently disabled");
            return true;
        }

        if (label.equalsIgnoreCase("message") || label.equalsIgnoreCase("msg")) {
            if (args.length > 0) {
                Player target = Bukkit.getPlayer(args[0]);
                if (target != null && target.isOnline()) {
                    if (args.length == 1) {
                        main.sendMessage(sender, "§cExpected a message");
                        return true;
                    }

                    StringBuilder message = new StringBuilder();
                    message.append(args[1]);
                    for (int i = 2; i < args.length; i++) {
                        message.append(" ").append(args[i]);
                    }

                    String colored = ChatColor.translateAlternateColorCodes('&', message.toString());
                    sender.sendMessage("§dTo §f" + target.getName() + " §8§l>> §f" + colored);
                    target.sendMessage("§dFrom §f" + sender.getName() + " §8§l>> §f" + colored);

                    lastMessage.put(target.getName(), sender);
                } else {
                    main.sendMessage(sender, "§cThe player §o" + args[0] + "§c is not online");
                }
            } else {
                main.sendMessage(sender, "§cExpected a player to send the message to");
            }
        } else if (label.equalsIgnoreCase("reply") || label.equalsIgnoreCase("r")) {
            if (lastMessage.containsKey(sender.getName())) {
                if (args.length == 0) {
                    main.sendMessage(sender, "§cExpected a message");
                    return true;
                }

                CommandSender target = lastMessage.get(sender.getName());

                StringBuilder message = new StringBuilder();
                message.append(args[0]);
                for (int i = 1; i < args.length; i++) {
                    message.append(" ").append(args[i]);
                }

                String colored = ChatColor.translateAlternateColorCodes('&', message.toString());
                sender.sendMessage("§dTo §f" + target.getName() + " §8§l>> §f" + colored);
                target.sendMessage("§dFrom §f" + sender.getName() + " §8§l>> §f" + colored);

                lastMessage.put(target.getName(), sender);
            } else {
                main.sendMessage(sender, "§eThere is no message to reply to");
            }
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if ((label.equalsIgnoreCase("message") || label.equalsIgnoreCase("msg")) && args.length == 1) {
            Collection<? extends Player> players = Bukkit.getOnlinePlayers();

            List<String> response = new ArrayList<>();
            for (Player player : players)
                if (!player.getName().equalsIgnoreCase(sender.getName()))
                    response.add(player.getName());

            return response;
        }

        return new ArrayList<>();
    }
}
