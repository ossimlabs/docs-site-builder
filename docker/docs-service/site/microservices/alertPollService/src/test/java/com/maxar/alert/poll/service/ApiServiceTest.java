package com.maxar.alert.poll.service;

import java.util.Collections;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

import com.maxar.alert.model.Event;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@TestPropertySource(locations = "classpath:test-alert-poll-disabled.properties")
public class ApiServiceTest
{
	@Autowired
	private ApiService apiService;

	@MockBean(name = "restTemplate")
	private RestTemplate restTemplate;

	@MockBean(name = "discoveryRestTemplate")
	@LoadBalanced
	private RestTemplate discoveryRestTemplate;

	@Value("${alert-poll.endpoint.alert-source-by-country-url}")
	private String alertSourceByCountryUrl;

	@Value("${alert-poll.endpoint.alert-destination-url}")
	private String alertDestinationUrl;

	@Test
	public void testGetAllAlertsEmpty() {
		// noinspection unchecked
		Mockito.when(restTemplate.exchange(	Mockito.eq(alertSourceByCountryUrl),
											Mockito.eq(HttpMethod.POST),
											Mockito.any(),
											(ParameterizedTypeReference<List<Event>>) Mockito.any()))
				.thenReturn(ResponseEntity.ok(Collections.emptyList()));

		final List<Event> events = apiService.getAllAlerts();

		Assert.assertNotNull(events);
		Assert.assertTrue(events.isEmpty());
	}

	@Test
	public void testGetAllAlertsNull() {
		// noinspection unchecked
		Mockito.when(restTemplate.exchange(	Mockito.eq(alertSourceByCountryUrl),
											Mockito.eq(HttpMethod.POST),
											Mockito.any(),
											(ParameterizedTypeReference<List<Event>>) Mockito.any()))
				.thenReturn(ResponseEntity.noContent()
						.build());

		final List<Event> events = apiService.getAllAlerts();

		Assert.assertNotNull(events);
		Assert.assertTrue(events.isEmpty());
	}

	@Test
	public void testGetAllAlertsSingleResult() {
		final Event event = new Event();

		// noinspection unchecked
		Mockito.when(restTemplate.exchange(	Mockito.eq(alertSourceByCountryUrl),
											Mockito.eq(HttpMethod.POST),
											Mockito.any(),
											(ParameterizedTypeReference<List<Event>>) Mockito.any()))
				.thenReturn(ResponseEntity.ok(Collections.singletonList(event)));

		final List<Event> events = apiService.getAllAlerts();

		Assert.assertNotNull(events);
		Assert.assertEquals(1,
							events.size());
		Assert.assertNotNull(events.get(0));
	}

	@Test
	public void testGetAllStoredAlertIdsEmpty() {
		// noinspection unchecked
		Mockito.when(discoveryRestTemplate.exchange(Mockito.eq(alertDestinationUrl),
													Mockito.eq(HttpMethod.GET),
													Mockito.isNull(),
													(ParameterizedTypeReference<List<String>>) Mockito.any()))
				.thenReturn(ResponseEntity.ok(Collections.emptyList()));

		final List<String> ids = apiService.getAllStoredAlertIds();

		Assert.assertNotNull(ids);
		Assert.assertTrue(ids.isEmpty());
	}

	@Test
	public void testGetAllStoredAlertIdsNull() {
		// noinspection unchecked
		Mockito.when(discoveryRestTemplate.exchange(Mockito.eq(alertDestinationUrl),
													Mockito.eq(HttpMethod.GET),
													Mockito.isNull(),
													(ParameterizedTypeReference<List<String>>) Mockito.any()))
				.thenReturn(ResponseEntity.noContent()
						.build());

		final List<String> ids = apiService.getAllStoredAlertIds();

		Assert.assertNotNull(ids);
		Assert.assertTrue(ids.isEmpty());
	}

	@Test
	public void testGetAllStoredAlertIdsSingleResult() {
		// noinspection unchecked
		Mockito.when(discoveryRestTemplate.exchange(Mockito.eq(alertDestinationUrl),
													Mockito.eq(HttpMethod.GET),
													Mockito.isNull(),
													(ParameterizedTypeReference<List<String>>) Mockito.any()))
				.thenReturn(ResponseEntity.ok(Collections.singletonList("id0")));

		final List<String> ids = apiService.getAllStoredAlertIds();

		Assert.assertNotNull(ids);
		Assert.assertEquals(1,
							ids.size());
		Assert.assertEquals("id0",
							ids.get(0));
	}

	@Test
	public void testPostAlert() {
		Mockito.when(discoveryRestTemplate.postForEntity(	Mockito.eq(alertDestinationUrl),
															Mockito.any(),
															Mockito.eq(String.class)))
				.thenReturn(ResponseEntity.ok()
						.build());

		final Event event = new Event();

		apiService.postAlert(event);

		Assert.assertTrue(true);
	}

	@Test
	public void testPostAlertError() {
		Mockito.when(discoveryRestTemplate.postForEntity(	Mockito.eq(alertDestinationUrl),
															Mockito.any(),
															Mockito.eq(String.class)))
				.thenThrow(new RuntimeException(
						"error"));

		final Event event = new Event();

		apiService.postAlert(event);

		Assert.assertTrue(true);
	}
}
