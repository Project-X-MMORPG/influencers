package dev.arttention.influencers.api.basic;

import java.util.List;

public interface Messages {

	String getSingle(String path);

	void setSingle(String path, String value);

	List<String> getMulti(String path);

	void setMulti(String path, List<String> value);

	void load();

	void save();

	void reload();
}
