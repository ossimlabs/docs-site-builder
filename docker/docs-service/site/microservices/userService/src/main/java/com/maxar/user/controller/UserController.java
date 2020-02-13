package com.maxar.user.controller;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.maxar.user.model.UserPreferences;
import com.maxar.user.model.UserSessions;
import com.maxar.user.exception.SessionExistsException;
import com.maxar.user.exception.SessionNotFoundException;
import com.maxar.user.exception.UserExistsException;
import com.maxar.user.exception.UserNotFoundException;
import com.maxar.user.exception.UserPreferencesProcessingException;
import com.maxar.user.service.UserService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Api
@RequestMapping("/user")
@RestController
public class UserController
{
	@Autowired
	private UserService userService;

	@ApiOperation("Adds a new user")
	@ApiResponses({
		@ApiResponse(code = 201, message = "The user was created successfully"),
		@ApiResponse(code = 409, message = "A user with the specified username already exists")
	})
	@PostMapping(value = "/{user}")
	public ResponseEntity<String> createUser(
			@PathVariable(name = "user")
			final String user,
			@ApiParam(value = "The user preferences JSON document")
			@RequestBody
			final UserPreferences preferences )
			throws UserExistsException,
			URISyntaxException,
			UserPreferencesProcessingException {
		final String userId = userService.createUser(	user,
														preferences);

		final URI userUri = new URI(
				"/user/" + userId);

		return ResponseEntity.created(userUri)
				.build();
	}

	@ApiOperation("Adds a new session to a user")
	@ApiResponses({
		@ApiResponse(code = 201, message = "The session was successfully added to the user"),
		@ApiResponse(code = 409, message = "The user already contains the specified session")
	})
	@PostMapping(value = "/{user}/session/{session}")
	public ResponseEntity<String> createUserSession(
			@PathVariable(name = "user")
			final String user,
			@PathVariable(name = "session")
			final String session )
			throws UserNotFoundException,
			SessionExistsException,
			URISyntaxException {
		final String sessionId = userService.createUserSession(	user,
																session);

		final URI userSessionUri = new URI(
				"/user/" + user + "/session/" + sessionId);

		return ResponseEntity.created(userSessionUri)
				.build();
	}

	@ApiOperation("Gets the list of users")
	@ApiResponses({
		@ApiResponse(code = 200, message = "The list of users was retrieved successfully")
	})
	@GetMapping(value = "")
	public ResponseEntity<List<String>> getUsers() {
		final List<String> users = userService.getUsers();

		return ResponseEntity.ok(users);
	}

	@ApiOperation("Gets the list of sessions for a user")
	@ApiResponses({
		@ApiResponse(code = 200, message = "The list of sessions for the user was retrieved successfully"),
		@ApiResponse(code = 404, message = "The user requested does not exist")
	})
	@GetMapping(value = "/{user}/session")
	public ResponseEntity<UserSessions> getSessionsForUser(
			@PathVariable(name = "user")
			final String user )
			throws UserNotFoundException {
		final UserSessions userSessions = userService.getSessionsForUser(user);

		return ResponseEntity.ok(userSessions);
	}

	@ApiOperation("Deletes the requested user")
	@ApiResponses({
		@ApiResponse(code = 200, message = "The user was deleted successfully"),
		@ApiResponse(code = 404, message = "The user requested does not exist")
	})
	@DeleteMapping(value = "/{user}")
	public ResponseEntity<String> deleteUser(
			@PathVariable(name = "user")
			final String user )
			throws UserNotFoundException {
		userService.deleteUser(user);

		return ResponseEntity.ok()
				.build();
	}

	@ApiOperation("Deletes the requested session from the user")
	@ApiResponses({
		@ApiResponse(code = 200, message = "The session was deleted successfully"),
		@ApiResponse(code = 404, message = "Either the user does not exist, or the session does not exist")
	})
	@DeleteMapping(value = "/{user}/session/{session}")
	public ResponseEntity<String> deleteUserSession(
			@PathVariable(name = "user")
			final String user,
			@PathVariable(name = "session")
			final String session )
			throws UserNotFoundException,
			SessionNotFoundException {
		userService.deleteUserSession(	user,
										session);

		return ResponseEntity.ok()
				.build();
	}

	@ApiOperation("Updates the specified user")
	@ApiResponses({
		@ApiResponse(code = 201, message = "The user was successfully updated"),
		@ApiResponse(code = 404, message = "The user does not exist")
	})
	@PutMapping(value = "/{user}")
	public ResponseEntity<String> updateUser(
			@ApiParam(value = "The user", example = "jsmith")
			@PathVariable(name = "user")
			final String user,
			@ApiParam(value = "The user preferences JSON document")
			@RequestBody
			final UserPreferences preferences )
			throws UserNotFoundException,
			UserPreferencesProcessingException {
		try {
			final String updatedUser = userService.updateUser(	user,
																preferences);

			return ResponseEntity.ok(updatedUser);
		}
		catch (final BeansException e) {
			return ResponseEntity.badRequest()
					.body(e.getMessage());
		}
	}

	@ApiOperation("Gets preferences for a user")
	@ApiResponses({
		@ApiResponse(code = 200, message = "Preferences for the user were retrieved successfully"),
		@ApiResponse(code = 404, message = "The user requested does not exist")
	})
	@GetMapping(value = "/{user}/preferences")
	public ResponseEntity<UserPreferences> getUserPreferences(
			@ApiParam(value = "The user", example = "jsmith")
			@PathVariable(name = "user")
			final String user )
			throws UserNotFoundException,
			UserPreferencesProcessingException {
		final UserPreferences preferences = userService.getUserPreferences(user);

		return ResponseEntity.ok(preferences);
	}

	@ApiOperation("Rename user's existing session ID")
	@ApiResponses({
		@ApiResponse(code = 200, message = "User's session ID was renamed successfully"),
		@ApiResponse(code = 404, message = "Either the user does not exist, or the session does not exist")
	})
	@GetMapping(value = "/{user}/currentSessionId/{currentSessionId}/newSessionId/{newSessionId}")
	public ResponseEntity<String> renameUserSessionID(
			@PathVariable(name = "user")
			final String user,
			@PathVariable(name = "currentSessionId")
			final String currentSessionId,
			@PathVariable(name = "newSessionId")
			final String newSessionId )
			throws UserNotFoundException,
			SessionNotFoundException,
			SessionExistsException {
		userService.renameSessionForUser(	user,
											currentSessionId,
											newSessionId);
		return ResponseEntity.ok(newSessionId);
	}
}
