package com.maxar.weather.controller;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import com.maxar.common.exception.BadRequestException;
import com.maxar.weather.model.map.RTNEPHModel;
import com.maxar.weather.model.map.WTMModel;
import com.maxar.weather.model.weather.CloudCoverAtTime;
import com.maxar.weather.model.weather.WeatherByDate;
import com.maxar.weather.model.weather.WeatherByDateAndGeometryRequest;
import com.maxar.weather.model.weather.WeatherByDateAndGeometryRequestBody;
import com.maxar.weather.model.weather.WeatherByGeometry;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class WeatherControllerTest
{
	private static final String EXAMPLE_WKT = "POLYGON((120.0 31.0,120.6 31.0,120.6 30.8,120.0 30.8,120.0 31.0))";

	private static final String EXAMPLE_WKT_NO_WTM = "POLYGON((135.0 90.0,135.0 89.99,134.99 89.99,134.99 90.0,135.0 90.0))";

	private static final String EXAMPLE_WKT_NO_RTNEPH = "POLYGON((935.0 990.0,935.0 989.99,934.99 989.99,934.99 990.0,935.0 990.0))";

	private static final String EXAMPLE_WKT_INVALID = "POLGON((120.0 31.0,120.6 31.0,120.6 30.8,120.0 30.8,120.0 31.0))";

	private static final String EXAMPLE_START_DATE = "2020-01-01T06:00:00Z";

	private static final String EXAMPLE_END_DATE = "2020-01-01T09:00:00Z";

	private static final String EXAMPLE_START_DATE_NO_RESULTS = "1990-01-01T06:00:00Z";

	private static final String EXAMPLE_END_DATE_NO_RESULTS = "1990-01-01T09:00:00Z";

	private static final String EXAMPLE_DATE_INVALID = "foo";

	private static final String EXAMPLE_PARENT_ID = "parent";

	private static final String EXAMPLE_WTM_MAP_GRID_ID = "01200101";

	private static final String EXAMPLE_WTM_MAP_GRID_ID_NO_RESULTS = "00000000";

	private static final String EXAMPLE_RTNEPH_MAP_GRID_ID = "47610";

	private static final String EXAMPLE_RTNEPH_MAP_GRID_ID_NO_RESULTS = "00000";

	@Autowired
	private WeatherController weatherController;

	@Test
	public void testGetWeatherByWKTGeometryStringAndDate() {
		final ResponseEntity<Double> response = weatherController.getWeatherByWKTGeometryStringAndDate(	EXAMPLE_WKT,
																										EXAMPLE_START_DATE);

		Assert.assertNotNull(response);
		Assert.assertEquals(HttpStatus.OK,
							response.getStatusCode());
		Assert.assertNotNull(response.getBody());
		Assert.assertEquals(100.0,
							response.getBody(),
							0.0);
	}

	@Test
	public void testGetWeatherByWKTGeometryStringAndDateNoResults() {
		final ResponseEntity<Double> response = weatherController.getWeatherByWKTGeometryStringAndDate(	EXAMPLE_WKT,
																										EXAMPLE_START_DATE_NO_RESULTS);

		Assert.assertNotNull(response);
		Assert.assertEquals(HttpStatus.OK,
							response.getStatusCode());
		Assert.assertNotNull(response.getBody());
		Assert.assertEquals(0.0,
							response.getBody(),
							0.0);
	}

	@Test(expected = BadRequestException.class)
	public void testGetWeatherByWKTGeometryStringAndDateWktNull() {
		weatherController.getWeatherByWKTGeometryStringAndDate(	null,
																EXAMPLE_START_DATE);
	}

	@Test(expected = BadRequestException.class)
	public void testGetWeatherByWKTGeometryStringAndDateWktEmpty() {
		weatherController.getWeatherByWKTGeometryStringAndDate(	"",
																EXAMPLE_START_DATE);
	}

	@Test(expected = BadRequestException.class)
	public void testGetWeatherByWKTGeometryStringAndDateWktInvalid() {
		weatherController.getWeatherByWKTGeometryStringAndDate(	EXAMPLE_WKT_INVALID,
																EXAMPLE_START_DATE);
	}

	@Test(expected = BadRequestException.class)
	public void testGetWeatherByWKTGeometryStringAndDateDateNull() {
		weatherController.getWeatherByWKTGeometryStringAndDate(	EXAMPLE_WKT,
																null);
	}

	@Test(expected = BadRequestException.class)
	public void testGetWeatherByWKTGeometryStringAndDateDateEmpty() {
		weatherController.getWeatherByWKTGeometryStringAndDate(	EXAMPLE_WKT,
																"");
	}

	@Test(expected = BadRequestException.class)
	public void testGetWeatherByWKTGeometryStringAndDateDateInvalid() {
		weatherController.getWeatherByWKTGeometryStringAndDate(	EXAMPLE_WKT,
																EXAMPLE_DATE_INVALID);
	}

	@Test
	public void testGetWeatherByWKTGeometryStringAndDateRange() {
		final ResponseEntity<List<CloudCoverAtTime>> response = weatherController
				.getWeatherByWKTGeometryStringAndDateRange(	EXAMPLE_WKT,
															EXAMPLE_START_DATE,
															EXAMPLE_END_DATE);

		Assert.assertNotNull(response);
		Assert.assertEquals(HttpStatus.OK,
							response.getStatusCode());
		Assert.assertNotNull(response.getBody());
		Assert.assertEquals(2,
							response.getBody()
									.size());
		Assert.assertEquals(DateTime.parse(EXAMPLE_START_DATE),
							DateTime.parse(response.getBody()
									.get(0)
									.getIso8601Date()));
		Assert.assertEquals(100.0,
							response.getBody()
									.get(0)
									.getCloudCoverPercent(),
							0.0);
		Assert.assertEquals(DateTime.parse(EXAMPLE_END_DATE),
							DateTime.parse(response.getBody()
									.get(1)
									.getIso8601Date()));
		Assert.assertEquals(75.625,
							response.getBody()
									.get(1)
									.getCloudCoverPercent(),
							Double.MIN_VALUE);
	}

	@Test
	public void testGetWeatherByWKTGeometryStringAndDateRangeNoResults() {
		final ResponseEntity<List<CloudCoverAtTime>> response = weatherController
				.getWeatherByWKTGeometryStringAndDateRange(	EXAMPLE_WKT,
															EXAMPLE_START_DATE_NO_RESULTS,
															EXAMPLE_END_DATE_NO_RESULTS);

		Assert.assertNotNull(response);
		Assert.assertEquals(HttpStatus.OK,
							response.getStatusCode());
		Assert.assertNull(response.getBody());
	}

	@Test(expected = BadRequestException.class)
	public void testGetWeatherByWKTGeometryStringAndDateRangeWktNull() {
		weatherController.getWeatherByWKTGeometryStringAndDateRange(null,
																	EXAMPLE_START_DATE,
																	EXAMPLE_END_DATE);
	}

	@Test(expected = BadRequestException.class)
	public void testGetWeatherByWKTGeometryStringAndDateRangeWktEmpty() {
		weatherController.getWeatherByWKTGeometryStringAndDateRange("",
																	EXAMPLE_START_DATE,
																	EXAMPLE_END_DATE);
	}

	@Test(expected = BadRequestException.class)
	public void testGetWeatherByWKTGeometryStringAndDateRangeWktInvalid() {
		weatherController.getWeatherByWKTGeometryStringAndDateRange(EXAMPLE_WKT_INVALID,
																	EXAMPLE_START_DATE,
																	EXAMPLE_END_DATE);
	}

	@Test(expected = BadRequestException.class)
	public void testGetWeatherByWKTGeometryStringAndDateRangeStartDateNull() {
		weatherController.getWeatherByWKTGeometryStringAndDateRange(EXAMPLE_WKT,
																	null,
																	EXAMPLE_END_DATE);
	}

	@Test(expected = BadRequestException.class)
	public void testGetWeatherByWKTGeometryStringAndDateRangeStartDateEmpty() {
		weatherController.getWeatherByWKTGeometryStringAndDateRange(EXAMPLE_WKT,
																	"",
																	EXAMPLE_END_DATE);
	}

	@Test(expected = BadRequestException.class)
	public void testGetWeatherByWKTGeometryStringAndDateRangeStartDateInvalid() {
		weatherController.getWeatherByWKTGeometryStringAndDateRange(EXAMPLE_WKT,
																	EXAMPLE_DATE_INVALID,
																	EXAMPLE_END_DATE);
	}

	@Test(expected = BadRequestException.class)
	public void testGetWeatherByWKTGeometryStringAndDateRangeStopDateNull() {
		weatherController.getWeatherByWKTGeometryStringAndDateRange(EXAMPLE_WKT,
																	EXAMPLE_START_DATE,
																	null);
	}

	@Test(expected = BadRequestException.class)
	public void testGetWeatherByWKTGeometryStringAndDateRangeStopDateEmpty() {
		weatherController.getWeatherByWKTGeometryStringAndDateRange(EXAMPLE_WKT,
																	EXAMPLE_START_DATE,
																	"");
	}

	@Test(expected = BadRequestException.class)
	public void testGetWeatherByWKTGeometryStringAndDateRangeStopDateInvalid() {
		weatherController.getWeatherByWKTGeometryStringAndDateRange(EXAMPLE_WKT,
																	EXAMPLE_START_DATE,
																	EXAMPLE_DATE_INVALID);
	}

	@Test
	public void testGetWeatherAndGeometryByWKTGeometryStringAndDateRange() {
		final ResponseEntity<List<WeatherByDate>> response = weatherController
				.getWeatherAndGeometryByWKTGeometryStringAndDateRange(	EXAMPLE_WKT,
																		EXAMPLE_START_DATE,
																		EXAMPLE_END_DATE);

		Assert.assertNotNull(response);
		Assert.assertEquals(HttpStatus.OK,
							response.getStatusCode());
		Assert.assertNotNull(response.getBody());
		Assert.assertEquals(2,
							response.getBody()
									.size());
		Assert.assertEquals(DateTime.parse(EXAMPLE_START_DATE),
							DateTime.parse(response.getBody()
									.get(0)
									.getAtTimeISOFormat()));
		Assert.assertEquals(8,
							response.getBody()
									.get(0)
									.getWeathers()
									.size());

		final double expectedSum = Arrays.stream(new double[] {
			1.0,
			1.0,
			1.0,
			1.0,
			1.0,
			1.0,
			1.0,
			1.0
		})
				.sum();

		final double actualSum = response.getBody()
				.get(0)
				.getWeathers()
				.stream()
				.map(WeatherByGeometry::getCloudCoverPercent)
				.mapToDouble(Double::doubleValue)
				.sum();

		Assert.assertEquals(expectedSum,
							actualSum,
							0.0);
		Assert.assertEquals(DateTime.parse(EXAMPLE_END_DATE),
							DateTime.parse(response.getBody()
									.get(1)
									.getAtTimeISOFormat()));
		Assert.assertEquals(8,
							response.getBody()
									.get(1)
									.getWeathers()
									.size());

		final double expectedSum2 = Arrays.stream(new double[] {
			0.25,
			0.05,
			1.0,
			1.0,
			1.0,
			0.75,
			1.0,
			1.0
		})
				.sum();

		final double actualSum2 = response.getBody()
				.get(1)
				.getWeathers()
				.stream()
				.map(WeatherByGeometry::getCloudCoverPercent)
				.mapToDouble(Double::doubleValue)
				.sum();

		Assert.assertEquals(expectedSum2,
							actualSum2,
							Double.MIN_VALUE);
	}

	@Test
	public void testGetWeatherAndGeometryByWKTGeometryStringAndDateRangeNoResults() {
		final ResponseEntity<List<WeatherByDate>> response = weatherController
				.getWeatherAndGeometryByWKTGeometryStringAndDateRange(	EXAMPLE_WKT,
																		EXAMPLE_START_DATE_NO_RESULTS,
																		EXAMPLE_END_DATE_NO_RESULTS);

		Assert.assertNotNull(response);
		Assert.assertEquals(HttpStatus.OK,
							response.getStatusCode());
		Assert.assertNull(response.getBody());
	}

	@Test(expected = BadRequestException.class)
	public void testGetWeatherAndGeometryByWKTGeometryStringAndDateRangeWktNull() {
		weatherController.getWeatherAndGeometryByWKTGeometryStringAndDateRange(	null,
																				EXAMPLE_START_DATE,
																				EXAMPLE_END_DATE);
	}

	@Test(expected = BadRequestException.class)
	public void testGetWeatherAndGeometryByWKTGeometryStringAndDateRangeWktEmpty() {
		weatherController.getWeatherAndGeometryByWKTGeometryStringAndDateRange(	"",
																				EXAMPLE_START_DATE,
																				EXAMPLE_END_DATE);
	}

	@Test(expected = BadRequestException.class)
	public void testGetWeatherAndGeometryByWKTGeometryStringAndDateRangeWktInvalid() {
		weatherController.getWeatherAndGeometryByWKTGeometryStringAndDateRange(	EXAMPLE_WKT_INVALID,
																				EXAMPLE_START_DATE,
																				EXAMPLE_END_DATE);
	}

	@Test(expected = BadRequestException.class)
	public void testGetWeatherAndGeometryByWKTGeometryStringAndDateRangeStartDateNull() {
		weatherController.getWeatherAndGeometryByWKTGeometryStringAndDateRange(	EXAMPLE_WKT,
																				null,
																				EXAMPLE_END_DATE);
	}

	@Test(expected = BadRequestException.class)
	public void testGetWeatherAndGeometryByWKTGeometryStringAndDateRangeStartDateEmpty() {
		weatherController.getWeatherAndGeometryByWKTGeometryStringAndDateRange(	EXAMPLE_WKT,
																				"",
																				EXAMPLE_END_DATE);
	}

	@Test(expected = BadRequestException.class)
	public void testGetWeatherAndGeometryByWKTGeometryStringAndDateRangeStartDateInvalid() {
		weatherController.getWeatherAndGeometryByWKTGeometryStringAndDateRange(	EXAMPLE_WKT,
																				EXAMPLE_DATE_INVALID,
																				EXAMPLE_END_DATE);
	}

	@Test(expected = BadRequestException.class)
	public void testGetWeatherAndGeometryByWKTGeometryStringAndDateRangeStopDateNull() {
		weatherController.getWeatherAndGeometryByWKTGeometryStringAndDateRange(	EXAMPLE_WKT,
																				EXAMPLE_START_DATE,
																				null);
	}

	@Test(expected = BadRequestException.class)
	public void testGetWeatherAndGeometryByWKTGeometryStringAndDateRangeStopDateEmpty() {
		weatherController.getWeatherAndGeometryByWKTGeometryStringAndDateRange(	EXAMPLE_WKT,
																				EXAMPLE_START_DATE,
																				"");
	}

	@Test(expected = BadRequestException.class)
	public void testGetWeatherAndGeometryByWKTGeometryStringAndDateRangeStopDateInvalid() {
		weatherController.getWeatherAndGeometryByWKTGeometryStringAndDateRange(	EXAMPLE_WKT,
																				EXAMPLE_START_DATE,
																				EXAMPLE_DATE_INVALID);
	}

	@Test
	public void testGetWeathersAndGeometriesByWKTGeometryStringsAndDates() {
		final WeatherByDateAndGeometryRequestBody requestBody = new WeatherByDateAndGeometryRequestBody();
		requestBody.setParentId(EXAMPLE_PARENT_ID);
		final WeatherByDateAndGeometryRequest request = new WeatherByDateAndGeometryRequest();
		request.setDateTimeISO8601(EXAMPLE_START_DATE);
		request.setGeometryWKT(EXAMPLE_WKT);
		requestBody.setWeatherRequestList(Collections.singletonList(request));

		final ResponseEntity<List<WeatherByDate>> response = weatherController
				.getWeathersAndGeometriesByWKTGeometryStringsAndDates(requestBody);

		Assert.assertNotNull(response);
		Assert.assertEquals(HttpStatus.OK,
							response.getStatusCode());
		Assert.assertNotNull(response.getBody());
		Assert.assertEquals(1,
							response.getBody()
									.size());
		Assert.assertNotNull(response.getBody()
				.get(0));

		Assert.assertEquals(DateTime.parse(EXAMPLE_START_DATE),
							DateTime.parse(response.getBody()
									.get(0)
									.getAtTimeISOFormat()));
		Assert.assertEquals(EXAMPLE_PARENT_ID,
							response.getBody()
									.get(0)
									.getParentId());
		Assert.assertEquals(8,
							response.getBody()
									.get(0)
									.getWeathers()
									.size());

		final double expectedSum = Arrays.stream(new double[] {
			1.0,
			1.0,
			1.0,
			1.0,
			1.0,
			1.0,
			1.0,
			1.0
		})
				.sum();

		final double actualSum = response.getBody()
				.get(0)
				.getWeathers()
				.stream()
				.map(WeatherByGeometry::getCloudCoverPercent)
				.mapToDouble(Double::doubleValue)
				.sum();

		Assert.assertEquals(expectedSum,
							actualSum,
							0.0);
	}

	@Test
	public void testGetWeathersAndGeometriesByWKTGeometryStringsAndDatesNoResults() {
		final WeatherByDateAndGeometryRequestBody requestBody = new WeatherByDateAndGeometryRequestBody();
		requestBody.setParentId(EXAMPLE_PARENT_ID);
		final WeatherByDateAndGeometryRequest request = new WeatherByDateAndGeometryRequest();
		request.setDateTimeISO8601(EXAMPLE_START_DATE_NO_RESULTS);
		request.setGeometryWKT(EXAMPLE_WKT);
		requestBody.setWeatherRequestList(Collections.singletonList(request));

		final ResponseEntity<List<WeatherByDate>> response = weatherController
				.getWeathersAndGeometriesByWKTGeometryStringsAndDates(requestBody);

		Assert.assertNotNull(response);
		Assert.assertEquals(HttpStatus.OK,
							response.getStatusCode());
		Assert.assertNotNull(response.getBody());
		Assert.assertTrue(response.getBody()
				.isEmpty());
	}

	@Test(expected = BadRequestException.class)
	public void testGetWeathersAndGeometriesByWKTGeometryStringsAndDatesRequestBodyNull() {
		weatherController.getWeathersAndGeometriesByWKTGeometryStringsAndDates(null);
	}

	@Test(expected = BadRequestException.class)
	public void testGetWeathersAndGeometriesByWKTGeometryStringsAndDatesRequestBodyEmpty() {
		final WeatherByDateAndGeometryRequestBody requestBody = new WeatherByDateAndGeometryRequestBody();

		weatherController.getWeathersAndGeometriesByWKTGeometryStringsAndDates(requestBody);
	}

	@Test(expected = BadRequestException.class)
	public void testGetWeathersAndGeometriesByWKTGeometryStringsAndDatesRequestWktNull() {
		final WeatherByDateAndGeometryRequestBody requestBody = new WeatherByDateAndGeometryRequestBody();
		requestBody.setParentId(EXAMPLE_PARENT_ID);
		final WeatherByDateAndGeometryRequest request = new WeatherByDateAndGeometryRequest();
		request.setDateTimeISO8601(EXAMPLE_START_DATE);
		request.setGeometryWKT(null);
		requestBody.setWeatherRequestList(Collections.singletonList(request));

		weatherController.getWeathersAndGeometriesByWKTGeometryStringsAndDates(requestBody);
	}

	@Test(expected = BadRequestException.class)
	public void testGetWeathersAndGeometriesByWKTGeometryStringsAndDatesRequestWktEmpty() {
		final WeatherByDateAndGeometryRequestBody requestBody = new WeatherByDateAndGeometryRequestBody();
		requestBody.setParentId(EXAMPLE_PARENT_ID);
		final WeatherByDateAndGeometryRequest request = new WeatherByDateAndGeometryRequest();
		request.setDateTimeISO8601(EXAMPLE_START_DATE);
		request.setGeometryWKT("");
		requestBody.setWeatherRequestList(Collections.singletonList(request));

		weatherController.getWeathersAndGeometriesByWKTGeometryStringsAndDates(requestBody);
	}

	@Test(expected = BadRequestException.class)
	public void testGetWeathersAndGeometriesByWKTGeometryStringsAndDatesRequestWktInvalid() {
		final WeatherByDateAndGeometryRequestBody requestBody = new WeatherByDateAndGeometryRequestBody();
		requestBody.setParentId(EXAMPLE_PARENT_ID);
		final WeatherByDateAndGeometryRequest request = new WeatherByDateAndGeometryRequest();
		request.setDateTimeISO8601(EXAMPLE_START_DATE);
		request.setGeometryWKT(EXAMPLE_WKT_INVALID);
		requestBody.setWeatherRequestList(Collections.singletonList(request));

		weatherController.getWeathersAndGeometriesByWKTGeometryStringsAndDates(requestBody);
	}

	@Test(expected = BadRequestException.class)
	public void testGetWeathersAndGeometriesByWKTGeometryStringsAndDatesRequestDateNull() {
		final WeatherByDateAndGeometryRequestBody requestBody = new WeatherByDateAndGeometryRequestBody();
		requestBody.setParentId(EXAMPLE_PARENT_ID);
		final WeatherByDateAndGeometryRequest request = new WeatherByDateAndGeometryRequest();
		request.setDateTimeISO8601(null);
		request.setGeometryWKT(EXAMPLE_WKT);
		requestBody.setWeatherRequestList(Collections.singletonList(request));

		weatherController.getWeathersAndGeometriesByWKTGeometryStringsAndDates(requestBody);
	}

	@Test(expected = BadRequestException.class)
	public void testGetWeathersAndGeometriesByWKTGeometryStringsAndDatesRequestDateEmpty() {
		final WeatherByDateAndGeometryRequestBody requestBody = new WeatherByDateAndGeometryRequestBody();
		requestBody.setParentId(EXAMPLE_PARENT_ID);
		final WeatherByDateAndGeometryRequest request = new WeatherByDateAndGeometryRequest();
		request.setDateTimeISO8601("");
		request.setGeometryWKT(EXAMPLE_WKT);
		requestBody.setWeatherRequestList(Collections.singletonList(request));

		weatherController.getWeathersAndGeometriesByWKTGeometryStringsAndDates(requestBody);
	}

	@Test(expected = BadRequestException.class)
	public void testGetWeathersAndGeometriesByWKTGeometryStringsAndDatesRequestDateInvalid() {
		final WeatherByDateAndGeometryRequestBody requestBody = new WeatherByDateAndGeometryRequestBody();
		requestBody.setParentId(EXAMPLE_PARENT_ID);
		final WeatherByDateAndGeometryRequest request = new WeatherByDateAndGeometryRequest();
		request.setDateTimeISO8601(EXAMPLE_DATE_INVALID);
		request.setGeometryWKT(EXAMPLE_WKT);
		requestBody.setWeatherRequestList(Collections.singletonList(request));

		weatherController.getWeathersAndGeometriesByWKTGeometryStringsAndDates(requestBody);
	}

	@Test
	public void testGetWTMsByWKTGeometryString() {
		final ResponseEntity<List<WTMModel>> response = weatherController.getWTMsByWKTGeometryString(EXAMPLE_WKT);

		Assert.assertNotNull(response);
		Assert.assertEquals(HttpStatus.OK,
							response.getStatusCode());
		Assert.assertNotNull(response.getBody());
		Assert.assertEquals(8,
							response.getBody()
									.size());
	}

	@Test
	public void testGetWTMsByWKTGeometryStringNoResults() {
		final ResponseEntity<List<WTMModel>> response = weatherController
				.getWTMsByWKTGeometryString(EXAMPLE_WKT_NO_WTM);

		Assert.assertNotNull(response);
		Assert.assertEquals(HttpStatus.NO_CONTENT,
							response.getStatusCode());
		Assert.assertNull(response.getBody());
	}

	@Test(expected = BadRequestException.class)
	public void testGetWTMsByWKTGeometryStringWktNull() {
		weatherController.getWTMsByWKTGeometryString(null);
	}

	@Test(expected = BadRequestException.class)
	public void testGetWTMsByWKTGeometryStringWktEmpty() {
		weatherController.getWTMsByWKTGeometryString("");
	}

	@Test(expected = BadRequestException.class)
	public void testGetWTMsByWKTGeometryStringWktInvalid() {
		weatherController.getWTMsByWKTGeometryString(EXAMPLE_WKT_INVALID);
	}

	@Test
	public void testGetRTNPHSsByWKTGeometryString() {
		final ResponseEntity<List<RTNEPHModel>> response = weatherController.getRTNPHSsByWKTGeometryString(EXAMPLE_WKT);

		Assert.assertNotNull(response);
		Assert.assertEquals(HttpStatus.OK,
							response.getStatusCode());
		Assert.assertNotNull(response.getBody());
		Assert.assertEquals(1,
							response.getBody()
									.size());
	}

	@Test
	public void testGetRTNPHSsByWKTGeometryStringNoResults() {
		final ResponseEntity<List<RTNEPHModel>> response = weatherController
				.getRTNPHSsByWKTGeometryString(EXAMPLE_WKT_NO_RTNEPH);

		Assert.assertNotNull(response);
		Assert.assertEquals(HttpStatus.NO_CONTENT,
							response.getStatusCode());
		Assert.assertNull(response.getBody());
	}

	@Test(expected = BadRequestException.class)
	public void testGetRTNPHSsByWKTGeometryStringWktNull() {
		weatherController.getRTNPHSsByWKTGeometryString(null);
	}

	@Test(expected = BadRequestException.class)
	public void testGetRTNPHSsByWKTGeometryStringWktEmpty() {
		weatherController.getRTNPHSsByWKTGeometryString("");
	}

	@Test(expected = BadRequestException.class)
	public void testGetRTNPHSsByWKTGeometryStringWktInvalid() {
		weatherController.getRTNPHSsByWKTGeometryString(EXAMPLE_WKT_INVALID);
	}

	@Test
	public void testGetWTMsByMapGridId() {
		final ResponseEntity<List<WTMModel>> response = weatherController.getWTMsByMapGridId(EXAMPLE_WTM_MAP_GRID_ID);

		Assert.assertNotNull(response);
		Assert.assertEquals(HttpStatus.OK,
							response.getStatusCode());
		Assert.assertNotNull(response.getBody());
		Assert.assertEquals(1,
							response.getBody()
									.size());
		Assert.assertEquals(1,
							response.getBody()
									.get(0)
									.getWtmId());
	}

	@Test
	public void testGetWTMsByMapGridIdNoResults() {
		final ResponseEntity<List<WTMModel>> response = weatherController
				.getWTMsByMapGridId(EXAMPLE_WTM_MAP_GRID_ID_NO_RESULTS);

		Assert.assertNotNull(response);
		Assert.assertEquals(HttpStatus.NOT_FOUND,
							response.getStatusCode());
		Assert.assertNull(response.getBody());
	}

	@Test
	public void testGetRTNEPHsByMapGridId() {
		final ResponseEntity<List<RTNEPHModel>> response = weatherController
				.getRTNEPHsByMapGridId(EXAMPLE_RTNEPH_MAP_GRID_ID);

		Assert.assertNotNull(response);
		Assert.assertEquals(HttpStatus.OK,
							response.getStatusCode());
		Assert.assertNotNull(response.getBody());
		Assert.assertEquals(2,
							response.getBody()
									.size());
		Assert.assertEquals(Integer.parseInt(EXAMPLE_RTNEPH_MAP_GRID_ID),
							response.getBody()
									.get(0)
									.getRtnephId());
		Assert.assertEquals(Integer.parseInt(EXAMPLE_RTNEPH_MAP_GRID_ID),
							response.getBody()
									.get(1)
									.getRtnephId());
	}

	@Test
	public void testGetRTNEPHsByMapGridIdNoResults() {
		final ResponseEntity<List<RTNEPHModel>> response = weatherController
				.getRTNEPHsByMapGridId(EXAMPLE_RTNEPH_MAP_GRID_ID_NO_RESULTS);

		Assert.assertNotNull(response);
		Assert.assertEquals(HttpStatus.NOT_FOUND,
							response.getStatusCode());
		Assert.assertNull(response.getBody());
	}
}
