package me.twoaster.smputils.commands.rank;

import me.twoaster.smputils.CommandManager;
import me.twoaster.smputils.RankManager;
import me.twoaster.smputils.SMPUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.*;

public class RankCommand implements CommandExecutor, TabCompleter {

    private final SMPUtils main;
    private final CommandManager commandManager;
    private final RankManager rankManager;

    private final Map<String, RankArgumentHandler> handlers;

    public RankCommand(SMPUtils main, CommandManager commandManager) {
        this.main = main;
        this.commandManager = commandManager;
        this.rankManager = main.rankManager;

        handlers = new HashMap<>();

        handlers.put("create", new CreateRankCommand(main, rankManager));
        handlers.put("delete", new DeleteRankCommand(main, rankManager));
        handlers.put("list", new ListRanksCommand(main, rankManager));
        handlers.put("set", new SetRankCommand(main, rankManager));
        handlers.put("prefix", new RankPrefixCommand(main, rankManager));
        handlers.put("suffix", new RankSuffixCommand(main, rankManager));
        handlers.put("namecolor", new RankNameColorCommand(main, rankManager));
        handlers.put("chatcolor", new RankChatColorCommand(main, rankManager));
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!label.equalsIgnoreCase("rank")) {
            main.sendMessage(sender, "§cUnknown command '/" + label);
            return true;
        }

        if (!sender.isOp()) {
            main.sendMessage(sender, "§cYou do not have permission to do this");
            return true;
        }

        if (args.length == 0) {
            main.sendMessage(sender, "\n§8---------------< §3Rank Usage §8>---------------" +
                                     "\n§7- §b/rank create §e<rankname>" +
                                     "\n§7- §b/rank delete §e<rankname>" +
                                     "\n§7- §b/rank list" +
                                     "\n§7- §b/rank set §e<player> <rank>" +
                                     "\n§7- §b/rank prefix §e<rank> §a[new prefix]" +
                                     "\n§7- §b/rank suffix §e<rank> §a[new suffix]" +
                                     "\n§7- §b/rank namecolor §e<rank> §a[new color]" +
                                     "\n§7- §b/rank chatcolor §e<rank> §a[new color]" +
                                     "\n§8-------------------------------------------");
            return true;
        }

        if (!handlers.containsKey(args[0].toLowerCase())) {
            main.sendMessage(sender, "§cThe command '/rank " + args[0] + "' is not valid");
            return true;
        }

        handlers.get(args[0].toLowerCase()).handle(sender, label, args);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1)
            return Arrays.asList("create", "delete", "list", "set", "prefix", "suffix", "namecolor", "chatcolor");
        else if (args.length > 1 && handlers.containsKey(args[0].toLowerCase()))
            return handlers.get(args[0].toLowerCase()).handleTab(sender, label, args);

        return new ArrayList<>();
    }
}
