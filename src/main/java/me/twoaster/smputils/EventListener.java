package me.twoaster.smputils;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.ServerCommandEvent;
import org.bukkit.event.server.ServerListPingEvent;

import static me.twoaster.smputils.SMPUtils.DELIM;

public class EventListener implements Listener {

    private final SMPUtils main;
    private final RankManager rankManager;

    public EventListener(SMPUtils main, RankManager rankManager) {
        this.main = main;
        this.rankManager = rankManager;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        main.commandManager.getPlayTime().startSession(event.getPlayer().getUniqueId());
        event.setJoinMessage("§8[§2+§8] §f" + rankManager.getPrefix(player.getUniqueId()) + rankManager.getNameColor(player.getUniqueId()) + player.getDisplayName() + rankManager.getSuffix(player.getUniqueId()));
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        main.commandManager.getPlayTime().endSession(event.getPlayer().getUniqueId());
        event.setQuitMessage("§8[§4-§8] §f" + rankManager.getPrefix(player.getUniqueId()) + rankManager.getNameColor(player.getUniqueId()) + player.getDisplayName() + rankManager.getSuffix(player.getUniqueId()));
    }

    @EventHandler
    public void onPing(ServerListPingEvent event) {
        event.setMotd("§7Twoaster's §6SMP §7Server");
        event.setMaxPlayers(-1);
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();

        event.setFormat(rankManager.getPrefix(player.getUniqueId()) +
                        rankManager.getNameColor(player.getUniqueId()) +
                        event.getPlayer().getDisplayName() +
                        rankManager.getSuffix(player.getUniqueId()) + DELIM + "§f" +
                        rankManager.getChatColor(player.getUniqueId()) +
                        ChatColor.translateAlternateColorCodes('&', event.getMessage()));
    }

    @EventHandler
    public void onServerChat(ServerCommandEvent event) {
        if (event.getCommand().startsWith("say ")) {
            event.setCancelled(true);

            String message = "§8[§c§lCONSOLE§8]" + DELIM + "§f" + event.getCommand().substring(4).replace('&', '§');

            for (Player player : Bukkit.getOnlinePlayers())
                player.sendMessage(message);

            Bukkit.getConsoleSender().sendMessage(message);
        }
    }
}
