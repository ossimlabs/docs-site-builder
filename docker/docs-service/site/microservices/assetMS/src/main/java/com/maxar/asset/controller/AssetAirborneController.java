package com.maxar.asset.controller;

import static com.maxar.asset.common.utils.MultipartFileToString.multipartFileToString;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.ISODateTimeFormat;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.maxar.asset.common.exception.AssetExtraDataDoesNotExistException;
import com.maxar.asset.common.exception.AssetIdDoesNotExistException;
import com.maxar.asset.common.exception.AssetNameDoesNotExistException;
import com.maxar.asset.exception.AssetExtraDataExistsException;
import com.maxar.asset.exception.AssetIdDoesNotMatchException;
import com.maxar.asset.exception.AssetIdExistsException;
import com.maxar.asset.exception.AssetNameExistsException;
import com.maxar.asset.service.AssetAirborneService;
import com.maxar.asset.model.AirborneAssetModel;
import com.maxar.asset.model.AssetSmear;
import com.maxar.asset.model.AssetSmearWithBeamsRequest;
import com.maxar.asset.model.ExtraData;
import com.maxar.asset.model.FieldOfRegard;
import com.maxar.asset.model.IdList;
import com.maxar.asset.model.NameList;
import com.maxar.common.exception.BadRequestException;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@RestController
@RequestMapping("/airborne")
@Api
public class AssetAirborneController
{
	static {
		DateTimeZone.setDefault(DateTimeZone.UTC);
	}

	@Autowired
	private AssetAirborneService assetAirborneService;

	@Value("${microservices.asset.airborneUriPart}")
	private String airborneUri;

	@Value("${microservices.asset.extraUriPart}")
	private String extraUri;

	@PostMapping(value = "")
	@ApiOperation("Adds a new asset model")
	@ApiResponses({
		@ApiResponse(code = 201, message = "The new airborne asset was successfully added"),
		@ApiResponse(code = 400, message = "The input XML could not be parsed into an airborne asset, "
				+ "or the parsed ID was invalid"),
		@ApiResponse(code = 409, message = "An asset with the specified ID already exists")
	})
	public ResponseEntity<String> createAirborne(
			@ApiParam(value = "The asset model XML document")
			@RequestBody
			final AirborneAssetModel assetModel )
			throws AssetIdExistsException,
			AssetNameExistsException {
		try {
			final String assetId = assetAirborneService.createAsset(assetModel.getModelXml());
			final URI assetUri = createAirborneUri(assetId);

			return ResponseEntity.created(assetUri)
					.build();
		}
		catch (final URISyntaxException | BeansException e) {
			return ResponseEntity.badRequest()
					.body(e.getMessage());
		}
	}

	@PostMapping(value = "/file")
	@ApiOperation("Adds a new asset model")
	@ApiResponses({
		@ApiResponse(code = 201, message = "The new airborne asset was successfully added"),
		@ApiResponse(code = 400, message = "The input XML could not be parsed into a airborne asset, "
				+ "the parsed ID was invalid, or there was an error reading the file provided"),
		@ApiResponse(code = 409, message = "A airborne asset with the specified ID already exists")
	})
	public ResponseEntity<String> createAirborneFile(
			@ApiParam(value = "The XML file containing the asset model")
			@RequestParam("file")
			final MultipartFile file )
			throws AssetIdExistsException,
			AssetNameExistsException {
		try {
			final String modelXml = multipartFileToString(file);
			final String assetId = assetAirborneService.createAsset(modelXml);
			final URI assetUri = createAirborneUri(assetId);

			return ResponseEntity.created(assetUri)
					.build();
		}
		catch (final IOException | URISyntaxException | BeansException e) {
			return ResponseEntity.badRequest()
					.body(e.getMessage());
		}
	}

	@PostMapping(value = "/{id}/extra/{extraName}")
	@ApiOperation("Adds new extra data to an existing asset")
	@ApiResponses({
		@ApiResponse(code = 201, message = "The airborne asset extra data was successfully added"),
		@ApiResponse(code = 400, message = "The extra data name was invalid"),
		@ApiResponse(code = 404, message = "A airborne asset with the specified ID does not exist"),
		@ApiResponse(code = 409, message = "A airborne asset with the specified ID already contains extra data with "
				+ "the specified name")
	})
	public ResponseEntity<String> createAirborneExtraData(
			@ApiParam(value = "The ID of the asset", example = "2")
			@PathVariable(name = "id")
			final String id,
			@ApiParam(value = "The name of the extra data", example = "AIR_EO_test")
			@PathVariable(name = "extraName")
			final String extraName,
			@ApiParam(value = "The extra data type and raw data")
			@RequestBody
			final ExtraData extraData )
			throws AssetIdDoesNotExistException,
			AssetExtraDataExistsException {
		try {
			assetAirborneService.createAssetExtraData(	id,
														extraName,
														extraData);
			final URI extraDataUri = createExtraUri(id,
													extraName);

			return ResponseEntity.created(extraDataUri)
					.build();
		}
		catch (final URISyntaxException e) {
			return ResponseEntity.badRequest()
					.body(e.getMessage());
		}
	}

	@PostMapping(value = "/{id}/extra/{extraName}/{extraType}/file")
	@ApiOperation("Adds new extra data to an existing asset")
	@ApiResponses({
		@ApiResponse(code = 201, message = "The airborne asset extra data was successfully added"),
		@ApiResponse(code = 400, message = "The extra data name was invalid, or there was an error reading the "
				+ "file provided"),
		@ApiResponse(code = 404, message = "A airborne asset with the specified ID does not exist"),
		@ApiResponse(code = 409, message = "A airborne asset with the specified ID already contains extra data with "
				+ "the specified name")
	})
	public ResponseEntity<String> createAirborneExtraDataFile(
			@ApiParam(value = "The ID of the asset", example = "2")
			@PathVariable(name = "id")
			final String id,
			@ApiParam(value = "The name of the extra data", example = "AIR_EO_test")
			@PathVariable(name = "extraName")
			final String extraName,
			@ApiParam(value = "The type of the extra data", example = "Text")
			@PathVariable(name = "extraType")
			final String extraType,
			@ApiParam(value = "The file containing the raw extra data")
			@RequestParam("file")
			final MultipartFile file )
			throws AssetIdDoesNotExistException,
			AssetExtraDataExistsException {
		try {
			final ExtraData extraData = new ExtraData();
			extraData.setType(extraType);
			extraData.setData(multipartFileToString(file));

			assetAirborneService.createAssetExtraData(	id,
														extraName,
														extraData);
			final URI extraDataUri = createExtraUri(id,
													extraName);

			return ResponseEntity.created(extraDataUri)
					.build();
		}
		catch (final IOException | URISyntaxException e) {
			return ResponseEntity.badRequest()
					.body(e.getMessage());
		}
	}

	@GetMapping(value = "")
	@ApiOperation("Gets the IDs of all airborne assets")
	@ApiResponses({
		@ApiResponse(code = 200, message = "The IDs of airborne assets were successfully retrieved")
	})
	public ResponseEntity<IdList> getAirborneAll() {
		final IdList idList = new IdList();
		idList.setIds(assetAirborneService.getAllAssetIds());

		return ResponseEntity.ok(idList);
	}

	@GetMapping(value = "/{id}")
	@ApiOperation("Gets the airborne asset model with the specified ID")
	@ApiResponses({
		@ApiResponse(code = 200, message = "The airborne asset model was successfully retrieved"),
		@ApiResponse(code = 404, message = "There was no airborne asset model associated with the specified ID")
	})
	public ResponseEntity<AirborneAssetModel> getAirborneId(
			@ApiParam(value = "The ID of the asset", example = "1")
			@PathVariable(name = "id")
			final String id )
			throws AssetIdDoesNotExistException {
		final String assetModelXml = assetAirborneService.getAssetById(id);
		final AirborneAssetModel assetModel = new AirborneAssetModel();
		assetModel.setModelXml(assetModelXml);

		return ResponseEntity.ok(assetModel);
	}

	@GetMapping(value = "/name")
	@ApiOperation("Gets the names of all airborne assets")
	@ApiResponses({
		@ApiResponse(code = 200, message = "The names of airborne assets were successfully retrieved")
	})
	public ResponseEntity<NameList> getAirborneNames() {
		final NameList nameList = new NameList();
		nameList.setNames(assetAirborneService.getAllAssetNames());

		return ResponseEntity.ok(nameList);
	}

	@GetMapping(value = "/name/{name}")
	@ApiOperation("Gets the airborne asset model ID with the specified name")
	@ApiResponses({
		@ApiResponse(code = 200, message = "The airborne asset models were successfully retrieved"),
		@ApiResponse(code = 404, message = "There was no airborne asset model associated with the specified name")
	})
	public ResponseEntity<String> getAirborneName(
			@ApiParam(value = "The name of the asset", example = "AIR_SAR")
			@PathVariable(name = "name")
			final String name )
			throws AssetNameDoesNotExistException {
		final String id = assetAirborneService.getAssetIdByName(name);

		return ResponseEntity.ok(id);
	}

	@GetMapping(value = "/{id}/extra")
	@ApiOperation("Gets the extra data names of the airborne asset with the specified ID")
	@ApiResponses({
		@ApiResponse(code = 200, message = "The names of extra data for the airborne asset model were "
				+ "successfully retrieved"),
		@ApiResponse(code = 404, message = "There was no airborne asset model associated with the specified ID")
	})
	public ResponseEntity<NameList> getAirborneExtra(
			@ApiParam(value = "The ID of the asset", example = "1")
			@PathVariable(name = "id")
			final String id )
			throws AssetIdDoesNotExistException {
		final List<String> extraDataNames = assetAirborneService.getAllAssetExtraDataNames(id);

		final NameList nameList = new NameList();
		nameList.setNames(extraDataNames);

		return ResponseEntity.ok(nameList);
	}

	@GetMapping(value = "/{id}/extra/{extraName}")
	@ApiOperation("Gets the constraints of the airborne asset with the specified ID")
	@ApiResponses({
		@ApiResponse(code = 200, message = "The names of extra data for the airborne asset model were "
				+ "successfully retrieved"),
		@ApiResponse(code = 404, message = "A airborne asset with the specified ID does not exist, or it does not "
				+ "contain extra data with the specified name"),
	})
	public ResponseEntity<ExtraData> getAirborneExtraName(
			@ApiParam(value = "The ID of the asset", example = "2")
			@PathVariable(name = "id")
			final String id,
			@ApiParam(value = "The name of the extra data", example = "AIR_EO_test")
			@PathVariable(name = "extraName")
			final String extraName )
			throws AssetIdDoesNotExistException,
			AssetExtraDataDoesNotExistException {
		final ExtraData extraData = assetAirborneService.getAssetExtraDataByName(	id,
																					extraName);

		return ResponseEntity.ok(extraData);
	}

	@GetMapping(value = "/{id}/for")
	@ApiOperation("Gets the fields of regard of the airborne asset with the specified ID at the specified time")
	@ApiResponses({
		@ApiResponse(code = 200, message = "The sensors and the fields of regard for the airborne asset were "
				+ "successfully retrieved"),
		@ApiResponse(code = 400, message = "The atTime parameter was not in the correct format"),
		@ApiResponse(code = 404, message = "A airborne asset with the specified ID does not exist")
	})
	public ResponseEntity<List<FieldOfRegard>> getAirborneFieldsOfRegard(
			@ApiParam(value = "The ID of the asset", example = "1")
			@PathVariable
			final String id,
			@ApiParam(value = "The time to get the fields of regard for the Asset (ISO 8601)", example = "2020-01-01T15:00:00.000Z")
			@RequestParam
			final String atTime )
			throws AssetIdDoesNotExistException {
		final DateTime atDateTime;
		try {
			atDateTime = ISODateTimeFormat.dateTimeParser()
					.parseDateTime(atTime);
		}
		catch (final RuntimeException e) {
			throw new BadRequestException(
					e.getMessage());
		}

		final List<FieldOfRegard> fieldsOfRegard = assetAirborneService.getAssetFieldsOfRegard(	id,
																								atDateTime,
																								null);

		return ResponseEntity.ok(fieldsOfRegard);
	}

	@PostMapping(value = "/smear")
	@ApiOperation("Gets the smears of the airborne asset with the specified ID for the specified time frame. Request may include optional beams for CZML generation purposes.")
	@ApiResponses({
		@ApiResponse(code = 200, message = "Smears for the airborne asset were successfully retrieved"),
		@ApiResponse(code = 400, message = "The something in the json request body was not in the correct format"),
		@ApiResponse(code = 404, message = "An airborne asset with the specified ID does not exist")
	})
	public ResponseEntity<List<AssetSmear>> getAirborneSmears(
			@RequestBody
			final AssetSmearWithBeamsRequest assetSmearRequest )
			throws AssetIdDoesNotExistException {
		final List<AssetSmear> assetSmear = assetAirborneService.getAssetSmears(assetSmearRequest,
																				null);

		return ResponseEntity.ok(assetSmear);
	}

	@PutMapping(value = "/{id}")
	@ApiOperation("Updates an existing asset model")
	@ApiResponses({
		@ApiResponse(code = 200, message = "The airborne asset was successfully updated"),
		@ApiResponse(code = 400, message = "The input XML could not be parsed into a airborne asset, "
				+ "or the parsed ID was invalid"),
		@ApiResponse(code = 404, message = "There was no airborne asset model associated with the specified ID")
	})
	public ResponseEntity<String> updateAirborne(
			@ApiParam(value = "The ID of the asset", example = "2")
			@PathVariable(name = "id")
			final String id,
			@ApiParam(value = "The asset model XML document")
			@RequestBody
			final AirborneAssetModel modelXml )
			throws AssetIdDoesNotExistException,
			AssetIdDoesNotMatchException {
		try {
			assetAirborneService.updateAsset(	id,
												modelXml.getModelXml());

			return ResponseEntity.ok()
					.build();
		}
		catch (final BeansException e) {
			return ResponseEntity.badRequest()
					.body(e.getMessage());
		}
	}

	@PutMapping(value = "/{id}/file")
	@ApiOperation("Updates an existing asset model")
	@ApiResponses({
		@ApiResponse(code = 200, message = "The airborne asset was successfully updated"),
		@ApiResponse(code = 400, message = "The input XML could not be parsed into a airborne asset, "
				+ "the parsed ID was invalid, or there was an error reading the file provided"),
		@ApiResponse(code = 404, message = "There was no airborne asset model associated with the specified ID")
	})
	public ResponseEntity<String> updateAirborneFile(
			@ApiParam(value = "The ID of the asset", example = "1")
			@PathVariable(name = "id")
			final String id,
			@ApiParam(value = "The XML file containing the asset model")
			@RequestParam("file")
			final MultipartFile file )
			throws AssetIdDoesNotExistException,
			AssetIdDoesNotMatchException {
		try {
			final String modelXml = multipartFileToString(file);
			assetAirborneService.updateAsset(	id,
												modelXml);

			return ResponseEntity.ok()
					.build();
		}
		catch (final IOException | BeansException e) {
			return ResponseEntity.badRequest()
					.body(e.getMessage());
		}
	}

	@PutMapping(value = "/{id}/extra/{extraName}")
	@ApiOperation("Updates extra data for an existing asset")
	@ApiResponses({
		@ApiResponse(code = 200, message = "The airborne asset extra data was successfully updated"),
		@ApiResponse(code = 404, message = "A airborne asset with the specified ID does not exist, or it does not "
				+ "contain extra data with the specified name"),
	})
	public ResponseEntity<String> updateAirborneExtraData(
			@ApiParam(value = "The ID of the asset", example = "2")
			@PathVariable(name = "id")
			final String id,
			@ApiParam(value = "The name of the extra data", example = "AIR_EO_test")
			@PathVariable(name = "extraName")
			final String extraName,
			@ApiParam(value = "The extra data type and raw data")
			@RequestBody
			final ExtraData extraData )
			throws AssetIdDoesNotExistException,
			AssetExtraDataDoesNotExistException {
		assetAirborneService.updateAssetExtraData(	id,
													extraName,
													extraData);

		return ResponseEntity.ok()
				.build();
	}

	@PutMapping(value = "/{id}/extra/{extraName}/{extraType}/file")
	@ApiOperation("Updates extra data for an existing asset")
	@ApiResponses({
		@ApiResponse(code = 200, message = "The airborne asset extra data was successfully updated"),
		@ApiResponse(code = 400, message = "There was an error reading the file provided"),
		@ApiResponse(code = 404, message = "A airborne asset with the specified ID does not exist, or it does not "
				+ "contain extra data with the specified name"),
	})
	public ResponseEntity<String> updateAirborneExtraDataFile(
			@ApiParam(value = "The ID of the asset", example = "2")
			@PathVariable(name = "id")
			final String id,
			@ApiParam(value = "The name of the extra data", example = "AIR_EO_test")
			@PathVariable(name = "extraName")
			final String extraName,
			@ApiParam(value = "The type of the extra data", example = "Text")
			@PathVariable(name = "extraType")
			final String extraType,
			@ApiParam(value = "The file containing the raw extra data")
			@RequestParam("file")
			final MultipartFile file )
			throws AssetIdDoesNotExistException,
			AssetExtraDataDoesNotExistException {
		try {
			final ExtraData extraData = new ExtraData();
			extraData.setType(extraType);
			extraData.setData(multipartFileToString(file));

			assetAirborneService.updateAssetExtraData(	id,
														extraName,
														extraData);

			return ResponseEntity.ok()
					.build();
		}
		catch (final IOException e) {
			return ResponseEntity.badRequest()
					.body(e.getMessage());
		}
	}

	@DeleteMapping(value = "/{id}")
	@ApiOperation("Deletes an asset model")
	@ApiResponses({
		@ApiResponse(code = 200, message = "The airborne asset was successfully deleted"),
		@ApiResponse(code = 404, message = "There was no airborne asset model associated with the specified ID")
	})
	public ResponseEntity<String> deleteAirborne(
			@ApiParam(value = "The ID of the asset", example = "1")
			@PathVariable(name = "id")
			final String id )
			throws AssetIdDoesNotExistException {
		assetAirborneService.deleteAsset(id);

		return ResponseEntity.ok()
				.build();
	}

	@DeleteMapping(value = "/{id}/extra/{extraName}")
	@ApiOperation("Deletes extra data for an existing asset")
	@ApiResponses({
		@ApiResponse(code = 200, message = "The airborne asset extra data was successfully deleted"),
		@ApiResponse(code = 404, message = "A airborne asset with the specified ID does not exist, or it does not "
				+ "contain extra data with the specified name"),
	})
	public ResponseEntity<String> deleteAirborneExtraData(
			@ApiParam(value = "The ID of the asset", example = "2")
			@PathVariable(name = "id")
			final String id,
			@ApiParam(value = "The name of the extra data", example = "AIR_EO_test")
			@PathVariable(name = "extraName")
			final String extraName )
			throws AssetIdDoesNotExistException,
			AssetExtraDataDoesNotExistException {
		assetAirborneService.deleteAssetExtraData(	id,
													extraName);

		return ResponseEntity.ok()
				.build();
	}

	private URI createAirborneUri(
			final String assetId )
			throws URISyntaxException {
		return new URI(
				airborneUri + assetId);
	}

	private URI createExtraUri(
			final String assetId,
			final String extraName )
			throws URISyntaxException {
		return new URI(
				airborneUri + assetId + extraUri + extraName);
	}
}
