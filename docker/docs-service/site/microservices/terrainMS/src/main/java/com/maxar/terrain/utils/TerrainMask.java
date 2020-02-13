package com.maxar.terrain.utils;

import java.util.List;

import com.radiantblue.analytics.core.measures.Angle;
import com.radiantblue.analytics.core.measures.Length;

import lombok.Getter;
import lombok.Setter;

/**
 * The TerrainMask class.
 */
public class TerrainMask
{
	/** The nodes. */
	@Getter
	@Setter
	private List<List<TerrainMaskNode>> nodes;

	/** The location hash for this target. */
	@Getter
	@Setter
	private String locationHash;

	/** The id. */
	@Getter
	@Setter
	private int id;

	/**
	 * Instantiates a new terrain mask.
	 *
	 * @param nodes
	 *            the nodes
	 */
	public TerrainMask(
			final List<List<TerrainMaskNode>> nodes ) {
		this.nodes = nodes;
	}

	/**
	 * Gets the mask at azimuth.
	 *
	 * @param az
	 *            the az
	 * @return the mask at azimuth
	 */
	private List<TerrainMaskNode> getMaskAtAzimuth(
			final int az ) {
		return nodes.get(az);
	}

	/**
	 * Find min graze.
	 *
	 * @param az
	 *            the az
	 * @param groundDistance
	 *            the ground distance
	 * @return the angle
	 */
	public Angle findMinGraze(
			final Angle az,
			final Length groundDistance ) {
		// find minGraze for azimuth and ground distance
		final List<TerrainMaskNode> maskAtAzimuth = getMaskAtAzimuth((int) az.degrees());

		Angle minGraze = Angle.fromDegrees(-90.0);

		// need to traverse list backwards (farther ground distance first)
		int i = maskAtAzimuth.size() - 1;

		while (i >= 0) {
			final TerrainMaskNode tmn = maskAtAzimuth.get(i--);

			if (groundDistance.meters() > tmn.getGroundDistance()
					.meters()) {
				minGraze = tmn.getMinGraze();
				break;
			}
		}
		return minGraze;
	}
}
