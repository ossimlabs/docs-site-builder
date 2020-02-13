package com.maxar.geometry.ingest.service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.maxar.geometric.intersection.model.AreaOfInterest;
import com.radiantblue.analytics.core.log.SourceLogger;

@Component
public class ApiService
{
	private static final Logger logger = SourceLogger.getLogger(ApiService.class.getName());

	@Autowired
	private RestTemplate restTemplate;

	@Autowired
	@LoadBalanced
	private RestTemplate discoveryRestTemplate;

	@Value("${geometry-ingest.aoi-source.url}")
	private String aoiSourceUrl;

	@Value("${geometry-ingest.aoi-destination.url}")
	private String aoiDestUrl;

	@Value("${geometry-ingest.aoi-id-source.url}")
	private String aoiIdSourceUrl;

	/**
	 * Get the raw data for areas of interest from an external service.
	 *
	 * @return The raw data for areas of interest.
	 */
	public byte[] getRawAois() {
		final ResponseEntity<byte[]> response = restTemplate.getForEntity(	aoiSourceUrl,
																			byte[].class);

		if (response.getStatusCode()
				.is2xxSuccessful()) {
			return response.getBody();
		}
		else {
			return null;
		}
	}

	/**
	 * Get a list of all area of interest (AOI) IDs currently stored by the
	 * Geometric Intersection service.
	 *
	 * @return The list of AOI IDs returned by the Geometric Intersection service.
	 */
	List<String> getAllStoredAoiIds() {
		final ParameterizedTypeReference<List<String>> typeReference = new ParameterizedTypeReference<>() {};

		final ResponseEntity<List<String>> response = discoveryRestTemplate.exchange(	aoiIdSourceUrl,
																						HttpMethod.GET,
																						null,
																						typeReference);

		return Optional.ofNullable(response.getBody())
				.orElse(Collections.emptyList());
	}

	/**
	 * Send an area of interest (AOI) to the Geometric Intersection service to be
	 * persisted.
	 *
	 * @param aoi
	 *            The AOI to be stored.
	 */
	void createAoi(
			final AreaOfInterest aoi ) {
		try {
			discoveryRestTemplate.postForEntity(aoiDestUrl,
												aoi,
												String.class);
		}
		catch (final Exception e) {
			logger.error(e.getMessage());
		}
	}
}
