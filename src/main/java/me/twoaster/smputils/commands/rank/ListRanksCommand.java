package me.twoaster.smputils.commands.rank;

import me.twoaster.smputils.RankManager;
import me.twoaster.smputils.SMPUtils;
import org.bukkit.command.CommandSender;

import java.util.Collection;

public class ListRanksCommand extends ArgumentHandler {
    public ListRanksCommand(SMPUtils main, RankManager rankManager) {
        super(main, rankManager);
    }

    @Override
    public void handle(CommandSender sender, String label, String[] args) {
        Collection<RankManager.Rank> ranks = rankManager.getAllRanks();

        StringBuilder out = new StringBuilder();
        out.append("\n§8---------------< §3Ranks §8>---------------\n");

        for (RankManager.Rank rank : ranks) {
            out.append("§6").append(rank.name).append(": ")
                    .append("§ePrefix: §f'").append(rank.prefix).append("§f', ")
                    .append("§eSuffix: §f'").append(rank.suffix).append("§f', ")
                    .append("§eNameColor: §f'").append(rank.nameColor.replace('§', '&')).append("§f', ")
                    .append("§eChatColor: §f'").append(rank.chatColor.replace('§', '&')).append("§f'\n");
        }

        out.append("§8--------------------------------------");

        main.sendMessage(sender, out.toString());
    }
}
