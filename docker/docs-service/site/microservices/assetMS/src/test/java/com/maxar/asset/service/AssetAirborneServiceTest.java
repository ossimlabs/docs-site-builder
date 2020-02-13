package com.maxar.asset.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.maxar.asset.common.exception.AssetExtraDataDoesNotExistException;
import com.maxar.asset.common.exception.AssetIdDoesNotExistException;
import com.maxar.asset.common.exception.AssetNameDoesNotExistException;
import com.maxar.asset.common.exception.MissionIdDoesNotExistException;
import com.maxar.asset.common.exception.NoMissionsFoundException;
import com.maxar.asset.common.service.ApiService;
import com.maxar.asset.exception.AssetExtraDataExistsException;
import com.maxar.asset.exception.AssetHasNoFieldOfRegardException;
import com.maxar.asset.exception.AssetIdDoesNotMatchException;
import com.maxar.asset.exception.AssetIdExistsException;
import com.maxar.asset.exception.AssetNameExistsException;
import com.maxar.asset.model.AssetSmear;
import com.maxar.asset.model.AssetSmearWithBeamsRequest;
import com.maxar.asset.model.ExtraData;
import com.maxar.asset.model.FieldOfRegard;
import com.maxar.common.exception.BadRequestException;
import com.maxar.mission.model.MissionModel;
import com.radiantblue.analytics.isr.core.model.asset.Asset;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@TestPropertySource(locations = "classpath:testassetms.properties")
@AutoConfigureTestDatabase
public class AssetAirborneServiceTest
{
	@Autowired
	private AssetAirborneService assetAirborneService;

	@Autowired
	private ObjectMapper objectMapper;

	@MockBean
	private ApiService apiService;

	private String assetXml;

	private static final String AIRCRAFT_ID = "1";

	private static final String AIRCRAFT_NAME = "AIR_EO";

	private static final DateTime AT_TIME = ISODateTimeFormat.dateTimeParser()
			.parseDateTime("2015-01-01T12:00:00Z");

	@Before
	public void setUp() {
		try (final InputStream inputStream = this.getClass()
				.getResourceAsStream("/xml/AIR_EO.xml")) {
			assetXml = new BufferedReader(
					new InputStreamReader(
							inputStream)).lines()
									.collect(Collectors.joining(System.lineSeparator()));

			final String aircraftId = assetAirborneService.createAsset(assetXml);

			Assert.assertEquals(AIRCRAFT_ID,
								aircraftId);

			Mockito.doCallRealMethod()
					.when(apiService)
					.updateAssetMission(ArgumentMatchers.any(Asset.class),
										ArgumentMatchers.any(DateTime.class),
										ArgumentMatchers.isNull());

			Mockito.doCallRealMethod()
					.when(apiService)
					.updateAssetMission(ArgumentMatchers.any(Asset.class),
										ArgumentMatchers.any(DateTime.class),
										ArgumentMatchers.any(DateTime.class),
										ArgumentMatchers.isNull());
		}
		catch (final AssetIdExistsException | AssetNameExistsException | BeansException | IOException
				| NoMissionsFoundException | MissionIdDoesNotExistException e) {
			Assert.fail(e.getMessage());
		}
	}

	@After
	public void tearDown() {
		try {
			assetAirborneService.deleteAsset(AIRCRAFT_ID);
		}
		catch (final AssetIdDoesNotExistException e) {
			Assert.fail(e.getMessage());
		}
	}

	@Test
	public void testCreateAsset() {
		Assert.assertNotNull(assetXml);
	}

	@Test
	public void testCreateAssetBadXml() {
		try {
			assetAirborneService.createAsset("bad xml");

			Assert.fail("Expected to fail parsing XML");
		}
		catch (final AssetIdExistsException | AssetNameExistsException e) {
			Assert.fail("Expected to fail parsing XML");
		}
		catch (final BeansException e) {
			Assert.assertNotNull(e.getMessage());
			Assert.assertTrue(e.getMessage()
					.contains("SAXParseException"));
		}
	}

	@Test
	public void testCreateAssetTwice() {
		try {
			final String aircraftId = assetAirborneService.createAsset(assetXml);

			Assert.fail("Asset with ID " + aircraftId + " should not have been added twice.");
		}
		catch (final AssetIdExistsException e) {
			Assert.assertTrue(e.getMessage()
					.contains(AIRCRAFT_ID));
		}
		catch (final AssetNameExistsException | BeansException e) {
			Assert.fail(e.getMessage());
		}
	}

	@Test
	public void testCreateAssetTwiceDifferentId() {
		try {
			final String assetXml = this.assetXml.replace(	"name=\"id\" value=\"" + AIRCRAFT_ID + "\"",
															"name=\"id\" value=\"1111\"");

			assetAirborneService.createAsset(assetXml);

			Assert.fail("Asset with name " + AIRCRAFT_NAME + " should not have been added twice.");
		}
		catch (final AssetNameExistsException e) {
			Assert.assertTrue(e.getMessage()
					.contains(AIRCRAFT_NAME));
		}
		catch (final AssetIdExistsException | BeansException e) {
			Assert.fail(e.getMessage());
		}
	}

	@Test
	public void testCreateAssetExtraData() {
		final String extraDataName = AIRCRAFT_NAME + "_extradata";
		final ExtraData extraData = new ExtraData();
		extraData.setType("text");
		extraData.setData("The extra data for the asset");

		try {
			assetAirborneService.createAssetExtraData(	AIRCRAFT_ID,
														extraDataName,
														extraData);
		}
		catch (final AssetExtraDataExistsException | AssetIdDoesNotExistException e) {
			Assert.fail(e.getMessage());
		}
	}

	@Test
	public void testCreateAssetExtraDataTwice() {
		final String extraDataName = AIRCRAFT_NAME + "_extradata";
		final ExtraData extraData = new ExtraData();
		extraData.setType("text");
		extraData.setData("The extra data for the asset");

		try {
			assetAirborneService.createAssetExtraData(	AIRCRAFT_ID,
														extraDataName,
														extraData);
		}
		catch (final AssetExtraDataExistsException | AssetIdDoesNotExistException e) {
			Assert.fail(e.getMessage());
		}

		try {
			assetAirborneService.createAssetExtraData(	AIRCRAFT_ID,
														extraDataName,
														extraData);

			Assert.fail("Extra data with name " + extraDataName + " should not have been added twice.");
		}
		catch (final AssetExtraDataExistsException e) {
			Assert.assertTrue(e.getMessage()
					.contains(AIRCRAFT_ID));
			Assert.assertTrue(e.getMessage()
					.contains(extraDataName));
		}
		catch (final AssetIdDoesNotExistException e) {
			Assert.fail(e.getMessage());
		}
	}

	@Test
	public void testCreateAssetExtraDataDoesNotExist() {
		final String extraDataName = AIRCRAFT_NAME + "_extradata";
		final String aircraftId = "1111";
		final ExtraData extraData = new ExtraData();
		extraData.setType("text");
		extraData.setData("The extra data for the asset");

		try {
			assetAirborneService.createAssetExtraData(	aircraftId,
														extraDataName,
														extraData);

			Assert.fail("There should be no asset with ID " + aircraftId);
		}
		catch (final AssetExtraDataExistsException e) {
			Assert.fail(e.getMessage());
		}
		catch (final AssetIdDoesNotExistException e) {
			Assert.assertTrue(e.getMessage()
					.contains(aircraftId));
		}
	}

	@Test
	public void testGetAllAssetIds() {
		final List<String> assetIds = assetAirborneService.getAllAssetIds();

		Assert.assertEquals(1,
							assetIds.size());
		Assert.assertEquals(AIRCRAFT_ID,
							assetIds.get(0));
	}

	@Test
	public void testGetAssetById() {
		try {
			final String assetXml = assetAirborneService.getAssetById(AIRCRAFT_ID);

			Assert.assertEquals(this.assetXml,
								assetXml);
		}
		catch (final AssetIdDoesNotExistException e) {
			Assert.fail(e.getMessage());
		}
	}

	@Test
	public void testGetAssetByIdDoesNotExist() {
		final String aircraftId = "1111";

		try {
			assetAirborneService.getAssetById(aircraftId);

			Assert.fail("There should be no asset with ID " + aircraftId);
		}
		catch (final AssetIdDoesNotExistException e) {
			Assert.assertTrue(e.getMessage()
					.contains(aircraftId));
		}
	}

	@Test
	public void testGetAllAssetNames() {
		final List<String> assetNames = assetAirborneService.getAllAssetNames();

		Assert.assertEquals(1,
							assetNames.size());
		Assert.assertEquals(AIRCRAFT_NAME,
							assetNames.get(0));
	}

	@Test
	public void testGetAssetIdByName() {
		try {
			final String aircraftId = assetAirborneService.getAssetIdByName(AIRCRAFT_NAME);

			Assert.assertEquals(AIRCRAFT_ID,
								aircraftId);
		}
		catch (final AssetNameDoesNotExistException e) {
			Assert.fail(e.getMessage());
		}
	}

	@Test
	public void testGetAssetIdByNameDoesNotExist() {
		final String aircraftId = "1111";

		try {
			assetAirborneService.getAssetIdByName(aircraftId);

			Assert.fail("There should be no asset with name " + aircraftId);
		}
		catch (final AssetNameDoesNotExistException e) {
			Assert.assertTrue(e.getMessage()
					.contains(aircraftId));
		}
	}

	@Test
	public void testGetAllAssetExtraDataNames() {
		final String extraDataName = AIRCRAFT_NAME + "_extradata";
		final ExtraData extraData = new ExtraData();
		extraData.setType("text");
		extraData.setData("The extra data for the asset");

		try {
			assetAirborneService.createAssetExtraData(	AIRCRAFT_ID,
														extraDataName,
														extraData);

			final List<String> extraDataNames = assetAirborneService.getAllAssetExtraDataNames(AIRCRAFT_ID);

			Assert.assertEquals(1,
								extraDataNames.size());
			Assert.assertEquals(extraDataName,
								extraDataNames.get(0));
		}
		catch (final AssetExtraDataExistsException | AssetIdDoesNotExistException e) {
			Assert.fail(e.getMessage());
		}
	}

	@Test
	public void testGetAllAssetExtraDataNamesEmpty() {
		try {
			final List<String> extraDataNames = assetAirborneService.getAllAssetExtraDataNames(AIRCRAFT_ID);

			Assert.assertTrue(extraDataNames.isEmpty());
		}
		catch (final AssetIdDoesNotExistException e) {
			Assert.fail(e.getMessage());
		}
	}

	@Test
	public void testGetAllAssetExtraDataNamesDoesNotExist() {
		final String aircraftId = "1111";

		try {
			assetAirborneService.getAllAssetExtraDataNames(aircraftId);

			Assert.fail("There should be no asset with ID " + aircraftId);
		}
		catch (final AssetIdDoesNotExistException e) {
			Assert.assertTrue(e.getMessage()
					.contains(aircraftId));
		}
	}

	@Test
	public void testGetAssetExtraDataByName() {
		final String extraDataName = AIRCRAFT_NAME + "_extradata";
		final ExtraData extraData = new ExtraData();
		extraData.setType("text");
		extraData.setData("The extra data for the asset");

		try {
			assetAirborneService.createAssetExtraData(	AIRCRAFT_ID,
														extraDataName,
														extraData);

			final ExtraData extraData2 = assetAirborneService.getAssetExtraDataByName(	AIRCRAFT_ID,
																						extraDataName);

			Assert.assertNotNull(extraData2);
			Assert.assertEquals("text",
								extraData2.getType());
			Assert.assertEquals("The extra data for the asset",
								extraData2.getData());
		}
		catch (final AssetExtraDataExistsException | AssetIdDoesNotExistException
				| AssetExtraDataDoesNotExistException e) {
			Assert.fail(e.getMessage());
		}
	}

	@Test
	public void testGetAssetExtraDataByNameAssetIdDoesNotExist() {
		final String aircraftId = "1111";
		final String extraDataName = AIRCRAFT_NAME + "_extradata";

		try {
			assetAirborneService.getAssetExtraDataByName(	aircraftId,
															extraDataName);

			Assert.fail("There should be no asset with ID " + aircraftId);
		}
		catch (final AssetExtraDataDoesNotExistException e) {
			Assert.fail(e.getMessage());
		}
		catch (final AssetIdDoesNotExistException e) {
			Assert.assertTrue(e.getMessage()
					.contains(aircraftId));
		}
	}

	@Test
	public void testGetAssetExtraDataByNameDoesNotExist() {
		final String extraDataName = AIRCRAFT_NAME + "_extradata";
		final String doesNotExistExtraDataName = "doesnotexist";
		final ExtraData extraData = new ExtraData();
		extraData.setType("text");
		extraData.setData("The extra data for the asset");

		try {
			assetAirborneService.createAssetExtraData(	AIRCRAFT_ID,
														extraDataName,
														extraData);

			assetAirborneService.getAssetExtraDataByName(	AIRCRAFT_ID,
															doesNotExistExtraDataName);

			Assert.fail("There should be no extra data with name " + doesNotExistExtraDataName);
		}
		catch (final AssetExtraDataExistsException | AssetIdDoesNotExistException e) {
			Assert.fail(e.getMessage());
		}
		catch (final AssetExtraDataDoesNotExistException e) {
			Assert.assertTrue(e.getMessage()
					.contains(AIRCRAFT_ID));
			Assert.assertTrue(e.getMessage()
					.contains(doesNotExistExtraDataName));
		}
	}

	@Test
	public void testgetAssetFieldsOfRegardButterflyFOR()
			throws AssetIdDoesNotExistException,
			IOException {
		final MissionModel missionModel;
		try (final InputStream missionModelStream = this.getClass()
				.getResourceAsStream("/json/mission_example.json")) {
			missionModel = objectMapper.readValue(	missionModelStream,
													MissionModel.class);
		}

		Mockito.when(apiService.getAssetMissionsAtTime(	ArgumentMatchers.eq(AIRCRAFT_ID),
														ArgumentMatchers.any(DateTime.class)))
				.thenReturn(Collections.singletonList(missionModel));

		Mockito.when(apiService.getAssetMissionsAtTime(	ArgumentMatchers.eq(AIRCRAFT_ID),
														ArgumentMatchers.any(DateTime.class),
														ArgumentMatchers.any(DateTime.class)))
				.thenReturn(Collections.singletonList(missionModel));

		final List<FieldOfRegard> fieldsOfRegard = assetAirborneService.getAssetFieldsOfRegard(	AIRCRAFT_ID,
																								AT_TIME,
																								"");

		fieldsOfRegard.forEach(fieldOfRegard -> Assert.assertNotNull(fieldOfRegard.getFieldOfRegardAngleWkt()));
	}

	@Test
	public void testgetAssetFieldsOfRegardAxebladeFOR()
			throws AssetIdDoesNotExistException,
			IOException,
			AssetIdExistsException,
			AssetNameExistsException {
		final MissionModel missionModel;
		try (final InputStream missionModelStream = this.getClass()
				.getResourceAsStream("/json/mission_example.json")) {
			missionModel = objectMapper.readValue(	missionModelStream,
													MissionModel.class);
		}

		Mockito.when(apiService.getAssetMissionsAtTime(	ArgumentMatchers.anyString(),
														ArgumentMatchers.any(DateTime.class)))
				.thenReturn(Collections.singletonList(missionModel));

		Mockito.when(apiService.getAssetMissionsAtTime(	ArgumentMatchers.anyString(),
														ArgumentMatchers.any(DateTime.class),
														ArgumentMatchers.any(DateTime.class)))
				.thenReturn(Collections.singletonList(missionModel));

		String deleteAircraftId = "";

		try (final InputStream inputStream = this.getClass()
				.getResourceAsStream("/xml/AIR_EO_axebladeFOR.xml")) {
			assetXml = new BufferedReader(
					new InputStreamReader(
							inputStream)).lines()
									.collect(Collectors.joining(System.lineSeparator()));

			final String aircraftId = assetAirborneService.createAsset(assetXml);
			deleteAircraftId = aircraftId;

			final List<FieldOfRegard> fieldsOfRegard = assetAirborneService.getAssetFieldsOfRegard(	aircraftId,
																									AT_TIME,
																									"");

			fieldsOfRegard.forEach(fieldOfRegard -> Assert.assertNotNull(fieldOfRegard.getFieldOfRegardAngleWkt()));
		}
		finally {
			if (!deleteAircraftId.isEmpty()) {
				assetAirborneService.deleteAsset(deleteAircraftId);
			}
		}
	}

	@Test
	public void testgetAssetFieldsOfRegardGrazeFOR()
			throws AssetIdDoesNotExistException,
			IOException,
			AssetIdExistsException,
			AssetNameExistsException {
		final MissionModel missionModel;
		try (final InputStream missionModelStream = this.getClass()
				.getResourceAsStream("/json/mission_example.json")) {
			missionModel = objectMapper.readValue(	missionModelStream,
													MissionModel.class);
		}

		Mockito.when(apiService.getAssetMissionsAtTime(	ArgumentMatchers.anyString(),
														ArgumentMatchers.any(DateTime.class)))
				.thenReturn(Collections.singletonList(missionModel));

		Mockito.when(apiService.getAssetMissionsAtTime(	ArgumentMatchers.anyString(),
														ArgumentMatchers.any(DateTime.class),
														ArgumentMatchers.any(DateTime.class)))
				.thenReturn(Collections.singletonList(missionModel));

		String deleteAircraftId = "";

		try (final InputStream inputStream = this.getClass()
				.getResourceAsStream("/xml/AIR_EO_grazeFOR.xml")) {
			assetXml = new BufferedReader(
					new InputStreamReader(
							inputStream)).lines()
									.collect(Collectors.joining(System.lineSeparator()));

			final String aircraftId = assetAirborneService.createAsset(assetXml);
			deleteAircraftId = aircraftId;

			final List<FieldOfRegard> fieldsOfRegard = assetAirborneService.getAssetFieldsOfRegard(	aircraftId,
																									AT_TIME,
																									"");

			fieldsOfRegard.forEach(fieldOfRegard -> Assert.assertNotNull(fieldOfRegard.getFieldOfRegardAngleWkt()));
		}
		finally {
			if (!deleteAircraftId.isEmpty()) {
				assetAirborneService.deleteAsset(deleteAircraftId);
			}
		}
	}

	@Test
	public void testgetAssetFieldsOfRegardNadirFOR()
			throws AssetIdDoesNotExistException,
			IOException,
			AssetIdExistsException,
			AssetNameExistsException {
		final MissionModel missionModel;
		try (final InputStream missionModelStream = this.getClass()
				.getResourceAsStream("/json/mission_example.json")) {
			missionModel = objectMapper.readValue(	missionModelStream,
													MissionModel.class);
		}

		Mockito.when(apiService.getAssetMissionsAtTime(	ArgumentMatchers.anyString(),
														ArgumentMatchers.any(DateTime.class)))
				.thenReturn(Collections.singletonList(missionModel));

		Mockito.when(apiService.getAssetMissionsAtTime(	ArgumentMatchers.anyString(),
														ArgumentMatchers.any(DateTime.class),
														ArgumentMatchers.any(DateTime.class)))
				.thenReturn(Collections.singletonList(missionModel));

		String deleteAircraftId = "";

		try (final InputStream inputStream = this.getClass()
				.getResourceAsStream("/xml/AIR_EO_nadirFOR.xml")) {
			assetXml = new BufferedReader(
					new InputStreamReader(
							inputStream)).lines()
									.collect(Collectors.joining(System.lineSeparator()));

			final String aircraftId = assetAirborneService.createAsset(assetXml);
			deleteAircraftId = aircraftId;

			final List<FieldOfRegard> fieldsOfRegard = assetAirborneService.getAssetFieldsOfRegard(	aircraftId,
																									AT_TIME,
																									"");

			fieldsOfRegard.forEach(fieldOfRegard -> Assert.assertNotNull(fieldOfRegard.getFieldOfRegardAngleWkt()));
		}
		finally {
			if (!deleteAircraftId.isEmpty()) {
				assetAirborneService.deleteAsset(deleteAircraftId);
			}
		}
	}

	@Test
	public void testgetAssetFieldsOfRegardDoesNotExist() {
		final String airborneId = "1111";

		try {
			assetAirborneService.getAssetFieldsOfRegard(airborneId,
														AT_TIME,
														"");

			Assert.fail("There should be no asset with ID " + airborneId);
		}
		catch (final AssetIdDoesNotExistException e) {
			Assert.assertTrue(e.getMessage()
					.contains(airborneId));
		}
	}

	@Test
	public void testgetAssetFieldsOfRegardNoFieldOfRegard() {
		Mockito.when(apiService.getAssetMissionsAtTime(	ArgumentMatchers.eq(AIRCRAFT_NAME),
														ArgumentMatchers.any(DateTime.class)))
				.thenReturn(null);

		try {
			assetAirborneService.getAssetFieldsOfRegard(AIRCRAFT_ID,
														AT_TIME,
														"");

			Assert.fail("There should be no field of regard for asset with ID " + AIRCRAFT_ID);
		}
		catch (final BadRequestException | AssetHasNoFieldOfRegardException e) {
			Assert.assertTrue(e.getMessage()
					.contains(AIRCRAFT_ID));
		}
		catch (final AssetIdDoesNotExistException e) {
			Assert.fail(e.getMessage());
		}
	}

	@Test
	public void testUpdateAsset() {
		try {
			assetAirborneService.updateAsset(	AIRCRAFT_ID,
												assetXml);
		}
		catch (final AssetIdDoesNotExistException | AssetIdDoesNotMatchException | BeansException e) {
			Assert.fail(e.getMessage());
		}
	}

	@Test
	public void testUpdateAssetDoesNotExist() {
		final String aircraftId = "1111";

		try {
			final String assetXml = this.assetXml.replace(	"name=\"id\" value=\"" + AIRCRAFT_ID + "\"",
															"name=\"id\" value=\"" + aircraftId + "\"");
			assetAirborneService.updateAsset(	aircraftId,
												assetXml);

			Assert.fail("Expected to fail because ID is not in database");
		}
		catch (final AssetIdDoesNotMatchException | BeansException e) {
			Assert.fail("Expected to fail because ID is not in database");
		}
		catch (final AssetIdDoesNotExistException e) {
			Assert.assertTrue(e.getMessage()
					.contains(aircraftId));
		}
	}

	@Test
	public void testUpdateAssetDoesNotMatch() {
		final String aircraftId = "1111";

		try {
			final String assetXml = this.assetXml.replace(	"name=\"id\" value=\"" + AIRCRAFT_ID + "\"",
															"name=\"id\" value=\"" + aircraftId + "\"");
			assetAirborneService.updateAsset(	AIRCRAFT_ID,
												assetXml);

			Assert.fail("Expected to fail because IDs did not match");
		}
		catch (final AssetIdDoesNotMatchException e) {
			Assert.assertTrue(e.getMessage()
					.contains(aircraftId));
			Assert.assertTrue(e.getMessage()
					.contains(AIRCRAFT_ID));
		}
		catch (final AssetIdDoesNotExistException | BeansException e) {
			Assert.fail("Expected to fail because IDs did not match");
		}
	}

	@Test
	public void testUpdateAssetBadXml() {
		try {
			assetAirborneService.updateAsset(	AIRCRAFT_ID,
												"bad xml");

			Assert.fail("Expected to fail parsing XML");
		}
		catch (final AssetIdDoesNotMatchException | AssetIdDoesNotExistException e) {
			Assert.fail("Expected to fail parsing XML");
		}
		catch (final BeansException e) {
			Assert.assertNotNull(e.getMessage());
			Assert.assertTrue(e.getMessage()
					.contains("SAXParseException"));
		}
	}

	@Test
	public void testUpdateAssetExtraData() {
		final String extraDataName = AIRCRAFT_NAME + "_extradata";
		final ExtraData extraData = new ExtraData();
		extraData.setType("text");
		extraData.setData("The extra data for the asset");

		try {
			assetAirborneService.createAssetExtraData(	AIRCRAFT_ID,
														extraDataName,
														extraData);

			final ExtraData extraData2 = new ExtraData();
			extraData2.setType("text");
			extraData2.setData("The modified extra data for the asset");

			assetAirborneService.updateAssetExtraData(	AIRCRAFT_ID,
														extraDataName,
														extraData2);
		}
		catch (final AssetExtraDataExistsException | AssetIdDoesNotExistException
				| AssetExtraDataDoesNotExistException e) {
			Assert.fail(e.getMessage());
		}
	}

	@Test
	public void testUpdateAssetExtraDataAssetIdDoesNotExist() {
		final String aircraftId = "1111";
		final String extraDataName = AIRCRAFT_NAME + "_extradata";
		final ExtraData extraData = new ExtraData();
		extraData.setType("text");
		extraData.setData("The extra data for the asset");

		try {
			assetAirborneService.createAssetExtraData(	AIRCRAFT_ID,
														extraDataName,
														extraData);
		}
		catch (final AssetExtraDataExistsException | AssetIdDoesNotExistException e) {
			Assert.fail(e.getMessage());
		}

		try {
			final ExtraData extraData2 = new ExtraData();
			extraData2.setType("text");
			extraData2.setData("The modified extra data for the asset");

			assetAirborneService.updateAssetExtraData(	aircraftId,
														extraDataName,
														extraData2);

			Assert.fail("There should be no asset with ID " + aircraftId);
		}
		catch (final AssetExtraDataDoesNotExistException e) {
			Assert.fail(e.getMessage());
		}
		catch (final AssetIdDoesNotExistException e) {
			Assert.assertTrue(e.getMessage()
					.contains(aircraftId));
		}
	}

	@Test
	public void testUpdateAssetExtraDataDoesNotExist() {
		final String extraDataName = AIRCRAFT_NAME + "_extradata";
		final String doesNotExistExtraDataName = "doesnotexist";
		final ExtraData extraData = new ExtraData();
		extraData.setType("text");
		extraData.setData("The extra data for the asset");

		try {
			assetAirborneService.createAssetExtraData(	AIRCRAFT_ID,
														extraDataName,
														extraData);

			final ExtraData extraData2 = new ExtraData();
			extraData2.setType("text");
			extraData2.setData("The modified extra data for the asset");

			assetAirborneService.updateAssetExtraData(	AIRCRAFT_ID,
														doesNotExistExtraDataName,
														extraData2);

			Assert.fail("There should be no extra data with name " + doesNotExistExtraDataName);
		}
		catch (final AssetExtraDataExistsException | AssetIdDoesNotExistException e) {
			Assert.fail(e.getMessage());
		}
		catch (final AssetExtraDataDoesNotExistException e) {
			Assert.assertTrue(e.getMessage()
					.contains(AIRCRAFT_ID));
			Assert.assertTrue(e.getMessage()
					.contains(doesNotExistExtraDataName));
		}
	}

	@Test
	public void testDeleteAssetExtraData() {
		final String extraDataName = AIRCRAFT_NAME + "_extradata";
		final ExtraData extraData = new ExtraData();
		extraData.setType("text");
		extraData.setData("The extra data for the asset");

		try {
			assetAirborneService.createAssetExtraData(	AIRCRAFT_ID,
														extraDataName,
														extraData);

			assetAirborneService.deleteAssetExtraData(	AIRCRAFT_ID,
														extraDataName);
		}
		catch (final AssetExtraDataExistsException | AssetIdDoesNotExistException
				| AssetExtraDataDoesNotExistException e) {
			Assert.fail(e.getMessage());
		}
	}

	@Test
	public void testDeleteAssetExtraDataAssetIdDoesNotExist() {
		final String aircraftId = "1111";
		final String extraDataName = AIRCRAFT_NAME + "_extradata";
		final ExtraData extraData = new ExtraData();
		extraData.setType("text");
		extraData.setData("The extra data for the asset");

		try {
			assetAirborneService.createAssetExtraData(	AIRCRAFT_ID,
														extraDataName,
														extraData);

			assetAirborneService.deleteAssetExtraData(	aircraftId,
														extraDataName);

			Assert.fail("There should be no asset with ID " + aircraftId);
		}
		catch (final AssetExtraDataExistsException | AssetExtraDataDoesNotExistException e) {
			Assert.fail(e.getMessage());
		}
		catch (final AssetIdDoesNotExistException e) {
			Assert.assertTrue(e.getMessage()
					.contains(aircraftId));
		}
	}

	@Test
	public void testDeleteAssetExtraDataDoesNotExist() {
		final String extraDataName = AIRCRAFT_NAME + "_extradata";
		final String doesNotExistExtraDataName = "doesnotexist";
		final ExtraData extraData = new ExtraData();
		extraData.setType("text");
		extraData.setData("The extra data for the asset");

		try {
			assetAirborneService.createAssetExtraData(	AIRCRAFT_ID,
														extraDataName,
														extraData);

			assetAirborneService.deleteAssetExtraData(	AIRCRAFT_ID,
														doesNotExistExtraDataName);

			Assert.fail("There should be no extra data with name " + doesNotExistExtraDataName);
		}
		catch (final AssetExtraDataExistsException | AssetIdDoesNotExistException e) {
			Assert.fail(e.getMessage());
		}
		catch (final AssetExtraDataDoesNotExistException e) {
			Assert.assertTrue(e.getMessage()
					.contains(AIRCRAFT_ID));
			Assert.assertTrue(e.getMessage()
					.contains(doesNotExistExtraDataName));
		}
	}

	@Test
	public void testGetAssetSmears()
			throws NoMissionsFoundException,
			MissionIdDoesNotExistException,
			IOException,
			AssetIdDoesNotExistException {
		Mockito.doCallRealMethod()
				.when(apiService)
				.updateAssetMission(Mockito.any(),
									Mockito.any(),
									Mockito.anyString());

		Mockito.doCallRealMethod()
				.when(apiService)
				.updateAssetMission(Mockito.any(),
									Mockito.any(),
									Mockito.any(),
									Mockito.anyString());

		final MissionModel missionModel;
		try (final InputStream missionModelStream = this.getClass()
				.getResourceAsStream("/json/mission_example.json")) {
			missionModel = objectMapper.readValue(	missionModelStream,
													MissionModel.class);
		}

		Mockito.when(apiService.getAssetMissionsAtTime(	Mockito.eq(AIRCRAFT_ID),
														Mockito.any()))
				.thenReturn(Collections.singletonList(missionModel));

		Mockito.when(apiService.getAssetMissionsAtTime(	Mockito.eq(AIRCRAFT_ID),
														Mockito.any(),
														Mockito.any()))
				.thenReturn(Collections.singletonList(missionModel));

		final AssetSmearWithBeamsRequest assetSmearWithBeamsRequest = new AssetSmearWithBeamsRequest();
		assetSmearWithBeamsRequest.setAssetId(AIRCRAFT_ID);
		assetSmearWithBeamsRequest.setStartTimeISO8601(AT_TIME.toString());
		assetSmearWithBeamsRequest.setStopTimeISO8601(AT_TIME.plusSeconds(30)
				.toString());
		assetSmearWithBeamsRequest.setForFrameIncrementSec(1);
		assetSmearWithBeamsRequest.setForFramesCzmlRequested(false);
		assetSmearWithBeamsRequest.setOpBeamsCzmlRequested(false);
		assetSmearWithBeamsRequest.setSmearCzmlRequested(false);

		final List<AssetSmear> assetSmears = assetAirborneService.getAssetSmears(	assetSmearWithBeamsRequest,
																					null);

		Assert.assertNotNull(assetSmears);
		Assert.assertEquals(1,
							assetSmears.size());
		Assert.assertEquals(AIRCRAFT_NAME,
							assetSmears.get(0)
									.getAsset()
									.getName());
		Assert.assertEquals(AIRCRAFT_ID,
							Integer.toString(assetSmears.get(0)
									.getAsset()
									.id()));
	}
}
