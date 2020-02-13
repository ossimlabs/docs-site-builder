package com.maxar.terrain.utils;

import com.radiantblue.analytics.core.measures.Angle;
import com.radiantblue.analytics.core.measures.Length;
import com.radiantblue.analytics.geodesy.GeodeticPoint;

import lombok.Getter;
import lombok.Setter;

/**
 * The TerrainMaskNode class
 */
@Getter
@Setter
public class TerrainMaskNode
{
	/** The center point of this node */
	private GeodeticPoint center;

	/** The azimuth this node is along */
	private Angle azimuth;

	/** The ground distance from the center this node is on */
	private Length groundDistance;

	/** The minimum graze angle at this node */
	private Angle minGraze;

	/**
	 * Instantiates a new terrain mask node.
	 *
	 * @param center
	 *            the center
	 * @param azimuth
	 *            the azimuth
	 * @param groundDistance
	 *            the ground distance
	 * @param minGraze
	 *            the min graze
	 */
	public TerrainMaskNode(
			final GeodeticPoint center,
			final Angle azimuth,
			final Length groundDistance,
			final Angle minGraze ) {
		this.center = center;
		this.azimuth = azimuth;
		this.groundDistance = groundDistance;
		this.minGraze = minGraze;
	}

	/**
	 * Simple representation of this node as a string
	 */
	@Override
	public String toString() {
		return "TerrainMaskNode: " + center + "/" + azimuth + "/" + groundDistance + "/" + minGraze;
	}
}
