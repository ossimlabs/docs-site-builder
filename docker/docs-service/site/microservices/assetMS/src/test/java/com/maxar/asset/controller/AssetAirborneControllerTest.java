package com.maxar.asset.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.xml.XmlBeanDefinitionStoreException;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.multipart.MultipartFile;
import org.xml.sax.SAXParseException;

import com.maxar.asset.common.exception.AssetExtraDataDoesNotExistException;
import com.maxar.asset.common.exception.AssetIdDoesNotExistException;
import com.maxar.asset.common.exception.AssetNameDoesNotExistException;
import com.maxar.asset.exception.AssetExtraDataExistsException;
import com.maxar.asset.exception.AssetHasNoFieldOfRegardException;
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

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@TestPropertySource(locations = "classpath:testassetms.properties")
public class AssetAirborneControllerTest
{
	private final static String GOOD_ASSET_ID = "good_asset_id";

	private final static String GOOD_ASSET_NAME = "good_asset_name";

	private final static String GOOD_XML = "good_xml";

	private final static String GOOD_EXTRA_DATA_NAME = "good_extra_data_name";

	private final static String BAD_ASSET_ID_DOES_NOT_EXIST = "bad_asset_id_does_not_exist";

	private final static String BAD_ASSET_ID_NO_FIELD_OF_REGARD = "bad_asset_id_no_field_of_regard";

	private final static String BAD_XML_CONFLICT = "bad_xml_conflict";

	private final static String BAD_XML_CONFLICT_NAME = "bad_xml_name_conflict";

	private final static String BAD_XML_INVALID = "bad_xml_invalid";

	private final static String BAD_XML_ID = "bad_xml_id";

	private final static String BAD_EXTRA_DATA_NAME_EXISTS = "bad_extra_data_name_exists";

	private final static String BAD_EXTRA_DATA_NAME_DOES_NOT_EXIST = "bad_extra_data_name_does_not_exist";

	private final static String BAD_EXTRA_DATA_NAME_INVALID = "\\09";

	private final static String GOOD_DATE_TIME_STRING = "2019-07-01T00:00:00.000Z";

	private final static String BAD_DATE_TIME_STRING = "2019-07-01T70:00:00.000Z";

	@Autowired
	AssetAirborneController assetAirborneController;

	@MockBean
	AssetAirborneService assetAirborneService;

	@Before
	public void setUp()
			throws AssetIdExistsException,
			AssetIdDoesNotExistException,
			AssetExtraDataExistsException,
			AssetNameDoesNotExistException,
			AssetExtraDataDoesNotExistException,
			AssetIdDoesNotMatchException,
			AssetNameExistsException {
		Mockito.when(assetAirborneService.createAsset(ArgumentMatchers.startsWith(GOOD_XML)))
				.thenReturn(GOOD_ASSET_ID);

		Mockito.when(assetAirborneService.createAsset(ArgumentMatchers.startsWith(BAD_XML_CONFLICT)))
				.thenThrow(new AssetIdExistsException(
						GOOD_ASSET_ID));

		Mockito.when(assetAirborneService.createAsset(ArgumentMatchers.startsWith(BAD_XML_CONFLICT_NAME)))
				.thenThrow(new AssetNameExistsException(
						GOOD_ASSET_NAME));

		Mockito.when(assetAirborneService.createAsset(ArgumentMatchers.startsWith(BAD_XML_INVALID)))
				.thenThrow(new XmlBeanDefinitionStoreException(
						"",
						"",
						new SAXParseException(
								"",
								null)));

		Mockito.when(assetAirborneService.createAsset(ArgumentMatchers.startsWith(BAD_XML_ID)))
				.thenReturn("\\09");

		Mockito.doNothing()
				.when(assetAirborneService)
				.createAssetExtraData(	ArgumentMatchers.eq(GOOD_ASSET_ID),
										ArgumentMatchers.eq(GOOD_EXTRA_DATA_NAME),
										ArgumentMatchers.any(ExtraData.class));

		Mockito.doThrow(new AssetExtraDataExistsException(
				GOOD_ASSET_ID,
				BAD_EXTRA_DATA_NAME_EXISTS))
				.when(assetAirborneService)
				.createAssetExtraData(	ArgumentMatchers.eq(GOOD_ASSET_ID),
										ArgumentMatchers.eq(BAD_EXTRA_DATA_NAME_EXISTS),
										ArgumentMatchers.any(ExtraData.class));

		Mockito.doThrow(new AssetIdDoesNotExistException(
				BAD_ASSET_ID_DOES_NOT_EXIST))
				.when(assetAirborneService)
				.createAssetExtraData(	ArgumentMatchers.eq(BAD_ASSET_ID_DOES_NOT_EXIST),
										ArgumentMatchers.eq(GOOD_EXTRA_DATA_NAME),
										ArgumentMatchers.any(ExtraData.class));

		Mockito.doNothing()
				.when(assetAirborneService)
				.createAssetExtraData(	ArgumentMatchers.eq(GOOD_ASSET_ID),
										ArgumentMatchers.eq(BAD_EXTRA_DATA_NAME_INVALID),
										ArgumentMatchers.any(ExtraData.class));

		Mockito.when(assetAirborneService.getAllAssetIds())
				.thenReturn(Collections.singletonList(GOOD_ASSET_ID));

		Mockito.when(assetAirborneService.getAssetById(GOOD_ASSET_ID))
				.thenReturn(GOOD_XML);

		Mockito.when(assetAirborneService.getAssetById(BAD_ASSET_ID_DOES_NOT_EXIST))
				.thenThrow(new AssetIdDoesNotExistException(
						BAD_ASSET_ID_DOES_NOT_EXIST));

		Mockito.when(assetAirborneService.getAllAssetNames())
				.thenReturn(Collections.singletonList(GOOD_ASSET_ID));

		Mockito.when(assetAirborneService.getAssetIdByName(ArgumentMatchers.eq(GOOD_ASSET_ID)))
				.thenReturn(GOOD_ASSET_ID);

		Mockito.when(assetAirborneService.getAssetIdByName(ArgumentMatchers.eq(BAD_ASSET_ID_DOES_NOT_EXIST)))
				.thenThrow(new AssetNameDoesNotExistException(
						BAD_ASSET_ID_DOES_NOT_EXIST));

		Mockito.when(assetAirborneService.getAllAssetExtraDataNames(ArgumentMatchers.eq(GOOD_ASSET_ID)))
				.thenReturn(Collections.singletonList(GOOD_EXTRA_DATA_NAME));

		Mockito.when(assetAirborneService.getAllAssetExtraDataNames(ArgumentMatchers.eq(BAD_ASSET_ID_DOES_NOT_EXIST)))
				.thenThrow(new AssetIdDoesNotExistException(
						BAD_ASSET_ID_DOES_NOT_EXIST));

		Mockito.when(assetAirborneService.getAssetExtraDataByName(	ArgumentMatchers.eq(GOOD_ASSET_ID),
																	ArgumentMatchers.eq(GOOD_EXTRA_DATA_NAME)))
				.thenReturn(new ExtraData());

		Mockito.when(assetAirborneService.getAssetExtraDataByName(	ArgumentMatchers.eq(BAD_ASSET_ID_DOES_NOT_EXIST),
																	ArgumentMatchers.eq(GOOD_EXTRA_DATA_NAME)))
				.thenThrow(new AssetIdDoesNotExistException(
						BAD_ASSET_ID_DOES_NOT_EXIST));

		Mockito.when(assetAirborneService.getAssetExtraDataByName(	ArgumentMatchers.eq(GOOD_ASSET_ID),
																	ArgumentMatchers
																			.eq(BAD_EXTRA_DATA_NAME_DOES_NOT_EXIST)))
				.thenThrow(new AssetExtraDataDoesNotExistException(
						GOOD_ASSET_ID,
						BAD_ASSET_ID_DOES_NOT_EXIST));

		final List<FieldOfRegard> fieldsOfRegard = new ArrayList<>();
		final FieldOfRegard fieldOfRegard = new FieldOfRegard();
		fieldOfRegard.setFieldOfRegardAngleWkt("POINT(0.0 0.0)");
		fieldsOfRegard.add(fieldOfRegard);

		Mockito.when(assetAirborneService.getAssetFieldsOfRegard(	ArgumentMatchers.eq(GOOD_ASSET_ID),
																	ArgumentMatchers.any(DateTime.class),
																	ArgumentMatchers.isNull()))
				.thenReturn(fieldsOfRegard);

		Mockito.when(assetAirborneService.getAssetFieldsOfRegard(	ArgumentMatchers.eq(BAD_ASSET_ID_DOES_NOT_EXIST),
																	ArgumentMatchers.any(DateTime.class),
																	ArgumentMatchers.isNull()))
				.thenThrow(new AssetIdDoesNotExistException(
						BAD_ASSET_ID_DOES_NOT_EXIST));

		Mockito.when(assetAirborneService.getAssetFieldsOfRegard(	ArgumentMatchers.eq(BAD_ASSET_ID_NO_FIELD_OF_REGARD),
																	ArgumentMatchers.any(DateTime.class),
																	ArgumentMatchers.isNull()))
				.thenThrow(new AssetHasNoFieldOfRegardException(
						BAD_ASSET_ID_DOES_NOT_EXIST));

		Mockito.doNothing()
				.when(assetAirborneService)
				.updateAsset(	ArgumentMatchers.eq(GOOD_ASSET_ID),
								ArgumentMatchers.startsWith(GOOD_XML));

		Mockito.doThrow(new AssetIdDoesNotExistException(
				BAD_ASSET_ID_DOES_NOT_EXIST))
				.when(assetAirborneService)
				.updateAsset(	ArgumentMatchers.eq(BAD_ASSET_ID_DOES_NOT_EXIST),
								ArgumentMatchers.startsWith(GOOD_XML));

		Mockito.doThrow(new AssetIdDoesNotMatchException(
				GOOD_ASSET_ID,
				BAD_ASSET_ID_DOES_NOT_EXIST))
				.when(assetAirborneService)
				.updateAsset(	ArgumentMatchers.eq(GOOD_ASSET_ID),
								ArgumentMatchers.startsWith(BAD_XML_ID));

		Mockito.doThrow(new XmlBeanDefinitionStoreException(
				"",
				"",
				new SAXParseException(
						"",
						null)))
				.when(assetAirborneService)
				.updateAsset(	ArgumentMatchers.eq(GOOD_ASSET_ID),
								ArgumentMatchers.startsWith(BAD_XML_INVALID));

		Mockito.doNothing()
				.when(assetAirborneService)
				.updateAssetExtraData(	ArgumentMatchers.eq(GOOD_ASSET_ID),
										ArgumentMatchers.eq(GOOD_EXTRA_DATA_NAME),
										ArgumentMatchers.any(ExtraData.class));

		Mockito.doThrow(new AssetIdDoesNotExistException(
				BAD_ASSET_ID_DOES_NOT_EXIST))
				.when(assetAirborneService)
				.updateAssetExtraData(	ArgumentMatchers.eq(BAD_ASSET_ID_DOES_NOT_EXIST),
										ArgumentMatchers.eq(GOOD_EXTRA_DATA_NAME),
										ArgumentMatchers.any(ExtraData.class));

		Mockito.doThrow(new AssetExtraDataDoesNotExistException(
				GOOD_ASSET_ID,
				BAD_EXTRA_DATA_NAME_DOES_NOT_EXIST))
				.when(assetAirborneService)
				.updateAssetExtraData(	ArgumentMatchers.eq(GOOD_ASSET_ID),
										ArgumentMatchers.eq(BAD_EXTRA_DATA_NAME_DOES_NOT_EXIST),
										ArgumentMatchers.any(ExtraData.class));

		Mockito.doNothing()
				.when(assetAirborneService)
				.deleteAsset(ArgumentMatchers.eq(GOOD_ASSET_ID));

		Mockito.doThrow(new AssetIdDoesNotExistException(
				BAD_ASSET_ID_DOES_NOT_EXIST))
				.when(assetAirborneService)
				.deleteAsset(ArgumentMatchers.eq(BAD_ASSET_ID_DOES_NOT_EXIST));

		Mockito.doNothing()
				.when(assetAirborneService)
				.deleteAssetExtraData(	GOOD_ASSET_ID,
										GOOD_EXTRA_DATA_NAME);

		Mockito.doThrow(new AssetIdDoesNotExistException(
				BAD_ASSET_ID_DOES_NOT_EXIST))
				.when(assetAirborneService)
				.deleteAssetExtraData(	BAD_ASSET_ID_DOES_NOT_EXIST,
										GOOD_EXTRA_DATA_NAME);

		Mockito.doThrow(new AssetExtraDataDoesNotExistException(
				GOOD_ASSET_ID,
				BAD_EXTRA_DATA_NAME_DOES_NOT_EXIST))
				.when(assetAirborneService)
				.deleteAssetExtraData(	GOOD_ASSET_ID,
										BAD_EXTRA_DATA_NAME_DOES_NOT_EXIST);

		Mockito.when(assetAirborneService.getAssetSmears(	Mockito.any(),
															Mockito.isNull()))
				.thenReturn(Collections.emptyList());
	}

	@Test
	public void testCreateAirborne()
			throws AssetIdExistsException,
			AssetNameExistsException {
		final AirborneAssetModel assetModel = new AirborneAssetModel();
		assetModel.setModelXml(GOOD_XML);

		final ResponseEntity<String> response = assetAirborneController.createAirborne(assetModel);

		Assert.assertEquals(HttpStatus.CREATED,
							response.getStatusCode());
		Assert.assertNull(response.getBody());

		final List<String> locationHeaders = response.getHeaders()
				.get("Location");

		Assert.assertNotNull(locationHeaders);
		Assert.assertEquals(1,
							locationHeaders.size());
		Assert.assertEquals("/airborne/" + GOOD_ASSET_ID,
							locationHeaders.get(0));
	}

	@Test(expected = AssetIdExistsException.class)
	public void testCreateAirborneConflict()
			throws AssetIdExistsException,
			AssetNameExistsException {
		final AirborneAssetModel assetModel = new AirborneAssetModel();
		assetModel.setModelXml(BAD_XML_CONFLICT);

		assetAirborneController.createAirborne(assetModel);
	}

	@Test(expected = AssetNameExistsException.class)
	public void testCreateAirborneConflictName()
			throws AssetNameExistsException,
			AssetIdExistsException {
		final AirborneAssetModel assetModel = new AirborneAssetModel();
		assetModel.setModelXml(BAD_XML_CONFLICT_NAME);

		assetAirborneController.createAirborne(assetModel);
	}

	@Test
	public void testCreateAirborneInvalidXml()
			throws AssetIdExistsException,
			AssetNameExistsException {
		final AirborneAssetModel assetModel = new AirborneAssetModel();
		assetModel.setModelXml(BAD_XML_INVALID);

		final ResponseEntity<String> response = assetAirborneController.createAirborne(assetModel);

		Assert.assertEquals(HttpStatus.BAD_REQUEST,
							response.getStatusCode());
		Assert.assertNotNull(response.getBody());
	}

	@Test
	public void testCreateAirborneBadId()
			throws AssetIdExistsException,
			AssetNameExistsException {
		final AirborneAssetModel assetModel = new AirborneAssetModel();
		assetModel.setModelXml(BAD_XML_ID);

		final ResponseEntity<String> response = assetAirborneController.createAirborne(assetModel);

		Assert.assertEquals(HttpStatus.BAD_REQUEST,
							response.getStatusCode());
		Assert.assertNotNull(response.getBody());
	}

	@Test
	public void testCreateAirborneFile()
			throws AssetIdExistsException,
			AssetNameExistsException {
		final MultipartFile file = new MockMultipartFile(
				"file",
				GOOD_XML.getBytes());

		final ResponseEntity<String> response = assetAirborneController.createAirborneFile(file);

		Assert.assertEquals(HttpStatus.CREATED,
							response.getStatusCode());
		Assert.assertNull(response.getBody());

		final List<String> locationHeaders = response.getHeaders()
				.get("Location");

		Assert.assertNotNull(locationHeaders);
		Assert.assertEquals(1,
							locationHeaders.size());
		Assert.assertEquals("/airborne/" + GOOD_ASSET_ID,
							locationHeaders.get(0));
	}

	@Test(expected = AssetIdExistsException.class)
	public void testCreateAirborneFileConflict()
			throws AssetIdExistsException,
			AssetNameExistsException {
		final MultipartFile file = new MockMultipartFile(
				"file",
				BAD_XML_CONFLICT.getBytes());

		assetAirborneController.createAirborneFile(file);
	}

	@Test(expected = AssetNameExistsException.class)
	public void testCreateAirborneFileConflictName()
			throws AssetNameExistsException,
			AssetIdExistsException {
		final MultipartFile file = new MockMultipartFile(
				"file",
				BAD_XML_CONFLICT_NAME.getBytes());

		assetAirborneController.createAirborneFile(file);
	}

	@Test
	public void testCreateAirborneFileInvalidXml()
			throws AssetIdExistsException,
			AssetNameExistsException {
		final MultipartFile file = new MockMultipartFile(
				"file",
				BAD_XML_INVALID.getBytes());

		final ResponseEntity<String> response = assetAirborneController.createAirborneFile(file);

		Assert.assertEquals(HttpStatus.BAD_REQUEST,
							response.getStatusCode());
		Assert.assertNotNull(response.getBody());
	}

	@Test
	public void testCreateAirborneFileBadId()
			throws AssetIdExistsException,
			AssetNameExistsException {
		final MultipartFile file = new MockMultipartFile(
				"file",
				BAD_XML_ID.getBytes());

		final ResponseEntity<String> response = assetAirborneController.createAirborneFile(file);

		Assert.assertEquals(HttpStatus.BAD_REQUEST,
							response.getStatusCode());
		Assert.assertNotNull(response.getBody());
	}

	@Test
	public void testCreateAirborneFileBadIO()
			throws IOException,
			AssetIdExistsException,
			AssetNameExistsException {
		final MultipartFile file = Mockito.mock(MockMultipartFile.class);

		Mockito.when(file.getInputStream())
				.thenThrow(new IOException(
						"Error reading XML"));

		final ResponseEntity<String> response = assetAirborneController.createAirborneFile(file);

		Assert.assertEquals(HttpStatus.BAD_REQUEST,
							response.getStatusCode());
		Assert.assertNotNull(response.getBody());
	}

	@Test
	public void testCreateAirborneExtraData()
			throws AssetIdDoesNotExistException,
			AssetExtraDataExistsException {
		final ExtraData extraData = new ExtraData();

		final ResponseEntity<String> response = assetAirborneController.createAirborneExtraData(GOOD_ASSET_ID,
																								GOOD_EXTRA_DATA_NAME,
																								extraData);

		Assert.assertEquals(HttpStatus.CREATED,
							response.getStatusCode());
		Assert.assertNull(response.getBody());

		final List<String> locationHeaders = response.getHeaders()
				.get("Location");

		Assert.assertNotNull(locationHeaders);
		Assert.assertEquals(1,
							locationHeaders.size());
		Assert.assertEquals("/airborne/" + GOOD_ASSET_ID + "/extra/" + GOOD_EXTRA_DATA_NAME,
							locationHeaders.get(0));
	}

	@Test(expected = AssetIdDoesNotExistException.class)
	public void testCreateAirborneExtraDataIdDoesNotExist()
			throws AssetIdDoesNotExistException,
			AssetExtraDataExistsException {
		final ExtraData extraData = new ExtraData();

		assetAirborneController.createAirborneExtraData(BAD_ASSET_ID_DOES_NOT_EXIST,
														GOOD_EXTRA_DATA_NAME,
														extraData);
	}

	@Test(expected = AssetExtraDataExistsException.class)
	public void testCreateAirborneExtraDataConflict()
			throws AssetExtraDataExistsException,
			AssetIdDoesNotExistException {
		final ExtraData extraData = new ExtraData();

		assetAirborneController.createAirborneExtraData(GOOD_ASSET_ID,
														BAD_EXTRA_DATA_NAME_EXISTS,
														extraData);
	}

	@Test
	public void testCreateAirborneExtraDataBadName()
			throws AssetIdDoesNotExistException,
			AssetExtraDataExistsException {
		final ExtraData extraData = new ExtraData();

		final ResponseEntity<String> response = assetAirborneController.createAirborneExtraData(GOOD_ASSET_ID,
																								BAD_EXTRA_DATA_NAME_INVALID,
																								extraData);

		Assert.assertEquals(HttpStatus.BAD_REQUEST,
							response.getStatusCode());
		Assert.assertNotNull(response.getBody());
	}

	@Test
	public void testCreateAirborneExtraDataFile()
			throws AssetIdDoesNotExistException,
			AssetExtraDataExistsException {
		final MultipartFile file = new MockMultipartFile(
				"file",
				"".getBytes());

		final ResponseEntity<String> response = assetAirborneController.createAirborneExtraDataFile(GOOD_ASSET_ID,
																									GOOD_EXTRA_DATA_NAME,
																									"",
																									file);

		Assert.assertEquals(HttpStatus.CREATED,
							response.getStatusCode());
		Assert.assertNull(response.getBody());

		final List<String> locationHeaders = response.getHeaders()
				.get("Location");

		Assert.assertNotNull(locationHeaders);
		Assert.assertEquals(1,
							locationHeaders.size());
		Assert.assertEquals("/airborne/" + GOOD_ASSET_ID + "/extra/" + GOOD_EXTRA_DATA_NAME,
							locationHeaders.get(0));
	}

	@Test(expected = AssetIdDoesNotExistException.class)
	public void testCreateAirborneExtraDataFileIdDoesNotExist()
			throws AssetIdDoesNotExistException,
			AssetExtraDataExistsException {
		final MultipartFile file = new MockMultipartFile(
				"file",
				"".getBytes());

		assetAirborneController.createAirborneExtraDataFile(BAD_ASSET_ID_DOES_NOT_EXIST,
															GOOD_EXTRA_DATA_NAME,
															"",
															file);
	}

	@Test(expected = AssetExtraDataExistsException.class)
	public void testCreateAirborneExtraDataFileConflict()
			throws AssetExtraDataExistsException,
			AssetIdDoesNotExistException {
		final MultipartFile file = new MockMultipartFile(
				"file",
				"".getBytes());

		assetAirborneController.createAirborneExtraDataFile(GOOD_ASSET_ID,
															BAD_EXTRA_DATA_NAME_EXISTS,
															"",
															file);
	}

	@Test
	public void testCreateAirborneExtraDataFileBadName()
			throws AssetIdDoesNotExistException,
			AssetExtraDataExistsException {
		final MultipartFile file = new MockMultipartFile(
				"file",
				"".getBytes());

		final ResponseEntity<String> response = assetAirborneController.createAirborneExtraDataFile(GOOD_ASSET_ID,
																									BAD_EXTRA_DATA_NAME_INVALID,
																									"",
																									file);

		Assert.assertEquals(HttpStatus.BAD_REQUEST,
							response.getStatusCode());
		Assert.assertNotNull(response.getBody());
	}

	@Test
	public void testCreateAirborneExtraDataFileBadIO()
			throws IOException,
			AssetIdDoesNotExistException,
			AssetExtraDataExistsException {
		final MultipartFile file = Mockito.mock(MockMultipartFile.class);

		Mockito.when(file.getInputStream())
				.thenThrow(new IOException(
						"Error reading extra data"));

		final ResponseEntity<String> response = assetAirborneController.createAirborneExtraDataFile(GOOD_ASSET_ID,
																									GOOD_EXTRA_DATA_NAME,
																									"",
																									file);

		Assert.assertEquals(HttpStatus.BAD_REQUEST,
							response.getStatusCode());
		Assert.assertNotNull(response.getBody());
	}

	@Test
	public void testGetAirborneAll() {
		final ResponseEntity<IdList> response = assetAirborneController.getAirborneAll();

		Assert.assertEquals(HttpStatus.OK,
							response.getStatusCode());

		final IdList responseBody = response.getBody();

		Assert.assertNotNull(responseBody);

		final List<String> ids = responseBody.getIds();

		Assert.assertNotNull(ids);
		Assert.assertEquals(1,
							ids.size());
		Assert.assertEquals(GOOD_ASSET_ID,
							ids.get(0));
	}

	@Test
	public void testGetAirborneId()
			throws AssetIdDoesNotExistException {
		final ResponseEntity<AirborneAssetModel> response = assetAirborneController.getAirborneId(GOOD_ASSET_ID);

		Assert.assertEquals(HttpStatus.OK,
							response.getStatusCode());
		Assert.assertNotNull(response.getBody());
	}

	@Test(expected = AssetIdDoesNotExistException.class)
	public void testGetAirborneIdDoesNotExist()
			throws AssetIdDoesNotExistException {
		assetAirborneController.getAirborneId(BAD_ASSET_ID_DOES_NOT_EXIST);
	}

	@Test
	public void testGetAirborneNames() {
		final ResponseEntity<NameList> response = assetAirborneController.getAirborneNames();

		Assert.assertEquals(HttpStatus.OK,
							response.getStatusCode());

		final NameList responseBody = response.getBody();

		Assert.assertNotNull(responseBody);

		final List<String> names = responseBody.getNames();

		Assert.assertNotNull(names);
		Assert.assertEquals(1,
							names.size());
		Assert.assertEquals(GOOD_ASSET_ID,
							names.get(0));
	}

	@Test
	public void getAirborneName()
			throws AssetNameDoesNotExistException {
		final ResponseEntity<String> response = assetAirborneController.getAirborneName(GOOD_ASSET_ID);

		Assert.assertEquals(HttpStatus.OK,
							response.getStatusCode());

		final String responseBody = response.getBody();

		Assert.assertNotNull(responseBody);
		Assert.assertEquals(GOOD_ASSET_ID,
							responseBody);
	}

	@Test(expected = AssetNameDoesNotExistException.class)
	public void getAirborneNameDoesNotExist()
			throws AssetNameDoesNotExistException {
		assetAirborneController.getAirborneName(BAD_ASSET_ID_DOES_NOT_EXIST);
	}

	@Test
	public void testGetAirborneExtra()
			throws AssetIdDoesNotExistException {
		final ResponseEntity<NameList> response = assetAirborneController.getAirborneExtra(GOOD_ASSET_ID);

		Assert.assertEquals(HttpStatus.OK,
							response.getStatusCode());

		final NameList responseBody = response.getBody();

		Assert.assertNotNull(responseBody);

		final List<String> names = responseBody.getNames();

		Assert.assertNotNull(names);
		Assert.assertEquals(1,
							names.size());
		Assert.assertEquals(GOOD_EXTRA_DATA_NAME,
							names.get(0));
	}

	@Test(expected = AssetIdDoesNotExistException.class)
	public void testGetAirborneExtraIdDoesNotExist()
			throws AssetIdDoesNotExistException {
		assetAirborneController.getAirborneExtra(BAD_ASSET_ID_DOES_NOT_EXIST);
	}

	@Test
	public void testGetAirborneExtraName()
			throws AssetIdDoesNotExistException,
			AssetExtraDataDoesNotExistException {
		final ResponseEntity<ExtraData> response = assetAirborneController.getAirborneExtraName(GOOD_ASSET_ID,
																								GOOD_EXTRA_DATA_NAME);

		Assert.assertEquals(HttpStatus.OK,
							response.getStatusCode());
		Assert.assertNotNull(response.getBody());
	}

	@Test(expected = AssetIdDoesNotExistException.class)
	public void testGetAirborneExtraNameIdDoesNotExist()
			throws AssetIdDoesNotExistException,
			AssetExtraDataDoesNotExistException {
		assetAirborneController.getAirborneExtraName(	BAD_ASSET_ID_DOES_NOT_EXIST,
														GOOD_EXTRA_DATA_NAME);
	}

	@Test(expected = AssetExtraDataDoesNotExistException.class)
	public void testGetAirborneExtraNameDoesNotExist()
			throws AssetExtraDataDoesNotExistException,
			AssetIdDoesNotExistException {
		assetAirborneController.getAirborneExtraName(	GOOD_ASSET_ID,
														BAD_EXTRA_DATA_NAME_DOES_NOT_EXIST);
	}

	@Test
	public void testgetAirborneFieldsOfRegard()
			throws AssetHasNoFieldOfRegardException,
			AssetIdDoesNotExistException,
			BadRequestException {
		final ResponseEntity<List<FieldOfRegard>> response = assetAirborneController
				.getAirborneFieldsOfRegard(	GOOD_ASSET_ID,
											GOOD_DATE_TIME_STRING);

		Assert.assertNotNull(response);
		Assert.assertNotNull(response.getBody());

		final List<FieldOfRegard> fieldsOfRegard = response.getBody();

		fieldsOfRegard.forEach(fieldOfRegard -> Assert.assertNotNull(fieldOfRegard.getFieldOfRegardAngleWkt()));
	}

	@Test(expected = AssetIdDoesNotExistException.class)
	public void testgetAirborneFieldsOfRegardIdDoesNotExist()
			throws AssetHasNoFieldOfRegardException,
			AssetIdDoesNotExistException,
			BadRequestException {
		assetAirborneController.getAirborneFieldsOfRegard(	BAD_ASSET_ID_DOES_NOT_EXIST,
															GOOD_DATE_TIME_STRING);
	}

	@Test(expected = BadRequestException.class)
	public void testgetAirborneFieldsOfRegardInvalidDateTime()
			throws AssetHasNoFieldOfRegardException,
			AssetIdDoesNotExistException,
			BadRequestException {
		assetAirborneController.getAirborneFieldsOfRegard(	GOOD_ASSET_ID,
															BAD_DATE_TIME_STRING);
	}

	@Test(expected = AssetHasNoFieldOfRegardException.class)
	public void testgetAirborneFieldsOfRegardNoFieldOfRegard()
			throws AssetHasNoFieldOfRegardException,
			AssetIdDoesNotExistException,
			BadRequestException {
		assetAirborneController.getAirborneFieldsOfRegard(	BAD_ASSET_ID_NO_FIELD_OF_REGARD,
															GOOD_DATE_TIME_STRING);
	}

	@Test
	public void testUpdateAirborne()
			throws AssetIdDoesNotExistException,
			AssetIdDoesNotMatchException {
		final AirborneAssetModel assetModel = new AirborneAssetModel();
		assetModel.setModelXml(GOOD_XML);

		final ResponseEntity<String> response = assetAirborneController.updateAirborne(	GOOD_ASSET_ID,
																						assetModel);

		Assert.assertEquals(HttpStatus.OK,
							response.getStatusCode());
		Assert.assertNull(response.getBody());
	}

	@Test(expected = AssetIdDoesNotExistException.class)
	public void testUpdateAirborneDoesNotExist()
			throws AssetIdDoesNotExistException,
			AssetIdDoesNotMatchException {
		final AirborneAssetModel assetModel = new AirborneAssetModel();
		assetModel.setModelXml(GOOD_XML);

		assetAirborneController.updateAirborne(	BAD_ASSET_ID_DOES_NOT_EXIST,
												assetModel);
	}

	@Test(expected = AssetIdDoesNotMatchException.class)
	public void testUpdateAirborneDoesNotMatch()
			throws AssetIdDoesNotMatchException,
			AssetIdDoesNotExistException {
		final AirborneAssetModel assetModel = new AirborneAssetModel();
		assetModel.setModelXml(BAD_XML_ID);

		assetAirborneController.updateAirborne(	GOOD_ASSET_ID,
												assetModel);
	}

	@Test
	public void testUpdateAirborneInvalidXml()
			throws AssetIdDoesNotExistException,
			AssetIdDoesNotMatchException {
		final AirborneAssetModel assetModel = new AirborneAssetModel();
		assetModel.setModelXml(BAD_XML_INVALID);

		final ResponseEntity<String> response = assetAirborneController.updateAirborne(	GOOD_ASSET_ID,
																						assetModel);

		Assert.assertEquals(HttpStatus.BAD_REQUEST,
							response.getStatusCode());
		Assert.assertNotNull(response.getBody());
	}

	@Test
	public void testUpdateAirborneFile()
			throws AssetIdDoesNotExistException,
			AssetIdDoesNotMatchException {
		final MultipartFile file = new MockMultipartFile(
				"file",
				GOOD_XML.getBytes());

		final ResponseEntity<String> response = assetAirborneController.updateAirborneFile(	GOOD_ASSET_ID,
																							file);

		Assert.assertEquals(HttpStatus.OK,
							response.getStatusCode());
		Assert.assertNull(response.getBody());
	}

	@Test(expected = AssetIdDoesNotExistException.class)
	public void testUpdateAirborneFileDoesNotExist()
			throws AssetIdDoesNotExistException,
			AssetIdDoesNotMatchException {
		final MultipartFile file = new MockMultipartFile(
				"file",
				GOOD_XML.getBytes());

		assetAirborneController.updateAirborneFile(	BAD_ASSET_ID_DOES_NOT_EXIST,
													file);
	}

	@Test(expected = AssetIdDoesNotMatchException.class)
	public void testUpdateAirborneFileDoesNotMatch()
			throws AssetIdDoesNotMatchException,
			AssetIdDoesNotExistException {
		final MultipartFile file = new MockMultipartFile(
				"file",
				BAD_XML_ID.getBytes());

		assetAirborneController.updateAirborneFile(	GOOD_ASSET_ID,
													file);
	}

	@Test
	public void testUpdateAirborneFileInvalidXml()
			throws AssetIdDoesNotExistException,
			AssetIdDoesNotMatchException {
		final MultipartFile file = new MockMultipartFile(
				"file",
				BAD_XML_INVALID.getBytes());

		final ResponseEntity<String> response = assetAirborneController.updateAirborneFile(	GOOD_ASSET_ID,
																							file);

		Assert.assertEquals(HttpStatus.BAD_REQUEST,
							response.getStatusCode());
		Assert.assertNotNull(response.getBody());
	}

	@Test
	public void testUpdateAirborneFileBadIO()
			throws AssetIdDoesNotExistException,
			AssetIdDoesNotMatchException,
			IOException {
		final MultipartFile file = Mockito.mock(MockMultipartFile.class);

		Mockito.when(file.getInputStream())
				.thenThrow(new IOException(
						"Error reading XML"));

		final ResponseEntity<String> response = assetAirborneController.updateAirborneFile(	GOOD_ASSET_ID,
																							file);

		Assert.assertEquals(HttpStatus.BAD_REQUEST,
							response.getStatusCode());
		Assert.assertNotNull(response.getBody());
	}

	@Test
	public void testUpdateAirborneExtraData()
			throws AssetIdDoesNotExistException,
			AssetExtraDataDoesNotExistException {
		final ExtraData extraData = new ExtraData();

		final ResponseEntity<String> response = assetAirborneController.updateAirborneExtraData(GOOD_ASSET_ID,
																								GOOD_EXTRA_DATA_NAME,
																								extraData);

		Assert.assertEquals(HttpStatus.OK,
							response.getStatusCode());
		Assert.assertNull(response.getBody());
	}

	@Test(expected = AssetIdDoesNotExistException.class)
	public void testUpdateAirborneExtraDataIdDoesNotExist()
			throws AssetIdDoesNotExistException,
			AssetExtraDataDoesNotExistException {
		final ExtraData extraData = new ExtraData();

		assetAirborneController.updateAirborneExtraData(BAD_ASSET_ID_DOES_NOT_EXIST,
														GOOD_EXTRA_DATA_NAME,
														extraData);
	}

	@Test(expected = AssetExtraDataDoesNotExistException.class)
	public void testUpdateAirborneExtraDataDoesNotExist()
			throws AssetIdDoesNotExistException,
			AssetExtraDataDoesNotExistException {
		final ExtraData extraData = new ExtraData();

		assetAirborneController.updateAirborneExtraData(GOOD_ASSET_ID,
														BAD_EXTRA_DATA_NAME_DOES_NOT_EXIST,
														extraData);
	}

	@Test
	public void testUpdateAirborneExtraDataFile()
			throws AssetIdDoesNotExistException,
			AssetExtraDataDoesNotExistException {
		final MultipartFile file = new MockMultipartFile(
				"file",
				"".getBytes());

		final ResponseEntity<String> response = assetAirborneController.updateAirborneExtraDataFile(GOOD_ASSET_ID,
																									GOOD_EXTRA_DATA_NAME,
																									"",
																									file);

		Assert.assertEquals(HttpStatus.OK,
							response.getStatusCode());
		Assert.assertNull(response.getBody());
	}

	@Test(expected = AssetIdDoesNotExistException.class)
	public void testUpdateAirborneExtraDataFileIdDoesNotExist()
			throws AssetIdDoesNotExistException,
			AssetExtraDataDoesNotExistException {
		final MultipartFile file = new MockMultipartFile(
				"file",
				"".getBytes());

		assetAirborneController.updateAirborneExtraDataFile(BAD_ASSET_ID_DOES_NOT_EXIST,
															GOOD_EXTRA_DATA_NAME,
															"",
															file);
	}

	@Test(expected = AssetExtraDataDoesNotExistException.class)
	public void testUpdateAirborneExtraDataFileDoesNotExist()
			throws AssetIdDoesNotExistException,
			AssetExtraDataDoesNotExistException {
		final MultipartFile file = new MockMultipartFile(
				"file",
				"".getBytes());

		assetAirborneController.updateAirborneExtraDataFile(GOOD_ASSET_ID,
															BAD_EXTRA_DATA_NAME_DOES_NOT_EXIST,
															"",
															file);
	}

	@Test
	public void testUpdateAirborneExtraDataFileBadIO()
			throws AssetIdDoesNotExistException,
			AssetExtraDataDoesNotExistException,
			IOException {
		final MultipartFile file = Mockito.mock(MockMultipartFile.class);

		Mockito.when(file.getInputStream())
				.thenThrow(new IOException(
						"Error reading extra data"));

		final ResponseEntity<String> response = assetAirborneController.updateAirborneExtraDataFile(GOOD_ASSET_ID,
																									GOOD_EXTRA_DATA_NAME,
																									"",
																									file);

		Assert.assertEquals(HttpStatus.BAD_REQUEST,
							response.getStatusCode());
		Assert.assertNotNull(response.getBody());
	}

	@Test
	public void testDeleteAirborne()
			throws AssetIdDoesNotExistException {
		final ResponseEntity<String> response = assetAirborneController.deleteAirborne(GOOD_ASSET_ID);

		Assert.assertEquals(HttpStatus.OK,
							response.getStatusCode());
		Assert.assertNull(response.getBody());
	}

	@Test(expected = AssetIdDoesNotExistException.class)
	public void testDeleteAirborneDoesNotExist()
			throws AssetIdDoesNotExistException {
		assetAirborneController.deleteAirborne(BAD_ASSET_ID_DOES_NOT_EXIST);
	}

	@Test
	public void testDeleteAirborneExtraData()
			throws AssetIdDoesNotExistException,
			AssetExtraDataDoesNotExistException {
		final ResponseEntity<String> response = assetAirborneController.deleteAirborneExtraData(GOOD_ASSET_ID,
																								GOOD_EXTRA_DATA_NAME);

		Assert.assertEquals(HttpStatus.OK,
							response.getStatusCode());
		Assert.assertNull(response.getBody());
	}

	@Test(expected = AssetIdDoesNotExistException.class)
	public void testDeleteAirborneExtraDataIdDoesNotExist()
			throws AssetIdDoesNotExistException,
			AssetExtraDataDoesNotExistException {
		assetAirborneController.deleteAirborneExtraData(BAD_ASSET_ID_DOES_NOT_EXIST,
														GOOD_EXTRA_DATA_NAME);
	}

	@Test(expected = AssetExtraDataDoesNotExistException.class)
	public void testDeleteAirborneExtraDataDoesNotExist()
			throws AssetIdDoesNotExistException,
			AssetExtraDataDoesNotExistException {
		assetAirborneController.deleteAirborneExtraData(GOOD_ASSET_ID,
														BAD_EXTRA_DATA_NAME_DOES_NOT_EXIST);
	}

	@Test
	public void testGetAirborneSmears()
			throws AssetIdDoesNotExistException {
		final AssetSmearWithBeamsRequest assetSmearWithBeamsRequest = new AssetSmearWithBeamsRequest();

		final ResponseEntity<List<AssetSmear>> response = assetAirborneController
				.getAirborneSmears(assetSmearWithBeamsRequest);

		Assert.assertNotNull(response);
		Assert.assertEquals(HttpStatus.OK,
							response.getStatusCode());
		Assert.assertNotNull(response.getBody());
		Assert.assertTrue(response.getBody()
				.isEmpty());
	}
}
