package com.maxar.geometry.ingest.service;

import java.net.URI;
import java.util.Arrays;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.maxar.geometric.intersection.model.AreaOfInterest;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@TestPropertySource(locations = "classpath:test-geometry-ingest.properties")
public class ApiServiceTest
{
	private final static String EXAMPLE_UUID_0 = "cbb1d96a-0f3a-4fe2-bcca-25b3ddec26f5";

	private final static String EXAMPLE_UUID_1 = "df660168-1949-4059-8c5e-54519d878dbb";

	private final static String EXAMPLE_WKT = "POLYGON ((29.70703125 24.407137917727667, "
			+ "31.596679687499996 24.407137917727667, 31.596679687499996 26.293415004265796, "
			+ "29.70703125 26.293415004265796, 29.70703125 24.407137917727667))";

	@Autowired
	private ApiService apiService;

	@MockBean(name = "discoveryRestTemplate")
	@LoadBalanced
	private RestTemplate discoveryRestTemplate;

	@MockBean(name = "restTemplate")
	private RestTemplate restTemplate;

	@Value("${geometry-ingest.aoi-source.url}")
	private String aoiSourceUrl;

	@Value("${geometry-ingest.aoi-destination.url}")
	private String aoiDestUrl;

	@Value("${geometry-ingest.aoi-id-source.url}")
	private String aoiIdSourceUrl;

	@Test
	public void testGetRawAois() {
		final byte[] bytes = {
			65
		};

		Mockito.when(restTemplate.getForEntity(	Mockito.eq(aoiSourceUrl),
												Mockito.any()))
				.thenReturn(ResponseEntity.ok(bytes));

		final byte[] responseBytes = apiService.getRawAois();

		Assert.assertNotNull(responseBytes);
		Assert.assertEquals(1,
							responseBytes.length);
		Assert.assertEquals(65,
							responseBytes[0]);
	}

	@Test
	public void testGetRawAoisNull() {
		Mockito.when(restTemplate.getForEntity(	Mockito.eq(aoiSourceUrl),
												Mockito.any()))
				.thenReturn(ResponseEntity.ok()
						.build());

		final byte[] responseBytes = apiService.getRawAois();

		Assert.assertNull(responseBytes);
	}

	@Test
	public void testGetRawAoisEmpty() {
		Mockito.when(restTemplate.getForEntity(	Mockito.eq(aoiSourceUrl),
												Mockito.any()))
				.thenReturn(ResponseEntity.ok(new byte[0]));

		final byte[] responseBytes = apiService.getRawAois();

		Assert.assertNotNull(responseBytes);
		Assert.assertEquals(0,
							responseBytes.length);
	}

	@Test
	public void testGetRawAoisNoContent() {
		Mockito.when(restTemplate.getForEntity(	Mockito.eq(aoiSourceUrl),
												Mockito.any()))
				.thenReturn(ResponseEntity.noContent()
						.build());

		final byte[] responseBytes = apiService.getRawAois();

		Assert.assertNull(responseBytes);
	}

	@Test
	public void testGetRawAoisNotFoundError() {
		Mockito.when(restTemplate.getForEntity(	Mockito.eq(aoiSourceUrl),
												Mockito.any()))
				.thenReturn(ResponseEntity.notFound()
						.build());

		final byte[] responseBytes = apiService.getRawAois();

		Assert.assertNull(responseBytes);
	}

	@Test
	public void testGetRawAoisInternalServerError() {
		Mockito.when(restTemplate.getForEntity(	Mockito.eq(aoiSourceUrl),
												Mockito.any()))
				.thenReturn(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
						.build());

		final byte[] responseBytes = apiService.getRawAois();

		Assert.assertNull(responseBytes);
	}

	@Test
	public void testGetAllStoredAoiIds() {
		final ResponseEntity<List<String>> responseEntity = ResponseEntity
				.ok(Collections.singletonList(EXAMPLE_UUID_0.toString()));

		Mockito.when(discoveryRestTemplate.exchange(Mockito.eq(aoiIdSourceUrl),
													Mockito.eq(HttpMethod.GET),
													Mockito.isNull(),
													(ParameterizedTypeReference<List<String>>) Mockito.any()))
				.thenReturn(responseEntity);

		final List<String> ids = apiService.getAllStoredAoiIds();

		Assert.assertNotNull(ids);
		Assert.assertEquals(1,
							ids.size());
		Assert.assertEquals(EXAMPLE_UUID_0,
							ids.get(0));
	}

	@Test
	public void testGetAllStoredAoiIdsMultiple() {
		final ResponseEntity<List<String>> responseEntity = ResponseEntity.ok(Arrays.asList(EXAMPLE_UUID_0.toString(),
																							EXAMPLE_UUID_1.toString()));

		Mockito.when(discoveryRestTemplate.exchange(Mockito.eq(aoiIdSourceUrl),
													Mockito.eq(HttpMethod.GET),
													Mockito.isNull(),
													(ParameterizedTypeReference<List<String>>) Mockito.any()))
				.thenReturn(responseEntity);

		final List<String> ids = apiService.getAllStoredAoiIds();

		Assert.assertNotNull(ids);
		Assert.assertEquals(2,
							ids.size());
		Assert.assertEquals(EXAMPLE_UUID_0,
							ids.get(0));
		Assert.assertEquals(EXAMPLE_UUID_1,
							ids.get(1));
	}

	@Test
	public void testGetAllStoredEmptyList() {
		final ResponseEntity<List<String>> responseEntity = ResponseEntity.ok(Collections.emptyList());

		Mockito.when(discoveryRestTemplate.exchange(Mockito.eq(aoiIdSourceUrl),
													Mockito.eq(HttpMethod.GET),
													Mockito.isNull(),
													(ParameterizedTypeReference<List<String>>) Mockito.any()))
				.thenReturn(responseEntity);

		final List<String> ids = apiService.getAllStoredAoiIds();

		Assert.assertNotNull(ids);
		Assert.assertTrue(ids.isEmpty());
	}

	@Test
	public void testGetAllStoredNullList() {
		final ResponseEntity<List<String>> responseEntity = ResponseEntity.ok()
				.build();

		Mockito.when(discoveryRestTemplate.exchange(Mockito.eq(aoiIdSourceUrl),
													Mockito.eq(HttpMethod.GET),
													Mockito.isNull(),
													(ParameterizedTypeReference<List<String>>) Mockito.any()))
				.thenReturn(responseEntity);

		final List<String> ids = apiService.getAllStoredAoiIds();

		Assert.assertNotNull(ids);
		Assert.assertTrue(ids.isEmpty());
	}

	@Test
	public void testCreateAoi() {
		final AreaOfInterest aoi = new AreaOfInterest();
		aoi.setId(EXAMPLE_UUID_0);
		aoi.setGeometryWkt(EXAMPLE_WKT);

		Mockito.when(discoveryRestTemplate.postForEntity(	Mockito.eq(aoiDestUrl),
															Mockito.any(AreaOfInterest.class),
															Mockito.eq(String.class)))
				.thenReturn(ResponseEntity.created(URI.create("/geometry/" + EXAMPLE_UUID_0))
						.build());

		apiService.createAoi(aoi);
	}

	@Test
	public void testCreateAoiThrowException() {
		final AreaOfInterest aoi = new AreaOfInterest();
		aoi.setId(EXAMPLE_UUID_0);
		aoi.setGeometryWkt(EXAMPLE_WKT);

		Mockito.when(discoveryRestTemplate.postForEntity(	Mockito.eq(aoiDestUrl),
															Mockito.any(AreaOfInterest.class),
															Mockito.eq(String.class)))
				.thenThrow(new RestClientException(
						"error"));

		apiService.createAoi(aoi);
	}
}
