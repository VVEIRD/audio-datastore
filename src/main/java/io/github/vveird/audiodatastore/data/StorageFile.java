package io.github.vveird.audiodatastore.data;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class StorageFile {

	private String uploadPath = null;

	private String uploadFileName = null;

	private String localFile = null;
	
	public StorageFile() {
	}

	public StorageFile(String uploadPath, String uploadFileName, String localFile) {
		super();
		this.uploadPath = uploadPath;
		this.uploadFileName = uploadFileName;
		this.localFile = localFile;
	}

	public String getUploadPath() {
		return uploadPath;
	}

	public void setUploadPath(String uploadPath) {
		this.uploadPath = uploadPath;
	}

	public String getUploadFileName() {
		return uploadFileName;
	}

	public void setUploadFileName(String uploadFileName) {
		this.uploadFileName = uploadFileName;
	}

	public String getLocalFile() {
		return localFile;
	}

	public void setLocalFile(String localFile) {
		this.localFile = localFile;
	}
	
	public boolean exists() {
		return Files.exists(Paths.get(this.localFile));
	}
	
	public boolean save(InputStream is) throws IOException {
		return Files.copy(is, Paths.get(this.localFile), StandardCopyOption.REPLACE_EXISTING) > 0;
	}
	public boolean delete() throws IOException {
		return Files.deleteIfExists(Paths.get(this.localFile));
	}

	public static StorageFile.StorageFileBuilder builder() {
		return new StorageFileBuilder();
	}

	public static class StorageFileBuilder {
		private String uploadPath = null;
		private String uploadFileName = null;
		private String localFile = null;

		public StorageFile.StorageFileBuilder uploadPath(String uploadPath) {
			this.uploadPath = uploadPath;
			return this;
		}

		public StorageFile.StorageFileBuilder uploadFileName(String uploadFileName) {
			this.uploadFileName = uploadFileName;
			return this;
		}

		public StorageFile.StorageFileBuilder localFile(String localFile) {
			this.localFile = localFile;
			return this;
		}

		public StorageFile build() {
			return new StorageFile(uploadPath, uploadFileName, localFile);
		}
	}

	public static StorageFile noFile() {
		return new StorageFile(null, null, null);
	}
}