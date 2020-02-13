package com.maxar.geometry.ingest.translate;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import com.maxar.geometric.intersection.model.AreaOfInterest;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@TestPropertySource(locations = "classpath:test-geometry-ingest.properties")
public class KmlAoiTranslatorTest
{
	@Autowired
	private KmlAoiTranslator kmlAoiTranslator;

	private static final String EXPECTED_WKT = "LINEARRING (29.70703125 24.407137917727667, "
			+ "31.596679687499996 24.407137917727667, 31.596679687499996 26.293415004265796, "
			+ "29.70703125 26.293415004265796, 29.70703125 24.407137917727667)";

	@Test
	public void testTranslateKmlToAoisDocumentPlacemarkPolygon()
			throws IOException {
		final InputStream inputStream = this.getClass()
				.getResourceAsStream("/kml/document_placemark_polygon.kml");
		final byte[] bytes = inputStream.readAllBytes();

		Assert.assertNotNull(bytes);

		final List<AreaOfInterest> aois = kmlAoiTranslator.translateKmlToAois(bytes);

		Assert.assertNotNull(aois);
		Assert.assertEquals(1,
							aois.size());
		Assert.assertNotNull(aois.get(0));
		Assert.assertEquals("document_placemark_polygon",
							aois.get(0)
									.getId());
		Assert.assertEquals(EXPECTED_WKT,
							aois.get(0)
									.getGeometryWkt());
	}

	@Test
	public void testTranslateKmlToAoisDocumentPlacemarkLinearRing()
			throws IOException {
		final InputStream inputStream = this.getClass()
				.getResourceAsStream("/kml/document_placemark_linearring.kml");
		final byte[] bytes = inputStream.readAllBytes();

		Assert.assertNotNull(bytes);

		final List<AreaOfInterest> aois = kmlAoiTranslator.translateKmlToAois(bytes);

		Assert.assertNotNull(aois);
		Assert.assertEquals(1,
							aois.size());
		Assert.assertNotNull(aois.get(0));
		Assert.assertEquals("document_placemark_linearring",
							aois.get(0)
									.getId());
		Assert.assertEquals(EXPECTED_WKT,
							aois.get(0)
									.getGeometryWkt());
	}

	@Test
	public void testTranslateKmlToAoisDocumentFolderPlacemarkMultiGeometry()
			throws IOException {
		final InputStream inputStream = this.getClass()
				.getResourceAsStream("/kml/document_folder_placemark_multigeometry.kml");
		final byte[] bytes = inputStream.readAllBytes();

		Assert.assertNotNull(bytes);

		final List<AreaOfInterest> aois = kmlAoiTranslator.translateKmlToAois(bytes);

		Assert.assertNotNull(aois);
		Assert.assertEquals(1,
							aois.size());
		Assert.assertNotNull(aois.get(0));
		Assert.assertEquals("document_folder_placemark_multigeometry",
							aois.get(0)
									.getId());
		Assert.assertEquals(EXPECTED_WKT,
							aois.get(0)
									.getGeometryWkt());
	}

	@Test
	public void testTranslateKmlToAoisDocumentFolderPlacemarkMultiGeometryMultiple()
			throws IOException {
		final InputStream inputStream = this.getClass()
				.getResourceAsStream("/kml/document_folder_placemark_multigeometry_multiple.kml");
		final byte[] bytes = inputStream.readAllBytes();

		Assert.assertNotNull(bytes);

		final List<AreaOfInterest> aois = kmlAoiTranslator.translateKmlToAois(bytes);

		Assert.assertNotNull(aois);
		Assert.assertEquals(3,
							aois.size());
		Assert.assertNotNull(aois.get(0));
		Assert.assertEquals("document_folder_placemark_multigeometry_multiple_00",
							aois.get(0)
									.getId());
		Assert.assertEquals(EXPECTED_WKT,
							aois.get(0)
									.getGeometryWkt());

		Assert.assertNotNull(aois.get(1));
		Assert.assertEquals("document_folder_placemark_multigeometry_multiple_01",
							aois.get(1)
									.getId());
		Assert.assertEquals("LINEARRING (29.7 24.4, 31.5 24.4, 31.5 26.2, 29.7 26.2, 29.7 24.4)",
							aois.get(1)
									.getGeometryWkt());

		Assert.assertNotNull(aois.get(2));
		Assert.assertEquals("document_folder_placemark_multigeometry_multiple_02",
							aois.get(2)
									.getId());
		Assert.assertEquals("LINEARRING (28.7 23.4, 30.5 23.4, 30.5 25.2, 28.7 25.2, 28.7 23.4)",
							aois.get(2)
									.getGeometryWkt());
	}

	@Test
	public void testTranslateKmlToAoisDocumentFolderPlacemarkMultiGeometryId()
			throws IOException {
		final InputStream inputStream = this.getClass()
				.getResourceAsStream("/kml/document_folder_placemark_multigeometry_id.kml");
		final byte[] bytes = inputStream.readAllBytes();

		Assert.assertNotNull(bytes);

		final List<AreaOfInterest> aois = kmlAoiTranslator.translateKmlToAois(bytes);

		Assert.assertNotNull(aois);
		Assert.assertEquals(1,
							aois.size());
		Assert.assertNotNull(aois.get(0));
		Assert.assertEquals("document_folder_placemark_multigeometry_id",
							aois.get(0)
									.getId());
		Assert.assertEquals(EXPECTED_WKT,
							aois.get(0)
									.getGeometryWkt());
	}

	@Test
	public void testTranslateKmlToAoisDocumentFolderPlacemarkMultiGeometryNoName()
			throws IOException {
		final InputStream inputStream = this.getClass()
				.getResourceAsStream("/kml/document_folder_placemark_multigeometry_no_name.kml");
		final byte[] bytes = inputStream.readAllBytes();

		Assert.assertNotNull(bytes);

		final List<AreaOfInterest> aois = kmlAoiTranslator.translateKmlToAois(bytes);

		Assert.assertNotNull(aois);
		Assert.assertTrue(aois.isEmpty());
	}

	@Test
	public void testTranslateKmlToAoisDocumentFolderPlacemarkMultiGeometryNoPolygon()
			throws IOException {
		final InputStream inputStream = this.getClass()
				.getResourceAsStream("/kml/document_folder_placemark_multigeometry_no_polygon.kml");
		final byte[] bytes = inputStream.readAllBytes();

		Assert.assertNotNull(bytes);

		final List<AreaOfInterest> aois = kmlAoiTranslator.translateKmlToAois(bytes);

		Assert.assertNotNull(aois);
		Assert.assertTrue(aois.isEmpty());
	}

	@Test
	public void testTranslateKmlToAoisDocument()
			throws IOException {
		final InputStream inputStream = this.getClass()
				.getResourceAsStream("/kml/document.kml");
		final byte[] bytes = inputStream.readAllBytes();

		Assert.assertNotNull(bytes);

		final List<AreaOfInterest> aois = kmlAoiTranslator.translateKmlToAois(bytes);

		Assert.assertNotNull(aois);
		Assert.assertTrue(aois.isEmpty());
	}

	@Test
	public void testTranslateKmlToAoisDocumentFolder()
			throws IOException {
		final InputStream inputStream = this.getClass()
				.getResourceAsStream("/kml/document_folder.kml");
		final byte[] bytes = inputStream.readAllBytes();

		Assert.assertNotNull(bytes);

		final List<AreaOfInterest> aois = kmlAoiTranslator.translateKmlToAois(bytes);

		Assert.assertNotNull(aois);
		Assert.assertTrue(aois.isEmpty());
	}

	@Test
	public void testTranslateKmlToAoisDocumentGroundOverlay()
			throws IOException {
		final InputStream inputStream = this.getClass()
				.getResourceAsStream("/kml/document_groundoverlay.kml");
		final byte[] bytes = inputStream.readAllBytes();

		Assert.assertNotNull(bytes);

		final List<AreaOfInterest> aois = kmlAoiTranslator.translateKmlToAois(bytes);

		Assert.assertNotNull(aois);
		Assert.assertTrue(aois.isEmpty());
	}

	@Test
	public void testTranslateKmlToAoisNoDocument()
			throws IOException {
		final InputStream inputStream = this.getClass()
				.getResourceAsStream("/kml/no_document.kml");
		final byte[] bytes = inputStream.readAllBytes();

		Assert.assertNotNull(bytes);

		final List<AreaOfInterest> aois = kmlAoiTranslator.translateKmlToAois(bytes);

		Assert.assertNotNull(aois);
		Assert.assertTrue(aois.isEmpty());
	}

	@Test
	public void testTranslateKmlToAoisEmptyByteArray() {
		final byte[] bytes = new byte[0];

		final List<AreaOfInterest> aois = kmlAoiTranslator.translateKmlToAois(bytes);

		Assert.assertNotNull(aois);
		Assert.assertTrue(aois.isEmpty());
	}

	@Test
	public void testTranslateKmlToAoisNullByteArray() {
		final byte[] bytes = null;

		final List<AreaOfInterest> aois = kmlAoiTranslator.translateKmlToAois(bytes);

		Assert.assertNotNull(aois);
		Assert.assertTrue(aois.isEmpty());
	}
}
