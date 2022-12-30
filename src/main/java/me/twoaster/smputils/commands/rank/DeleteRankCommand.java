package me.twoaster.smputils.commands.rank;

import me.twoaster.smputils.RankManager;
import me.twoaster.smputils.SMPUtils;
import org.bukkit.command.CommandSender;

public class DeleteRankCommand extends RankArgumentHandler {
    public DeleteRankCommand(SMPUtils main, RankManager rankManager) {
        super(main, rankManager);
    }

    @Override
    public void handle(CommandSender sender, String label, String[] args) {
        if (args.length < 2) {
            main.sendMessage(sender, "§cExpected a rank name");
            return;
        }

        if (!rankManager.rankExists(args[1])) {
            main.sendMessage(sender, "§eThat rank doesn't exist");
            return;
        }

        rankManager.removeRank(args[1]);
        main.sendMessage(sender, "§aThe rank §2" + args[1] + "§a has been deleted");
    }
}
