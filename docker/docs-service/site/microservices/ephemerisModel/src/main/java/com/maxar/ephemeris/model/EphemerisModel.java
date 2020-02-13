package com.maxar.ephemeris.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EphemerisModel
{
	private int scn;
	private EphemerisType type;
	private long epochMillis;
}
