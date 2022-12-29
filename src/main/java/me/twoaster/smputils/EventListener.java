package me.twoaster.smputils;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.*;
import org.bukkit.event.server.ServerListPingEvent;

public class EventListener implements Listener {

    private final SMPUtils main;

    public EventListener(SMPUtils main) {
        this.main = main;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        main.commandManager.getPlayTime().startSession(event.getPlayer().getUniqueId());
        event.setJoinMessage("§8[§2+§8] §f" + player.getDisplayName());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        main.commandManager.getPlayTime().endSession(event.getPlayer().getUniqueId());
        event.setQuitMessage("§8[§4-§8] §f" + player.getDisplayName());
    }

    @EventHandler
    public void onPing(ServerListPingEvent event) {
        event.setMotd("§7Twoaster's §6SMP §7Server");
        event.setMaxPlayers(-1);
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        event.setFormat(event.getPlayer().getDisplayName() + " §8>> §f" + ChatColor.translateAlternateColorCodes('&', event.getMessage()));
    }
}
