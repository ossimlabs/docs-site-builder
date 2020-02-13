package com.maxar.geometry.ingest.translate;

import java.io.ByteArrayInputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateSequence;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.impl.CoordinateArraySequence;
import org.locationtech.jts.io.WKTWriter;
import org.springframework.stereotype.Component;

import com.maxar.geometric.intersection.model.AreaOfInterest;

import de.micromata.opengis.kml.v_2_2_0.Document;
import de.micromata.opengis.kml.v_2_2_0.Feature;
import de.micromata.opengis.kml.v_2_2_0.Folder;
import de.micromata.opengis.kml.v_2_2_0.Geometry;
import de.micromata.opengis.kml.v_2_2_0.Kml;
import de.micromata.opengis.kml.v_2_2_0.LinearRing;
import de.micromata.opengis.kml.v_2_2_0.MultiGeometry;
import de.micromata.opengis.kml.v_2_2_0.Placemark;
import de.micromata.opengis.kml.v_2_2_0.Polygon;

@Component
public class KmlAoiTranslator
{
	private static GeometryFactory geometryFactory = new GeometryFactory();

	/**
	 * Translates a raw byte array of KML into a list of areas of interest (AOIs).
	 *
	 * This method looks for a KML document with folders or placemarks inside. The
	 * folders are searched for folders or placemarks. As placemarks are found, they
	 * are searched for an ID attribute or name element, and are also searched for a
	 * geometry. The geometry can be a linear ring, a multi-geometry, or a polygon.
	 * The geometry is translated into well-known text (WKT). All AOIs found in the
	 * document are returned. Any placemark without an ID or a recognized geometry
	 * will be ignored.
	 *
	 * If the KML cannot be parsed, the error is ignored, and no AOIs will be
	 * returned.
	 *
	 * @param rawKml
	 *            The raw KML data.
	 * @return A list of AOIs retrieved from the KML.
	 */
	public List<AreaOfInterest> translateKmlToAois(
			final byte[] rawKml ) {
		if (rawKml == null || rawKml.length == 0) {
			return Collections.emptyList();
		}

		final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(
				rawKml);

		final Kml kml = Kml.unmarshal(byteArrayInputStream);

		return Optional.ofNullable(kml)
				.map(Kml::getFeature)
				.map(Document.class::cast)
				.map(Document::getFeature)
				.orElse(Collections.emptyList())
				.stream()
				.map(this::translateFeatureToAois)
				.flatMap(Collection::stream)
				.collect(Collectors.toList());
	}

	private List<AreaOfInterest> translateFeatureToAois(
			final Feature feature ) {
		if (feature == null) {
			return Collections.emptyList();
		}
		else if (feature.getClass()
				.isAssignableFrom(Folder.class)) {
			return translateFolderToAois((Folder) feature);
		}
		else if (feature.getClass()
				.isAssignableFrom(Placemark.class)) {
			return translatePlacemarkToAois((Placemark) feature);
		}
		else {
			return Collections.emptyList();
		}
	}

	private List<AreaOfInterest> translateFolderToAois(
			final Folder folder ) {
		return folder.getFeature()
				.stream()
				.map(this::translateFeatureToAois)
				.flatMap(Collection::stream)
				.collect(Collectors.toList());
	}

	private List<AreaOfInterest> translatePlacemarkToAois(
			final Placemark placemark ) {
		final String id = Optional.ofNullable(placemark.getId())
				.orElseGet(placemark::getName);

		if (id == null || id.isEmpty()) {
			return Collections.emptyList();
		}

		final Geometry geometry = placemark.getGeometry();

		final String wkt = translateGeometryToWkt(geometry);

		if (wkt == null || wkt.isEmpty()) {
			return Collections.emptyList();
		}

		final AreaOfInterest aoi = new AreaOfInterest();
		aoi.setId(id);
		aoi.setGeometryWkt(wkt);

		return Collections.singletonList(aoi);
	}

	private String translateGeometryToWkt(
			final Geometry geometry ) {
		if (geometry == null) {
			return "";
		}
		if (geometry.getClass()
				.isAssignableFrom(LinearRing.class)) {
			final LinearRing linearRing = (LinearRing) geometry;
			final org.locationtech.jts.geom.Geometry jtsGeometry = linearRingToJts(linearRing);

			return jtsGeometryToWkt(jtsGeometry);
		}
		else if (geometry.getClass()
				.isAssignableFrom(MultiGeometry.class)) {
			final MultiGeometry multiGeometry = (MultiGeometry) geometry;

			return multiGeometry.getGeometry()
					.stream()
					.map(this::translateGeometryToWkt)
					.filter(s -> !s.isEmpty())
					.findFirst()
					.orElse("");
		}
		else if (geometry.getClass()
				.isAssignableFrom(Polygon.class)) {
			final Polygon polygon = (Polygon) geometry;

			return Optional.ofNullable(polygon.getOuterBoundaryIs())
					.map(outerBoundary -> linearRingToJts(outerBoundary.getLinearRing()))
					.map(this::jtsGeometryToWkt)
					.orElse("");
		}
		else {
			return "";
		}
	}

	private org.locationtech.jts.geom.LinearRing linearRingToJts(
			final LinearRing linearRing ) {
		final Coordinate[] coordinates = Optional.ofNullable(linearRing)
				.map(LinearRing::getCoordinates)
				.orElse(Collections.emptyList())
				.stream()
				.map(c -> new Coordinate(
						c.getLongitude(),
						c.getLatitude(),
						c.getAltitude()))
				.toArray(Coordinate[]::new);

		final CoordinateSequence coordinateSequence = new CoordinateArraySequence(
				coordinates);

		return new org.locationtech.jts.geom.LinearRing(
				coordinateSequence,
				geometryFactory);
	}

	private String jtsGeometryToWkt(
			final org.locationtech.jts.geom.Geometry geometry ) {
		final WKTWriter wktWriter = new WKTWriter();

		return wktWriter.write(geometry);
	}
}
