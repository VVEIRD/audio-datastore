package io.github.vveird.audiodatastore.rest;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Objects;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;

import io.github.vveird.audiodatastore.AudioStorage;
import io.github.vveird.audiodatastore.data.AccessKey;
import io.github.vveird.audiodatastore.data.StorageFile;
import io.github.vveird.audiodatastore.restdata.HttpAccess;
import io.github.vveird.audiodatastore.restdata.MediaEntry;

/**
 * TODO Implement basic HTTP auth
 * (http://www.java2novice.com/restful-web-services/http-basic-authentication/)
 * 
 * @author vveird
 *
 */
@Path("/")
public class AudioStorageResource {

	private static final String UUID_STRING = "([a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12}){1}";
	AudioStorage as = null;

	public AudioStorageResource() {
		super();
		this.as = AudioStorage.getInstance();
	}

	@GET
	@Path("/access")
	@Produces(MediaType.APPLICATION_JSON)
	public AccessKey createAccess() {
		return as.generateKey();
	}

	@DELETE
	@Path("/access/{owner:" + UUID_STRING + "}")
	public boolean deleteUser(@PathParam("owner") String owner) {
		return as.deleteUser(owner);
	}
	
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Path("/access/control/{owner:" + UUID_STRING + "}")
	@Produces(MediaType.APPLICATION_JSON)
	public HttpAccess setHttpAccess(HttpAccess httpAccess, @PathParam("owner") String owner) {
		System.out.println();
		System.out.println("-----------------------------------------------------------------------");
		System.out.println("                              SETTING Http Access                      ");
		System.out.println(" Owner Key provided: " + owner);
		System.out.println(" ID:                 " + httpAccess.getId());
		System.out.println(" Owner of ID:        " + httpAccess.getOwner());
		System.out.println(" Is Owner:           " + owner.equals(httpAccess.getOwner()));
		Objects.requireNonNull(httpAccess);
		Objects.requireNonNull(httpAccess.getId());
		Objects.requireNonNull(owner);
		if(as.addHttpAccess(owner, httpAccess)) 
			System.out.println(" Http Access Updated.");
		else 
			httpAccess = null;
		System.out.println("-----------------------------------------------------------------------");
		return httpAccess;
	}
	
	@DELETE
	@Path("/access/control/{owner:" + UUID_STRING + "}")
	public Response deleteHttpAccess(@PathParam("owner") String owner) {
		System.out.println();
		System.out.println("-----------------------------------------------------------------------");
		System.out.println("                              DELETE Http Access                       ");
		System.out.println(" Owner Key provided: " + owner);
		boolean deleted = as.removeHttpAccess(owner);
		if(deleted) 
			System.out.println(" Http Access deleted.");
		System.out.println("-----------------------------------------------------------------------");
		return Response.status(deleted ? 200 : 404).build();
	}
	
	@GET
	@Path("/access/{owner:" + UUID_STRING + "}")
	public HttpAccess getHttpAccess(@PathParam("owner") String owner) {
		return as.getHttpAccess(owner);
	}

	@POST
	@Path("/{id:" + UUID_STRING + "}/{name}/{user:" + UUID_STRING + "}")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.APPLICATION_JSON)
	public MediaEntry uploadAudio(@PathParam("id") String id, @PathParam("name") String name,
			@PathParam("user") String user, @DefaultValue("true") @FormDataParam("enabled") boolean enabled,
			@FormDataParam("file") InputStream uploadedInputStream,
			@FormDataParam("file") FormDataContentDisposition fileDetail) {
		// Only with the right access key files can be saved under the corresponding
		// path
		System.out.println("-----------------------------------------------");
		System.out.println("Upload initiated [/" + id + "/" + name + "]");
		System.out.println("Access Key: " + user);
		System.out.println("ID:         " + id);
		System.out.println("Name:       " + name);
		System.out.println("Verified:   " + as.hasAccess(id, user, false, true, false));
		int rc = as.saveFile(user, id, name, fileDetail.getFileName(), uploadedInputStream);
		if (rc == 200)
			System.out.println("File [" + String.format("/%s/%s", id, name) + "] saved");
		System.out.println("-----------------------------------------------");
		System.out.println();
		return MediaEntry.builder().returnCode(rc).id(id).name(name).baseUrl(as.getBaseUrl())
				.storageId(as.getStorageId()).build();
	}

	@GET
	@Path("/{id:" + UUID_STRING + "}/{name}/{user:" + UUID_STRING + "}")
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	public Response downloadAudio(@PathParam("id") String id, @PathParam("name") String name, @PathParam("user") String user) {
		StorageFile file = as.getFile(user, id, name);
		if(!file.exists())
			return Response.status(404).build();
		java.nio.file.Path filePath = Paths.get(file.getLocalFile());
		ResponseBuilder response = Response.ok((Object) filePath.toFile());
		response.header("Content-Disposition", "attachment; filename=" + file.getUploadFileName());
		return response.build();
	}
	

	@GET
	@Path("/{id:" + UUID_STRING + "}/{name}")
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	public Response downloadAudio(@PathParam("id") String id, @PathParam("name") String name) {
		return downloadAudio(id, name, null);
	}

	@DELETE
	@Path("/{id:" + UUID_STRING + "}/{name}/{user:" + UUID_STRING + "}")
	@Produces(MediaType.APPLICATION_JSON)
	public MediaEntry deleteAudio(@PathParam("id") String id, @PathParam("name") String name,
			@PathParam("user") String user) {
		int rc = as.deleteFile(user, id, name);
		return MediaEntry.builder().returnCode(rc).id(id).name(name).baseUrl(as.getBaseUrl())
				.storageId(as.getStorageId()).build();
	}
}
