package me.twoaster.smputils;

import me.twoaster.smputils.commands.*;
import me.twoaster.smputils.commands.rank.RankCommand;
import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.*;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.*;

public class CommandManager implements CommandExecutor, TabCompleter {

    private final SMPUtils main;
    private final HomeCommands homeCommands;
    private final TpaCommands tpaCommands;
    private final PlayTime playTime;
    private final MessageCommands messageCommands;
    private final SetSpawn setSpawn;
    private final InvSee invSee;
    private final RankCommand rankCommand;
    private final SeeAdvancementsCommand seeAdvancementsCommand;
    private final WarpCommands warpCommands;

    public boolean enableTpa;
    public boolean enableSetHome;
    public boolean enablePlayTime;
    public boolean enableMessaging;
    public boolean enableSetSpawn;
    public boolean enableInvSee;
    public boolean enableSeeAdvancements;
    public boolean enableWarp;

    public CommandManager(SMPUtils main) {
        this.main = main;

        loadConfig();

        homeCommands = new HomeCommands(main, this);
        tpaCommands = new TpaCommands(main, this);
        playTime = new PlayTime(main, this);
        messageCommands = new MessageCommands(main, this);
        setSpawn = new SetSpawn(main, this);
        invSee = new InvSee(main, this);
        rankCommand = new RankCommand(main, this);
        seeAdvancementsCommand = new SeeAdvancementsCommand(main, this);
        warpCommands = new WarpCommands(main, this);

        getCommand("tpa").setExecutor(tpaCommands);
        getCommand("tpahere").setExecutor(tpaCommands);
        getCommand("tpah").setExecutor(tpaCommands);
        getCommand("tpaaccept").setExecutor(tpaCommands);
        getCommand("tpaa").setExecutor(tpaCommands);
        getCommand("tpadeny").setExecutor(tpaCommands);
        getCommand("tpad").setExecutor(tpaCommands);

        getCommand("home").setExecutor(homeCommands);
        getCommand("sethome").setExecutor(homeCommands);

        getCommand("playtime").setExecutor(playTime);

        getCommand("message").setExecutor(messageCommands);
        getCommand("msg").setExecutor(messageCommands);
        getCommand("reply").setExecutor(messageCommands);
        getCommand("r").setExecutor(messageCommands);

        getCommand("setspawn").setExecutor(setSpawn);

        getCommand("inventory").setExecutor(invSee);
        getCommand("invsee").setExecutor(invSee);

        getCommand("rank").setExecutor(rankCommand);

        getCommand("seeAdvancements").setExecutor(seeAdvancementsCommand);

        getCommand("warp").setExecutor(warpCommands);
        getCommand("setwarp").setExecutor(warpCommands);

        getCommand("SMPUtils").setExecutor(this);
    }

    private PluginCommand getCommand(String name) {
        PluginCommand command = main.getCommand(name);

        if (command == null || !command.isRegistered()) {
            throw new RuntimeException("The command '" + name + "' is not registerd");
        }

        return command;
    }

    private void loadConfig() {
        main.reloadConfig();

        for (Field field : getClass().getDeclaredFields()) {
            if (field.getName().startsWith("enable")) {
                try {
                    field.set(this, getConfigSetting(field.getName()));
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    private boolean getConfigSetting(String setting) {
        if (main.getConfig().contains(setting, true))
            return main.getConfig().getBoolean(setting);

        main.getConfig().set(setting, false);
        main.saveConfig();
        return false;
    }

    private boolean setConfigSetting(String setting, boolean value) {
        main.getConfig().set(setting, value);
        main.saveConfig();
        return false;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (label.equalsIgnoreCase("SMPUtils")) {
            if (!sender.isOp()) {
                main.sendMessage(sender, "§cYou do not have the required permissions to do that");
                return true;
            }

            if (args.length > 0) {
                if (args[0].equalsIgnoreCase("reload")) {
                    loadConfig();

                    main.rankManager.reloadConfig();
                    main.rankManager.loadRanks();
                    main.rankManager.loadPlayers();

                    main.sendMessage(sender, "§aConfig has been reloaded");
                } else if (args[0].equalsIgnoreCase("download")) {
                    final String githubURL = "https://github.com/Toastwer/SMPUtils/releases/latest/download/SMPUtils.jar";

                    try {
                        URL website = new URL(githubURL);
                        FileUtils.copyURLToFile(website, new File(main.getDataFolder().getAbsolutePath() + ".jar"));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }

                    ConsoleCommandSender console = main.getServer().getConsoleSender();
                    Bukkit.dispatchCommand(console, "reload confirm");
                } else if (args[0].equalsIgnoreCase("toggleSetting")) {
                    Map<String, Field> options = new HashMap<>();

                    for (Field field : getClass().getDeclaredFields()) {
                        if (field.getName().startsWith("enable"))
                            options.put(field.getName().toLowerCase(), field);
                    }

                    if (!options.containsKey(args[1].toLowerCase())) {
                        main.sendMessage(sender, "§cThat setting does not exist");
                        return true;
                    }

                    boolean newValue = !getConfigSetting(options.get(args[1].toLowerCase()).getName());
                    setConfigSetting(options.get(args[1].toLowerCase()).getName(), newValue);

                    try {
                        options.get(args[1].toLowerCase()).set(this, newValue);
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }

                    main.sendMessage(sender, "§fThe setting §6" + options.get(args[1].toLowerCase()).getName() + "§f has been" + (newValue ? "§a enabled" : "§c disabled"));
                } else {
                    main.sendMessage(sender, "§cInvalid argument");
                }
            } else {
                main.sendMessage(sender, "§cInvalid command, expected an argument");
            }
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (label.equalsIgnoreCase("SMPUtils")) {
            if (args.length == 1) {
                return Arrays.asList("reload", "download", "toggleSetting");
            } else if (args.length == 2 && args[0].equalsIgnoreCase("toggleSetting")) {
                List<String> options = new ArrayList<>();

                for (Field field : getClass().getDeclaredFields()) {
                    if (field.getName().startsWith("enable"))
                        options.add(field.getName());
                }

                return options;
            }
        }

        return new ArrayList<>();
    }

    public PlayTime getPlayTime() {
        return playTime;
    }
}
