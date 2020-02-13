package com.maxar.alert.poll.controller;

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

import com.maxar.alert.poll.service.AlertPollService;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@TestPropertySource(locations = "classpath:test-alert-poll-disabled.properties")
public class AlertPollControllerTest
{
	@Autowired
	private AlertPollController alertPollController;

	@MockBean
	private AlertPollService alertPollService;

	@Test
	public void testPause() {
		Mockito.when(alertPollService.pause())
				.thenReturn(true);

		final ResponseEntity<String> response = alertPollController.pause();

		Assert.assertNotNull(response);
		Assert.assertEquals(HttpStatus.OK,
							response.getStatusCode());
		Assert.assertNotNull(response.getBody());
	}

	@Test
	public void testPauseAlreadyPaused() {
		Mockito.when(alertPollService.pause())
				.thenReturn(false);

		final ResponseEntity<String> response = alertPollController.pause();

		Assert.assertNotNull(response);
		Assert.assertEquals(HttpStatus.OK,
							response.getStatusCode());
		Assert.assertNotNull(response.getBody());
	}

	@Test
	public void testResume() {
		Mockito.when(alertPollService.resume())
				.thenReturn(true);

		final ResponseEntity<String> response = alertPollController.resume();

		Assert.assertNotNull(response);
		Assert.assertEquals(HttpStatus.OK,
							response.getStatusCode());
		Assert.assertNotNull(response.getBody());
	}

	@Test
	public void testResumeNotPaused() {
		Mockito.when(alertPollService.resume())
				.thenReturn(false);

		final ResponseEntity<String> response = alertPollController.resume();

		Assert.assertNotNull(response);
		Assert.assertEquals(HttpStatus.OK,
							response.getStatusCode());
		Assert.assertNotNull(response.getBody());
	}

	@Test
	public void testPollForAlertsNoOverride() {
		Mockito.when(alertPollService.pollForAlerts(Mockito.eq(false)))
				.thenReturn(true);

		final ResponseEntity<String> response = alertPollController.pollForAlerts(false);

		Assert.assertNotNull(response);
		Assert.assertEquals(HttpStatus.OK,
							response.getStatusCode());
		Assert.assertNotNull(response.getBody());
	}

	@Test
	public void testPollForAlertsNoOverridePaused() {
		Mockito.when(alertPollService.pollForAlerts(Mockito.eq(false)))
				.thenReturn(false);

		final ResponseEntity<String> response = alertPollController.pollForAlerts(false);

		Assert.assertNotNull(response);
		Assert.assertEquals(HttpStatus.OK,
							response.getStatusCode());
		Assert.assertNotNull(response.getBody());
	}

	@Test
	public void testPollForAlertsOverride() {
		Mockito.when(alertPollService.pollForAlerts(Mockito.eq(true)))
				.thenReturn(true);

		final ResponseEntity<String> response = alertPollController.pollForAlerts(true);

		Assert.assertNotNull(response);
		Assert.assertEquals(HttpStatus.OK,
							response.getStatusCode());
		Assert.assertNotNull(response.getBody());
	}

	@Test
	public void testPollForAlertsOverridePaused() {
		Mockito.when(alertPollService.pollForAlerts(Mockito.eq(true)))
				.thenReturn(true);

		final ResponseEntity<String> response = alertPollController.pollForAlerts(true);

		Assert.assertNotNull(response);
		Assert.assertEquals(HttpStatus.OK,
							response.getStatusCode());
		Assert.assertNotNull(response.getBody());
	}
}
