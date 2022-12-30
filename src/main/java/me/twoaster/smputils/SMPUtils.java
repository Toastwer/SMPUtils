package me.twoaster.smputils;

import me.twoaster.smputils.commands.CommandManager;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

public final class SMPUtils extends JavaPlugin {

    // TODO:
    // - invsee
    // - sleeping percentage

    public EventListener eventListener;
    public CommandManager commandManager;

    @Override
    public void onEnable() {
        eventListener = new EventListener(this);
        getServer().getPluginManager().registerEvents(eventListener, this);

        saveResource("config.yml", false);
        saveResource("home.yml", false);
        saveResource("playtime.yml", false);

        commandManager = new CommandManager(this);

        for (Player player : Bukkit.getOnlinePlayers()) {
            commandManager.getPlayTime().startSession(player.getUniqueId());
        }

        Bukkit.getConsoleSender().sendMessage("§8[§bSMPUtils§8] §l>> §aSMPUtils has successfully started");
    }

    @Override
    public void onDisable() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            commandManager.getPlayTime().endSession(player.getUniqueId());
        }

        Bukkit.getConsoleSender().sendMessage("§8[§bSMPUtils§8] §l>> §cSMPUtils has been disabled");
    }

    public void sendMessage(CommandSender sender, String message) {
        sender.sendMessage("§8[§bSMPUtils§8] §l>>§7 " + message);
    }

    public void sendMessage(CommandSender sender, BaseComponent... message) {
        BaseComponent[] arr = new BaseComponent[message.length + 1];
        arr[0] = new TextComponent("§8[§bSMPUtils§8] §l>>§7 ");
        System.arraycopy(message, 0, arr, 1, message.length);
        sender.spigot().sendMessage(arr);
    }

    public void saveResource(@NonNull String resourcePath, boolean replace) {
        if (resourcePath.equals("")) {
            throw new IllegalArgumentException("ResourcePath cannot be null or empty");
        }

        try {
            Files.createDirectories(Paths.get(resourcePath));
        } catch (IOException e) {
            throw new IllegalArgumentException("The path " + resourcePath + " cannot be created");
        }

        resourcePath = resourcePath.replace('\\', '/');
        InputStream in = getResource(resourcePath);
        if (in == null) {
            throw new IllegalArgumentException("The embedded resource '" + resourcePath + "' cannot be found in " + getFile());
        }

        File outFile = new File(getDataFolder(), resourcePath);
        int lastIndex = resourcePath.lastIndexOf('/');
        File outDir = new File(getDataFolder(), resourcePath.substring(0, Math.max(lastIndex, 0)));

        if (!outDir.exists())
            if(!outDir.mkdirs())
                getServer().getLogger().warning("Something went wrong while saving the '" + outFile.getName() + "' file");

        try {
            if (!outFile.exists() || replace) {
                OutputStream out = Files.newOutputStream(outFile.toPath());
                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
                out.close();
                in.close();
            }
        } catch (IOException ex) {
            getServer().getLogger().warning("Something went wrong while saving the '" + outFile.getName() + "' file");
        }
    }
}
