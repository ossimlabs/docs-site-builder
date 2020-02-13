package com.maxar.weather.dm.manager;

import java.io.File;
import java.util.List;

import com.maxar.manager.dataingest.FileIngester;

public class BadFileIngester extends
		FileIngester
{
	@Override
	public void ingestToDatabase(
			File f,
			List<File> associatedFiles )
			throws Exception {
		// Unimplemented because it's useless for this test case.
	}

	@Override
	protected void internalInit() {
		// Unimplemented because it's useless for this test case.
	}
}
