package com.maxar.alert.poll.service;

import java.util.Collections;

import org.joda.time.DateTime;
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

import com.maxar.alert.model.Event;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@TestPropertySource(locations = "classpath:test-alert-poll-disabled.properties")
public class AlertPollServiceTest
{
	@Autowired
	private AlertPollService alertPollService;

	@MockBean
	private ApiService apiService;

	@Before
	public void resume() {
		// Don't leave the service scheduler paused before running a test.
		alertPollService.resume();
	}

	@Test
	public void testPause() {
		Assert.assertTrue(alertPollService.pause());
	}

	@Test
	public void testPauseTwice() {
		Assert.assertTrue(alertPollService.pause());
		Assert.assertFalse(alertPollService.pause());
	}

	@Test
	public void testResumeNotPaused() {
		Assert.assertFalse(alertPollService.resume());
	}

	@Test
	public void testPauseThenResume() {
		Assert.assertTrue(alertPollService.pause());
		Assert.assertTrue(alertPollService.resume());
	}

	@Test
	public void testPauseThenResumeTwice() {
		Assert.assertTrue(alertPollService.pause());
		Assert.assertTrue(alertPollService.resume());
		Assert.assertFalse(alertPollService.resume());
	}

	@Test
	public void testPollForAlerts() {
		final Event event = new Event();
		event.setId("1");
		event.setType("type");
		event.setStartTime(DateTime.parse("2019-09-11T00:00:00.000Z"));
		event.setEndTime(DateTime.parse("2019-09-12T00:00:00.000Z"));
		event.setCountry("country");
		event.setSource("source");
		event.setGeometryWkt("POINT(0 0)");

		Mockito.when(apiService.getAllAlerts())
				.thenReturn(Collections.singletonList(event));

		Mockito.when(apiService.getAllStoredAlertIds())
				.thenReturn(Collections.singletonList("2"));

		Mockito.doNothing()
				.when(apiService)
				.postAlert(Mockito.any());

		Assert.assertTrue(alertPollService.pollForAlerts());
	}

	@Test
	public void testPollForAlertsEmpty() {
		Mockito.when(apiService.getAllAlerts())
				.thenReturn(Collections.emptyList());

		Mockito.when(apiService.getAllStoredAlertIds())
				.thenThrow(new RuntimeException(
						"Should not have requested stored IDs when no alerts were returned"));

		Assert.assertTrue(alertPollService.pollForAlerts());
	}

	@Test
	public void testPollForAlertsNull() {
		Mockito.when(apiService.getAllAlerts())
				.thenReturn(Collections.singletonList(null));

		Mockito.when(apiService.getAllStoredAlertIds())
				.thenReturn(Collections.emptyList());

		Mockito.doThrow(new RuntimeException(
				"Should not have posted any alerts"))
				.when(apiService)
				.postAlert(Mockito.any());

		Assert.assertTrue(alertPollService.pollForAlerts());
	}

	@Test
	public void testPollForAlertsNullGeometryWkt() {
		final Event event = new Event();
		event.setId("1");
		event.setType("type");
		event.setStartTime(DateTime.parse("2019-09-11T00:00:00.000Z"));
		event.setEndTime(DateTime.parse("2019-09-12T00:00:00.000Z"));
		event.setCountry("country");
		event.setSource("source");
		event.setGeometryWkt(null);

		Mockito.when(apiService.getAllAlerts())
				.thenReturn(Collections.singletonList(event));

		Mockito.when(apiService.getAllStoredAlertIds())
				.thenReturn(Collections.emptyList());

		Mockito.doThrow(new RuntimeException(
				"Should not have posted any alerts"))
				.when(apiService)
				.postAlert(Mockito.any());

		Assert.assertTrue(alertPollService.pollForAlerts());
	}

	@Test
	public void testPollForAlertsNonUnique() {
		final Event event = new Event();
		event.setId("1");
		event.setType("type");
		event.setStartTime(DateTime.parse("2019-09-11T00:00:00.000Z"));
		event.setEndTime(DateTime.parse("2019-09-12T00:00:00.000Z"));
		event.setCountry("country");
		event.setSource("source");
		event.setGeometryWkt("POINT(0 0)");

		Mockito.when(apiService.getAllAlerts())
				.thenReturn(Collections.singletonList(event));

		Mockito.when(apiService.getAllStoredAlertIds())
				.thenReturn(Collections.singletonList("1"));

		Mockito.doThrow(new RuntimeException(
				"Should not have posted any alerts"))
				.when(apiService)
				.postAlert(Mockito.any());

		Assert.assertTrue(alertPollService.pollForAlerts());
	}

	@Test
	public void testPollForAlertsPaused() {
		Mockito.when(apiService.getAllAlerts())
				.thenThrow(new RuntimeException(
						"Should not have requested alerts while paused"));

		alertPollService.pause();

		Assert.assertFalse(alertPollService.pollForAlerts());
	}

	@Test
	public void testPollForAlertsPausedForcePoll() {
		final Event event = new Event();
		event.setId("1");
		event.setType("type");
		event.setStartTime(DateTime.parse("2019-09-11T00:00:00.000Z"));
		event.setEndTime(DateTime.parse("2019-09-12T00:00:00.000Z"));
		event.setCountry("country");
		event.setSource("source");
		event.setGeometryWkt("POINT(0 0)");

		Mockito.when(apiService.getAllAlerts())
				.thenReturn(Collections.singletonList(event));

		Mockito.when(apiService.getAllStoredAlertIds())
				.thenReturn(Collections.singletonList("2"));

		Mockito.doNothing()
				.when(apiService)
				.postAlert(Mockito.any());

		alertPollService.pause();

		Assert.assertTrue(alertPollService.pollForAlerts(true));
	}
}
