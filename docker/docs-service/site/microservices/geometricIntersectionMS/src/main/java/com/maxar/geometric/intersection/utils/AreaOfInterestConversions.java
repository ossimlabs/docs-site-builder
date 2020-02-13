package com.maxar.geometric.intersection.utils;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.io.WKTReader;

import com.maxar.geometric.intersection.exception.InvalidAreaOfInterestException;
import com.maxar.geometric.intersection.model.AreaOfInterest;

public class AreaOfInterestConversions
{
	private AreaOfInterestConversions() {}

	private static GeometryFactory geometryFactory = new GeometryFactory();

	/**
	 * Convert a model area of interest to an entity area of interest.
	 *
	 * @param modelAreaOfInterest
	 *            The area of interest in POJO form.
	 * @return The area of interest in the form stored in the database.
	 */
	public static com.maxar.geometric.intersection.entity.AreaOfInterest modelToEntity(
			final AreaOfInterest modelAreaOfInterest ) {
		try {
			final WKTReader reader = new WKTReader();
			final Geometry geometry = reader.read(modelAreaOfInterest.getGeometryWkt());

			final Polygon polygon = polygonFromGeometry(geometry);
			final com.maxar.geometric.intersection.entity.AreaOfInterest entityAreaOfInterest = new com.maxar.geometric.intersection.entity.AreaOfInterest();
			entityAreaOfInterest.setId(modelAreaOfInterest.getId());
			entityAreaOfInterest.setGeometry(polygon);

			return entityAreaOfInterest;
		}
		catch (final Exception e) {
			// Throwing a subclass of a RuntimeException is unchecked, so we can use this
			// function in a stream map operation.
			throw new InvalidAreaOfInterestException(
					e.getMessage());
		}
	}

	/**
	 * Convert an entity area of interest to a model area of interest.
	 *
	 * @param entityAreaOfInterest
	 *            The area of interest in the form stored in the database.
	 * @return The area of interest in POJO form.
	 */
	public static AreaOfInterest entityToModel(
			final com.maxar.geometric.intersection.entity.AreaOfInterest entityAreaOfInterest ) {
		final AreaOfInterest modelAreaOfInterest = new AreaOfInterest();
		modelAreaOfInterest.setId(entityAreaOfInterest.getId());
		if (entityAreaOfInterest.getGeometry() != null) {
			modelAreaOfInterest.setGeometryWkt(entityAreaOfInterest.getGeometry()
					.toText());
		}

		return modelAreaOfInterest;
	}

	private static Polygon polygonFromGeometry(
			final Geometry geometry ) {
		if (geometry.getGeometryType()
				.equalsIgnoreCase("polygon")) {
			return (Polygon) geometry;
		}
		else if (geometry.getGeometryType()
				.equalsIgnoreCase("linearring")) {
			return geometryFactory.createPolygon((LinearRing) geometry);
		}
		else {
			throw new InvalidAreaOfInterestException(
					"Invalid geometry type: \"" + geometry.getGeometryType() + "\"");
		}
	}
}
