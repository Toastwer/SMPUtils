package me.twoaster.smputils.commands;

import me.twoaster.smputils.CommandManager;
import me.twoaster.smputils.SMPUtils;
import me.twoaster.smputils.utils.Converter;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static me.twoaster.smputils.SMPUtils.findOfflinePlayer;

public class PlayTime implements CommandExecutor, TabCompleter {

    private final SMPUtils main;
    private final CommandManager commandManager;

    private final Map<String, String> playTime;
    private final Map<UUID, Long> session;

    private YamlConfiguration config;

    public PlayTime(SMPUtils main, CommandManager commandManager) {
        this.main = main;
        this.commandManager = commandManager;

        reloadConfig();

        session = new HashMap<>();
        playTime = Converter.stringListToMap(config.getStringList("playtime"));
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!commandManager.enablePlayTime) {
            main.sendMessage(sender, "§c/playtime is currently disabled");
            return true;
        }

        if (label.equalsIgnoreCase("playtime")) {
            if (args.length > 0) {
                OfflinePlayer target = findOfflinePlayer(args[0]);
                if (target != null) {
                    long time = 0;
                    if (playTime.containsKey(target.getUniqueId().toString()))
                        time = Long.parseLong(playTime.get(target.getUniqueId().toString()));

                    if (session.containsKey(target.getUniqueId()))
                        time += (new Date().getTime() - session.get(target.getUniqueId()));

                    String type = Objects.requireNonNull(target.getName()).equalsIgnoreCase(sender.getName()) ? "Your" : "Their";
                    if (args.length > 1) {
                        switch (args[1]) {
                            case "days":
                            case "d":
                                main.sendMessage(sender, "§f" + type + " playtime so far is: §6" + TimeUnit.MILLISECONDS.toDays(time) + "§f days");
                                break;
                            case "hours":
                            case "h":
                                main.sendMessage(sender, "§f" + type + " playtime so far is: §6" + TimeUnit.MILLISECONDS.toHours(time) + "§f hours");
                                break;
                            case "minutes":
                            case "m":
                                main.sendMessage(sender, "§f" + type + " playtime so far is: §6" + TimeUnit.MILLISECONDS.toMinutes(time) + "§f minutes");
                                break;
                            case "seconds":
                            case "s":
                                main.sendMessage(sender, "§f" + type + " playtime so far is: §6" + TimeUnit.MILLISECONDS.toSeconds(time) + "§f seconds");
                                break;
                            case "milliseconds":
                            case "ms":
                                main.sendMessage(sender, "§f" + type + " playtime so far is: §6" + TimeUnit.MILLISECONDS.toMillis(time) + "§f milliseconds");
                                break;
                            default:
                                main.sendMessage(sender, "§cThe timeunit '" + args[0] + "' is invalid. Options are: days, hours, minutes, seconds, milliseconds");
                                break;
                        }
                    } else {
                        main.sendMessage(sender, "§f" + type + " playtime so far is: §6" + TimeUnit.MILLISECONDS.toHours(time) + "§f hours");
                    }
                } else {
                    main.sendMessage(sender, "§cThe player §o" + args[0] + "§c could not be found");
                }
            } else {
                if (!(sender instanceof Player)) {
                    main.getLogger().severe("You must specify a player to use this command");
                    return true;
                }

                Player player = (Player) sender;

                long time = 0;
                if (playTime.containsKey(player.getUniqueId().toString()))
                    time = Long.parseLong(playTime.get(player.getUniqueId().toString()));

                time += (new Date().getTime() - session.get(player.getUniqueId()));

                main.sendMessage(player, "§fYour playtime so far is: §6" + TimeUnit.MILLISECONDS.toHours(time) + "§f hours");
            }
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (label.equalsIgnoreCase("playtime")) {
            if (args.length == 1) {
                List<String> result = new ArrayList<>();

                OfflinePlayer[] players = Bukkit.getOfflinePlayers();
                for (OfflinePlayer player : players)
                    if (player.getName() != null)
                        result.add(player.getName());

                return result;
            } else if (args.length == 2) {
                return Arrays.asList("days", "d", "hours", "h", "minutes", "m", "seconds", "s", "milliseconds", "ms");
            }
        }

        return new ArrayList<>();
    }

    public void startSession(UUID uuid) {
        session.put(uuid, new Date().getTime());
    }

    public void endSession(UUID uuid) {
        long time = 0;
        if (playTime.containsKey(uuid.toString()))
            time = Long.parseLong(playTime.get(uuid.toString()));

        time += (new Date().getTime() - session.get(uuid));

        playTime.put(uuid.toString(), String.valueOf(time));

        config.set("playtime", Converter.mapToStringList(playTime));
        saveConfig();
    }

    private void reloadConfig() {
        main.saveResource("playtime.yml", false);

        config = YamlConfiguration.loadConfiguration(new File(main.getDataFolder(), "playtime.yml"));
    }

    private void saveConfig() {
        if (!(new File(main.getDataFolder(), "playtime.yml").exists())) {
            try {
                if (!new File(main.getDataFolder(), "playtime.yml").createNewFile())
                    main.getServer().getLogger().warning("Something went wrong while creating the 'playtime.yml' file");
            } catch (IOException exception) {
                exception.printStackTrace();
            }
        }

        try {
            config.save(new File(main.getDataFolder(), "playtime.yml"));
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }
}
