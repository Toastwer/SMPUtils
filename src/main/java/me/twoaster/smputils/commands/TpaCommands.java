package me.twoaster.smputils.commands;

import me.twoaster.smputils.CommandManager;
import me.twoaster.smputils.SMPUtils;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.*;

import static me.twoaster.smputils.SMPUtils.getOnlinePlayersExcept;

public class TpaCommands implements CommandExecutor, TabCompleter {

    private static class Request {
        private enum RequestType {
            TO_SENDER,
            TO_RECEIVER
        }

        private final Player receiver;
        private final Player sender;
        private final RequestType type;
        private long requestTime;

        private Request(Player sender, Player receiver, RequestType type, long requestTime) {
            this.receiver = receiver;
            this.sender = sender;
            this.type = type;
            this.requestTime = requestTime;
        }
    }

    private final SMPUtils main;
    private final CommandManager commandManager;

    private final LinkedList<Request> tpaRequests;

    public TpaCommands(SMPUtils main, CommandManager commandManager) {
        this.main = main;
        this.commandManager = commandManager;

        tpaRequests = new LinkedList<>();

        Bukkit.getScheduler().runTaskTimerAsynchronously(main, () -> {
            if (tpaRequests.isEmpty())
                return;

            long time = new Date().getTime();
            while (!tpaRequests.isEmpty()) {
                if (time - tpaRequests.peek().requestTime < 60 * 1000)
                    break;

                Request request = tpaRequests.poll();
                main.sendMessage(request.sender, "§eYour tpa request to §o" + request.receiver.getName() + "§e has expired");
                main.sendMessage(request.receiver, "§eYour tpa request from §o" + request.sender.getName() + "§e has expired");
            }
        }, 0L, 1L);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            main.getLogger().severe("Only players can use this command");
            return true;
        }

        if (!commandManager.enableTpa) {
            main.sendMessage(sender, "§c/tpa functionality is currently disabled");
            return true;
        }

        Player player = (Player) sender;

        if (label.equalsIgnoreCase("tpa")) {
            if (args.length > 0) {
                Player target = Bukkit.getPlayer(args[0]);
                if (target != null && target.isOnline()) {
                    if (target.getName().equalsIgnoreCase(player.getName())) {
                        main.sendMessage(player, "§cYou cannot send a request to yourself");
                        return true;
                    }

                    TextComponent accept = new TextComponent("§8[§a§lAccept§8]");
                    accept.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tpaaccept " + player.getName()));
                    accept.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("§fClick here to §aaccept§f the request").create()));

                    TextComponent deny = new TextComponent("§8[§c§lDeny§8]");
                    deny.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tpadeny " + player.getName()));
                    deny.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("§fClick here to §cdeny§f the request").create()));

                    main.sendMessage(target, new TextComponent("§fThe player §6" + player.getName() + "§f has requested to teleport to your location "),
                            accept, new TextComponent(" "), deny);
                    target.playSound(target.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5f, 1);

                    if (removeOverlapping(player, target))
                        return true;

                    tpaRequests.add(new Request(player, target, Request.RequestType.TO_RECEIVER, new Date().getTime()));
                    main.sendMessage(player, "§fA request has been sent to §6" + target.getName());
                } else {
                    main.sendMessage(player, "§cThe player §o" + args[0] + "§c is not online");
                }
            } else {
                main.sendMessage(player, "§cExpected a player to send the request to");
            }
        } else if (label.equalsIgnoreCase("tpahere") || label.equalsIgnoreCase("tpah")) {
            if (args.length > 0) {
                Player target = Bukkit.getPlayer(args[0]);
                if (target != null && target.isOnline()) {
                    if (target.getName().equalsIgnoreCase(player.getName())) {
                        main.sendMessage(player, "§cYou cannot send a request to yourself");
                        return true;
                    }

                    TextComponent accept = new TextComponent("§8[§a§lAccept§8]");
                    accept.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tpaaccept " + player.getName()));
                    accept.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("§fClick here to §aaccept§f the request").create()));

                    TextComponent deny = new TextComponent("§8[§c§lDeny§8]");
                    deny.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tpadeny " + player.getName()));
                    deny.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("§fClick here to §cdeny§f the request").create()));

                    main.sendMessage(target, new TextComponent("§fThe player §6" + player.getName() + "§f wants to teleport you to their location "),
                            accept, new TextComponent(" "), deny);
                    target.playSound(target.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5f, 1);

                    if (removeOverlapping(player, target))
                        return true;

                    tpaRequests.add(new Request(player, target, Request.RequestType.TO_SENDER, new Date().getTime()));
                    main.sendMessage(player, "§fA request has been sent to §6" + target.getName());
                } else {
                    main.sendMessage(player, "§cThe player §o" + args[0] + "§c is not online");
                }
            } else {
                main.sendMessage(player, "§cExpected a player to send the request to");
            }
        } else if (label.equalsIgnoreCase("tpaaccept") || label.equalsIgnoreCase("tpaa")) {
            if (args.length > 0) {
                Player target = Bukkit.getPlayer(args[0]);
                if (target != null && target.isOnline()) {
                    for (Request request : tpaRequests) {
                        if (request.receiver.getName().equalsIgnoreCase(player.getName())) {
                            acceptRequest(request);
                            return true;
                        }
                    }

                    main.sendMessage(player, "§eThe player §o" + args[0] + "§e has not sent any request to you");
                } else {
                    main.sendMessage(player, "§cThe player §o" + args[0] + "§c is not online");
                }
            } else {
                for (Request request : tpaRequests) {
                    if (request.receiver.getName().equalsIgnoreCase(player.getName())) {
                        acceptRequest(request);
                        return true;
                    }
                }

                main.sendMessage(player, "§eYou currently do not have any tpa requests");
            }
        } else if (label.equalsIgnoreCase("tpadeny") || label.equalsIgnoreCase("tpad")) {
            if (args.length > 0) {
                Player target = Bukkit.getPlayer(args[0]);
                if (target != null && target.isOnline()) {
                    for (Request request : tpaRequests) {
                        if (request.receiver.getName().equalsIgnoreCase(player.getName())) {
                            denyRequest(request);
                            return true;
                        }
                    }

                    main.sendMessage(player, "§eThe player §o" + args[0] + "§e has not sent any request to you");
                } else {
                    main.sendMessage(player, "§cThe player §o" + args[0] + "§c is not online");
                }
            } else {
                for (Request request : tpaRequests) {
                    if (request.receiver.getName().equalsIgnoreCase(player.getName())) {
                        denyRequest(request);
                        return true;
                    }
                }

                main.sendMessage(player, "§eYou currently do not have any tpa requests");
            }
        }

        return true;
    }

    private boolean removeOverlapping(Player player, Player target) {
        for (Request request : tpaRequests) {
            if (request.sender.getName().equalsIgnoreCase(player.getName())) {
                tpaRequests.remove(request);
                request.requestTime = new Date().getTime();
                main.sendMessage(player, "§fA request has been sent to §6" + target.getName() + "§f, your previous request " +
                                         "has been overwritten");
                return true;
            }
        }

        return false;
    }

    private void acceptRequest(Request request) {
        main.sendMessage(request.receiver, "§fYou have §aaccepted§f the request from the player §6" + request.sender.getName());
        main.sendMessage(request.sender, "§fThe request to the player §6" + request.receiver.getName() + "§f has been §aaccepted");

        Particle.DustOptions dustOptions = new Particle.DustOptions(Color.fromRGB(157, 25, 205), 1.0F);
        switch (request.type) {
            case TO_SENDER:
                request.receiver.teleport(request.sender.getLocation());
                request.receiver.playSound(request.sender.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 0.5f, 1);

                request.sender.getWorld().spawnParticle(Particle.REDSTONE, request.sender.getLocation().add(0, 1, 0),
                        75, 0.5d, 1d, 0.5d, dustOptions);
                break;
            case TO_RECEIVER:
                request.sender.teleport(request.receiver.getLocation());
                request.sender.playSound(request.receiver.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 0.5f, 1);

                request.receiver.getWorld().spawnParticle(Particle.REDSTONE, request.receiver.getLocation().add(0, 1, 0),
                        75, 0.5d, 1d, 0.5d, dustOptions);
                break;
        }

        tpaRequests.remove(request);
    }

    private void denyRequest(Request request) {
        main.sendMessage(request.receiver, "§fYou have §cdenied§f the request from the player §6" + request.sender.getName());
        main.sendMessage(request.sender, "§fThe request to the player §6" + request.receiver.getName() + "§f has been §cdenied");
        tpaRequests.remove(request);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length > 1)
            return new ArrayList<>();

        return getOnlinePlayersExcept(sender.getName());
    }
}
