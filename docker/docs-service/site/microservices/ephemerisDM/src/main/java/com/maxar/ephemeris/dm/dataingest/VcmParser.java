package com.maxar.ephemeris.dm.dataingest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.maxar.ephemeris.entity.VCM;
import com.maxar.manager.dataingest.DataTypeParser;

public class VcmParser extends
		DataTypeParser<VCM>
{
	private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormat
			.forPattern("yyyyDDD'('ddMMM')'HH:mm:s.SSS");

	private static final String VCM_HEADER = "SP VECTOR/COVARIANCE MESSAGE";

	private static final String VCM_LINE_PREFIX = "<>";

	// VCM level indicies
	private static final int SCN_LINE_NUMBER = 3;
	private static final int SCN_COLUMN_START = 20;
	private static final int SCN_COLUMN_END = 46;

	private static final int EPOCH_LINE_NUMBER = 5;
	private static final int EPOCH_COLUMN_START = 20;
	private static final int EPOCH_COLUMN_END = 53;

	private static List<VCM> parseVcms(
			final InputStream rawText ) {
		final Logger logger = Logger.getLogger(VcmParser.class);

		final List<VCM> vcms = new ArrayList<>();

		try (final BufferedReader reader = new BufferedReader(
				new InputStreamReader(
						rawText))) {
			// Filter out empty lines and those that do not start with '<>'
			final List<String> filteredLines = reader.lines()
					.filter(x -> !x.trim()
							.isEmpty())
					.filter(x -> x.startsWith(VCM_LINE_PREFIX))
					.collect(Collectors.toList());

			StringBuilder sb = null;

			for (final String line : filteredLines) {
				if (line.contains(VCM_HEADER)) {
					// If this isn't the first VCM, create the previous VCM
					if (sb != null) {
						final VCM vcm = createVcm(sb.toString());

						vcms.add(vcm);
					}

					// New VCM, add the header
					sb = new StringBuilder();
					sb.append(line);
					sb.append("\n");
				}
				else if ((sb != null) && (filteredLines.indexOf(line) == (filteredLines.size() - 1))) {
					// The last line of the file. Close the last VCM
					sb.append(line);

					final VCM vcm = createVcm(sb.toString());

					vcms.add(vcm);
				}
				else {
					// Regular line, add it to the current VCM
					if (sb != null) {
						sb.append(line);
						sb.append("\n");
					}
				}
			}
		}
		catch (final IOException e) {
			logger.error(e);

			return Collections.emptyList();
		}

		return vcms;
	}

	private static VCM createVcm(
			final String vcmText ) {
		final VCM vcm = new VCM(
				vcmText);

		updateVcm(vcm);

		return vcm;
	}

	private static void updateVcm(
			final VCM vcm ) {
		final String[] lines = StringUtils.split(	vcm.getVcm(),
													"\n");

		// Parse out the SCN and set it
		final String scnLine = lines[SCN_LINE_NUMBER];

		final String scn = scnLine.substring(	SCN_COLUMN_START,
												SCN_COLUMN_END)
				.trim();

		vcm.setScn(Integer.valueOf(scn));

		// Parse out the epoch and set it
		// Strip out all spaces since the format is not
		// Consistent with padding 0s. The formatter
		// Will account for this
		final String epochLine = lines[EPOCH_LINE_NUMBER];
		final String epoch = epochLine.substring(	EPOCH_COLUMN_START,
		                                         	EPOCH_COLUMN_END)
				.trim()
				.replace(	" ",
							"");

		final DateTime epochDateTime = DATE_TIME_FORMATTER.parseDateTime(epoch);

		vcm.setEpochMillis(epochDateTime.getMillis());
	}

	@Override
	public List<VCM> parseData(
			final InputStream input,
			final int beginIndex,
			final int toIndex )
			throws Exception {
		// parse whole file
		return parseVcms(input);
	}
}
