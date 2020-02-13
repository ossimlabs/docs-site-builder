package com.maxar.asset.common.service;

import java.util.List;

import com.maxar.asset.common.exception.AssetExtraDataDoesNotExistException;
import com.maxar.asset.common.exception.AssetIdDoesNotExistException;
import com.maxar.asset.common.exception.AssetNameDoesNotExistException;
import com.maxar.asset.model.ExtraData;

public interface AssetRetriever
{
	public String getAssetById(
			final String id ) throws AssetIdDoesNotExistException;

	public String getAssetIdByName(
			final String name ) throws AssetNameDoesNotExistException;

	public List<String> getAllAssetExtraDataNames(
			final String id ) throws AssetIdDoesNotExistException;

	public ExtraData getAssetExtraDataByName(
			final String id,
			final String extraDataName )
			throws AssetExtraDataDoesNotExistException,
			AssetIdDoesNotExistException;
}
