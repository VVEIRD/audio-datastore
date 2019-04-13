package io.github.vveird.audiodatastore.restdata;

import java.beans.Transient;
import java.util.List;

import io.github.vveird.audiodatastore.server.AudioStorage;

public class HttpAccess {
	
	private String id = null;

	private List<String> read = null;

	private List<String> write = null;
	
	private List<String> delete = null;
	
	public HttpAccess() {
	}
	
	@Transient
	public String getSecret() {
		return AudioStorage.getInstance().getSecret(id);
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public List<String> getRead() {
		return read;
	}

	public void setRead(List<String> read) {
		this.read = read;
	}
	
	public void addReadAccess(String uuid) {
		this.read.add(uuid);
	}
	
	public void removeReadAccess(String uuid) {
		this.read.add(uuid);
	}

	public List<String> getWrite() {
		return write;
	}

	public void setWrite(List<String> write) {
		this.write = write;
	}
	
	public void addWriteAccess(String uuid) {
		this.write.add(uuid);
	}
	
	public void removeWriteAccess(String uuid) {
		this.write.add(uuid);
	}

	public List<String> getDelete() {
		return delete;
	}

	public void setDelete(List<String> delete) {
		this.delete = delete;
	}
	
	public void addDeleteAccess(String uuid) {
		this.delete.add(uuid);
	}
	
	public void removeDeleteAccess(String uuid) {
		this.delete.add(uuid);
	}

}
