package com.maxar.target.controller;

import static com.maxar.common.utils.PaginationParameterValidator.validatePageAndCountParameters;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.maxar.target.entity.BasTarget;
import com.maxar.target.entity.DsaTarget;
import com.maxar.target.entity.LocTarget;
import com.maxar.target.entity.PointTarget;
import com.maxar.target.entity.Target;
import com.maxar.target.exception.TargetQueryNoParametersException;
import com.maxar.target.model.BasTargetModel;
import com.maxar.target.model.DsaTargetModel;
import com.maxar.target.model.LocTargetModel;
import com.maxar.target.model.PointTargetModel;
import com.maxar.target.model.TargetModel;
import com.maxar.target.repository.BasTargetRepository;
import com.maxar.target.repository.DsaTargetRepository;
import com.maxar.target.repository.LocTargetRepository;
import com.maxar.target.repository.PointTargetRepository;
import com.maxar.target.repository.TargetRepository;
import com.radiantblue.analytics.core.log.SourceLogger;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@RestController
@Api(value = "/targetMS")
public class TargetController
{
	private static Logger logger = SourceLogger.getLogger(TargetController.class.getName());

	// Static strings for swagger examples
	private static final String EXAMPLE_WKT_STRING = "POLYGON((24.63719 29.79381, 29.79381 29.79381, 29.79381 24.63719,"
			+ "24.63719 24.63719, 24.63719 29.79381))";
	private static final String EXAMPLE_POINT_ID = "P000000001";
	private static final String EXAMPLE_DSA_ID = "D00001";
	private static final String EXAMPLE_LOC_ID = "L00001";
	private static final String EXAMPLE_BAS_ID = "B00001";
	private static final String EXAMPLE_POINT_COUNTRYCODE = "CI";
	private static final String EXAMPLE_DSA_LOC_BAS_COUNTRYCODE = "ZZ";
	private static final String EXAMPLE_WKT_FOR_ESTIMATED = "POINT(20.0 20.0)";

	@Value("${microservices.target.estimatedTargetName:ESTIMATED TARGET}")
	private String estimatedTargetName;

	@Value("${microservices.target.estimatedTargetMinorAxis:10.0}")
	private double estimatedTargetMinorAxisMeters;

	@Value("${microservices.target.estimatedTargetMajorAxis:10.0}")
	private double estimatedTargetMajorAxisMeters;

	@Value("${microservices.target.estimatedTargetNumPolyPoints:16}")
	private int estimatedTargetNumPolyPoints;

	@Autowired
	private TargetRepository targetRepository;

	@Autowired
	private PointTargetRepository pointTargetRepository;

	@Autowired
	private DsaTargetRepository dsaTargetRepository;

	@Autowired
	private LocTargetRepository locTargetRepository;

	@Autowired
	private BasTargetRepository basTargetRepository;

	@GetMapping("/targets/{id}")
	@ApiOperation("Gets the Target for a given id")
	@ApiResponses({
		@ApiResponse(code = 200, message = "The Target was successfully found"),
		@ApiResponse(code = 404, message = "No Target was found with given id")
	})
	public @ResponseBody ResponseEntity<TargetModel> getTargetById(
			@PathVariable
			@ApiParam(name = "id", value = "Target ID", example = EXAMPLE_POINT_ID)
			final String id,
			@RequestParam(required = false)
			@ApiParam(name = "czmlStart", value = "ISO8601 Formatted Date String")
			final String czmlStart,
			@RequestParam(required = false)
			@ApiParam(name = "czmlStop", value = "ISO8601 Formatted Date String")
			final String czmlStop ) {
		final Target target = targetRepository.findByTargetId(id);
		if (target == null) {
			return ResponseEntity.notFound()
					.build();
		}

		final DateTime czmlStartTime;
		final DateTime czmlStopTime;

		try {
			czmlStartTime = czmlStart != null ? ISODateTimeFormat.dateTimeParser()
					.parseDateTime(czmlStart) : null;

			czmlStopTime = czmlStop != null ? ISODateTimeFormat.dateTimeParser()
					.parseDateTime(czmlStop) : null;
		}
		catch (final Exception e) {
			logger.error("Cannot parse target czml ISO8601 datetime Strings: " + czmlStart + "/" + czmlStop);
			return new ResponseEntity<>(
					HttpStatus.BAD_REQUEST);
		}

		final TargetModel model = target.toModel(	czmlStartTime,
													czmlStopTime);

		return new ResponseEntity<>(
				model,
				HttpStatus.OK);
	}

	@GetMapping("/targetsbycc")
	@ApiOperation("Gets the Targets for a given country code")
	@ApiResponses({
		@ApiResponse(code = 200, message = "The Targets were successfully found"),
		@ApiResponse(code = 204, message = "No Targets were found for given country code"),
		@ApiResponse(code = 400, message = "The input page or count parameter was invalid")
	})
	public @ResponseBody ResponseEntity<List<TargetModel>> getTargetByCountryCode(
			@RequestParam
			@ApiParam(name = "cc", value = "Country Code", example = EXAMPLE_POINT_COUNTRYCODE)
			final String cc,
			@RequestParam
			@ApiParam(name = "page", value = "Page number", example = "0")
			final int page,
			@RequestParam
			@ApiParam(name = "count", value = "Result limit", example = "100")
			final int count ) {
		validatePageAndCountParameters(	page,
										count);

		final PageRequest pageRequest = PageRequest.of(	page,
														count,
														sortByTargetId());

		final List<Target> targets = targetRepository.findByCountryCode(cc,
																		pageRequest);

		if (targets.isEmpty()) {
			return new ResponseEntity<>(
					HttpStatus.NO_CONTENT);
		}

		final List<TargetModel> models = targets.stream()
				.map(Target::toModel)
				.collect(Collectors.toList());

		return new ResponseEntity<>(
				models,
				HttpStatus.OK);
	}

	@GetMapping("/targets/count")
	@ApiOperation("Counts the targets")
	@ApiResponses({
		@ApiResponse(code = 200, message = "The number of Targets was found")
	})
	public @ResponseBody ResponseEntity<Long> getNumberOfTargets(
			@RequestParam(required = false, defaultValue = "")
			@ApiParam(name = "cc", value = "Country Code", example = EXAMPLE_POINT_COUNTRYCODE)
			final String cc ) {
		long count;
		if (cc == null || cc.isEmpty()) {
			count = targetRepository.count();
		}
		else {
			count = targetRepository.countByCountryCode(cc);
		}
		return new ResponseEntity<>(
				count,
				HttpStatus.OK);
	}

	@GetMapping("/targetsbygeometry")
	@ApiOperation("Gets the Targets for a given geometry")
	@ApiResponses({
		@ApiResponse(code = 200, message = "The Targets were successfully found"),
		@ApiResponse(code = 204, message = "No Targets were found for given geometry"),
		@ApiResponse(code = 400, message = "The input geometry well-known text (WKT) could not be parsed")
	})
	public @ResponseBody ResponseEntity<List<TargetModel>> getTargetsByGeometry(
			@RequestParam
			@ApiParam(name = "geometry", value = "Geometry String using well known text (WKT)", example = EXAMPLE_WKT_STRING)
			final String geometry,
			@RequestParam(required = false)
			@ApiParam(name = "genEstimatedTarget", value = "Generate estimated target if not found")
			final boolean genEstimatedTarget,
			@RequestParam(required = false)
			@ApiParam(name = "czmlStart", value = "ISO8601 Formatted Date String")
			final String czmlStart,
			@RequestParam(required = false)
			@ApiParam(name = "czmlStop", value = "ISO8601 Formatted Date String")
			final String czmlStop ) {
		final DateTime czmlStartTime;
		final DateTime czmlStopTime;

		try {
			czmlStartTime = czmlStart != null ? ISODateTimeFormat.dateTimeParser()
					.parseDateTime(czmlStart) : null;

			czmlStopTime = czmlStop != null ? ISODateTimeFormat.dateTimeParser()
					.parseDateTime(czmlStop) : null;

		}
		catch (final Exception e) {
			logger.error("Cannot parse czml ISO8601 datetime Strings: " + czmlStart + "/" + czmlStop);
			return new ResponseEntity<>(
					HttpStatus.BAD_REQUEST);
		}

		final WKTReader reader = new WKTReader();
		Geometry geom;
		try {
			geom = reader.read(geometry);
		}
		catch (final ParseException e) {
			logger.error("Cannot parse WKT string: " + geometry);
			return new ResponseEntity<>(
					HttpStatus.BAD_REQUEST);
		}

		final List<Target> targets = targetRepository.findByGeometry(geom);

		List<TargetModel> models = null;
		if (targets.isEmpty()) {
			if (genEstimatedTarget) {
				final TargetModel model = PointTargetModel.generateEstimatedTarget(	geometry,
																					UUID.randomUUID()
																							.toString(),
																					estimatedTargetName,
																					estimatedTargetMinorAxisMeters,
																					estimatedTargetMajorAxisMeters,
																					estimatedTargetNumPolyPoints,
																					czmlStartTime,
																					czmlStopTime);
				if (model != null) {
					models = Collections.singletonList(model);
				}
			}
			else {
				return new ResponseEntity<>(
						HttpStatus.NO_CONTENT);
			}
		}
		else {
			models = targets.stream()
					.map(target -> target.toModel(	czmlStartTime,
													czmlStopTime))
					.collect(Collectors.toList());
		}

		return new ResponseEntity<>(
				models,
				HttpStatus.OK);
	}

	@GetMapping("/targets")
	@ApiOperation("Gets the Targets for a given geometry and/or countrycodes")
	@ApiResponses({
		@ApiResponse(code = 200, message = "The Targets were successfully found"),
		@ApiResponse(code = 204, message = "No Targets were found for given geometry"),
		@ApiResponse(code = 400, message = "The input geometry well-known text (WKT) could not be parsed")
	})
	public @ResponseBody ResponseEntity<List<TargetModel>> getTargets(
			@RequestParam(required = false)
			@ApiParam(name = "geometry", value = "Geometry String using well known text (WKT)", example = EXAMPLE_WKT_STRING)
			final String geometry,
			@RequestParam(required = false)
			@ApiParam(name = "cc", value = "List of two character countrycodes")
			final List<String> cc )
			throws TargetQueryNoParametersException {
		if ((geometry == null) && (cc == null)) {
			throw new TargetQueryNoParametersException(
					"geometry, cc");
		}
		final WKTReader reader = new WKTReader();
		Geometry geom = null;
		if (geometry != null) {
			try {
				geom = reader.read(geometry);
			}
			catch (final ParseException e) {
				logger.error("Cannot parse WKT string: " + geometry);
				return new ResponseEntity<>(
						HttpStatus.BAD_REQUEST);
			}
		}

		final List<Target> targets = targetRepository.findTargetByGeometryAndCountryCodeList(	geom,
																								cc);

		if (targets.isEmpty()) {
			return new ResponseEntity<>(
					HttpStatus.NO_CONTENT);
		}

		final List<TargetModel> models = targets.stream()
				.map(Target::toModel)
				.collect(Collectors.toList());

		return new ResponseEntity<>(
				models,
				HttpStatus.OK);
	}

	@GetMapping("/pointtargets/{id}")
	@ApiOperation("Gets the PointTarget for a given id")
	@ApiResponses({
		@ApiResponse(code = 200, message = "The PointTarget was successfully found"),
		@ApiResponse(code = 404, message = "No PointTarget was found with given id")
	})
	public @ResponseBody ResponseEntity<PointTargetModel> getPointTargetById(
			@PathVariable
			@ApiParam(name = "id", value = "Target ID", example = EXAMPLE_POINT_ID)
			final String id ) {
		final PointTarget target = pointTargetRepository.findByTargetId(id);
		if (target == null) {
			return ResponseEntity.notFound()
					.build();
		}

		final PointTargetModel model = (PointTargetModel) target.toModel();

		return new ResponseEntity<>(
				model,
				HttpStatus.OK);
	}

	@GetMapping("/pointtargets")
	@ApiOperation("Gets the PointTargets for a given country code")
	@ApiResponses({
		@ApiResponse(code = 200, message = "The PointTargets were successfully found"),
		@ApiResponse(code = 204, message = "No PointTargets were found for given country code"),
		@ApiResponse(code = 400, message = "The input page or count parameter was invalid")
	})
	public @ResponseBody ResponseEntity<List<PointTargetModel>> getPointTargetByCountryCode(
			@RequestParam
			@ApiParam(name = "cc", value = "Country Code", example = EXAMPLE_POINT_COUNTRYCODE)
			final String cc,
			@RequestParam
			@ApiParam(name = "page", value = "Page number", example = "0")
			final int page,
			@RequestParam
			@ApiParam(name = "count", value = "Result limit", example = "100")
			final int count ) {
		validatePageAndCountParameters(	page,
										count);

		final PageRequest pageRequest = PageRequest.of(	page,
														count,
														sortByTargetId());

		final List<PointTarget> targets = pointTargetRepository.findByCountryCode(	cc,
																					pageRequest);

		if (targets.isEmpty()) {
			return new ResponseEntity<>(
					HttpStatus.NO_CONTENT);
		}

		final List<PointTargetModel> models = targets.stream()
				.map(PointTarget::toModel)
				.map(PointTargetModel.class::cast)
				.collect(Collectors.toList());

		return new ResponseEntity<>(
				models,
				HttpStatus.OK);
	}

	@GetMapping("/pointtargets/count")
	@ApiOperation("Counts the targets")
	@ApiResponses({
		@ApiResponse(code = 200, message = "The number of Targets was found")
	})
	public @ResponseBody ResponseEntity<Long> getNumberOfPointTargets(
			@RequestParam(required = false, defaultValue = "")
			@ApiParam(name = "cc", value = "Country Code", example = EXAMPLE_POINT_COUNTRYCODE)
			final String cc ) {
		long count;
		if (cc == null || cc.isEmpty()) {
			count = pointTargetRepository.count();
		}
		else {
			count = pointTargetRepository.countByCountryCode(cc);
		}
		return new ResponseEntity<>(
				count,
				HttpStatus.OK);
	}

	@GetMapping("/dsatargets/{id}")
	@ApiOperation("Gets the DSATarget for a given id")
	@ApiResponses({
		@ApiResponse(code = 200, message = "The DSATarget was successfully found"),
		@ApiResponse(code = 404, message = "No DSATarget was found with given id")
	})
	public @ResponseBody ResponseEntity<DsaTargetModel> getDSATargetById(
			@PathVariable
			@ApiParam(name = "id", value = "Target ID", example = EXAMPLE_DSA_ID)
			final String id ) {
		final DsaTarget target = dsaTargetRepository.findByTargetId(id);
		if (target == null) {
			return ResponseEntity.notFound()
					.build();
		}

		final DsaTargetModel model = (DsaTargetModel) target.toModel();

		return new ResponseEntity<>(
				model,
				HttpStatus.OK);
	}

	@GetMapping("/dsatargets")
	@ApiOperation("Gets the DSATargets for a given country code")
	@ApiResponses({
		@ApiResponse(code = 200, message = "The DSATargets were successfully found"),
		@ApiResponse(code = 204, message = "No DSATargets were found for given country code"),
		@ApiResponse(code = 400, message = "The input page or count parameter was invalid")
	})
	public @ResponseBody ResponseEntity<List<DsaTargetModel>> getDSATargetByCountryCode(
			@RequestParam
			@ApiParam(name = "cc", value = "Country Code", example = EXAMPLE_DSA_LOC_BAS_COUNTRYCODE)
			final String cc,
			@RequestParam
			@ApiParam(name = "page", value = "Page number", example = "0")
			final int page,
			@RequestParam
			@ApiParam(name = "count", value = "Result limit", example = "100")
			final int count ) {
		validatePageAndCountParameters(	page,
										count);

		final PageRequest pageRequest = PageRequest.of(	page,
														count,
														sortByTargetId());

		final List<DsaTarget> targets = dsaTargetRepository.findByCountryCode(	cc,
																				pageRequest);

		if (targets.isEmpty()) {
			return new ResponseEntity<>(
					HttpStatus.NO_CONTENT);
		}

		final List<DsaTargetModel> models = targets.stream()
				.map(DsaTarget::toModel)
				.map(DsaTargetModel.class::cast)
				.collect(Collectors.toList());

		return new ResponseEntity<>(
				models,
				HttpStatus.OK);
	}

	@GetMapping("/dsatargets/count")
	@ApiOperation("Counts the targets")
	@ApiResponses({
		@ApiResponse(code = 200, message = "The number of Targets was found")
	})
	public @ResponseBody ResponseEntity<Long> getNumberOfDSATargets(
			@RequestParam(required = false, defaultValue = "")
			@ApiParam(name = "cc", value = "Country Code", example = EXAMPLE_DSA_LOC_BAS_COUNTRYCODE)
			final String cc ) {
		long count;
		if (cc == null || cc.isEmpty()) {
			count = dsaTargetRepository.count();
		}
		else {
			count = dsaTargetRepository.countByCountryCode(cc);
		}
		return new ResponseEntity<>(
				count,
				HttpStatus.OK);
	}

	@GetMapping("/loctargets/{id}")
	@ApiOperation("Gets the LOCTarget for a given id")
	@ApiResponses({
		@ApiResponse(code = 200, message = "The LOCTarget was successfully found"),
		@ApiResponse(code = 404, message = "No LOCTarget was found with given id")
	})
	public @ResponseBody ResponseEntity<LocTargetModel> getLOCTargetById(
			@PathVariable
			@ApiParam(name = "id", value = "Target ID", example = EXAMPLE_LOC_ID)
			final String id ) {
		final LocTarget target = locTargetRepository.findByTargetId(id);
		if (target == null) {
			return ResponseEntity.notFound()
					.build();
		}

		final LocTargetModel model = (LocTargetModel) target.toModel();

		return new ResponseEntity<>(
				model,
				HttpStatus.OK);
	}

	@GetMapping("/loctargets")
	@ApiOperation("Gets the LOCTargets for a given country code")
	@ApiResponses({
		@ApiResponse(code = 200, message = "The LOCTargets were successfully found"),
		@ApiResponse(code = 204, message = "No LOCTargets were found for given country code"),
		@ApiResponse(code = 400, message = "The input page or count parameter was invalid")
	})
	public @ResponseBody ResponseEntity<List<LocTargetModel>> getLOCTargetByCountryCode(
			@RequestParam
			@ApiParam(name = "cc", value = "Country Code", example = EXAMPLE_DSA_LOC_BAS_COUNTRYCODE)
			final String cc,
			@RequestParam
			@ApiParam(name = "page", value = "Page number", example = "0")
			final int page,
			@RequestParam
			@ApiParam(name = "count", value = "Result limit", example = "100")
			final int count ) {
		validatePageAndCountParameters(	page,
										count);

		final PageRequest pageRequest = PageRequest.of(	page,
														count,
														sortByTargetId());

		final List<LocTarget> targets = locTargetRepository.findByCountryCode(	cc,
																				pageRequest);

		if (targets.isEmpty()) {
			return new ResponseEntity<>(
					HttpStatus.NO_CONTENT);
		}

		final List<LocTargetModel> models = targets.stream()
				.map(LocTarget::toModel)
				.map(LocTargetModel.class::cast)
				.collect(Collectors.toList());

		return new ResponseEntity<>(
				models,
				HttpStatus.OK);
	}

	@GetMapping("/loctargets/count")
	@ApiOperation("Counts the targets")
	@ApiResponses({
		@ApiResponse(code = 200, message = "The number of Targets was found")
	})
	public @ResponseBody ResponseEntity<Long> getNumberOfLOCTargets(
			@RequestParam(required = false, defaultValue = "")
			@ApiParam(name = "cc", value = "Country Code", example = EXAMPLE_DSA_LOC_BAS_COUNTRYCODE)
			final String cc ) {
		long count;
		if (cc == null || cc.isEmpty()) {
			count = locTargetRepository.count();
		}
		else {
			count = locTargetRepository.countByCountryCode(cc);
		}
		return new ResponseEntity<>(
				count,
				HttpStatus.OK);
	}

	@GetMapping("/bastargets/{id}")
	@ApiOperation("Gets the BASTarget for a given id")
	@ApiResponses({
		@ApiResponse(code = 200, message = "The BASTarget was successfully found"),
		@ApiResponse(code = 404, message = "No BASTarget was found with given id")
	})
	public @ResponseBody ResponseEntity<BasTargetModel> getBasTargetById(
			@PathVariable
			@ApiParam(name = "id", value = "Target ID", example = EXAMPLE_BAS_ID)
			final String id ) {
		final BasTarget target = basTargetRepository.findByTargetId(id);
		if (target == null) {
			return ResponseEntity.notFound()
					.build();
		}

		final BasTargetModel model = (BasTargetModel) target.toModel();

		return new ResponseEntity<>(
				model,
				HttpStatus.OK);
	}

	@GetMapping("/bastargets")
	@ApiOperation("Gets the BASTargets for a given country code")
	@ApiResponses({
		@ApiResponse(code = 200, message = "The BASTargets were successfully found"),
		@ApiResponse(code = 204, message = "No BASTargets were found for given country code"),
		@ApiResponse(code = 400, message = "The input page or count parameter was invalid")
	})
	public @ResponseBody ResponseEntity<List<BasTargetModel>> getBasTargetByCountryCode(
			@RequestParam
			@ApiParam(name = "cc", value = "Country Code", example = EXAMPLE_DSA_LOC_BAS_COUNTRYCODE)
			final String cc,
			@RequestParam
			@ApiParam(name = "page", value = "Page number", example = "0")
			final int page,
			@RequestParam
			@ApiParam(name = "count", value = "Result limit", example = "100")
			final int count ) {
		validatePageAndCountParameters(	page,
										count);

		final PageRequest pageRequest = PageRequest.of(	page,
														count,
														sortByTargetId());

		final List<BasTarget> targets = basTargetRepository.findByCountryCode(	cc,
																				pageRequest);

		if (targets.isEmpty()) {
			return new ResponseEntity<>(
					HttpStatus.NO_CONTENT);
		}

		final List<BasTargetModel> models = targets.stream()
				.map(BasTarget::toModel)
				.map(BasTargetModel.class::cast)
				.collect(Collectors.toList());

		return new ResponseEntity<>(
				models,
				HttpStatus.OK);
	}

	@GetMapping("/bastargets/count")
	@ApiOperation("Counts the targets")
	@ApiResponses({
		@ApiResponse(code = 200, message = "The number of Targets was found")
	})
	public @ResponseBody ResponseEntity<Long> getNumberOfBasTargets(
			@RequestParam(required = false, defaultValue = "")
			@ApiParam(name = "cc", value = "Country Code", example = EXAMPLE_DSA_LOC_BAS_COUNTRYCODE)
			final String cc ) {
		long count;
		if (cc == null || cc.isEmpty()) {
			count = basTargetRepository.count();
		}
		else {
			count = basTargetRepository.countByCountryCode(cc);
		}
		return new ResponseEntity<>(
				count,
				HttpStatus.OK);
	}

	@GetMapping("/targets/estimated/{wktGeometry}")
	@ApiOperation("Gets the Estimated Target for a given wkt geometry")
	@ApiResponses({
		@ApiResponse(code = 200, message = "The Target was successfully created")
	})
	public @ResponseBody ResponseEntity<TargetModel> getEstimatedTarget(
			@PathVariable
			@ApiParam(name = "wktGeometry", value = "WKT Geometry", example = EXAMPLE_WKT_FOR_ESTIMATED)
			final String wktGeometry,
			@RequestParam(required = false)
			@ApiParam(name = "czmlStart", value = "ISO8601 Formatted Date String")
			final String czmlStart,
			@RequestParam(required = false)
			@ApiParam(name = "czmlStop", value = "ISO8601 Formatted Date String")
			final String czmlStop ) {
		final DateTime czmlStartTime;
		final DateTime czmlStopTime;

		try {
			czmlStartTime = czmlStart != null ? ISODateTimeFormat.dateTimeParser()
					.parseDateTime(czmlStart) : null;

			czmlStopTime = czmlStop != null ? ISODateTimeFormat.dateTimeParser()
					.parseDateTime(czmlStop) : null;
		}
		catch (final Exception e) {
			logger.error("Cannot parse estimated target czml ISO8601 datetime Strings: " + czmlStart + "/" + czmlStop);
			return new ResponseEntity<>(
					HttpStatus.BAD_REQUEST);
		}

		final TargetModel model = PointTargetModel.generateEstimatedTarget(	wktGeometry,
																			UUID.randomUUID()
																					.toString(),
																			estimatedTargetName,
																			estimatedTargetMinorAxisMeters,
																			estimatedTargetMajorAxisMeters,
																			estimatedTargetNumPolyPoints,
																			czmlStartTime,
																			czmlStopTime);

		return new ResponseEntity<>(
				model,
				HttpStatus.OK);
	}

	private static Sort sortByTargetId() {
		return Sort.by("targetId");
	}
}
