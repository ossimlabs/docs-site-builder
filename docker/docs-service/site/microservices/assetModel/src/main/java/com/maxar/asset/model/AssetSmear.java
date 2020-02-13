package com.maxar.asset.model;

import java.awt.Color;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.maxar.asset.model.czml.AssetSmearCzmlProperties;
import com.maxar.cesium.czmlwriter.Packet;
import com.maxar.cesium.czmlwriter.material.PolylineMaterial;
import com.maxar.cesium.czmlwriter.material.PolylineOutlineMaterial;
import com.maxar.cesium.czmlwriter.packet.Path;
import com.maxar.cesium.czmlwriter.packet.Polygon;
import com.maxar.cesium.czmlwriter.packet.Position;
import com.maxar.cesium.czmlwriter.packet.TimelineControl;
import com.maxar.cesium.czmlwriter.positionlist.PositionList;
import com.maxar.cesium.czmlwriter.property.Property;
import com.maxar.cesium.czmlwriter.refvalue.BooleanRefValue;
import com.maxar.cesium.czmlwriter.refvalue.ColorRefValue;
import com.maxar.cesium.czmlwriter.refvalue.DoubleRefValue;
import com.maxar.cesium.czmlwriter.types.PropertyInterval;
import com.maxar.cesium.czmlwriter.types.TimeTaggedValue;
import com.radiantblue.analytics.core.DateTimeFactory;
import com.radiantblue.analytics.geodesy.EarthCenteredFrame;
import com.radiantblue.analytics.geodesy.GeodeticPoint;
import com.radiantblue.analytics.geodesy.LatLonAlt;
import com.radiantblue.analytics.isr.core.model.asset.IAsset;

import cesiumlanguagewriter.MaterialCesiumWriter;
import cesiumlanguagewriter.SolidColorMaterialCesiumWriter;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

/**
 * The response object for an asset's smear over a specified time range (i.e.
 * collection window start/end) with optional beams.
 */
@Getter
@Setter
@ApiModel
public class AssetSmear
{
	/** The smear angle of the sensor in degrees. */
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
			+ "159.3090548375059 32.60713228311288" + "))", notes = "The smear angle of the asset in degrees")
	private String smearAngleWkt;
	private String smearName;
	private String sensorType;
	private DateTime startTime;
	private DateTime stopTime;
	private List<OpBeam> opBeams;
	private List<FieldOfRegard> forFrames = new ArrayList<>();
	private boolean smearCzmlRequested;
	private boolean forFramesCzmlRequested;
	private boolean opBeamsCzmlRequested;
	private Integer forFrameIncrementSec;

	@JsonIgnore
	private IAsset asset;

	private Color getColorFromSensorTypeAndAlpha(
			final AssetSmearCzmlProperties properties,
			final String alpha ) {
		Color color = new Color(
				new BigInteger(
						alpha.concat(properties.getColor()),
						16).intValue(),
				true);
		if (sensorType.equalsIgnoreCase("EO")) {
			color = new Color(
					new BigInteger(
							alpha.concat(properties.getEoColor()),
							16).intValue(),
					true);
		}
		else if (sensorType.equalsIgnoreCase("RADAR")) {
			color = new Color(
					new BigInteger(
							alpha.concat(properties.getRadarColor()),
							16).intValue(),
					true);
		}
		else if (sensorType.equalsIgnoreCase("IR")) {
			color = new Color(
					new BigInteger(
							alpha.concat(properties.getIrColor()),
							16).intValue(),
					true);
		}
		return color;
	}

	private Color getSmearColorFromSensorType(
			final AssetSmearCzmlProperties properties ) {
		return getColorFromSensorTypeAndAlpha(	properties,
												properties.getSmearAlpha());
	}

	private Color getForColorFromSensorType(
			final AssetSmearCzmlProperties properties ) {
		return getColorFromSensorTypeAndAlpha(	properties,
												properties.getForAlpha());
	}

	public String produceCzml(
			final String parentId,
			final AssetSmearCzmlProperties properties ) {
		final List<String> czmlStringList = new ArrayList<>();
		final WKTReader reader = new WKTReader();

		// determine color to use based on sensor type
		final Color smearColor = getSmearColorFromSensorType(properties);
		final Color forColor = getForColorFromSensorType(properties);

		if (smearCzmlRequested) {
			Geometry smearGeo = null;

			try {
				smearGeo = reader.read(smearAngleWkt);
			}
			catch (final ParseException e) {
				e.printStackTrace();
			}

			final boolean smearCrossesDateline = (smearGeo.getEnvelope()
					.getCoordinates()[0].getX() == -180.0)
					|| (smearGeo.getEnvelope()
							.getCoordinates()[2].getX() == 180.0);
			
			String id = smearName + "-" + startTime.toString();
			if(smearCrossesDateline) {
				id += "-Dateline Smear";
			}else {
				id += "-Smear";
			}

			Packet packet = Packet.create()
					.id(id)
					.name(id);

			if (parentId != null) {
				packet = packet.parent(parentId);
			}

			if (properties.isDisplaySmearOnTimeline()) {
				packet = packet.timelineControl(TimelineControl.create()
						.start(DateTimeFactory.fromMillis(startTime.getMillis()))
						.end(DateTimeFactory.fromMillis(stopTime.getMillis()))
						.group(asset.getName() + " Smear"));
			}

			final Interval smearInterval = new Interval(
					startTime.minusSeconds(properties.getSmearLeadTimeSec()),
					stopTime.plusSeconds(properties.getSmearTrailTimeSec()));

			final Polygon smearPolygon = Polygon.create()
					.material(new Property<MaterialCesiumWriter>() {
						@Override
						public void write(
								final MaterialCesiumWriter writer ) {
							final SolidColorMaterialCesiumWriter colorWriter = writer.openSolidColorProperty();
							colorWriter.writeColorProperty(smearColor);
							colorWriter.close();
						}
					})
					.outline(BooleanRefValue.booleanValue(true))
					.fill(BooleanRefValue.booleanValue(!smearCrossesDateline))
					.outlineColor(ColorRefValue.color(smearColor))
					.outlineWidth(DoubleRefValue.number(properties.getSmearOutlineWidth()))
					.positions(Property.interval(Arrays.asList(new PropertyInterval<PositionList>(
							smearInterval,
							PositionList.geometry(smearGeo)))));

			czmlStringList.add(packet.polygon(smearPolygon)
					.writeString());
		}

		if (forFramesCzmlRequested) {
			// FOR frames parent packet
			final String forFramesPacketId = smearName + "-" + startTime.toString() + "-FOR";
			final String forFramesPacketIdDateLine = smearName + "-" + startTime.toString() + "-Dateline FOR";

			// show timeline for for frames if no smear is shown. Since the FOR frame
			// packets don't have a common parent, just need to add to first packet
			boolean showFORTimeline = !smearCzmlRequested && properties.isDisplaySmearOnTimeline();

			// FOR frames for animation
			for (final FieldOfRegard singleForFrame : forFrames) {
				Geometry singleForFrameGeo = null;

				try {
					singleForFrameGeo = reader.read(singleForFrame.getFieldOfRegardAngleWkt());
				}
				catch (final ParseException e) {
					e.printStackTrace();
				}

				final boolean frameCrossesDateline = (singleForFrameGeo.getEnvelope()
						.getCoordinates()[0].getX() == -180.0)
						|| (singleForFrameGeo.getEnvelope()
								.getCoordinates()[2].getX() == 180.0);

				Packet forFramePacket;
				if(frameCrossesDateline) {
					forFramePacket = Packet.create()
						.id(forFramesPacketIdDateLine)
						.name(forFramesPacketIdDateLine);
				}
				else {
					forFramePacket = Packet.create()
						.id(forFramesPacketId)
						.name(forFramesPacketId);
				}

				if (parentId != null) {
					forFramePacket = forFramePacket.parent(parentId);
				}

				if (showFORTimeline) {
					forFramePacket = forFramePacket.timelineControl(TimelineControl.create()
							.start(DateTimeFactory.fromMillis(startTime.getMillis()))
							.end(DateTimeFactory.fromMillis(stopTime.getMillis()))
							.group(asset.getName() + " FOR Frames"));

					// only need timeline for first FOR packet
					showFORTimeline = false;
				}

				final Interval frameInterval = new Interval(
						singleForFrame.getFieldOfRegardAtTime(),
						singleForFrame.getFieldOfRegardAtTime()
								.plusSeconds(forFrameIncrementSec));

				final Polygon forAnimationPolygon = Polygon.create()
						.material(new Property<MaterialCesiumWriter>() {
							@Override
							public void write(
									final MaterialCesiumWriter writer ) {
								final SolidColorMaterialCesiumWriter colorWriter = writer.openSolidColorProperty();
								colorWriter.writeColorProperty(forColor);
								colorWriter.close();
							}
						})
						.outline(BooleanRefValue.booleanValue(true))
						.fill(BooleanRefValue.booleanValue(!frameCrossesDateline))
						.outlineColor(ColorRefValue.color(forColor))
						.outlineWidth(DoubleRefValue.number(properties.getForFrameOutlineWidth()))
						.positions(Property.interval(Arrays.asList(new PropertyInterval<>(
								frameInterval,
								PositionList.geometry(singleForFrameGeo)))));

				czmlStringList.add(forFramePacket.polygon(forAnimationPolygon)
						.writeString());
			}
		}

		if (opBeamsCzmlRequested) {
			if ((opBeams != null) && !opBeams.isEmpty()) {
				// create entire asset path
				final List<TimeTaggedValue<LatLonAlt>> positions = new ArrayList<>();
				DateTime atTime = new DateTime(
						startTime);

				while (atTime.isBefore(stopTime)) {
					GeodeticPoint pos = asset.getStateVectors(	atTime,
																EarthCenteredFrame.ECEF)
							.geodeticPosition();
					LatLonAlt lla = new LatLonAlt(
							pos.latitude(),
							pos.longitude(),
							pos.altitude());
					positions.add(new TimeTaggedValue<>(
							atTime,
							lla));

					atTime = atTime.plusMillis(properties.getAssetSamplingMillis());

					// Add the last moment of the timeframe as the final sample
					if (!atTime.isBefore(stopTime)) {
						atTime = stopTime;
						pos = asset.getStateVectors(atTime,
													EarthCenteredFrame.ECEF)
								.geodeticPosition();
						lla = new LatLonAlt(
								pos.latitude(),
								pos.longitude(),
								pos.altitude());
						positions.add(new TimeTaggedValue<>(
								atTime,
								lla));
					}
				}

				final String assetId = UUID.randomUUID()
						.toString();

				Packet assetPacket = Packet.create()
						.id(assetId)
						.name(asset.getName() + "-Beams")
						.parent(parentId)
						.path(Path.create()
								.leadTime(DoubleRefValue.number(0.0))
								.trailTime(DoubleRefValue.number(properties.getAssetTrailTimeSec()))
								.material(PolylineMaterial.create()
										.polylineOutline(PolylineOutlineMaterial.create()
												.outlineWidth(DoubleRefValue
														.number(properties.getAssetOutlineWidth())))))
						.position(Position.cartographicDegrees(positions));

				if (parentId != null) {
					assetPacket = assetPacket.parent(parentId);
				}

				czmlStringList.add(assetPacket.writeString());

				int beamCount = 1;

				for (final OpBeam opBeam : opBeams) {
					czmlStringList.add(opBeam.produceCzml(	parentId,
															asset.getName(),
															assetId,
															beamCount++,
															properties,
															forColor));
				}
			}
		}

		return String.join(	", ",
							czmlStringList);
	}
}
