package me.twoaster.smputils.commands;

import me.twoaster.smputils.SMPUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class SetSpawn implements CommandExecutor, TabCompleter {

    private final SMPUtils main;
    private final CommandManager commandManager;

    public SetSpawn(SMPUtils main, CommandManager commandManager) {
        this.main = main;
        this.commandManager = commandManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            main.getLogger().severe("Only players can use this command");
            return true;
        }

        if (!commandManager.enableSetSpawn) {
            main.sendMessage(sender, "§c/setspawn is currently disabled");
            return true;
        }

        if (label.equalsIgnoreCase("setspawn")) {
            Player player = (Player) sender;

            Location loc = player.getLocation();

            ConsoleCommandSender console = main.getServer().getConsoleSender();
            Bukkit.dispatchCommand(console, "spawnpoint " + player.getName() + " " + loc.getBlockX() + " " + loc.getBlockY() + " " + loc.getBlockZ() + " " + loc.getYaw());
            main.sendMessage(player, "§aYour spawn has successfully been set to your current position");
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        return new ArrayList<>();
    }
}
