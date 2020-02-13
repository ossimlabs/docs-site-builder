package com.maxar.spaceobjectcatalog.service;

import java.util.List;
import java.util.stream.Collectors;

import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import com.maxar.ephemeris.entity.Ephemeris;
import com.maxar.ephemeris.model.EphemerisModel;
import com.maxar.spaceobjectcatalog.model.SpaceObject;
import com.maxar.spaceobjectcatalog.repository.SpaceObjectCatalogRepository;

@Component
public class SpaceObjectCatalogService
{
	@Autowired
	private SpaceObjectCatalogRepository spaceObjectCatalogRepository;

	/**
	 * Get all ephemerides for a satellite in a paginated format.
	 *
	 * @param scn
	 *            The satellite catalog number (SCN).
	 * @param page
	 *            The zero indexed page number of the ephemerides.
	 * @param count
	 *            The number of ephemerides per page.
	 * @return The paginated ephemerides for the requested satellite.
	 */
	public SpaceObject getEphemerisPaginated(
			final Integer scn,
			final Integer page,
			final Integer count ) {
		final PageRequest pageRequest = PageRequest.of(	page,
														count);

		final List<EphemerisModel> ephemerides = spaceObjectCatalogRepository.findByScnOrderByEpochMillisDesc(	scn,
																												pageRequest)
				.stream()
				.map(Ephemeris::toModel)
				.collect(Collectors.toList());

		final SpaceObject spaceObject = new SpaceObject();
		spaceObject.setScn(scn);
		spaceObject.setEphemerides(ephemerides);

		return spaceObject;
	}

	/**
	 * Get the number of ephemerides for a satellite.
	 *
	 * @param scn
	 *            The satellite catalog number (SCN).
	 * @return The number of ephemerides for the requested satellite.
	 */
	public long getEphemerisCount(
			final Integer scn ) {
		return spaceObjectCatalogRepository.countByScn(scn);
	}

	/**
	 * Get ephemerides for a satellite in a date range.
	 *
	 * @param scn
	 *            The satellite catalog number (SCN).
	 * @param start
	 *            The start (inclusive) of the date range to search.
	 * @param end
	 *            The end (inclusive) of the date range to search.
	 * @return The ephemerides for the requested satellite in the requested date
	 *         range.
	 */
	public SpaceObject getEphemerisInDateRange(
			final Integer scn,
			final DateTime start,
			final DateTime end ) {
		final List<EphemerisModel> ephemerides = spaceObjectCatalogRepository
				.findByScnAndEpochMillisGreaterThanEqualAndEpochMillisLessThanEqualOrderByEpochMillisDesc(	scn,
																											start.getMillis(),
																											end.getMillis())
				.stream()
				.map(Ephemeris::toModel)
				.collect(Collectors.toList());

		final SpaceObject spaceObject = new SpaceObject();
		spaceObject.setScn(scn);
		spaceObject.setEphemerides(ephemerides);

		return spaceObject;
	}
}
