package io.github.vveird.audiodatastore.server.restdata;

public class MediaEntry {
	public int returnCode = 404;
	public String storageId;
	public String endpoint;
	public String id;
	public String name;
	
	
	public MediaEntry() {
	}

	public MediaEntry(int returnCode, String baseUrl, String id, String name) {
		super();
		this.returnCode = returnCode;
		this.endpoint = baseUrl;
		this.id = id;
		this.name = name;
	}

	public int getReturnCode() {
		return returnCode;
	}

	public void setReturnCode(int returnCode) {
		this.returnCode = returnCode;
	}

	public String getEndpoint() {
		return endpoint;
	}

	public void setEndpoint(String baseUrl) {
		this.endpoint = baseUrl;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public static MediaEntryBuilder builder() {
		return new MediaEntryBuilder();
	}
	
	public String getStorageId() {
		return storageId;
	}
	
	public void setStorageId(String storageId) {
		this.storageId = storageId;
	}
	
	public static class MediaEntryBuilder {
		private MediaEntry e = new MediaEntry();
		
		public MediaEntryBuilder name(String name) {
			e.name = name;
			return this;
		}
		
		public MediaEntryBuilder returnCode(int returnCode) {
			e.returnCode = returnCode;
			return this;
		}
		
		public MediaEntryBuilder storageId(String storageId) {
			e.storageId = storageId;
			return this;
		}
		
		public MediaEntryBuilder id(String id) {
			e.id = id;
			return this;
		}
		
		public MediaEntryBuilder endpoint(String endpoint) {
			e.endpoint = endpoint;
			return this;
		}
		
		public MediaEntry build() {
			return e;
		}
	}
}
