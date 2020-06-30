package dev.jaqobb.influencers.bungee.util;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;

public final class ColorHelper {

	private ColorHelper() {
		throw new UnsupportedOperationException("Cannot create instance of this class");
	}

	public static BaseComponent[] colorize(String string) {
		return TextComponent.fromLegacyText(colorizeAbnormal(string));
	}

	public static String colorizeAbnormal(String string) {
		return ChatColor.translateAlternateColorCodes('&', string);
	}
}
