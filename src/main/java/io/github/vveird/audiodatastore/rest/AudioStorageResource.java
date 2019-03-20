package io.github.vveird.audiodatastore.rest;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

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
import io.github.vveird.audiodatastore.restdata.MediaEntry;

/**
 * TODO Implement basic HTTP auth (http://www.java2novice.com/restful-web-services/http-basic-authentication/)
 * @author vveird
 *
 */
@Path("/")
public class AudioStorageResource {

	AudioStorage as = null;

	public AudioStorageResource() {
		super();
		this.as = AudioStorage.getInstance();
	}

	@POST
	@Path("/{id}/{pathname}")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.APPLICATION_JSON)
	public MediaEntry uploadAudio(@PathParam("id") String id, @PathParam("pathname") String pathName,
			@DefaultValue("true") @FormDataParam("enabled") boolean enabled,
			@FormDataParam("file") InputStream uploadedInputStream,
			@FormDataParam("file") FormDataContentDisposition fileDetail) {
		System.out.println("Recieved file");
		java.nio.file.Path filePath = Paths.get(as.getFileRoot(), id, pathName);
		boolean saved = saveToFile(uploadedInputStream, filePath, fileDetail.getFileName());
		if(saved)
			System.out.println("File [" + filePath.toString() +  "] saved");
		return new MediaEntry(saved? 200 : 409, as.getBaseUrl(), id, pathName);
	}
	
	@GET
	@Path("/{id}/{pathname}")
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	public Response downloadAudio(@PathParam("id") String id, @PathParam("pathname") String pathName) {
		java.nio.file.Path filePath = Paths.get(as.getFileRoot(), id, pathName);
		try {
			filePath = Files.list(filePath).findFirst().orElse(null);

		    ResponseBuilder response = Response.ok((Object) filePath.toFile());
		    response.header("Content-Disposition", "attachment; filename=" + filePath.getFileName().toString());
		    return response.build();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return Response.status(404).build();
	}
	
	@DELETE
	@Path("/{id}/{pathname}")
	@Produces(MediaType.APPLICATION_JSON)
	public MediaEntry deleteAudio(@PathParam("id") String id, @PathParam("pathname") String pathName) {
		java.nio.file.Path filePath = Paths.get(as.getFileRoot(), id, pathName);
		try {
			filePath = Files.list(filePath).findFirst().orElse(null);
			
			if(Files.exists(filePath) && !Files.isDirectory(filePath))
				Files.delete(filePath);

		    return MediaEntry.builder().returnCode(200).id(id).name(pathName).baseUrl(as.getBaseUrl()).build();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return MediaEntry.builder().returnCode(404).id(id).name(pathName).baseUrl(as.getBaseUrl()).build();
	}

	private boolean saveToFile(InputStream uploadedInputStream, java.nio.file.Path uploadedFileLocation, String fileName) {
		try {
			if(Files.exists(uploadedFileLocation) && Files.list(uploadedFileLocation).findFirst().orElse(null) != null) 
				return false;
			if(!Files.exists(uploadedFileLocation) || !Files.isDirectory(uploadedFileLocation))
				Files.createDirectories(uploadedFileLocation);
			java.nio.file.Path filePath = Files.list(uploadedFileLocation).findFirst().orElse(null);
			if(filePath != null)
				return false;
		    try (OutputStream out = Files.newOutputStream(uploadedFileLocation.resolve(fileName), StandardOpenOption.CREATE_NEW)){
		        int read = 0;
		        byte[] bytes = new byte[1024];
	
		        while ((read = uploadedInputStream.read(bytes)) != -1) {
		            out.write(bytes, 0, read);
		        }
		        out.flush();
		        out.close();
		        return true;
		    }
		}catch (IOException e) {
	        e.printStackTrace();
	    }
        return false;
	}
}
