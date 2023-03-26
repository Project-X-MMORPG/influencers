package dev.arttention.influencers.bungee.command;

import dev.arttention.influencers.bungee.util.ColorHelper;
import dev.arttention.influencers.api.rank.type.YouTubeRank;
import dev.arttention.influencers.api.service.ServiceType;
import dev.arttention.influencers.api.service.list.YouTubeService;
import dev.arttention.influencers.api.service.list.YouTubeService.Channel;
import dev.arttention.influencers.api.user.User;
import dev.arttention.influencers.api.util.common.YouTubeHelper;
import dev.arttention.influencers.bungee.InfluencersBungeePlugin;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.stream.Collectors;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;

public class YouTubeCommand extends Command implements TabExecutor {

    private InfluencersBungeePlugin plugin;

    public YouTubeCommand(InfluencersBungeePlugin plugin) {
        super("youtuber", null, "ytuber");
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender sender, String[] arguments) {
        if (!this.plugin.getConfiguration().isYouTubeEnabled()) {
            sender.sendMessage(ColorHelper.colorize(this.plugin.getMessages().getSingle("general.service-disabled")));
            return;
        }
        if (!(sender instanceof ProxiedPlayer)) {
            sender.sendMessage(ColorHelper.colorize(this.plugin.getMessages().getSingle("general.not-player")));
            return;
        }
        ProxiedPlayer player = (ProxiedPlayer) sender;
        if (!player.hasPermission("influencers.command.youtube")) {
            player.sendMessage(ColorHelper.colorize(this.plugin.getMessages().getSingle("general.no-permission")));
            return;
        }
        if (arguments.length == 0) {
            for (String message : this.plugin.getMessages().getMulti("youtube.help")) {
                player.sendMessage(ColorHelper.colorize(message));
            }
            return;
        }
        if (arguments[0].equalsIgnoreCase("information") || arguments[0].equalsIgnoreCase("info") || arguments[0].equalsIgnoreCase("view")) {
            User user;
            if (arguments.length > 1) {
                user = this.plugin.getUserRepository().get(arguments[1]);
            } else {
                user = this.plugin.getUserRepository().get(player.getUniqueId());
            }
            if (user == null) {
                if (arguments.length > 1) {
                    player.sendMessage(ColorHelper.colorize(this.plugin.getMessages().getSingle("youtube.user-no-data.player")));
                } else {
                    player.sendMessage(ColorHelper.colorize(this.plugin.getMessages().getSingle("youtube.user-no-data.you")));
                }
                return;
            }
            if (!user.hasServiceLinked(ServiceType.YOUTUBE)) {
                if (arguments.length > 1) {
                    player.sendMessage(ColorHelper.colorize(this.plugin.getMessages().getSingle("youtube.not-linked.player")));
                } else {
                    player.sendMessage(ColorHelper.colorize(this.plugin.getMessages().getSingle("youtube.not-linked.you")));
                }
                return;
            }
            YouTubeService service = (YouTubeService) user.getService(ServiceType.YOUTUBE);
            if (!service.isChannelVerified()) {
                if (arguments.length > 1) {
                    player.sendMessage(ColorHelper.colorize(this.plugin.getMessages().getSingle("youtube.not-verified.player")));
                } else {
                    player.sendMessage(ColorHelper.colorize(this.plugin.getMessages().getSingle("youtube.not-verified.you")));
                }
                return;
            }
            Channel channel = service.getChannel();
            if (channel == null || service.getCurrentRank() == null) {
                if (arguments.length > 1) {
                    player.sendMessage(ColorHelper.colorize(this.plugin.getMessages().getSingle("youtube.no-channel-information.player")));
                } else {
                    player.sendMessage(ColorHelper.colorize(this.plugin.getMessages().getSingle("youtube.no-channel-information.you")));
                }
                return;
            }
            for (String message : this.plugin.getMessages().getMulti("youtube.information")) {
                if (message.contains("{channel}")) {
                    message = this.plugin.getMessages().getSingle("youtube.channel-variable.message");
                    message = message.replace("{player}", user.getName());
                    TextComponent messageComponent = new TextComponent(ColorHelper.colorizeAbnormal(message));
                    messageComponent.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://www.youtube.com/channel/" + service.getChannelId()));
                    String hoverMessage = this.plugin.getMessages().getSingle("youtube.channel-variable.hover");
                    if (hoverMessage != null && !hoverMessage.isEmpty()) {
                        messageComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, ColorHelper.colorize(hoverMessage.replace("{player}", user.getName()))));
                    }
                    player.sendMessage(messageComponent);
                } else {
                    message = message.replace("{player}", user.getName());
                    message = message.replace("{subscribers}", channel.getSubscribers() == -1 ? "not visible" : String.valueOf(channel.getSubscribers()));
                    message = message.replace("{videos}", String.valueOf(channel.getVideos()));
                    message = message.replace("{total_views}", String.valueOf(channel.getTotalViews()));
                    message = message.replace("{average_views}", String.valueOf(channel.getAverageViews()));
                    String lastVideoPublishTime = "no information";
                    if (!channel.getLastVideos().isEmpty()) {
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(this.plugin.getConfiguration().getTimeFormat());
                        LocalDateTime publishTime = LocalDateTime.ofInstant(channel.getLastVideos().get(0).getPublishTime(), this.plugin.getConfiguration().getTimeZone());
                        lastVideoPublishTime = formatter.format(publishTime);
                    }
                    message = message.replace("{last_video_publish_time}", lastVideoPublishTime);
                    message = message.replace("{current_rank}", service.getCurrentRank().getName());
                    player.sendMessage(ColorHelper.colorize(message));
                }
            }
            return;
        }
        if (arguments[0].equalsIgnoreCase("link")) {
            if (arguments.length < 2) {
                player.sendMessage(ColorHelper.colorize(this.plugin.getMessages().getSingle("youtube.link-usage")));
                return;
            }
            User user = this.plugin.getUserRepository().get(player.getUniqueId());
            if (user == null) {
                player.sendMessage(ColorHelper.colorize(this.plugin.getMessages().getSingle("youtube.user-no-data.you")));
                return;
            }
            if (user.hasServiceBlacklisted(ServiceType.YOUTUBE)) {
                player.sendMessage(ColorHelper.colorize(this.plugin.getMessages().getSingle("general.service-blacklisted.you")));
                return;
            }
            if (user.hasServiceLinked(ServiceType.YOUTUBE)) {
                YouTubeService service = (YouTubeService) user.getService(ServiceType.YOUTUBE);
                if (service.isChannelVerified()) {
                    player.sendMessage(ColorHelper.colorize(this.plugin.getMessages().getSingle("youtube.already-linked-and-verified")));
                } else {
                    for (String message : this.plugin.getMessages().getMulti("youtube.already-linked.you")) {
                        message = message.replace("{verification_key}", service.getChannelVerificationKey());
                        player.sendMessage(ColorHelper.colorize(message));
                    }
                }
                return;
            }
            String channelId = YouTubeHelper.escapeYouTubeLink(arguments[1]);
            for (User cache : this.plugin.getUserRepository().getAll()) {
                if (cache.hasServiceBlacklisted(ServiceType.YOUTUBE)) {
                    continue;
                }
                if (!cache.hasServiceLinked(ServiceType.YOUTUBE)) {
                    continue;
                }
                YouTubeService service = (YouTubeService) cache.getService(ServiceType.YOUTUBE);
                if (service.getChannelId().equals(channelId)) {
                    player.sendMessage(ColorHelper.colorize(this.plugin.getMessages().getSingle("youtube.taken")));
                    return;
                }
            }
            YouTubeService service = new YouTubeService(user.getName(), channelId);
            user.linkService(service);
            for (String message : this.plugin.getMessages().getMulti("youtube.linked")) {
                message = message.replace("{verification_key}", service.getChannelVerificationKey());
                player.sendMessage(ColorHelper.colorize(message));
            }
            return;
        }
        if (arguments[0].equalsIgnoreCase("unlink")) {
            User user = this.plugin.getUserRepository().get(player.getUniqueId());
            if (user == null) {
                player.sendMessage(ColorHelper.colorize(this.plugin.getMessages().getSingle("youtube.user-no-data.you")));
                return;
            }
            if (!user.hasServiceLinked(ServiceType.YOUTUBE)) {
                player.sendMessage(ColorHelper.colorize(this.plugin.getMessages().getSingle("youtube.not-linked.you")));
                return;
            }
            YouTubeRank noRank = this.plugin.getConfiguration().getYouTubeRanks().get(0);
            for (String noRankCommand : noRank.getCommands()) {
                ProxyServer.getInstance().getPluginManager().dispatchCommand(ProxyServer.getInstance().getConsole(), noRankCommand.replace("{player}", user.getName()));
            }
            user.unlinkService(ServiceType.YOUTUBE);
            player.sendMessage(ColorHelper.colorize(this.plugin.getMessages().getSingle("youtube.unlinked")));
            return;
        }
        if (arguments[0].equalsIgnoreCase("verify")) {
            User user = this.plugin.getUserRepository().get(player.getUniqueId());
            if (user == null) {
                player.sendMessage(ColorHelper.colorize(this.plugin.getMessages().getSingle("youtube.user-no-data.you")));
                return;
            }
            if (user.hasServiceBlacklisted(ServiceType.YOUTUBE)) {
                player.sendMessage(ColorHelper.colorize(this.plugin.getMessages().getSingle("general.service-blacklisted.you")));
                return;
            }
            if (!user.hasServiceLinked(ServiceType.YOUTUBE)) {
                player.sendMessage(ColorHelper.colorize(this.plugin.getMessages().getSingle("youtube.not-linked.you")));
                return;
            }
            YouTubeService service = (YouTubeService) user.getService(ServiceType.YOUTUBE);
            if (service.isChannelVerified()) {
                player.sendMessage(ColorHelper.colorize(this.plugin.getMessages().getSingle("youtube.already-verified")));
                return;
            }
            this.plugin.getProxy().getScheduler().runAsync(this.plugin, () -> {
                String result = service.verify(this.plugin);
                if (result != null) {
                    for (String message : this.plugin.getMessages().getMulti("youtube.verify-error.provided")) {
                        message = message.replace("{error}", result);
                        message = message.replace("{verification_key}", service.getChannelVerificationKey());
                        player.sendMessage(ColorHelper.colorize(message));
                    }
                    return;
                }
                if (service.isChannelVerified()) {
                    this.plugin.getUserRepository().updateService(user, ServiceType.YOUTUBE);
                    player.sendMessage(ColorHelper.colorize(this.plugin.getMessages().getSingle("youtube.verified")));
                } else {
                    for (String message : this.plugin.getMessages().getMulti("youtube.verify-error.unknown")) {
                        message = message.replace("{verification_key}", service.getChannelVerificationKey());
                        player.sendMessage(ColorHelper.colorize(message));
                    }
                }
            });
            return;
        }
        if (arguments[0].equalsIgnoreCase("add")) {
            if (!player.hasPermission("influencers.command.youtube.administrator") && !player.hasPermission("influencers.command.youtube.admin")) {
                player.sendMessage(ColorHelper.colorize(this.plugin.getMessages().getSingle("general.no-permission")));
                return;
            }
            if (arguments.length < 3) {
                player.sendMessage(ColorHelper.colorize(this.plugin.getMessages().getSingle("youtube.add-usage")));
                return;
            }
            User user = this.plugin.getUserRepository().get(arguments[1]);
            if (user == null) {
                player.sendMessage(ColorHelper.colorize(this.plugin.getMessages().getSingle("youtube.user-no-data.player")));
                return;
            }
            if (user.hasServiceBlacklisted(ServiceType.YOUTUBE)) {
                player.sendMessage(ColorHelper.colorize(this.plugin.getMessages().getSingle("general.service-blacklisted.player")));
                return;
            }
            if (user.hasServiceLinked(ServiceType.YOUTUBE)) {
                player.sendMessage(ColorHelper.colorize(this.plugin.getMessages().getSingle("youtube.already-linked.player")));
                return;
            }
            user.linkService(new YouTubeService(user.getName(), arguments[2], true));
            this.plugin.getProxy().getScheduler().runAsync(this.plugin, () -> this.plugin.getUserRepository().updateService(user, ServiceType.YOUTUBE));
            player.sendMessage(ColorHelper.colorize(this.plugin.getMessages().getSingle("youtube.added").replace("{player}", user.getName())));
            return;
        }
        if (arguments[0].equalsIgnoreCase("remove")) {
            if (!player.hasPermission("influencers.command.youtube.administrator") && !player.hasPermission("influencers.command.youtube.admin")) {
                player.sendMessage(ColorHelper.colorize(this.plugin.getMessages().getSingle("general.no-permission")));
                return;
            }
            if (arguments.length < 2) {
                player.sendMessage(ColorHelper.colorize(this.plugin.getMessages().getSingle("youtube.remove-usage")));
                return;
            }
            User user = this.plugin.getUserRepository().get(arguments[1]);
            if (user == null) {
                player.sendMessage(ColorHelper.colorize(this.plugin.getMessages().getSingle("youtube.user-no-data.player")));
                return;
            }
            if (!user.hasServiceLinked(ServiceType.YOUTUBE)) {
                player.sendMessage(ColorHelper.colorize(this.plugin.getMessages().getSingle("youtube.not-linked.player")));
                return;
            }
            YouTubeRank noRank = this.plugin.getConfiguration().getYouTubeRanks().get(0);
            for (String noRankCommand : noRank.getCommands()) {
                ProxyServer.getInstance().getPluginManager().dispatchCommand(ProxyServer.getInstance().getConsole(), noRankCommand.replace("{player}", user.getName()));
            }
            user.unlinkService(ServiceType.YOUTUBE);
            player.sendMessage(ColorHelper.colorize(this.plugin.getMessages().getSingle("youtube.removed").replace("{player}", user.getName())));
            return;
        }
        if (arguments[0].equalsIgnoreCase("blacklist")) {
            if (!player.hasPermission("influencers.command.youtube.administrator") && !player.hasPermission("influencers.command.youtube.admin")) {
                player.sendMessage(ColorHelper.colorize(this.plugin.getMessages().getSingle("general.no-permission")));
                return;
            }
            if (arguments.length < 2) {
                player.sendMessage(ColorHelper.colorize(this.plugin.getMessages().getSingle("youtube.blacklist-usage")));
                return;
            }
            User user = this.plugin.getUserRepository().get(arguments[1]);
            if (user == null) {
                player.sendMessage(ColorHelper.colorize(this.plugin.getMessages().getSingle("youtube.user-no-data.player")));
                return;
            }
            if (user.hasServiceBlacklisted(ServiceType.YOUTUBE)) {
                player.sendMessage(ColorHelper.colorize(this.plugin.getMessages().getSingle("youtube.service-already-blacklisted")));
                return;
            }
            if (user.hasServiceLinked(ServiceType.YOUTUBE)) {
                player.sendMessage(ColorHelper.colorize(this.plugin.getMessages().getSingle("youtube.blacklist-unlink-first")));
                return;
            }
            user.blacklistService(ServiceType.YOUTUBE);
            player.sendMessage(ColorHelper.colorize(this.plugin.getMessages().getSingle("youtube.blacklisted")));
            return;
        }
        if (arguments[0].equalsIgnoreCase("unblacklist")) {
            if (!player.hasPermission("influencers.command.youtube.administrator") && !player.hasPermission("influencers.command.youtube.admin")) {
                player.sendMessage(ColorHelper.colorize(this.plugin.getMessages().getSingle("general.no-permission")));
                return;
            }
            if (arguments.length < 2) {
                player.sendMessage(ColorHelper.colorize(this.plugin.getMessages().getSingle("youtube.unblacklist-usage")));
                return;
            }
            User user = this.plugin.getUserRepository().get(arguments[1]);
            if (user == null) {
                player.sendMessage(ColorHelper.colorize(this.plugin.getMessages().getSingle("youtube.user-no-data.player")));
                return;
            }
            if (!user.hasServiceBlacklisted(ServiceType.YOUTUBE)) {
                player.sendMessage(ColorHelper.colorize(this.plugin.getMessages().getSingle("youtube.service-not-blacklisted")));
                return;
            }
            user.unblacklistService(ServiceType.YOUTUBE);
            player.sendMessage(ColorHelper.colorize(this.plugin.getMessages().getSingle("youtube.unblacklisted")));
            return;
        }
        for (String message : this.plugin.getMessages().getMulti("youtube.help")) {
            player.sendMessage(ColorHelper.colorize(message));
        }
    }

    @Override
    public Iterable<String> onTabComplete(CommandSender sender, String[] arguments) {
        if (!this.plugin.getConfiguration().isYouTubeEnabled()) {
            return Collections.emptyList();
        }
        if (!(sender instanceof ProxiedPlayer)) {
            return Collections.emptyList();
        }
        ProxiedPlayer player = (ProxiedPlayer) sender;
        if (!player.hasPermission("influencers.command.youtube")) {
            return Collections.emptyList();
        }
        if (arguments.length != 2) {
            return Collections.emptyList();
        }
        if (!arguments[0].equalsIgnoreCase("information") && !arguments[0].equalsIgnoreCase("info") && !arguments[0].equalsIgnoreCase("view")) {
            return Collections.emptyList();
        }
        return this.plugin.getUserRepository().getAll().stream()
                .filter(user -> {
                    if (user.hasServiceBlacklisted(ServiceType.YOUTUBE)) {
                        return false;
                    }
                    if (!user.hasServiceLinked(ServiceType.YOUTUBE)) {
                        return false;
                    }
                    String argument = arguments[1].toLowerCase();
                    if (argument.isEmpty()) {
                        return true;
                    }
                    return user.getName().toLowerCase().startsWith(argument);
                })
                .map(User::getName)
                .sorted()
                .collect(Collectors.toList());
    }
}
