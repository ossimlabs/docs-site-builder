package com.maxar.cop.dm.ingest;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.fasterxml.jackson.databind.JsonNode;
import com.maxar.cesium.czmlwriter.Packet;
import com.maxar.cesium.czmlwriter.types.TimeTaggedValue;
import com.maxar.cop.dm.cesium.CesiumSession;
import com.maxar.cop.dm.service.CopServiceClient;
import com.maxar.manager.dataingest.Ingester;
import com.radiantblue.analytics.core.log.SourceLogger;

public abstract class CopIngester extends
		Ingester
{

	protected static Logger logger = SourceLogger.getLogger(CopIngester.class.getName());

	@Value("${microservices.copdm.updateintervalseconds}")
	private int updateintervalseconds;

	private DateTime previouslyUpdated;

	@Autowired
	protected CopServiceClient serviceClient;

	@Autowired
	protected CesiumSession cesiumSession;

	@Value("#{T(org.joda.time.DateTime).parse('${microservices.copdm.demodatetime}')}")
	private DateTime demodatetime;

	@Value("${microservices.copdm.dataLifeSpanSeconds}")
	protected int dataLifeSpanSeconds;

	protected static final String VEHICLE_NODE = "Vehicle Positions";
	protected static final String PLAN_NODE = "Planned Collections";

	protected final List<TimeTaggedValue<String>> deletablePacketIds = new ArrayList<>();

	@Override
	protected void doLoopIngest() {

		// Start timer task
		final TimerTask repeatedTask = new TimerTask() {
			@Override
			public void run() {
				// Grab updates and post them to the COP
				updateCopData();
			}
		};
		final Timer timer = new Timer(
				"Timer");

		final long delay = 0L;
		final long period = updateintervalseconds * 1000l;
		timer.scheduleAtFixedRate(	repeatedTask,
									delay,
									period);
	}

	private void updateCopData() {
		DateTime now;
		if (previouslyUpdated == null) {
			if (demodatetime != null) {
				now = demodatetime;
			}
			else {
				now = DateTime.now();
			}
		}
		else {
			now = previouslyUpdated.plusSeconds(updateintervalseconds);
		}
		final List<String> assetIds = getAssetIds();

		deleteStaleData(assetIds,
						now);

		for (final String assetId : assetIds) {
			// gather data covering until the next scheduled update
			updateCzmlData(	assetId,
							now,
							now.plusSeconds(updateintervalseconds));
		}

		previouslyUpdated = now;
	}

	private void deleteStaleData(
			final List<String> assetIds,
			final DateTime now ) {

		final Interval deleteInterval = new Interval(
				now.minusSeconds(dataLifeSpanSeconds * 3),
				now.minusSeconds(dataLifeSpanSeconds));

		final List<JsonNode> deletePackets = new ArrayList<>();

		final Iterator<TimeTaggedValue<String>> i = deletablePacketIds.iterator();
		while (i.hasNext()) {
			final TimeTaggedValue<String> timeVal = i.next();
			if (timeVal.getTime()
					.isBefore(deleteInterval.getEnd())) {
				final Packet deletePacket = Packet.create()
						.id(timeVal.getValue())
						.delete(true);
				deletePackets.add(deletePacket.toJsonNode());
				i.remove();
			}
		}

		serviceClient.postPackets(	deletePackets,
									null);

		cleanupStaleDataWithinPackets(	assetIds,
										deleteInterval);
	}

	public abstract void cleanupStaleDataWithinPackets(
			final List<String> assetIds,
			final Interval deleteInterval );

	protected abstract List<String> getAssetIds();

	protected abstract void updateCzmlData(
			final String assetId,
			final DateTime start,
			final DateTime stop );

	@Override
	protected void internalInit() {
		createInitialTreeNodes();
	}

	protected abstract void createInitialTreeNodes();

	@Override
	protected void catchUpIngest() {}

}
