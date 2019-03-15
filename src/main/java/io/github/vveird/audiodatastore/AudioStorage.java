package io.github.vveird.audiodatastore;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.yaml.snakeyaml.Yaml;

public class AudioStorage {
	
	private static AudioStorage instance = null;

	public synchronized static AudioStorage getInstance()  {
		if(instance == null)
			instance = new AudioStorage();
		return instance;
	}
	
	private AudioStorageConfiguration config = null;
	
	private HttpServer restWebServer = null;

	public AudioStorage()  {
		Yaml yaml = new Yaml();
		try (InputStream in = Files.newInputStream(Paths.get("config" + File.separator + "storage.yml"))) {
			config = yaml.loadAs(in, AudioStorageConfiguration.class);
		} catch (IOException e) {
			config = new AudioStorageConfiguration();
			e.printStackTrace();
		}
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
        restWebServer = GrizzlyHttpServerFactory.createHttpServer(URI.create(config.getBaseUrl()), rc);
        System.out.println("Server listening on [" + config.getBaseUrl() + "]");
    }

	public static class AudioStorageConfiguration {

		private String version = "0.1";
		
		private String root = ".";
		
		private String interfaceAdress = "localhost";
		
		private String uriBase = "as";
		
		private int port = 3002;
		
	    private List<String> endPoints;
		
		public String getRoot() {
			return root;
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
		public String getBaseUrl() {
			return String.format("http://%s:%d/%s/", interfaceAdress, port, uriBase);
		}
		
		@Override
		public String toString() {
			String tos = String.format("AudioStorage\r\nVersion: %s\r\nRoot: %s\r\nURL: %s", version, root, getBaseUrl());
			return tos;
		}
		
		public static void main(String[] args) throws IOException {
			System.out.println(new AudioStorage().config.toString());
		}
	}

	public String getBaseUrl() {
		return config.getBaseUrl();
	}

	public String getFileRoot() {
		return config.getRoot();
	}
	
}
