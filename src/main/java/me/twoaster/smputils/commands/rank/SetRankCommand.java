package me.twoaster.smputils.commands.rank;

import me.twoaster.smputils.RankManager;
import me.twoaster.smputils.SMPUtils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class SetRankCommand extends RankArgumentHandler {
    public SetRankCommand(SMPUtils main, RankManager rankManager) {
        super(main, rankManager);
    }

    @Override
    public void handle(CommandSender sender, String label, String[] args) {
        if (args.length < 3) {
            main.sendMessage(sender, "§cThe command should be in the format §4/rank set <player> <rank>");
            return;
        }

        if (!rankManager.rankExists(args[2])) {
            main.sendMessage(sender, "§cThe rank §o" + args[2] + "§c does not exist");
            return;
        }

        OfflinePlayer[] players = Bukkit.getOfflinePlayers();
        for (OfflinePlayer player : players) {
            if (player.getName() != null && player.getName().equals(args[1])) {
                rankManager.setRank(player.getUniqueId(), args[2]);
                main.sendMessage(sender, "§aThe rank of §o" + args[1] + "§a has been set to §o" + args[2]);
                return;
            }
        }

        main.sendMessage(sender, "§cThe player §o" + args[1] + "§c cannot be found");
    }

    @Override
    public List<String> handleTab(CommandSender sender, String label, String[] args) {
        List<String> result = new ArrayList<>();
        if (args.length == 2) {
            OfflinePlayer[] players = Bukkit.getOfflinePlayers();
            for (OfflinePlayer player : players)
                if (player.getName() != null)
                    result.add(player.getName());
        } else if (args.length == 3) {
            Collection<RankManager.Rank> ranks = rankManager.getAllRanks();
            for (RankManager.Rank rank : ranks) {
                result.add(rank.name);
            }
        }

        return result;
    }
}
