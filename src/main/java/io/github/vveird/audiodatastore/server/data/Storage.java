package io.github.vveird.audiodatastore.server.data;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class Storage {
	
	private String root = "data";

	private Map<String, StorageFile> storagePaths = new HashMap<>();
	
	public Storage() {
	}

	public Storage(String root) {
		super();
		this.root = root;
	}

	public int saveFile(String id, String uploadPath, String uploadFileName, InputStream uploadedInputStream) throws IOException {
		uploadPath = String.format("/%s/%s", id, uploadPath);
		// File already exists
		if(storagePaths.containsKey(uploadPath))
			return 409;
		StorageFile sf = null;
		do {
			sf = StorageFile.builder()
					.localFile(Paths.get(this.root, UUID.randomUUID().toString() + ".bin").toString())
					.uploadFileName(uploadFileName)
					.uploadPath(uploadPath)
					.build();
		} while(sf.exists());
		boolean saved = sf.save(uploadedInputStream);
		if(saved)
			storagePaths.put(uploadPath, sf);
		return saved ? 200 : 500;
	}

	public int deleteFile(String id, String uploadPath) throws IOException {
		int rc = 500;
		uploadPath = String.format("/%s/%s", id, uploadPath);
		if(storagePaths.containsKey(uploadPath)) {
			StorageFile f = storagePaths.get(uploadPath); 
			boolean deleted = f.delete();
			if(deleted) {
				rc = 200;
				storagePaths.remove(uploadPath);
			}
			else if(!f.exists()){
				rc = 404;
				storagePaths.remove(uploadPath);
			}
		}
		return rc;
	}

	public void deleteAll(String id) {
		storagePaths.keySet().stream().filter(k -> k.startsWith("/" + id)).map(k -> storagePaths.get(k)).forEach(f -> {
			try {
				f.delete();
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
	}
	
	public Map<String, StorageFile> getStoragePaths() {
		return storagePaths;
	}
	
	public void setStoragePaths(Map<String, StorageFile> storagePaths) {
		this.storagePaths = storagePaths;
	}

	public StorageFile getFile(String id, String uploadPath) {
		uploadPath = String.format("/%s/%s", id, uploadPath);
		return storagePaths.containsKey(uploadPath) ? storagePaths.get(uploadPath) : StorageFile.noFile();
	}
	
	public void setRoot(String root) {
		this.root = root;
	}
	
	public String getRoot() {
		return root;
	}
}
