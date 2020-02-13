package com.maxar.ephemeris.dm.dataingest;

import java.io.File;
import java.io.InputStream;
import java.util.List;

import org.apache.log4j.Logger;

import com.maxar.ephemeris.entity.Ephemeris;
import com.maxar.ephemeris.repository.EphemerisRepository;
import com.maxar.manager.dataingest.DataTypeParser;
import com.maxar.manager.dataingest.FileIngester;
import com.radiantblue.analytics.core.log.SourceLogger;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EphemerisFileIngester extends
		FileIngester
{
	private static Logger logger = SourceLogger.getLogger(EphemerisFileIngester.class.getName());

	private DataTypeParser<?> parser;

	private EphemerisRepository repository;

	@Override
	public void ingestToDatabase(
			final File f,
			final List<File> associatedFiles )
			throws Exception {
		try (final InputStream input = openInputStream(f)) {
			@SuppressWarnings("unchecked")
			final List<Ephemeris> ephemerisList = (List<Ephemeris>) parser.parseData(input);

			repository.saveAll(ephemerisList);
		}
	}

	@Override
	protected void internalInit() {
		if (!successDir.isDirectory() && !successDir.mkdir()) {
			logger.warn("Unable to create success directory, " + successDir.getAbsolutePath());
		}
		if (!errorDir.isDirectory() && !errorDir.mkdir()) {
			logger.warn("Unable to create error directory, " + errorDir.getAbsolutePath());
		}
	}

}
