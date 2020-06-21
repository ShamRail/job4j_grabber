package ru.job4j.quartz;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ConfigManager {

    private Properties properties = new Properties();

    private final String name;

    public ConfigManager(String name) {
        this.name = name;
        this.load();
    }

    public String get(String key) {
        return properties.getProperty(key);
    }

    private void load() {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(name)) {
            properties.load(is);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
