package dev.arttention.influencers.api.util.common;

import java.util.List;

public final class StringHelper {

	private StringHelper() {
		throw new UnsupportedOperationException("Cannot create instance of this class");
	}

	public static boolean containsIgnoreCase(String string, String searchString) {
		if (string == null || searchString == null) {
			return false;
		}
		int length = searchString.length();
		if (length == 0) {
			return true;
		}
		for (int index = string.length() - length; index >= 0; index--) {
			if (string.regionMatches(true, index, searchString, 0, length)) {
				return true;
			}
		}
		return false;
	}

	public static boolean containsIgnoreCase(String string, List<String> searchList) {
		for (String searchString : searchList) {
			if (containsIgnoreCase(string, searchString)) {
				return true;
			}
		}
		return false;
	}
}
