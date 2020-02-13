package com.maxar.asset.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import lombok.Data;

@Entity
@Table(name = "assetsextradata")
@Data
public class AssetExtraData implements
		Serializable
{
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "name")
	private String name;

	@Column(name = "type")
	private String type;

	@Lob
	@Column(name = "rawdata")
	private byte[] rawData;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "assetid")
	private Asset asset;
}
