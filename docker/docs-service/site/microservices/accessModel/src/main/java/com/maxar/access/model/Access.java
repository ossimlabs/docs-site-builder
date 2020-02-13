package com.maxar.access.model;

import java.util.List;

import org.joda.time.DateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;

import lombok.Data;

@Data
public class Access implements
		Comparable<Access>
{
	private String startTimeISO8601;
	private String endTimeISO8601;
	private String tcaTimeISO8601;
	@JsonIgnore
	private List<JsonNode> czml;

	public void calculateTCA(
			final DateTime untrimmedTCA ) {
		if (untrimmedTCA
				.isBefore(
						new DateTime(
								startTimeISO8601))) {
			tcaTimeISO8601 = startTimeISO8601;
		}
		else if (untrimmedTCA
				.isAfter(
						new DateTime(
								endTimeISO8601))) {
			tcaTimeISO8601 = endTimeISO8601;
		}
		else {
			tcaTimeISO8601 = untrimmedTCA.toString();
		}
	}

	@Override
	public int compareTo(
			final Access o ) {
		return new DateTime(
				startTimeISO8601)
						.compareTo(
								new DateTime(
										o.getStartTimeISO8601()));
	}

}
