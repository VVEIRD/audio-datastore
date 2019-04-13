package io.github.vveird.audiodatastore.client;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.EntityBuilder;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.logging.log4j.core.util.IOUtils;

import com.google.gson.GsonBuilder;

import io.github.vveird.audiodatastore.client.discovery.AudioDatastoreDiscovery;
import io.github.vveird.audiodatastore.restdata.AccessKey;
import io.github.vveird.audiodatastore.restdata.MediaEntry;
import io.github.vveird.ssdp.SSDPClient;
import io.github.vveird.ssdp.SSDPMessage;

public class AudioDatastoreClient {

	private static Map<String, AudioDatastoreClient> endpoints = new HashMap<>();

	/**
	 * 1: Endpoint (e.G. http://localhost:3002/as) 2: UploadPath (UUID) 3: Custom
	 * name, userdefined 4: User UUID
	 */
	private static final String CREATE_ID_PATTERN = "%s/access";
	
	private static final String UPLOAD_PATTERN = "%s/%s/%s/%s";

	static {
		endpoints = AudioDatastoreDiscovery.getEndpoints();
	}
	
	private String urlEndpoint = null;

	private String storageId = null;

	private String defaultId = null;

	private String secret = null;

	public AudioDatastoreClient(String storageId, String urlEndpoint) {
		this.storageId = storageId;
		this.urlEndpoint = urlEndpoint;
	}

	public AudioDatastoreClient(AccessKey ac) {
		AudioDatastoreClient adc =  this.endpoints.get(ac.getStorageId());
		this.storageId = ac.getStorageId();
		this.defaultId = ac.getId();
		this.secret = ac.getSecret();
	}

	public InputStream getFile(MediaEntry me) {
		return null;
	}

	public MediaEntry uploadFile(String name, String fileName, InputStream is) throws ClientProtocolException, IOException {
		CloseableHttpClient httpClient = HttpClients.createDefault();
		HttpPost uploadFile = new HttpPost(String.format(UPLOAD_PATTERN, this.urlEndpoint, this.defaultId, name, this.secret));
		MultipartEntityBuilder builder = MultipartEntityBuilder.create();

		// This attaches the file to the POST:
		builder.addBinaryBody("file", is, ContentType.APPLICATION_OCTET_STREAM, fileName);

		HttpEntity multipart = builder.build();
		uploadFile.setEntity(multipart);
		CloseableHttpResponse response = httpClient.execute(uploadFile);
		InputStream meStream = response.getEntity().getContent();
		MediaEntry me = new GsonBuilder().create().fromJson(new InputStreamReader(meStream), MediaEntry.class);
		return me;
	}

	public AccessKey createId() throws ClientProtocolException, IOException {
		CloseableHttpClient httpClient = HttpClients.createDefault();
		HttpGet createIdHttpGet = new HttpGet(String.format(CREATE_ID_PATTERN, this.urlEndpoint));

		CloseableHttpResponse response = httpClient.execute(createIdHttpGet);
		InputStream meStream = response.getEntity().getContent();
		String aks =IOUtils.toString(new InputStreamReader(meStream, StandardCharsets.UTF_8));
		AccessKey ac = new GsonBuilder().create().fromJson(aks, AccessKey.class);
		this.defaultId = ac.getId();
		this.secret = ac.getSecret();
		this.storageId = ac.getStorageId();
		return ac;
	}

	public static void main(String[] args) throws ClientProtocolException, IOException {
		AudioDatastoreClient adc = endpoints.values().stream().findFirst().orElse(null);
		if (adc != null ) {
			AccessKey  ak = adc.createId();
			System.out.println("Access Key:");
			System.out.println(" ID:          " + ak.getId());
			System.out.println(" Secret:      " + ak.getSecret());
			System.out.println(" Storage-ID:  " + ak.getStorageId());
			try (InputStream is = Files.newInputStream(Paths.get("C:\\Users\\" + System.getProperty("user.name") + "\\Downloads\\The-Awakening-1.png"))) {
				MediaEntry me = adc.uploadFile("ADumbFile", "The-Awakening-1.png", is);
				String meS = new GsonBuilder().setPrettyPrinting().create().toJson(me);
				System.out.println(meS);
			}
		}
		else {
			System.out.println("no ad");
		}
	}
}
