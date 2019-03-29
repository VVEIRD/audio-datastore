package io.github.vveird.audiodatastore.server.restdata;

public class StorageInfo {
	
	private String storageId = null;
	
	private String endpoint = null;
	
	private long freeSpace = 0;

	public StorageInfo(String storageId, String endpoint, long freeSpace) {
		super();
		this.storageId = storageId;
		this.endpoint = endpoint;
		this.freeSpace = freeSpace;
	}

	public String getStorageId() {
		return storageId;
	}

	public void setStorageId(String storageId) {
		this.storageId = storageId;
	}

	public String getEndpoint() {
		return endpoint;
	}

	public void setEndpoint(String endpoint) {
		this.endpoint = endpoint;
	}

	public long getFreeSpace() {
		return freeSpace;
	}

	public void setFreeSpace(long freeSpace) {
		this.freeSpace = freeSpace;
	}
	
	public static StorageInfoBuilder builder() {
		return new StorageInfoBuilder();
	}
	
	public static class StorageInfoBuilder {
		
		private String storageId = null;
		private String endpoint = null;
		private long freeSpace = 0;
		
		public StorageInfoBuilder storageId(String storageId) {
			this.storageId = storageId;
			return this;
		}
		public StorageInfoBuilder endpoint(String endpoint) {
			this.endpoint = endpoint;
			return this;
		}
		public StorageInfoBuilder freeSpace(long freeSpace) {
			this.freeSpace = freeSpace;
			return this;
		}
		
		public StorageInfo build() {
			return new StorageInfo(storageId, endpoint, freeSpace);
		}
	}
}
