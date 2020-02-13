package com.maxar.alert.czml;

import java.awt.Color;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.joda.time.Interval;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;
import org.springframework.beans.factory.annotation.Autowired;

import com.maxar.alert.model.Event;
import com.maxar.cesium.czmlwriter.Packet;
import com.maxar.cesium.czmlwriter.material.PolylineMaterial;
import com.maxar.cesium.czmlwriter.material.PolylineOutlineMaterial;
import com.maxar.cesium.czmlwriter.packet.Polyline;
import com.maxar.cesium.czmlwriter.packet.Position;
import com.maxar.cesium.czmlwriter.packet.TimelineControl;
import com.maxar.cesium.czmlwriter.positionlist.PositionList;
import com.maxar.cesium.czmlwriter.property.Property;
import com.maxar.cesium.czmlwriter.refvalue.BooleanRefValue;
import com.maxar.cesium.czmlwriter.refvalue.ColorRefValue;
import com.maxar.cesium.czmlwriter.refvalue.DoubleRefValue;
import com.maxar.cesium.czmlwriter.types.PropertyInterval;
import com.radiantblue.analytics.core.measures.Angle;
import com.radiantblue.analytics.core.measures.Length;
import com.radiantblue.analytics.geodesy.LatLonAlt;

/**
 * Creates CZML for alerts.
 */
public class AlertCzmlProducer
{
	@Autowired
	private AlertCzmlProperties alertCzmlProperties;

	/**
	 * @param event
	 *            The event to produce CZML for.
	 * @return CZML for the event geometry.
	 */
	String produceCzml(
			final Event event ) {
		try {
			final List<Packet> packets = makePackets(event);

			return packets.stream()
					.map(Packet::writeString)
					.collect(Collectors.joining(", "));
		}
		catch (final ParseException e) {
			return null;
		}
	}

	private List<Packet> makePackets(
			final Event event )
			throws ParseException {
		final WKTReader wktReader = new WKTReader();
		final List<Packet> alertPackets = new ArrayList<>();

		final Geometry geometry = wktReader.read(event.getGeometryWkt());

		// Alert hierarchy - 1) Country Code 2) Source 3) Type
		// Country Code
		alertPackets.add(Packet.create()
				.id(event.getCountry())
				.name(event.getCountry()));
		// Source
		alertPackets.add(Packet.create()
				.id(event.getCountry() + event.getSource())
				.name(event.getSource())
				.parent(event.getCountry()));
		// Type
		alertPackets.add(Packet.create()
				.id(event.getCountry() + event.getSource() + event.getType())
				.name(event.getType())
				.parent(event.getCountry() + event.getSource()));

		final Packet packet = Packet.create()
				.id(event.getId())
				.name(event.getCountry() + ": " + event.getType())
				.parent(event.getCountry() + event.getSource() + event.getType())
				.timelineControl(TimelineControl.create()
						.start(event.getStartTime())
						.end(event.getEndTime())
						.group(event.getType()))
				.availability(	event.getStartTime(),
								event.getEndTime());

		if (geometry instanceof Point) {
			alertPackets.add(makePointPacket(	event,
												packet,
												(Point) geometry));
		}
		else {
			alertPackets.add(makePolylinePacket(event,
												packet,
												geometry));
		}

		return alertPackets;
	}

	private Packet makePointPacket(
			final Event event,
			final Packet packet,
			final Point point ) {
		final com.maxar.cesium.czmlwriter.packet.Point packetPoint = com.maxar.cesium.czmlwriter.packet.Point.create()
				.color(colorFromString(alertCzmlProperties.getPointColor()))
				.pixelSize(DoubleRefValue.number(alertCzmlProperties.getPointPixelSize()));

		final PropertyInterval<Position> propertyInterval = new PropertyInterval<>(
				new Interval(
						event.getStartTime(),
						event.getEndTime()),
				Position.cartographicDegrees(new LatLonAlt(
						Angle.fromDegrees(point.getY()),
						Angle.fromDegrees(point.getX()),
						Length.Zero())));

		return packet.position(Property.interval(Collections.singletonList(propertyInterval)))
				.point(packetPoint);
	}

	private Packet makePolylinePacket(
			final Event event,
			final Packet packet,
			final Geometry geometry ) {
		final PropertyInterval<PositionList> propertyInterval = new PropertyInterval<>(
				new Interval(
						event.getStartTime(),
						event.getEndTime()),
				PositionList.geometry(geometry));

		final Polyline polyline = Polyline.create()
				.clampToGround(BooleanRefValue.booleanValue(true))
				.material(PolylineMaterial.create()
						.polylineOutline(PolylineOutlineMaterial.create()
								.outlineWidth(DoubleRefValue.number(alertCzmlProperties.getPolylineOutlineWidth()))
								.color(colorFromString(alertCzmlProperties.getPolylineOutlineColor()))))
				.positions(Property.interval(Collections.singletonList(propertyInterval)));

		return packet.polyline(polyline);
	}

	private static ColorRefValue colorFromString(
			final String colorString ) {
		return ColorRefValue.color(new Color(
				new BigInteger(
						colorString,
						16).intValue(),
				true));
	}
}
