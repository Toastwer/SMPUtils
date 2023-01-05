package me.twoaster.smputils.commands;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.twoaster.smputils.CommandManager;
import me.twoaster.smputils.SMPUtils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.advancement.Advancement;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static me.twoaster.smputils.SMPUtils.findOfflinePlayer;

public class SeeAdvancementsCommand implements CommandExecutor, TabCompleter {
    private final SMPUtils main;
    private final CommandManager commandManager;

    public SeeAdvancementsCommand(SMPUtils main, CommandManager commandManager) {
        this.main = main;
        this.commandManager = commandManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        OfflinePlayer target;

        if (args.length == 0) {
            if (!(sender instanceof Player)) {
                main.getLogger().severe("Only players can use this command");
                return true;
            }

            target = (Player) sender;
        } else {
            OfflinePlayer player = findOfflinePlayer(args[0]);
            if (player == null) {
                main.sendMessage(sender, "§cThe player §o" + args[0] + "§c cannot be found");
                return true;
            }

            target = player;
        }

        String jsonString = null;
        try {
            String absolute = main.getDataFolder().getAbsolutePath();
            String[] parts = absolute.split("[\\\\/]");

            StringBuilder path = new StringBuilder();
            for (int i = 0; i < parts.length - 2; i++) {
                path.append(parts[i]).append("/");
            }
            path.append(main.getServer().getWorlds().get(0).getName()).append("/advancements/").append(target.getUniqueId()).append(".json");

            jsonString = Files.readString(Path.of(path.toString()));
        } catch (IOException ignored) {
        }

        Set<String> completedKeys = new HashSet<>();
        Set<String> unCompletedKeys = new HashSet<>();
        Map<String, Integer> unCompleteStatus = new HashMap<>();

        if (jsonString != null) {
            JsonObject json = JsonParser.parseString(jsonString).getAsJsonObject();

            for (String key : json.keySet()) {
                if (!json.get(key).isJsonObject()) {
                    continue;
                }

                JsonObject advancement = json.getAsJsonObject(key);

                if (advancement.get("done").getAsBoolean()) {
                    completedKeys.add(key);
                } else {
                    unCompletedKeys.add(key);
                    unCompleteStatus.put(key, advancement.get("criteria").getAsJsonObject().size());
                }
            }
        }

        Iterator<Advancement> advancements = main.getServer().advancementIterator();

        List<Advancement> completed = new ArrayList<>();
        List<Advancement> unCompleted = new ArrayList<>();
        List<Advancement> notStarted = new ArrayList<>();

        while (advancements.hasNext()) {
            Advancement advancement = advancements.next();
            if (advancement.getDisplay() == null) {
                continue;
            }

            if (completedKeys.contains(advancement.getKey().toString())) {
                completed.add(advancement);
            } else if (unCompletedKeys.contains(advancement.getKey().toString())) {
                unCompleted.add(advancement);
            } else {
                notStarted.add(advancement);
            }
        }

        StringBuilder out = new StringBuilder();

        out.append("§8--- §b").append(target.getName()).append(" has ").append(completed.size()).append(" completed advancement").append(completed.size() == 1 ? "" : "s").append(" §8---§r\n");
        for (Advancement advancement : completed) {
            out.append("§7- ").append(Objects.requireNonNull(advancement.getDisplay()).isHidden() ? "§a§o" : "§a").append(advancement.getDisplay().getTitle()).append("§r\n");
        }

        out.append("§8--- §b").append(target.getName()).append(" has ").append(unCompleted.size()).append(" uncompleted advancement").append(unCompleted.size() == 1 ? "" : "s").append(" §8---§r\n");
        for (Advancement advancement : unCompleted) {
            out.append("§7- ").append(Objects.requireNonNull(advancement.getDisplay()).isHidden() ? "§e§o" : "§e").append(advancement.getDisplay().getTitle()).append(" §7(").append(unCompleteStatus.get(advancement.getKey().toString())).append("/").append(advancement.getCriteria().size()).append(")").append("§r\n");
        }

        out.append("§8--- §b").append(target.getName()).append(" has ").append(notStarted.size()).append(" unstarted advancement").append(notStarted.size() == 1 ? "" : "s").append(" §8---§r\n");
        for (Advancement advancement : notStarted) {
            out.append("§7- ").append(Objects.requireNonNull(advancement.getDisplay()).isHidden() ? "§c§o" : "§c").append(advancement.getDisplay().getTitle()).append("§r\n");
        }

        int totalSize = completed.size() + unCompleted.size() + notStarted.size();
        out.append("§8--- §bThey have ").append(Math.round(completed.size() * 100f / totalSize)).append("% completion §8---§r");

        main.sendMessage(sender, out.toString());

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        List<String> result = new ArrayList<>();

        if (args.length == 1) {
            OfflinePlayer[] players = Bukkit.getOfflinePlayers();
            for (OfflinePlayer player : players)
                if (player.getName() != null)
                    result.add(player.getName());
        }

        return result;
    }
}
