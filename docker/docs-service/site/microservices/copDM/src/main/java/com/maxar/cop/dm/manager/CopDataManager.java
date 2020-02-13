package com.maxar.cop.dm.manager;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Controller;

import com.maxar.cop.dm.cesium.CesiumSession;
import com.maxar.cop.dm.ingest.ephemeris.EphemerisIngester;
import com.maxar.cop.dm.ingest.planning.AirbornePlanIngester;
import com.maxar.cop.dm.ingest.planning.SpacePlanIngester;
import com.maxar.cop.dm.ingest.track.TrackIngester;
import com.maxar.manager.dataingest.Ingester;
import com.maxar.manager.manager.DataManager;

@Controller
public class CopDataManager extends
		DataManager
{
	@Autowired
	private EphemerisIngester ephemerisIngester;

	@Autowired
	private TrackIngester trackIngester;

	@Autowired
	private SpacePlanIngester spacePlanIngester;

	@Autowired
	private AirbornePlanIngester airbornePlanIngester;

	@Autowired
	private CesiumSession cesiumSession;

	private List<Ingester> ingesters;

	@Override
	protected String getManagerName() {
		return "CopDataManager";
	}

	@Override
	protected void startIngest() {
		for (final Ingester ingester : ingesters) {
			startIngester(ingester);
		}
	}

	@Override
	protected void afterInit() {
		// Init ingesters
		ingesters = List.of(ephemerisIngester,
							trackIngester,
							spacePlanIngester,
							airbornePlanIngester);
		ingesters.forEach(Ingester::init);

		cesiumSession.submitTreeNodes();
	}

	@Override
	protected void initManager() {
		// There is nothing to do here.
	}

	@EventListener
	public void launch(
			final ApplicationReadyEvent event ) {
		init();
		start();
	}
}
