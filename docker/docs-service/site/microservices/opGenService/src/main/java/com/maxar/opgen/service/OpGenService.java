package com.maxar.opgen.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.joda.time.DateTime;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;
import org.locationtech.jts.io.WKTWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.maxar.asset.common.exception.SensorModeNameDoesNotExistException;
import com.maxar.common.exception.BadRequestException;
import com.maxar.common.utils.GeoUtils;
import com.maxar.opgen.model.Op;
import com.maxar.opgen.model.OpBeam;
import com.maxar.opgen.model.OpRequest;
import com.maxar.opgen.types.DummyValidAccess;
import com.maxar.opgen.types.TargetRequirement;
import com.radiantblue.analytics.core.Context;
import com.radiantblue.analytics.geodesy.geometry.GeodeticGeometry;
import com.radiantblue.analytics.isr.core.component.accgen.IAccess;
import com.radiantblue.analytics.isr.core.component.accgen.IAccessRequest;
import com.radiantblue.analytics.isr.core.component.accgen.request.SimpleAccessRequest;
import com.radiantblue.analytics.isr.core.component.opgen.ModeAccessTime;
import com.radiantblue.analytics.isr.core.model.asset.Asset;
import com.radiantblue.analytics.isr.core.model.payload.IPayload;
import com.radiantblue.analytics.isr.core.model.sensor.ISensor;
import com.radiantblue.analytics.isr.core.model.sensormode.ISensorMode;
import com.radiantblue.analytics.isr.core.op.IBeam;
import com.radiantblue.analytics.isr.core.op.IModeOp;
import com.radiantblue.analytics.isr.core.op.InvalidModeOp;
import com.radiantblue.analytics.mechanics.IStateVectorProvider;

import scala.collection.immutable.HashMap;

@Component
public class OpGenService
{
	@Value("${microservices.opgen.accessRequestDurationMinutes:900000}")
	private int accessRequestDurationMinutes;

	@Value("${microservices.opgen.samplingInterval_ms:1000}")
	private int samplingIntervalMillis;

	public List<Op> createOpAtTime(
			final OpRequest opRequest,
			final Asset asset )
			throws SensorModeNameDoesNotExistException {

		final List<Op> ops = new ArrayList<>();
		final Op op = new Op();
		op.setValid(false);

		// Make sure we have a legit asset
		if (asset == null) {
			op.setReason("Asset '" + opRequest.getAssetName() + "' was not found");
			ops.add(op);
			return ops;
		}

		// Find the requested sensor mode and return an error if invalid
		final ISensorMode mode = getSensorMode(	asset,
												opRequest.getSensorModeName());

		if (mode == null) {
			throw new SensorModeNameDoesNotExistException(
					opRequest.getSensorModeName(),
					opRequest.getAssetName());
		}

		// make sure we have a legit target
		Geometry geometry = null;
		try {
			final WKTReader reader = new WKTReader();
			geometry = reader.read(opRequest.getTargetGeometryWkt());
		}
		catch (final ParseException e) {
			throw new BadRequestException(
					"Unable to parse geometry WKT: " + opRequest.getTargetGeometryWkt());
		}

		// Create an ImintRequirement since it is an IStateVectorProvider
		final Geometry geometryInRadians = GeoUtils.convertDegreesToRadians(geometry);
		final IStateVectorProvider tgt = new TargetRequirement(
				"TargetRequirement",
				GeodeticGeometry.create(geometryInRadians));

		// Create an access request so we can create our dummy access
		final DateTime accessRequestStartTime = opRequest.getStartTime();
		DateTime accessRequestEndTime = opRequest.getEndTime();
		if (accessRequestEndTime == null) {
			accessRequestEndTime = accessRequestStartTime;
		}
		accessRequestEndTime = accessRequestEndTime.plusMinutes(accessRequestDurationMinutes);

		final IAccessRequest accReq = new SimpleAccessRequest(
				accessRequestStartTime,
				accessRequestEndTime,
				asset,
				tgt,
				Collections.emptyList(),
				Collections.emptyList());

		// Create a Dummy Valid Access for Opgen
		final IAccess dummyAccess = new DummyValidAccess(
				accessRequestStartTime,
				accessRequestEndTime,
				accReq);

		final DateTime opRequestStartTime = opRequest.getStartTime();
		DateTime opRequestEndTime = opRequest.getEndTime();
		if (opRequestEndTime == null) {
			opRequestEndTime = opRequestStartTime;
		}

		int sampleMillis = opRequest.getOpSampleTime_ms();
		if (sampleMillis == 0) {
			sampleMillis = samplingIntervalMillis;
		}

		// Try to create ops over the time range
		for (DateTime attemptTime = opRequestStartTime; attemptTime.getMillis() <= opRequestEndTime
				.getMillis(); attemptTime = attemptTime.plusMillis(sampleMillis)) {
			final Op opSample = new Op();
			opSample.setAssetName(asset.getName());
			opSample.setSensorType(mode.sensor()
					.getSensorType());
			opSample.setStartTime(attemptTime);
			opSample.setValid(false);
			final ModeAccessTime modeAccessTime = new ModeAccessTime(
					attemptTime,
					dummyAccess,
					mode);
			final Context context = new Context(
					new HashMap<>());
			final IModeOp modeOp = mode.createOp(	modeAccessTime,
													context);
			if (!modeOp.isValid()) {
				if (modeOp instanceof InvalidModeOp) {
					final InvalidModeOp invalid = (InvalidModeOp) modeOp;
					opSample.setReason(invalid.reason());
				}
				else {
					opSample.setReason("Mode op is invalid but not an InvalidModeOp");
				}
			}
			else {
				// Return the successful Op
				opSample.setStartTime(modeOp.startTime());
				opSample.setEndTime(modeOp.endTime());
				opSample.setValid(modeOp.isValid());
				final List<OpBeam> beams = convertModeOpBeams(modeOp.beams());

				opSample.setBeams(beams);
			}
			ops.add(opSample);
		}

		return ops;
	}

	private List<OpBeam> convertModeOpBeams(
			final List<IBeam> modeOpBeams ) {
		final List<OpBeam> beams = new ArrayList<>();
		final WKTWriter writer = new WKTWriter();
		for (final IBeam beam : modeOpBeams) {
			if (beam.polygon() != null) {
				final OpBeam opBeam = new OpBeam();
				opBeam.setStartTime(beam.getStartTime());
				opBeam.setEndTime(beam.getEndTime());
				opBeam.setGeometryWkt(writer.writeFormatted(beam.polygon()
						.jtsGeometry_deg()));
				beams.add(opBeam);
			}
		}
		return beams;
	}

	private ISensorMode getSensorMode(
			final Asset asset,
			final String sensorModeName ) {
		ISensorMode mode = null;

		final List<IPayload> payloads = asset.getPayloads();
		for (final IPayload payload : payloads) {
			final List<ISensor> sensors = payload.sensors();
			for (final ISensor sensor : sensors) {
				final List<ISensorMode> modes = sensor.modes();
				for (final ISensorMode curMode : modes) {
					if (curMode.getName()
							.equals(sensorModeName)) {
						mode = curMode;
						break;
					}
				}
			}
		}
		return mode;
	}
}
