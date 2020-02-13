package com.maxar.cesium.czmlwriter;

import static com.maxar.cesium.czmlwriter.property.Property.writeAndClose;

import org.joda.time.DateTime;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.maxar.cesium.czmlwriter.packet.LayerControl;
import com.maxar.cesium.czmlwriter.packet.TimelineControl;
import com.maxar.cesium.czmlwriter.property.Property;

import cesiumlanguagewriter.BillboardCesiumWriter;
import cesiumlanguagewriter.CustomPropertiesCesiumWriter;
import cesiumlanguagewriter.LabelCesiumWriter;
import cesiumlanguagewriter.ModelCesiumWriter;
import cesiumlanguagewriter.OrientationCesiumWriter;
import cesiumlanguagewriter.PathCesiumWriter;
import cesiumlanguagewriter.PointCesiumWriter;
import cesiumlanguagewriter.PolygonCesiumWriter;
import cesiumlanguagewriter.PolylineCesiumWriter;
import cesiumlanguagewriter.PositionCesiumWriter;
import cesiumlanguagewriter.StringCesiumWriter;

public interface Packet
{

	public void write(
			final PacketWriter writer );

	public static Packet create() {
		return writer -> {};
	}

	default public ObjectNode toJsonNode() {
		final PacketWriter packetWriter = new PacketWriter();
		write(packetWriter);
		packetWriter.close();
		return packetWriter.toJsonNode();
	}

	default public String writeString() {
		final PacketWriter packetWriter = new PacketWriter();
		write(packetWriter);
		packetWriter.close();
		return packetWriter.toString();
	}

	default Packet id(
			final String id ) {
		return writer -> {
			write(writer);
			writer.writeId(id);
		};
	}

	default Packet delete(
			final boolean delete ) {
		return writer -> {
			write(writer);
			writer.writeDelete(delete);
		};
	}

	default Packet name(
			final String name ) {
		return writer -> {
			write(writer);
			writer.writeName(name);
		};
	}

	default Packet parent(
			final String parent ) {
		return writer -> {
			write(writer);
			writer.writeParent(parent);
		};
	}

	default Packet description(
			final Property<StringCesiumWriter> description ) {
		return writer -> {
			write(writer);
			writeAndClose(	writer.openDescriptionProperty(),
							description);
		};
	}

	// clock - Purposely skipping since it is only for the document Packet
	// version - Purposely skipping since it is only for the document Packet

	default Packet availability(
			final DateTime start,
			final DateTime stop ) {
		return writer -> {
			write(writer);
			writer.writeAvailability(	CesiumLanguageWriterUtils.joda2Julian(start),
										CesiumLanguageWriterUtils.joda2Julian(stop));
		};
	}

	default Packet properties(
			final Property<CustomPropertiesCesiumWriter> customProperties ) {
		return writer -> {
			write(writer);
			writeAndClose(	writer.openPropertiesProperty(),
							customProperties);
		};
	}

	default Packet position(
			final Property<PositionCesiumWriter> position ) {
		return writer -> {
			write(writer);
			writeAndClose(	writer.openPositionProperty(),
							position);
		};
	}

	default Packet orientation(
			final Property<OrientationCesiumWriter> orientation ) {
		return writer -> {
			write(writer);
			writeAndClose(	writer.openOrientationProperty(),
							orientation);
		};
	}

	// TODO ViewFrom

	default Packet billboard(
			final Property<BillboardCesiumWriter> billboard ) {
		return writer -> {
			write(writer);
			writeAndClose(	writer.openBillboardProperty(),
							billboard);
		};
	}

	// TODO BOX
	// TODO Corridor
	// TODO Cylinder
	// TODO Ellipse
	// TODO Ellipsoid

	default Packet label(
			final Property<LabelCesiumWriter> label ) {
		return writer -> {
			write(writer);
			writeAndClose(	writer.openLabelProperty(),
							label);
		};
	}

	default Packet model(
			final Property<ModelCesiumWriter> model ) {
		return writer -> {
			write(writer);
			writeAndClose(	writer.openModelProperty(),
							model);
		};
	}

	default Packet path(
			final Property<PathCesiumWriter> path ) {
		return writer -> {
			write(writer);
			writeAndClose(	writer.openPathProperty(),
							path);
		};
	}

	default Packet point(
			final Property<PointCesiumWriter> point ) {
		return writer -> {
			write(writer);
			writeAndClose(	writer.openPointProperty(),
							point);
		};
	}

	default Packet polygon(
			final Property<PolygonCesiumWriter> polygon ) {
		return writer -> {
			write(writer);
			writeAndClose(	writer.openPolygonProperty(),
							polygon);
		};
	}

	default Packet polyline(
			final Property<PolylineCesiumWriter> polyline ) {
		return writer -> {
			write(writer);
			writeAndClose(	writer.openPolylineProperty(),
							polyline);
		};
	}

	// TODO Rectangle
	// TODO Wall
	// TODO ConicSensor
	// TODO CustomPatternSensor
	// TODO RectangularSensor
	// TODO Fan
	// TODO agi_Vector

	// NON Cesium MAXAR Visualization pieces

	default Packet layerControl(
			LayerControl layerControl ) {
		return writer -> {
			write(writer);
			layerControl.write(writer.openLayerControl());
			writer.closeProperty();
		};
	}

	default Packet timelineControl(
			TimelineControl timelineControl ) {
		return writer -> {
			write(writer);
			timelineControl.write(writer.openTimelineControl());
			writer.closeProperty();
		};
	}

	default Packet timelineControl(
			String timelinePacket ) {
		return writer -> {
			write(writer);
			writer.writeProperty(	PacketWriter.TIMELINE_CONTROL_PROPETY_NAME,
									timelinePacket);
			writer.closeProperty();
		};
	}
}
