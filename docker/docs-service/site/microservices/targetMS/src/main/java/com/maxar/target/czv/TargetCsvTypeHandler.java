package com.maxar.target.czv;

import java.util.Collections;
import java.util.List;

import com.maxar.common.csv.CsvTypeHandler;
import com.maxar.target.model.TargetModel;

public class TargetCsvTypeHandler implements
		CsvTypeHandler<TargetModel>
{

	private static final List<String> HEADERS = List.of("ID",
														"NAME",
														"DESCRIPTION",
														"CC",
														"GEO REGION",
														"ORDER OF BATTLE",
														"TYPE",
														"GEOMETRY");
	private static final String HEADERS_STRING = String.join(	", ",
																HEADERS);

	@Override
	public boolean canHandle(
			final Class<? extends Object> clazz ) {
		return TargetModel.class.isAssignableFrom(clazz);
	}

	@Override
	public String headers() {
		return HEADERS_STRING;
	}

	@Override
	public List<String> handle(
			final TargetModel target ) {

		return Collections.singletonList(target.asCsv());
	}

}
