package me.twoaster.smputils;

import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class RankManager {

    public static class Rank {
        public final String name;
        public String prefix;
        public String suffix;
        public String nameColor;
        public String chatColor;

        public Rank(String name, String prefix, String suffix, String nameColor, String chatColor) {
            this.name = name;
            this.prefix = prefix;
            this.suffix = suffix;
            this.nameColor = nameColor;
            this.chatColor = chatColor;
        }

        @Override
        public String toString() {
            return "Rank{" +
                   "name='" + name + '\'' +
                   ", prefix='" + prefix + "§f'" +
                   ", suffix='" + suffix + "§f'" +
                   ", nameColor='" + nameColor.replace('§', '&') + "§f'" +
                   ", chatColor='" + chatColor.replace('§', '&') + "§f'" +
                   '}';
        }
    }

    private Map<String, Rank> ranks;
    private Map<UUID, String> playerRanks;

    private final SMPUtils main;

    private YamlConfiguration config;

    public RankManager(SMPUtils main) {
        this.main = main;
        reloadConfig();

        loadRanks();
        loadPlayers();
    }

    public void loadRanks() {
        ranks = new HashMap<>();

        if (!config.isSet("ranks")) {
            main.logger.warning("§eNo ranks have been set");
            addDefault();
            return;
        }

        Set<String> ranks = Objects.requireNonNull(config.getConfigurationSection("ranks")).getKeys(false);
        for(String rank : ranks) {
            String prefix = "§8[§4???§8]";
            if(config.isSet("ranks." + rank + ".prefix")) {
                prefix = Objects.requireNonNull(config.getString("ranks." + rank + ".prefix")).replace('&', '§');
            } else {
                config.set("ranks." + rank + ".prefix", "§8[§4???§8]");
            }

            String suffix = "§8[§4???§8]";
            if(config.isSet("ranks." + rank + ".suffix")) {
                suffix = Objects.requireNonNull(config.getString("ranks." + rank + ".suffix")).replace('&', '§');
            } else {
                config.set("ranks." + rank + ".suffix", "§8[§4???§8]");
            }

            String nameColor = "§f";
            if(config.isSet("ranks." + rank + ".nameColor")) {
                nameColor = config.getString("ranks." + rank + ".nameColor", "&f");
                nameColor = nameColor.replace("&", "§");

                if(!validRankColor(nameColor)) {
                    main.logger.warning("§eThe rank §6" + rank + "§e has an invalid name color specified, everyone with this rank will have a §fwhite§e name");
                    config.set("ranks." + rank + ".nameColor", "&f");
                    nameColor = "§f";
                }
            } else {
                config.set("ranks." + rank + ".nameColor", "&f");
            }

            String chatColor = "§7";
            if(config.isSet("ranks." + rank + ".chatColor")) {
                chatColor = config.getString("ranks." + rank + ".chatColor", "&7");
                chatColor = chatColor.replace('&', '§');

                if(!validRankColor(chatColor)) {
                    main.logger.warning("§eThe rank §6" + rank + "§e has an invalid chat color specified, everyone with this rank will have §7gray§e text.");
                    config.set("ranks." + rank + ".chatColor", "&7");
                    chatColor = "§f";
                }
            } else {
                config.set("ranks." + rank + ".chatColor", "&7");
            }


            this.ranks.put(rank, new Rank(rank, prefix, suffix, nameColor, chatColor));
        }

        if (!rankExists("default")) {
            addDefault();
        }
    }

    private void addDefault() {
        Rank def = new Rank("default", "", "", "§f", "§7");
        this.ranks.put(def.name, def);
        saveRanks();
    }

    private void saveRanks() {
        for (Rank rank : ranks.values()) {
            config.set("ranks." + rank.name + ".prefix", rank.prefix.replace('§', '&'));
            config.set("ranks." + rank.name + ".suffix", rank.suffix.replace('§', '&'));
            config.set("ranks." + rank.name + ".nameColor", rank.nameColor.replace('§', '&'));
            config.set("ranks." + rank.name + ".chatColor", rank.chatColor.replace('§', '&'));
        }

        saveConfig();
    }

    public void loadPlayers() {
        playerRanks = new HashMap<>();

        if (!config.isSet("players")) {
            return;
        }

        Set<String> players = Objects.requireNonNull(config.getConfigurationSection("players")).getKeys(false);

        for (String uuid : players) {
            String rank = config.getString("players." + uuid);

            if (!rankExists(rank)) {
                playerRanks.put(UUID.fromString(uuid), "default");
                main.logger.warning("§eThe player with UUID §6" + uuid + "§e has an invalid rank set, defaulting to 'default'");
                continue;
            }

            playerRanks.put(UUID.fromString(uuid), rank);
        }
    }

    private void savePlayers() {
        for (Map.Entry<UUID, String> entry : playerRanks.entrySet()) {
            config.set("players." + entry.getKey(), entry.getValue());
        }

        saveConfig();
    }

    public void addRank(String name) {
        ranks.put(name, new Rank(name, "", "", "§f", "§7"));
        saveRanks();
    }

    public void removeRank(String name) {
        ranks.remove(name);
        saveRanks();
    }

    public void setRank(UUID uuid, String rankName) {
        if (!ranks.containsKey(rankName))
            throw new IllegalArgumentException("Invalid rank name '" + rankName + "' given, rank does not exist.");

        playerRanks.put(uuid, rankName);
        savePlayers();
    }

    public boolean rankExists(String name) {
        return ranks.containsKey(name);
    }

    public Collection<Rank> getAllRanks() {
        return ranks.values();
    }

    public String getPrefix(UUID uuid) {
        Rank rank = playerRanks.containsKey(uuid) ? ranks.get(playerRanks.get(uuid)) : ranks.get("default");

        return rank.prefix.isEmpty() ? "" : rank.prefix + " ";
    }

    public void setPrefix(String rank, String newPrefix) {
        Rank edited = ranks.get(rank);
        edited.prefix = newPrefix.replace('&', '§');
        ranks.put(rank, edited);
        saveRanks();
    }

    public String getSuffix(UUID uuid) {
        Rank rank = playerRanks.containsKey(uuid) ? ranks.get(playerRanks.get(uuid)) : ranks.get("default");

        return rank.suffix.isEmpty() ? "" : " " + rank.suffix;
    }

    public void setSuffix(String rank, String newSuffix) {
        Rank edited = ranks.get(rank);
        edited.suffix = newSuffix.replace('&', '§');
        ranks.put(rank, edited);
        saveRanks();
    }

    public String getNameColor(UUID uuid) {
        Rank rank = playerRanks.containsKey(uuid) ? ranks.get(playerRanks.get(uuid)) : ranks.get("default");

        return rank.nameColor;
    }

    public void setNameColor(String rank, String newNameColor) {
        Rank edited = ranks.get(rank);
        edited.nameColor = newNameColor.replace('&', '§');
        ranks.put(rank, edited);
        saveRanks();
    }

    public String getChatColor(UUID uuid) {
        Rank rank = playerRanks.containsKey(uuid) ? ranks.get(playerRanks.get(uuid)) : ranks.get("default");

        return rank.chatColor;
    }

    public void setChatColor(String rank, String newChatColor) {
        Rank edited = ranks.get(rank);
        edited.chatColor = newChatColor.replace('&', '§');
        ranks.put(rank, edited);
        saveRanks();
    }

    private boolean validRankColor(String color) {
        if(color.length() % 2 == 0) {
            char[] chars = color.toCharArray();
            for(int i = 0; i < chars.length; i++) {
                char c = chars[i];

                if(i % 2 == 0) {
                    if(c != 167)
                        return false;
                } else {
                    if(!((c >= 48 && c <= 57) || (c >= 97 && c <= 102) || (c >= 107 && c <= 111) || c == 114))
                        return false;
                }
            }

            return true;
        }

        return false;
    }

    private void saveConfig() {
        if (!(new File(main.getDataFolder(), "ranks.yml").exists())) {
            try {
                if (!new File(main.getDataFolder(), "ranks.yml").createNewFile())
                    main.logger.warning("Something went wrong while creating the 'ranks.yml' file");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            config.save(new File(main.getDataFolder(), "ranks.yml"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void reloadConfig() {
        main.saveResource("ranks.yml", false);

        config = YamlConfiguration.loadConfiguration(new File(main.getDataFolder(), "ranks.yml"));
    }
}
