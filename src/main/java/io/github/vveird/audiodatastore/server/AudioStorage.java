package io.github.vveird.audiodatastore.server;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.yaml.snakeyaml.Yaml;

import io.github.vveird.audiodatastore.server.data.Storage;
import io.github.vveird.audiodatastore.server.data.StorageFile;
import io.github.vveird.audiodatastore.server.restdata.AccessKey;
import io.github.vveird.audiodatastore.server.restdata.HttpAccess;

public class AudioStorage {
	
	private static AudioStorage instance = null;

	public synchronized static AudioStorage getInstance()  {
		if(instance == null)
			instance = new AudioStorage();
		return instance;
	}
	
	private AudioStorageConfiguration config = null;
	
	private Storage storage = null;
	
	private HttpServer restWebServer = null;

	public AudioStorage()  {
		initConfig();
		initStorage();
	}

	private void initConfig() {
		Yaml yaml = new Yaml();
		try (InputStream in = Files.newInputStream(Paths.get("config" + File.separator + "base.yml"))) {
			config = yaml.loadAs(in, AudioStorageConfiguration.class);
		} catch (IOException e) {
			config = new AudioStorageConfiguration();
			e.printStackTrace();
		}
		if(config.getStorageId() == null) {
			config.setStorageId(UUID.randomUUID().toString());
			saveConfig();
		}
	}

	private void initStorage() {
		Yaml yaml = new Yaml();
		try (InputStream in = Files.newInputStream(Paths.get("config" + File.separator + "storage.yml"))) {
			storage = yaml.loadAs(in, Storage.class);
		} catch (IOException e) {
			storage = new Storage(config.getRoot());
			saveStorage();
			e.printStackTrace();
		}
	}
	
	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// ~~                                                      ~~
	// ~~           Up-/downloading and deleting files         ~~
	// ~~                                                      ~~
	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

	public int saveFile(String user, String id, String uploadPath, String uploadFileName,
			InputStream uploadedInputStream) {
		if(!this.hasAccess(id, user, false, true, false))
			return 403;
		int rc;
		try {
			rc = this.storage.saveFile(id, uploadPath, uploadFileName, uploadedInputStream);
			if(rc == 200)
				saveStorage();
		} catch (IOException e) {
			e.printStackTrace();
			rc = 500;
		}
		return rc;
	}

	public int deleteFile(String user, String id, String uploadPath) {
		if(!this.hasAccess(id, user, false, false, true))
			return 403;
		int rc;
		try {
			rc = this.storage.deleteFile(id, uploadPath);
			if(rc == 200 || rc == 404)
				saveStorage();
		} catch (IOException e) {
			e.printStackTrace();
			rc = 500;
		}
		return rc;
	}

	public StorageFile getFile(String user, String id, String uploadPath) {
		if(!this.hasAccess(id, user, true, false, false))
			return StorageFile.noFile();
		return this.storage.getFile(id, uploadPath);
	}
	
    /**
     * Starts Grizzly HTTP server exposing JAX-RS resources defined in this application.
     * @return Grizzly HTTP server.
     */
    public void startServer() {
        // create a resource config that scans for JAX-RS resources and providers
        // in io.github.vveird.apinstance package
        final ResourceConfig rc = new ResourceConfig().packages(config.getEndPoints().toArray(new String[0]));
        rc.register(MultiPartFeature.class);
        // create and start a new instance of grizzly http server
        // exposing the Jersey application at BASE_URI
        restWebServer = GrizzlyHttpServerFactory.createHttpServer(URI.create(config.getEndpoint()), rc);
        Logger l = Logger.getLogger("org.glassfish.grizzly.http.server.HttpHandler");
        l.setLevel(Level.FINE);
        l.setUseParentHandlers(false);
        ConsoleHandler ch = new ConsoleHandler();
        ch.setLevel(Level.ALL);
        l.addHandler(ch);
        System.out.println("Server listening on [" + config.getEndpoint() + "]");
    }

	public String getEndpoint() {
		return config.getEndpoint();
	}

	public String getFileRoot() {
		return config.getRoot();
	}

	public AccessKey generateKey() {
		AccessKey ak = AccessKey.builder().idgen().keygen().storageId(getStorageId()).build();
		config.addAccessKey(ak);
		saveConfig();
		return ak;
	}

	public boolean deleteUser(String owner) {
		List<AccessKey> ak = config.getAccessKeyByOwner(owner);
		ak.stream().forEach(k -> storage.deleteAll(k.getId()));
		config.getKeys().removeAll(ak);
		saveStorage();
		saveConfig();
		return true;
	}
	
	private void saveConfig() {
		Yaml yaml = new Yaml();
		try (OutputStreamWriter out = new OutputStreamWriter(Files.newOutputStream(Paths.get("config" + File.separator + "base.yml")))) {
			yaml.dump(config, out);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void saveStorage() {
		System.out.println("-- Saving storage information -----------------");
		Yaml yaml = new Yaml();
		try (OutputStreamWriter out = new OutputStreamWriter(Files.newOutputStream(Paths.get("config" + File.separator + "storage.yml")))) {
			yaml.dump(storage, out);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public String getStorageId() {
		return config.getStorageId();
	}

	public String getOwner(String id) {
		return config.keys.stream().filter(a -> a.getId().equals(id)).map(a -> a.getOwner()).findFirst().orElse(null);
	}
	
	public boolean transferOwnership(String id, String oldOwner, String newOwner) {
		String currentOwner = getOwner(id);
		Objects.requireNonNull(id, "id cannot be null");
		Objects.requireNonNull(oldOwner, "oldOwner cannot be null");
		Objects.requireNonNull(newOwner, "newOwner cannot be null");
		Objects.requireNonNull(currentOwner, "ID does not exists/Wrong owner supplied");
		if(!currentOwner.equals(oldOwner))
			Objects.requireNonNull(null, "ID does not exists/Wrong owner supplied");
		AccessKey ak = getAccessKey(id);
		ak.setOwner(newOwner);
		saveConfig();
		return true;
	}

	private AccessKey getAccessKey(String id) {
		return config.getAccesKeyById(id);
	}
	
	/**
	 * Checks if the given uuid has access to the given id.
	 * 
	 * @param id     ID to check for
	 * @param uuid   UUID that wants to access ID
	 * @param read   <code>true</code>: Check for read access, <code>false</code>: do not check
	 * @param write  <code>true</code>: Check for write access, <code>false</code>: do not check
	 * @param delete <code>true</code>: Check for delete access, <code>false</code>: do not check
	 * @return <code>true</code> if the selected access paths are allowed,
	 *         <code>false</code> if one or more selected access paths are forbidden.
	 */
	public boolean hasAccess(String id, String uuid, boolean read, boolean write, boolean delete) {
		Objects.requireNonNull(id, "id cannot be null");
		HttpAccess ha = config.getHttpAccessById(id);
		// If no HttpAccess exists, read access is granted to everyone and write/delete
		// access is granted to the owner only 
		if(ha == null)
			return !write && !delete && read || config.getAccesKeyById(id) != null && config.getAccesKeyById(id).getOwner().equals(uuid);
		boolean hasAccess = false;
		boolean isOwner = ha.getOwner().equals(uuid);
		boolean readAccess = ha.getRead().contains(uuid);
		boolean writeAccess = ha.getWrite().contains(uuid);
		boolean deleteAccess = ha.getDelete().contains(uuid);
		hasAccess = read ? readAccess : hasAccess;
		hasAccess = write ? writeAccess : hasAccess;
		hasAccess = delete ? deleteAccess : hasAccess;
		return isOwner || hasAccess;
	}
	
	public AudioStorageConfiguration getConfig() {
		return config;
	}

	public boolean addHttpAccess(String accessKey, HttpAccess httpAccess) {
		Objects.requireNonNull(accessKey);
		Objects.requireNonNull(httpAccess);
		if(accessKey.equals(httpAccess.getOwner())) {
			config.addHttpAccess(httpAccess);
			saveConfig();
			return true;
		}
		return false;
	}

	public boolean removeHttpAccess(String owner) {
		boolean deleted = config.removeHttpAccess(config.getHttpAccessByOwner(owner)) ;
		if(deleted)
			saveConfig();
		return deleted;
	}
	
	public HttpAccess getHttpAccess (String owner) {
		return config.getHttpAccessByOwner(owner);
	}

	public static class AudioStorageConfiguration {
		
		private String storageId = null;

		private String version = "0.1";
		
		private String root = ".";
		
		private String interfaceAdress = "localhost";
		
		private String uriBase = "as";
		
		private int port = 3002;
		
	    private List<String> endPoints;
	    
	    private List<AccessKey> keys = new LinkedList<>();
	    
	    private List<HttpAccess> accessList = new LinkedList<>();
		
		public String getRoot() {
			return root;
		}

		public String getStorageId() {
			return storageId;
		}
		
		public void setStorageId(String id) {
			this.storageId = id;
		}
		
		public void addAccessKey(AccessKey ak) {
			this.keys.add(ak);
		}
		
		public AccessKey getAccesKeyById(String id) {
			return keys.stream().filter(a -> a.getId().equals(id)).findFirst().orElse(null);
		}
		
		public List<AccessKey> getAccessKeyByOwner(String owner) {
			return keys.stream().filter(k -> k.getOwner().equals(owner)).collect(Collectors.toList());
		}
		
		public void setRoot(String root) {
			this.root = root;
		}
		
		public String getVersion() {
			return version;
		}
		
		public void setVersion(String version) {
			this.version = version;
		}
		
		public int getPort() {
			return port;
		}
		
		public void setPort(int port) {
			this.port = port;
		}
		
		public String getInterfaceAdress() {
			return interfaceAdress;
		}
		
		public void setInterfaceAdress(String interfaceAdress) {
			this.interfaceAdress = interfaceAdress;
		}
		
		public String getUriBase() {
			return uriBase;
		}
		
		public void setUriBase(String uriBase) {
			this.uriBase = uriBase;
		}
		
		public List<String> getEndPoints() {
			return endPoints;
		}
		
		public void setEndPoints(List<String> endPoints) {
			this.endPoints = endPoints;
		}
		
		public String getEndpoint() {
			return String.format("http://%s:%d/%s/", interfaceAdress, port, uriBase);
		}
		
		@Override
		public String toString() {
			String tos = String.format("AudioStorage\r\nVersion: %s\r\nRoot: %s\r\nURL: %s", version, root, getEndpoint());
			return tos;
		}
		
		public static void main(String[] args) throws IOException {
			System.out.println(new AudioStorage().config.toString());
		}
		
		public List<AccessKey> getKeys() {
			return keys;
		}
		
		public void setKeys(List<AccessKey> keys) {
			this.keys = keys;
		}
		
		public List<HttpAccess> getAccessList() {
			return accessList;
		}
		
		public void setAccessList(List<HttpAccess> accessList) {
			this.accessList = accessList;
		}
		
		public void addHttpAccess(HttpAccess access) {
			List<HttpAccess> has = this.accessList.stream().filter(a -> a.getId().equals(access.getId())).collect(Collectors.toList());
			this.accessList.removeAll(has);
			this.accessList.add(access);
		}
		
		public boolean removeHttpAccess(HttpAccess access) {
			return this.accessList.remove(access);
		}
		
		public HttpAccess getHttpAccessById(String id) {
			return this.accessList.stream().filter(a -> a.getId().equals(id)).findFirst().orElse(null);
		}
		
		public HttpAccess getHttpAccessByOwner(String owner) {
			return this.accessList.stream().filter(a -> a.getOwner().equals(owner)).findFirst().orElse(null);
		}
		
	}
	
}
