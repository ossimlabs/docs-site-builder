package com.maxar.alert.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Component;

import com.maxar.alert.model.Event;
import com.maxar.alert.repository.EventRepository;
import com.maxar.alert.exception.AlertIdDoesNotExistException;
import com.maxar.alert.exception.AlertIdExistsException;
import com.maxar.alert.exception.InvalidAlertException;
import com.maxar.alert.exception.InvalidRequestException;

@Component
public class AlertService
{
	@Autowired
	private EventRepository eventRepository;

	/**
	 * Create and save an alert in the database.
	 *
	 * @param event
	 *            The alert to save.
	 * @return The identifier of the alert.
	 * @throws AlertIdExistsException
	 *             Thrown if there is an existing alert with the same ID.
	 * @throws InvalidAlertException
	 *             Thrown if the alert to create is invalid.
	 */
	public String createAlert(
			final Event event )
			throws AlertIdExistsException,
			InvalidAlertException {
		final com.maxar.alert.entity.Event entityEvent = Optional.ofNullable(event)
				.filter(AlertService::eventIsValid)
				.map(com.maxar.alert.entity.Event::new)
				.orElseThrow(InvalidAlertException::new);

		if (eventRepository.existsById(entityEvent.getId())) {
			throw new AlertIdExistsException(
					entityEvent.getId());
		}

		return eventRepository.save(entityEvent)
				.getId();
	}

	/**
	 * Get all alert IDs.
	 *
	 * @return The list of IDs for all alerts.
	 */
	public List<String> getAllAlerts() {
		return eventRepository.findAll()
				.stream()
				.map(com.maxar.alert.entity.Event::getId)
				.collect(Collectors.toList());
	}

	/**
	 * Get an alert by its ID.
	 *
	 * @param id
	 *            The ID of the alert to get.
	 * @return The alert with the specified ID.
	 * @throws AlertIdDoesNotExistException
	 *             Thrown if there is no alert with the given ID in the database.
	 */
	public Event getAlertById(
			final String id )
			throws AlertIdDoesNotExistException {
		final com.maxar.alert.entity.Event event = eventRepository.findById(id)
				.orElseThrow(() -> new AlertIdDoesNotExistException(
						id));

		event.updateGeometryWkt();

		return event;
	}

	/**
	 * Get events that intersect a geometry, given by its well-known text (WKT).
	 *
	 * @param geometry
	 *            The WKT geometry to search for intersecting events.
	 * @return A list of events whose geometries intersect the specified geometry,
	 *         which may be empty.
	 * @throws InvalidRequestException
	 *             Thrown if the given wkt geometry is invalid wkt.
	 */
	public List<Event> getAlertsByGeometry(
			final String geometry )
			throws InvalidRequestException {
		final WKTReader reader = new WKTReader();
		Geometry geom;
		try {
			geom = reader.read(geometry);
		}
		catch (final ParseException e) {
			throw new InvalidRequestException(
					"Cannot parse WKT string: " + geometry + ", " + e.getMessage());
		}

		final List<com.maxar.alert.entity.Event> events = eventRepository.findByGeometry(geom);

		events.forEach(com.maxar.alert.entity.Event::updateGeometryWkt);

		return events.stream()
				.map(Event.class::cast)
				.collect(Collectors.toList());
	}

	/**
	 * Delete an alert by its ID.
	 *
	 * @param id
	 *            The ID of the alert to delete.
	 * @throws AlertIdDoesNotExistException
	 *             Thrown if there is no alert with the given ID in the database.
	 */
	public void deleteAlertById(
			final String id )
			throws AlertIdDoesNotExistException {
		try {
			eventRepository.deleteById(id);
		}
		catch (final EmptyResultDataAccessException e) {
			throw new AlertIdDoesNotExistException(
					id);
		}
	}

	/**
	 * @param event
	 *            The event to check.
	 * @return True if the event'd ID, start time, end time, and geometry WKT are
	 *         all non-null.
	 */
	private static boolean eventIsValid(
			final Event event ) {
		return event.getId() != null && event.getStartTime() != null && event.getEndTime() != null
				&& event.getGeometryWkt() != null;
	}
}
