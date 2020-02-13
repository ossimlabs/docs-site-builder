package com.maxar.target.model;

import lombok.Data;

@Data
public class BasWtmModel
{
	protected String mapGridId;
	
	public BasWtmModel(String mapGridId) {
		this.mapGridId = mapGridId;
	}
}
