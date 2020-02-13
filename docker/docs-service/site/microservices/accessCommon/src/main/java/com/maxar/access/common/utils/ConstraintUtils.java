package com.maxar.access.common.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.maxar.access.common.types.TerrainConstraint;
import com.maxar.access.common.types.WeatherConstraint;
import com.maxar.access.model.AccessConstraint;
import com.radiantblue.analytics.core.constraint.IAccessConstraint;

/**
 * Responsible for manipulating and inspecting the constraints used in accgen
 */
@Component
public class ConstraintUtils
{

	public static final String terrainConstraintName = "Terrain";
	public static final String weatherConstraintName = "Weather";
	public static final String qualityConstraintName = "Quality";
	
	@Autowired
	private ServerConstraints serverConstraints;

	public IAccessConstraint buildConstraint(
			final AccessConstraint requestConstraint ) {

		if (requestConstraint
				.getName()
				.equals(
						terrainConstraintName)) {
			return new TerrainConstraint();
		}
		else if (requestConstraint
				.getName()
				.equals(
						weatherConstraintName)) {
			return new WeatherConstraint(
					requestConstraint.getMaxValue());
		}

		final ConstraintDetail det = serverConstraints
				.getConstraintByName(
						requestConstraint.getName());

		if (det == null) {
			return null;
		}

		return det
				.toConstraint(
						requestConstraint.getMinValue(),
						requestConstraint.getMaxValue());
	}

	public List<String> getAllConstraintNames() {
		final Map<String, IAccessConstraint> allConstraints = serverConstraints.getAllConstraints();
		final List<String> names = new ArrayList<>(
				allConstraints.keySet());

		names
				.add(
						terrainConstraintName);
		names
				.add(
						weatherConstraintName);

		return names;
	}

}
