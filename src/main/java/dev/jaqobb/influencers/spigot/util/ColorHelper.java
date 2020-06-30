package dev.jaqobb.influencers.spigot.util;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;

public final class ColorHelper {

	private ColorHelper() {
		throw new UnsupportedOperationException("Cannot create instance of this class");
	}

	public static String colorize(String string) {
		return ChatColor.translateAlternateColorCodes('&', string);
	}

	public static BaseComponent[] colorizeAbnormal(String string) {
		return TextComponent.fromLegacyText(colorize(string));
	}
}
