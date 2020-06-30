package dev.jaqobb.influencers.spigot.command;

import dev.jaqobb.influencers.spigot.InfluencersSpigotPlugin;
import dev.jaqobb.influencers.spigot.util.ColorHelper;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class InfluencersCommand implements CommandExecutor {

	private InfluencersSpigotPlugin plugin;

	public InfluencersCommand(InfluencersSpigotPlugin plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] arguments) {
		if (!sender.hasPermission("influencers.command.influencers")) {
			sender.sendMessage(ColorHelper.colorize(this.plugin.getMessages().getSingle("general.no-permission")));
			return true;
		}
		for (String message : this.plugin.getMessages().getMulti("general.help")) {
			message = message.replace("{version}", this.plugin.getDescription().getVersion());
			message = message.replace("{author}", this.plugin.getDescription().getAuthors().get(0));
			sender.sendMessage(ColorHelper.colorize(message));
		}
		return true;
	}
}
