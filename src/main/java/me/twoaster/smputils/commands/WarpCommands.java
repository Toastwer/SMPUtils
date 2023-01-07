package me.twoaster.smputils.commands;

import me.twoaster.smputils.CommandManager;
import me.twoaster.smputils.SMPUtils;
import me.twoaster.smputils.utils.PlayerDelayedTeleport;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class WarpCommands implements CommandExecutor, TabCompleter {
    private static class FormattableLocation extends Location {
        private FormattableLocation(String input) {
            super(null, 0, 0, 0, 0f, 0f);

            String[] parts = input.split(",");
            setWorld(Bukkit.getWorld(parts[0]));
            setX(Integer.parseInt(parts[1]));
            setY(Integer.parseInt(parts[2]));
            setZ(Integer.parseInt(parts[3]));
            setYaw(Float.parseFloat(parts[4]));
        }

        private FormattableLocation(Location location) {
            super(location.getWorld(), location.getBlockX(), location.getBlockY(), location.getBlockZ(), location.getYaw(), 0);
        }

        private String formatToString() {
            return Objects.requireNonNull(getWorld()).getName() + "," + getBlockX() + "," + getBlockY() + "," + getBlockZ() + "," + getYaw();
        }
    }

    private final SMPUtils main;
    private final CommandManager commandManager;

    private YamlConfiguration config;
    private final Map<String, FormattableLocation> warps;


    public WarpCommands(SMPUtils main, CommandManager commandManager) {
        this.main = main;
        this.commandManager = commandManager;

        warps = new HashMap<>();

        reloadConfig();
        loadWarps();
    }

    private void loadWarps() {
        List<String> warpsList = config.getStringList("warps");

        for (String warp : warpsList) {
            String[] parts = warp.split(";");
            warps.put(parts[0], new FormattableLocation(parts[1]));
        }
    }

    private void saveWarps() {
        List<String> warpsList = new ArrayList<>();
        for (Map.Entry<String, FormattableLocation> warp : warps.entrySet()) {
            warpsList.add(warp.getKey() + ";" + warp.getValue().formatToString());
        }

        config.set("warps", warpsList);
        saveConfig();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            main.getLogger().severe("Only players can use this command");
            return true;
        }

        if (!commandManager.enableWarp) {
            main.sendMessage(sender, "§c/warp and /setwarp are currently disabled");
            return true;
        }

        if (label.equalsIgnoreCase("warp")) {
            if (args.length < 1) {
                main.sendMessage(sender, "§cExpected a name of the warp");
                return true;
            }

            if (args.length > 1) {
                main.sendMessage(sender, "§cThe name of a warp cannot contain spaces");
                return true;
            }

            if (!warps.containsKey(args[0].toLowerCase())) {
                main.sendMessage(sender, "§cThat warp does not exist");
                return true;
            }

            PlayerDelayedTeleport delayedTeleport = new PlayerDelayedTeleport(main);

            main.sendMessage(sender, "§fStand still for §63 seconds §fto teleport to '" + args[0].toLowerCase() + "'");
            delayedTeleport.TeleportPlayer((Player) sender, warps.get(args[0].toLowerCase()), 3,
                    () -> main.sendMessage(sender, "§aYou have been sent to '" + args[0].toLowerCase() + "'"));
        } else if (label.equalsIgnoreCase("setwarp")) {
            if (args.length < 1) {
                main.sendMessage(sender, "§cExpected a name for the new warp");
                return true;
            }

            if (args.length > 1) {
                main.sendMessage(sender, "§cThe name of a warp cannot contain spaces");
                return true;
            }

            main.sendMessage(sender, "§aThe warp has been " + (warps.containsKey(args[0].toLowerCase()) ? "edited" : "created"));
            warps.put(args[0].toLowerCase(), new FormattableLocation(((Player) sender).getLocation()));

            saveWarps();
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (label.equalsIgnoreCase("warp") && args.length == 1)
            return new ArrayList<>(warps.keySet());

        return new ArrayList<>();
    }


    private void reloadConfig() {
        main.saveResource("warps.yml", false);

        config = YamlConfiguration.loadConfiguration(new File(main.getDataFolder(), "warps.yml"));
    }

    private void saveConfig() {
        if (!(new File(main.getDataFolder(), "warps.yml").exists())) {
            try {
                if (!new File(main.getDataFolder(), "warps.yml").createNewFile())
                    main.getServer().getLogger().warning("Something went wrong while creating the 'playtime.yml' file");
            } catch (IOException exception) {
                exception.printStackTrace();
            }
        }

        try {
            config.save(new File(main.getDataFolder(), "warps.yml"));
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }
}
