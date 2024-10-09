package com.reeman.commons.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;

public class FileMapUtils {

    public static void put(String name, String key, String value) throws Exception {
        checkExist(name);
        FileWriter writer = new FileWriter(name, true);
        writer.write(key + ":" + value + "\n");
        writer.flush();
        writer.close();
    }

    private static void checkExist(String name) throws IOException {
        File file = new File(name);
        File parentFile = file.getParentFile();
        if (!parentFile.exists()) parentFile.mkdirs();
        if (!file.exists()) file.createNewFile();
    }

    public static void clear(String name) throws Exception {
        checkExist(name);
        FileWriter writer = new FileWriter(name, false);
        writer.write("");
        writer.flush();
        writer.close();
    }

    public static String get(String name, String key) throws Exception {
        checkExist(name);
        String line = null;
        BufferedReader reader = new BufferedReader(new FileReader(name));
        while ((line = reader.readLine()) != null) {
            String trim = line.trim();
            String[] split = trim.split(":");
            if (split.length >= 2 && split[0].equals(key)) {
                int i = trim.indexOf(":")+1;
                return trim.substring(i);
            }
        }
        reader.close();
        return "";
    }

    public static HashMap<String, String> getAll(String name) throws Exception {
        checkExist(name);
        String line = null;
        HashMap<String, String> map = new HashMap<>();
        BufferedReader reader = new BufferedReader(new FileReader(name));
        while ((line = reader.readLine()) != null) {
            String trim = line.trim();
            String[] split = trim.split(":");
            if (split.length >= 2) {
                map.put(split[0], trim.substring(trim.indexOf(":")+1));
            }
        }
        reader.close();
        return map;
    }

    public static void deleteByKey(String name, String key) throws Exception {
        checkExist(name);
        StringBuilder sb = new StringBuilder();
        BufferedReader reader = new BufferedReader(new FileReader(name));
        String line;
        while ((line = reader.readLine()) != null) {
            String[] split = line.trim().split(":");
            if (split.length >= 2 && split[0].equals(key)) {
                continue;
            }
            sb.append(line).append("\n");
        }
        reader.close();

        FileWriter writer = new FileWriter(name, false);
        writer.write(sb.toString());
        writer.flush();
        writer.close();
    }


    public static void replace(String name, String key, String value) throws Exception {
        checkExist(name);
        StringBuilder sb = new StringBuilder();
        BufferedReader reader = new BufferedReader(new FileReader(name));
        String line = null;
        while ((line = reader.readLine()) != null) {
            String[] split = line.trim().split(":");
            if (split.length >= 2 && split[0].equals(key)) {
                sb.append(key).append(":").append(value).append("\n");
            } else {
                sb.append(line).append("\n");
            }
        }
        FileWriter writer = new FileWriter(name, false);
        writer.write(sb.toString());
        writer.flush();
        writer.close();
    }
}
