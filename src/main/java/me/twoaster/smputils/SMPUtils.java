package me.twoaster.smputils;

import me.twoaster.smputils.utils.ColoredLogger;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public final class SMPUtils extends JavaPlugin {

    public static final String PREFIX = "§8[§bSMPUtils§8]";
    public static final String DELIM = " §8§l>> §f";

    public final ColoredLogger logger = new ColoredLogger();

    public EventListener eventListener;
    public CommandManager commandManager;
    public RankManager rankManager;

    @Override
    public void onEnable() {
        rankManager = new RankManager(this);

        eventListener = new EventListener(this, rankManager);
        getServer().getPluginManager().registerEvents(eventListener, this);

        saveResource("config.yml", false);
        saveResource("home.yml", false);
        saveResource("playtime.yml", false);
        saveResource("ranks.yml", false);

        commandManager = new CommandManager(this);

        for (Player player : Bukkit.getOnlinePlayers()) {
            commandManager.getPlayTime().startSession(player.getUniqueId());
        }

        Bukkit.getConsoleSender().sendMessage(PREFIX + DELIM + "§aSMPUtils has successfully started");
    }

    @Override
    public void onDisable() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            commandManager.getPlayTime().endSession(player.getUniqueId());
        }

        Bukkit.getConsoleSender().sendMessage(PREFIX + DELIM + "§cSMPUtils has been disabled");
    }

    public void sendMessage(CommandSender sender, String message) {
        sender.sendMessage(PREFIX + DELIM + "§7" + message);
    }

    public void sendMessage(CommandSender sender, BaseComponent... message) {
        BaseComponent[] arr = new BaseComponent[message.length + 1];
        arr[0] = new TextComponent(PREFIX + DELIM);
        System.arraycopy(message, 0, arr, 1, message.length);
        sender.spigot().sendMessage(arr);
    }

    public void saveResource(@NonNull String resourcePath, boolean replace) {
        if (resourcePath.equals("")) {
            throw new IllegalArgumentException("ResourcePath cannot be null or empty");
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
                logger.warning("Something went wrong while saving the '" + outFile.getName() + "' file");

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
            logger.warning("Something went wrong while saving the '" + outFile.getName() + "' file");
        }
    }

    public static List<String> getOfflinePlayersExcept(String exceptUsername) {
        OfflinePlayer[] players = Bukkit.getOfflinePlayers();

        List<String> response = new ArrayList<>();
        for (OfflinePlayer player : players)
            if (player.getName() != null && !player.getName().equalsIgnoreCase(exceptUsername))
                response.add(player.getName());

        return response;
    }

    public static List<String> getOnlinePlayersExcept(String exceptUsername) {
        Collection<? extends Player> players = Bukkit.getOnlinePlayers();

        List<String> response = new ArrayList<>();
        for (Player player : players)
            if (!player.getName().equalsIgnoreCase(exceptUsername))
                response.add(player.getName());

        return response;
    }

    @Nullable
    public static OfflinePlayer findOfflinePlayer(String username) {
        OfflinePlayer[] players = Bukkit.getOfflinePlayers();

        for (OfflinePlayer player : players)
            if (player.getName() != null && player.getName().equalsIgnoreCase(username))
                return player;

        return null;
    }
}
