package com.maxar.ephemeris.dm.dataingest;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.joda.time.DateTime;

import com.maxar.ephemeris.entity.StateVector;
import com.maxar.ephemeris.entity.StateVectorSet;
import com.maxar.manager.dataingest.DataTypeParser;

//
//Unclass format Craig Davis made up
//  SCN
//  AT_TIME like YYYY-MM-SSTHH:MM:SS.MMMZ    -|
//  ECIPOS.X                                  |
//  ECIPOS.Y                                  |
//  ECIPOS.Z                                  |
//  ECIVEL.X                                  |
//  ECIVEL.Y                                  |
//  ECIVEL.Z                                  |
//  ECIACC.X                                  |
//  ECIACC.Y                                  |
//  ECIACC.Z                                  |
//  ECFPOS.X                                  |====== These repeat multiple times
//  ECFPOS.Y                                  |
//  ECFPOS.Z                                  |
//  ECFVEL.X                                  |
//  ECFVEL.Y                                  |
//  ECFVEL.Z                                  |
//  ECFACC.X                                  |
//  ECFACC.Y                                  |
//  ECFACC.Z                                 -|
//  DONE                                   - Indicate end of records for this scn

public class SampleStateVectorParser extends
		DataTypeParser<StateVectorSet>
{
	@Override
	public List<StateVectorSet> parseData(
			final InputStream input,
			final int beginIndex,
			final int toIndex )
			throws Exception {
		// parse whole file
		return parseData(input);
	}

	@Override
	public List<StateVectorSet> parseData(
			final InputStream input )
			throws Exception {

		final ArrayList<StateVectorSet> returnSvs = new ArrayList<>();

		final BufferedReader br = new BufferedReader(
				new InputStreamReader(
						input));

		final String scnLine = br.readLine();
		if (scnLine != null) {
			final StateVectorSet stateVectorSet = new StateVectorSet();
			final Set<StateVector> svs = new HashSet<>();
			final int scn = Integer.parseInt(scnLine);
			stateVectorSet.setScn(scn);

			while (true) {
				final String first = br.readLine();
				if (first.equals("DONE")) {
					break;
				}
				final StateVector theVector = new StateVector();

				// first String holds atTime
				final DateTime dt = DateTime.parse(first);
				theVector.setAtTimeMillis(dt.getMillis());

				theVector.setEciPosX(Double.parseDouble(br.readLine()));
				theVector.setEciPosY(Double.parseDouble(br.readLine()));
				theVector.setEciPosZ(Double.parseDouble(br.readLine()));

				theVector.setEciVelX(Double.parseDouble(br.readLine()));
				theVector.setEciVelY(Double.parseDouble(br.readLine()));
				theVector.setEciVelZ(Double.parseDouble(br.readLine()));

				theVector.setEciAccelX(Double.parseDouble(br.readLine()));
				theVector.setEciAccelY(Double.parseDouble(br.readLine()));
				theVector.setEciAccelZ(Double.parseDouble(br.readLine()));

				theVector.setEcfPosX(Double.parseDouble(br.readLine()));
				theVector.setEcfPosY(Double.parseDouble(br.readLine()));
				theVector.setEcfPosZ(Double.parseDouble(br.readLine()));

				theVector.setEcfVelX(Double.parseDouble(br.readLine()));
				theVector.setEcfVelY(Double.parseDouble(br.readLine()));
				theVector.setEcfVelZ(Double.parseDouble(br.readLine()));

				theVector.setEcfAccelX(Double.parseDouble(br.readLine()));
				theVector.setEcfAccelY(Double.parseDouble(br.readLine()));
				theVector.setEcfAccelZ(Double.parseDouble(br.readLine()));

				svs.add(theVector);
			}

			stateVectorSet.getStateVectors()
					.addAll(svs);
			returnSvs.add(stateVectorSet);
		}

		return returnSvs;
	}
}
