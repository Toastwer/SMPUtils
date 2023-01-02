package me.twoaster.smputils.utils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.text.DecimalFormat;

public class TPSUtil {
    private static final DecimalFormat format = new DecimalFormat("0.00");
    private Object serverInstance;
    private Field tpsField;
    private Field tickTimes;

    public TPSUtil() {
        try {
            Class<?> minecraftServer = Class.forName("net.minecraft.server.MinecraftServer");
            serverInstance = minecraftServer.getDeclaredMethod("getServer").invoke(null);

            tpsField = minecraftServer.getDeclaredField("recentTps");
            tpsField.setAccessible(true);

            tickTimes = minecraftServer.getDeclaredField("k");
            tickTimes.setAccessible(true);

        } catch (NoSuchFieldException | SecurityException | IllegalAccessException | IllegalArgumentException |
                 InvocationTargetException | NoSuchMethodException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public double[] getTPS() {
        try {
            return (double[]) tpsField.get(serverInstance);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public double averageTickTime() {
        try {
            long[] times = (long[]) tickTimes.get(serverInstance);

            long total = 0L;
            for (long time : times)
                total += time;

            return (double) total / times.length * 1.0E-6D;
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static String format(double raw) {
        return format.format(raw);
    }
}
