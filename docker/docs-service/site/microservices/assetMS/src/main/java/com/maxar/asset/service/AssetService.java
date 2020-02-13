package com.maxar.asset.service;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.format.ISODateTimeFormat;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.GenericXmlApplicationContext;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.lang.Nullable;
import org.springframework.transaction.annotation.Transactional;

import com.maxar.asset.common.exception.AssetExtraDataDoesNotExistException;
import com.maxar.asset.common.exception.AssetIdDoesNotExistException;
import com.maxar.asset.common.exception.AssetNameDoesNotExistException;
import com.maxar.asset.common.service.AssetRetriever;
import com.maxar.asset.exception.AssetExtraDataExistsException;
import com.maxar.asset.exception.AssetIdDoesNotMatchException;
import com.maxar.asset.exception.AssetIdExistsException;
import com.maxar.asset.exception.AssetNameExistsException;
import com.maxar.asset.model.AssetSmear;
import com.maxar.asset.model.AssetSmearWithBeamsRequest;
import com.maxar.asset.model.ExtraData;
import com.maxar.asset.model.FieldOfRegard;
import com.maxar.asset.model.OpBeam;
import com.maxar.asset.entity.Asset;
import com.maxar.asset.entity.AssetExtraData;
import com.maxar.asset.entity.AssetType;
import com.maxar.asset.repository.AssetRepository;
import com.radiantblue.analytics.geodesy.geometry.GeodeticGeometry;
import com.radiantblue.analytics.geodesy.geometry.GeodeticPolygon;
import com.radiantblue.analytics.isr.core.model.payload.IPayload;
import com.radiantblue.analytics.isr.core.model.sensor.ISensor;

/**
 * The parent class for the specific asset services.
 */
public abstract class AssetService implements
		AssetRetriever
{
	private static final Integer FOR_FRAME_DEFAULT = 1;

	@Autowired
	private AssetRepository assetRepository;

	/**
	 * Add a new model to the database, keyed by its ID field.
	 *
	 * @param assetModelXml
	 *            The raw XML of the asset model.
	 * @return The ID of the asset model.
	 * @throws AssetIdExistsException
	 *             Thrown if there is already an asset in the database with the ID
	 *             specified in the XML.
	 * @throws AssetNameExistsException
	 *             Thrown if there is already an asset in the database with the name
	 *             specified in the XML.
	 */
	public String createAsset(
			final String assetModelXml )
			throws AssetIdExistsException,
			AssetNameExistsException {
		final com.radiantblue.analytics.isr.core.model.asset.Asset asset = xmlToAsset(assetModelXml);

		final String assetId = Integer.toString(asset.getId());

		if (assetRepository.findById(assetId)
				.isPresent()) {
			throw new AssetIdExistsException(
					assetId);
		}

		final String assetName = asset.getName();

		if (assetRepository.getByName(assetName)
				.isPresent()) {
			throw new AssetNameExistsException(
					assetName);
		}

		instantiateAndSaveAsset(assetId,
								asset,
								assetModelXml);

		return assetId;
	}

	/**
	 * Adds new extra data to a asset entry in the database.
	 *
	 * @param id
	 *            The ID of the asset to add extra data to.
	 * @param extraDataName
	 *            The name of the extra data object.
	 * @param extraData
	 *            The extra data type and raw data.
	 * @throws AssetExtraDataExistsException
	 *             Thrown if there is already extra data for the asset with the
	 *             specified name.
	 * @throws AssetIdDoesNotExistException
	 *             Thrown if no asset with the specified ID exists in the database.
	 */
	@Transactional
	public void createAssetExtraData(
			final String id,
			final String extraDataName,
			final ExtraData extraData )
			throws AssetExtraDataExistsException,
			AssetIdDoesNotExistException {
		final Asset asset = assetRepository.getByIdAndType(	id,
															getAssetType())
				.orElseThrow(() -> new AssetIdDoesNotExistException(
						id));

		if (asset.getExtraData()
				.stream()
				.anyMatch(assetExtraData -> assetExtraData.getName()
						.equals(extraDataName))) {
			throw new AssetExtraDataExistsException(
					id,
					extraDataName);
		}

		addExtraDataAndSaveAsset(	asset,
									extraDataName,
									extraData);
	}

	/**
	 * @return A list of all asset IDs.
	 */
	public List<String> getAllAssetIds() {
		return assetRepository.getByType(getAssetType())
				.stream()
				.map(Asset::getId)
				.collect(Collectors.toList());
	}

	/**
	 * Find an asset by its ID.
	 *
	 * @param id
	 *            The ID of the asset to find.
	 * @return The asset XML.
	 * @throws AssetIdDoesNotExistException
	 *             Thrown if no asset with the specified ID exists in the database.
	 */
	@Override
	public String getAssetById(
			final String id )
			throws AssetIdDoesNotExistException {
		return assetRepository.getByIdAndType(	id,
												getAssetType())
				.orElseThrow(() -> new AssetIdDoesNotExistException(
						id))
				.getXml();
	}

	/**
	 * @return A list of all asset names.
	 */
	public List<String> getAllAssetNames() {
		return assetRepository.getByType(getAssetType())
				.stream()
				.map(Asset::getName)
				.collect(Collectors.toList());
	}

	/**
	 * Find an asset's ID by its name.
	 *
	 * @param name
	 *            The name of the asset to find.
	 * @return The asset ID.
	 * @throws AssetNameDoesNotExistException
	 *             Thrown if no asset with the specified name exists in the
	 *             database.
	 */
	@Override
	public String getAssetIdByName(
			final String name )
			throws AssetNameDoesNotExistException {
		return assetRepository.getByNameAndType(name,
												getAssetType())
				.orElseThrow(() -> new AssetNameDoesNotExistException(
						name))
				.getId();
	}

	/**
	 * Find all extra data names with the specified asset ID.
	 *
	 * @param id
	 *            The asset ID to search for.
	 * @return A list of the extra data names.
	 * @throws AssetIdDoesNotExistException
	 *             Thrown if no asset with the specified ID exists in the database.
	 */
	@Transactional(readOnly = true)
	@Override
	public List<String> getAllAssetExtraDataNames(
			final String id )
			throws AssetIdDoesNotExistException {
		return assetRepository.getByIdAndType(	id,
												getAssetType())
				.map(Asset::getExtraData)
				.orElseThrow(() -> new AssetIdDoesNotExistException(
						id))
				.stream()
				.map(AssetExtraData::getName)
				.collect(Collectors.toList());
	}

	/**
	 * Get the extra data for an asset with the specified name.
	 *
	 * @param id
	 *            The ID of the asset.
	 * @param extraDataName
	 *            The name of the extra data for the asset.
	 * @return The extra data with the specified name if there is any, or null.
	 * @throws AssetExtraDataDoesNotExistException
	 *             Thrown if the asset does not contain extra data with the
	 *             specified name.
	 * @throws AssetIdDoesNotExistException
	 *             Thrown if no asset with the specified ID exists in the database.
	 */
	@Transactional(readOnly = true)
	@Override
	public ExtraData getAssetExtraDataByName(
			final String id,
			final String extraDataName )
			throws AssetExtraDataDoesNotExistException,
			AssetIdDoesNotExistException {
		return assetRepository.getByIdAndType(	id,
												getAssetType())
				.map(Asset::getExtraData)
				.orElseThrow(() -> new AssetIdDoesNotExistException(
						id))
				.stream()
				.filter(assetExtraData -> extraDataName.equals(assetExtraData.getName()))
				.findFirst()
				.map(assetExtraData -> {
					final ExtraData extraData = new ExtraData();
					extraData.setType(assetExtraData.getType());
					extraData.setData(new String(
							assetExtraData.getRawData()));

					return extraData;
				})
				.orElseThrow(() -> new AssetExtraDataDoesNotExistException(
						id,
						extraDataName));
	}

	/**
	 * Get the fields of regard for an asset at a specified time.
	 *
	 * @param id
	 *            The ID of the asset.
	 * @param atTime
	 *            The time to get the fields of regard for the asset.
	 * @param propagatorType
	 *            The type of propagator to use, if applicable.
	 * @return The fields of regard angle for the asset at the specified time.
	 * @throws AssetIdDoesNotExistException
	 *             Thrown if no asset with the specified ID exists in the database.
	 */
	public List<FieldOfRegard> getAssetFieldsOfRegard(
			final String id,
			final DateTime atTime,
			@Nullable
			final String propagatorType )
			throws AssetIdDoesNotExistException {
		return assetRepository.getByIdAndType(	id,
												getAssetType())
				.map(Asset::getXml)
				.map(this::xmlToAsset)
				.map(asset -> getAssetFieldsOfRegard(	asset,
														atTime,
														propagatorType))
				.orElseThrow(() -> new AssetIdDoesNotExistException(
						id));
	}

	/**
	 * Update an existing model in the database, keyed by its ID field.
	 *
	 * @param id
	 *            The ID of the asset to update.
	 * @param assetModelXml
	 *            The raw XML of the asset model.
	 * @throws AssetIdDoesNotExistException
	 *             Thrown if no asset with the specified ID exists in the database.
	 * @throws AssetIdDoesNotMatchException
	 *             Thrown if the asset ID specified does not match the asset ID
	 *             parsed from the XML.
	 */
	public void updateAsset(
			final String id,
			final String assetModelXml )
			throws AssetIdDoesNotExistException,
			AssetIdDoesNotMatchException {
		final com.radiantblue.analytics.isr.core.model.asset.Asset asset = xmlToAsset(assetModelXml);

		final String assetId = Integer.toString(asset.getId());

		if (!assetId.equals(id)) {
			throw new AssetIdDoesNotMatchException(
					id,
					assetId);
		}

		if (!assetRepository.getByIdAndType(assetId,
											getAssetType())
				.isPresent()) {
			throw new AssetIdDoesNotExistException(
					assetId);
		}

		instantiateAndSaveAsset(assetId,
								asset,
								assetModelXml);
	}

	/**
	 * Modifies extra data for a asset entry in the database.
	 *
	 * @param id
	 *            The ID of the asset to modify extra data for.
	 * @param extraDataName
	 *            The name of the extra data object.
	 * @param extraData
	 *            The extra data type and raw data.
	 * @throws AssetExtraDataDoesNotExistException
	 *             Thrown if the asset does not contain extra data with the
	 *             specified name.
	 * @throws AssetIdDoesNotExistException
	 *             Thrown if no asset with the specified ID exists in the database.
	 */
	@Transactional
	public void updateAssetExtraData(
			final String id,
			final String extraDataName,
			final ExtraData extraData )
			throws AssetExtraDataDoesNotExistException,
			AssetIdDoesNotExistException {
		final Asset asset = removeExtraDataFromAsset(	id,
														extraDataName);

		addExtraDataAndSaveAsset(	asset,
									extraDataName,
									extraData);
	}

	/**
	 * Remove a model from the database.
	 *
	 * @param id
	 *            The ID of the asset to delete.
	 * @throws AssetIdDoesNotExistException
	 *             Thrown if no asset with the specified ID exists in the database.
	 */
	public void deleteAsset(
			final String id )
			throws AssetIdDoesNotExistException {
		if (!assetRepository.getByIdAndType(id,
											getAssetType())
				.isPresent()) {
			throw new AssetIdDoesNotExistException(
					id);
		}

		assetRepository.deleteById(id);
	}

	/**
	 * Remove extra data from a asset.
	 *
	 * @param id
	 *            The ID of the asset to remove extra data from.
	 * @param extraDataName
	 *            The name of the extra data object to remove.
	 * @throws AssetExtraDataDoesNotExistException
	 *             Thrown if the asset does not contain extra data with the
	 *             specified name.
	 * @throws AssetIdDoesNotExistException
	 *             Thrown if no asset with the specified ID exists in the database.
	 */
	@Transactional
	public void deleteAssetExtraData(
			final String id,
			final String extraDataName )
			throws AssetExtraDataDoesNotExistException,
			AssetIdDoesNotExistException {
		final Asset asset = removeExtraDataFromAsset(	id,
														extraDataName);

		assetRepository.save(asset);
	}

	/**
	 * @return The type of this service.
	 */
	protected abstract AssetType getAssetType();

	/**
	 * Convert XML to an Asset object.
	 *
	 * @param xml
	 *            The XML of the asset beans.
	 * @return The Asset object defined in the XML.
	 */
	protected abstract com.radiantblue.analytics.isr.core.model.asset.Asset xmlToAsset(
			final String xml );

	/**
	 * Create a Asset and save it in the database.
	 *
	 * @param id
	 *            The ID of the asset.
	 * @param asset
	 *            The Asset object.
	 * @param xml
	 *            The asset model XML.
	 */
	private void instantiateAndSaveAsset(
			final String id,
			final com.radiantblue.analytics.isr.core.model.asset.Asset asset,
			final String xml ) {
		final Asset persistedAsset = new Asset();
		persistedAsset.setId(id);
		persistedAsset.setName(asset.getName());
		persistedAsset.setXml(xml);
		persistedAsset.setType(getAssetType());

		assetRepository.save(persistedAsset);
	}

	/**
	 * Add extra data to an existing Asset and save it in the database.
	 *
	 * @param asset
	 *            The asset to update.
	 * @param extraDataName
	 *            The name of the extra data.
	 * @param extraData
	 *            The extra data type and raw data.
	 */
	private void addExtraDataAndSaveAsset(
			final Asset asset,
			final String extraDataName,
			final ExtraData extraData ) {
		final AssetExtraData assetExtraData = new AssetExtraData();
		assetExtraData.setName(extraDataName);
		assetExtraData.setType(extraData.getType());
		assetExtraData.setRawData(extraData.getData()
				.getBytes());

		asset.addExtraData(assetExtraData);

		assetRepository.save(asset);
	}

	/**
	 * Remove extra data from an asset.
	 *
	 * @param id
	 *            The ID of the asset to remove extra data from.
	 * @param extraDataName
	 *            The name of the extra data to remove the from the asset.
	 * @return The updated asset, with the extra data specified removed.
	 * @throws AssetExtraDataDoesNotExistException
	 *             The asset does not contain extra data with the specified name.
	 * @throws AssetIdDoesNotExistException
	 *             Thrown if no asset with the specified ID exists in the database.
	 */
	private Asset removeExtraDataFromAsset(
			final String id,
			final String extraDataName )
			throws AssetExtraDataDoesNotExistException,
			AssetIdDoesNotExistException {
		final Asset asset = assetRepository.getByIdAndType(	id,
															getAssetType())
				.orElseThrow(() -> new AssetIdDoesNotExistException(
						id));

		asset.removeExtraData(asset.getExtraData()
				.stream()
				.filter(assetExtraData -> assetExtraData.getName()
						.equals(extraDataName))
				.findFirst()
				.orElseThrow(() -> new AssetExtraDataDoesNotExistException(
						id,
						extraDataName)));

		return asset;
	}

	/**
	 * Make a new bean factory from XML.
	 *
	 * @param xml
	 *            The XML document.
	 * @return A GenericXmlApplicationContext created from the XML document.
	 */
	static BeanFactory beanFactoryFromXml(
			final String xml ) {
		final Resource resource = new ByteArrayResource(
				xml.getBytes(StandardCharsets.UTF_8));

		return new GenericXmlApplicationContext(
				resource);
	}

	/**
	 * Convert from a GeodeticGeometry to a FieldOfRegard response type.
	 *
	 * @param geodeticGeometry
	 *            The geometry to use to set the fieldOfRegardAngleWkt field of the
	 *            FieldOfRegard.
	 * @return A FieldOfRegard type with its fieldOfRegardAngleWkt field set to the
	 *         WKT of the geometry in degrees.
	 */
	private static FieldOfRegard geodeticGeometryToFieldOfRegard(
			final GeodeticGeometry geodeticGeometry,
			final String forName,
			final String sensorType,
			final DateTime atTime ) {
		final FieldOfRegard fieldOfRegard = new FieldOfRegard();

		if (geodeticGeometry == null) {
			return null;
		}

		if (geodeticGeometry instanceof GeodeticPolygon) {
			final GeodeticPolygon geodeticPolygon = (GeodeticPolygon) geodeticGeometry;
			fieldOfRegard.setFieldOfRegardAngleWkt(geodeticPolygon.splitOnDateLine()
					.jtsGeometry_deg()
					.toText());
		}
		else {
			fieldOfRegard.setFieldOfRegardAngleWkt(geodeticGeometry.jtsGeometry_deg()
					.toText());
		}

		fieldOfRegard.setFieldOfRegardName(forName);
		fieldOfRegard.setFieldOfRegardAtTime(atTime);
		fieldOfRegard.setSensorType(sensorType);

		return fieldOfRegard;
	}

	private static AssetSmear geodeticGeometryToAssetSmear(
			final com.radiantblue.analytics.isr.core.model.asset.Asset asset,
			final GeodeticGeometry geodeticGeometry,
			final String forName,
			final String sensorType,
			final DateTime startTime,
			final DateTime stopTime,
			final List<OpBeam> beams,
			final AssetSmearWithBeamsRequest request ) {
		final AssetSmear assetSmear = new AssetSmear();

		if (geodeticGeometry == null) {
			return null;
		}

		if (geodeticGeometry instanceof GeodeticPolygon) {
			final GeodeticPolygon geodeticPolygon = (GeodeticPolygon) geodeticGeometry;
			assetSmear.setSmearAngleWkt(geodeticPolygon.splitOnDateLine()
					.jtsGeometry_deg()
					.toText());
		}
		else {
			assetSmear.setSmearAngleWkt(geodeticGeometry.jtsGeometry_deg()
					.toText());
		}

		assetSmear.setSmearName(forName);
		assetSmear.setStartTime(startTime);
		assetSmear.setStopTime(stopTime);
		assetSmear.setOpBeams(beams);
		assetSmear.setAsset(asset);
		assetSmear.setSensorType(sensorType);
		assetSmear.setSmearCzmlRequested(request.isSmearCzmlRequested());
		assetSmear.setForFramesCzmlRequested(request.isForFramesCzmlRequested());
		assetSmear.setOpBeamsCzmlRequested(request.isOpBeamsCzmlRequested());
		final Integer forFrameIncrementSec = Optional.ofNullable(request.getForFrameIncrementSec())
				.orElse(FOR_FRAME_DEFAULT);
		assetSmear.setForFrameIncrementSec(forFrameIncrementSec);

		return assetSmear;
	}

	/**
	 * Get the fields of regard for an asset. This returns a list of field of regard
	 * reflecting the asset, asset payloads, and asset payload sensors.
	 *
	 * @param asset
	 *            The asset to get the fields of regard of.
	 * @param atTime
	 *            The time to get the fields of regard for the asset.
	 * @param propagatorType
	 *            The type of propagator to use, if applicable.
	 * @return The asset's fields of regard from the asset, asset payloads, and
	 *         asset payload sensors.
	 */
	protected List<FieldOfRegard> getAssetFieldsOfRegard(
			final com.radiantblue.analytics.isr.core.model.asset.Asset asset,
			final DateTime atTime,
			@Nullable
			final String propagatorType ) {
		asset.init();
		final List<FieldOfRegard> assetFieldOfRegards = new ArrayList<>();
		final List<IPayload> payloads = asset.getPayloads();
		final List<ISensor> sensors = asset.getPayloads()
				.stream()
				.map(IPayload::sensors)
				.flatMap(Collection::stream)
				.collect(Collectors.toList());

		final GeodeticGeometry assetGeo = asset.getFOR(atTime);

		if (assetGeo != null) {
			assetFieldOfRegards.add(geodeticGeometryToFieldOfRegard(assetGeo,
																	asset.getName(),
																	"", // no sensor type at the asset level
																	atTime));
		}

		assetFieldOfRegards.addAll(payloads.stream()
				.map(payload -> geodeticGeometryToFieldOfRegard(payload.getFOR(atTime),
																payload.asset()
																		.getName() + " " + payload.getName(),
																payload.getPayloadType(),
																atTime))
				.filter(Objects::nonNull)
				.collect(Collectors.toList()));

		assetFieldOfRegards.addAll(sensors.stream()
				.map(sensor -> geodeticGeometryToFieldOfRegard(	sensor.getFOR(atTime),
																sensor.payload()
																		.asset()
																		.getName() + " "
																		+ sensor.payload()
																				.getName()
																		+ " " + sensor.getName(),
																sensor.getSensorType(),
																atTime))
				.filter(Objects::nonNull)
				.collect(Collectors.toList()));

		return assetFieldOfRegards;
	}

	/**
	 * Get smears for an asset. This returns a list of smears reflecting the asset,
	 * asset payloads, and asset payload sensors.
	 *
	 * @param asset
	 *            The asset to get the smears.
	 * @param assetSmearRequest
	 *            Request object that contains required start/stop datetime for
	 *            asset smears and optional centroids with start/stop.
	 * @param propagatorType
	 *            The type of propagator to use, if applicable.
	 * @return The asset's smears from the asset, asset payloads, and asset payload
	 *         sensors.
	 */
	protected List<AssetSmear> getAssetSmears(
			final com.radiantblue.analytics.isr.core.model.asset.Asset asset,
			final AssetSmearWithBeamsRequest assetSmearRequest,
			@Nullable
			final String propagatorType ) {
		asset.init();
		final List<AssetSmear> smears = new ArrayList<>();
		final List<IPayload> payloads = asset.getPayloads();
		final List<ISensor> sensors = asset.getPayloads()
				.stream()
				.map(IPayload::sensors)
				.flatMap(Collection::stream)
				.collect(Collectors.toList());

		final DateTime startTime = ISODateTimeFormat.dateTimeParser()
				.parseDateTime(assetSmearRequest.getStartTimeISO8601());
		final DateTime stopTime = ISODateTimeFormat.dateTimeParser()
				.parseDateTime(assetSmearRequest.getStopTimeISO8601());
		final List<OpBeam> beams = assetSmearRequest.getBeams();
		final Integer forFrameIncrementSec = Optional.ofNullable(assetSmearRequest.getForFrameIncrementSec())
				.orElse(FOR_FRAME_DEFAULT);

		final Duration smearDurationMillis = Duration.millis(stopTime.getMillis() - startTime.getMillis());

		final GeodeticGeometry assetSmearGeo = asset.getFORSmear(	startTime,
																	stopTime,
																	smearDurationMillis);

		if (assetSmearGeo != null) {
			final AssetSmear assetSmear = geodeticGeometryToAssetSmear(	asset,
																		assetSmearGeo,
																		asset.getName(),
																		"", // no sensor type at the asset level
																		startTime,
																		stopTime,
																		beams,
																		assetSmearRequest);
			assetSmear.getForFrames()
					.addAll(generateSmearFORFrames(	new AssetFORFrameProducer(
							asset),
													startTime,
													stopTime,
													forFrameIncrementSec));

			smears.add(assetSmear);
		}

		smears.addAll(payloads.stream()
				.map(payload -> {

					final AssetSmear payloadSmear = geodeticGeometryToAssetSmear(	asset,
																					payload.getFORSmear(startTime,
																										stopTime,
																										smearDurationMillis),
																					payload.asset()
																							.getName() + " "
																							+ payload.getName(),
																					payload.getPayloadType(),
																					startTime,
																					stopTime,
																					beams,
																					assetSmearRequest);

					if (payloadSmear != null) {
						payloadSmear.getForFrames()
								.addAll(generateSmearFORFrames(	new PayloadFORFrameProducer(
										payload),
																startTime,
																stopTime,
																forFrameIncrementSec));
					}

					return payloadSmear;
				})
				.filter(Objects::nonNull)
				.collect(Collectors.toList()));

		smears.addAll(sensors.stream()
				.map(sensor -> {
					final AssetSmear sensorSmear = geodeticGeometryToAssetSmear(asset,
																				sensor.getFORSmear(	startTime,
																									stopTime,
																									smearDurationMillis),
																				sensor.payload()
																						.asset()
																						.getName() + " "
																						+ sensor.payload()
																								.getName()
																						+ " " + sensor.getName(),
																				sensor.getSensorType(),
																				startTime,
																				stopTime,
																				beams,
																				assetSmearRequest);

					if (sensorSmear != null) {
						final FORFrameProducer sensorFORFrameProducer = new SensorFORFrameProducer(
								sensor);
						sensorSmear.getForFrames()
								.addAll(generateSmearFORFrames(	sensorFORFrameProducer,
																startTime,
																stopTime,
																forFrameIncrementSec));
					}

					return sensorSmear;
				})
				.filter(Objects::nonNull)
				.collect(Collectors.toList()));

		return smears;
	}

	private List<FieldOfRegard> generateSmearFORFrames(
			final FORFrameProducer forSource,
			final DateTime startTime,
			final DateTime stopTime,
			final int frameIncrementSec ) {
		DateTime currentIncrement = startTime;

		final List<FieldOfRegard> forFrames = new ArrayList<>();

		while (currentIncrement.isBefore(stopTime)) {
			forFrames.add(geodeticGeometryToFieldOfRegard(	forSource.getFOR(currentIncrement),
															forSource.getName(),
															forSource.getSensorType(),
															currentIncrement));

			currentIncrement = currentIncrement.plusSeconds(frameIncrementSec);

			if (currentIncrement.isEqual(frameIncrementSec) || currentIncrement.isAfter(stopTime)) {
				forFrames.add(geodeticGeometryToFieldOfRegard(	forSource.getFOR(stopTime),
																forSource.getName(),
																forSource.getSensorType(),
																stopTime));
			}
		}

		return forFrames;
	}

	public List<AssetSmear> getAssetSmears(
			final AssetSmearWithBeamsRequest assetSmearRequest,
			@Nullable
			final String propagatorType )
			throws AssetIdDoesNotExistException {
		return assetRepository.getByIdAndType(	assetSmearRequest.getAssetId(),
												getAssetType())
				.map(Asset::getXml)
				.map(this::xmlToAsset)
				.map(asset -> getAssetSmears(	asset,
												assetSmearRequest,
												propagatorType))
				.orElseThrow(() -> new AssetIdDoesNotExistException(
						assetSmearRequest.getAssetId()));
	}
}
