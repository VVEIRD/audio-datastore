package io.github.vveird.audiodatastore;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import java.io.IOException;
import java.net.URI;

/**
 * Main class.
 *
 */
public class Main {
	public static void main(String[] args) throws IOException {
		AudioStorage as = AudioStorage.getInstance();
		as.startServer();
		System.in.read();
	}
}

