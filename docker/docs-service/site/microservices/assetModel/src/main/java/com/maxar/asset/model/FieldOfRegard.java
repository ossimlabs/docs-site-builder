package com.maxar.asset.model;

import java.awt.Color;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.UUID;

import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;

import com.maxar.asset.model.czml.FieldOfRegardCzmlProperties;
import com.maxar.cesium.czmlwriter.Packet;
import com.maxar.cesium.czmlwriter.packet.Polygon;
import com.maxar.cesium.czmlwriter.positionlist.PositionList;
import com.maxar.cesium.czmlwriter.property.Property;
import com.maxar.cesium.czmlwriter.refvalue.ColorRefValue;
import com.maxar.cesium.czmlwriter.refvalue.DoubleRefValue;
import com.maxar.cesium.czmlwriter.types.PropertyInterval;

import cesiumlanguagewriter.MaterialCesiumWriter;
import cesiumlanguagewriter.SolidColorMaterialCesiumWriter;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

/**
 * The response object for an asset's fields of regard angles.
 */
@ApiModel
public class FieldOfRegard
{
	/** The field of regard angle of the sensor in degrees. */
	@ApiModelProperty(required = true, example = "POLYGON ((" + "159.3090548375059 32.60713228311288, "
			+ "158.51878457702375 32.65732940481734, " + "157.7415942078348 32.5277404042514, "
			+ "157.03480578819907 32.22801686853407, " + "156.44929444627005 31.78028961961575, "
			+ "156.02526841916497 31.217134590836025, " + "155.78955838301806 30.578745193581724, "
			+ "155.7545156106752 29.909699262362146, " + "155.9182911453107 29.255674833484978, "
			+ "156.26610730108888 28.660377374446526, " + "156.77213228226364 28.162840167779432, "
			+ "157.4016627537739 27.795182073928903, " + "158.11343548631396 27.580862458625578, "
			+ "158.86198157084146 27.533454324215743, " + "159.59999098545677 27.655950213019718, "
			+ "160.28067882943833 27.94061057410618, " + "160.86015122015843 28.369356941366373, "
			+ "161.2997671917343 28.914704021034108, " + "161.56848035698792 29.54121860790338, "
			+ "161.64510857069843 30.20748858623372, " + "161.52040861152943 30.86857498325914, "
			+ "161.19872638692354 31.47889143904729, " + "160.6988790910016 31.9953964930431, "
			+ "160.0538640540894 32.38089367948142, " + "160.0538640540894 32.38089367948142, "
			+ "159.3090548375059 32.60713228311288" + "))", notes = "The field of regard angle of the asset in degrees")
	@Getter
	@Setter
	private String fieldOfRegardAngleWkt;

	@Getter
	@Setter
	private String fieldOfRegardName;

	@Getter
	@Setter
	private String sensorType;

	@Getter
	@Setter
	private DateTime fieldOfRegardAtTime;

	private Color getColorFromSensorType(
			final FieldOfRegardCzmlProperties properties ) {
		Color color = new Color(
				new BigInteger(
						properties.getColor(),
						16).intValue(),
				true);
		if (sensorType.equalsIgnoreCase("EO")) {
			color = new Color(
					new BigInteger(
							properties.getEoColor(),
							16).intValue(),
					true);
		}
		else if (sensorType.equalsIgnoreCase("RADAR")) {
			color = new Color(
					new BigInteger(
							properties.getRadarColor(),
							16).intValue(),
					true);
		}
		else if (sensorType.equalsIgnoreCase("IR")) {
			color = new Color(
					new BigInteger(
							properties.getIrColor(),
							16).intValue(),
					true);
		}
		return color;
	}

	public String produceCzml(
			final String parentId,
			final FieldOfRegardCzmlProperties properties ) {
		final String id = UUID.randomUUID()
				.toString();
		final WKTReader reader = new WKTReader();

		Geometry fieldOfRegardGeo = null;

		try {
			fieldOfRegardGeo = reader.read(fieldOfRegardAngleWkt);
		}
		catch (final ParseException e) {
			e.printStackTrace();
		}

		final Interval forInterval = new Interval(
				fieldOfRegardAtTime,
				fieldOfRegardAtTime.plusMillis(properties.getForDurationMillis()));

		Packet packet = Packet.create()
				.id(id)
				.name(fieldOfRegardName)
				.availability(	forInterval.getStart(),
								forInterval.getEnd());

		if (parentId != null) {
			packet = packet.parent(parentId);
		}

		final Color forColor = getColorFromSensorType(properties);

		final Polygon fieldOfRegardPolygon = Polygon.create()
				.material(new Property<MaterialCesiumWriter>() {
					@Override
					public void write(
							final MaterialCesiumWriter writer ) {
						final SolidColorMaterialCesiumWriter colorWriter = writer.openSolidColorProperty();
						colorWriter.writeColorProperty(forColor);
						colorWriter.close();
					}
				})
				.outlineColor(ColorRefValue.color(new Color(
						new BigInteger(
								properties.getOutlineColor(),
								16).intValue(),
						true)))
				.outlineWidth(DoubleRefValue.number(properties.getOutlineWidth()))
				.positions(Property.interval(Arrays.asList(new PropertyInterval<PositionList>(
						forInterval,
						PositionList.geometry(fieldOfRegardGeo)))));

		return packet.polygon(fieldOfRegardPolygon)
				.writeString();
	}
}
