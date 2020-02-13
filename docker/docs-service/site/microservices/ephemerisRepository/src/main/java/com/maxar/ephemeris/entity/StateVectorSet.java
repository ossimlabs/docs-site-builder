package com.maxar.ephemeris.entity;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import com.maxar.common.utils.DateUtils;
import com.maxar.ephemeris.model.EphemerisModel;
import com.maxar.ephemeris.model.EphemerisType;
import com.maxar.ephemeris.model.StateVectorModel;
import com.maxar.ephemeris.model.StateVectorSetModel;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Table(name = "statevectorset")
@Data
@EqualsAndHashCode(callSuper=true)
public class StateVectorSet extends
		Ephemeris
{
	private static final long serialVersionUID = 1L;
	
	@OneToMany(mappedBy = "stateVectorSet", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
	private Set<StateVector> stateVectors = new HashSet<>();

	public StateVectorSet() {
		type = EphemerisType.STATE_VECTOR_SET;
	}
	
	public StateVectorSet(
			final StateVectorSet svs ) {
		this(
				svs.scn,
				svs.stateVectors);
	}

	public StateVectorSet(
			final int scn,
			final Set<StateVector> stateVectors ) {
		super(
				scn);
		this.stateVectors = stateVectors;
		type = EphemerisType.STATE_VECTOR_SET;
		
		setEpochFromStateVectors(stateVectors);
	}
	
	private void setEpochFromStateVectors(
			final Set<StateVector> vectors ) {
		final Optional<Long> epoch = vectors.stream()
				.map(StateVector::getAtTimeMillis)
				.min(Long::compareTo);

		if (epoch.isPresent()) {
			setEpochMillis(epoch.get());
		}
		else {
			setEpochMillis(DateUtils.START_OF_TIME.getMillis());
		}
	}

	@Override
	public EphemerisModel toModel() {
		Set<StateVectorModel> svm = new HashSet<>();
		
		for(StateVector sv : stateVectors) {
			svm.add(sv.toModel());
		}
		
		return StateVectorSetModel.stateVectorSetBuilder()
				.scn(scn)
				.type(type)
				.epoch(epochMillis)
				.stateVectors(svm)
				.build();
	}
}
