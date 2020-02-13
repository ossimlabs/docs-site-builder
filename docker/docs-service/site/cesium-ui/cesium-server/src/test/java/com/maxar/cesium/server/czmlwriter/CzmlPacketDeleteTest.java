package com.maxar.cesium.server.czmlwriter;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.joda.time.DateTime;

import com.fasterxml.jackson.databind.JsonNode;
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

public class CzmlPacketDeleteTest
{
	public static void main(
			final String[] args ) {
		testDeletePart1();
		// testDeletePart2();
	}

	final static String ip = "localhost:8081";
	final static String session = "test";
	final static String LONE1_TO_DELETE_ID = "Lone1ToDelete";
	final static String LONE2_TO_STAY_ID = "Lone2ToStay";
	final static String PARENT1_TO_DELETE_ID = "Parent1ToDelete";
	final static String CHILD_OF_PARENT1_TO_DELETE_ID = "Child1ToDelete";
	final static String PARENT2_TO_STAY_ID = "Parent2ToStay";
	final static String CHILD_OF_PARENT2_TO_DELETE_ID = "Child2ToDelete";
	final static String PARENT3_TO_DELETE_ID = "Parent3ToDelete";
	final static String CHILD_OF_PARENT3_TO_DELETE_VIA_PARENT_ID = "Child3ToDeleteViaParent";
	final static String PARENT4_TO_STAY_ID = "Parent4ToStay";
	final static String CHILD1_OF_PARENT4_TO_STAY_ID = "Child41ToStay";
	final static String CHILD2_OF_PARENT4_TO_DELETE_ID = "Child42ToDelete";
	final static String CHILD3_OF_PARENT4_TO_STAY_ID = "Child43ToStay";

	final private static List<LatLonAlt> ORIGIN_BOX_POINTS = new ArrayList<>() {
		private static final long serialVersionUID = 1L;

		{
			add(new LatLonAlt(
					Angle.Zero(),
					Angle.Zero(),
					Length.Zero()));
			add(new LatLonAlt(
					Angle.fromDegrees(5),
					Angle.Zero(),
					Length.Zero()));
			add(new LatLonAlt(
					Angle.fromDegrees(5),
					Angle.fromDegrees(5),
					Length.Zero()));
			add(new LatLonAlt(
					Angle.Zero(),
					Angle.fromDegrees(5),
					Length.Zero()));
			add(new LatLonAlt(
					Angle.Zero(),
					Angle.Zero(),
					Length.Zero()));
		}
	};

	public static void testDeletePart1() {

		// Stand alone tree node to be deleted, with timeline item and group to be
		// deleted
		final JsonNode lone1 = createBox(	LONE1_TO_DELETE_ID,
											ORIGIN_BOX_POINTS,
											null,
											"TestToBeDeleted");

		// Stand alone tree node to stick around, with timeline group
		final List<LatLonAlt> lone2PointsList = shiftBoxEast(ORIGIN_BOX_POINTS);
		final JsonNode lone2 = createBox(	LONE2_TO_STAY_ID,
											lone2PointsList,
											null,
											"TestToStay");

		// Parent tree node to be deleted
		final List<LatLonAlt> parent1PointsList = shiftBoxSouth(ORIGIN_BOX_POINTS);
		final JsonNode parentNode = createBox(	PARENT1_TO_DELETE_ID,
												parent1PointsList,
												null,
												null);
		// ***Child tree node to be deleted specifically
		final List<LatLonAlt> childPointsList = shiftBoxEast(parent1PointsList);
		final JsonNode childNode = createBox(	CHILD_OF_PARENT1_TO_DELETE_ID,
												childPointsList,
												PARENT1_TO_DELETE_ID,
												null);

		// Parent tree node to stick around (with child to be deleted)
		final List<LatLonAlt> parent2PointsList = shiftBoxSouth(parent1PointsList);
		final JsonNode parent2Node = createBox(	PARENT2_TO_STAY_ID,
												parent2PointsList,
												null,
												null);
		// ***Child tree node to be deleted specifically (while it's parent is not)
		final List<LatLonAlt> child2PointsList = shiftBoxEast(parent2PointsList);
		final JsonNode child2Node = createBox(	CHILD_OF_PARENT2_TO_DELETE_ID,
												child2PointsList,
												PARENT2_TO_STAY_ID,
												null);

		// Parent tree node to be deleted and take it's child with it that is not marked
		// for deletion
		final List<LatLonAlt> parent3PointsList = shiftBoxSouth(parent2PointsList);
		final JsonNode parent3Node = createBox(	PARENT3_TO_DELETE_ID,
												parent3PointsList,
												null,
												null);
		// ***Child tree node to be deleted along with its parent, but not via it's own
		// delete packet
		final List<LatLonAlt> child3PointsList = shiftBoxEast(parent3PointsList);
		final JsonNode child3Node = createBox(	CHILD_OF_PARENT3_TO_DELETE_VIA_PARENT_ID,
												child3PointsList,
												PARENT3_TO_DELETE_ID,
												null);

		// Parent tree node to stick around with some childrent staying, and some being
		// deleted
		final List<LatLonAlt> parent4PointsList = shiftBoxSouth(parent3PointsList);
		final JsonNode parent4Node = createBox(	PARENT4_TO_STAY_ID,
												parent4PointsList,
												null,
												null);
		// ***Child node to stick around, but sibling deleted
		final List<LatLonAlt> child41PointsList = shiftBoxEast(parent4PointsList);
		final JsonNode child41Node = createBox(	CHILD1_OF_PARENT4_TO_STAY_ID,
												child41PointsList,
												PARENT4_TO_STAY_ID,
												null);
		// ***Child to be deleted, but siblings stick around
		final List<LatLonAlt> child42PointsList = shiftBoxEast(child41PointsList);
		final JsonNode child42Node = createBox(	CHILD2_OF_PARENT4_TO_DELETE_ID,
												child42PointsList,
												PARENT4_TO_STAY_ID,
												null);
		// ***Child node to stick around, but sibling deleted
		final List<LatLonAlt> child43PointsList = shiftBoxEast(child42PointsList);
		final JsonNode child43Node = createBox(	CHILD3_OF_PARENT4_TO_STAY_ID,
												child43PointsList,
												PARENT4_TO_STAY_ID,
												null);

		final List<JsonNode> packets = Arrays.asList(new JsonNode[] {
			lone1,
			lone2,
			parentNode,
			childNode,
			parent2Node,
			child2Node,
			parent3Node,
			child3Node,
			parent4Node,
			child41Node,
			child42Node,
			child43Node
		});

		CzmlTestUtils.sendPackets(	packets,
									true,
									ip,
									session);
	}

	private static List<LatLonAlt> shiftBoxEast(
			final List<LatLonAlt> points ) {
		return shiftBox(points,
						Angle.Zero(),
						Angle.fromDegrees(5));
	}

	private static List<LatLonAlt> shiftBoxSouth(
			final List<LatLonAlt> points ) {
		return shiftBox(points,
						Angle.fromDegrees(-5),
						Angle.Zero());
	}

	private static List<LatLonAlt> shiftBox(
			final List<LatLonAlt> points,
			final Angle latitude,
			final Angle longitude ) {

		return points.stream()
				.map(point -> new LatLonAlt(
						point.latitude()
								.plus(latitude),
						point.longitude()
								.plus(longitude),
						point.altitude()))
				.collect(Collectors.toList());

	}

	private static JsonNode createBox(
			final String id,
			final List<LatLonAlt> points,
			final String parent,
			final String group ) {
		Packet packet = Packet.create()
				.id(id)
				.name(id)

				.polyline(Polyline.create()
						.positions(PositionList.cartographicDegrees(points))
						.material(PolylineMaterial.create()
								.polylineOutline(PolylineOutlineMaterial.create()
										.color(ColorRefValue.color(new Color(
												255,
												0,
												0))))));

		if (parent != null) {
			packet = packet.parent(parent);
		}

		if (group != null) {
			packet = packet.timelineControl(TimelineControl.create()
					.group(group)
					.start(new DateTime())
					.end(new DateTime().plusHours(1)));
		}

		return packet.toJsonNode();
	}

	public static void testDeletePart2() {

		final JsonNode lone1 = createDeletePacket(LONE1_TO_DELETE_ID);
		final JsonNode parent1Node = createDeletePacket(PARENT1_TO_DELETE_ID);
		final JsonNode child1Node = createDeletePacket(CHILD_OF_PARENT1_TO_DELETE_ID);
		final JsonNode child2Node = createDeletePacket(CHILD_OF_PARENT2_TO_DELETE_ID);
		final JsonNode parent3Node = createDeletePacket(PARENT3_TO_DELETE_ID);
		final JsonNode child42Node = createDeletePacket(CHILD2_OF_PARENT4_TO_DELETE_ID);

		final List<JsonNode> packets = Arrays.asList(new JsonNode[] {
			lone1,
			parent1Node,
			child1Node,
			child2Node,
			parent3Node,
			child42Node
		});

		CzmlTestUtils.sendPackets(	packets,
									false,
									ip,
									session);
	}

	public static JsonNode createDeletePacket(
			final String id ) {
		return Packet.create()
				.id(id)
				.delete(true)
				.toJsonNode();
	}

}
