package com.maxar.asset.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.stream.Collectors;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
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

import com.maxar.asset.common.exception.AssetExtraDataDoesNotExistException;
import com.maxar.asset.common.exception.AssetIdDoesNotExistException;
import com.maxar.asset.common.exception.AssetNameDoesNotExistException;
import com.maxar.asset.common.exception.NoEphemerisFoundException;
import com.maxar.asset.common.exception.PropagatorTypeDoesNotExistException;
import com.maxar.asset.common.service.ApiService;
import com.maxar.asset.exception.AssetExtraDataExistsException;
import com.maxar.asset.exception.AssetIdDoesNotMatchException;
import com.maxar.asset.exception.AssetIdExistsException;
import com.maxar.asset.exception.AssetNameExistsException;
import com.maxar.asset.model.AssetSmear;
import com.maxar.asset.model.AssetSmearWithBeamsRequest;
import com.maxar.asset.model.ExtraData;
import com.maxar.asset.model.FieldOfRegard;
import com.maxar.common.exception.BadRequestException;
import com.maxar.ephemeris.model.EphemerisType;
import com.maxar.ephemeris.model.TLEModel;
import com.radiantblue.analytics.isr.core.model.asset.Asset;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@TestPropertySource(locations = "classpath:testassetms.properties")
@AutoConfigureTestDatabase
public class AssetSpaceServiceTest
{
	@Autowired
	private AssetSpaceService assetSpaceService;

	@MockBean
	private ApiService apiService;

	private String assetXml;

	private TLEModel tleModel;

	private static final String SATELLITE_ID = "32060";

	private static final String SATELLITE_NAME = "WV01";

	private static final DateTime AT_TIME = ISODateTimeFormat.dateTimeParser()
			.parseDateTime("2019-07-01T00:00:00.000Z");

	@Before
	public void setUp() {
		tleModel = new TLEModel();
		tleModel.setScn(Integer.parseInt(SATELLITE_ID));
		tleModel.setType(EphemerisType.TLE);
		tleModel.setEpochMillis(1560460296793L);
		tleModel.setDescription("WORLDVIEW-1 (WV-1)");
		tleModel.setTleLineOne("1 32060U 07041A   19164.88306474  .00000453  00000-0  21379-4 0  9992");
		tleModel.setTleLineTwo("2 32060  97.3900 284.5950 0001530 113.2584 246.8815 15.24382087652969");

		try (final InputStream inputStream = this.getClass()
				.getResourceAsStream("/xml/WV01_full.xml")) {
			assetXml = new BufferedReader(
					new InputStreamReader(
							inputStream)).lines()
									.collect(Collectors.joining(System.lineSeparator()));

			final String satelliteId = assetSpaceService.createAsset(assetXml);

			Assert.assertEquals(SATELLITE_ID,
								satelliteId);

			Mockito.doCallRealMethod()
					.when(apiService)
					.updateAssetEphemeris(	ArgumentMatchers.any(Asset.class),
											ArgumentMatchers.any(DateTime.class),
											ArgumentMatchers.anyString());
		}
		catch (final AssetIdExistsException | AssetNameExistsException | BeansException | IOException
				| PropagatorTypeDoesNotExistException | NoEphemerisFoundException e) {
			Assert.fail(e.getMessage());
		}
	}

	@After
	public void tearDown() {
		try {
			assetSpaceService.deleteAsset(SATELLITE_ID);
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
			assetSpaceService.createAsset("bad xml");

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
			final String satelliteId = assetSpaceService.createAsset(assetXml);

			Assert.fail("Asset with ID " + satelliteId + " should not have been added twice.");
		}
		catch (final AssetIdExistsException e) {
			Assert.assertTrue(e.getMessage()
					.contains(SATELLITE_ID));
		}
		catch (final AssetNameExistsException | BeansException e) {
			Assert.fail(e.getMessage());
		}
	}

	@Test
	public void testCreateAssetTwiceDifferentId() {
		try {
			final String assetXml = this.assetXml.replace(	"name=\"id\" value=\"" + SATELLITE_ID + "\"",
															"name=\"id\" value=\"1111\"");

			assetSpaceService.createAsset(assetXml);

			Assert.fail("Asset with name " + SATELLITE_NAME + " should not have been added twice.");
		}
		catch (final AssetNameExistsException e) {
			Assert.assertTrue(e.getMessage()
					.contains(SATELLITE_NAME));
		}
		catch (final AssetIdExistsException | BeansException e) {
			Assert.fail(e.getMessage());
		}
	}

	@Test
	public void testCreateAssetExtraData() {
		final String extraDataName = SATELLITE_NAME + "_extradata";
		final ExtraData extraData = new ExtraData();
		extraData.setType("text");
		extraData.setData("The extra data for the asset");

		try {
			assetSpaceService.createAssetExtraData(	SATELLITE_ID,
													extraDataName,
													extraData);
		}
		catch (final AssetExtraDataExistsException | AssetIdDoesNotExistException e) {
			Assert.fail(e.getMessage());
		}
	}

	@Test
	public void testCreateAssetExtraDataTwice() {
		final String extraDataName = SATELLITE_NAME + "_extradata";
		final ExtraData extraData = new ExtraData();
		extraData.setType("text");
		extraData.setData("The extra data for the asset");

		try {
			assetSpaceService.createAssetExtraData(	SATELLITE_ID,
													extraDataName,
													extraData);
		}
		catch (final AssetExtraDataExistsException | AssetIdDoesNotExistException e) {
			Assert.fail(e.getMessage());
		}

		try {
			assetSpaceService.createAssetExtraData(	SATELLITE_ID,
													extraDataName,
													extraData);

			Assert.fail("Extra data with name " + extraDataName + " should not have been added twice.");
		}
		catch (final AssetExtraDataExistsException e) {
			Assert.assertTrue(e.getMessage()
					.contains(SATELLITE_ID));
			Assert.assertTrue(e.getMessage()
					.contains(extraDataName));
		}
		catch (final AssetIdDoesNotExistException e) {
			Assert.fail(e.getMessage());
		}
	}

	@Test
	public void testCreateAssetExtraDataDoesNotExist() {
		final String extraDataName = SATELLITE_NAME + "_extradata";
		final String satelliteId = "1111";
		final ExtraData extraData = new ExtraData();
		extraData.setType("text");
		extraData.setData("The extra data for the asset");

		try {
			assetSpaceService.createAssetExtraData(	satelliteId,
													extraDataName,
													extraData);

			Assert.fail("There should be no asset with ID " + satelliteId);
		}
		catch (final AssetExtraDataExistsException e) {
			Assert.fail(e.getMessage());
		}
		catch (final AssetIdDoesNotExistException e) {
			Assert.assertTrue(e.getMessage()
					.contains(satelliteId));
		}
	}

	@Test
	public void testGetAllAssetIds() {
		final List<String> assetIds = assetSpaceService.getAllAssetIds();

		Assert.assertEquals(1,
							assetIds.size());
		Assert.assertEquals(SATELLITE_ID,
							assetIds.get(0));
	}

	@Test
	public void testGetAssetById() {
		try {
			final String assetXml = assetSpaceService.getAssetById(SATELLITE_ID);

			Assert.assertEquals(this.assetXml,
								assetXml);
		}
		catch (final AssetIdDoesNotExistException e) {
			Assert.fail(e.getMessage());
		}
	}

	@Test
	public void testGetAssetByIdDoesNotExist() {
		final String satelliteId = "1111";

		try {
			assetSpaceService.getAssetById(satelliteId);

			Assert.fail("There should be no asset with ID " + satelliteId);
		}
		catch (final AssetIdDoesNotExistException e) {
			Assert.assertTrue(e.getMessage()
					.contains(satelliteId));
		}
	}

	@Test
	public void testGetAllAssetNames() {
		final List<String> assetNames = assetSpaceService.getAllAssetNames();

		Assert.assertEquals(1,
							assetNames.size());
		Assert.assertEquals(SATELLITE_NAME,
							assetNames.get(0));
	}

	@Test
	public void testGetAssetIdByName() {
		try {
			final String satelliteId = assetSpaceService.getAssetIdByName(SATELLITE_NAME);

			Assert.assertEquals(SATELLITE_ID,
								satelliteId);
		}
		catch (final AssetNameDoesNotExistException e) {
			Assert.fail(e.getMessage());
		}
	}

	@Test
	public void testGetAssetIdByNameDoesNotExist() {
		final String satelliteName = "1111";

		try {
			assetSpaceService.getAssetIdByName(satelliteName);

			Assert.fail("There should be no asset with name " + satelliteName);
		}
		catch (final AssetNameDoesNotExistException e) {
			Assert.assertTrue(e.getMessage()
					.contains(satelliteName));
		}
	}

	@Test
	public void testGetAllAssetExtraDataNames() {
		final String extraDataName = SATELLITE_NAME + "_extradata";
		final ExtraData extraData = new ExtraData();
		extraData.setType("text");
		extraData.setData("The extra data for the asset");

		try {
			assetSpaceService.createAssetExtraData(	SATELLITE_ID,
													extraDataName,
													extraData);

			final List<String> extraDataNames = assetSpaceService.getAllAssetExtraDataNames(SATELLITE_ID);

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
			final List<String> extraDataNames = assetSpaceService.getAllAssetExtraDataNames(SATELLITE_ID);

			Assert.assertTrue(extraDataNames.isEmpty());
		}
		catch (final AssetIdDoesNotExistException e) {
			Assert.fail(e.getMessage());
		}
	}

	@Test
	public void testGetAllAssetExtraDataNamesDoesNotExist() {
		final String satelliteId = "1111";

		try {
			assetSpaceService.getAllAssetExtraDataNames(satelliteId);

			Assert.fail("There should be no asset with ID " + satelliteId);
		}
		catch (final AssetIdDoesNotExistException e) {
			Assert.assertTrue(e.getMessage()
					.contains(satelliteId));
		}
	}

	@Test
	public void testGetAssetExtraDataByName() {
		final String extraDataName = SATELLITE_NAME + "_extradata";
		final ExtraData extraData = new ExtraData();
		extraData.setType("text");
		extraData.setData("The extra data for the asset");

		try {
			assetSpaceService.createAssetExtraData(	SATELLITE_ID,
													extraDataName,
													extraData);

			final ExtraData extraData2 = assetSpaceService.getAssetExtraDataByName(	SATELLITE_ID,
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
		final String satelliteId = "1111";
		final String extraDataName = SATELLITE_NAME + "_extradata";

		try {
			assetSpaceService.getAssetExtraDataByName(	satelliteId,
														extraDataName);

			Assert.fail("There should be no asset with ID " + satelliteId);
		}
		catch (final AssetExtraDataDoesNotExistException e) {
			Assert.fail(e.getMessage());
		}
		catch (final AssetIdDoesNotExistException e) {
			Assert.assertTrue(e.getMessage()
					.contains(satelliteId));
		}
	}

	@Test
	public void testGetAssetExtraDataByNameDoesNotExist() {
		final String extraDataName = SATELLITE_NAME + "_extradata";
		final String doesNotExistExtraDataName = "doesnotexist";
		final ExtraData extraData = new ExtraData();
		extraData.setType("text");
		extraData.setData("The extra data for the asset");

		try {
			assetSpaceService.createAssetExtraData(	SATELLITE_ID,
													extraDataName,
													extraData);

			assetSpaceService.getAssetExtraDataByName(	SATELLITE_ID,
														doesNotExistExtraDataName);

			Assert.fail("There should be no extra data with name " + doesNotExistExtraDataName);
		}
		catch (final AssetExtraDataExistsException | AssetIdDoesNotExistException e) {
			Assert.fail(e.getMessage());
		}
		catch (final AssetExtraDataDoesNotExistException e) {
			Assert.assertTrue(e.getMessage()
					.contains(SATELLITE_ID));
			Assert.assertTrue(e.getMessage()
					.contains(doesNotExistExtraDataName));
		}
	}

	@Test
	public void testgetAssetFieldsOfRegardNadirFOR()
			throws AssetIdDoesNotExistException {
		Mockito.when(apiService.getAssetEphemerisAtTime(	ArgumentMatchers.eq(SATELLITE_ID),
													ArgumentMatchers.any(DateTime.class)))
				.thenReturn(tleModel);

		final List<FieldOfRegard> fieldsOfRegard = assetSpaceService.getAssetFieldsOfRegard(SATELLITE_ID,
																							AT_TIME,
																							"");

		fieldsOfRegard.forEach(fieldOfRegard -> Assert.assertNotNull(fieldOfRegard.getFieldOfRegardAngleWkt()));
	}

	@Test
	public void testgetAssetFieldsOfRegardButterflyFOR()
			throws AssetIdDoesNotExistException {
		Mockito.when(apiService.getAssetEphemerisAtTime(	ArgumentMatchers.anyString(),
													ArgumentMatchers.any(DateTime.class)))
				.thenReturn(tleModel);

		String deleteSatelliteId = "";

		try (final InputStream inputStream = this.getClass()
				.getResourceAsStream("/xml/RS02_full.xml")) {
			final String assetXml = new BufferedReader(
					new InputStreamReader(
							inputStream)).lines()
									.collect(Collectors.joining(System.lineSeparator()));

			final String satelliteId = assetSpaceService.createAsset(assetXml);
			deleteSatelliteId = satelliteId;

			final List<FieldOfRegard> fieldsOfRegard = assetSpaceService.getAssetFieldsOfRegard(satelliteId,
																								AT_TIME,
																								"");

			fieldsOfRegard.forEach(fieldOfRegard -> Assert.assertNotNull(fieldOfRegard.getFieldOfRegardAngleWkt()));
		}
		catch (final AssetIdExistsException | AssetNameExistsException | BeansException | IOException e) {
			Assert.fail(e.getMessage());
		}
		finally {
			if (!deleteSatelliteId.isEmpty()) {
				assetSpaceService.deleteAsset(deleteSatelliteId);
			}
		}
	}

	@Test
	public void testgetAssetFieldsOfRegardAxebladeFOR()
			throws AssetIdDoesNotExistException {
		Mockito.when(apiService.getAssetEphemerisAtTime(	ArgumentMatchers.anyString(),
													ArgumentMatchers.any(DateTime.class)))
				.thenReturn(tleModel);

		String deleteSatelliteId = "";

		try (final InputStream inputStream = this.getClass()
				.getResourceAsStream("/xml/RS02_full_axebladeFOR.xml")) {
			final String assetXml = new BufferedReader(
					new InputStreamReader(
							inputStream)).lines()
									.collect(Collectors.joining(System.lineSeparator()));

			final String satelliteId = assetSpaceService.createAsset(assetXml);
			deleteSatelliteId = satelliteId;

			final List<FieldOfRegard> fieldsOfRegard = assetSpaceService.getAssetFieldsOfRegard(satelliteId,
																								AT_TIME,
																								"");

			fieldsOfRegard.forEach(fieldOfRegard -> Assert.assertNotNull(fieldOfRegard.getFieldOfRegardAngleWkt()));
		}
		catch (final AssetIdExistsException | AssetNameExistsException | BeansException | IOException e) {
			Assert.fail(e.getMessage());
		}
		finally {
			if (!deleteSatelliteId.isEmpty()) {
				assetSpaceService.deleteAsset(deleteSatelliteId);
			}
		}
	}

	@Test
	public void testgetAssetFieldsOfRegardGrazeFOR()
			throws AssetIdDoesNotExistException {
		Mockito.when(apiService.getAssetEphemerisAtTime(	ArgumentMatchers.anyString(),
													ArgumentMatchers.any(DateTime.class)))
				.thenReturn(tleModel);

		String deleteSatelliteId = "";

		try (final InputStream inputStream = this.getClass()
				.getResourceAsStream("/xml/SIRIUS01.xml")) {
			final String assetXml = new BufferedReader(
					new InputStreamReader(
							inputStream)).lines()
									.collect(Collectors.joining(System.lineSeparator()));

			final String satelliteId = assetSpaceService.createAsset(assetXml);
			deleteSatelliteId = satelliteId;

			final List<FieldOfRegard> fieldsOfRegard = assetSpaceService.getAssetFieldsOfRegard(satelliteId,
																								AT_TIME,
																								"");

			fieldsOfRegard.forEach(fieldOfRegard -> Assert.assertNotNull(fieldOfRegard.getFieldOfRegardAngleWkt()));
		}
		catch (final AssetIdExistsException | AssetNameExistsException | BeansException | IOException e) {
			Assert.fail(e.getMessage());
		}
		finally {
			if (!deleteSatelliteId.isEmpty()) {
				assetSpaceService.deleteAsset(deleteSatelliteId);
			}
		}
	}

	@Test
	public void testgetAssetFieldsOfRegardJ2PropagatorType()
			throws AssetIdDoesNotExistException {
		Mockito.when(apiService.getAssetEphemerisAtTime(	ArgumentMatchers.eq(SATELLITE_ID),
													ArgumentMatchers.any(DateTime.class)))
				.thenReturn(tleModel);

		final List<FieldOfRegard> fieldsOfRegard = assetSpaceService.getAssetFieldsOfRegard(SATELLITE_ID,
																							AT_TIME,
																							"J2");

		fieldsOfRegard.forEach(fieldOfRegard -> Assert.assertNotNull(fieldOfRegard.getFieldOfRegardAngleWkt()));
	}

	@Test
	public void testgetAssetFieldsOfRegardSGP4PropagatorType()
			throws AssetIdDoesNotExistException {
		Mockito.when(apiService.getAssetEphemerisAtTime(	ArgumentMatchers.eq(SATELLITE_ID),
													ArgumentMatchers.any(DateTime.class)))
				.thenReturn(tleModel);

		final List<FieldOfRegard> fieldsOfRegard = assetSpaceService.getAssetFieldsOfRegard(SATELLITE_ID,
																							AT_TIME,
																							"SGP4");

		fieldsOfRegard.forEach(fieldOfRegard -> Assert.assertNotNull(fieldOfRegard.getFieldOfRegardAngleWkt()));
	}

	@Test
	public void testgetAssetFieldsOfRegardDoesNotExist() {
		final String satelliteId = "1111";

		try {
			assetSpaceService.getAssetFieldsOfRegard(	satelliteId,
														AT_TIME,
														"");

			Assert.fail("There should be no asset with ID " + satelliteId);
		}
		catch (final AssetIdDoesNotExistException e) {
			Assert.assertTrue(e.getMessage()
					.contains(satelliteId));
		}
	}

	@Test
	public void testgetAssetFieldsOfRegardInvalidPropagatorType() {
		Mockito.when(apiService.getAssetEphemerisAtTime(	ArgumentMatchers.eq(SATELLITE_ID),
													ArgumentMatchers.any(DateTime.class)))
				.thenReturn(tleModel);

		try {
			assetSpaceService.getAssetFieldsOfRegard(	SATELLITE_ID,
														AT_TIME,
														"INVALID");

			Assert.fail("The propagator type is invalid and should have thrown an exception");
		}
		catch (final BadRequestException e) {
			Assert.assertTrue(e.getMessage()
					.contains("INVALID"));
		}
		catch (final AssetIdDoesNotExistException e) {
			Assert.fail(e.getMessage());
		}
	}

	@Test
	public void testgetAssetFieldsOfRegardNoEphemeris() {
		Mockito.when(apiService.getAssetEphemerisAtTime(	ArgumentMatchers.eq(SATELLITE_ID),
													ArgumentMatchers.any(DateTime.class)))
				.thenReturn(null);

		try {
			assetSpaceService.getAssetFieldsOfRegard(	SATELLITE_ID,
														AT_TIME,
														"J2");

			Assert.fail("No ephemeris should have been found and should have thrown an exception");
		}
		catch (final BadRequestException e) {
			Assert.assertTrue(e.getMessage()
					.contains(SATELLITE_NAME));
		}
		catch (final AssetIdDoesNotExistException e) {
			Assert.fail(e.getMessage());
		}
	}

	@Test
	public void testUpdateAsset() {
		try {
			assetSpaceService.updateAsset(	SATELLITE_ID,
											assetXml);
		}
		catch (final AssetIdDoesNotExistException | AssetIdDoesNotMatchException | BeansException e) {
			Assert.fail(e.getMessage());
		}
	}

	@Test
	public void testUpdateAssetDoesNotExist() {
		final String satelliteId = "1111";

		try {
			final String assetXml = this.assetXml.replace(	"name=\"id\" value=\"" + SATELLITE_ID + "\"",
															"name=\"id\" value=\"" + satelliteId + "\"");
			assetSpaceService.updateAsset(	satelliteId,
											assetXml);

			Assert.fail("Expected to fail because ID is not in database");
		}
		catch (final AssetIdDoesNotMatchException | BeansException e) {
			Assert.fail("Expected to fail because ID is not in database");
		}
		catch (final AssetIdDoesNotExistException e) {
			Assert.assertTrue(e.getMessage()
					.contains(satelliteId));
		}
	}

	@Test
	public void testUpdateAssetDoesNotMatch() {
		final String satelliteId = "1111";

		try {
			final String assetXml = this.assetXml.replace(	"name=\"id\" value=\"" + SATELLITE_ID + "\"",
															"name=\"id\" value=\"" + satelliteId + "\"");
			assetSpaceService.updateAsset(	SATELLITE_ID,
											assetXml);

			Assert.fail("Expected to fail because IDs did not match");
		}
		catch (final AssetIdDoesNotMatchException e) {
			Assert.assertTrue(e.getMessage()
					.contains(satelliteId));
			Assert.assertTrue(e.getMessage()
					.contains(SATELLITE_ID));
		}
		catch (final AssetIdDoesNotExistException | BeansException e) {
			Assert.fail("Expected to fail because IDs did not match");
		}
	}

	@Test
	public void testUpdateAssetBadXml() {
		try {
			assetSpaceService.updateAsset(	SATELLITE_ID,
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
		final String extraDataName = SATELLITE_NAME + "_extradata";
		final ExtraData extraData = new ExtraData();
		extraData.setType("text");
		extraData.setData("The extra data for the asset");

		try {
			assetSpaceService.createAssetExtraData(	SATELLITE_ID,
													extraDataName,
													extraData);

			final ExtraData extraData2 = new ExtraData();
			extraData2.setType("text");
			extraData2.setData("The modified extra data for the asset");

			assetSpaceService.updateAssetExtraData(	SATELLITE_ID,
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
		final String satelliteId = "1111";
		final String extraDataName = SATELLITE_NAME + "_extradata";
		final ExtraData extraData = new ExtraData();
		extraData.setType("text");
		extraData.setData("The extra data for the asset");

		try {
			assetSpaceService.createAssetExtraData(	SATELLITE_ID,
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

			assetSpaceService.updateAssetExtraData(	satelliteId,
													extraDataName,
													extraData2);

			Assert.fail("There should be no asset with ID " + satelliteId);
		}
		catch (final AssetExtraDataDoesNotExistException e) {
			Assert.fail(e.getMessage());
		}
		catch (final AssetIdDoesNotExistException e) {
			Assert.assertTrue(e.getMessage()
					.contains(satelliteId));
		}
	}

	@Test
	public void testUpdateAssetExtraDataDoesNotExist() {
		final String extraDataName = SATELLITE_NAME + "_extradata";
		final String doesNotExistExtraDataName = "doesnotexist";
		final ExtraData extraData = new ExtraData();
		extraData.setType("text");
		extraData.setData("The extra data for the asset");

		try {
			assetSpaceService.createAssetExtraData(	SATELLITE_ID,
													extraDataName,
													extraData);

			final ExtraData extraData2 = new ExtraData();
			extraData2.setType("text");
			extraData2.setData("The modified extra data for the asset");

			assetSpaceService.updateAssetExtraData(	SATELLITE_ID,
													doesNotExistExtraDataName,
													extraData2);

			Assert.fail("There should be no extra data with name " + doesNotExistExtraDataName);
		}
		catch (final AssetExtraDataExistsException | AssetIdDoesNotExistException e) {
			Assert.fail(e.getMessage());
		}
		catch (final AssetExtraDataDoesNotExistException e) {
			Assert.assertTrue(e.getMessage()
					.contains(SATELLITE_ID));
			Assert.assertTrue(e.getMessage()
					.contains(doesNotExistExtraDataName));
		}
	}

	@Test
	public void testDeleteAssetExtraData() {
		final String extraDataName = SATELLITE_NAME + "_extradata";
		final ExtraData extraData = new ExtraData();
		extraData.setType("text");
		extraData.setData("The extra data for the asset");

		try {
			assetSpaceService.createAssetExtraData(	SATELLITE_ID,
													extraDataName,
													extraData);

			assetSpaceService.deleteAssetExtraData(	SATELLITE_ID,
													extraDataName);
		}
		catch (final AssetExtraDataExistsException | AssetIdDoesNotExistException
				| AssetExtraDataDoesNotExistException e) {
			Assert.fail(e.getMessage());
		}
	}

	@Test
	public void testDeleteAssetExtraDataAssetIdDoesNotExist() {
		final String satelliteId = "1111";
		final String extraDataName = SATELLITE_NAME + "_extradata";
		final ExtraData extraData = new ExtraData();
		extraData.setType("text");
		extraData.setData("The extra data for the asset");

		try {
			assetSpaceService.createAssetExtraData(	SATELLITE_ID,
													extraDataName,
													extraData);

			assetSpaceService.deleteAssetExtraData(	satelliteId,
													extraDataName);

			Assert.fail("There should be no asset with ID " + satelliteId);
		}
		catch (final AssetExtraDataExistsException | AssetExtraDataDoesNotExistException e) {
			Assert.fail(e.getMessage());
		}
		catch (final AssetIdDoesNotExistException e) {
			Assert.assertTrue(e.getMessage()
					.contains(satelliteId));
		}
	}

	@Test
	public void testDeleteAssetExtraDataDoesNotExist() {
		final String extraDataName = SATELLITE_NAME + "_extradata";
		final String doesNotExistExtraDataName = "doesnotexist";
		final ExtraData extraData = new ExtraData();
		extraData.setType("text");
		extraData.setData("The extra data for the asset");

		try {
			assetSpaceService.createAssetExtraData(	SATELLITE_ID,
													extraDataName,
													extraData);

			assetSpaceService.deleteAssetExtraData(	SATELLITE_ID,
													doesNotExistExtraDataName);

			Assert.fail("There should be no extra data with name " + doesNotExistExtraDataName);
		}
		catch (final AssetExtraDataExistsException | AssetIdDoesNotExistException e) {
			Assert.fail(e.getMessage());
		}
		catch (final AssetExtraDataDoesNotExistException e) {
			Assert.assertTrue(e.getMessage()
					.contains(SATELLITE_ID));
			Assert.assertTrue(e.getMessage()
					.contains(doesNotExistExtraDataName));
		}
	}

	@Test
	public void testGetAssetSmearsPropagatorTypeNullSelectJ2()
			throws AssetIdDoesNotExistException,
			NoEphemerisFoundException,
			PropagatorTypeDoesNotExistException {
		Mockito.when(apiService.updateAssetEphemeris(	Mockito.any(),
														Mockito.any(),
														Mockito.isNull()))
				.thenCallRealMethod();

		Mockito.when(apiService.getAssetEphemerisAtTime(	Mockito.eq(SATELLITE_ID),
													Mockito.any()))
				.thenReturn(tleModel);

		final AssetSmearWithBeamsRequest assetSmearWithBeamsRequest = new AssetSmearWithBeamsRequest();
		assetSmearWithBeamsRequest.setAssetId(SATELLITE_ID);
		assetSmearWithBeamsRequest.setStartTimeISO8601(AT_TIME.toString());
		assetSmearWithBeamsRequest.setStopTimeISO8601(AT_TIME.plusSeconds(30)
				.toString());
		assetSmearWithBeamsRequest.setForFrameIncrementSec(1);
		assetSmearWithBeamsRequest.setForFramesCzmlRequested(false);
		assetSmearWithBeamsRequest.setOpBeamsCzmlRequested(false);
		assetSmearWithBeamsRequest.setSmearCzmlRequested(false);

		final List<AssetSmear> assetSmears = assetSpaceService.getAssetSmears(	assetSmearWithBeamsRequest,
																				null);

		Assert.assertNotNull(assetSmears);
		Assert.assertEquals(1,
							assetSmears.size());
		Assert.assertEquals(SATELLITE_NAME,
							assetSmears.get(0)
									.getAsset()
									.getName());
		Assert.assertEquals(SATELLITE_ID,
							Integer.toString(assetSmears.get(0)
									.getAsset()
									.id()));
	}

	@Test
	public void testGetAssetSmearsPropagatorTypeEmptySelectJ2()
			throws AssetIdDoesNotExistException,
			NoEphemerisFoundException,
			PropagatorTypeDoesNotExistException {
		Mockito.when(apiService.updateAssetEphemeris(	Mockito.any(),
														Mockito.any(),
														Mockito.eq("")))
				.thenCallRealMethod();

		Mockito.when(apiService.getAssetEphemerisAtTime(	Mockito.eq(SATELLITE_ID),
													Mockito.any()))
				.thenReturn(tleModel);

		final AssetSmearWithBeamsRequest assetSmearWithBeamsRequest = new AssetSmearWithBeamsRequest();
		assetSmearWithBeamsRequest.setAssetId(SATELLITE_ID);
		assetSmearWithBeamsRequest.setStartTimeISO8601(AT_TIME.toString());
		assetSmearWithBeamsRequest.setStopTimeISO8601(AT_TIME.plusSeconds(30)
				.toString());
		assetSmearWithBeamsRequest.setForFrameIncrementSec(1);
		assetSmearWithBeamsRequest.setForFramesCzmlRequested(false);
		assetSmearWithBeamsRequest.setOpBeamsCzmlRequested(false);
		assetSmearWithBeamsRequest.setSmearCzmlRequested(false);

		final List<AssetSmear> assetSmears = assetSpaceService.getAssetSmears(	assetSmearWithBeamsRequest,
																				"");

		Assert.assertNotNull(assetSmears);
		Assert.assertEquals(1,
							assetSmears.size());
		Assert.assertEquals(SATELLITE_NAME,
							assetSmears.get(0)
									.getAsset()
									.getName());
		Assert.assertEquals(SATELLITE_ID,
							Integer.toString(assetSmears.get(0)
									.getAsset()
									.id()));
	}

	@Test
	public void testGetAssetSmearsPropagatorTypeJ2()
			throws AssetIdDoesNotExistException,
			NoEphemerisFoundException,
			PropagatorTypeDoesNotExistException {
		Mockito.when(apiService.updateAssetEphemeris(	Mockito.any(),
														Mockito.any(),
														Mockito.eq("J2")))
				.thenCallRealMethod();

		Mockito.when(apiService.getAssetEphemerisAtTime(	Mockito.eq(SATELLITE_ID),
													Mockito.any()))
				.thenReturn(tleModel);

		final AssetSmearWithBeamsRequest assetSmearWithBeamsRequest = new AssetSmearWithBeamsRequest();
		assetSmearWithBeamsRequest.setAssetId(SATELLITE_ID);
		assetSmearWithBeamsRequest.setStartTimeISO8601(AT_TIME.toString());
		assetSmearWithBeamsRequest.setStopTimeISO8601(AT_TIME.plusSeconds(30)
				.toString());
		assetSmearWithBeamsRequest.setForFrameIncrementSec(1);
		assetSmearWithBeamsRequest.setForFramesCzmlRequested(false);
		assetSmearWithBeamsRequest.setOpBeamsCzmlRequested(false);
		assetSmearWithBeamsRequest.setSmearCzmlRequested(false);

		final List<AssetSmear> assetSmears = assetSpaceService.getAssetSmears(	assetSmearWithBeamsRequest,
																				"J2");

		Assert.assertNotNull(assetSmears);
		Assert.assertEquals(1,
							assetSmears.size());
		Assert.assertEquals(SATELLITE_NAME,
							assetSmears.get(0)
									.getAsset()
									.getName());
		Assert.assertEquals(SATELLITE_ID,
							Integer.toString(assetSmears.get(0)
									.getAsset()
									.id()));
	}

	@Test
	public void testGetAssetSmearsPropagatorTypeJ2UnevenIncrement()
			throws AssetIdDoesNotExistException,
			NoEphemerisFoundException,
			PropagatorTypeDoesNotExistException {
		Mockito.when(apiService.updateAssetEphemeris(	Mockito.any(),
														Mockito.any(),
														Mockito.eq("J2")))
				.thenCallRealMethod();

		Mockito.when(apiService.getAssetEphemerisAtTime(	Mockito.eq(SATELLITE_ID),
													Mockito.any()))
				.thenReturn(tleModel);

		final AssetSmearWithBeamsRequest assetSmearWithBeamsRequest = new AssetSmearWithBeamsRequest();
		assetSmearWithBeamsRequest.setAssetId(SATELLITE_ID);
		assetSmearWithBeamsRequest.setStartTimeISO8601(AT_TIME.toString());
		assetSmearWithBeamsRequest.setStopTimeISO8601(AT_TIME.plusSeconds(30)
				.toString());
		assetSmearWithBeamsRequest.setForFrameIncrementSec(7);
		assetSmearWithBeamsRequest.setForFramesCzmlRequested(false);
		assetSmearWithBeamsRequest.setOpBeamsCzmlRequested(false);
		assetSmearWithBeamsRequest.setSmearCzmlRequested(false);

		final List<AssetSmear> assetSmears = assetSpaceService.getAssetSmears(	assetSmearWithBeamsRequest,
																				"J2");

		Assert.assertNotNull(assetSmears);
		Assert.assertEquals(1,
							assetSmears.size());
		Assert.assertEquals(SATELLITE_NAME,
							assetSmears.get(0)
									.getAsset()
									.getName());
		Assert.assertEquals(SATELLITE_ID,
							Integer.toString(assetSmears.get(0)
									.getAsset()
									.id()));
	}

	@Test
	public void testGetAssetSmearsPropagatorTypeNullSelectSGP4()
			throws AssetIdDoesNotExistException,
			NoEphemerisFoundException,
			PropagatorTypeDoesNotExistException {
		Mockito.when(apiService.updateAssetEphemeris(	Mockito.any(),
														Mockito.any(),
														Mockito.isNull()))
				.thenCallRealMethod();

		Mockito.when(apiService.getAssetEphemerisAtTime(	Mockito.eq(SATELLITE_ID),
													Mockito.any()))
				.thenReturn(tleModel);

		final DateTime startTime = new DateTime(
				tleModel.getEpochMillis(),
				DateTimeZone.UTC);

		final AssetSmearWithBeamsRequest assetSmearWithBeamsRequest = new AssetSmearWithBeamsRequest();
		assetSmearWithBeamsRequest.setAssetId(SATELLITE_ID);
		assetSmearWithBeamsRequest.setStartTimeISO8601(startTime.toString());
		assetSmearWithBeamsRequest.setStopTimeISO8601(startTime.plusSeconds(30)
				.toString());
		assetSmearWithBeamsRequest.setForFrameIncrementSec(1);
		assetSmearWithBeamsRequest.setForFramesCzmlRequested(false);
		assetSmearWithBeamsRequest.setOpBeamsCzmlRequested(false);
		assetSmearWithBeamsRequest.setSmearCzmlRequested(false);

		final List<AssetSmear> assetSmears = assetSpaceService.getAssetSmears(	assetSmearWithBeamsRequest,
																				null);

		Assert.assertNotNull(assetSmears);
		Assert.assertEquals(1,
							assetSmears.size());
		Assert.assertEquals(SATELLITE_NAME,
							assetSmears.get(0)
									.getAsset()
									.getName());
		Assert.assertEquals(SATELLITE_ID,
							Integer.toString(assetSmears.get(0)
									.getAsset()
									.id()));
	}

	@Test
	public void testGetAssetSmearsPropagatorTypeEmptySelectSGP4()
			throws AssetIdDoesNotExistException,
			NoEphemerisFoundException,
			PropagatorTypeDoesNotExistException {
		Mockito.when(apiService.updateAssetEphemeris(	Mockito.any(),
														Mockito.any(),
														Mockito.eq("")))
				.thenCallRealMethod();

		Mockito.when(apiService.getAssetEphemerisAtTime(	Mockito.eq(SATELLITE_ID),
													Mockito.any()))
				.thenReturn(tleModel);

		final DateTime startTime = new DateTime(
				tleModel.getEpochMillis(),
				DateTimeZone.UTC);

		final AssetSmearWithBeamsRequest assetSmearWithBeamsRequest = new AssetSmearWithBeamsRequest();
		assetSmearWithBeamsRequest.setAssetId(SATELLITE_ID);
		assetSmearWithBeamsRequest.setStartTimeISO8601(startTime.toString());
		assetSmearWithBeamsRequest.setStopTimeISO8601(startTime.plusSeconds(30)
				.toString());
		assetSmearWithBeamsRequest.setForFrameIncrementSec(1);
		assetSmearWithBeamsRequest.setForFramesCzmlRequested(false);
		assetSmearWithBeamsRequest.setOpBeamsCzmlRequested(false);
		assetSmearWithBeamsRequest.setSmearCzmlRequested(false);

		final List<AssetSmear> assetSmears = assetSpaceService.getAssetSmears(	assetSmearWithBeamsRequest,
																				"");

		Assert.assertNotNull(assetSmears);
		Assert.assertEquals(1,
							assetSmears.size());
		Assert.assertEquals(SATELLITE_NAME,
							assetSmears.get(0)
									.getAsset()
									.getName());
		Assert.assertEquals(SATELLITE_ID,
							Integer.toString(assetSmears.get(0)
									.getAsset()
									.id()));
	}

	@Test
	public void testGetAssetSmearsPropagatorTypeSGP4()
			throws AssetIdDoesNotExistException,
			NoEphemerisFoundException,
			PropagatorTypeDoesNotExistException {
		Mockito.when(apiService.updateAssetEphemeris(	Mockito.any(),
														Mockito.any(),
														Mockito.eq("SGP4")))
				.thenCallRealMethod();

		Mockito.when(apiService.getAssetEphemerisAtTime(	Mockito.eq(SATELLITE_ID),
													Mockito.any()))
				.thenReturn(tleModel);

		final DateTime startTime = new DateTime(
				tleModel.getEpochMillis(),
				DateTimeZone.UTC);

		final AssetSmearWithBeamsRequest assetSmearWithBeamsRequest = new AssetSmearWithBeamsRequest();
		assetSmearWithBeamsRequest.setAssetId(SATELLITE_ID);
		assetSmearWithBeamsRequest.setStartTimeISO8601(startTime.toString());
		assetSmearWithBeamsRequest.setStopTimeISO8601(startTime.plusSeconds(30)
				.toString());
		assetSmearWithBeamsRequest.setForFrameIncrementSec(1);
		assetSmearWithBeamsRequest.setForFramesCzmlRequested(false);
		assetSmearWithBeamsRequest.setOpBeamsCzmlRequested(false);
		assetSmearWithBeamsRequest.setSmearCzmlRequested(false);

		final List<AssetSmear> assetSmears = assetSpaceService.getAssetSmears(	assetSmearWithBeamsRequest,
																				"SGP4");

		Assert.assertNotNull(assetSmears);
		Assert.assertEquals(1,
							assetSmears.size());
		Assert.assertEquals(SATELLITE_NAME,
							assetSmears.get(0)
									.getAsset()
									.getName());
		Assert.assertEquals(SATELLITE_ID,
							Integer.toString(assetSmears.get(0)
									.getAsset()
									.id()));
	}

	@Test(expected = AssetIdDoesNotExistException.class)
	public void testGetAssetSmearsAssetDoesNotExist()
			throws AssetIdDoesNotExistException {
		final AssetSmearWithBeamsRequest assetSmearWithBeamsRequest = new AssetSmearWithBeamsRequest();
		assetSmearWithBeamsRequest.setAssetId("does not exist");
		assetSmearWithBeamsRequest.setStartTimeISO8601(AT_TIME.toString());
		assetSmearWithBeamsRequest.setStopTimeISO8601(AT_TIME.plusSeconds(30)
				.toString());
		assetSmearWithBeamsRequest.setForFrameIncrementSec(1);
		assetSmearWithBeamsRequest.setForFramesCzmlRequested(false);
		assetSmearWithBeamsRequest.setOpBeamsCzmlRequested(false);
		assetSmearWithBeamsRequest.setSmearCzmlRequested(false);

		assetSpaceService.getAssetSmears(	assetSmearWithBeamsRequest,
											"J2");
	}
}
