package com.maxar.geometric.intersection.utils;

import org.assertj.core.util.Arrays;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateSequence;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.impl.CoordinateArraySequence;

import com.maxar.geometric.intersection.exception.InvalidAreaOfInterestException;
import com.maxar.geometric.intersection.model.AreaOfInterest;

public class AreaOfInterestConversionsTest
{
	private GeometryFactory geometryFactory;

	@Before
	public void setUp() {
		geometryFactory = new GeometryFactory();
	}

	@Test
	public void testModelToEntityPolygon() {
		final AreaOfInterest modelAoi = new AreaOfInterest();
		modelAoi.setId("id0");
		modelAoi.setGeometryWkt("POLYGON ((-77.4232292175293 38.959759183009226, "
				+ "-77.42216706275939 38.959233596526964, -77.42136240005493 38.960459958921135, "
				+ "-77.42239236831665 38.96086039918754, -77.4232292175293 38.959759183009226))");

		final com.maxar.geometric.intersection.entity.AreaOfInterest entityAoi = AreaOfInterestConversions
				.modelToEntity(modelAoi);

		Assert.assertNotNull(entityAoi);
		Assert.assertNotNull(entityAoi.getId());
		Assert.assertNotNull(entityAoi.getGeometry());
		Assert.assertTrue(entityAoi.getGeometry()
				.isValid());
		Assert.assertEquals("Polygon",
							entityAoi.getGeometry()
									.getGeometryType());
	}

	@Test
	public void testModelToEntityLinearRing() {
		final AreaOfInterest modelAoi = new AreaOfInterest();
		modelAoi.setId("id0");
		modelAoi.setGeometryWkt("LINEARRING (-77.4232292175293 38.959759183009226, "
				+ "-77.42216706275939 38.959233596526964, -77.42136240005493 38.960459958921135, "
				+ "-77.42239236831665 38.96086039918754, -77.4232292175293 38.959759183009226)");

		final com.maxar.geometric.intersection.entity.AreaOfInterest entityAoi = AreaOfInterestConversions
				.modelToEntity(modelAoi);

		Assert.assertNotNull(entityAoi);
		Assert.assertNotNull(entityAoi.getId());
		Assert.assertNotNull(entityAoi.getGeometry());
		Assert.assertTrue(entityAoi.getGeometry()
				.isValid());
		Assert.assertEquals("Polygon",
							entityAoi.getGeometry()
									.getGeometryType());
	}

	@Test(expected = InvalidAreaOfInterestException.class)
	public void testModelToEntityBadWkt() {
		final AreaOfInterest modelAoi = new AreaOfInterest();
		modelAoi.setId("id0");
		modelAoi.setGeometryWkt("OLYGON ((-77.4232292175293 38.959759183009226, "
				+ "-77.42216706275939 38.959233596526964, -77.42136240005493 38.960459958921135, "
				+ "-77.42239236831665 38.96086039918754, -77.4232292175293 38.959759183009226))");

		AreaOfInterestConversions.modelToEntity(modelAoi);
	}

	@Test(expected = InvalidAreaOfInterestException.class)
	public void testModelToEntityInvalidGeometryType() {
		final AreaOfInterest modelAoi = new AreaOfInterest();
		modelAoi.setId("id0");
		modelAoi.setGeometryWkt("LINESTRING (-77.4232292175293 38.959759183009226, "
				+ "-77.42216706275939 38.959233596526964, -77.42136240005493 38.960459958921135, "
				+ "-77.42239236831665 38.96086039918754 )");

		AreaOfInterestConversions.modelToEntity(modelAoi);
	}

	@Test
	public void testEntityToModel() {
		final com.maxar.geometric.intersection.entity.AreaOfInterest entityAoi = new com.maxar.geometric.intersection.entity.AreaOfInterest();
		entityAoi.setId("id0");
		final Coordinate[] coordinates = Arrays.array(	new Coordinate(
				-77.4232292175293,
				38.959759183009226),
														new Coordinate(
																-77.42216706275939,
																38.959233596526964),
														new Coordinate(
																-77.42136240005493,
																38.960459958921135),
														new Coordinate(
																-77.42239236831665,
																38.96086039918754),
														new Coordinate(
																-77.4232292175293,
																38.959759183009226));

		final CoordinateSequence points = new CoordinateArraySequence(
				coordinates);

		final LinearRing shell = new LinearRing(
				points,
				geometryFactory);

		final Polygon geometry = new Polygon(
				shell,
				null,
				geometryFactory);
		entityAoi.setGeometry(geometry);

		final AreaOfInterest modelAoi = AreaOfInterestConversions.entityToModel(entityAoi);

		Assert.assertNotNull(modelAoi);
		Assert.assertNotNull(modelAoi.getGeometryWkt());
		Assert.assertEquals("POLYGON ((-77.4232292175293 38.959759183009226, "
				+ "-77.42216706275939 38.959233596526964, -77.42136240005493 38.960459958921135, "
				+ "-77.42239236831665 38.96086039918754, -77.4232292175293 38.959759183009226))",
							modelAoi.getGeometryWkt());
	}

	@Test
	public void testEntityToModelNullGeometry() {
		final com.maxar.geometric.intersection.entity.AreaOfInterest entityAoi = new com.maxar.geometric.intersection.entity.AreaOfInterest();
		entityAoi.setId("id0");
		entityAoi.setGeometry(null);

		final AreaOfInterest modelAoi = AreaOfInterestConversions.entityToModel(entityAoi);

		Assert.assertNotNull(modelAoi);
		Assert.assertNull(modelAoi.getGeometryWkt());
	}
}
