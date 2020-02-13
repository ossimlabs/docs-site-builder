package com.maxar.manager.dataingest;

import org.springframework.beans.factory.annotation.Value;

public abstract class Ingester implements
		Runnable
{
	@Value("${ingester.feedid:}")
	protected String feedId;

	@Value("${ingester.feedname:}")
	protected String feedName;

	protected boolean enabled = true;

	public void init() {
		internalInit();
	}

	public void start() {
		if (enabled) {
			catchUpIngest();
			doLoopIngest();
		}
	}

	public void stop() {}

	@Override
	public void run() {
		start();
	}

	abstract protected void internalInit();

	// After an outage, ingest accumulated data updates
	abstract protected void catchUpIngest();

	// Perform standard polling/listening for data updates
	abstract protected void doLoopIngest();

	public String getFeedId() {
		return feedId;
	}

	public void setFeedId(
			final String feedId ) {
		this.feedId = feedId;
	}

	public String getFeedName() {
		return feedName;
	}

	public void setFeedName(
			final String feedName ) {
		this.feedName = feedName;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(
			final boolean enabled ) {
		this.enabled = enabled;
	}
}
