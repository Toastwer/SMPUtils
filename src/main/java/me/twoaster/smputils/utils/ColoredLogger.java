package me.twoaster.smputils.utils;

import me.twoaster.smputils.SMPUtils;
import org.bukkit.Bukkit;

public class ColoredLogger {

    public void info(Object message) {
    Bukkit.getConsoleSender().sendMessage(SMPUtils.PREFIX + " §fINFO: " + message);
    }

    public void info(String message) {
        Bukkit.getConsoleSender().sendMessage(SMPUtils.PREFIX + " §fINFO: " + message);
    }

    public void warning(Object message) {
        Bukkit.getConsoleSender().sendMessage(SMPUtils.PREFIX + " §eWARNING: " + message);
    }

    public void warning(String message) {
        Bukkit.getConsoleSender().sendMessage(SMPUtils.PREFIX + " §eWARNING: " + message);
    }

    public void severe(Object message) {
        Bukkit.getConsoleSender().sendMessage(SMPUtils.PREFIX + " §cSEVERE: " + message);
    }

    public void severe(String message) {
        Bukkit.getConsoleSender().sendMessage(SMPUtils.PREFIX + " §cSEVERE: " + message);
    }
}
