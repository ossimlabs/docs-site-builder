package com.maxar.asset.entity;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import lombok.Data;

@Entity
@Table(name = "assets", indexes = {
	@Index(name = "IDX_fk_assets_name", columnList = "name", unique = true),
	@Index(name = "IDX_fk_assets_type", columnList = "type")
})
@Data
public class Asset implements
		Serializable
{
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "id")
	private String id;

	@Column(name = "name")
	private String name;

	@Column(name = "type")
	@Enumerated
	private AssetType type;

	@Column(name = "xml", columnDefinition = "TEXT")
	private String xml;

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "asset", orphanRemoval = true)
	private final List<AssetExtraData> extraData = Collections.emptyList();

	public void addExtraData(
			final AssetExtraData assetExtraData ) {
		extraData
				.add(
						assetExtraData);
		assetExtraData
				.setAsset(
						this);
	}

	public void removeExtraData(final AssetExtraData assetExtraData) {
		extraData.remove(assetExtraData);
		assetExtraData.setAsset(null);
	}

}
