package me.twoaster.smputils.commands;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.twoaster.smputils.CommandManager;
import me.twoaster.smputils.SMPUtils;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.advancement.Advancement;
import org.bukkit.advancement.AdvancementProgress;
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
        if (!commandManager.enableSeeAdvancements) {
            main.sendMessage(sender, "§c/seeAdvancements is currently disabled");
            return true;
        }

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

        List<Advancement> completed = new ArrayList<>();
        List<Advancement> unCompleted = new ArrayList<>();
        List<Advancement> notStarted = new ArrayList<>();

        Map<String, Collection<String>> awardedCriteria = new HashMap<>();

        if (target.isOnline()) {
            Player player = target.getPlayer();

            Iterator<Advancement> advancements = main.getServer().advancementIterator();

            while (advancements.hasNext()) {
                Advancement advancement = advancements.next();
                if (advancement.getDisplay() == null) {
                    continue;
                }

                AdvancementProgress progress = Objects.requireNonNull(player).getAdvancementProgress(advancement);

                if (progress.isDone()) {
                    completed.add(advancement);
                } else if (progress.getAwardedCriteria().size() > 0) {
                    unCompleted.add(advancement);
                    awardedCriteria.put(advancement.getKey().toString(), progress.getAwardedCriteria());
                } else {
                    notStarted.add(advancement);
                }
            }
        } else {
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

                        JsonObject criteria = advancement.get("criteria").getAsJsonObject();

                        Set<String> awarded = new HashSet<>(criteria.keySet());
                        awardedCriteria.put(key, awarded);
                    }
                }
            }

            Iterator<Advancement> advancements = main.getServer().advancementIterator();

            while (advancements.hasNext()) {
                Advancement advancement = advancements.next();
                if (advancement.getDisplay() == null) {
                    continue;
                }

                String key = advancement.getKey().toString();
                if (completedKeys.contains(key)) {
                    completed.add(advancement);
                } else if (unCompletedKeys.contains(key)) {
                    unCompleted.add(advancement);
                } else {
                    notStarted.add(advancement);
                }
            }
        }

        List<BaseComponent> out = new ArrayList<>();

        out.add(new TextComponent("§f" + target.getName() + " their advancements:\n"));

        out.add(new TextComponent("§8--- §b" + target.getName() + " has " + completed.size() + " completed advancement" + (completed.size() == 1 ? "" : "s") + " §8---§r\n"));
        for (Advancement advancement : completed) {
            out.add(new TextComponent("§7- " + (Objects.requireNonNull(advancement.getDisplay()).isHidden() ? "§a§o" : "§a") + advancement.getDisplay().getTitle() + "§r\n"));
        }

        out.add(new TextComponent("§8--- §b" + target.getName() + " has " + unCompleted.size() + " uncompleted advancement" + (unCompleted.size() == 1 ? "" : "s") + " §8---§r\n"));
        for (Advancement advancement : unCompleted) {
            Collection<String> awarded = awardedCriteria.get(advancement.getKey().toString());

            StringBuilder missingCriteria = new StringBuilder();
            for (String criteria : advancement.getCriteria()) {
                if (!awarded.contains(criteria)) {
                    String name = criteria.startsWith("minecraft:") ? criteria.substring(10) : criteria;
                    missingCriteria.append("\n§7- §f").append(name);
                }
            }

            TextComponent completionDisplay = new TextComponent("§7(" + awarded.size() + "/" + advancement.getCriteria().size() + ")");
            completionDisplay.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("§bMissing Criteria: " + missingCriteria).create()));

            out.add(new TextComponent("§7- " + (Objects.requireNonNull(advancement.getDisplay()).isHidden() ? "§e§o" : "§e") + advancement.getDisplay().getTitle() + " "));
            out.add(completionDisplay);
            out.add(new TextComponent("§r\n"));
        }

        out.add(new TextComponent("§8--- §b" + target.getName() + " has " + notStarted.size() + " unstarted advancement" + (notStarted.size() == 1 ? "" : "s") + " §8---§r\n"));
        for (Advancement advancement : notStarted) {
            out.add(new TextComponent("§7- " + (Objects.requireNonNull(advancement.getDisplay()).isHidden() ? "§c§o" : "§c") + advancement.getDisplay().getTitle() + "§r\n"));
        }

        int totalSize = completed.size() + unCompleted.size() + notStarted.size();
        out.add(new TextComponent("§8--- §fThey have §6" + Math.round(completed.size() * 100f / totalSize) + "%§f advancement completion §8---§r"));

        main.sendMessage(sender, out.toArray(new BaseComponent[0]));

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
