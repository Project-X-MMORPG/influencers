package dev.jaqobb.influencers.api.util.common;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

public final class YouTubeHelper {

	public static final int YOUTUBE_CHANNEL_ID_LENGTH = 24;
	public static final Collection<String> YOUTUBE_PREFIXES = Collections.unmodifiableCollection(Arrays.asList(
		"https://www.youtube.com/channel/",
		"https://youtube.com/channel/",
		"http://www.youtube.com/channel/",
		"http://youtube.com/channel/",
		"www.youtube.com/channel/",
		"youtube.com/channel/"
	));

	private YouTubeHelper() {
		throw new UnsupportedOperationException("Cannot create instance of this class");
	}

	public static String escapeYouTubeLink(String string) {
		for (String prefix : YOUTUBE_PREFIXES) {
			if (string.startsWith(prefix)) {
				string = string.substring(prefix.length());
			}
		}
		if (string.length() > YOUTUBE_CHANNEL_ID_LENGTH) {
			string = string.substring(0, YOUTUBE_CHANNEL_ID_LENGTH);
		}
		return string;
	}
}
