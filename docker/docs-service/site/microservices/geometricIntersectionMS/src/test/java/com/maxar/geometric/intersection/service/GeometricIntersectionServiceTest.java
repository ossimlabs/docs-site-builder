package com.maxar.geometric.intersection.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import com.maxar.geometric.intersection.exception.AreaOfInterestIdDoesNotExistException;
import com.maxar.geometric.intersection.exception.InvalidAreaOfInterestException;
import com.maxar.geometric.intersection.model.AreaOfInterest;
import com.maxar.geometric.intersection.repository.AreaOfInterestRepository;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@TestPropertySource(locations = "classpath:test-geometricintersectionms.properties")
public class GeometricIntersectionServiceTest
{
	private static final List<String> GEOMETRY_WKT_LIST = Arrays
			.asList("POLYGON ((" + "-77.4232292175293 38.959759183009226, -77.42216706275939 38.959233596526964, "
					+ "-77.42136240005493 38.960459958921135, -77.42239236831665 38.96086039918754, "
					+ "-77.4232292175293 38.959759183009226))",
					"POLYGON ((-77.42194175720215 38.95917519778828, -77.42098689079285 38.95885817522417, "
							+ "-77.42018222808838 38.96000946091624, -77.42107272148132 38.960334820873754, "
							+ "-77.42194175720215 38.95917519778828))",
					"POLYGON ((-77.42231726646423 38.96094382395817, -77.4213194847107 38.96051001407818, "
							+ "-77.42039680480957 38.96167795770428, -77.42140531539917 38.96211176043332, "
							+ "-77.42231726646423 38.96094382395817))",
					"POLYGON ((-77.42097616195679 38.960409903728696, -77.42011785507202 38.96012625697033, "
							+ "-77.41925954818726 38.96114404300697, -77.4200963973999 38.96151110979358, "
							+ "-77.42097616195679 38.960409903728696))");

	@Autowired
	private GeometricIntersectionService geometricIntersectionService;

	@Autowired
	private AreaOfInterestRepository areaOfInterestRepository;

	private List<String> idsCreated = new ArrayList<>();

	@Before
	public void setUp() {
		idsCreated.clear();
	}

	@After
	public void tearDown() {
		idsCreated.forEach(areaOfInterestRepository::deleteById);
	}

	@Test
	public void testCreateGeometry() {
		final AreaOfInterest areaOfInterest = new AreaOfInterest();
		areaOfInterest.setId("id0");
		areaOfInterest.setGeometryWkt(GEOMETRY_WKT_LIST.get(0));

		final String id = geometricIntersectionService.createGeometry(areaOfInterest);
		idsCreated.add(id);

		Assert.assertEquals("id0",
							id);
	}

	@Test(expected = InvalidAreaOfInterestException.class)
	public void testCreateGeometryInvalidWkt() {
		final AreaOfInterest areaOfInterest = new AreaOfInterest();
		areaOfInterest.setId("id0");
		areaOfInterest.setGeometryWkt(GEOMETRY_WKT_LIST.get(0)
				.substring(1));

		geometricIntersectionService.createGeometry(areaOfInterest);
	}

	@Test(expected = InvalidAreaOfInterestException.class)
	public void testCreateGeometryNullWkt() {
		final AreaOfInterest areaOfInterest = new AreaOfInterest();
		areaOfInterest.setId("id0");
		areaOfInterest.setGeometryWkt(null);

		geometricIntersectionService.createGeometry(areaOfInterest);
	}

	@Test(expected = InvalidAreaOfInterestException.class)
	public void testCreateGeometryUnclosed() {
		final AreaOfInterest areaOfInterest = new AreaOfInterest();
		areaOfInterest.setId("id0");
		areaOfInterest.setGeometryWkt("POLYGON ((-77.4232292175293 38.959759183009226, "
				+ "-77.42216706275939 38.959233596526964, -77.42136240005493 38.960459958921135, "
				+ "-77.42239236831665 38.96086039918754, -77.4232292175293 39.959759183009226))");

		geometricIntersectionService.createGeometry(areaOfInterest);
	}

	@Test(expected = InvalidAreaOfInterestException.class)
	public void testCreateGeometryNullAoi() {
		geometricIntersectionService.createGeometry(null);
	}

	@Test
	public void testGetGeometriesOne() {
		final AreaOfInterest areaOfInterest = new AreaOfInterest();
		areaOfInterest.setId("id0");
		areaOfInterest.setGeometryWkt(GEOMETRY_WKT_LIST.get(0));

		final String id = geometricIntersectionService.createGeometry(areaOfInterest);
		idsCreated.add(id);

		final List<String> ids = geometricIntersectionService.getGeometries();

		Assert.assertNotNull(ids);
		Assert.assertFalse(ids.isEmpty());
		Assert.assertTrue(ids.contains(id));
	}

	@Test
	public void testGetGeometriesFour() {
		final List<String> idsCreatedLocal = new ArrayList<>();

		int i = 0;
		for (final String wkt : GEOMETRY_WKT_LIST) {
			final AreaOfInterest areaOfInterest = new AreaOfInterest();
			areaOfInterest.setId("id" + i++);
			areaOfInterest.setGeometryWkt(wkt);

			final String id = geometricIntersectionService.createGeometry(areaOfInterest);
			idsCreated.add(id);
			idsCreatedLocal.add(id);
		}

		final List<String> ids = geometricIntersectionService.getGeometries();

		Assert.assertNotNull(ids);
		Assert.assertTrue(ids.size() >= 4);
		Assert.assertTrue(ids.containsAll(idsCreatedLocal));
	}

	@Test
	public void testGetGeometryById()
			throws AreaOfInterestIdDoesNotExistException {
		final AreaOfInterest areaOfInterest = new AreaOfInterest();
		areaOfInterest.setId("id0");
		areaOfInterest.setGeometryWkt(GEOMETRY_WKT_LIST.get(0));

		final String id = geometricIntersectionService.createGeometry(areaOfInterest);
		idsCreated.add(id);

		final AreaOfInterest areaOfInterest2 = geometricIntersectionService.getGeometryById(id);

		Assert.assertNotNull(areaOfInterest2);
		Assert.assertEquals(areaOfInterest.getId(),
							areaOfInterest2.getId());
		Assert.assertEquals(areaOfInterest.getGeometryWkt(),
							areaOfInterest2.getGeometryWkt());
	}

	@Test(expected = AreaOfInterestIdDoesNotExistException.class)
	public void testGetGeometryByIdDoesNotExist()
			throws AreaOfInterestIdDoesNotExistException {
		final String id = "00000000-0000-0000-0000-000000000000";

		geometricIntersectionService.getGeometryById(id);
	}

	@Test
	public void testGetIntersectingGeometriesPointNoMatches() {
		final AreaOfInterest areaOfInterest = new AreaOfInterest();
		areaOfInterest.setId("id0");
		areaOfInterest.setGeometryWkt(GEOMETRY_WKT_LIST.get(0));

		final String id = geometricIntersectionService.createGeometry(areaOfInterest);
		idsCreated.add(id);

		final String wkt = "POINT(-77.42078304290771 38.95920856850198)";

		final List<AreaOfInterest> areasOfInterest = geometricIntersectionService.getIntersectingGeometries(wkt);

		Assert.assertNotNull(areasOfInterest);
		Assert.assertTrue(areasOfInterest.isEmpty());
	}

	@Test
	public void testGetIntersectingGeometriesPolygonNoMatches() {
		final AreaOfInterest areaOfInterest = new AreaOfInterest();
		areaOfInterest.setId("id0");
		areaOfInterest.setGeometryWkt(GEOMETRY_WKT_LIST.get(0));

		final String id = geometricIntersectionService.createGeometry(areaOfInterest);
		idsCreated.add(id);

		final String wkt = GEOMETRY_WKT_LIST.get(1);

		final List<AreaOfInterest> areasOfInterest = geometricIntersectionService.getIntersectingGeometries(wkt);

		Assert.assertNotNull(areasOfInterest);
		Assert.assertTrue(areasOfInterest.isEmpty());
	}

	@Test
	public void testGetIntersectingGeometriesPointOneMatch() {
		final AreaOfInterest areaOfInterest = new AreaOfInterest();
		areaOfInterest.setId("id0");
		areaOfInterest.setGeometryWkt(GEOMETRY_WKT_LIST.get(0));

		final String id = geometricIntersectionService.createGeometry(areaOfInterest);
		idsCreated.add(id);

		final String wkt = "POINT(-77.4223655462265 38.960172141081756)";

		final List<AreaOfInterest> areasOfInterest = geometricIntersectionService.getIntersectingGeometries(wkt);

		Assert.assertNotNull(areasOfInterest);
		Assert.assertEquals(1,
							areasOfInterest.size());
		Assert.assertEquals(areaOfInterest.getGeometryWkt(),
							areasOfInterest.get(0)
									.getGeometryWkt());
	}

	@Test
	public void testGetIntersectingGeometriesPolygonOneMatch() {
		final AreaOfInterest areaOfInterest = new AreaOfInterest();
		areaOfInterest.setId("id0");
		areaOfInterest.setGeometryWkt(GEOMETRY_WKT_LIST.get(0));

		final String id = geometricIntersectionService.createGeometry(areaOfInterest);
		idsCreated.add(id);

		final String wkt = "POLYGON((-77.42164134979248 38.96085205670507,-77.421555519104 38.959742497784,"
				+ "-77.42032170295715 38.959817581266606,-77.42056846618652 38.96095216642984,"
				+ "-77.42164134979248 38.96085205670507))";

		final List<AreaOfInterest> areasOfInterest = geometricIntersectionService.getIntersectingGeometries(wkt);

		Assert.assertNotNull(areasOfInterest);
		Assert.assertEquals(1,
							areasOfInterest.size());
		Assert.assertEquals(areaOfInterest.getGeometryWkt(),
							areasOfInterest.get(0)
									.getGeometryWkt());
	}

	@Test
	public void testGetIntersectingGeometriesPolygonFourMatches() {
		int i = 0;
		for (final String wkt : GEOMETRY_WKT_LIST) {
			final AreaOfInterest areaOfInterest = new AreaOfInterest();
			areaOfInterest.setId("id" + i++);
			areaOfInterest.setGeometryWkt(wkt);

			final String id = geometricIntersectionService.createGeometry(areaOfInterest);
			idsCreated.add(id);
		}

		final String wkt = "POLYGON((-77.42164134979248 38.96085205670507,-77.421555519104 38.959742497784,"
				+ "-77.42032170295715 38.959817581266606,-77.42056846618652 38.96095216642984,"
				+ "-77.42164134979248 38.96085205670507))";

		final List<AreaOfInterest> areasOfInterest = geometricIntersectionService.getIntersectingGeometries(wkt);

		Assert.assertNotNull(areasOfInterest);
		Assert.assertEquals(4,
							areasOfInterest.size());
		Assert.assertTrue(areasOfInterest.stream()
				.map(AreaOfInterest::getGeometryWkt)
				.collect(Collectors.toList())
				.containsAll(GEOMETRY_WKT_LIST));
	}

	@Test(expected = InvalidAreaOfInterestException.class)
	public void testGetIntersectingGeometriesPolygonInvalidWkt() {
		final AreaOfInterest areaOfInterest = new AreaOfInterest();
		areaOfInterest.setId("id0");
		areaOfInterest.setGeometryWkt(GEOMETRY_WKT_LIST.get(0));

		final String id = geometricIntersectionService.createGeometry(areaOfInterest);
		idsCreated.add(id);

		final String wkt = "OLYGON((-77.42164134979248 38.96085205670507,-77.421555519104 38.959742497784,"
				+ "-77.42032170295715 38.959817581266606,-77.42056846618652 38.96095216642984,"
				+ "-77.42164134979248 38.96085205670507))";

		geometricIntersectionService.getIntersectingGeometries(wkt);
	}

	@Test
	public void testDeleteGeometryById()
			throws AreaOfInterestIdDoesNotExistException {
		final AreaOfInterest areaOfInterest = new AreaOfInterest();
		areaOfInterest.setGeometryWkt(GEOMETRY_WKT_LIST.get(0));
		areaOfInterest.setId("id0");

		final String id = geometricIntersectionService.createGeometry(areaOfInterest);

		geometricIntersectionService.deleteGeometryById(id);

		Assert.assertTrue(true);
	}

	@Test(expected = AreaOfInterestIdDoesNotExistException.class)
	public void testDeleteGeometryByIdDoesNotExist()
			throws AreaOfInterestIdDoesNotExistException {
		final AreaOfInterest areaOfInterest = new AreaOfInterest();
		areaOfInterest.setId("id0");
		areaOfInterest.setGeometryWkt(GEOMETRY_WKT_LIST.get(0));

		final String id = "00000000-0000-0000-0000-000000000000";

		geometricIntersectionService.deleteGeometryById(id);
	}
}
