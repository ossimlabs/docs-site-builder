package com.maxar.asset.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.maxar.asset.entity.AssetExtraData;

@Repository
public interface ExtraDataRepository extends
		JpaRepository<AssetExtraData, String>
{
}
