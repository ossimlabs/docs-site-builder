package com.maxar.geometric.intersection.czml;

import java.awt.Color;
import java.math.BigInteger;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;

import com.maxar.cesium.czmlwriter.Packet;
import com.maxar.cesium.czmlwriter.material.PolylineMaterial;
import com.maxar.cesium.czmlwriter.material.PolylineOutlineMaterial;
import com.maxar.cesium.czmlwriter.packet.Polyline;
import com.maxar.cesium.czmlwriter.positionlist.PositionList;
import com.maxar.cesium.czmlwriter.refvalue.BooleanRefValue;
import com.maxar.cesium.czmlwriter.refvalue.ColorRefValue;
import com.maxar.cesium.czmlwriter.refvalue.DoubleRefValue;
import com.maxar.geometric.intersection.model.AreaOfInterest;

public class AreaOfInterestCzmlProducer
{
	/**
	 * Create a CZML string from an area of interest's geometry.
	 *
	 * @param areaOfInterest
	 *            The area of interest to produce CZML for.
	 * @param properties
	 *            The properties set for this service's CZML generation.
	 * @return CZML based on the area of interest's geometry.
	 */
	String produceCzml(
			final AreaOfInterest areaOfInterest,
			final AreaOfInterestCzmlProperties properties ) {
		final Packet packet = makePacket(	areaOfInterest,
											properties);

		if (packet == null) {
			return null;
		}
		else {
			return packet.writeString();
		}
	}

	/**
	 * @param areaOfInterest
	 *            The area of interest to produce CZML for.
	 * @param properties
	 *            The properties set for this service's CZML generation.
	 * @return A CZML packet based on the area of interest's geometry.
	 */
	private Packet makePacket(
			final AreaOfInterest areaOfInterest,
			final AreaOfInterestCzmlProperties properties ) {
		final WKTReader reader = new WKTReader();

		try {
			final Geometry geometry = reader.read(areaOfInterest.getGeometryWkt());

			final Packet packet = Packet.create();

			final Polyline polyline = Polyline.create()
					.clampToGround(BooleanRefValue.booleanValue(true))
					.material(PolylineMaterial.create()
							.polylineOutline(PolylineOutlineMaterial.create()
									.color(ColorRefValue.color(makeColorFromString(properties.getColor())))
									.outlineWidth(DoubleRefValue.number(properties.getOutlineWidth()))))
					.positions(PositionList.geometry(geometry));

			return packet.polyline(polyline)
					.id(areaOfInterest.getId())
					.name(areaOfInterest.getId());
		}
		catch (final ParseException e) {
			return null;
		}
	}

	private static Color makeColorFromString(
			final String s ) {
		return new Color(
				new BigInteger(
						s,
						16).intValue(),
				true);
	}
}
