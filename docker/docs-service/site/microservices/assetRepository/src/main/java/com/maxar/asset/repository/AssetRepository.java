package com.maxar.asset.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.maxar.asset.entity.Asset;
import com.maxar.asset.entity.AssetType;

@Repository
public interface AssetRepository extends
		JpaRepository<Asset, String>
{
	List<Asset> getByType(
			@Param("type")
			final AssetType type);

	Optional<Asset> getByName(
			@Param("name")
			final String name);

	Optional<Asset> getByNameAndType(
			@Param("name")
			final String name,
			@Param("type")
			final AssetType type);

	Optional<Asset> getByIdAndType(
			@Param("id")
			final String id,
			@Param("type")
			final AssetType type);
}
