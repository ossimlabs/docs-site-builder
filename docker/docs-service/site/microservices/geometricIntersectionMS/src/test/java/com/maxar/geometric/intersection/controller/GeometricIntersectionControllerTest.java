package com.maxar.geometric.intersection.controller;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

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

import com.maxar.geometric.intersection.exception.AreaOfInterestIdDoesNotExistException;
import com.maxar.geometric.intersection.exception.InvalidAreaOfInterestException;
import com.maxar.geometric.intersection.service.GeometricIntersectionService;
import com.maxar.geometric.intersection.model.AreaOfInterest;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@TestPropertySource(locations = "classpath:test-geometricintersectionms.properties")
public class GeometricIntersectionControllerTest
{
	@Autowired
	private GeometricIntersectionController geometricIntersectionController;

	@MockBean
	private GeometricIntersectionService geometricIntersectionService;

	private final static String BASE_URL = "/geometry";

	private final static String EXAMPLE_GEOMETRY_WKT = "POLYGON ((-77.4232292175293 38.959759183009226, "
			+ "-77.42216706275939 38.959233596526964, -77.42136240005493 38.960459958921135, "
			+ "-77.42239236831665 38.96086039918754, -77.4232292175293 38.959759183009226))";

	private final static String EXAMPLE_ID = "id0";

	@Test
	public void testCreateGeometry() {
		Mockito.when(geometricIntersectionService.createGeometry(Mockito.any()))
				.thenReturn(EXAMPLE_ID);

		final AreaOfInterest areaOfInterest = new AreaOfInterest();
		areaOfInterest.setId(EXAMPLE_ID);
		areaOfInterest.setGeometryWkt(EXAMPLE_GEOMETRY_WKT);

		final ResponseEntity<String> response = geometricIntersectionController.createGeometry(areaOfInterest);

		Assert.assertNotNull(response);
		Assert.assertEquals(HttpStatus.CREATED,
							response.getStatusCode());
		Assert.assertNull(response.getBody());
		Assert.assertNotNull(response.getHeaders());
		Assert.assertNotNull(response.getHeaders()
				.getLocation());
		Assert.assertEquals(BASE_URL + "/" + EXAMPLE_ID,
							response.getHeaders()
									.getLocation()
									.toString());
	}

	@Test(expected = InvalidAreaOfInterestException.class)
	public void testCreateGeometryInvalidWkt() {
		Mockito.when(geometricIntersectionService.createGeometry(Mockito.any()))
				.thenThrow(InvalidAreaOfInterestException.class);

		final AreaOfInterest areaOfInterest = new AreaOfInterest();
		areaOfInterest.setId(EXAMPLE_ID);
		areaOfInterest.setGeometryWkt(EXAMPLE_GEOMETRY_WKT.substring(1));

		geometricIntersectionController.createGeometry(areaOfInterest);
	}

	@Test
	public void testCreateGeometryUi() {
		Mockito.when(geometricIntersectionService.createGeometry(Mockito.any()))
				.thenReturn(EXAMPLE_ID);

		final List<List<List<Double>>> polygons = Collections
				.singletonList(Arrays.asList(	Arrays.asList(-77.4232292175293,
															38.959759183009226),
												Arrays.asList(	-77.42216706275939,
																38.959233596526964),
												Arrays.asList(	-77.42136240005493,
																38.960459958921135),
												Arrays.asList(	-77.42239236831665,
																38.96086039918754),
												Arrays.asList(	-77.4232292175293,
																38.959759183009226)));

		final ResponseEntity<List<String>> response = geometricIntersectionController.createGeometryUi(polygons);

		Assert.assertNotNull(response);
		Assert.assertEquals(HttpStatus.OK,
							response.getStatusCode());
		Assert.assertNotNull(response.getBody());
		Assert.assertEquals(1,
							response.getBody()
									.size());
		Assert.assertEquals(EXAMPLE_ID,
							response.getBody()
									.get(0));
	}

	@Test
	public void testCreateGeometryUiEmpty() {
		Mockito.when(geometricIntersectionService.createGeometry(Mockito.any()))
				.thenReturn(EXAMPLE_ID);

		final List<List<List<Double>>> polygons = Collections.emptyList();

		final ResponseEntity<List<String>> response = geometricIntersectionController.createGeometryUi(polygons);

		Assert.assertNotNull(response);
		Assert.assertEquals(HttpStatus.OK,
							response.getStatusCode());
		Assert.assertNotNull(response.getBody());
		Assert.assertTrue(response.getBody()
				.isEmpty());
	}

	@Test
	public void testGetGeometriesEmpty() {
		Mockito.when(geometricIntersectionService.getGeometries())
				.thenReturn(Collections.emptyList());

		final ResponseEntity<List<String>> response = geometricIntersectionController.getGeometries();

		Assert.assertNotNull(response);
		Assert.assertEquals(HttpStatus.OK,
							response.getStatusCode());
		Assert.assertNotNull(response.getBody());
		Assert.assertTrue(response.getBody()
				.isEmpty());
	}

	@Test
	public void testGetGeometriesOne() {
		Mockito.when(geometricIntersectionService.getGeometries())
				.thenReturn(Collections.singletonList(EXAMPLE_ID));

		final ResponseEntity<List<String>> response = geometricIntersectionController.getGeometries();

		Assert.assertNotNull(response);
		Assert.assertEquals(HttpStatus.OK,
							response.getStatusCode());
		Assert.assertNotNull(response.getBody());
		Assert.assertEquals(1,
							response.getBody()
									.size());
		Assert.assertEquals(EXAMPLE_ID,
							response.getBody()
									.get(0));
	}

	@Test
	public void testGetGeometryById()
			throws AreaOfInterestIdDoesNotExistException {
		final AreaOfInterest areaOfInterest = new AreaOfInterest();
		areaOfInterest.setId(EXAMPLE_ID);
		areaOfInterest.setGeometryWkt(EXAMPLE_GEOMETRY_WKT);

		Mockito.when(geometricIntersectionService.getGeometryById(Mockito.eq(EXAMPLE_ID)))
				.thenReturn(areaOfInterest);

		final ResponseEntity<AreaOfInterest> response = geometricIntersectionController.getGeometryById(EXAMPLE_ID);

		Assert.assertNotNull(response);
		Assert.assertEquals(HttpStatus.OK,
							response.getStatusCode());
		Assert.assertNotNull(response.getBody());
		Assert.assertEquals(EXAMPLE_GEOMETRY_WKT,
							response.getBody()
									.getGeometryWkt());
	}

	@Test(expected = AreaOfInterestIdDoesNotExistException.class)
	public void testGetGeometryByIdDoesNotExist()
			throws AreaOfInterestIdDoesNotExistException {
		Mockito.when(geometricIntersectionService.getGeometryById(Mockito.eq(EXAMPLE_ID)))
				.thenThrow(new AreaOfInterestIdDoesNotExistException(
						EXAMPLE_ID));

		geometricIntersectionController.getGeometryById(EXAMPLE_ID);
	}

	@Test
	public void testGetIntersectingGeometriesNone() {
		Mockito.when(geometricIntersectionService.getIntersectingGeometries(Mockito.eq(EXAMPLE_GEOMETRY_WKT)))
				.thenReturn(Collections.emptyList());

		final ResponseEntity<List<AreaOfInterest>> response = geometricIntersectionController
				.getIntersectingGeometries(EXAMPLE_GEOMETRY_WKT);

		Assert.assertNotNull(response);
		Assert.assertEquals(HttpStatus.OK,
							response.getStatusCode());
		Assert.assertNotNull(response.getBody());
		Assert.assertTrue(response.getBody()
				.isEmpty());
	}

	@Test(expected = InvalidAreaOfInterestException.class)
	public void testGetIntersectingGeometriesInvalidWkt() {
		Mockito.when(geometricIntersectionService.getIntersectingGeometries(Mockito.eq("")))
				.thenThrow(InvalidAreaOfInterestException.class);

		geometricIntersectionController.getIntersectingGeometries("");
	}

	@Test
	public void testGetIntersectingGeometriesOne() {
		final AreaOfInterest areaOfInterest = new AreaOfInterest();
		areaOfInterest.setId(EXAMPLE_ID);
		areaOfInterest.setGeometryWkt(EXAMPLE_GEOMETRY_WKT);

		Mockito.when(geometricIntersectionService.getIntersectingGeometries(Mockito.eq(EXAMPLE_GEOMETRY_WKT)))
				.thenReturn(Collections.singletonList(areaOfInterest));

		final ResponseEntity<List<AreaOfInterest>> response = geometricIntersectionController
				.getIntersectingGeometries(EXAMPLE_GEOMETRY_WKT);

		Assert.assertNotNull(response);
		Assert.assertEquals(HttpStatus.OK,
							response.getStatusCode());
		Assert.assertNotNull(response.getBody());
		Assert.assertEquals(1,
							response.getBody()
									.size());
		Assert.assertEquals(EXAMPLE_GEOMETRY_WKT,
							response.getBody()
									.get(0)
									.getGeometryWkt());
	}

	@Test
	public void testDeleteGeometryById()
			throws AreaOfInterestIdDoesNotExistException {
		Mockito.doNothing()
				.when(geometricIntersectionService)
				.deleteGeometryById(Mockito.eq(EXAMPLE_ID));

		geometricIntersectionController.deleteGeometryById(EXAMPLE_ID);

		Assert.assertTrue(true);
	}

	@Test(expected = AreaOfInterestIdDoesNotExistException.class)
	public void testDeleteGeometryByIdDoesNotExist()
			throws AreaOfInterestIdDoesNotExistException {
		Mockito.doThrow(new AreaOfInterestIdDoesNotExistException(
				EXAMPLE_ID))
				.when(geometricIntersectionService)
				.deleteGeometryById(Mockito.eq(EXAMPLE_ID));

		geometricIntersectionController.deleteGeometryById(EXAMPLE_ID);
	}
}
