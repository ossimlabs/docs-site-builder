package com.maxar.ephemeris.model;

import java.util.Iterator;
import java.util.Optional;
import java.util.Set;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.Duration;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.maxar.common.utils.DateUtils;
import com.radiantblue.analytics.core.Vector3D;
import com.radiantblue.analytics.core.log.SourceLogger;
import com.radiantblue.analytics.geodesy.EarthCenteredFrame;
import com.radiantblue.analytics.mechanics.statevectors.StateVectorsInFrame;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=true)
public class StateVectorSetModel extends
		EphemerisModel
{
	private static Logger logger = SourceLogger.getLogger(new Object() {}.getClass()
			.getEnclosingClass()
			.getName());

	private Set<StateVectorModel> stateVectors;

	public StateVectorSetModel() {
		setType(EphemerisType.STATE_VECTOR_SET);
	}

	@Builder(builderMethodName = "stateVectorSetBuilder")
	public StateVectorSetModel(
			final int scn,
			final EphemerisType type,
			final long epoch,
			final Set<StateVectorModel> stateVectors ) {
		super(
				scn,
				type,
				epoch);
		this.stateVectors = stateVectors;

		setEpochFromStateVectors(stateVectors);
	}

	public StateVectorSetModel(
			final StateVectorSetModel s ) {
		this(
				s.getScn(),
				s.getType(),
				s.getEpochMillis(),
				s.stateVectors);
		setType(EphemerisType.STATE_VECTOR_SET);
		setStateVectors(stateVectors);
	}

	public Set<StateVectorModel> getStateVectors() {
		return stateVectors;
	}

	public void setStateVectors(
			final Set<StateVectorModel> stateVectors ) {
		this.stateVectors = stateVectors;
		setEpochFromStateVectors(stateVectors);
	}

	private void setEpochFromStateVectors(
			final Set<StateVectorModel> vectors ) {
		final Optional<Long> epoch = vectors.stream()
				.map(StateVectorModel::getAtTimeMillis)
				.min(Long::compareTo);

		if (epoch.isPresent()) {
			setEpochMillis(epoch.get());
		}
		else {
			setEpochMillis(DateUtils.START_OF_TIME.getMillis());
		}
	}

	@Override
	public String toString() {
		return String.format(	"%s has %d state vectors",
								super.toString(),
								stateVectors.size());
	}

	@JsonIgnore
	public DateTime[] getAtTimes() {
		final DateTime[] dts = new DateTime[stateVectors.size()];
		int i = 0;
		for (final StateVectorModel vec : stateVectors) {
			dts[i] = new DateTime(
					vec.getAtTimeMillis());
			i++;
		}
		return dts;
	}

	@JsonIgnore
	public Vector3D[] getEcfPos() {
		final Vector3D[] vecs = new Vector3D[stateVectors.size()];
		int i = 0;
		for (final StateVectorModel vec : stateVectors) {
			vecs[i] = vec.getEcfPos()
					.toRBAnalyticsVector3D();
			i++;
		}
		return vecs;
	}

	@JsonIgnore
	public Vector3D[] getEcfVel() {
		final Vector3D[] vecs = new Vector3D[stateVectors.size()];
		int i = 0;
		for (final StateVectorModel vec : stateVectors) {
			vecs[i] = vec.getEcfPos()
					.toRBAnalyticsVector3D();
			i++;
		}
		return vecs;
	}

	@JsonIgnore
	public Vector3D[] getEcfAcc() {
		final Vector3D[] vecs = new Vector3D[stateVectors.size()];
		int i = 0;
		for (final StateVectorModel vec : stateVectors) {
			vecs[i] = vec.getEcfPos()
					.toRBAnalyticsVector3D();
			i++;
		}
		return vecs;
	}

	/**
	 * Convert the set of {@link StateVectorModel}s into
	 * {@link StateVectorsInFrame}. These are used to update the asset.
	 *
	 * @return the {@link StateVectorsInFrame} array
	 */
	@JsonIgnore
	public StateVectorsInFrame[] getStateVectorsInFrame() {
		final StateVectorsInFrame[] returnSvif = new StateVectorsInFrame[stateVectors.size()];
		int i = 0;
		for (final StateVectorModel vec : stateVectors) {
			returnSvif[i] = new StateVectorsInFrame(
					new DateTime(
							vec.getAtTimeMillis()),
					vec.getEcfPos()
							.toRBAnalyticsVector3D(),
					vec.getEcfVel()
							.toRBAnalyticsVector3D(),
					EarthCenteredFrame.ECEF);
			i++;
		}
		return returnSvif;
	}

	@JsonIgnore
	public Duration getDuration() {
		final Optional<Long> first = stateVectors.stream()
				.map(StateVectorModel::getAtTimeMillis)
				.min(Long::compareTo);

		final Optional<Long> last = stateVectors.stream()
				.map(StateVectorModel::getAtTimeMillis)
				.max(Long::compareTo);

		if (first.isPresent() && last.isPresent()) {
			final Duration dur = new Duration(
					first.get(),
					last.get());

			return dur;
		}

		return null;
	}

	/**
	 * gets the time step between the first two statevectors note that there is no
	 * additional error checking to make sure that all of the statevectors in this
	 * set are actually using the same sample size.
	 *
	 * Use this method when initializing an InterpolatedTableStateVectorProvider
	 *
	 * @return the time step
	 */
	@JsonIgnore
	public Duration getStep() {
		final Iterator<StateVectorModel> iterator = stateVectors.iterator();

		if (!iterator.hasNext()) {
			logger.fatal("Empty StateVectorSet encountered while trying to determine step size");
		}
		final StateVectorModel first = iterator.next();

		if (!iterator.hasNext()) {
			logger.fatal("At least two StateVectors are required to determine the step size");
		}
		final StateVectorModel second = iterator.next();

		final Duration step = Duration.millis(second.getAtTimeMillis() - first.getAtTimeMillis());

		return step;

	}
}