package dev.jaqobb.influencers.api.rank;

public abstract class Rank {

	private String id;
	private String name;

	protected Rank(String id, String name) {
		this.id = id;
		this.name = name;
	}

	public String getId() {
		return this.id;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return "Rank{" +
			"id='" + this.id + "'" +
			", name='" + this.name + "'" +
			"}";
	}
}
