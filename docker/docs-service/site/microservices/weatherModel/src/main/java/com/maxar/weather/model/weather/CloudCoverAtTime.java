package com.maxar.weather.model.weather;

public class CloudCoverAtTime
{
	String iso8601Date;
	Double cloudCoverPercent;
	
	public CloudCoverAtTime() {};
	
	public CloudCoverAtTime(String iso8601Date, Double cloudCoverPercent) {
		this.iso8601Date = iso8601Date;
		this.cloudCoverPercent = cloudCoverPercent;
	}

	public String getIso8601Date() {
		return iso8601Date;
	}

	public void setIso8601Date(
			String iso8601Date ) {
		this.iso8601Date = iso8601Date;
	}

	public Double getCloudCoverPercent() {
		return cloudCoverPercent;
	}

	public void setCloudCoverPercent(
			Double cloudCoverPercent ) {
		this.cloudCoverPercent = cloudCoverPercent;
	}
}
