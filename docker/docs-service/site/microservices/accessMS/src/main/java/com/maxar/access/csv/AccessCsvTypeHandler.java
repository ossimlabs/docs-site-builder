package com.maxar.access.csv;

import java.util.List;

import com.maxar.access.model.UntrimmedAccess;
import com.maxar.common.csv.CsvTypeHandler;

public class AccessCsvTypeHandler implements
		CsvTypeHandler<UntrimmedAccess>
{

	private static final List<String> HEADERS = List.of("START",
														"TCA",
														"END",
														"ASSET ID",
														"SENSOR MODE",
														"FAILURE REASON",
														"REV",
														"PASS",
														"PROPAGATOR",
														"GEOMETRY",
														"TRIMMED START",
														"TRIMMED TCA",
														"TRIMMED END");
	private static final String HEADERS_STRING = String.join(	", ",
																HEADERS);

	@Override
	public boolean canHandle(
			final Class<? extends Object> clazz ) {
		return UntrimmedAccess.class.isAssignableFrom(clazz);
	}

	@Override
	public String headers() {
		return HEADERS_STRING;
	}

	@Override
	public List<String> handle(
			final UntrimmedAccess access ) {

		return access.asCsv();
	}

}
