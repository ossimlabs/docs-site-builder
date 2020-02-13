package com.maxar.cesium.server.czmlwriter;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.maxar.cesium.czmlwriter.Packet;
import com.maxar.cesium.czmlwriter.material.PolylineMaterial;
import com.maxar.cesium.czmlwriter.material.PolylineOutlineMaterial;
import com.maxar.cesium.czmlwriter.packet.Polyline;
import com.maxar.cesium.czmlwriter.positionlist.PositionList;
import com.maxar.cesium.czmlwriter.property.deletable.DeletableProperty;
import com.maxar.cesium.czmlwriter.refvalue.ColorRefValue;
import com.radiantblue.analytics.core.measures.Angle;
import com.radiantblue.analytics.core.measures.Length;
import com.radiantblue.analytics.geodesy.LatLonAlt;

public class CzmlPropertyDeleteTest
{
	final static String ip = "localhost:8081";
	final static String session = "test";

	public static void main(
			final String[] args ) {
		testDeletePart1();
		// testDeletePart2();
	}

	public static void testDeletePart1() {

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

		final Packet theNode = Packet.create()
				.id("DeleteTest")
				.name("DeleteTestName")
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

		// System.out.println(theNode.writeString());

		CzmlTestUtils.sendPackets(	Collections.singletonList(theNode.toJsonNode()),
									true,
									ip,
									session);
	}

	public static void testDeletePart2() {

		final Packet theNode = Packet.create()
				.id("DeleteTest")
				.polyline(Polyline.create()
						.material(PolylineMaterial.create()
								.polylineOutline(PolylineOutlineMaterial.create()
										.outlineColor(DeletableProperty.createDelete()))));

		// System.out.println(theNode.writeString());

		CzmlTestUtils.sendPackets(	Collections.singletonList(theNode.toJsonNode()),
									false,
									ip,
									session);
	}
}
