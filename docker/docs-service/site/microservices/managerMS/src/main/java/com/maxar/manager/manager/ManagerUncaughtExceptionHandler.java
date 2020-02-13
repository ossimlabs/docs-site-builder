package com.maxar.manager.manager;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.radiantblue.analytics.core.log.SourceLogger;

public class ManagerUncaughtExceptionHandler implements
		UncaughtExceptionHandler
{
	private static Logger logger = SourceLogger
			.getLogger(
					new Object() {}.getClass().getEnclosingClass().getName());

	private final Map<ThreadGroup, Logger> threadMap;

	public ManagerUncaughtExceptionHandler() {
		threadMap = new HashMap<>();
	}

	@Override
	public void uncaughtException(
			final Thread t,
			final Throwable e ) {
		final Logger loggerFromGroup = getLoggerFromGroup(
				t.getThreadGroup());
		if (loggerFromGroup == null) {
			logger
					.error(
							"Uncaught Exception in thread " + t.getId(),
							e);
		}
		else {
			loggerFromGroup
					.error(
							"Uncaught Exception in thread " + t.getId(),
							e);
		}
	}

	public Logger getLoggerFromGroup(
			final ThreadGroup group ) {
		final Logger logger = threadMap
				.get(
						group);
		if (logger != null) {
			return logger;
		}
		else if (group.getParent() != null) {
			return getLoggerFromGroup(
					group.getParent());
		}

		return null;
	}

	public void addHandler(
			final ThreadGroup group,
			final Logger logger ) {
		threadMap
				.put(
						group,
						logger);

	}

}
