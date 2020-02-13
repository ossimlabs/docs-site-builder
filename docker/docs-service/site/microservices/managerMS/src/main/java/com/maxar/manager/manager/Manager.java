package com.maxar.manager.manager;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.joda.time.DateTimeZone;
import org.joda.time.Duration;
import org.springframework.beans.factory.annotation.Value;

import com.radiantblue.analytics.core.log.SourceLogger;

public abstract class Manager
{
	private static Logger logger = SourceLogger.getLogger(new Object() {}.getClass()
			.getEnclosingClass()
			.getName());

	// Cleanup-related variables set from properties
	@Value("${manager.cleanupEnabled:false}")
	protected boolean cleanupEnabled;

	protected Duration cleanupSleepTime;

	protected Duration cleanupDelayTime;

	protected Timer cleanupTimer;

	private Duration startupDelay;

	protected abstract void beforeInit();

	protected abstract void initManager();

	protected abstract void afterInit();

	protected abstract String getManagerName();

	protected void runCleanup() {}

	protected final void init() {
		DateTimeZone.setDefault(DateTimeZone.UTC);
		BasicConfigurator.configure();

		UncaughtExceptionHandler handler = Thread.getDefaultUncaughtExceptionHandler();
		if (!(handler instanceof ManagerUncaughtExceptionHandler)) {
			handler = new ManagerUncaughtExceptionHandler();
		}
		((ManagerUncaughtExceptionHandler) handler).addHandler(	Thread.currentThread()
				.getThreadGroup()
				.getParent(),
																logger);
		Thread.setDefaultUncaughtExceptionHandler(handler);

		beforeInit();

		// startup delay
		logger.info(getManagerName() + " starting with delay: " + startupDelay.getStandardSeconds());

		try {
			Thread.sleep(startupDelay.getMillis());
		}
		catch (final InterruptedException e) {
			logger.error(	"Thread was interrupted: " + e.getLocalizedMessage(),
							e);
		}

		initManager();
		afterInit();

		if (cleanupEnabled) {
			initCleanupThread();
		}
	}

	private void initCleanupThread() {
		if ((cleanupDelayTime != null) && (cleanupSleepTime != null)) {
			cleanupTimer = new Timer(
					getManagerName() + " Cleanup",
					true);
			cleanupTimer.scheduleAtFixedRate(	new TimerTask() {

				@Override
				public void run() {
					runCleanup();
				}

			},
												cleanupDelayTime.getMillis(),
												cleanupSleepTime.getMillis());
		}
		else {
			logger.error("Cleanup thread not initialized. Properties not set for "
					+ "'cleanupDelaySeconds' and/or 'cleanupSleepSeconds'");
		}
	}

	protected void cleanupManager() {
		if (cleanupTimer != null) {
			logger.info("Attempting to kill cleanup thread...");
			cleanupTimer.cancel();
		}
	}

	@Value("${manager.cleanupSleepSeconds:0}")
	public void setCleanupSleepTime(
			final String cleanupSleepSeconds ) {
		this.cleanupSleepTime = stringSecondsToDuration(cleanupSleepSeconds);
	}

	@Value("${manager.cleanupDelaySeconds:0}")
	public void setCleanupDelayTime(
			final String cleanupDelaySeconds ) {
		this.cleanupDelayTime = stringSecondsToDuration(cleanupDelaySeconds);
	}

	@Value("${manager.startupDelaySeconds:0}")
	public void setStartupDelay(
			final String startupDelaySeconds ) {
		this.startupDelay = stringSecondsToDuration(startupDelaySeconds);
	}

	private static Duration stringSecondsToDuration(
			final String s ) {
		return Optional.ofNullable(s)
				.map(String::trim)
				.map(Integer::parseInt)
				.map(Duration::standardSeconds)
				.orElse(Duration.ZERO);
	}
}
