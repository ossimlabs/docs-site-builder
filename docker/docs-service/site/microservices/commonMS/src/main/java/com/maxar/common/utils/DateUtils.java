package com.maxar.common.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

public class DateUtils
{
	public static final DateTime START_OF_TIME = new DateTime(
			0000,
			1,
			1,
			0,
			0,
			0,
			DateTimeZone.UTC);

	public static final DateTime END_OF_TIME = new DateTime(
			9999,
			1,
			1,
			0,
			0,
			0,
			DateTimeZone.UTC);

	public static String dateTimeToXml(
			final DateTime dt ) {
		return dateToXml(
				dt.toDate());
	}

	public static String dateToXml(
			final Date dt ) {

		SimpleDateFormat xmlDateFormatter;

		xmlDateFormatter = new SimpleDateFormat(
				"yyyy-MM-dd'T'HH:mm:ss'Z'");
		xmlDateFormatter.setTimeZone(
				TimeZone.getTimeZone(
						"GMT"));

		return xmlDateFormatter.format(
				dt);
	}
}
