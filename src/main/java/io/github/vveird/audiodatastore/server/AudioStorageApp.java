package io.github.vveird.audiodatastore.server;


import java.io.IOException;

/**
 * AudioStorageApp class.
 *
 */
public class AudioStorageApp {
	public static void main(String[] args) throws IOException {
		AudioStorage as = AudioStorage.getInstance();
		as.startServer();
		System.in.read();
	}
}

