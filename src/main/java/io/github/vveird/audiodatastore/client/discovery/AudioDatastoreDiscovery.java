package io.github.vveird.audiodatastore.client.discovery;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

import io.github.vveird.audiodatastore.client.AudioDatastoreClient;
import io.github.vveird.audiodatastore.restdata.AudioStoreParameter;
import io.github.vveird.ssdp.SSDPListener;
import io.github.vveird.ssdp.SSDPMessage;
import io.github.vveird.ssdp.server.SSDPServer;

public class AudioDatastoreDiscovery {
	
	private static Map<String, AudioDatastoreClient> adServices = new HashMap<>();
	
	private static SSDPServer server = null;
	
	private static class AudioDatastoreSSDPListener implements SSDPListener {
		
		@Override
		public void notify(SSDPMessage msg) {
			if(AudioStoreParameter.SSDP_STN.equals(msg.getServiceType())) {
				if(msg.isAlive() && !adServices.keySet().contains(msg.getUSN())) {
					adServices.put(msg.getUSN(), new AudioDatastoreClient(msg.getUSN(), msg.getLocation()));
				}
				else if(msg.isByeBye() && adServices.keySet().contains(msg.getUSN())) {
					adServices.remove(msg.getUSN());
				}
			}
		}

		@Override
		public void msearchResponse(SSDPMessage msg) {
			if(AudioStoreParameter.SSDP_STN.equals(msg.getServiceType())) {
				if(msg.isMSearchResponse() && !adServices.keySet().contains(msg.getUSN())) {
					adServices.put(msg.getUSN(), new AudioDatastoreClient(msg.getUSN(), msg.getLocation()));
				}
				else if(msg.isByeBye() && adServices.keySet().contains(msg.getUSN())) {
					adServices.remove(msg.getUSN());
				}
			}
		}

		@Override
		public void msearch(SSDPMessage msg) {
			// TODO Auto-generated method stub
			
		}
		
	}
	
	static {
		initDiscover();
	}
	
	
	private static void initDiscover() {
		server = new SSDPServer();
		server.addSSDPListener(new AudioDatastoreSSDPListener());
		server.sendMulticastSearch(AudioStoreParameter.SSDP_STN);
		try {
			Thread.sleep(1_000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	private static InetAddress[] getNicIps() throws UnknownHostException {
		InetAddress localhost = InetAddress.getLocalHost();
		// Just in case this host has multiple IP addresses....
		return InetAddress.getAllByName(localhost.getCanonicalHostName());
	}

	public static Map<String, AudioDatastoreClient> getEndpoints() {
		return adServices;
	}

}
