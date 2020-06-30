package dev.jaqobb.influencers.spigot.basic;

import dev.jaqobb.influencers.api.basic.Messages;
import dev.jaqobb.influencers.spigot.InfluencersSpigotPlugin;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.stream.Collectors;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class SpigotMessages implements Messages {

	private InfluencersSpigotPlugin plugin;
	private File file;
	private FileConfiguration fileConfiguration;
	private Map<String, String> single;
	private Map<String, List<String>> multi;

	public SpigotMessages(InfluencersSpigotPlugin plugin) {
		this.plugin = plugin;
		this.file = new File(this.plugin.getDataFolder(), "messages.yml");
		if (!this.file.exists()) {
			this.plugin.saveResource("messages.yml", false);
		}
		this.reload();
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

	private void load0(ConfigurationSection section, String prefix) {
		for (String key : section.getKeys(false)) {
			String newPrefix = prefix.isEmpty() ? key : prefix + "." + key;
			if (section.isString(key)) {
				this.single.put(newPrefix, section.getString(key));
			} else if (section.isList(key)) {
				this.multi.put(newPrefix, section.getList(key).stream()
					.map(Object::toString)
					.collect(Collectors.toList()));
			} else if (section.isConfigurationSection(key)) {
				this.load0(section.getConfigurationSection(key), newPrefix);
			}
		}
	}

	@Override
	public void save() {
		try {
			this.fileConfiguration.save(this.file);
		} catch (Exception exception) {
			this.plugin.getLogger().log(Level.WARNING, "Could not save messages.", exception);
		}
	}

	@Override
	public void reload() {
		this.fileConfiguration = YamlConfiguration.loadConfiguration(this.file);
		InputStream configurationStream = this.plugin.getResource("configuration.yml");
		if (configurationStream != null) {
			this.fileConfiguration.setDefaults(YamlConfiguration.loadConfiguration(new InputStreamReader(configurationStream, StandardCharsets.UTF_8)));
		}
		this.load();
	}
}
