package com.maxar.user.service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.maxar.user.model.UserPreferences;
import com.maxar.user.model.UserSessions;
import com.maxar.user.entity.Session;
import com.maxar.user.entity.User;
import com.maxar.user.repository.UserRepository;
import com.maxar.user.exception.SessionExistsException;
import com.maxar.user.exception.SessionNotFoundException;
import com.maxar.user.exception.UserExistsException;
import com.maxar.user.exception.UserNotFoundException;
import com.maxar.user.exception.UserPreferencesProcessingException;

/**
 * Handles management of users and their connections to sessions.
 */
@Component
public class UserService
{
	@Autowired
	private UserRepository userRepository;

	/**
	 * Add a new user to the database.
	 *
	 * @param username
	 *            The ID of the user to add.
	 * @param preferences
	 *            User cesium UI preferences in JSON format
	 * @return The ID of the added user.
	 * @throws UserExistsException
	 *             Thrown if there is already a user with the specified user ID.
	 * @throws UserPreferencesProcessingException
	 *             Thrown if there was an issue processing the JSON preferences.
	 */
	public String createUser(
			final String username,
			final UserPreferences preferences )
			throws UserExistsException,
			UserPreferencesProcessingException {
		if (userRepository.findById(username)
				.isPresent()) {
			throw new UserExistsException(
					username);
		}

		return saveUser(username,
						preferences);
	}

	/**
	 * Update user.
	 *
	 * @param username
	 *            The ID of the user to update.
	 * @param preferences
	 *            User cesium UI preferences in JSON format
	 * @return The ID of the added user.
	 * @throws UserNotFoundException
	 *             Thrown if user with the specified user ID does not exist.
	 * @throws UserPreferencesProcessingException
	 *             Thrown if there was an issue processing the JSON preferences.
	 */
	public String updateUser(
			final String username,
			final UserPreferences preferences )
			throws UserNotFoundException,
			UserPreferencesProcessingException {
		if (!userRepository.findById(username)
				.isPresent()) {
			throw new UserNotFoundException(
					username);
		}

		return saveUser(username,
						preferences);
	}

	private String saveUser(
			final String username,
			final UserPreferences preferences )
			throws UserPreferencesProcessingException {
		final ObjectMapper mapper = new ObjectMapper();

		String preferencesString;

		try {
			preferencesString = mapper.writeValueAsString(preferences);
		}
		catch (final JsonProcessingException e) {
			throw new UserPreferencesProcessingException(
					username);
		}

		final User user = new User();
		user.setUsername(username);
		user.setPreferences(preferencesString);

		final User savedUser = userRepository.save(user);

		return savedUser.getUsername();
	}

	/**
	 * Add a new or existing session to a user.
	 *
	 * This adds a connection from a user to a session. There may be a session with
	 * the ID already in the database. If no session in the database contains the ID
	 * specified, then a new session will be added to the database.
	 *
	 * @param username
	 *            The ID of the user to add the session to.
	 * @param sessionId
	 *            The ID of the session to add to the user's session list.
	 * @return The ID of the added session.
	 * @throws UserNotFoundException
	 *             Thrown if there is no user with the specified user ID.
	 * @throws SessionExistsException
	 *             Thrown if there is already a session with the specified ID for
	 *             the user specified.
	 */
	@Transactional
	public String createUserSession(
			final String username,
			final String sessionId )
			throws UserNotFoundException,
			SessionExistsException {
		final User user = userRepository.findById(username)
				.orElseThrow(() -> new UserNotFoundException(
						username));

		final Session session = new Session();
		session.setId(sessionId);
		session.setUuid(UUID.randomUUID());

		if (user.addSession(session)) {
			userRepository.save(user);

			return session.getId();
		}
		else {
			throw new SessionExistsException(
					username,
					sessionId);
		}
	}

	/**
	 * Gets a list of all user IDs.
	 *
	 * @return A list containing all user IDs.
	 */
	public List<String> getUsers() {
		return userRepository.findAll()
				.stream()
				.map(User::getUsername)
				.collect(Collectors.toList());
	}

	/**
	 * Gets all session IDs associated with a user.
	 *
	 * @param username
	 *            The user ID to get sessions for.
	 * @return A list of session IDs that are associated with the specified user.
	 * @throws UserNotFoundException
	 *             Thrown if there is no user with the specified user ID.
	 */
	@Transactional
	public UserSessions getSessionsForUser(
			final String username )
			throws UserNotFoundException {
		final User user = userRepository.findById(username)
				.orElseThrow(() -> new UserNotFoundException(
						username));

		final UserSessions userSessions = new UserSessions();
		userSessions.setUser(user.getUsername());
		userSessions.setSessionIds(user.getSessions()
				.stream()
				.map(Session::getId)
				.collect(Collectors.toSet()));

		return userSessions;
	}

	/**
	 * Rename a session ID for a user with a new ID.
	 *
	 * @param username
	 *            The user ID to get sessions for.
	 * @param currentSessionId
	 *            The session ID to be renamed
	 * @param newSessionId
	 *            The new session ID the current session ID should be changed to
	 * @throws UserNotFoundException
	 *             Thrown if there is no user with the specified user ID.
	 * @throws SessionNotFoundException
	 *             Thrown if there is no session with the specified session ID.
	 */
	@Transactional
	public String renameSessionForUser(
			final String username,
			final String currentSessionId,
			final String newSessionId )
			throws UserNotFoundException,
			SessionNotFoundException,
			SessionExistsException {
		final User user = userRepository.findById(username)
				.orElseThrow(() -> new UserNotFoundException(
						username));

		Set<Session> sessions = user.getSessions();
		UUID sessionUUID = null;
		for (Session session : sessions) {
			if (session.getId()
					.equals(currentSessionId)) {
				sessionUUID = session.getUuid();
				break;
			}
		}

		final Session oldSession = new Session();
		oldSession.setId(currentSessionId);
		Set<User> usersOldSession = new HashSet<>();
		usersOldSession.add(user);
		oldSession.setUsers(usersOldSession);
		oldSession.setUuid(sessionUUID);

		if (!user.removeSession(oldSession)) {
			throw new SessionNotFoundException(
					username,
					currentSessionId);
		}

		final Session newSession = new Session();
		newSession.setId(newSessionId);
		newSession.setUuid(UUID.randomUUID());
		if (user.addSession(newSession)) {
			userRepository.save(user);

			return newSessionId;
		}
		else {
			throw new SessionExistsException(
					username,
					newSessionId);
		}

	}

	/**
	 * Delete a user from the database.
	 *
	 * This will delete a user from the database, as well as any connections from a
	 * user to its sessions. However, the session themselves will not be deleted
	 * from the database, as they may be shared between users, and may persist even
	 * if no user is actively using them.
	 *
	 * @param username
	 *            The ID of the user to delete.
	 * @throws UserNotFoundException
	 *             Thrown if there is no user with the specified user ID.
	 */
	public void deleteUser(
			final String username )
			throws UserNotFoundException {
		final User user = userRepository.findById(username)
				.orElseThrow(() -> new UserNotFoundException(
						username));

		userRepository.delete(user);
	}

	/**
	 * Gets cesium UI preferences associated with a user.
	 *
	 * @param username
	 *            The user ID to get preferences for.
	 * @return UserPreferences associated with the specified user.
	 * @throws UserNotFoundException
	 *             Thrown if there is no user with the specified user ID.
	 * @throws UserPreferencesProcessingException
	 *             Thrown if there was an issue processing the JSON preferences.
	 */
	public UserPreferences getUserPreferences(
			final String username )
			throws UserNotFoundException,
			UserPreferencesProcessingException {
		final User user = userRepository.findById(username)
				.orElseThrow(() -> new UserNotFoundException(
						username));

		UserPreferences preferences;

		try {
			final ObjectMapper mapper = new ObjectMapper();

			preferences = mapper.readValue(	user.getPreferences(),
											UserPreferences.class);
		}
		catch (final Exception e) {
			throw new UserPreferencesProcessingException(
					username);
		}

		return preferences;
	}

	/**
	 * Delete a session from a user.
	 *
	 * This will delete the connection from a user to a session, but will not delete
	 * the session itself. Sessions may be shared between users, and may persist
	 * even after no users are actively using them.
	 *
	 * @param username
	 *            The ID of the user to delete the session for.
	 * @param sessionId
	 *            The ID of the session to delete for the user.
	 * @throws UserNotFoundException
	 *             Thrown if there is no user with the specified user ID.
	 * @throws SessionNotFoundException
	 *             Thrown if there is no session with the specified session ID.
	 */
	@Transactional
	public void deleteUserSession(
			final String username,
			final String sessionId )
			throws UserNotFoundException,
			SessionNotFoundException {
		final User user = userRepository.findById(username)
				.orElseThrow(() -> new UserNotFoundException(
						username));

		final Session session = user.getSessions()
				.stream()
				.filter(s -> s.getId()
						.equals(sessionId))
				.findFirst()
				.orElseThrow(() -> new SessionNotFoundException(
						username,
						sessionId));

		if (!user.removeSession(session)) {
			throw new SessionNotFoundException(
					username,
					sessionId);
		}

		userRepository.save(user);
	}
}
