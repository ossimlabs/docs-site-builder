package com.maxar.asset.common.client.space;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.maxar.asset.common.exception.AssetExtraDataDoesNotExistException;
import com.maxar.asset.common.exception.AssetIdDoesNotExistException;
import com.maxar.asset.common.exception.AssetNameDoesNotExistException;
import com.maxar.asset.common.service.AssetRetrieverAbstract;
import com.maxar.asset.model.AssetModel;
import com.maxar.asset.model.ExtraData;
import com.maxar.asset.model.NameList;

@Component
public class AssetRetrieverSpace extends
		AssetRetrieverAbstract
{
	@Autowired
	private RestTemplate restTemplate;

	@Value("${microservices.asset.space.getAssetIdByName}")
	private String getAssetIdByNameUrl;

	@Value("${microservices.asset.space.getAssetModelXmlById}")
	private String getAssetModelXmlByIdUrl;

	@Value("${microservices.asset.space.getAllAssetExtraDataNames}")
	private String getAllAssetExtraDataNamesUrl;

	@Value("${microservices.asset.space.getAssetExtraDataByName}")
	private String getAssetExtraDataByNameUrl;

	@Value("${microservices.asset.space.propagatorTypeThresholdDays}")
	private long PROPAGATOR_TYPE_THRESHOLD_DAYS;

	@Override
	public String getAssetById(
			final String id )
			throws AssetIdDoesNotExistException {
		final Map<String, String> params = new HashMap<>();
		params.put(	"id",
					id);
		final ResponseEntity<AssetModel> response = restTemplate.getForEntity(	getAssetModelXmlByIdUrl,
																				AssetModel.class,
																				params);
		if (response.getStatusCode()
				.isError()) {
			throw new AssetIdDoesNotExistException(
					id);
		}
		return response.getBody()
				.getModelXml();
	}

	@Override
	public String getAssetIdByName(
			final String name )
			throws AssetNameDoesNotExistException {
		final Map<String, String> params = new HashMap<>();
		params.put(	"name",
					name);
		final ResponseEntity<String> response = restTemplate.getForEntity(	getAssetIdByNameUrl,
																			String.class,
																			params);
		if (response.getStatusCode()
				.isError()) {
			throw new AssetNameDoesNotExistException(
					name);
		}
		return response.getBody();
	}

	@Override
	public List<String> getAllAssetExtraDataNames(
			final String id )
			throws AssetIdDoesNotExistException {
		final Map<String, String> params = new HashMap<>();
		params.put(	"id",
					id);
		final ResponseEntity<NameList> response = restTemplate.getForEntity(getAllAssetExtraDataNamesUrl,
																			NameList.class,
																			params);
		if (response.getStatusCode()
				.isError()) {
			throw new AssetIdDoesNotExistException(
					id);
		}
		return response.getBody()
				.getNames();
	}

	@Override
	public ExtraData getAssetExtraDataByName(
			final String id,
			final String extraDataName )
			throws AssetExtraDataDoesNotExistException,
			AssetIdDoesNotExistException {
		final Map<String, String> params = new HashMap<>();
		params.put(	"id",
					id);
		params.put(	"extraName",
					extraDataName);
		final ResponseEntity<ExtraData> response = restTemplate.getForEntity(	getAssetExtraDataByNameUrl,
																				ExtraData.class,
																				params);
		if (response.getStatusCode()
				.isError()) {
			throw new AssetIdDoesNotExistException(
					id);
		}
		else if (response.getBody() == null) {
			throw new AssetExtraDataDoesNotExistException(
					id,
					extraDataName);
		}

		return response.getBody();
	}
}
