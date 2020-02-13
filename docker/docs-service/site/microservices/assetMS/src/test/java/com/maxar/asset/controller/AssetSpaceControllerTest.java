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
import com.maxar.asset.service.AssetSpaceService;
import com.maxar.asset.model.AssetSmear;
import com.maxar.asset.model.AssetSmearWithBeamsRequest;
import com.maxar.asset.model.ExtraData;
import com.maxar.asset.model.FieldOfRegard;
import com.maxar.asset.model.IdList;
import com.maxar.asset.model.NameList;
import com.maxar.asset.model.SpaceAssetModel;
import com.maxar.common.exception.BadRequestException;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@TestPropertySource(locations = "classpath:testassetms.properties")
public class AssetSpaceControllerTest
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
	AssetSpaceController assetSpaceController;

	@MockBean
	AssetSpaceService assetSpaceService;

	@Before
	public void setUp()
			throws AssetIdExistsException,
			AssetIdDoesNotExistException,
			AssetExtraDataExistsException,
			AssetNameDoesNotExistException,
			AssetExtraDataDoesNotExistException,
			AssetIdDoesNotMatchException,
			AssetNameExistsException {
		Mockito.when(assetSpaceService.createAsset(ArgumentMatchers.startsWith(GOOD_XML)))
				.thenReturn(GOOD_ASSET_ID);

		Mockito.when(assetSpaceService.createAsset(ArgumentMatchers.startsWith(BAD_XML_CONFLICT)))
				.thenThrow(new AssetIdExistsException(
						GOOD_ASSET_ID));

		Mockito.when(assetSpaceService.createAsset(ArgumentMatchers.startsWith(BAD_XML_CONFLICT_NAME)))
				.thenThrow(new AssetNameExistsException(
						GOOD_ASSET_NAME));

		Mockito.when(assetSpaceService.createAsset(ArgumentMatchers.startsWith(BAD_XML_INVALID)))
				.thenThrow(new XmlBeanDefinitionStoreException(
						"",
						"",
						new SAXParseException(
								"",
								null)));

		Mockito.when(assetSpaceService.createAsset(ArgumentMatchers.startsWith(BAD_XML_ID)))
				.thenReturn("\\09");

		Mockito.doNothing()
				.when(assetSpaceService)
				.createAssetExtraData(	ArgumentMatchers.eq(GOOD_ASSET_ID),
										ArgumentMatchers.eq(GOOD_EXTRA_DATA_NAME),
										ArgumentMatchers.any(ExtraData.class));

		Mockito.doThrow(new AssetExtraDataExistsException(
				GOOD_ASSET_ID,
				BAD_EXTRA_DATA_NAME_EXISTS))
				.when(assetSpaceService)
				.createAssetExtraData(	ArgumentMatchers.eq(GOOD_ASSET_ID),
										ArgumentMatchers.eq(BAD_EXTRA_DATA_NAME_EXISTS),
										ArgumentMatchers.any(ExtraData.class));

		Mockito.doThrow(new AssetIdDoesNotExistException(
				BAD_ASSET_ID_DOES_NOT_EXIST))
				.when(assetSpaceService)
				.createAssetExtraData(	ArgumentMatchers.eq(BAD_ASSET_ID_DOES_NOT_EXIST),
										ArgumentMatchers.eq(GOOD_EXTRA_DATA_NAME),
										ArgumentMatchers.any(ExtraData.class));

		Mockito.doNothing()
				.when(assetSpaceService)
				.createAssetExtraData(	ArgumentMatchers.eq(GOOD_ASSET_ID),
										ArgumentMatchers.eq(BAD_EXTRA_DATA_NAME_INVALID),
										ArgumentMatchers.any(ExtraData.class));

		Mockito.when(assetSpaceService.getAllAssetIds())
				.thenReturn(Collections.singletonList(GOOD_ASSET_ID));

		Mockito.when(assetSpaceService.getAssetById(GOOD_ASSET_ID))
				.thenReturn(GOOD_XML);

		Mockito.when(assetSpaceService.getAssetById(BAD_ASSET_ID_DOES_NOT_EXIST))
				.thenThrow(new AssetIdDoesNotExistException(
						BAD_ASSET_ID_DOES_NOT_EXIST));

		Mockito.when(assetSpaceService.getAllAssetNames())
				.thenReturn(Collections.singletonList(GOOD_ASSET_ID));

		Mockito.when(assetSpaceService.getAssetIdByName(ArgumentMatchers.eq(GOOD_ASSET_ID)))
				.thenReturn(GOOD_ASSET_ID);

		Mockito.when(assetSpaceService.getAssetIdByName(ArgumentMatchers.eq(BAD_ASSET_ID_DOES_NOT_EXIST)))
				.thenThrow(new AssetNameDoesNotExistException(
						BAD_ASSET_ID_DOES_NOT_EXIST));

		Mockito.when(assetSpaceService.getAllAssetExtraDataNames(ArgumentMatchers.eq(GOOD_ASSET_ID)))
				.thenReturn(Collections.singletonList(GOOD_EXTRA_DATA_NAME));

		Mockito.when(assetSpaceService.getAllAssetExtraDataNames(ArgumentMatchers.eq(BAD_ASSET_ID_DOES_NOT_EXIST)))
				.thenThrow(new AssetIdDoesNotExistException(
						BAD_ASSET_ID_DOES_NOT_EXIST));

		Mockito.when(assetSpaceService.getAssetExtraDataByName(	ArgumentMatchers.eq(GOOD_ASSET_ID),
																ArgumentMatchers.eq(GOOD_EXTRA_DATA_NAME)))
				.thenReturn(new ExtraData());

		Mockito.when(assetSpaceService.getAssetExtraDataByName(	ArgumentMatchers.eq(BAD_ASSET_ID_DOES_NOT_EXIST),
																ArgumentMatchers.eq(GOOD_EXTRA_DATA_NAME)))
				.thenThrow(new AssetIdDoesNotExistException(
						BAD_ASSET_ID_DOES_NOT_EXIST));

		Mockito.when(assetSpaceService.getAssetExtraDataByName(	ArgumentMatchers.eq(GOOD_ASSET_ID),
																ArgumentMatchers
																		.eq(BAD_EXTRA_DATA_NAME_DOES_NOT_EXIST)))
				.thenThrow(new AssetExtraDataDoesNotExistException(
						GOOD_ASSET_ID,
						BAD_ASSET_ID_DOES_NOT_EXIST));

		final FieldOfRegard fieldOfRegard = new FieldOfRegard();
		fieldOfRegard.setFieldOfRegardAngleWkt("POINT(0.0 0.0)");
		final List<FieldOfRegard> fieldsOfRegard = new ArrayList<>();
		fieldsOfRegard.add(fieldOfRegard);

		Mockito.when(assetSpaceService.getAssetFieldsOfRegard(	ArgumentMatchers.eq(GOOD_ASSET_ID),
																ArgumentMatchers.any(DateTime.class),
																ArgumentMatchers.anyString()))
				.thenReturn(fieldsOfRegard);

		Mockito.when(assetSpaceService.getAssetFieldsOfRegard(	ArgumentMatchers.eq(BAD_ASSET_ID_DOES_NOT_EXIST),
																ArgumentMatchers.any(DateTime.class),
																ArgumentMatchers.anyString()))
				.thenThrow(new AssetIdDoesNotExistException(
						BAD_ASSET_ID_DOES_NOT_EXIST));

		Mockito.when(assetSpaceService.getAssetFieldsOfRegard(	ArgumentMatchers.eq(BAD_ASSET_ID_NO_FIELD_OF_REGARD),
																ArgumentMatchers.any(DateTime.class),
																ArgumentMatchers.anyString()))
				.thenThrow(new AssetHasNoFieldOfRegardException(
						BAD_ASSET_ID_DOES_NOT_EXIST));

		Mockito.doNothing()
				.when(assetSpaceService)
				.updateAsset(	ArgumentMatchers.eq(GOOD_ASSET_ID),
								ArgumentMatchers.startsWith(GOOD_XML));

		Mockito.doThrow(new AssetIdDoesNotExistException(
				BAD_ASSET_ID_DOES_NOT_EXIST))
				.when(assetSpaceService)
				.updateAsset(	ArgumentMatchers.eq(BAD_ASSET_ID_DOES_NOT_EXIST),
								ArgumentMatchers.startsWith(GOOD_XML));

		Mockito.doThrow(new AssetIdDoesNotMatchException(
				GOOD_ASSET_ID,
				BAD_ASSET_ID_DOES_NOT_EXIST))
				.when(assetSpaceService)
				.updateAsset(	ArgumentMatchers.eq(GOOD_ASSET_ID),
								ArgumentMatchers.startsWith(BAD_XML_ID));

		Mockito.doThrow(new XmlBeanDefinitionStoreException(
				"",
				"",
				new SAXParseException(
						"",
						null)))
				.when(assetSpaceService)
				.updateAsset(	ArgumentMatchers.eq(GOOD_ASSET_ID),
								ArgumentMatchers.startsWith(BAD_XML_INVALID));

		Mockito.doNothing()
				.when(assetSpaceService)
				.updateAssetExtraData(	ArgumentMatchers.eq(GOOD_ASSET_ID),
										ArgumentMatchers.eq(GOOD_EXTRA_DATA_NAME),
										ArgumentMatchers.any(ExtraData.class));

		Mockito.doThrow(new AssetIdDoesNotExistException(
				BAD_ASSET_ID_DOES_NOT_EXIST))
				.when(assetSpaceService)
				.updateAssetExtraData(	ArgumentMatchers.eq(BAD_ASSET_ID_DOES_NOT_EXIST),
										ArgumentMatchers.eq(GOOD_EXTRA_DATA_NAME),
										ArgumentMatchers.any(ExtraData.class));

		Mockito.doThrow(new AssetExtraDataDoesNotExistException(
				GOOD_ASSET_ID,
				BAD_EXTRA_DATA_NAME_DOES_NOT_EXIST))
				.when(assetSpaceService)
				.updateAssetExtraData(	ArgumentMatchers.eq(GOOD_ASSET_ID),
										ArgumentMatchers.eq(BAD_EXTRA_DATA_NAME_DOES_NOT_EXIST),
										ArgumentMatchers.any(ExtraData.class));

		Mockito.doNothing()
				.when(assetSpaceService)
				.deleteAsset(ArgumentMatchers.eq(GOOD_ASSET_ID));

		Mockito.doThrow(new AssetIdDoesNotExistException(
				BAD_ASSET_ID_DOES_NOT_EXIST))
				.when(assetSpaceService)
				.deleteAsset(ArgumentMatchers.eq(BAD_ASSET_ID_DOES_NOT_EXIST));

		Mockito.doNothing()
				.when(assetSpaceService)
				.deleteAssetExtraData(	GOOD_ASSET_ID,
										GOOD_EXTRA_DATA_NAME);

		Mockito.doThrow(new AssetIdDoesNotExistException(
				BAD_ASSET_ID_DOES_NOT_EXIST))
				.when(assetSpaceService)
				.deleteAssetExtraData(	BAD_ASSET_ID_DOES_NOT_EXIST,
										GOOD_EXTRA_DATA_NAME);

		Mockito.doThrow(new AssetExtraDataDoesNotExistException(
				GOOD_ASSET_ID,
				BAD_EXTRA_DATA_NAME_DOES_NOT_EXIST))
				.when(assetSpaceService)
				.deleteAssetExtraData(	GOOD_ASSET_ID,
										BAD_EXTRA_DATA_NAME_DOES_NOT_EXIST);

		Mockito.when(assetSpaceService.getAssetSmears(	Mockito.any(),
														Mockito.isNull()))
				.thenReturn(Collections.emptyList());

		Mockito.when(assetSpaceService.getAssetSmears(	Mockito.any(),
														Mockito.eq("")))
				.thenReturn(Collections.emptyList());

		Mockito.when(assetSpaceService.getAssetSmears(	Mockito.any(),
														Mockito.eq("J2")))
				.thenReturn(Collections.emptyList());

		Mockito.when(assetSpaceService.getAssetSmears(	Mockito.any(),
														Mockito.eq("SGP4")))
				.thenReturn(Collections.emptyList());

		Mockito.when(assetSpaceService.getAssetSmears(	Mockito.any(),
														Mockito.eq("invalid")))
				.thenThrow(new BadRequestException(
						"Invalid propagator type"));
	}

	@Test
	public void testCreateSpace()
			throws AssetIdExistsException,
			AssetNameExistsException {
		final SpaceAssetModel assetModel = new SpaceAssetModel();
		assetModel.setModelXml(GOOD_XML);

		final ResponseEntity<String> response = assetSpaceController.createSpace(assetModel);

		Assert.assertEquals(HttpStatus.CREATED,
							response.getStatusCode());
		Assert.assertNull(response.getBody());

		final List<String> locationHeaders = response.getHeaders()
				.get("Location");

		Assert.assertNotNull(locationHeaders);
		Assert.assertEquals(1,
							locationHeaders.size());
		Assert.assertEquals("/space/" + GOOD_ASSET_ID,
							locationHeaders.get(0));
	}

	@Test(expected = AssetIdExistsException.class)
	public void testCreateSpaceConflict()
			throws AssetIdExistsException,
			AssetNameExistsException {
		final SpaceAssetModel assetModel = new SpaceAssetModel();
		assetModel.setModelXml(BAD_XML_CONFLICT);

		assetSpaceController.createSpace(assetModel);
	}

	@Test(expected = AssetNameExistsException.class)
	public void testCreateSpaceConflictName()
			throws AssetNameExistsException,
			AssetIdExistsException {
		final SpaceAssetModel assetModel = new SpaceAssetModel();
		assetModel.setModelXml(BAD_XML_CONFLICT_NAME);

		assetSpaceController.createSpace(assetModel);
	}

	@Test
	public void testCreateSpaceInvalidXml()
			throws AssetIdExistsException,
			AssetNameExistsException {
		final SpaceAssetModel assetModel = new SpaceAssetModel();
		assetModel.setModelXml(BAD_XML_INVALID);

		final ResponseEntity<String> response = assetSpaceController.createSpace(assetModel);

		Assert.assertEquals(HttpStatus.BAD_REQUEST,
							response.getStatusCode());
		Assert.assertNotNull(response.getBody());
	}

	@Test
	public void testCreateSpaceBadId()
			throws AssetIdExistsException,
			AssetNameExistsException {
		final SpaceAssetModel assetModel = new SpaceAssetModel();
		assetModel.setModelXml(BAD_XML_ID);

		final ResponseEntity<String> response = assetSpaceController.createSpace(assetModel);

		Assert.assertEquals(HttpStatus.BAD_REQUEST,
							response.getStatusCode());
		Assert.assertNotNull(response.getBody());
	}

	@Test
	public void testCreateSpaceFile()
			throws AssetIdExistsException,
			AssetNameExistsException {
		final MultipartFile file = new MockMultipartFile(
				"file",
				GOOD_XML.getBytes());

		final ResponseEntity<String> response = assetSpaceController.createSpaceFile(file);

		Assert.assertEquals(HttpStatus.CREATED,
							response.getStatusCode());
		Assert.assertNull(response.getBody());

		final List<String> locationHeaders = response.getHeaders()
				.get("Location");

		Assert.assertNotNull(locationHeaders);
		Assert.assertEquals(1,
							locationHeaders.size());
		Assert.assertEquals("/space/" + GOOD_ASSET_ID,
							locationHeaders.get(0));
	}

	@Test(expected = AssetIdExistsException.class)
	public void testCreateSpaceFileConflict()
			throws AssetIdExistsException,
			AssetNameExistsException {
		final MultipartFile file = new MockMultipartFile(
				"file",
				BAD_XML_CONFLICT.getBytes());

		assetSpaceController.createSpaceFile(file);
	}

	@Test(expected = AssetNameExistsException.class)
	public void testCreateSpaceFileConflictName()
			throws AssetNameExistsException,
			AssetIdExistsException {
		final MultipartFile file = new MockMultipartFile(
				"file",
				BAD_XML_CONFLICT_NAME.getBytes());

		assetSpaceController.createSpaceFile(file);
	}

	@Test
	public void testCreateSpaceFileInvalidXml()
			throws AssetIdExistsException,
			AssetNameExistsException {
		final MultipartFile file = new MockMultipartFile(
				"file",
				BAD_XML_INVALID.getBytes());

		final ResponseEntity<String> response = assetSpaceController.createSpaceFile(file);

		Assert.assertEquals(HttpStatus.BAD_REQUEST,
							response.getStatusCode());
		Assert.assertNotNull(response.getBody());
	}

	@Test
	public void testCreateSpaceFileBadId()
			throws AssetIdExistsException,
			AssetNameExistsException {
		final MultipartFile file = new MockMultipartFile(
				"file",
				BAD_XML_ID.getBytes());

		final ResponseEntity<String> response = assetSpaceController.createSpaceFile(file);

		Assert.assertEquals(HttpStatus.BAD_REQUEST,
							response.getStatusCode());
		Assert.assertNotNull(response.getBody());
	}

	@Test
	public void testCreateSpaceFileBadIO()
			throws IOException,
			AssetIdExistsException,
			AssetNameExistsException {
		final MultipartFile file = Mockito.mock(MockMultipartFile.class);

		Mockito.when(file.getInputStream())
				.thenThrow(new IOException(
						"Error reading XML"));

		final ResponseEntity<String> response = assetSpaceController.createSpaceFile(file);

		Assert.assertEquals(HttpStatus.BAD_REQUEST,
							response.getStatusCode());
		Assert.assertNotNull(response.getBody());
	}

	@Test
	public void testCreateSpaceExtraData()
			throws AssetIdDoesNotExistException,
			AssetExtraDataExistsException {
		final ExtraData extraData = new ExtraData();

		final ResponseEntity<String> response = assetSpaceController.createSpaceExtraData(	GOOD_ASSET_ID,
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
		Assert.assertEquals("/space/" + GOOD_ASSET_ID + "/extra/" + GOOD_EXTRA_DATA_NAME,
							locationHeaders.get(0));
	}

	@Test(expected = AssetIdDoesNotExistException.class)
	public void testCreateSpaceExtraDataIdDoesNotExist()
			throws AssetIdDoesNotExistException,
			AssetExtraDataExistsException {
		final ExtraData extraData = new ExtraData();

		assetSpaceController.createSpaceExtraData(	BAD_ASSET_ID_DOES_NOT_EXIST,
													GOOD_EXTRA_DATA_NAME,
													extraData);
	}

	@Test(expected = AssetExtraDataExistsException.class)
	public void testCreateSpaceExtraDataConflict()
			throws AssetExtraDataExistsException,
			AssetIdDoesNotExistException {
		final ExtraData extraData = new ExtraData();

		assetSpaceController.createSpaceExtraData(	GOOD_ASSET_ID,
													BAD_EXTRA_DATA_NAME_EXISTS,
													extraData);
	}

	@Test
	public void testCreateSpaceExtraDataBadName()
			throws AssetIdDoesNotExistException,
			AssetExtraDataExistsException {
		final ExtraData extraData = new ExtraData();

		final ResponseEntity<String> response = assetSpaceController.createSpaceExtraData(	GOOD_ASSET_ID,
																							BAD_EXTRA_DATA_NAME_INVALID,
																							extraData);

		Assert.assertEquals(HttpStatus.BAD_REQUEST,
							response.getStatusCode());
		Assert.assertNotNull(response.getBody());
	}

	@Test
	public void testCreateSpaceExtraDataFile()
			throws AssetIdDoesNotExistException,
			AssetExtraDataExistsException {
		final MultipartFile file = new MockMultipartFile(
				"file",
				"".getBytes());

		final ResponseEntity<String> response = assetSpaceController.createSpaceExtraDataFile(	GOOD_ASSET_ID,
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
		Assert.assertEquals("/space/" + GOOD_ASSET_ID + "/extra/" + GOOD_EXTRA_DATA_NAME,
							locationHeaders.get(0));
	}

	@Test(expected = AssetIdDoesNotExistException.class)
	public void testCreateSpaceExtraDataFileIdDoesNotExist()
			throws AssetIdDoesNotExistException,
			AssetExtraDataExistsException {
		final MultipartFile file = new MockMultipartFile(
				"file",
				"".getBytes());

		assetSpaceController.createSpaceExtraDataFile(	BAD_ASSET_ID_DOES_NOT_EXIST,
														GOOD_EXTRA_DATA_NAME,
														"",
														file);
	}

	@Test(expected = AssetExtraDataExistsException.class)
	public void testCreateSpaceExtraDataFileConflict()
			throws AssetExtraDataExistsException,
			AssetIdDoesNotExistException {
		final MultipartFile file = new MockMultipartFile(
				"file",
				"".getBytes());

		assetSpaceController.createSpaceExtraDataFile(	GOOD_ASSET_ID,
														BAD_EXTRA_DATA_NAME_EXISTS,
														"",
														file);
	}

	@Test
	public void testCreateSpaceExtraDataFileBadName()
			throws AssetIdDoesNotExistException,
			AssetExtraDataExistsException {
		final MultipartFile file = new MockMultipartFile(
				"file",
				"".getBytes());

		final ResponseEntity<String> response = assetSpaceController.createSpaceExtraDataFile(	GOOD_ASSET_ID,
																								BAD_EXTRA_DATA_NAME_INVALID,
																								"",
																								file);

		Assert.assertEquals(HttpStatus.BAD_REQUEST,
							response.getStatusCode());
		Assert.assertNotNull(response.getBody());
	}

	@Test
	public void testCreateSpaceExtraDataFileBadIO()
			throws IOException,
			AssetIdDoesNotExistException,
			AssetExtraDataExistsException {
		final MultipartFile file = Mockito.mock(MockMultipartFile.class);

		Mockito.when(file.getInputStream())
				.thenThrow(new IOException(
						"Error reading extra data"));

		final ResponseEntity<String> response = assetSpaceController.createSpaceExtraDataFile(	GOOD_ASSET_ID,
																								GOOD_EXTRA_DATA_NAME,
																								"",
																								file);

		Assert.assertEquals(HttpStatus.BAD_REQUEST,
							response.getStatusCode());
		Assert.assertNotNull(response.getBody());
	}

	@Test
	public void testGetSpaceAll() {
		final ResponseEntity<IdList> response = assetSpaceController.getSpaceAll();

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
	public void testGetSpaceId()
			throws AssetIdDoesNotExistException {
		final ResponseEntity<SpaceAssetModel> response = assetSpaceController.getSpaceId(GOOD_ASSET_ID);

		Assert.assertEquals(HttpStatus.OK,
							response.getStatusCode());
		Assert.assertNotNull(response.getBody());
	}

	@Test(expected = AssetIdDoesNotExistException.class)
	public void testGetSpaceIdDoesNotExist()
			throws AssetIdDoesNotExistException {
		assetSpaceController.getSpaceId(BAD_ASSET_ID_DOES_NOT_EXIST);
	}

	@Test
	public void testGetSpaceNames() {
		final ResponseEntity<NameList> response = assetSpaceController.getSpaceNames();

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
	public void getSpaceName()
			throws AssetNameDoesNotExistException {
		final ResponseEntity<String> response = assetSpaceController.getSpaceName(GOOD_ASSET_ID);

		Assert.assertEquals(HttpStatus.OK,
							response.getStatusCode());

		final String responseBody = response.getBody();

		Assert.assertNotNull(responseBody);
		Assert.assertEquals(GOOD_ASSET_ID,
							responseBody);
	}

	@Test(expected = AssetNameDoesNotExistException.class)
	public void getSpaceNameDoesNotExist()
			throws AssetNameDoesNotExistException {
		assetSpaceController.getSpaceName(BAD_ASSET_ID_DOES_NOT_EXIST);
	}

	@Test
	public void testGetSpaceExtra()
			throws AssetIdDoesNotExistException {
		final ResponseEntity<NameList> response = assetSpaceController.getSpaceExtra(GOOD_ASSET_ID);

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
	public void testGetSpaceExtraIdDoesNotExist()
			throws AssetIdDoesNotExistException {
		assetSpaceController.getSpaceExtra(BAD_ASSET_ID_DOES_NOT_EXIST);
	}

	@Test
	public void testGetSpaceExtraName()
			throws AssetIdDoesNotExistException,
			AssetExtraDataDoesNotExistException {
		final ResponseEntity<ExtraData> response = assetSpaceController.getSpaceExtraName(	GOOD_ASSET_ID,
																							GOOD_EXTRA_DATA_NAME);

		Assert.assertEquals(HttpStatus.OK,
							response.getStatusCode());
		Assert.assertNotNull(response.getBody());
	}

	@Test(expected = AssetIdDoesNotExistException.class)
	public void testGetSpaceExtraNameIdDoesNotExist()
			throws AssetIdDoesNotExistException,
			AssetExtraDataDoesNotExistException {
		assetSpaceController.getSpaceExtraName(	BAD_ASSET_ID_DOES_NOT_EXIST,
												GOOD_EXTRA_DATA_NAME);
	}

	@Test(expected = AssetExtraDataDoesNotExistException.class)
	public void testGetSpaceExtraNameDoesNotExist()
			throws AssetExtraDataDoesNotExistException,
			AssetIdDoesNotExistException {
		assetSpaceController.getSpaceExtraName(	GOOD_ASSET_ID,
												BAD_EXTRA_DATA_NAME_DOES_NOT_EXIST);
	}

	@Test
	public void testGetAirborneFieldOfRegard()
			throws AssetHasNoFieldOfRegardException,
			AssetIdDoesNotExistException,
			BadRequestException {
		final ResponseEntity<List<FieldOfRegard>> response = assetSpaceController.getSpaceFieldsOfRegard(	GOOD_ASSET_ID,
																											GOOD_DATE_TIME_STRING,
																											"");

		Assert.assertNotNull(response);
		Assert.assertNotNull(response.getBody());

		final List<FieldOfRegard> fieldsOfRegard = response.getBody();

		fieldsOfRegard.forEach(fieldOfRegard -> Assert.assertNotNull(fieldOfRegard.getFieldOfRegardAngleWkt()));
	}

	@Test(expected = AssetIdDoesNotExistException.class)
	public void testGetAirborneFieldOfRegardIdDoesNotExist()
			throws AssetHasNoFieldOfRegardException,
			AssetIdDoesNotExistException,
			BadRequestException {
		assetSpaceController.getSpaceFieldsOfRegard(BAD_ASSET_ID_DOES_NOT_EXIST,
													GOOD_DATE_TIME_STRING,
													"");
	}

	@Test(expected = BadRequestException.class)
	public void testGetAirborneFieldOfRegardInvalidDateTime()
			throws AssetHasNoFieldOfRegardException,
			AssetIdDoesNotExistException,
			BadRequestException {
		assetSpaceController.getSpaceFieldsOfRegard(GOOD_ASSET_ID,
													BAD_DATE_TIME_STRING,
													"");
	}

	@Test(expected = AssetHasNoFieldOfRegardException.class)
	public void testGetAirborneFieldOfRegardNoFieldOfRegard()
			throws AssetHasNoFieldOfRegardException,
			AssetIdDoesNotExistException,
			BadRequestException {
		assetSpaceController.getSpaceFieldsOfRegard(BAD_ASSET_ID_NO_FIELD_OF_REGARD,
													GOOD_DATE_TIME_STRING,
													"");
	}

	@Test
	public void testUpdateSpace()
			throws AssetIdDoesNotExistException,
			AssetIdDoesNotMatchException {
		final SpaceAssetModel assetModel = new SpaceAssetModel();
		assetModel.setModelXml(GOOD_XML);

		final ResponseEntity<String> response = assetSpaceController.updateSpace(	GOOD_ASSET_ID,
																					assetModel);

		Assert.assertEquals(HttpStatus.OK,
							response.getStatusCode());
		Assert.assertNull(response.getBody());
	}

	@Test(expected = AssetIdDoesNotExistException.class)
	public void testUpdateSpaceDoesNotExist()
			throws AssetIdDoesNotExistException,
			AssetIdDoesNotMatchException {
		final SpaceAssetModel assetModel = new SpaceAssetModel();
		assetModel.setModelXml(GOOD_XML);

		assetSpaceController.updateSpace(	BAD_ASSET_ID_DOES_NOT_EXIST,
											assetModel);
	}

	@Test(expected = AssetIdDoesNotMatchException.class)
	public void testUpdateSpaceDoesNotMatch()
			throws AssetIdDoesNotMatchException,
			AssetIdDoesNotExistException {
		final SpaceAssetModel assetModel = new SpaceAssetModel();
		assetModel.setModelXml(BAD_XML_ID);

		assetSpaceController.updateSpace(	GOOD_ASSET_ID,
											assetModel);
	}

	@Test
	public void testUpdateSpaceInvalidXml()
			throws AssetIdDoesNotExistException,
			AssetIdDoesNotMatchException {
		final SpaceAssetModel assetModel = new SpaceAssetModel();
		assetModel.setModelXml(BAD_XML_INVALID);

		final ResponseEntity<String> response = assetSpaceController.updateSpace(	GOOD_ASSET_ID,
																					assetModel);

		Assert.assertEquals(HttpStatus.BAD_REQUEST,
							response.getStatusCode());
		Assert.assertNotNull(response.getBody());
	}

	@Test
	public void testUpdateSpaceFile()
			throws AssetIdDoesNotExistException,
			AssetIdDoesNotMatchException {
		final MultipartFile file = new MockMultipartFile(
				"file",
				GOOD_XML.getBytes());

		final ResponseEntity<String> response = assetSpaceController.updateSpaceFile(	GOOD_ASSET_ID,
																						file);

		Assert.assertEquals(HttpStatus.OK,
							response.getStatusCode());
		Assert.assertNull(response.getBody());
	}

	@Test(expected = AssetIdDoesNotExistException.class)
	public void testUpdateSpaceFileDoesNotExist()
			throws AssetIdDoesNotExistException,
			AssetIdDoesNotMatchException {
		final MultipartFile file = new MockMultipartFile(
				"file",
				GOOD_XML.getBytes());

		assetSpaceController.updateSpaceFile(	BAD_ASSET_ID_DOES_NOT_EXIST,
												file);
	}

	@Test(expected = AssetIdDoesNotMatchException.class)
	public void testUpdateSpaceFileDoesNotMatch()
			throws AssetIdDoesNotMatchException,
			AssetIdDoesNotExistException {
		final MultipartFile file = new MockMultipartFile(
				"file",
				BAD_XML_ID.getBytes());

		assetSpaceController.updateSpaceFile(	GOOD_ASSET_ID,
												file);
	}

	@Test
	public void testUpdateSpaceFileInvalidXml()
			throws AssetIdDoesNotExistException,
			AssetIdDoesNotMatchException {
		final MultipartFile file = new MockMultipartFile(
				"file",
				BAD_XML_INVALID.getBytes());

		final ResponseEntity<String> response = assetSpaceController.updateSpaceFile(	GOOD_ASSET_ID,
																						file);

		Assert.assertEquals(HttpStatus.BAD_REQUEST,
							response.getStatusCode());
		Assert.assertNotNull(response.getBody());
	}

	@Test
	public void testUpdateSpaceFileBadIO()
			throws AssetIdDoesNotExistException,
			AssetIdDoesNotMatchException,
			IOException {
		final MultipartFile file = Mockito.mock(MockMultipartFile.class);

		Mockito.when(file.getInputStream())
				.thenThrow(new IOException(
						"Error reading XML"));

		final ResponseEntity<String> response = assetSpaceController.updateSpaceFile(	GOOD_ASSET_ID,
																						file);

		Assert.assertEquals(HttpStatus.BAD_REQUEST,
							response.getStatusCode());
		Assert.assertNotNull(response.getBody());
	}

	@Test
	public void testUpdateSpaceExtraData()
			throws AssetIdDoesNotExistException,
			AssetExtraDataDoesNotExistException {
		final ExtraData extraData = new ExtraData();

		final ResponseEntity<String> response = assetSpaceController.updateSpaceExtraData(	GOOD_ASSET_ID,
																							GOOD_EXTRA_DATA_NAME,
																							extraData);

		Assert.assertEquals(HttpStatus.OK,
							response.getStatusCode());
		Assert.assertNull(response.getBody());
	}

	@Test(expected = AssetIdDoesNotExistException.class)
	public void testUpdateSpaceExtraDataIdDoesNotExist()
			throws AssetIdDoesNotExistException,
			AssetExtraDataDoesNotExistException {
		final ExtraData extraData = new ExtraData();

		assetSpaceController.updateSpaceExtraData(	BAD_ASSET_ID_DOES_NOT_EXIST,
													GOOD_EXTRA_DATA_NAME,
													extraData);
	}

	@Test(expected = AssetExtraDataDoesNotExistException.class)
	public void testUpdateSpaceExtraDataDoesNotExist()
			throws AssetIdDoesNotExistException,
			AssetExtraDataDoesNotExistException {
		final ExtraData extraData = new ExtraData();

		assetSpaceController.updateSpaceExtraData(	GOOD_ASSET_ID,
													BAD_EXTRA_DATA_NAME_DOES_NOT_EXIST,
													extraData);
	}

	@Test
	public void testUpdateSpaceExtraDataFile()
			throws AssetIdDoesNotExistException,
			AssetExtraDataDoesNotExistException {
		final MultipartFile file = new MockMultipartFile(
				"file",
				"".getBytes());

		final ResponseEntity<String> response = assetSpaceController.updateSpaceExtraDataFile(	GOOD_ASSET_ID,
																								GOOD_EXTRA_DATA_NAME,
																								"",
																								file);

		Assert.assertEquals(HttpStatus.OK,
							response.getStatusCode());
		Assert.assertNull(response.getBody());
	}

	@Test(expected = AssetIdDoesNotExistException.class)
	public void testUpdateSpaceExtraDataFileIdDoesNotExist()
			throws AssetIdDoesNotExistException,
			AssetExtraDataDoesNotExistException {
		final MultipartFile file = new MockMultipartFile(
				"file",
				"".getBytes());

		assetSpaceController.updateSpaceExtraDataFile(	BAD_ASSET_ID_DOES_NOT_EXIST,
														GOOD_EXTRA_DATA_NAME,
														"",
														file);
	}

	@Test(expected = AssetExtraDataDoesNotExistException.class)
	public void testUpdateSpaceExtraDataFileDoesNotExist()
			throws AssetIdDoesNotExistException,
			AssetExtraDataDoesNotExistException {
		final MultipartFile file = new MockMultipartFile(
				"file",
				"".getBytes());

		assetSpaceController.updateSpaceExtraDataFile(	GOOD_ASSET_ID,
														BAD_EXTRA_DATA_NAME_DOES_NOT_EXIST,
														"",
														file);
	}

	@Test
	public void testUpdateSpaceExtraDataFileBadIO()
			throws AssetIdDoesNotExistException,
			AssetExtraDataDoesNotExistException,
			IOException {
		final MultipartFile file = Mockito.mock(MockMultipartFile.class);

		Mockito.when(file.getInputStream())
				.thenThrow(new IOException(
						"Error reading extra data"));

		final ResponseEntity<String> response = assetSpaceController.updateSpaceExtraDataFile(	GOOD_ASSET_ID,
																								GOOD_EXTRA_DATA_NAME,
																								"",
																								file);

		Assert.assertEquals(HttpStatus.BAD_REQUEST,
							response.getStatusCode());
		Assert.assertNotNull(response.getBody());
	}

	@Test
	public void testDeleteSpace()
			throws AssetIdDoesNotExistException {
		final ResponseEntity<String> response = assetSpaceController.deleteSpace(GOOD_ASSET_ID);

		Assert.assertEquals(HttpStatus.OK,
							response.getStatusCode());
		Assert.assertNull(response.getBody());
	}

	@Test(expected = AssetIdDoesNotExistException.class)
	public void testDeleteSpaceDoesNotExist()
			throws AssetIdDoesNotExistException {
		assetSpaceController.deleteSpace(BAD_ASSET_ID_DOES_NOT_EXIST);
	}

	@Test
	public void testDeleteSpaceExtraData()
			throws AssetIdDoesNotExistException,
			AssetExtraDataDoesNotExistException {
		final ResponseEntity<String> response = assetSpaceController.deleteSpaceExtraData(	GOOD_ASSET_ID,
																							GOOD_EXTRA_DATA_NAME);

		Assert.assertEquals(HttpStatus.OK,
							response.getStatusCode());
		Assert.assertNull(response.getBody());
	}

	@Test(expected = AssetIdDoesNotExistException.class)
	public void testDeleteSpaceExtraDataIdDoesNotExist()
			throws AssetIdDoesNotExistException,
			AssetExtraDataDoesNotExistException {
		assetSpaceController.deleteSpaceExtraData(	BAD_ASSET_ID_DOES_NOT_EXIST,
													GOOD_EXTRA_DATA_NAME);
	}

	@Test(expected = AssetExtraDataDoesNotExistException.class)
	public void testDeleteSpaceExtraDataDoesNotExist()
			throws AssetIdDoesNotExistException,
			AssetExtraDataDoesNotExistException {
		assetSpaceController.deleteSpaceExtraData(	GOOD_ASSET_ID,
													BAD_EXTRA_DATA_NAME_DOES_NOT_EXIST);
	}

	@Test
	public void testGetSpaceSmearsPropagatorTypeNull()
			throws AssetIdDoesNotExistException {
		final AssetSmearWithBeamsRequest assetSmearWithBeamsRequest = new AssetSmearWithBeamsRequest();

		final ResponseEntity<List<AssetSmear>> response = assetSpaceController
				.getSpaceSmears(assetSmearWithBeamsRequest,
								null);

		Assert.assertNotNull(response);
		Assert.assertEquals(HttpStatus.OK,
							response.getStatusCode());
		Assert.assertNotNull(response.getBody());
		Assert.assertTrue(response.getBody()
				.isEmpty());
	}

	@Test
	public void testGetSpaceSmearsPropagatorTypeEmpty()
			throws AssetIdDoesNotExistException {
		final AssetSmearWithBeamsRequest assetSmearWithBeamsRequest = new AssetSmearWithBeamsRequest();

		final ResponseEntity<List<AssetSmear>> response = assetSpaceController
				.getSpaceSmears(assetSmearWithBeamsRequest,
								"");

		Assert.assertNotNull(response);
		Assert.assertEquals(HttpStatus.OK,
							response.getStatusCode());
		Assert.assertNotNull(response.getBody());
		Assert.assertTrue(response.getBody()
				.isEmpty());
	}

	@Test
	public void testGetSpaceSmearsPropagatorTypeJ2()
			throws AssetIdDoesNotExistException {
		final AssetSmearWithBeamsRequest assetSmearWithBeamsRequest = new AssetSmearWithBeamsRequest();

		final ResponseEntity<List<AssetSmear>> response = assetSpaceController
				.getSpaceSmears(assetSmearWithBeamsRequest,
								"J2");

		Assert.assertNotNull(response);
		Assert.assertEquals(HttpStatus.OK,
							response.getStatusCode());
		Assert.assertNotNull(response.getBody());
		Assert.assertTrue(response.getBody()
				.isEmpty());
	}

	@Test
	public void testGetSpaceSmearsPropagatorTypeSGP4()
			throws AssetIdDoesNotExistException {
		final AssetSmearWithBeamsRequest assetSmearWithBeamsRequest = new AssetSmearWithBeamsRequest();

		final ResponseEntity<List<AssetSmear>> response = assetSpaceController
				.getSpaceSmears(assetSmearWithBeamsRequest,
								"SGP4");

		Assert.assertNotNull(response);
		Assert.assertEquals(HttpStatus.OK,
							response.getStatusCode());
		Assert.assertNotNull(response.getBody());
		Assert.assertTrue(response.getBody()
				.isEmpty());
	}

	@Test(expected = BadRequestException.class)
	public void testGetSpaceSmearsPropagatorTypeInvalid()
			throws AssetIdDoesNotExistException {
		final AssetSmearWithBeamsRequest assetSmearWithBeamsRequest = new AssetSmearWithBeamsRequest();

		assetSpaceController.getSpaceSmears(assetSmearWithBeamsRequest,
											"invalid");
	}
}
