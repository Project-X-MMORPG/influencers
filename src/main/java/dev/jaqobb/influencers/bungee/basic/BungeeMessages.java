package dev.jaqobb.influencers.bungee.basic;

import dev.jaqobb.influencers.api.basic.Messages;
import dev.jaqobb.influencers.bungee.InfluencersBungeePlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.stream.Collectors;

import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

public class BungeeMessages implements Messages {

    private InfluencersBungeePlugin plugin;
    private File file;
    private Configuration fileConfiguration;
    private Map<String, String> single;
    private Map<String, List<String>> multi;

    public BungeeMessages(InfluencersBungeePlugin plugin) {
        this.plugin = plugin;
        this.file = new File(this.plugin.getDataFolder(), "messages.yml");
        if (!this.file.exists() && !this.file.getParentFile().exists() && !this.file.getParentFile().mkdirs()) {
            throw new RuntimeException("Could not create plugin's data folder");
        }
        if (!this.file.exists()) {
            if (!this.file.getParentFile().exists() && !this.file.getParentFile().mkdirs()) {
                throw new RuntimeException("Could not create plugin's data folder");
            }
            try (InputStream inputStream = this.plugin.getClass().getClassLoader().getResourceAsStream("messages.yml")) {
                if (inputStream == null) {
                    throw new RuntimeException("Could not find messages file.");
                }
                Files.copy(inputStream, Paths.get(this.file.toURI()), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException exception) {
                throw new RuntimeException("Could not create messages file.", exception);
            }
        }
        try {
            this.fileConfiguration = ConfigurationProvider.getProvider(YamlConfiguration.class).load(this.file);
        } catch (Exception exception) {
            this.plugin.getLogger().log(Level.WARNING, "Could not load configuration file.", exception);
        }
        this.load();
    }

    @Override
    public String getSingle(String path) {
        return this.single.getOrDefault(path, "Could not find a single message at path '" + path + "'.");
    }

    @Override
    public void setSingle(String path, String value) {
        this.single.put(path, value);
    }

    @Override
    public List<String> getMulti(String path) {
        return this.multi.getOrDefault(path, Arrays.asList("Could not find a multi message at path '" + path + "'."));
    }

    @Override
    public void setMulti(String path, List<String> value) {
        this.multi.put(path, value);
    }

    @Override
    public void load() {
        try {
            this.single = new HashMap<>(16);
            this.multi = new HashMap<>(16);
            this.load0(this.fileConfiguration, "");
        } catch (Exception exception) {
            this.plugin.getLogger().log(Level.WARNING, "Could not load messages.", exception);
        }
    }

    private void load0(Configuration section, String prefix) {
        for (String key : section.getKeys()) {
            String newPrefix = prefix.isEmpty() ? key : prefix + "." + key;
            if (section.get(key) instanceof String) {
                this.single.put(newPrefix, section.getString(key));
            } else if (section.get(key) instanceof List) {
                this.multi.put(newPrefix, section.getList(key).stream()
                        .map(Object::toString)
                        .collect(Collectors.toList()));
            } else if (section.get(key) instanceof Configuration) {
                this.load0(section.getSection(key), newPrefix);
            }
        }
    }

    @Override
    public void save() {
        try {
            ConfigurationProvider.getProvider(YamlConfiguration.class).save(this.fileConfiguration, this.file);
        } catch (Exception exception) {
            this.plugin.getLogger().log(Level.WARNING, "Could not save messages.", exception);
        }
    }

    @Override
    public void reload() {
        try {
            this.fileConfiguration = ConfigurationProvider.getProvider(YamlConfiguration.class).load(this.file);
        } catch (Exception exception) {
            this.plugin.getLogger().log(Level.WARNING, "Could not load messages file.", exception);
        }
        this.load();
        this.save();
    }
}
