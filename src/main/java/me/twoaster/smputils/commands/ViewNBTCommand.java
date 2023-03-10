package me.twoaster.smputils.commands;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import me.twoaster.smputils.CommandManager;
import me.twoaster.smputils.SMPUtils;
import me.twoaster.smputils.utils.NBTUtil;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ViewNBTCommand implements CommandExecutor, TabCompleter {

    private final SMPUtils main;
    private final CommandManager commandManager;

    public ViewNBTCommand(SMPUtils main, CommandManager commandManager) {
        this.main = main;
        this.commandManager = commandManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            main.getLogger().severe("Only players can use this command");
            return true;
        }

        if (!commandManager.enableViewNBT) {
            main.sendMessage(sender, "§c/viewnbt is currently disabled");
            return true;
        }

        Player player = (Player) sender;

        boolean isItem;
        Map<String, String> nbt;
        if (player.getInventory().getItemInMainHand().getType() != Material.AIR) {
            nbt = NBTUtil.getNBTData(player.getInventory().getItemInMainHand());
            isItem = true;
        } else {
            nbt = NBTUtil.getNBTData(player.getTargetBlock(null, 0));
            isItem = false;
        }

        if (nbt.isEmpty()) {
            main.sendMessage(sender, "§eThat " + (isItem ? "item" : "block") + " does not have any NBT data");
            return true;
        }

        StringBuilder out = new StringBuilder();

        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        out.append("§fThe NBTData of that ").append(isItem ? "item" : "block").append(" is:");
        for (Map.Entry<String, String> entry : nbt.entrySet()) {
            if (entry.getValue().startsWith("[") || entry.getValue().startsWith("{")) {
                JsonElement jsonElement = JsonParser.parseString(entry.getValue());

                out.append("\n§7- §f").append(entry.getKey()).append("§7:\n  §e").append(gson.toJson(jsonElement).replace("\n", "\n  "));
            } else {
                out.append("\n§7- §f").append(entry.getKey()).append("§7: §b").append(entry.getValue());
            }
        }

        main.sendMessage(sender, out.toString());

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        return new ArrayList<>();
    }
}
