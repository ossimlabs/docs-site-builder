package com.maxar.geometry.ingest.service;

import java.util.Collections;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import com.maxar.geometric.intersection.model.AreaOfInterest;
import com.maxar.geometry.ingest.translate.KmlAoiTranslator;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@TestPropertySource(locations = "classpath:test-geometry-ingest.properties")
public class GeometryIngestPollServiceTest
{
	private final static String EXAMPLE_ID_0 = "cbb1d96a-0f3a-4fe2-bcca-25b3ddec26f5";

	private final static String EXAMPLE_ID_1 = "df660168-1949-4059-8c5e-54519d878dbb";

	private final static String EXAMPLE_WKT = "POLYGON ((29.70703125 24.407137917727667, "
			+ "31.596679687499996 24.407137917727667, 31.596679687499996 26.293415004265796, "
			+ "29.70703125 26.293415004265796, 29.70703125 24.407137917727667))";

	@Autowired
	private GeometryIngestPollService geometryIngestPollService;

	@MockBean
	private ApiService apiService;

	@MockBean
	private KmlAoiTranslator kmlAoiTranslator;

	@Before
	public void resume() {
		// Don't leave the service scheduler paused before running a test.
		geometryIngestPollService.resume();
	}

	@Test
	public void testPause() {
		Assert.assertTrue(geometryIngestPollService.pause());
	}

	@Test
	public void testPauseTwice() {
		Assert.assertTrue(geometryIngestPollService.pause());
		Assert.assertFalse(geometryIngestPollService.pause());
	}

	@Test
	public void testResumeNotPaused() {
		Assert.assertFalse(geometryIngestPollService.resume());
	}

	@Test
	public void testPauseThenResume() {
		Assert.assertTrue(geometryIngestPollService.pause());
		Assert.assertTrue(geometryIngestPollService.resume());
	}

	@Test
	public void testPauseThenResumeTwice() {
		Assert.assertTrue(geometryIngestPollService.pause());
		Assert.assertTrue(geometryIngestPollService.resume());
		Assert.assertFalse(geometryIngestPollService.resume());
	}

	@Test
	public void testPollForGeometries() {
		final AreaOfInterest aoi = new AreaOfInterest();
		aoi.setId(EXAMPLE_ID_0);
		aoi.setGeometryWkt(EXAMPLE_WKT);

		Mockito.when(apiService.getRawAois())
				.thenReturn(new byte[1]);

		Mockito.when(kmlAoiTranslator.translateKmlToAois(Mockito.any()))
				.thenReturn(Collections.singletonList(aoi));

		Mockito.when(apiService.getAllStoredAoiIds())
				.thenReturn(Collections.singletonList(EXAMPLE_ID_1));

		Mockito.doNothing()
				.when(apiService)
				.createAoi(Mockito.any());

		Assert.assertTrue(geometryIngestPollService.pollForGeometries());
	}

	@Test
	public void testPollForGeometriesIngestNull() {
		Mockito.when(apiService.getRawAois())
				.thenReturn(null);

		Mockito.when(kmlAoiTranslator.translateKmlToAois(Mockito.any()))
				.thenThrow(new RuntimeException(
						"Should not have tried to translate null AOI array"));

		Mockito.when(apiService.getAllStoredAoiIds())
				.thenReturn(Collections.emptyList());

		Mockito.doThrow(new RuntimeException(
				"Should not have created any AOIs"))
				.when(apiService)
				.createAoi(Mockito.any());

		Assert.assertTrue(geometryIngestPollService.pollForGeometries());
	}

	@Test
	public void testPollForGeometriesEmpty() {
		Mockito.when(apiService.getRawAois())
				.thenReturn(new byte[1]);

		Mockito.when(kmlAoiTranslator.translateKmlToAois(Mockito.any()))
				.thenReturn(Collections.emptyList());

		Mockito.when(apiService.getAllStoredAoiIds())
				.thenThrow(new RuntimeException(
						"Should not have requested stored IDs when no AOIs were returned"));

		Assert.assertTrue(geometryIngestPollService.pollForGeometries());
	}

	@Test
	public void testPollForGeometriesNull() {
		Mockito.when(apiService.getRawAois())
				.thenReturn(new byte[1]);

		Mockito.when(kmlAoiTranslator.translateKmlToAois(Mockito.any()))
				.thenReturn(Collections.singletonList(null));

		Mockito.when(apiService.getAllStoredAoiIds())
				.thenReturn(Collections.emptyList());

		Mockito.doThrow(new RuntimeException(
				"Should not have created any AOIs"))
				.when(apiService)
				.createAoi(Mockito.any());

		Assert.assertTrue(geometryIngestPollService.pollForGeometries());
	}

	@Test
	public void testPollForGeometriesNullGeometryWkt() {
		final AreaOfInterest aoi = new AreaOfInterest();
		aoi.setId(EXAMPLE_ID_0);
		aoi.setGeometryWkt(null);

		Mockito.when(apiService.getRawAois())
				.thenReturn(new byte[1]);

		Mockito.when(kmlAoiTranslator.translateKmlToAois(Mockito.any()))
				.thenReturn(Collections.singletonList(aoi));

		Mockito.when(apiService.getAllStoredAoiIds())
				.thenReturn(Collections.emptyList());

		Mockito.doThrow(new RuntimeException(
				"Should not have created any AOIs"))
				.when(apiService)
				.createAoi(Mockito.any());

		Assert.assertTrue(geometryIngestPollService.pollForGeometries());
	}

	@Test
	public void testPollForGeometriesNonUnique() {
		final AreaOfInterest aoi = new AreaOfInterest();
		aoi.setId(EXAMPLE_ID_0);
		aoi.setGeometryWkt(EXAMPLE_WKT);

		Mockito.when(apiService.getRawAois())
				.thenReturn(new byte[1]);

		Mockito.when(kmlAoiTranslator.translateKmlToAois(Mockito.any()))
				.thenReturn(Collections.singletonList(aoi));

		Mockito.when(apiService.getAllStoredAoiIds())
				.thenReturn(Collections.singletonList(EXAMPLE_ID_0));

		Mockito.doThrow(new RuntimeException(
				"Should not have created any AOIs"))
				.when(apiService)
				.createAoi(Mockito.any());

		Assert.assertTrue(geometryIngestPollService.pollForGeometries());
	}

	@Test
	public void testPollForGeometriesPaused() {
		Mockito.when(apiService.getRawAois())
				.thenReturn(new byte[1]);

		Mockito.when(kmlAoiTranslator.translateKmlToAois(Mockito.any()))
				.thenThrow(new RuntimeException(
						"Should not have requested geometries while paused"));

		geometryIngestPollService.pause();

		Assert.assertFalse(geometryIngestPollService.pollForGeometries());
	}

	@Test
	public void testPollForGeometriesPausedForcePoll() {
		final AreaOfInterest aoi = new AreaOfInterest();
		aoi.setId(EXAMPLE_ID_0);
		aoi.setGeometryWkt(EXAMPLE_WKT);

		Mockito.when(apiService.getRawAois())
				.thenReturn(new byte[1]);

		Mockito.when(kmlAoiTranslator.translateKmlToAois(Mockito.any()))
				.thenReturn(Collections.singletonList(aoi));

		Mockito.when(apiService.getAllStoredAoiIds())
				.thenReturn(Collections.singletonList(EXAMPLE_ID_1));

		Mockito.doNothing()
				.when(apiService)
				.createAoi(Mockito.any());

		geometryIngestPollService.pause();

		Assert.assertTrue(geometryIngestPollService.pollForGeometries(true));
	}
}
