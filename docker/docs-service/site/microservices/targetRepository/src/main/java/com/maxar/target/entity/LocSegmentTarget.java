package com.maxar.target.entity;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.joda.time.DateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.maxar.target.model.LocSegmentTargetModel;
import com.maxar.target.model.TargetModel;
import com.maxar.target.model.TargetType;
import com.radiantblue.analytics.core.measures.Angle;
import com.radiantblue.analytics.core.measures.Length;
import com.radiantblue.analytics.geodesy.GeodeticPoint;
import com.radiantblue.analytics.geodesy.geometry.GeodeticLineString;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "locsegmenttarget")
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper=true)
public class LocSegmentTarget extends
		Target implements
		Comparable<LocSegmentTarget>
{
	private static final long serialVersionUID = 1L;

	protected int num;
	@Column(name = "startlatdeg")
	protected double startLatDeg;
	@Column(name = "startlondeg")
	protected double startLonDeg;
	@Column(name = "startelevationft")
	protected double startElevationFt;
	@Column(name = "endlatdeg")
	protected double endLatDeg;
	@Column(name = "endlondeg")
	protected double endLonDeg;
	@Column(name = "endelevationft")
	protected double endElevationFt;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "parentloctargetkey", referencedColumnName = "targetkey")
	@JsonIgnore
	@EqualsAndHashCode.Exclude
	protected LocTarget parentLoc;

	public LocSegmentTarget(
			final LocSegmentTarget seg ) {
		super(
				seg);
		parentLoc = seg.parentLoc;
		num = seg.num;
		startLatDeg = seg.startLatDeg;
		startLonDeg = seg.startLonDeg;
		startElevationFt = seg.startElevationFt;
		endLatDeg = seg.endLatDeg;
		endLonDeg = seg.endLonDeg;
		endElevationFt = seg.endElevationFt;
	}

	public LocSegmentTarget(
			final LocTarget parentLoc,
			final String id,
			final String name,
			final int num,
			final GeodeticPoint start,
			final GeodeticPoint end ) {
		super(
				TargetType.LOC,
				id,
				name,
				null, // description
				"ZZ", // country code
				"ZZ", // geo region
				null); // order of battle
		this.parentLoc = parentLoc;
		this.num = num;
		startLatDeg = start.latitude()
				.degrees();
		startLonDeg = start.longitude()
				.degrees();
		startElevationFt = start.altitude()
				.ft();
		endLatDeg = end.latitude()
				.degrees();
		endLonDeg = end.longitude()
				.degrees();
		endElevationFt = end.altitude()
				.ft();

		updateGeometry();
	}

	public LocTarget toLocTarget() {
		final Set<LocSegmentTarget> seg = new HashSet<>();
		seg.add(this);

		return new LocTarget(
				targetId,
				targetName,
				seg);
	}

	@Override
	public int compareTo(
			final LocSegmentTarget ls ) {
		return num - ls.num;
	}

	@Override
	public String toString() {
		return String.format(	"LOC segment %d: %s | %s",
								num,
								targetId,
								targetName);
	}

	protected void updateGeometry() {
		final GeodeticPoint[] points = new GeodeticPoint[2];

		points[0] = GeodeticPoint.fromLatLonAlt(Angle.fromDegrees(startLatDeg),
												Angle.fromDegrees(startLonDeg),
												Length.fromFeet(startElevationFt));
		points[1] = GeodeticPoint.fromLatLonAlt(Angle.fromDegrees(endLatDeg),
												Angle.fromDegrees(endLonDeg),
												Length.fromFeet(endElevationFt));

		geometry = GeodeticLineString.create(points)
				.jtsGeometry_deg();
	}
	
	@Override
	public TargetModel toModel(DateTime czmlStartTime, DateTime czmlStopTime) {
		return LocSegmentTargetModel.locSegmentBuilder()
				.targetType(targetType)
				.targetId(targetId)
				.targetName(targetName)
				.description(description)
				.countryCode(countryCode)
				.geoRegion(geoRegion)
				.orderOfBattle(orderOfBattle)
				.geometry(geometry)
				.num(num)
				.startLatDeg(startLatDeg)
				.startLonDeg(startLonDeg)
				.startElevationFt(startElevationFt)
				.endLatDeg(endLatDeg)
				.endLonDeg(endLonDeg)
				.endElevationFt(endElevationFt)
				.czmlStartTime(czmlStartTime)
				.czmlStopTime(czmlStopTime)
				.build();
	}
}
