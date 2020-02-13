package com.maxar.opgen.model;

import org.joda.time.DateTime;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class OpBeam
{
	DateTime startTime;
	DateTime endTime;
	String geometryWkt;
}
