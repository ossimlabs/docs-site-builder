package com.maxar.alert.poll.service;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.maxar.alert.model.Event;
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

	@Value("${alert-poll.endpoint.alert-source-by-country-url}")
	private String alertSourceByCountryUrl;

	@Value("${alert-poll.endpoint.alert-destination-url}")
	private String alertDestinationUrl;

	/**
	 * Get all alerts from the external source.
	 *
	 * @return A list of all alerts available from the external alert source.
	 */
	List<Event> getAllAlerts() {
		final Map<String, String> body = new HashMap<>();
		final HttpEntity<Map<String, String>> request = new HttpEntity<>(
				body);

		final ParameterizedTypeReference<List<Event>> typeReference = new ParameterizedTypeReference<>() {};

		final ResponseEntity<List<Event>> response = restTemplate.exchange(	alertSourceByCountryUrl,
																			HttpMethod.POST,
																			request,
																			typeReference);

		return Optional.ofNullable(response.getBody())
				.orElse(Collections.emptyList());
	}

	List<String> getAllStoredAlertIds() {
		final ParameterizedTypeReference<List<String>> typeReference = new ParameterizedTypeReference<>() {};

		final ResponseEntity<List<String>> response = discoveryRestTemplate.exchange(	alertDestinationUrl,
																						HttpMethod.GET,
																						null,
																						typeReference);

		return Optional.ofNullable(response.getBody())
				.orElse(Collections.emptyList());
	}

	/**
	 * Send an alert to the alert storage service to be persisted.
	 *
	 * @param event
	 *            The alert to be stored.
	 */
	void postAlert(
			final Event event ) {
		try {
			discoveryRestTemplate.postForEntity(alertDestinationUrl,
												event,
												String.class);
		}
		catch (final Exception e) {
			logger.error(e.getMessage());
		}
	}
}
