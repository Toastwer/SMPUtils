package me.twoaster.smputils;

import me.twoaster.smputils.commands.*;
import me.twoaster.smputils.commands.rank.RankCommand;
import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.*;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class CommandManager implements CommandExecutor, TabCompleter {

    private final SMPUtils main;
    private final HomeCommands homeCommands;
    private final TpaCommands tpaCommands;
    private final PlayTime playTime;
    private final MessageCommands messageCommands;
    private final SetSpawn setSpawn;
    private final InvSee invSee;
    private final RankCommand rankCommand;

    public boolean enableTpa;
    public boolean enableSetHome;
    public boolean enablePlayTime;
    public boolean enableMessaging;
    public boolean enableSetSpawn;
    public boolean enableInvSee;

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

        Objects.requireNonNull(main.getCommand("tpa")).setExecutor(tpaCommands);
        Objects.requireNonNull(main.getCommand("tpahere")).setExecutor(tpaCommands);
        Objects.requireNonNull(main.getCommand("tpah")).setExecutor(tpaCommands);
        Objects.requireNonNull(main.getCommand("tpaaccept")).setExecutor(tpaCommands);
        Objects.requireNonNull(main.getCommand("tpaa")).setExecutor(tpaCommands);
        Objects.requireNonNull(main.getCommand("tpadeny")).setExecutor(tpaCommands);
        Objects.requireNonNull(main.getCommand("tpad")).setExecutor(tpaCommands);

        Objects.requireNonNull(main.getCommand("home")).setExecutor(homeCommands);
        Objects.requireNonNull(main.getCommand("sethome")).setExecutor(homeCommands);

        Objects.requireNonNull(main.getCommand("playtime")).setExecutor(playTime);

        Objects.requireNonNull(main.getCommand("message")).setExecutor(messageCommands);
        Objects.requireNonNull(main.getCommand("msg")).setExecutor(messageCommands);
        Objects.requireNonNull(main.getCommand("reply")).setExecutor(messageCommands);
        Objects.requireNonNull(main.getCommand("r")).setExecutor(messageCommands);

        Objects.requireNonNull(main.getCommand("setspawn")).setExecutor(setSpawn);

        Objects.requireNonNull(main.getCommand("inventory")).setExecutor(invSee);
        Objects.requireNonNull(main.getCommand("invsee")).setExecutor(invSee);

        Objects.requireNonNull(main.getCommand("rank")).setExecutor(rankCommand);

        Objects.requireNonNull(main.getCommand("SMPUtils")).setExecutor(this);
    }

    private void loadConfig() {
        main.reloadConfig();

        enableTpa = getConfigSetting("enableTpa");
        enableSetHome = getConfigSetting("enableSetHome");
        enablePlayTime = getConfigSetting("enablePlayTime");
        enableMessaging = getConfigSetting("enableMessaging");
        enableSetSpawn = getConfigSetting("enableSetSpawn");
        enableInvSee = getConfigSetting("enableInvSee");
    }

    private boolean getConfigSetting(String setting) {
        if (main.getConfig().contains(setting, true))
            return main.getConfig().getBoolean(setting);

        main.getConfig().set(setting, false);
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
        if (label.equalsIgnoreCase("SMPUtils") && args.length == 1)
            return Arrays.asList("reload", "download");

        return null;
    }

    public PlayTime getPlayTime() {
        return playTime;
    }
}
