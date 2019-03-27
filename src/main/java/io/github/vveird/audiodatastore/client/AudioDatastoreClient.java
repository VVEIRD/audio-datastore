package io.github.vveird.audiodatastore.client;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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


public class AudioDatastoreClient {
	
	private static Map<String, String> endpoints = new HashMap<>();
	
	/**
	 * 1: Endpoint (e.G. http://localhost:3002/as)
	 * 2: UploadPath (UUID)
	 * 3: Custom name, userdefined
	 * 4: User UUID
	 */
	private static final String UPLOAD_PATTERN = "%s/%s/%s%s";
	
	static {
		endpoints = AudioDatastoreDiscovery.getEndpoints();
	}
	
	private String storageId = null;

	private String defaultId = null;
	
	private String authKey = null;
	
	
	public AudioDatastoreClient(String storageId) {
		this.storageId = endpoints.keySet().stream().filter(k -> endpoints.get(k).equals(storageId)).map(k -> endpoints.get(k)).findFirst().orElse(null);
	}
	
	public AudioDatastoreClient(AccessKey ac) {
		this.storageId = ac.getStorageId();
		this.defaultId = ac.getId();
		this.authKey = ac.getOwner();
	}
	
	public InputStream getFile(MediaEntry me) {
		return null;
	}
	
	public static MediaEntry uploadFile(String endpoint, String uploadPath, String uploadUser, String name, String fileName, InputStream is) throws ClientProtocolException, IOException {
		CloseableHttpClient httpClient = HttpClients.createDefault();
		HttpPost uploadFile = new HttpPost(String.format(UPLOAD_PATTERN, endpoint, uploadPath, name, uploadUser));
		MultipartEntityBuilder builder = MultipartEntityBuilder.create();

		// This attaches the file to the POST:
		builder.addBinaryBody(
		    "file",
		    is,
		    ContentType.APPLICATION_OCTET_STREAM,
		    fileName
		);

		HttpEntity multipart = builder.build();
		uploadFile.setEntity(multipart);
		CloseableHttpResponse response = httpClient.execute(uploadFile);
		HttpEntity responseEntity = response.getEntity();
		if(response.getEntity().getContentType().getValue().equals(ContentType.APPLICATION_JSON)) {
			InputStream meStream = response.getEntity().getContent();
			MediaEntry me = new GsonBuilder().create().fromJson(new InputStreamReader(meStream), MediaEntry.class);
			return me;
		}
		return null;
	}
	
	public static void main(String[] args) throws ClientProtocolException, IOException {
		try(InputStream is = Files.newInputStream(Paths.get("C:\\Users\\wkiv894\\Downloads\\The-Awakening-1.png"))) {
			MediaEntry me = uploadFile("http://localhost:3002/as", "88d5e517-6c10-43ee-adef-974f4bfd724d", "67984e61-66ce-453a-86b5-5488362a9382", "ADumbFile", "The-Awakening-1.png", is);
			String meS = new GsonBuilder().setPrettyPrinting().create().toJson(me);
			System.out.println(meS); 
		}
	}
}
