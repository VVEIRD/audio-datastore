package io.github.vveird.audiodatastore.restdata;

public class MediaEntry {
	public int returnCode = 404;
	public String baseUrl;
	public String id;
	public String name;
	
	
	public MediaEntry() {
	}

	public MediaEntry(int returnCode, String baseUrl, String id, String name) {
		super();
		this.returnCode = returnCode;
		this.baseUrl = baseUrl;
		this.id = id;
		this.name = name;
	}

	public int getReturnCode() {
		return returnCode;
	}

	public void setReturnCode(int returnCode) {
		this.returnCode = returnCode;
	}

	public String getBaseUrl() {
		return baseUrl;
	}

	public void setBaseUrl(String baseUrl) {
		this.baseUrl = baseUrl;
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
	
	

}
