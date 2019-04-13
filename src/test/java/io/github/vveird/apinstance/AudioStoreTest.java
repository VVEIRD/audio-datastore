package io.github.vveird.apinstance;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;

import org.glassfish.grizzly.http.server.HttpServer;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import io.github.vveird.audiodatastore.server.AudioStorage;
import io.github.vveird.audiodatastore.server.AudioStorageApp;

import static org.junit.Assert.assertEquals;

public class AudioStoreTest {

    private HttpServer server;
    private WebTarget target;

    @Before
    public void setUp() throws Exception {
        // start the server
        server = AudioStorage.getInstance().startServer();
        // create the client
        Client c = ClientBuilder.newClient();

        // uncomment the following line if you want to enable
        // support for JSON in the client (you also have to uncomment
        // dependency on jersey-media-json module in pom.xml and AudioStorageApp.startServer())
        // --
        // c.configuration().enable(new org.glassfish.jersey.media.json.JsonJaxbFeature());

        target = c.target(AudioStorage.getInstance().getEndpoint());
    }

    @After
    public void tearDown() throws Exception {
        server.stop();
    }

    /**
     * Test to see that the message "Got it!" is sent in the response.
     */
    @Test
    public void testGetIt() {
//    	PlaybackLineDescriptor responseMsg = target.path("playbackResource/100").request().get(PlaybackLineDescriptor.class);
//    	assertEquals(100, responseMsg.getId());
//    	assertEquals("default", responseMsg.getName());
    }
}
