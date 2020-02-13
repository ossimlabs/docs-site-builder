package com.maxar.geometric.intersection.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Component;

import com.maxar.geometric.intersection.exception.AreaOfInterestIdDoesNotExistException;
import com.maxar.geometric.intersection.exception.InvalidAreaOfInterestException;
import com.maxar.geometric.intersection.model.AreaOfInterest;
import com.maxar.geometric.intersection.repository.AreaOfInterestRepository;
import com.maxar.geometric.intersection.utils.AreaOfInterestConversions;

/**
 * Handles database interaction as well as the logic for each endpoint of the
 * Geometric Intersection service.
 */
@Component
public class GeometricIntersectionService
{
	@Autowired
	private AreaOfInterestRepository areaOfInterestRepository;

	/**
	 * Create a new geometry area of interest.
	 *
	 * @param areaOfInterest
	 *            The area of interest to create.
	 * @return The ID of the newly stored area of interest.
	 */
	public String createGeometry(
			final AreaOfInterest areaOfInterest ) {
		return Optional.ofNullable(areaOfInterest)
				.map(AreaOfInterestConversions::modelToEntity)
				.map(areaOfInterestRepository::save)
				.map(com.maxar.geometric.intersection.entity.AreaOfInterest::getId)
				.orElseThrow(InvalidAreaOfInterestException::new);
	}

	/**
	 * Gets all area of interest IDs.
	 *
	 * @return The list of IDs for all areas of interest.
	 */
	public List<String> getGeometries() {
		return areaOfInterestRepository.findAll()
				.stream()
				.map(com.maxar.geometric.intersection.entity.AreaOfInterest::getId)
				.collect(Collectors.toList());
	}

	/**
	 * Get an area of interest by its ID.
	 *
	 * @param id
	 *            The ID of the area of interest to get.
	 * @return The area of interest with the given ID.
	 * @throws AreaOfInterestIdDoesNotExistException
	 *             Thrown if there is no area of interest with the given ID in the
	 *             database.
	 */
	public AreaOfInterest getGeometryById(
			final String id )
			throws AreaOfInterestIdDoesNotExistException {
		return areaOfInterestRepository.findById(id)
				.map(AreaOfInterestConversions::entityToModel)
				.orElseThrow(() -> new AreaOfInterestIdDoesNotExistException(
						id));
	}

	/**
	 * Searches for intersecting areas of interest in the database.
	 *
	 * @param geometryWkt
	 *            The input geometry in well-known text (WKT).
	 * @return A list of all areas of interest that have geometries intersecting the
	 *         input geometry.
	 */
	public List<AreaOfInterest> getIntersectingGeometries(
			final String geometryWkt ) {
		try {
			final WKTReader reader = new WKTReader();
			final Geometry geometry = reader.read(geometryWkt);

			return areaOfInterestRepository.findByIntersectingGeometry(geometry)
					.stream()
					.map(AreaOfInterestConversions::entityToModel)
					.collect(Collectors.toList());
		}
		catch (final ParseException e) {
			throw new InvalidAreaOfInterestException(
					e.getMessage());
		}
	}

	/**
	 * Delete an area of interest by its ID.
	 *
	 * @param id
	 *            The ID of the area of interest to delete.
	 * @throws AreaOfInterestIdDoesNotExistException
	 *             Thrown if there is no area of interest with the given ID in the
	 *             database.
	 */
	public void deleteGeometryById(
			final String id )
			throws AreaOfInterestIdDoesNotExistException {
		try {
			areaOfInterestRepository.deleteById(id);
		}
		catch (final EmptyResultDataAccessException e) {
			throw new AreaOfInterestIdDoesNotExistException(
					id);
		}
	}
}
