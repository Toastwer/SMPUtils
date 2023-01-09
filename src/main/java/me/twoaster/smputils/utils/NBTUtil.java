package me.twoaster.smputils.utils;

import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class NBTUtil {
    private static String VERSION;
    private static String getVersion() {
        if (VERSION == null)
            VERSION = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];

        return VERSION;
    }

    private static Class<?> getCraftBukkitClass(String name) throws ClassNotFoundException {
        return Class.forName("org.bukkit.craftbukkit." + getVersion() + "." + name);
    }

    public static Map<String, String> getNBTData(ItemStack item) {
        try {
            Class<?> CraftItemStack = getCraftBukkitClass("inventory.CraftItemStack");
            Class<?> nmsItemStack = Class.forName("net.minecraft.world.item.ItemStack");
            Class<?> nmsNBTTagCompound = Class.forName("net.minecraft.nbt.NBTTagCompound");

            Object nmsItem = CraftItemStack.getMethod("asNMSCopy", ItemStack.class).invoke(null, item.clone());

            Object NBTTagCompound = null;
            for (Field field : nmsItemStack.getDeclaredFields()) {
                if (field.getType().getSimpleName().equals(nmsNBTTagCompound.getSimpleName())) {
                    field.setAccessible(true);
                    NBTTagCompound = field.get(nmsItem);
                    break;
                }
            }

            return extractNBTData(nmsNBTTagCompound, NBTTagCompound);
        } catch (Exception e) {
            e.printStackTrace();
            return new HashMap<>();
        }
    }

    public static Map<String, String> getNBTData(Block block) {
        try {
            Class<?> CraftBlockEntityState = getCraftBukkitClass("block.CraftBlockEntityState");
            Class<?> nmsNBTTagCompound = Class.forName("net.minecraft.nbt.NBTTagCompound");

            try {
                Object tileEntity = CraftBlockEntityState.cast(block.getState());

                Method getNBT = CraftBlockEntityState.getMethod("getSnapshotNBT");
                Object NBTTagCompound = getNBT.invoke(tileEntity);

                return extractNBTData(nmsNBTTagCompound, NBTTagCompound);
            } catch (ClassCastException ignored) {
                return new HashMap<>();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new HashMap<>();
        }
    }

    @SuppressWarnings("unchecked")
    private static Map<String, String> extractNBTData(Class<?> nmsNBTTagCompound, Object NBTTagCompound) throws IllegalAccessException {
        Map<String, String> out = new HashMap<>();

        if (NBTTagCompound == null)
            return out;

        Map<String, Object> tags = null;
        for (Field field : nmsNBTTagCompound.getDeclaredFields()) {
            if (field.getType().getSimpleName().contains("Map")) {
                field.setAccessible(true);
                tags = (Map<String, Object>) field.get(NBTTagCompound);
                break;
            }
        }

        if (tags == null)
            return out;

        for (String key : tags.keySet()) {
            out.put(key, tags.get(key).toString());
        }

        return out;
    }
}
