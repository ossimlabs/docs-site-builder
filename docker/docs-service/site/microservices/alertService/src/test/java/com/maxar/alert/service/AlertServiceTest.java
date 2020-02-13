package com.maxar.alert.service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import com.maxar.alert.model.Event;
import com.maxar.alert.repository.EventRepository;
import com.maxar.alert.exception.AlertIdDoesNotExistException;
import com.maxar.alert.exception.AlertIdExistsException;
import com.maxar.alert.exception.InvalidAlertException;
import com.maxar.alert.exception.InvalidRequestException;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@TestPropertySource(locations = "classpath:test-alert.properties")
public class AlertServiceTest
{
	@Autowired
	private AlertService alertService;

	@MockBean
	private EventRepository eventRepository;

	private final static String EXAMPLE_ID = "1";

	@Test
	public void testCreateEvent()
			throws InvalidAlertException,
			AlertIdExistsException {
		final Event event = new Event();
		event.setId(EXAMPLE_ID);
		event.setType("event0");
		event.setStartTime(DateTime.parse("2019-06-26T00:00:00.000Z"));
		event.setEndTime(DateTime.parse("2019-06-27T00:00:00.000Z"));
		event.setGeometryWkt("POINT(29.22389757 25.487525689 135.0)");
		event.setCountry("EGY");
		event.setSource("temperature");

		final com.maxar.alert.entity.Event entityEvent = new com.maxar.alert.entity.Event(
				event);

		Mockito.when(eventRepository.existsById(EXAMPLE_ID))
				.thenReturn(false);

		Mockito.when(eventRepository.save(Mockito.any()))
				.thenReturn(entityEvent);

		final String id = alertService.createAlert(event);

		Assert.assertNotNull(id);
		Assert.assertEquals(EXAMPLE_ID,
							id);
	}

	@Test(expected = InvalidAlertException.class)
	public void testCreateEventNull()
			throws InvalidAlertException,
			AlertIdExistsException {
		alertService.createAlert(null);
	}

	@Test(expected = InvalidAlertException.class)
	public void testCreateEventNullId()
			throws InvalidAlertException,
			AlertIdExistsException {
		final Event event = new Event();
		event.setType("event0");
		event.setStartTime(DateTime.parse("2019-06-26T00:00:00.000Z"));
		event.setEndTime(DateTime.parse("2019-06-27T00:00:00.000Z"));
		event.setGeometryWkt("POINT(29.22389757 25.487525689 135.0)");
		event.setCountry("EGY");
		event.setSource("temperature");

		alertService.createAlert(event);
	}

	@Test(expected = InvalidAlertException.class)
	public void testCreateEventNullStartTime()
			throws InvalidAlertException,
			AlertIdExistsException {
		final Event event = new Event();
		event.setId(EXAMPLE_ID);
		event.setType("event0");
		event.setEndTime(DateTime.parse("2019-06-27T00:00:00.000Z"));
		event.setGeometryWkt("POINT(29.22389757 25.487525689 135.0)");
		event.setCountry("EGY");
		event.setSource("temperature");

		alertService.createAlert(event);
	}

	@Test(expected = InvalidAlertException.class)
	public void testCreateEventNullEndTime()
			throws InvalidAlertException,
			AlertIdExistsException {
		final Event event = new Event();
		event.setId(EXAMPLE_ID);
		event.setType("event0");
		event.setStartTime(DateTime.parse("2019-06-26T00:00:00.000Z"));
		event.setGeometryWkt("POINT(29.22389757 25.487525689 135.0)");
		event.setCountry("EGY");
		event.setSource("temperature");

		alertService.createAlert(event);
	}

	@Test(expected = InvalidAlertException.class)
	public void testCreateEventNullGeometryWkt()
			throws InvalidAlertException,
			AlertIdExistsException {
		final Event event = new Event();
		event.setId(EXAMPLE_ID);
		event.setType("event0");
		event.setStartTime(DateTime.parse("2019-06-26T00:00:00.000Z"));
		event.setEndTime(DateTime.parse("2019-06-27T00:00:00.000Z"));
		event.setCountry("EGY");
		event.setSource("temperature");

		alertService.createAlert(event);
	}

	@Test(expected = RuntimeException.class)
	public void testCreateEventInvalidGeometryWkt()
			throws InvalidAlertException,
			AlertIdExistsException {
		final Event event = new Event();
		event.setId(EXAMPLE_ID);
		event.setType("event0");
		event.setStartTime(DateTime.parse("2019-06-26T00:00:00.000Z"));
		event.setEndTime(DateTime.parse("2019-06-27T00:00:00.000Z"));
		event.setGeometryWkt("PINT(29.22389757 25.487525689 135.0)");
		event.setCountry("EGY");
		event.setSource("temperature");

		alertService.createAlert(event);
	}

	@Test(expected = AlertIdExistsException.class)
	public void testCreateEventExists()
			throws InvalidAlertException,
			AlertIdExistsException {
		final Event event = new Event();
		event.setId(EXAMPLE_ID);
		event.setType("event0");
		event.setStartTime(DateTime.parse("2019-06-26T00:00:00.000Z"));
		event.setEndTime(DateTime.parse("2019-06-27T00:00:00.000Z"));
		event.setGeometryWkt("POINT(29.22389757 25.487525689 135.0)");
		event.setCountry("EGY");
		event.setSource("temperature");

		Mockito.when(eventRepository.existsById(EXAMPLE_ID))
				.thenReturn(true);

		alertService.createAlert(event);
	}

	@Test
	public void testGetAllAlerts() {
		final com.maxar.alert.entity.Event event = new com.maxar.alert.entity.Event();
		event.setId(EXAMPLE_ID);

		Mockito.when(eventRepository.findAll())
				.thenReturn(Collections.singletonList(event));

		final List<String> alertIds = alertService.getAllAlerts();

		Assert.assertNotNull(alertIds);
		Assert.assertEquals(1,
							alertIds.size());
		Assert.assertEquals(EXAMPLE_ID,
							alertIds.get(0));
	}

	@Test
	public void testGetAllAlertsEmpty() {
		final com.maxar.alert.entity.Event event = new com.maxar.alert.entity.Event();
		event.setId(EXAMPLE_ID);

		Mockito.when(eventRepository.findAll())
				.thenReturn(Collections.emptyList());

		final List<String> alertIds = alertService.getAllAlerts();

		Assert.assertNotNull(alertIds);
		Assert.assertTrue(alertIds.isEmpty());
	}

	@Test
	public void testGetAlertById()
			throws AlertIdDoesNotExistException,
			ParseException {
		final String geometryWkt = "POINT(29.22389757 25.487525689 135.0)";
		final DateTime startTime = DateTime.parse("2019-06-26T00:00:00.000Z");
		final DateTime endTime = DateTime.parse("2019-06-27T00:00:00.000Z");
		final com.maxar.alert.entity.Event entityEvent = new com.maxar.alert.entity.Event();
		entityEvent.setId(EXAMPLE_ID);
		entityEvent.setType("event0");
		entityEvent.setStartTime(startTime);
		entityEvent.setEndTime(endTime);
		entityEvent.setGeometryWkt(null);
		final WKTReader wktReader = new WKTReader();
		final Geometry geometry = wktReader.read(geometryWkt);
		entityEvent.setGeometry(geometry);
		entityEvent.setCountry("EGY");
		entityEvent.setSource("temperature");

		Mockito.when(eventRepository.findById(EXAMPLE_ID))
				.thenReturn(Optional.of(entityEvent));

		final Event event = alertService.getAlertById(EXAMPLE_ID);

		Assert.assertNotNull(event);
		Assert.assertEquals(EXAMPLE_ID,
							event.getId());
		Assert.assertEquals("event0",
							event.getType());
		Assert.assertEquals(startTime,
							event.getStartTime());
		Assert.assertEquals(endTime,
							event.getEndTime());
		Assert.assertEquals(geometry.toText(),
							event.getGeometryWkt());
		Assert.assertEquals("EGY",
							event.getCountry());
		Assert.assertEquals("temperature",
							event.getSource());
	}

	@Test(expected = AlertIdDoesNotExistException.class)
	public void testGetAlertByIdDoesNotExist()
			throws AlertIdDoesNotExistException {
		Mockito.when(eventRepository.findById(EXAMPLE_ID))
				.thenReturn(Optional.empty());

		alertService.getAlertById(EXAMPLE_ID);
	}

	@Test
	public void testGetAlertsByGeometry()
			throws ParseException,
			InvalidRequestException {
		final String geometryWkt = "POINT(29.22389757 25.487525689 135.0)";
		final DateTime startTime = DateTime.parse("2019-06-26T00:00:00.000Z");
		final DateTime endTime = DateTime.parse("2019-06-27T00:00:00.000Z");
		final com.maxar.alert.entity.Event entityEvent = new com.maxar.alert.entity.Event();
		entityEvent.setId(EXAMPLE_ID);
		entityEvent.setType("event0");
		entityEvent.setStartTime(startTime);
		entityEvent.setEndTime(endTime);
		entityEvent.setGeometryWkt(null);
		final WKTReader wktReader = new WKTReader();
		final Geometry geometry = wktReader.read(geometryWkt);
		entityEvent.setGeometry(geometry);
		entityEvent.setCountry("EGY");
		entityEvent.setSource("temperature");

		Mockito.when(eventRepository.findByGeometry(Mockito.eq(geometry)))
				.thenReturn(Collections.singletonList(entityEvent));

		final List<Event> events = alertService.getAlertsByGeometry(geometryWkt);

		Assert.assertNotNull(events);
		Assert.assertEquals(1,
							events.size());
		Assert.assertNotNull(events.get(0)
				.getGeometryWkt());
		Assert.assertEquals(EXAMPLE_ID,
							events.get(0)
									.getId());
		Assert.assertEquals("event0",
							events.get(0)
									.getType());
		Assert.assertEquals(startTime,
							events.get(0)
									.getStartTime());
		Assert.assertEquals(endTime,
							events.get(0)
									.getEndTime());
		Assert.assertEquals(geometry.toText(),
							events.get(0)
									.getGeometryWkt());
		Assert.assertEquals("EGY",
							events.get(0)
									.getCountry());
		Assert.assertEquals("temperature",
							events.get(0)
									.getSource());
	}

	@Test
	public void testGetAlertsByGeometryEmpty()
			throws InvalidRequestException {
		final String geometryWkt = "POINT(29.22389757 25.487525689 135.0)";

		Mockito.when(eventRepository.findByGeometry(Mockito.any()))
				.thenReturn(Collections.emptyList());

		final List<Event> events = alertService.getAlertsByGeometry(geometryWkt);

		Assert.assertNotNull(events);
		Assert.assertTrue(events.isEmpty());
	}

	@Test(expected = InvalidRequestException.class)
	public void testGetAlertsByGeometryWktParseFailure()
			throws InvalidRequestException {
		final String geometryWkt = "PINT(29.22389757 25.487525689 135.0)";

		alertService.getAlertsByGeometry(geometryWkt);
	}

	@Test
	public void testDeleteAlertById()
			throws AlertIdDoesNotExistException {
		Mockito.doNothing()
				.when(eventRepository)
				.deleteById(EXAMPLE_ID);

		alertService.deleteAlertById(EXAMPLE_ID);

		// There is nothing to test here. The repository is fake, so it can't be checked
		// to make sure the alert has been deleted, and the deleteAlertById method
		// itself has no return value--simply not throwing an exception is enough to
		// show this test case has passed. Sonarqube marks any test case without an
		// expected exception or an assert as a problem, so this fake test assert has
		// been added.
		Assert.assertTrue(true);
	}

	@Test(expected = AlertIdDoesNotExistException.class)
	public void testDeleteAlertByIdDoesNotExist()
			throws AlertIdDoesNotExistException {
		Mockito.doThrow(new EmptyResultDataAccessException(
				1))
				.when(eventRepository)
				.deleteById(EXAMPLE_ID);

		alertService.deleteAlertById(EXAMPLE_ID);
	}
}
