package me.twoaster.smputils.commands;

import me.twoaster.smputils.CommandManager;
import me.twoaster.smputils.SMPUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;

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

            if (args.length >= 2) {
                if (args[1].equalsIgnoreCase("raw")) {
                    player.openInventory(target.getInventory());
                    return true;
                }
            }

            Inventory invsee = main.getServer().createInventory(player, 54, "Inventory of: " + target.getDisplayName());

            ItemStack filler = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
            ItemMeta itemMeta = filler.getItemMeta();
            itemMeta.setDisplayName(" ");
            filler.setItemMeta(itemMeta);

            PlayerInventory inventory = target.getInventory();
            invsee.setItem(0, inventory.getHelmet());
            invsee.setItem(1, inventory.getChestplate());
            invsee.setItem(2, inventory.getLeggings());
            invsee.setItem(3, inventory.getBoots());

            invsee.setItem(4, filler);
            invsee.setItem(5, filler);
            invsee.setItem(6, filler);
            invsee.setItem(7, filler);

            invsee.setItem(8, inventory.getItemInOffHand());

            invsee.setItem(9, filler);
            invsee.setItem(10, filler);
            invsee.setItem(11, filler);
            invsee.setItem(12, filler);
            invsee.setItem(13, filler);
            invsee.setItem(14, filler);
            invsee.setItem(15, filler);
            invsee.setItem(16, filler);
            invsee.setItem(17, filler);

            int length = inventory.getStorageContents().length;
            for (int i = 9; i < length; i++) {
                ItemStack item = inventory.getStorageContents()[i];
                invsee.setItem(i + 9, item);
            }
            for (int i = 0; i < 9; i++) {
                ItemStack item = inventory.getStorageContents()[i];
                invsee.setItem(i + 45, item);
            }

            player.openInventory(invsee);
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
