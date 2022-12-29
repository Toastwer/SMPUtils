package me.twoaster.smputils.commands;

import me.twoaster.smputils.SMPUtils;
import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.*;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class CommandManager implements CommandExecutor, TabCompleter {

    private final SMPUtils main;
    private final HomeCommands homeCommands;
    private final TpaCommands tpaCommands;

    public boolean enableTpa;
    public boolean enableSetHome;

    public CommandManager(SMPUtils main) {
        this.main = main;

        enableTpa = main.getConfig().getBoolean("enableTpa");
        enableSetHome = main.getConfig().getBoolean("enableSetHome");

        homeCommands = new HomeCommands(main, this);
        tpaCommands = new TpaCommands(main, this);

        Objects.requireNonNull(main.getCommand("tpa")).setExecutor(tpaCommands);
        Objects.requireNonNull(main.getCommand("tpahere")).setExecutor(tpaCommands);
        Objects.requireNonNull(main.getCommand("tpah")).setExecutor(tpaCommands);
        Objects.requireNonNull(main.getCommand("tpaaccept")).setExecutor(tpaCommands);
        Objects.requireNonNull(main.getCommand("tpaa")).setExecutor(tpaCommands);
        Objects.requireNonNull(main.getCommand("tpadeny")).setExecutor(tpaCommands);
        Objects.requireNonNull(main.getCommand("tpad")).setExecutor(tpaCommands);

        Objects.requireNonNull(main.getCommand("home")).setExecutor(homeCommands);
        Objects.requireNonNull(main.getCommand("sethome")).setExecutor(homeCommands);

        Objects.requireNonNull(main.getCommand("SMPUtils")).setExecutor(this);
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
                    main.reloadConfig();

                    enableTpa = main.getConfig().getBoolean("enableTpa");
                    enableSetHome = main.getConfig().getBoolean("enableSetHome");

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
            return true;
        }

        return false;
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (label.equalsIgnoreCase("SMPUtils") && args.length == 1)
            return Collections.singletonList("reload");

        return null;
    }
}
