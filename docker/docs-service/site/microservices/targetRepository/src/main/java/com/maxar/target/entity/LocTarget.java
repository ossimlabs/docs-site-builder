package com.maxar.target.entity;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.joda.time.DateTime;

import com.maxar.target.model.LocSegmentTargetModel;
import com.maxar.target.model.LocTargetModel;
import com.maxar.target.model.TargetModel;
import com.maxar.target.model.TargetType;
import com.radiantblue.analytics.core.measures.Angle;
import com.radiantblue.analytics.core.measures.Length;
import com.radiantblue.analytics.geodesy.GeodeticPoint;
import com.radiantblue.analytics.geodesy.geometry.GeodeticLineString;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Table(name = "loctarget")
@Data
@EqualsAndHashCode(callSuper = true)
public class LocTarget extends
		Target
{
	private static final long serialVersionUID = 1L;

	@OneToMany(fetch = FetchType.EAGER, mappedBy = "parentLoc", cascade = CascadeType.ALL)
	protected Set<LocSegmentTarget> segments;

	public LocTarget() {
		super(
				TargetType.LOC);
	}

	public LocTarget(
			final LocTarget loc ) {
		super(
				loc);
		segments = loc.segments;
	}

	public LocTarget(
			final String targetId,
			final String targetName,
			final Set<LocSegmentTarget> segments ) {
		super(
				TargetType.LOC,
				targetId,
				targetName,
				null, // description
				"ZZ", // country code
				"ZZ", // geo region
				null); // order of battle
		this.segments = segments;

		updateGeometry();
	}

	@Override
	public String toString() {
		return String.format(	"LOC: %6s %d %s",
								targetId,
								segments.size(),
								targetName);
	}

	public void updateGeometry() {
		if ((segments == null) || (segments.size() == 0)) {
			return;
		}

		// TODO: USE LIST: Sort segments by order
		final LocSegmentTarget[] sorted = segments.toArray(new LocSegmentTarget[segments.size()]);
		Arrays.sort(sorted);

		// TODO: For now assuming segments are contiguous -
		// only using "start" points of each segment until the last one
		final GeodeticPoint[] points = new GeodeticPoint[segments.size() + 1];

		LocSegmentTarget last = null;
		int i = 0;
		for (final LocSegmentTarget segment : sorted) {
			points[i++] = GeodeticPoint.fromLatLonAlt(	Angle.fromDegrees(segment.startLatDeg),
														Angle.fromDegrees(segment.startLonDeg),
														Length.fromFeet(segment.startElevationFt));
			last = segment;
		}
		// add final segment end point
		points[i++] = GeodeticPoint.fromLatLonAlt(	Angle.fromDegrees(last.endLatDeg),
													Angle.fromDegrees(last.endLonDeg),
													Length.fromFeet(last.endElevationFt));

		geometry = GeodeticLineString.create(points)
				.jtsGeometry_deg();
	}

	@Override
	public TargetModel toModel(
			final DateTime czmlStartTime,
			final DateTime czmlStopTime ) {
		final Set<LocSegmentTargetModel> modelSegments = segments.stream()
				.map(LocSegmentTarget -> LocSegmentTarget.toModel(	czmlStartTime,
																	czmlStopTime))
				.map(LocSegmentTargetModel.class::cast)
				.collect(Collectors.toSet());

		return LocTargetModel.locBuilder()
				.targetType(targetType)
				.targetId(targetId)
				.targetName(targetName)
				.description(description)
				.countryCode(countryCode)
				.geoRegion(geoRegion)
				.orderOfBattle(orderOfBattle)
				.geometry(geometry)
				.segments(modelSegments)
				.czmlStartTime(czmlStartTime)
				.czmlStopTime(czmlStopTime)
				.build();
	}
}
