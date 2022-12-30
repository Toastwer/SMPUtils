package me.twoaster.smputils.commands.rank;

import me.twoaster.smputils.RankManager;
import me.twoaster.smputils.SMPUtils;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;

public abstract class RankArgumentHandler {

    protected final SMPUtils main;
    protected final RankManager rankManager;

    public RankArgumentHandler(SMPUtils main, RankManager rankManager) {
        this.main = main;
        this.rankManager = rankManager;
    }

    public abstract void handle(CommandSender sender, String label, String[] args);

    public List<String> handleTab(CommandSender sender, String label, String[] args) {
        return new ArrayList<>();
    }
}
