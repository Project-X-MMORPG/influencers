package dev.jaqobb.influencers.bungee.command;

import dev.jaqobb.influencers.bungee.InfluencersBungeePlugin;
import dev.jaqobb.influencers.bungee.util.ColorHelper;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;

public class InfluencersCommand extends Command {

	private InfluencersBungeePlugin plugin;

	public InfluencersCommand(InfluencersBungeePlugin plugin) {
		super("influencers");
		this.plugin = plugin;
	}

	@Override
	public void execute(CommandSender sender, String[] arguments) {
		if (!sender.hasPermission("influencers.command.influencers")) {
			sender.sendMessage(ColorHelper.colorize(this.plugin.getMessages().getSingle("general.no-permission")));
			return;
		}
		for (String message : this.plugin.getMessages().getMulti("general.help")) {
			message = message.replace("{version}", this.plugin.getDescription().getVersion());
			message = message.replace("{author}", this.plugin.getDescription().getAuthor());
			sender.sendMessage(ColorHelper.colorize(message));
		}
	}
}
