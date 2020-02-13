package com.maxar.alert.controller;

import java.util.Collections;
import java.util.List;

import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.locationtech.jts.io.ParseException;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import com.maxar.alert.model.Event;
import com.maxar.alert.exception.AlertIdDoesNotExistException;
import com.maxar.alert.exception.AlertIdExistsException;
import com.maxar.alert.exception.InvalidAlertException;
import com.maxar.alert.exception.InvalidRequestException;
import com.maxar.alert.service.AlertService;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@TestPropertySource(locations = "classpath:test-alert.properties")
public class AlertControllerTest
{
	@Autowired
	private AlertController alertController;

	@MockBean
	private AlertService alertService;

	private final static String BASE_URL = "/alert";
	private final static String EXAMPLE_ID = "1";

	@Test
	public void testCreateAlert()
			throws InvalidAlertException,
			AlertIdExistsException {
		Mockito.when(alertService.createAlert(Mockito.any()))
				.thenReturn(EXAMPLE_ID);

		final Event event = new Event();
		event.setId(EXAMPLE_ID);
		event.setType("event0");
		event.setStartTime(DateTime.parse("2019-06-26T00:00:00.000Z"));
		event.setEndTime(DateTime.parse("2019-06-27T00:00:00.000Z"));

		final ResponseEntity<String> response = alertController.createAlert(event);

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

	@Test(expected = InvalidAlertException.class)
	public void testCreateEventNull()
			throws InvalidAlertException,
			AlertIdExistsException {
		Mockito.when(alertService.createAlert(Mockito.isNull()))
				.thenThrow(new InvalidAlertException());

		alertController.createAlert(null);
	}

	@Test(expected = RuntimeException.class)
	public void testCreateEventRuntimeException()
			throws InvalidAlertException,
			AlertIdExistsException {
		Mockito.when(alertService.createAlert(Mockito.any()))
				.thenThrow(new RuntimeException(
						"error"));

		final Event event = new Event();
		event.setId(EXAMPLE_ID);
		event.setType("event0");
		event.setStartTime(DateTime.parse("2019-06-26T00:00:00.000Z"));
		event.setEndTime(DateTime.parse("2019-06-27T00:00:00.000Z"));

		alertController.createAlert(event);
	}

	@Test(expected = RuntimeException.class)
	public void testCreateEventRuntimeExceptionWithEmbeddedException()
			throws InvalidAlertException,
			AlertIdExistsException {
		Mockito.when(alertService.createAlert(Mockito.any()))
				.thenThrow(new RuntimeException(
						new Exception(
								"error")));

		final Event event = new Event();
		event.setId(EXAMPLE_ID);
		event.setType("event0");
		event.setStartTime(DateTime.parse("2019-06-26T00:00:00.000Z"));
		event.setEndTime(DateTime.parse("2019-06-27T00:00:00.000Z"));

		alertController.createAlert(event);
	}

	@Test
	public void testCreateEventParseException()
			throws InvalidAlertException,
			AlertIdExistsException {
		Mockito.when(alertService.createAlert(Mockito.any()))
				.thenThrow(new RuntimeException(
						new ParseException(
								"error")));

		final Event event = new Event();
		event.setId(EXAMPLE_ID);
		event.setType("event0");
		event.setStartTime(DateTime.parse("2019-06-26T00:00:00.000Z"));
		event.setEndTime(DateTime.parse("2019-06-27T00:00:00.000Z"));

		final ResponseEntity<String> response = alertController.createAlert(event);

		Assert.assertNotNull(response);
		Assert.assertEquals(HttpStatus.BAD_REQUEST,
							response.getStatusCode());
		Assert.assertNotNull(response.getBody());
		Assert.assertEquals("error",
							response.getBody());
	}

	@Test(expected = AlertIdExistsException.class)
	public void testCreateEventConflict()
			throws InvalidAlertException,
			AlertIdExistsException {
		Mockito.when(alertService.createAlert(Mockito.any()))
				.thenThrow(new AlertIdExistsException(
						EXAMPLE_ID));

		final Event event = new Event();
		event.setId(EXAMPLE_ID);
		event.setType("event0");
		event.setStartTime(DateTime.parse("2019-06-26T00:00:00.000Z"));
		event.setEndTime(DateTime.parse("2019-06-27T00:00:00.000Z"));

		alertController.createAlert(event);
	}

	@Test
	public void testGetAlerts() {
		Mockito.when(alertService.getAllAlerts())
				.thenReturn(Collections.singletonList(EXAMPLE_ID));

		final ResponseEntity<List<String>> response = alertController.getAlerts();

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
	public void testGetAlertsNone() {
		Mockito.when(alertService.getAllAlerts())
				.thenReturn(Collections.emptyList());

		final ResponseEntity<List<String>> response = alertController.getAlerts();

		Assert.assertNotNull(response);
		Assert.assertEquals(HttpStatus.OK,
							response.getStatusCode());
		Assert.assertNotNull(response.getBody());
		Assert.assertTrue(response.getBody()
				.isEmpty());
	}

	@Test
	public void testGetAlertById()
			throws AlertIdDoesNotExistException {
		Mockito.when(alertService.getAlertById(EXAMPLE_ID))
				.thenReturn(new Event());

		final ResponseEntity<Event> response = alertController.getAlertById(EXAMPLE_ID);

		Assert.assertNotNull(response);
		Assert.assertEquals(HttpStatus.OK,
							response.getStatusCode());
		Assert.assertNotNull(response.getBody());
	}

	@Test(expected = AlertIdDoesNotExistException.class)
	public void testGetAlertByIdDoesNotExist()
			throws AlertIdDoesNotExistException {
		Mockito.when(alertService.getAlertById(EXAMPLE_ID))
				.thenThrow(new AlertIdDoesNotExistException(
						EXAMPLE_ID));

		alertController.getAlertById(EXAMPLE_ID);
	}

	@Test
	public void testGetAlertByGeometry()
			throws InvalidRequestException {
		final String geometryWkt = "POINT(0 0 0)";

		Mockito.when(alertService.getAlertsByGeometry(Mockito.eq(geometryWkt)))
				.thenReturn(Collections.singletonList(new Event()));

		final ResponseEntity<List<Event>> response = alertController.getAlertByGeometry(geometryWkt);

		Assert.assertNotNull(response);
		Assert.assertEquals(HttpStatus.OK,
							response.getStatusCode());
		Assert.assertNotNull(response.getBody());
		Assert.assertEquals(1,
							response.getBody()
									.size());
		Assert.assertNotNull(response.getBody()
				.get(0));
	}

	@Test(expected = InvalidRequestException.class)
	public void testGetAlertByGeometryInvalidRequestException()
			throws InvalidRequestException {
		final String geometryWkt = "PINT(0 0 0)";

		Mockito.when(alertService.getAlertsByGeometry(Mockito.eq(geometryWkt)))
				.thenThrow(new InvalidRequestException(
						"Invalid WKT: " + geometryWkt));

		alertController.getAlertByGeometry(geometryWkt);
	}

	@Test
	public void testDeleteAlertById()
			throws AlertIdDoesNotExistException {
		Mockito.doNothing()
				.when(alertService)
				.deleteAlertById(Mockito.eq(EXAMPLE_ID));

		final ResponseEntity<String> response = alertController.deleteAlertById(EXAMPLE_ID);

		Assert.assertNotNull(response);
		Assert.assertEquals(HttpStatus.OK,
							response.getStatusCode());
		Assert.assertNull(response.getBody());
	}

	@Test(expected = AlertIdDoesNotExistException.class)
	public void testDeleteAlertByIdDoesNotExist()
			throws AlertIdDoesNotExistException {
		Mockito.doThrow(new AlertIdDoesNotExistException(
				EXAMPLE_ID))
				.when(alertService)
				.deleteAlertById(Mockito.eq(EXAMPLE_ID));

		alertController.deleteAlertById(EXAMPLE_ID);
	}
}
