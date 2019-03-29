package io.github.vveird.audiodatastore.client;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import com.google.gson.GsonBuilder;

import io.github.vveird.audiodatastore.client.discovery.AudioDatastoreDiscovery;
import io.github.vveird.audiodatastore.server.restdata.AccessKey;
import io.github.vveird.audiodatastore.server.restdata.MediaEntry;
import io.github.vveird.ssdp.SSDPClient;
import io.github.vveird.ssdp.SSDPMessage;

public class AudioDatastoreClient {

	private static Map<String, String> endpoints = new HashMap<>();

	/**
	 * 1: Endpoint (e.G. http://localhost:3002/as) 2: UploadPath (UUID) 3: Custom
	 * name, userdefined 4: User UUID
	 */
	private static final String UPLOAD_PATTERN = "%s/%s/%s/%s";

	static {
		endpoints = AudioDatastoreDiscovery.getEndpoints();
	}

	private String storageId = null;

	private String defaultId = null;

	private String authKey = null;

	public AudioDatastoreClient(String storageId) {
		this.storageId = endpoints.keySet().stream().filter(k -> endpoints.get(k).equals(storageId))
				.map(k -> endpoints.get(k)).findFirst().orElse(null);
	}

	public AudioDatastoreClient(AccessKey ac) {
		this.storageId = ac.getStorageId();
		this.defaultId = ac.getId();
		this.authKey = ac.getSecret();
	}

	public InputStream getFile(MediaEntry me) {
		return null;
	}

	public static MediaEntry uploadFile(String endpoint, String uploadPath, String uploadUser, String name,
			String fileName, InputStream is) throws ClientProtocolException, IOException {
		CloseableHttpClient httpClient = HttpClients.createDefault();
		HttpPost uploadFile = new HttpPost(String.format(UPLOAD_PATTERN, endpoint, uploadPath, name, uploadUser));
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

	public static void main(String[] args) throws ClientProtocolException, IOException {
		SSDPClient sc = new SSDPClient(InetAddress.getLocalHost());
		sc.sendMulticast(SSDPClient.getSSDPSearchMessage("urn:vveird:audio-storage:1"));
		DatagramPacket dp = sc.responseReceive();
		SSDPMessage sm = SSDPMessage.parse(dp, sc);
		System.out.println(sm.toJson());
		System.out.println(sm.toString());
		
		String usn = sm.getUSN();
		String loc = sm.getLocation();
		
		try (InputStream is = Files.newInputStream(Paths.get("C:\\Users\\" + System.getProperty("user.name") + "\\Downloads\\The-Awakening-1.png"))) {
			MediaEntry me = uploadFile(loc, "84d9152a-964e-4288-a218-0f32165c7527",
					"26a5cf77-950c-4c58-baea-dc7a105e60cf", "ADumbFile", "The-Awakening-1.png", is);
			String meS = new GsonBuilder().setPrettyPrinting().create().toJson(me);
			System.out.println(meS);
		}
	}
}
