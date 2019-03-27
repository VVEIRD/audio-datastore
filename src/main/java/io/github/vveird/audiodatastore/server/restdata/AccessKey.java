package io.github.vveird.audiodatastore.server.restdata;

import java.util.UUID;

public class AccessKey {
	
	private String storageId = null;
	
	private String owner;
	
	private String id;
	
	public AccessKey() {
	}

	public AccessKey(String storageId, String key, String id) {
		this.storageId = storageId;
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

	public void setId(String id) {
		this.id = id;
	}
	
	public String getStorageId() {
		return storageId;
	}
	
	public void setStorageId(String storageId) {
		this.storageId = storageId;
	}

	public static AccessKeyBuilder builder() {
		return new AccessKeyBuilder();
	}

	public static class AccessKeyBuilder {

		private String storageId = null;
		private String key;
		private String id;

		public AccessKeyBuilder storageId(String storageId) {
			this.storageId = storageId;
			return this;
		}

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
			return new AccessKey(storageId, key, id);
		}
	}
}
