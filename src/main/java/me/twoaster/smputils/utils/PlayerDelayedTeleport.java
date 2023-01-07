package me.twoaster.smputils.utils;

import me.twoaster.smputils.SMPUtils;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

public class PlayerDelayedTeleport {
    private final SMPUtils main;

    private int timeLeft;
    private BukkitTask task;
    public PlayerDelayedTeleport(SMPUtils main) {
        this.main = main;
    }

    public void TeleportPlayer(Player player, Location destination, int secondsDelay, Runnable whenComplete) {
        Location originalLocation = player.getLocation();

        timeLeft = secondsDelay * 3;
        task = Bukkit.getScheduler().runTaskTimer(main, () -> {
            if (!locationsEqual(player, originalLocation)) {
                main.sendMessage(player, "§cTeleport cancelled; you moved");
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(" "));
                task.cancel();
            } else if (timeLeft == 0) {
                player.teleport(destination);
                whenComplete.run();
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(" "));
                player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 0.5f, 1);
                task.cancel();
            } else if (timeLeft % 3 == 0) {
                int secondsLeft = timeLeft / 3;
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                        TextComponent.fromLegacyText("§fStand still for §6" + secondsLeft + "§f more second" + (secondsLeft == 1 ? "" : "s") + " to teleport"));
            }

            timeLeft--;
        }, 0, 5);
    }

    private boolean locationsEqual(Player player, Location original) {
        Location playerLoc = player.getLocation();
        return playerLoc.getBlockX() == original.getBlockX()
               && playerLoc.getBlockY() == original.getBlockY()
               && playerLoc.getBlockZ() == original.getBlockZ();
    }
}
