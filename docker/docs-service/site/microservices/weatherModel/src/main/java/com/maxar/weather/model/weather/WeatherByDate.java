package com.maxar.weather.model.weather;

import java.awt.Color;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.ISODateTimeFormat;

import com.maxar.cesium.czmlwriter.Packet;
import com.maxar.cesium.czmlwriter.material.Material;
import com.maxar.cesium.czmlwriter.material.SolidColorMaterial;
import com.maxar.cesium.czmlwriter.packet.Polygon;
import com.maxar.cesium.czmlwriter.positionlist.PositionList;
import com.maxar.cesium.czmlwriter.refvalue.BooleanRefValue;
import com.maxar.cesium.czmlwriter.refvalue.ColorRefValue;
import com.maxar.cesium.czmlwriter.refvalue.DoubleRefValue;
import com.maxar.weather.model.weather.czml.WeatherByDateCzmlProperties;
import com.radiantblue.analytics.core.log.SourceLogger;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class WeatherByDate implements
		Comparable<WeatherByDate>
{
	private static Logger logger = SourceLogger.getLogger(new Object() {}.getClass()
			.getEnclosingClass()
			.getName());

	static {
		DateTimeZone.setDefault(DateTimeZone.UTC);
	}

	String atTimeISOFormat;
	List<WeatherByGeometry> weathers;
	String parentId;

	@Override
	public int compareTo(
			final WeatherByDate o ) {
		return ISODateTimeFormat.dateTimeParser()
				.parseDateTime(atTimeISOFormat)
				.compareTo(ISODateTimeFormat.dateTimeParser()
						.parseDateTime(o.getAtTimeISOFormat()));
	}

	public String produceCzml(
			WeatherByDateCzmlProperties properties,
			String parentId ) {
		if ((weathers == null) || weathers.isEmpty()) {
			return null;
		}
		DateTime availableTime;
		try {
			availableTime = ISODateTimeFormat.dateTimeParser()
					.parseDateTime(atTimeISOFormat);
		}
		catch (final Exception e) {
			logger.error("Cannot parse ISO8601 datetime String: " + atTimeISOFormat);
			return null;
		}
		DateTime unavailableTime = availableTime.plusSeconds(properties.getUnavailableTimeSeconds());
		List<Packet> packets = new ArrayList<>();
		String weatherParentId = UUID.randomUUID()
				.toString();

		Packet weatherParentPacket = Packet.create()
				.id(weatherParentId)
				.name("Weather-" + atTimeISOFormat)
				.availability(	availableTime,
								unavailableTime);

		if (parentId != null) {
			weatherParentPacket = weatherParentPacket.parent(parentId);
		}

		packets.add(weatherParentPacket);

		for (WeatherByGeometry weather : weathers) {
			Polygon weatherPolygon = Polygon.create()
					.material(Material.create()
							.solidColor(SolidColorMaterial.create()
									.solidColor(ColorRefValue.color(new Color(
											255,
											255,
											255,
											(int) (255 * weather.getCloudCoverPercent()))))))
					.outline(BooleanRefValue.booleanValue(true))
					.height(DoubleRefValue.number(0.0))
					.outlineColor(ColorRefValue.color(new Color(
							new BigInteger(
									properties.getOutlineColor(),
									16).intValue(),
							true)))
					.outlineWidth(DoubleRefValue.number(properties.getOutlineWidth()))
					.positions(PositionList.geometry(weather.getGeometry()));

			packets.add(Packet.create()
					.id(UUID.randomUUID()
							.toString())
					.parent(weatherParentId)
					.name("Weather-" + atTimeISOFormat + "-" + weather.getGeometry()
							.toString())
					.polygon(weatherPolygon)
					.availability(	availableTime,
									unavailableTime));
		}
		return packets.stream()
				.map(Packet::writeString)
				.collect(Collectors.joining(","));
	}
}
