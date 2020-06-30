package dev.jaqobb.influencers.bungee.metrics;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import dev.jaqobb.influencers.bungee.InfluencersBungeePlugin;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.zip.GZIPOutputStream;
import javax.net.ssl.HttpsURLConnection;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

public class Metrics {

	public static final int VERSION = 1;

	private static final String SUBMIT_DATA_URL = "https://bStats.org/submitData/bungeecord";

	private InfluencersBungeePlugin plugin;
	private String serverUniqueId;

	public Metrics(InfluencersBungeePlugin plugin) {
		this.plugin = plugin;
		try {
			this.loadConfiguration();
		} catch (IOException exception) {
			this.plugin.getLogger().log(Level.WARNING, "Could not load bStats configuration.", exception);
			return;
		}
		this.startSubmitting();
	}

	private void loadConfiguration() throws IOException {
		Path configurationPath = this.plugin.getDataFolder().toPath().getParent().resolve("bStats");
		if (!configurationPath.toFile().exists() && !configurationPath.toFile().mkdirs()) {
			this.plugin.getLogger().log(Level.SEVERE, "Could not load create bStats data folder.");
			return;
		}
		File configurationFile = new File(configurationPath.toFile(), "config.yml");
		if (!configurationFile.exists()) {
			this.writeFile(configurationFile,
				"# bStats collects some data for plugin authors like how many servers are using their plugins.",
				"# To honor their work, you should not disable it.",
				"# This has nearly no effect on the server performance!",
				"# Check out https://bStats.org/ to learn more :)",
				"enabled: true",
				"serverUuid: \"" + UUID.randomUUID() + "\"",
				"logFailedRequests: false",
				"logSentData: false",
				"logResponseStatusText: false");
		}
		this.serverUniqueId = ConfigurationProvider.getProvider(YamlConfiguration.class).load(configurationFile).getString("serverUuid");
	}

	private void writeFile(File file, String... lines) throws IOException {
		if (!file.exists() && !file.createNewFile()) {
			this.plugin.getLogger().log(Level.SEVERE, "Could not create bStats \"" + file.getName() + "\" file.");
			return;
		}
		try (FileOutputStream fileOutputStream = new FileOutputStream(file); OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fileOutputStream, StandardCharsets.UTF_8)) {
			for (String line : lines) {
				outputStreamWriter.write(line);
				outputStreamWriter.write(System.lineSeparator());
			}
		}
	}

	private void startSubmitting() {
		this.plugin.getProxy().getScheduler().schedule(this.plugin, this::submitData, 2, 30, TimeUnit.MINUTES);
	}

	private void submitData() {
		JsonObject serverData = this.getServerData();
		JsonArray pluginData = new JsonArray();
		pluginData.add(this.getPluginData());
		serverData.add("plugins", pluginData);
		try {
			this.sendData(serverData);
		} catch (Exception exception) {
			this.plugin.debug("Could not send data to bStats.", exception);
		}
	}

	private JsonObject getServerData() {
		JsonObject data = new JsonObject();
		data.addProperty("serverUUID", this.serverUniqueId);
		data.addProperty("playerAmount", Math.min(ProxyServer.getInstance().getOnlineCount(), 500));
		data.addProperty("managedServers", ProxyServer.getInstance().getServers().size());
		data.addProperty("onlineMode", ProxyServer.getInstance().getConfig().isOnlineMode() ? 1 : 0);
		data.addProperty("bungeecordVersion", ProxyServer.getInstance().getVersion());
		data.addProperty("javaVersion", System.getProperty("java.version"));
		data.addProperty("osName", System.getProperty("os.name"));
		data.addProperty("osArch", System.getProperty("os.arch"));
		data.addProperty("osVersion", System.getProperty("os.version"));
		data.addProperty("coreCount", Runtime.getRuntime().availableProcessors());
		return data;
	}

	public JsonObject getPluginData() {
		JsonObject data = new JsonObject();
		data.addProperty("pluginName", this.plugin.getDescription().getName() + "Bungee");
		data.addProperty("pluginVersion", this.plugin.getDescription().getVersion());
		data.add("customCharts", new JsonArray());
		return data;
	}

	private void sendData(JsonObject data) throws Exception {
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
