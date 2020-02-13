package com.maxar.spaceobjectcatalog.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import com.maxar.ephemeris.entity.Ephemeris;
import com.maxar.ephemeris.model.EphemerisModel;
import com.maxar.ephemeris.model.EphemerisType;
import com.maxar.spaceobjectcatalog.model.SpaceObject;
import com.maxar.spaceobjectcatalog.repository.SpaceObjectCatalogRepository;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@TestPropertySource(locations = "classpath:test-space-object-catalog.properties")
public class SpaceObjectCatalogServiceTest
{
	private static final Integer EXAMPLE_SCN = 32060;
	private static final DateTime EXAMPLE_EPOCH_TIME = DateTime.parse("2020-01-01T01:00:00Z");
	private static final DateTime EXAMPLE_START_DATE = DateTime.parse("2020-01-01T00:00:00Z");
	private static final DateTime EXAMPLE_END_DATE = DateTime.parse("2020-01-02T00:00:00Z");

	@Autowired
	private SpaceObjectCatalogService spaceObjectCatalogService;

	@Autowired
	private SpaceObjectCatalogRepository spaceObjectCatalogRepository;

	@After
	public void tearDown() {
		spaceObjectCatalogRepository.deleteAll();
	}

	@Test
	public void testGetEphemerisPaginatedOne() {
		spaceObjectCatalogRepository.save(ephemerisFromEpoch(EXAMPLE_EPOCH_TIME));

		final SpaceObject spaceObject = spaceObjectCatalogService.getEphemerisPaginated(EXAMPLE_SCN,
																						0,
																						2);

		Assert.assertNotNull(spaceObject);
		Assert.assertEquals(EXAMPLE_SCN,
							spaceObject.getScn());
		Assert.assertNotNull(spaceObject.getEphemerides());
		Assert.assertEquals(1,
							spaceObject.getEphemerides()
									.size());
		Assert.assertNotNull(spaceObject.getEphemerides()
				.get(0));
		Assert.assertEquals(EXAMPLE_SCN.intValue(),
							spaceObject.getEphemerides()
									.get(0)
									.getScn());
		Assert.assertEquals(EphemerisType.TLE,
							spaceObject.getEphemerides()
									.get(0)
									.getType());
		Assert.assertEquals(EXAMPLE_EPOCH_TIME.getMillis(),
							spaceObject.getEphemerides()
									.get(0)
									.getEpochMillis());
	}

	@Test
	public void testGetEphemerisPaginatedNone() {
		final SpaceObject spaceObject = spaceObjectCatalogService.getEphemerisPaginated(EXAMPLE_SCN,
																						0,
																						2);

		Assert.assertNotNull(spaceObject);
		Assert.assertEquals(EXAMPLE_SCN,
							spaceObject.getScn());
		Assert.assertNotNull(spaceObject.getEphemerides());
		Assert.assertTrue(spaceObject.getEphemerides()
				.isEmpty());
	}

	@Test
	public void testGetEphemerisPaginatedMultiplePages() {
		final List<Ephemeris> ephemerides = Stream.of(	0,
														1,
														2,
														3,
														4)
				.map(EXAMPLE_EPOCH_TIME::plusHours)
				.map(SpaceObjectCatalogServiceTest::ephemerisFromEpoch)
				.collect(Collectors.toList());

		spaceObjectCatalogRepository.saveAll(ephemerides);

		final List<SpaceObject> spaceObjects = new ArrayList<>();
		Assert.assertTrue(spaceObjects.add(spaceObjectCatalogService.getEphemerisPaginated(	EXAMPLE_SCN,
																							0,
																							2)));
		Assert.assertTrue(spaceObjects.add(spaceObjectCatalogService.getEphemerisPaginated(	EXAMPLE_SCN,
																							1,
																							2)));
		Assert.assertTrue(spaceObjects.add(spaceObjectCatalogService.getEphemerisPaginated(	EXAMPLE_SCN,
																							2,
																							2)));

		Assert.assertEquals(3,
							spaceObjects.size());
		Assert.assertTrue(spaceObjects.stream()
				.allMatch(Objects::nonNull));
		Assert.assertTrue(spaceObjects.stream()
				.map(SpaceObject::getScn)
				.allMatch(Predicate.isEqual(EXAMPLE_SCN)));
		Assert.assertTrue(spaceObjects.stream()
				.map(SpaceObject::getEphemerides)
				.allMatch(Objects::nonNull));
		Assert.assertEquals(2,
							spaceObjects.get(0)
									.getEphemerides()
									.size());
		Assert.assertEquals(2,
							spaceObjects.get(1)
									.getEphemerides()
									.size());
		Assert.assertEquals(1,
							spaceObjects.get(2)
									.getEphemerides()
									.size());
		Assert.assertTrue(spaceObjects.stream()
				.map(SpaceObject::getEphemerides)
				.flatMap(List::stream)
				.map(EphemerisModel::getScn)
				.allMatch(Predicate.isEqual(EXAMPLE_SCN)));
		Assert.assertTrue(spaceObjects.stream()
				.map(SpaceObject::getEphemerides)
				.flatMap(List::stream)
				.map(EphemerisModel::getType)
				.allMatch(Predicate.isEqual(EphemerisType.TLE)));

		final List<Long> epochs = spaceObjects.stream()
				.map(SpaceObject::getEphemerides)
				.flatMap(List::stream)
				.map(EphemerisModel::getEpochMillis)
				.collect(Collectors.toList());

		Assert.assertEquals(5,
							epochs.size());
		// The ephemeris entries should be in descending time order (newest first).
		Assert.assertTrue(epochs.get(0) > epochs.get(1));
		Assert.assertTrue(epochs.get(1) > epochs.get(2));
		Assert.assertTrue(epochs.get(2) > epochs.get(3));
		Assert.assertTrue(epochs.get(3) > epochs.get(4));
	}

	@Test
	public void testGetEphemerisCountOne() {
		spaceObjectCatalogRepository.save(ephemerisFromEpoch(EXAMPLE_EPOCH_TIME));

		Assert.assertEquals(1L,
							spaceObjectCatalogService.getEphemerisCount(EXAMPLE_SCN));
	}

	@Test
	public void testGetEphemerisCountNone() {
		Assert.assertEquals(0L,
							spaceObjectCatalogService.getEphemerisCount(EXAMPLE_SCN));
	}

	public void testGetEphemerisCountFive() {
		final List<Ephemeris> ephemerides = Stream.of(	0,
														1,
														2,
														3,
														4)
				.map(EXAMPLE_EPOCH_TIME::plusHours)
				.map(SpaceObjectCatalogServiceTest::ephemerisFromEpoch)
				.collect(Collectors.toList());

		spaceObjectCatalogRepository.saveAll(ephemerides);

		Assert.assertEquals(5L,
							spaceObjectCatalogService.getEphemerisCount(EXAMPLE_SCN));
	}

	@Test
	public void testGetEphemerisInDateRangeOne() {
		spaceObjectCatalogRepository.save(ephemerisFromEpoch(EXAMPLE_EPOCH_TIME));

		final SpaceObject spaceObject = spaceObjectCatalogService.getEphemerisInDateRange(	EXAMPLE_SCN,
																							EXAMPLE_START_DATE,
																							EXAMPLE_END_DATE);

		Assert.assertNotNull(spaceObject);
		Assert.assertEquals(EXAMPLE_SCN,
							spaceObject.getScn());
		Assert.assertNotNull(spaceObject.getEphemerides());
		Assert.assertEquals(1,
							spaceObject.getEphemerides()
									.size());
		Assert.assertNotNull(spaceObject.getEphemerides()
				.get(0));
		Assert.assertEquals(EXAMPLE_SCN.intValue(),
							spaceObject.getEphemerides()
									.get(0)
									.getScn());
		Assert.assertEquals(EphemerisType.TLE,
							spaceObject.getEphemerides()
									.get(0)
									.getType());
		Assert.assertEquals(EXAMPLE_EPOCH_TIME.getMillis(),
							spaceObject.getEphemerides()
									.get(0)
									.getEpochMillis());
	}

	@Test
	public void testGetEphemerisInDateRangeNone() {
		final SpaceObject spaceObject = spaceObjectCatalogService.getEphemerisInDateRange(	EXAMPLE_SCN,
																							EXAMPLE_START_DATE,
																							EXAMPLE_END_DATE);

		Assert.assertNotNull(spaceObject);
		Assert.assertEquals(EXAMPLE_SCN,
							spaceObject.getScn());
		Assert.assertNotNull(spaceObject.getEphemerides());
		Assert.assertTrue(spaceObject.getEphemerides()
				.isEmpty());
	}

	@Test
	public void testGetEphemerisInDateRangeFive() {
		final List<Ephemeris> ephemerides = Stream.of(	0,
														1,
														2,
														3,
														4)
				.map(EXAMPLE_EPOCH_TIME::plusHours)
				.map(SpaceObjectCatalogServiceTest::ephemerisFromEpoch)
				.collect(Collectors.toList());

		spaceObjectCatalogRepository.saveAll(ephemerides);

		final SpaceObject spaceObject = spaceObjectCatalogService.getEphemerisInDateRange(	EXAMPLE_SCN,
																							EXAMPLE_START_DATE,
																							EXAMPLE_END_DATE);

		Assert.assertNotNull(spaceObject);
		Assert.assertEquals(EXAMPLE_SCN,
							spaceObject.getScn());
		Assert.assertNotNull(spaceObject.getEphemerides());
		Assert.assertEquals(5,
							spaceObject.getEphemerides()
									.size());
		Assert.assertTrue(spaceObject.getEphemerides()
				.stream()
				.allMatch(Objects::nonNull));
		Assert.assertTrue(spaceObject.getEphemerides()
				.stream()
				.map(EphemerisModel::getScn)
				.allMatch(Predicate.isEqual(EXAMPLE_SCN)));
		Assert.assertTrue(spaceObject.getEphemerides()
				.stream()
				.map(EphemerisModel::getType)
				.allMatch(Predicate.isEqual(EphemerisType.TLE)));

		final List<Long> epochs = spaceObject.getEphemerides()
				.stream()
				.map(EphemerisModel::getEpochMillis)
				.collect(Collectors.toList());

		Assert.assertEquals(5,
							epochs.size());
		// The ephemeris entries should be in descending time order (newest first).
		Assert.assertTrue(epochs.get(0) > epochs.get(1));
		Assert.assertTrue(epochs.get(1) > epochs.get(2));
		Assert.assertTrue(epochs.get(2) > epochs.get(3));
		Assert.assertTrue(epochs.get(3) > epochs.get(4));
	}

	@Test
	public void testGetEphemerisInDateRangeFiveThreeMatch() {
		final List<Ephemeris> ephemerides = Stream.of(	0,
														1,
														2,
														3,
														4)
				.map(EXAMPLE_EPOCH_TIME::plusHours)
				.map(SpaceObjectCatalogServiceTest::ephemerisFromEpoch)
				.collect(Collectors.toList());

		spaceObjectCatalogRepository.saveAll(ephemerides);

		final SpaceObject spaceObject = spaceObjectCatalogService.getEphemerisInDateRange(	EXAMPLE_SCN,
																							EXAMPLE_EPOCH_TIME
																									.plusHours(2),
																							EXAMPLE_END_DATE);

		Assert.assertNotNull(spaceObject);
		Assert.assertEquals(EXAMPLE_SCN,
							spaceObject.getScn());
		Assert.assertNotNull(spaceObject.getEphemerides());
		Assert.assertEquals(3,
							spaceObject.getEphemerides()
									.size());
		Assert.assertTrue(spaceObject.getEphemerides()
				.stream()
				.allMatch(Objects::nonNull));
		Assert.assertTrue(spaceObject.getEphemerides()
				.stream()
				.map(EphemerisModel::getScn)
				.allMatch(Predicate.isEqual(EXAMPLE_SCN)));
		Assert.assertTrue(spaceObject.getEphemerides()
				.stream()
				.map(EphemerisModel::getType)
				.allMatch(Predicate.isEqual(EphemerisType.TLE)));

		final List<Long> epochs = spaceObject.getEphemerides()
				.stream()
				.map(EphemerisModel::getEpochMillis)
				.collect(Collectors.toList());

		Assert.assertEquals(3,
							epochs.size());
		// The ephemeris entries should be in descending time order (newest first).
		Assert.assertTrue(epochs.get(0) > epochs.get(1));
		Assert.assertTrue(epochs.get(1) > epochs.get(2));
	}

	@Test
	public void testGetEphemerisInDateRangeFiveNoneMatch() {
		final List<Ephemeris> ephemerides = Stream.of(	0,
														1,
														2,
														3,
														4)
				.map(EXAMPLE_EPOCH_TIME::plusHours)
				.map(SpaceObjectCatalogServiceTest::ephemerisFromEpoch)
				.collect(Collectors.toList());

		spaceObjectCatalogRepository.saveAll(ephemerides);

		final SpaceObject spaceObject = spaceObjectCatalogService.getEphemerisInDateRange(	EXAMPLE_SCN,
																							EXAMPLE_START_DATE,
																							EXAMPLE_EPOCH_TIME
																									.minusMinutes(1));

		Assert.assertNotNull(spaceObject);
		Assert.assertEquals(EXAMPLE_SCN,
							spaceObject.getScn());
		Assert.assertNotNull(spaceObject.getEphemerides());
		Assert.assertTrue(spaceObject.getEphemerides()
				.isEmpty());
	}

	private static Ephemeris ephemerisFromEpoch(
			final DateTime epoch ) {
		final Ephemeris ephemeris = new Ephemeris();
		ephemeris.setScn(EXAMPLE_SCN);
		ephemeris.setEpochMillis(epoch.getMillis());
		ephemeris.setType(EphemerisType.TLE);

		return ephemeris;
	}
}
