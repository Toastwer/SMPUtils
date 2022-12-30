package me.twoaster.smputils.commands.rank;

import me.twoaster.smputils.RankManager;
import me.twoaster.smputils.SMPUtils;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class RankSuffixCommand extends RankArgumentHandler {
    public RankSuffixCommand(SMPUtils main, RankManager rankManager) {
        super(main, rankManager);
    }

    @Override
    public void handle(CommandSender sender, String label, String[] args) {
        if (args.length < 2) {
            main.sendMessage(sender, "§cThe command should be in the format §4/rank suffix <rank> §o[new suffix]");
            return;
        }

        if (!rankManager.rankExists(args[1])) {
            main.sendMessage(sender, "§cThe rank §o" + args[1] + "§c does not exist");
            return;
        }

        if (args.length < 3) {
            for (RankManager.Rank rank : rankManager.getAllRanks())
                if (rank.name.equals(args[1]))
                    main.sendMessage(sender, "§fThe rank §6" + args[1] + "§f has the suffix '" + rank.suffix + "§f'");

            return;
        }

        if (args[2].equals("!clear"))
            args[2] = "";

        rankManager.setSuffix(args[1], args[2]);
        main.sendMessage(sender, "§aThe rank §o" + args[1] + "§a now has the suffix '" + args[2].replace('&', '§') + "§a'");
    }

    @Override
    public List<String> handleTab(CommandSender sender, String label, String[] args) {
        List<String> result = new ArrayList<>();

        if (args.length == 2) {
            Collection<RankManager.Rank> ranks = rankManager.getAllRanks();
            for (RankManager.Rank rank : ranks) {
                result.add(rank.name);
            }
        }

        return result;
    }
}
