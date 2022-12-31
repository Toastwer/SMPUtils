package me.twoaster.smputils;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Collection;

public class ScoreboardManager {

    private final SMPUtils main;
    private final RankManager rankManager;

    public ScoreboardManager(SMPUtils main, RankManager rankManager) {
        this.main = main;
        this.rankManager = rankManager;
    }

    public void setTab() {
        Collection<? extends Player> players = Bukkit.getOnlinePlayers();
        for (Player player : players) {
            player.setPlayerListName(rankManager.getPrefix(player.getUniqueId()) + "§f" +
                                     rankManager.getNameColor(player.getUniqueId()) +
                                     player.getDisplayName() + "§f" +
                                     rankManager.getSuffix(player.getUniqueId()));
        }
    }
}
