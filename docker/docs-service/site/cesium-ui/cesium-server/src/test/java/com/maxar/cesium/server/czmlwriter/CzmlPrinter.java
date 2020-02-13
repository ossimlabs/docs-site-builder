package com.maxar.cesium.server.czmlwriter;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTime;

import com.maxar.cesium.czmlwriter.Packet;
import com.maxar.cesium.czmlwriter.material.PolylineMaterial;
import com.maxar.cesium.czmlwriter.material.PolylineOutlineMaterial;
import com.maxar.cesium.czmlwriter.packet.Polyline;
import com.maxar.cesium.czmlwriter.packet.TimelineControl;
import com.maxar.cesium.czmlwriter.positionlist.PositionList;
import com.maxar.cesium.czmlwriter.refvalue.ColorRefValue;
import com.radiantblue.analytics.core.measures.Angle;
import com.radiantblue.analytics.core.measures.Length;
import com.radiantblue.analytics.geodesy.LatLonAlt;

//Test Utility for printing out test packets
public class CzmlPrinter
{
	public static void main(
			final String[] args ) {

		final List<LatLonAlt> pointsList = new ArrayList<>();
		pointsList.add(new LatLonAlt(
				Angle.Zero(),
				Angle.Zero(),
				Length.Zero()));
		pointsList.add(new LatLonAlt(
				Angle.fromDegrees(5),
				Angle.Zero(),
				Length.Zero()));
		pointsList.add(new LatLonAlt(
				Angle.fromDegrees(5),
				Angle.fromDegrees(5),
				Length.Zero()));
		pointsList.add(new LatLonAlt(
				Angle.Zero(),
				Angle.fromDegrees(5),
				Length.Zero()));

		final Packet packet = Packet.create()
				.id("Test")
				.name("Test")
				.timelineControl(TimelineControl.create()
						.content("TEST")
						.group("TEST")
						.start(DateTime.parse("2018-12-01T21:32:22.284Z"))
						.end(DateTime.parse("2018-12-01T21:32:25.284Z")))
				.polyline(Polyline.create()
						.positions(PositionList.cartographicDegrees(pointsList))
						.material(PolylineMaterial.create()
								.polylineOutline(PolylineOutlineMaterial.create()
										.color(ColorRefValue.color(new Color(
												255,
												0,
												0)))
										.outlineColor(ColorRefValue.color(new Color(
												0,
												0,
												255))))));

		System.out.println(packet.writeString());
	}
}
