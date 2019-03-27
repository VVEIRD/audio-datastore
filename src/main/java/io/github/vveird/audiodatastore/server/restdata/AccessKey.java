package io.github.vveird.audiodatastore.server.restdata;

import java.util.UUID;

public class AccessKey {
	
	private String storageId = null;
	
	private String secret;
	
	private String id;
	
	public AccessKey() {
	}

	public AccessKey(String storageId, String key, String id) {
		this.storageId = storageId;
		this.secret = key;
		this.id = id;
	}

	public String getSecret() {
		return secret;
	}

	public void setSecret(String key) {
		this.secret = key;
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
		private String secret;
		private String id;

		public AccessKeyBuilder storageId(String storageId) {
			this.storageId = storageId;
			return this;
		}

		public AccessKeyBuilder secret(String secret) {
			this.secret = secret;
			return this;
		}

		public AccessKeyBuilder keygen() {
			this.secret = UUID.randomUUID().toString();
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
			return new AccessKey(storageId, secret, id);
		}
	}
}
