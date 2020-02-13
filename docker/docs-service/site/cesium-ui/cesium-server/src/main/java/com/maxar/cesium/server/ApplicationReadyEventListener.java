package com.maxar.cesium.server;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.maxar.cesium.csvtoczml.CsvToCzmlWriter;
import com.maxar.cesium.server.controller.CzmlRestController;

@Component
public class ApplicationReadyEventListener
{
	@Value("${data.location:}")
	private String dataLocation;

	@Autowired
	private CzmlRestController czmlRestController;

	@EventListener(ApplicationReadyEvent.class)
	public void afterStartup() {

		if ((dataLocation != null) && !dataLocation.isEmpty()) {
			final List<JsonNode> array = CsvToCzmlWriter.writeCsvFolderToCzml(dataLocation);
			czmlRestController.postPackets(	CzmlRestController.DEFAULT_SESSION,
			                               	null,
			                               	false,
			                               	true,
											array);
			System.out.println("Data loaded from: " + dataLocation);
		}
	}

}
