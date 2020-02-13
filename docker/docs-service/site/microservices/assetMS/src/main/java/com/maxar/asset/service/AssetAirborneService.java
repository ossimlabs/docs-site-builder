package com.maxar.asset.service;

import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import com.maxar.asset.common.exception.MissionIdDoesNotExistException;
import com.maxar.asset.common.exception.NoMissionsFoundException;
import com.maxar.asset.common.service.ApiService;
import com.maxar.asset.model.AssetSmear;
import com.maxar.asset.model.AssetSmearWithBeamsRequest;
import com.maxar.asset.model.FieldOfRegard;
import com.maxar.asset.entity.AssetType;
import com.maxar.common.exception.BadRequestException;
import com.radiantblue.analytics.isr.core.model.asset.Aircraft;
import com.radiantblue.analytics.isr.core.model.asset.Asset;

/**
 * Responsible for doing the work for the Asset service's airborne asset
 * endpoints.
 */
@Component
public class AssetAirborneService extends
		AssetService
{
	@Autowired
	private ApiService apiService;

	@Override
	protected List<FieldOfRegard> getAssetFieldsOfRegard(
			final Asset asset,
			final DateTime atTime,
			@Nullable
			final String propagatorType ) {
		updateAssetMission(	asset,
							atTime);

		return super.getAssetFieldsOfRegard(asset,
											atTime,
											propagatorType);
	}

	@Override
	protected List<AssetSmear> getAssetSmears(
			final Asset asset,
			final AssetSmearWithBeamsRequest assetSmearRequest,
			@Nullable
			final String propagatorType ) {
		updateAssetMission(	asset,
							ISODateTimeFormat.dateTimeParser()
									.parseDateTime(assetSmearRequest.getStartTimeISO8601()));

		return super.getAssetSmears(asset,
									assetSmearRequest,
									propagatorType);
	}

	private void updateAssetMission(
			final Asset asset,
			final DateTime atTime ) {
		try {
			apiService.updateAssetMission(	asset,
											atTime,
											null);
		}
		catch (NoMissionsFoundException | MissionIdDoesNotExistException e) {
			throw new BadRequestException(
					e.getMessage());
		}

	}

	@Override
	protected AssetType getAssetType() {
		return AssetType.AIRBORNE;
	}

	@Override
	protected Asset xmlToAsset(
			final String xml ) {
		return beanFactoryFromXml(xml).getBean(Aircraft.class);
	}
}
