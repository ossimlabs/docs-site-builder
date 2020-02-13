package com.maxar.user.service;

import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import com.maxar.user.model.UserPreferences;
import com.maxar.user.model.UserSessions;
import com.maxar.user.exception.SessionExistsException;
import com.maxar.user.exception.SessionNotFoundException;
import com.maxar.user.exception.UserExistsException;
import com.maxar.user.exception.UserNotFoundException;
import com.maxar.user.exception.UserPreferencesProcessingException;

@AutoConfigureTestDatabase
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@TestPropertySource(locations = "classpath:test-user.properties")
public class UserServiceTest
{
	@Autowired
	private UserService userSessionService;

	private static final String EXAMPLE_USER = "user0";

	private static final UserPreferences EXAMPLE_JSON = new UserPreferences();

	private static final String EXAMPLE_SESSION = "session0";
	private static final String EXAMPLE_SESSION2 = "session1";

	@Before
	public void setUp()
			throws UserExistsException,
			UserPreferencesProcessingException {
		final String savedUsername = userSessionService.createUser(	EXAMPLE_USER,
																	EXAMPLE_JSON);

		Assert.assertEquals(EXAMPLE_USER,
							savedUsername);
	}

	@After
	public void tearDown() {
		try {
			userSessionService.deleteUser(EXAMPLE_USER);
		}
		catch (final UserNotFoundException ignored) {}
	}

	@Test
	public void testCreateUser() {
		Assert.assertTrue(true);
	}

	@Test(expected = UserExistsException.class)
	public void testCreateUserExists()
			throws UserExistsException,
			UserPreferencesProcessingException {
		userSessionService.createUser(	EXAMPLE_USER,
										EXAMPLE_JSON);
	}

	@Test
	public void testCreateUserSession()
			throws UserNotFoundException,
			SessionExistsException {
		final String savedSession = userSessionService.createUserSession(	EXAMPLE_USER,
																			EXAMPLE_SESSION);

		Assert.assertEquals(EXAMPLE_SESSION,
							savedSession);
	}

	@Test(expected = UserNotFoundException.class)
	public void testCreateUserSessionUserNotFound()
			throws UserNotFoundException,
			SessionExistsException {
		userSessionService.createUserSession(	"invalid",
												EXAMPLE_SESSION);
	}

	@Test(expected = SessionExistsException.class)
	public void testCreateUserSessionExists()
			throws UserNotFoundException,
			SessionExistsException {
		userSessionService.createUserSession(	EXAMPLE_USER,
												EXAMPLE_SESSION);

		userSessionService.createUserSession(	EXAMPLE_USER,
												EXAMPLE_SESSION);
	}

	@Test
	public void testGetUsers() {
		final List<String> users = userSessionService.getUsers();

		Assert.assertEquals(1,
							users.size());
		Assert.assertEquals(EXAMPLE_USER,
							users.get(0));
	}

	@Test
	public void testGetSessionsForUser()
			throws UserNotFoundException,
			SessionExistsException {
		userSessionService.createUserSession(	EXAMPLE_USER,
												EXAMPLE_SESSION);

		final UserSessions userSessions = userSessionService.getSessionsForUser(EXAMPLE_USER);

		Assert.assertNotNull(userSessions);
		Assert.assertEquals(EXAMPLE_USER,
							userSessions.getUser());
		Assert.assertEquals(1,
							userSessions.getSessionIds()
									.size());
		Assert.assertTrue(userSessions.getSessionIds()
				.contains(EXAMPLE_SESSION));
	}

	@Test
	public void testGetSessionsForUserEmpty()
			throws UserNotFoundException {
		final UserSessions userSessions = userSessionService.getSessionsForUser(EXAMPLE_USER);

		Assert.assertNotNull(userSessions);
		Assert.assertEquals(EXAMPLE_USER,
							userSessions.getUser());
		Assert.assertTrue(userSessions.getSessionIds()
				.isEmpty());
	}

	@Test(expected = UserNotFoundException.class)
	public void testGetSessionsForUserNotFound()
			throws UserNotFoundException {
		userSessionService.getSessionsForUser("invalid");
	}

	@Test
	public void testDeleteUser()
			throws UserNotFoundException {
		userSessionService.deleteUser(EXAMPLE_USER);

		Assert.assertTrue(true);
	}

	@Test
	public void testDeleteUserWithSession()
			throws UserNotFoundException,
			SessionExistsException {
		userSessionService.createUserSession(	EXAMPLE_USER,
												EXAMPLE_SESSION);

		userSessionService.deleteUser(EXAMPLE_USER);

		Assert.assertTrue(true);
	}

	@Test(expected = UserNotFoundException.class)
	public void testDeleteUserNotFound()
			throws UserNotFoundException {
		userSessionService.deleteUser("invalid");
	}

	@Test
	public void testDeleteUserSession()
			throws UserNotFoundException,
			SessionExistsException,
			SessionNotFoundException {
		userSessionService.createUserSession(	EXAMPLE_USER,
												EXAMPLE_SESSION);

		userSessionService.deleteUserSession(	EXAMPLE_USER,
												EXAMPLE_SESSION);

		Assert.assertTrue(true);
	}

	@Test(expected = SessionNotFoundException.class)
	public void testDeleteUserSessionNotFound()
			throws UserNotFoundException,
			SessionNotFoundException {
		userSessionService.deleteUserSession(	EXAMPLE_USER,
												EXAMPLE_SESSION);
	}

	@Test(expected = UserNotFoundException.class)
	public void testDeleteUserSessionUserNotFound()
			throws UserNotFoundException,
			SessionNotFoundException {
		userSessionService.deleteUserSession(	"invalid",
												EXAMPLE_SESSION);
	}

	@Test
	public void testRenameSessionId()
			throws UserNotFoundException,
			SessionNotFoundException,
			SessionExistsException {

		userSessionService.createUserSession(	EXAMPLE_USER,
												EXAMPLE_SESSION);
		String newSessionId = userSessionService.renameSessionForUser(	EXAMPLE_USER,
																		EXAMPLE_SESSION,
																		EXAMPLE_SESSION2);
		Assert.assertEquals(EXAMPLE_SESSION2,
							newSessionId);
	}

	@Test(expected = SessionNotFoundException.class)
	public void testRenameSessionIdSessionNotFound()
			throws UserNotFoundException,
			SessionNotFoundException,
			SessionExistsException {

		userSessionService.createUserSession(	EXAMPLE_USER,
												EXAMPLE_SESSION);
		userSessionService.renameSessionForUser(EXAMPLE_USER,
												EXAMPLE_SESSION2,
												EXAMPLE_SESSION);

	}

	@Test(expected = SessionExistsException.class)
	public void testRenameSessionIdNewSessionExists()
			throws UserNotFoundException,
			SessionNotFoundException,
			SessionExistsException {

		userSessionService.createUserSession(	EXAMPLE_USER,
												EXAMPLE_SESSION);
		userSessionService.createUserSession(	EXAMPLE_USER,
												EXAMPLE_SESSION2);
		userSessionService.renameSessionForUser(EXAMPLE_USER,
												EXAMPLE_SESSION,
												EXAMPLE_SESSION2);
	}

	@Test
	public void testUpdateUser()
			throws UserNotFoundException,
			UserPreferencesProcessingException {
		final String updatedUser = userSessionService.updateUser(	EXAMPLE_USER,
																	EXAMPLE_JSON);

		Assert.assertEquals(EXAMPLE_USER,
							updatedUser);
	}

	@Test(expected = UserNotFoundException.class)
	public void testUpdateUserUserNotFound()
			throws UserNotFoundException,
			UserPreferencesProcessingException {
		userSessionService.updateUser(	"does not exist",
										EXAMPLE_JSON);
	}

	@Test
	public void testGetUserPreferences()
			throws UserNotFoundException,
			UserPreferencesProcessingException {
		final UserPreferences userPreferences = userSessionService.getUserPreferences(EXAMPLE_USER);

		Assert.assertNotNull(userPreferences);
	}
}
