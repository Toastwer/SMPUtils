package me.twoaster.smputils.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Converter {
    public static List<String> mapToStringList(Map<String, String> map) {
        List<String> list = new ArrayList<>();

        for (Map.Entry<String, String> entry : map.entrySet()) {
            list.add(entry.getKey() + ";" + entry.getValue());
        }

        return list;
    }

    public static Map<String, String> stringListToMap(List<String> list) {
        if (list == null)
            return new HashMap<>();

        Map<String, String> map = new HashMap<>();

        for (String entry : list) {
            String[] parts = entry.split(";");

            map.put(parts[0], parts[1]);
        }

        return map;
    }
}
