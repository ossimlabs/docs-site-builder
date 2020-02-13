package com.maxar.common.types;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.maxar.common.utils.SVIFSerializer;
import com.radiantblue.analytics.mechanics.statevectors.StateVectorsInFrame;

import lombok.Data;

@Data
public class VehiclePosition
{
	@JsonSerialize(using = SVIFSerializer.class)
	private StateVectorsInFrame svif;
	private String id;
}
