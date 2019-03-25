package io.github.vveird.audiodatastore.data;

import java.util.UUID;

public class AccessKey {
	
	private String owner;
	
	private String id;
	
	public AccessKey() {
	}

	public AccessKey(String key, String id) {
		this.owner = key;
		this.id = id;
	}

	public String getOwner() {
		return owner;
	}

	public void setOwner(String key) {
		this.owner = key;
	}

	public String getId() {
		return id;
	}

	public void setid(String id) {
		this.id = id;
	}

	public static AccessKeyBuilder builder() {
		return new AccessKeyBuilder();
	}

	public static class AccessKeyBuilder {

		private String key;
		private String id;

		public AccessKeyBuilder key(String key) {
			this.key = key;
			return this;
		}

		public AccessKeyBuilder keygen() {
			this.key = UUID.randomUUID().toString();
			return this;
		}

		public AccessKeyBuilder idgen() {
			this.id = UUID.randomUUID().toString();
			return this;
		}
		
		public AccessKeyBuilder id(String id) {
			this.id = id;
			return this;
		}
		
		public AccessKey build() {
			return new AccessKey(key, id);
		}
	}
}
