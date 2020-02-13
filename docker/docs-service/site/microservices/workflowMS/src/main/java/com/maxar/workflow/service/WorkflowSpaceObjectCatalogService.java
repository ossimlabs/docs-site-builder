package com.maxar.workflow.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.maxar.spaceobjectcatalog.model.SpaceObject;
import com.maxar.workflow.model.EphemeridesRequest;

@Component
public class WorkflowSpaceObjectCatalogService
{
	@Autowired
	private ApiService apiService;

	/**
	 * Get a list of the SCN values for the given list of space asset names
	 *
	 * @param ephemeridesRequest
	 *            Request for the ephemerides for a list of assets, contains list of
	 *            asset names, count, and page
	 * @return List of SCN values matching the requested space assets
	 */
	public List<Integer> getScnForAssetsByName(
			final EphemeridesRequest ephemeridesRequest ) {

		return ephemeridesRequest.getAssetNames()
				.stream()
				.map(s -> apiService.getSpaceAssetIdByName(s))
				.collect(Collectors.toList());

	}

	/**
	 * Get the ephemerides for each space asset by SCN
	 *
	 * @param ephemeridesRequest
	 *            Request for the ephemerides for a list of assets, contains list of
	 *            asset names, count, and page
	 * @return The list of ephemerides for requested space asset SCNs
	 */
	public List<SpaceObject> getEphemeridesByAssetName(
			final EphemeridesRequest ephemeridesRequest ) {

		final Integer page = ephemeridesRequest.getPage();
		final Integer count = ephemeridesRequest.getCount();

		return getScnForAssetsByName(ephemeridesRequest).stream()
				.map(s -> apiService.getSpaceAssetEphermeridesByScn(s,
																	page,
																	count))
				.collect(Collectors.toList());
	}
}