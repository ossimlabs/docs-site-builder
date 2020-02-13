package com.maxar.manager.manager;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.maxar.manager.dataingest.Ingester;
import com.radiantblue.analytics.core.log.SourceLogger;

public abstract class DataManager extends
		Manager
{
	private static Logger logger = SourceLogger.getLogger(new Object() {}.getClass()
			.getEnclosingClass()
			.getName());

	private final List<Thread> threads = new ArrayList<>();

	protected abstract void startIngest();

	@Override
	protected void beforeInit() {}

	protected void cleanupDataManager() {
		logger.info("Attempting to kill threads..." + threads.size());
		for (final Thread t : threads) {
			logger.info("Killing thread: " + t.getName());
			t.interrupt();
		}
	}

	protected void start() {
		startIngest();
	}

	protected void startIngester(
			final Ingester ingester ) {
		threads.add(new Thread(
				ingester));
		threads.get(threads.size() - 1)
				.start();
	}
}
