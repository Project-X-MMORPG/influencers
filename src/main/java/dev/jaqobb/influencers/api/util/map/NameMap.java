package dev.jaqobb.influencers.api.util.map;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;

public class NameMap<T> {

	private Map<String, T> byName;
	private Map<T, String> byValue;

	public NameMap(T[] constants, Function<T, String> namer) {
		Map<String, T> byName = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
		Map<T, String> byValue = new HashMap<>(constants.length);
		for (T constant : constants) {
			String name = namer.apply(constant);
			byName.put(name, constant);
			byValue.put(constant, name);
		}
		this.byName = Collections.unmodifiableMap(byName);
		this.byValue = Collections.unmodifiableMap(byValue);
	}

	public NameMap(Map<String, T> byName, Map<T, String> byValue) {
		this.byName = byName;
		this.byValue = byValue;
	}

	public String name(T value) {
		return this.byValue.get(value);
	}

	public T value(String name) {
		return this.byName.get(name);
	}
}
