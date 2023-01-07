package me.twoaster.smputils.commands;

import me.twoaster.smputils.CommandManager;
import me.twoaster.smputils.SMPUtils;
import me.twoaster.smputils.utils.Converter;
import me.twoaster.smputils.utils.PlayerDelayedTeleport;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class HomeCommands implements CommandExecutor, TabCompleter {

    private final SMPUtils main;
    private final CommandManager commandManager;

    private YamlConfiguration config;

    public HomeCommands(SMPUtils main, CommandManager commandManager) {
        this.main = main;
        this.commandManager = commandManager;

        reloadConfig();
    }

    private int timeLeft;
    private BukkitTask task;
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            main.getLogger().severe("Only players can use this command");
            return true;
        }

        Player player = (Player) sender;
        Location location = player.getLocation();

        if (label.equalsIgnoreCase("home")) {
            if (!commandManager.enableSetHome) {
                main.sendMessage(sender, "§c/home and /sethome are currently disabled");
                return true;
            }

            Map<String, String> homes = Converter.stringListToMap(config.getStringList("home"));

            if (!homes.containsKey(player.getUniqueId().toString())) {
                main.sendMessage(sender, "§cYou currently have no home set");
                return true;
            }

            String[] homeString = homes.get(player.getUniqueId().toString()).split(",");
            Location home = new Location(Bukkit.getWorld(homeString[0]), Double.parseDouble(homeString[1]), Double.parseDouble(homeString[2]),
                    Double.parseDouble(homeString[3]), Float.parseFloat(homeString[4]), Float.parseFloat(homeString[5]));

            PlayerDelayedTeleport delayedTeleport = new PlayerDelayedTeleport(main);

            main.sendMessage(player, "§fStand still for §63 seconds §fto teleport to your home");
            delayedTeleport.TeleportPlayer(player, home, 3, () -> main.sendMessage(player, "§aYou have been sent to your home"));
        } else if (label.equals("sethome")) {
            if (!commandManager.enableSetHome) {
                main.sendMessage(sender, "§c/home and /sethome are currently disabled");
                return true;
            }

            Map<String, String> homes = Converter.stringListToMap(config.getStringList("home"));

            String world = location.getWorld() == null ? "world" : location.getWorld().getName();

            String locationString = world + "," + location.getX() + "," + location.getY() + "," + location.getZ()
                    + "," + location.getYaw() + "," + location.getPitch();

            homes.put(player.getUniqueId().toString(), locationString);

            config.set("home", Converter.mapToStringList(homes));
            saveConfig();

            main.sendMessage(sender, "§aYou new home has been set!");
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (label.equalsIgnoreCase("sethome") || label.equalsIgnoreCase("home"))
            return new ArrayList<>();

        return null;
    }

    private void reloadConfig() {
        main.saveResource("home.yml", false);

        config = YamlConfiguration.loadConfiguration(new File(main.getDataFolder(), "home.yml"));
    }

    private void saveConfig() {
        if (!(new File(main.getDataFolder(), "home.yml").exists())) {
            try {
                if(!new File(main.getDataFolder(), "home.yml").createNewFile())
                    main.getServer().getLogger().warning("Something went wrong while creating the 'home.yml' file");
            } catch (IOException exception) {
                exception.printStackTrace();
            }
        }

        try {
            config.save(new File(main.getDataFolder(), "home.yml"));
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

}
