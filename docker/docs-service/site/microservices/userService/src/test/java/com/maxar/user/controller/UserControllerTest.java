package com.maxar.user.controller;

import java.net.URISyntaxException;
import java.util.Collections;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import com.maxar.user.model.UserPreferences;
import com.maxar.user.model.UserSessions;
import com.maxar.user.exception.SessionExistsException;
import com.maxar.user.exception.SessionNotFoundException;
import com.maxar.user.exception.UserExistsException;
import com.maxar.user.exception.UserNotFoundException;
import com.maxar.user.exception.UserPreferencesProcessingException;
import com.maxar.user.service.UserService;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@TestPropertySource(locations = "classpath:test-user.properties")
public class UserControllerTest
{
	@Autowired
	private UserController userSessionController;

	@MockBean
	private UserService userSessionService;

	private static final String EXAMPLE_USER = "user0";

	private static final UserPreferences EXAMPLE_JSON = new UserPreferences();

	private static final String EXAMPLE_SESSION = "session0";

	private static final String EXAMPLE_SESSION2 = "session2";

	@Test
	public void testCreateUser()
			throws UserExistsException,
			URISyntaxException,
			UserPreferencesProcessingException {
		Mockito.when(userSessionService.createUser(	ArgumentMatchers.eq(EXAMPLE_USER),
													ArgumentMatchers.eq(EXAMPLE_JSON)))
				.thenReturn(EXAMPLE_USER);

		final ResponseEntity<String> response = userSessionController.createUser(	EXAMPLE_USER,
																					EXAMPLE_JSON);

		Assert.assertEquals(HttpStatus.CREATED,
							response.getStatusCode());

		final List<String> locationHeaders = response.getHeaders()
				.get("Location");

		Assert.assertNotNull(locationHeaders);
		Assert.assertEquals(1,
							locationHeaders.size());
		Assert.assertEquals("/user/" + EXAMPLE_USER,
							locationHeaders.get(0));
	}

	@Test(expected = UserExistsException.class)
	public void testCreateUserExists()
			throws UserExistsException,
			URISyntaxException,
			UserPreferencesProcessingException {
		Mockito.when(userSessionService.createUser(	ArgumentMatchers.eq(EXAMPLE_USER),
													ArgumentMatchers.eq(EXAMPLE_JSON)))
				.thenThrow(new UserExistsException(
						EXAMPLE_USER));

		userSessionController.createUser(	EXAMPLE_USER,
											EXAMPLE_JSON);
	}

	@Test
	public void testCreateUserSession()
			throws UserNotFoundException,
			SessionExistsException,
			URISyntaxException {
		Mockito.when(userSessionService.createUserSession(	ArgumentMatchers.eq(EXAMPLE_USER),
															ArgumentMatchers.eq(EXAMPLE_SESSION)))
				.thenReturn(EXAMPLE_SESSION);

		final ResponseEntity<String> response = userSessionController.createUserSession(EXAMPLE_USER,
																						EXAMPLE_SESSION);

		Assert.assertEquals(HttpStatus.CREATED,
							response.getStatusCode());

		final List<String> locationHeaders = response.getHeaders()
				.get("Location");

		Assert.assertNotNull(locationHeaders);
		Assert.assertEquals(1,
							locationHeaders.size());
		Assert.assertEquals("/user/" + EXAMPLE_USER + "/session/" + EXAMPLE_SESSION,
							locationHeaders.get(0));
	}

	@Test(expected = UserNotFoundException.class)
	public void testCreateUserSessionUserNotFound()
			throws UserNotFoundException,
			SessionExistsException,
			URISyntaxException {
		Mockito.when(userSessionService.createUserSession(	ArgumentMatchers.eq(EXAMPLE_USER),
															ArgumentMatchers.eq(EXAMPLE_SESSION)))
				.thenThrow(new UserNotFoundException(
						EXAMPLE_USER));

		userSessionController.createUserSession(EXAMPLE_USER,
												EXAMPLE_SESSION);
	}

	@Test(expected = SessionExistsException.class)
	public void testCreateUserSessionExists()
			throws UserNotFoundException,
			SessionExistsException,
			URISyntaxException {
		Mockito.when(userSessionService.createUserSession(	ArgumentMatchers.eq(EXAMPLE_USER),
															ArgumentMatchers.eq(EXAMPLE_SESSION)))
				.thenThrow(new SessionExistsException(
						EXAMPLE_USER,
						EXAMPLE_SESSION));

		userSessionController.createUserSession(EXAMPLE_USER,
												EXAMPLE_SESSION);
	}

	@Test
	public void testGetUsers() {
		Mockito.when(userSessionService.getUsers())
				.thenReturn(Collections.singletonList(EXAMPLE_USER));

		final ResponseEntity<List<String>> response = userSessionController.getUsers();

		Assert.assertEquals(HttpStatus.OK,
							response.getStatusCode());

		final List<String> users = response.getBody();

		Assert.assertNotNull(users);
		Assert.assertEquals(1,
							users.size());
		Assert.assertEquals(EXAMPLE_USER,
							users.get(0));
	}

	@Test
	public void testGetUsersEmpty() {
		Mockito.when(userSessionService.getUsers())
				.thenReturn(Collections.emptyList());

		final ResponseEntity<List<String>> response = userSessionController.getUsers();

		Assert.assertEquals(HttpStatus.OK,
							response.getStatusCode());

		final List<String> users = response.getBody();

		Assert.assertNotNull(users);
		Assert.assertTrue(users.isEmpty());
	}

	@Test
	public void testGetSessionsForUser()
			throws UserNotFoundException {
		final UserSessions userSessions = new UserSessions();
		userSessions.setUser(EXAMPLE_USER);
		userSessions.setSessionIds(Collections.singleton(EXAMPLE_SESSION));

		Mockito.when(userSessionService.getSessionsForUser(EXAMPLE_USER))
				.thenReturn(userSessions);

		final ResponseEntity<UserSessions> response = userSessionController.getSessionsForUser(EXAMPLE_USER);

		Assert.assertEquals(HttpStatus.OK,
							response.getStatusCode());

		final UserSessions returnedUserSessions = response.getBody();

		Assert.assertNotNull(returnedUserSessions);
		Assert.assertEquals(EXAMPLE_USER,
							returnedUserSessions.getUser());
		Assert.assertNotNull(userSessions.getSessionIds());
		Assert.assertEquals(1,
							userSessions.getSessionIds()
									.size());
		Assert.assertTrue(userSessions.getSessionIds()
				.contains(EXAMPLE_SESSION));
	}

	@Test
	public void testGetSessionsForUserEmpty()
			throws UserNotFoundException {
		final UserSessions userSessions = new UserSessions();
		userSessions.setUser(EXAMPLE_USER);

		Mockito.when(userSessionService.getSessionsForUser(EXAMPLE_USER))
				.thenReturn(userSessions);

		final ResponseEntity<UserSessions> response = userSessionController.getSessionsForUser(EXAMPLE_USER);

		Assert.assertEquals(HttpStatus.OK,
							response.getStatusCode());

		final UserSessions returnedUserSessions = response.getBody();

		Assert.assertNotNull(returnedUserSessions);
		Assert.assertEquals(EXAMPLE_USER,
							returnedUserSessions.getUser());
		Assert.assertNotNull(userSessions.getSessionIds());
		Assert.assertTrue(userSessions.getSessionIds()
				.isEmpty());
	}

	@Test(expected = UserNotFoundException.class)
	public void testGetSessionsForUserNotFound()
			throws UserNotFoundException {
		Mockito.when(userSessionService.getSessionsForUser(EXAMPLE_USER))
				.thenThrow(new UserNotFoundException(
						EXAMPLE_USER));

		userSessionController.getSessionsForUser(EXAMPLE_USER);
	}

	@Test
	public void testDeleteUser()
			throws UserNotFoundException {
		Mockito.doNothing()
				.when(userSessionService)
				.deleteUser(ArgumentMatchers.eq(EXAMPLE_USER));

		final ResponseEntity<String> response = userSessionController.deleteUser(EXAMPLE_USER);

		Assert.assertEquals(HttpStatus.OK,
							response.getStatusCode());
	}

	@Test(expected = UserNotFoundException.class)
	public void testDeleteUserNotFound()
			throws UserNotFoundException {
		Mockito.doThrow(new UserNotFoundException(
				EXAMPLE_USER))
				.when(userSessionService)
				.deleteUser(ArgumentMatchers.eq(EXAMPLE_USER));

		userSessionController.deleteUser(EXAMPLE_USER);
	}

	@Test
	public void testDeleteUserSession()
			throws UserNotFoundException,
			SessionNotFoundException {
		Mockito.doNothing()
				.when(userSessionService)
				.deleteUserSession(	ArgumentMatchers.eq(EXAMPLE_USER),
									ArgumentMatchers.eq(EXAMPLE_SESSION));

		final ResponseEntity<String> response = userSessionController.deleteUserSession(EXAMPLE_USER,
																						EXAMPLE_SESSION);

		Assert.assertEquals(HttpStatus.OK,
							response.getStatusCode());
	}

	@Test(expected = UserNotFoundException.class)
	public void testDeleteUserSessionUserNotFound()
			throws UserNotFoundException,
			SessionNotFoundException {
		Mockito.doThrow(new UserNotFoundException(
				EXAMPLE_USER))
				.when(userSessionService)
				.deleteUserSession(	ArgumentMatchers.eq(EXAMPLE_USER),
									ArgumentMatchers.eq(EXAMPLE_SESSION));

		userSessionController.deleteUserSession(EXAMPLE_USER,
												EXAMPLE_SESSION);
	}

	@Test(expected = SessionNotFoundException.class)
	public void testDeleteUserSessionNotFound()
			throws UserNotFoundException,
			SessionNotFoundException {
		Mockito.doThrow(new SessionNotFoundException(
				EXAMPLE_USER,
				EXAMPLE_SESSION))
				.when(userSessionService)
				.deleteUserSession(	ArgumentMatchers.eq(EXAMPLE_USER),
									ArgumentMatchers.eq(EXAMPLE_SESSION));

		userSessionController.deleteUserSession(EXAMPLE_USER,
												EXAMPLE_SESSION);
	}

	@Test
	public void testRenameSessionId()
			throws UserNotFoundException,
			SessionExistsException,
			URISyntaxException,
			SessionNotFoundException {
		userSessionController.createUserSession(EXAMPLE_USER,
												EXAMPLE_SESSION);

		Mockito.when(userSessionService.renameSessionForUser(	EXAMPLE_USER,
																EXAMPLE_SESSION,
																EXAMPLE_SESSION2))
				.thenReturn(EXAMPLE_SESSION2);

		final ResponseEntity<String> response = userSessionController.renameUserSessionID(	EXAMPLE_USER,
																							EXAMPLE_SESSION,
																							EXAMPLE_SESSION2);
		Assert.assertEquals(EXAMPLE_SESSION2,
							response.getBody());
	}

	@Test(expected = SessionNotFoundException.class)
	public void testRenameSessionIdSessionNotFound()
			throws UserNotFoundException,
			SessionExistsException,
			URISyntaxException,
			SessionNotFoundException {
		userSessionController.createUserSession(EXAMPLE_USER,
												EXAMPLE_SESSION);

		Mockito.doThrow(new SessionNotFoundException(
				EXAMPLE_USER,
				EXAMPLE_SESSION))
				.when(userSessionService)
				.renameSessionForUser(	ArgumentMatchers.eq(EXAMPLE_USER),
										ArgumentMatchers.eq(EXAMPLE_SESSION),
										ArgumentMatchers.eq(EXAMPLE_SESSION2));

		userSessionController.renameUserSessionID(	EXAMPLE_USER,
													EXAMPLE_SESSION,
													EXAMPLE_SESSION2);

	}

	@Test
	public void testUpdateUser()
			throws UserNotFoundException,
			UserPreferencesProcessingException {
		Mockito.when(userSessionService.updateUser(	Mockito.eq(EXAMPLE_USER),
													Mockito.any()))
				.thenReturn(EXAMPLE_USER);

		final ResponseEntity<String> response = userSessionController.updateUser(	EXAMPLE_USER,
																					EXAMPLE_JSON);

		Assert.assertNotNull(response);
		Assert.assertEquals(HttpStatus.OK,
							response.getStatusCode());
		Assert.assertEquals(EXAMPLE_USER,
							response.getBody());
	}

	@Test
	public void testUpdateUserBeansException()
			throws UserNotFoundException,
			UserPreferencesProcessingException {
		Mockito.when(userSessionService.updateUser(	Mockito.eq(EXAMPLE_USER),
													Mockito.any()))
				.thenThrow(new BeanCreationException(
						"error"));

		final ResponseEntity<String> response = userSessionController.updateUser(	EXAMPLE_USER,
																					EXAMPLE_JSON);

		Assert.assertNotNull(response);
		Assert.assertEquals(HttpStatus.BAD_REQUEST,
							response.getStatusCode());
		Assert.assertEquals("error",
							response.getBody());
	}

	@Test(expected = UserPreferencesProcessingException.class)
	public void testUpdateUserUserPreferencesProcessingException()
			throws UserNotFoundException,
			UserPreferencesProcessingException {
		Mockito.when(userSessionService.updateUser(	Mockito.eq(EXAMPLE_USER),
													Mockito.any()))
				.thenThrow(new UserPreferencesProcessingException(
						"error"));

		userSessionController.updateUser(	EXAMPLE_USER,
											EXAMPLE_JSON);
	}

	@Test
	public void testGetUserPreferences()
			throws UserNotFoundException,
			UserPreferencesProcessingException {
		Mockito.when(userSessionService.getUserPreferences(Mockito.eq(EXAMPLE_USER)))
				.thenReturn(EXAMPLE_JSON);

		final ResponseEntity<UserPreferences> response = userSessionController.getUserPreferences(EXAMPLE_USER);

		Assert.assertNotNull(response);
		Assert.assertEquals(HttpStatus.OK,
							response.getStatusCode());
		Assert.assertNotNull(response.getBody());
	}
}
