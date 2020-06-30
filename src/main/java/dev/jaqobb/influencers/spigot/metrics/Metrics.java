package dev.jaqobb.influencers.spigot.metrics;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import dev.jaqobb.influencers.spigot.InfluencersSpigotPlugin;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.zip.GZIPOutputStream;
import javax.net.ssl.HttpsURLConnection;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;

public class Metrics {

	public static final int VERSION = 1;

	private static final String SUBMIT_DATA_URL = "https://bStats.org/submitData/bukkit";

	private InfluencersSpigotPlugin plugin;
	private String serverUniqueId;

	public Metrics(InfluencersSpigotPlugin plugin) {
		this.plugin = plugin;
		File dataFolder = new File(this.plugin.getDataFolder().getParentFile(), "bStats");
		File configurationFile = new File(dataFolder, "config.yml");
		YamlConfiguration configuration = YamlConfiguration.loadConfiguration(configurationFile);
		if (!configuration.isSet("serverUuid")) {
			configuration.addDefault("enabled", true);
			configuration.addDefault("serverUuid", UUID.randomUUID().toString());
			configuration.addDefault("logFailedRequests", false);
			configuration.addDefault("logSentData", false);
			configuration.addDefault("logResponseStatusText", false);
			configuration.options().header(
				"bStats collects some data for plugin authors like how many servers are using their plugins.\n" +
					"To honor their work, you should not disable it.\n" +
					"This has nearly no effect on the server performance!\n" +
					"Check out https://bStats.org/ to learn more :)"
			).copyDefaults(true);
			try {
				configuration.save(configurationFile);
			} catch (IOException ignored) {
			}
		}
		this.serverUniqueId = configuration.getString("serverUuid");
		this.startSubmitting();
	}

	private void startSubmitting() {
		Timer timer = new Timer(true);
		timer.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				if (!Metrics.this.plugin.isEnabled()) {
					timer.cancel();
					return;
				}
				Bukkit.getScheduler().runTask(Metrics.this.plugin, Metrics.this::submitData);
			}
		}, 1_000 * 60 * 5, 1_000 * 60 * 30);
	}

	private void submitData() {
		JsonObject serverData = this.getServerData();
		JsonArray pluginData = new JsonArray();
		pluginData.add(this.getPluginData());
		serverData.add("plugins", pluginData);
		new Thread(() -> {
			try {
				this.sendData(serverData);
			} catch (Exception exception) {
				this.plugin.debug("Could not send data to bStats.", exception);
			}
		}).start();
	}

	private JsonObject getServerData() {
		JsonObject data = new JsonObject();
		data.addProperty("serverUUID", this.serverUniqueId);
		data.addProperty("playerAmount", Bukkit.getOnlinePlayers().size());
		data.addProperty("onlineMode", Bukkit.getOnlineMode() ? 1 : 0);
		data.addProperty("bukkitVersion", Bukkit.getVersion());
		data.addProperty("bukkitName", Bukkit.getName());
		data.addProperty("javaVersion", System.getProperty("java.version"));
		data.addProperty("osName", System.getProperty("os.name"));
		data.addProperty("osArch", System.getProperty("os.arch"));
		data.addProperty("osVersion", System.getProperty("os.version"));
		data.addProperty("coreCount", Runtime.getRuntime().availableProcessors());
		return data;
	}

	public JsonObject getPluginData() {
		JsonObject data = new JsonObject();
		data.addProperty("pluginName", this.plugin.getDescription().getName() + "Spigot");
		data.addProperty("pluginVersion", this.plugin.getDescription().getVersion());
		data.add("customCharts", new JsonArray());
		return data;
	}

	private void sendData(JsonObject data) throws Exception {
		if (Bukkit.isPrimaryThread()) {
			throw new IllegalAccessException("This method must not be called from the main thread!");
		}
		this.plugin.debug("Sending data to bStats: \"" + data + "\".");
		HttpsURLConnection connection = (HttpsURLConnection) new URL(SUBMIT_DATA_URL).openConnection();
		byte[] compressedData = this.compress(data.toString());
		connection.setRequestMethod("POST");
		connection.addRequestProperty("Accept", "application/json");
		connection.addRequestProperty("Connection", "close");
		connection.addRequestProperty("Content-Encoding", "gzip");
		connection.addRequestProperty("Content-Length", String.valueOf(compressedData.length));
		connection.setRequestProperty("Content-Type", "application/json");
		connection.setRequestProperty("User-Agent", "MC-Server/" + VERSION);
		connection.setDoOutput(true);
		try (DataOutputStream dataOutputStream = new DataOutputStream(connection.getOutputStream())) {
			dataOutputStream.write(compressedData);
		}
		StringBuilder builder = new StringBuilder();
		InputStream inputStream = connection.getInputStream();
		try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
			String line;
			while ((line = bufferedReader.readLine()) != null) {
				builder.append(line);
			}
		}
		this.plugin.debug("Sent data to bStats and received response: \"" + builder + "\".");
	}

	private byte[] compress(String string) throws IOException {
		if (string == null) {
			return null;
		}
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		try (GZIPOutputStream gzipOutputStream = new GZIPOutputStream(byteArrayOutputStream)) {
			gzipOutputStream.write(string.getBytes(StandardCharsets.UTF_8));
		}
		return byteArrayOutputStream.toByteArray();
	}
}
