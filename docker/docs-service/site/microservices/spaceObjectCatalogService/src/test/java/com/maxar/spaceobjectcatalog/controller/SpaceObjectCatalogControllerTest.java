package com.maxar.spaceobjectcatalog.controller;

import java.util.Collections;

import org.joda.time.DateTime;
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

import com.maxar.common.exception.BadRequestException;
import com.maxar.spaceobjectcatalog.model.SpaceObject;
import com.maxar.spaceobjectcatalog.service.SpaceObjectCatalogService;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@TestPropertySource(locations = "classpath:test-space-object-catalog.properties")
public class SpaceObjectCatalogControllerTest
{
	private static final Integer EXAMPLE_SCN = 32060;
	private static final Integer EXAMPLE_SCN_INVALID_NEGATIVE = -1;
	private static final Integer EXAMPLE_SCN_INVALID_LARGE = 100000;
	private static final Integer EXAMPLE_PAGE = 0;
	private static final Integer EXAMPLE_PAGE_INVALID = -1;
	private static final Integer EXAMPLE_COUNT = 100;
	private static final Integer EXAMPLE_COUNT_INVALID = 0;
	private static final String EXAMPLE_START_DATE = "2020-01-01T00:00:00Z";
	private static final String EXAMPLE_START_DATE_INVALID = "2020-0100:00:00Z";
	private static final String EXAMPLE_END_DATE = "2020-01-02T00:00:00Z";
	private static final String EXAMPLE_END_DATE_INVALID = "201-02T0000Z";

	@Autowired
	private SpaceObjectCatalogController spaceObjectCatalogController;

	@MockBean
	private SpaceObjectCatalogService spaceObjectCatalogService;

	@Test
	public void testGetEphemerisPaginated() {
		final SpaceObject spaceObject = new SpaceObject();
		spaceObject.setScn(EXAMPLE_SCN);
		spaceObject.setEphemerides(Collections.emptyList());

		Mockito.when(spaceObjectCatalogService.getEphemerisPaginated(	Mockito.eq(EXAMPLE_SCN),
																		Mockito.eq(EXAMPLE_PAGE),
																		Mockito.eq(EXAMPLE_COUNT)))
				.thenReturn(spaceObject);

		final ResponseEntity<SpaceObject> response = spaceObjectCatalogController.getEphemerisPaginated(EXAMPLE_SCN,
																										EXAMPLE_PAGE,
																										EXAMPLE_COUNT);

		Assert.assertNotNull(response);
		Assert.assertEquals(HttpStatus.OK,
							response.getStatusCode());
		Assert.assertNotNull(response.getBody());
		Assert.assertEquals(EXAMPLE_SCN,
							response.getBody()
									.getScn());
		Assert.assertNotNull(response.getBody()
				.getEphemerides());
		Assert.assertTrue(response.getBody()
				.getEphemerides()
				.isEmpty());
	}

	@Test(expected = BadRequestException.class)
	public void testGetEphemerisPaginatedScnNull() {
		spaceObjectCatalogController.getEphemerisPaginated(	null,
															EXAMPLE_PAGE,
															EXAMPLE_COUNT);
	}

	@Test(expected = BadRequestException.class)
	public void testGetEphemerisPaginatedScnNegative() {
		spaceObjectCatalogController.getEphemerisPaginated(	EXAMPLE_SCN_INVALID_NEGATIVE,
															EXAMPLE_PAGE,
															EXAMPLE_COUNT);
	}

	@Test(expected = BadRequestException.class)
	public void testGetEphemerisPaginatedScnLarge() {
		spaceObjectCatalogController.getEphemerisPaginated(	EXAMPLE_SCN_INVALID_LARGE,
															EXAMPLE_PAGE,
															EXAMPLE_COUNT);
	}

	@Test(expected = BadRequestException.class)
	public void testGetEphemerisPaginatedPageNull() {
		spaceObjectCatalogController.getEphemerisPaginated(	EXAMPLE_SCN,
															null,
															EXAMPLE_COUNT);
	}

	@Test(expected = BadRequestException.class)
	public void testGetEphemerisPaginatedPageNegative() {
		spaceObjectCatalogController.getEphemerisPaginated(	EXAMPLE_SCN,
															EXAMPLE_PAGE_INVALID,
															EXAMPLE_COUNT);
	}

	@Test(expected = BadRequestException.class)
	public void testGetEphemerisPaginatedCountNull() {
		spaceObjectCatalogController.getEphemerisPaginated(	EXAMPLE_SCN,
															EXAMPLE_PAGE,
															null);
	}

	@Test(expected = BadRequestException.class)
	public void testGetEphemerisPaginatedCountZero() {
		spaceObjectCatalogController.getEphemerisPaginated(	EXAMPLE_SCN,
															EXAMPLE_PAGE,
															EXAMPLE_COUNT_INVALID);
	}

	@Test
	public void testGetEphemerisCount() {
		Mockito.when(spaceObjectCatalogService.getEphemerisCount(Mockito.eq(EXAMPLE_SCN)))
				.thenReturn(10L);

		final ResponseEntity<Long> response = spaceObjectCatalogController.getEphemerisCount(EXAMPLE_SCN);

		Assert.assertNotNull(response);
		Assert.assertEquals(HttpStatus.OK,
							response.getStatusCode());
		Assert.assertNotNull(response.getBody());
		Assert.assertEquals(Long.valueOf(10L),
							response.getBody());
	}

	@Test(expected = BadRequestException.class)
	public void testGetEphemerisCountScnNull() {
		spaceObjectCatalogController.getEphemerisCount(null);
	}

	@Test(expected = BadRequestException.class)
	public void testGetEphemerisCountScnNegative() {
		spaceObjectCatalogController.getEphemerisCount(EXAMPLE_SCN_INVALID_NEGATIVE);
	}

	@Test(expected = BadRequestException.class)
	public void testGetEphemerisCountScnLarge() {
		spaceObjectCatalogController.getEphemerisCount(EXAMPLE_SCN_INVALID_LARGE);
	}

	@Test
	public void testGetEphemerisInDateRange() {
		final SpaceObject spaceObject = new SpaceObject();
		spaceObject.setScn(EXAMPLE_SCN);
		spaceObject.setEphemerides(Collections.emptyList());

		Mockito.when(spaceObjectCatalogService.getEphemerisInDateRange(	Mockito.eq(EXAMPLE_SCN),
																		Mockito.any(DateTime.class),
																		Mockito.any(DateTime.class)))
				.thenReturn(spaceObject);

		final ResponseEntity<SpaceObject> response = spaceObjectCatalogController.getEphemerisInDateRange(	EXAMPLE_SCN,
																											EXAMPLE_START_DATE,
																											EXAMPLE_END_DATE);

		Assert.assertNotNull(response);
		Assert.assertEquals(HttpStatus.OK,
							response.getStatusCode());
		Assert.assertNotNull(response.getBody());
		Assert.assertEquals(EXAMPLE_SCN,
							response.getBody()
									.getScn());
		Assert.assertNotNull(response.getBody()
				.getEphemerides());
		Assert.assertTrue(response.getBody()
				.getEphemerides()
				.isEmpty());
	}

	@Test(expected = BadRequestException.class)
	public void testGetEphemerisInDateRangeScnNull() {
		spaceObjectCatalogController.getEphemerisInDateRange(	null,
																EXAMPLE_START_DATE,
																EXAMPLE_END_DATE);
	}

	@Test(expected = BadRequestException.class)
	public void testGetEphemerisInDateRangeScnNegative() {
		spaceObjectCatalogController.getEphemerisInDateRange(	EXAMPLE_SCN_INVALID_NEGATIVE,
																EXAMPLE_START_DATE,
																EXAMPLE_END_DATE);
	}

	@Test(expected = BadRequestException.class)
	public void testGetEphemerisInDateRangeScnLarge() {
		spaceObjectCatalogController.getEphemerisInDateRange(	EXAMPLE_SCN_INVALID_LARGE,
																EXAMPLE_START_DATE,
																EXAMPLE_END_DATE);
	}

	@Test(expected = BadRequestException.class)
	public void testGetEphemerisInDateRangeStartNull() {
		spaceObjectCatalogController.getEphemerisInDateRange(	EXAMPLE_SCN,
																null,
																EXAMPLE_END_DATE);
	}

	@Test(expected = BadRequestException.class)
	public void testGetEphemerisInDateRangeStartInvalid() {
		spaceObjectCatalogController.getEphemerisInDateRange(	EXAMPLE_SCN,
																EXAMPLE_START_DATE_INVALID,
																EXAMPLE_END_DATE);
	}

	@Test(expected = BadRequestException.class)
	public void testGetEphemerisInDateRangeEndNull() {
		spaceObjectCatalogController.getEphemerisInDateRange(	EXAMPLE_SCN,
																EXAMPLE_START_DATE,
																null);
	}

	@Test(expected = BadRequestException.class)
	public void testGetEphemerisInDateRangeEndInvalid() {
		spaceObjectCatalogController.getEphemerisInDateRange(	EXAMPLE_SCN,
																EXAMPLE_START_DATE,
																EXAMPLE_END_DATE_INVALID);
	}
}
