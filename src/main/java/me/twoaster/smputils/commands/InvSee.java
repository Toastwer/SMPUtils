package me.twoaster.smputils.commands;

import me.twoaster.smputils.CommandManager;
import me.twoaster.smputils.SMPUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

import static me.twoaster.smputils.SMPUtils.getOnlinePlayersExcept;

public class InvSee implements CommandExecutor, TabCompleter {

    private final SMPUtils main;
    private final CommandManager commandManager;

    public InvSee(SMPUtils main, CommandManager commandManager) {
        this.main = main;
        this.commandManager = commandManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            main.getLogger().severe("Only players can use this command");
            return true;
        }

        if (!commandManager.enableInvSee) {
            main.sendMessage(sender, "§c/invsee is currently disabled");
            return true;
        }

        if (label.equalsIgnoreCase("invsee") || label.equalsIgnoreCase("inventory")) {
            if (args.length < 1) {
                main.sendMessage(sender, "§cExpected a player to see the inventory of");
                return true;
            }

            Player player = (Player) sender;

            if (!player.isOp()) {
                main.sendMessage(sender, "§cYou do not have permission to do this");
                return true;
            }

            Player target = Bukkit.getPlayer(args[0]);
            if (target == null || !target.isOnline()) {
                main.sendMessage(sender, "§cThe player §o" + args[0] + "§c is not online");
                return true;
            }

            if (target.getName().equalsIgnoreCase(player.getName())) {
                main.sendMessage(sender, "§cYou cannot see your own inventory");
                return true;
            }

            player.openInventory(target.getInventory());
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1) {
            return getOnlinePlayersExcept(sender.getName());
        }

        return new ArrayList<>();
    }
}
