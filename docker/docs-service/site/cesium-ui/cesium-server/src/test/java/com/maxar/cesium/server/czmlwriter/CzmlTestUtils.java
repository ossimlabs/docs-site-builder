package com.maxar.cesium.server.czmlwriter;

import java.util.List;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;

public class CzmlTestUtils
{

	public static void sendPackets(
			final List<JsonNode> array,
			final boolean doDelete,
			final String serviceIp,
			final String session ) {
		final RestTemplate restTemplate = new RestTemplate();

		try {
			if (doDelete) {
				restTemplate.delete("http://" + serviceIp + "/czml/packets?session=" + session);
			}
		}
		catch (final Exception e) {}

		final HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);

		final HttpEntity<List<JsonNode>> entity = new HttpEntity<>(
				array,
				headers);
		restTemplate.postForLocation(	"http://" + serviceIp + "/czml/packets?session=" + session,
										entity);

		System.out.println("Done...");
	}

}
