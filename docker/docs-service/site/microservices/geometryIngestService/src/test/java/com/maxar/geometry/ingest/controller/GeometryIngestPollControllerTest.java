package com.maxar.geometry.ingest.controller;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import com.maxar.geometry.ingest.service.GeometryIngestPollService;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@TestPropertySource(locations = "classpath:test-geometry-ingest.properties")
public class GeometryIngestPollControllerTest
{
	@Autowired
	private GeometryIngestPollController geometryIngestPollController;

	@MockBean
	private GeometryIngestPollService geometryIngestPollService;

	@Test
	public void testPause() {
		Mockito.when(geometryIngestPollService.pause())
				.thenReturn(true);

		final ResponseEntity<String> response = geometryIngestPollController.pause();

		Assert.assertNotNull(response);
		Assert.assertEquals(HttpStatus.OK,
							response.getStatusCode());
		Assert.assertNotNull(response.getBody());
	}

	@Test
	public void testPauseAlreadyPaused() {
		Mockito.when(geometryIngestPollService.pause())
				.thenReturn(false);

		final ResponseEntity<String> response = geometryIngestPollController.pause();

		Assert.assertNotNull(response);
		Assert.assertEquals(HttpStatus.OK,
							response.getStatusCode());
		Assert.assertNotNull(response.getBody());
	}

	@Test
	public void testResume() {
		Mockito.when(geometryIngestPollService.resume())
				.thenReturn(true);

		final ResponseEntity<String> response = geometryIngestPollController.resume();

		Assert.assertNotNull(response);
		Assert.assertEquals(HttpStatus.OK,
							response.getStatusCode());
		Assert.assertNotNull(response.getBody());
	}

	@Test
	public void testResumeNotPaused() {
		Mockito.when(geometryIngestPollService.resume())
				.thenReturn(false);

		final ResponseEntity<String> response = geometryIngestPollController.resume();

		Assert.assertNotNull(response);
		Assert.assertEquals(HttpStatus.OK,
							response.getStatusCode());
		Assert.assertNotNull(response.getBody());
	}

	@Test
	public void testPollForGeometriesNoOverride() {
		Mockito.when(geometryIngestPollService.pollForGeometries(Mockito.eq(false)))
				.thenReturn(true);

		final ResponseEntity<String> response = geometryIngestPollController.pollForGeometries(false);

		Assert.assertNotNull(response);
		Assert.assertEquals(HttpStatus.OK,
							response.getStatusCode());
		Assert.assertNotNull(response.getBody());
	}

	@Test
	public void testPollForGeometriesNoOverridePaused() {
		Mockito.when(geometryIngestPollService.pollForGeometries(Mockito.eq(false)))
				.thenReturn(false);

		final ResponseEntity<String> response = geometryIngestPollController.pollForGeometries(false);

		Assert.assertNotNull(response);
		Assert.assertEquals(HttpStatus.OK,
							response.getStatusCode());
		Assert.assertNotNull(response.getBody());
	}

	@Test
	public void testPollForGeometriesOverride() {
		Mockito.when(geometryIngestPollService.pollForGeometries(Mockito.eq(true)))
				.thenReturn(true);

		final ResponseEntity<String> response = geometryIngestPollController.pollForGeometries(true);

		Assert.assertNotNull(response);
		Assert.assertEquals(HttpStatus.OK,
							response.getStatusCode());
		Assert.assertNotNull(response.getBody());
	}

	@Test
	public void testPollForGeometriesOverridePaused() {
		Mockito.when(geometryIngestPollService.pollForGeometries(Mockito.eq(true)))
				.thenReturn(true);

		final ResponseEntity<String> response = geometryIngestPollController.pollForGeometries(true);

		Assert.assertNotNull(response);
		Assert.assertEquals(HttpStatus.OK,
							response.getStatusCode());
		Assert.assertNotNull(response.getBody());
	}
}
