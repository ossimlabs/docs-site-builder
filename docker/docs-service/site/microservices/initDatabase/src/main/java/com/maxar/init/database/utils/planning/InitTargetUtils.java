package com.maxar.init.database.utils.planning;

import java.util.List;

import org.locationtech.jts.geom.Geometry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.maxar.target.entity.Target;
import com.maxar.target.repository.TargetRepository;
import com.radiantblue.analytics.core.measures.Angle;
import com.radiantblue.analytics.geodesy.GeodeticPoint;
import com.radiantblue.analytics.geodesy.geometry.GeodeticPolygon;

@Component
public class InitTargetUtils
{
	@Autowired
	private TargetRepository targetRepository;

	@Value("${microservices.initdatabase.targetsearch.minlatitude}")
	private double minLatitudeDegrees;

	@Value("${microservices.initdatabase.targetsearch.maxlatitude}")
	private double maxLatitudeDegrees;

	@Value("${microservices.initdatabase.targetsearch.minlongitude}")
	private double minLongitudeDegrees;

	@Value("${microservices.initdatabase.targetsearch.maxlongitude}")
	private double maxLongitudeDegrees;

	public List<Target> getInitTargets() {
		// Get all targets for properties polygon
		final Angle minLat = Angle.fromDegrees(minLatitudeDegrees);
		final Angle maxLat = Angle.fromDegrees(maxLatitudeDegrees);
		final Angle minLon = Angle.fromDegrees(minLongitudeDegrees);
		final Angle maxLon = Angle.fromDegrees(maxLongitudeDegrees);

		final GeodeticPoint ul = GeodeticPoint.fromLatLon(	maxLat,
															minLon);
		final GeodeticPoint ur = GeodeticPoint.fromLatLon(	maxLat,
															maxLon);
		final GeodeticPoint lr = GeodeticPoint.fromLatLon(	minLat,
															maxLon);
		final GeodeticPoint ll = GeodeticPoint.fromLatLon(	minLat,
															minLon);

		final List<GeodeticPoint> gps = List.of(ul,
												ur,
												lr,
												ll);
		final Geometry geom = GeodeticPolygon.create(gps)
				.jtsGeometry_deg();

		return targetRepository.findByGeometry(geom);
	}

}
