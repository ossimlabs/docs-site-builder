package com.maxar.planning.entity.image;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.maxar.planning.model.image.CollectionWindowModel;
import com.maxar.planning.model.image.ImageOpModel;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
@Table(name = "collectionwindow", indexes = {
	@Index(name = "IDX_cwid", columnList = "cwid"),
	@Index(name = "IDX_assetname", columnList = "assetname"),
	@Index(name = "IDX_assetscn", columnList = "assetscn")
})
public class CollectionWindow implements
		Serializable
{
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(generator = "uuidGenerator")
	@GenericGenerator(name = "uuidGenerator", strategy = "uuid2")
	@Column(name = "collectionwindowkey", columnDefinition = "UUID")
	@JsonIgnore
	protected UUID collectionWindowKey;

	@Column(name = "cwid")
	private String cwId;

	@Column(name = "status")
	private String status;

	@Column(name = "assetname")
	private String assetName;

	@Column(name = "assetscn")
	private Integer assetScn;

	@Column(name = "startmillis")
	private Long startMillis;

	@Column(name = "endmillis")
	private Long endMillis;

	@OneToMany(mappedBy = "collectionWindow", cascade = CascadeType.ALL, orphanRemoval = true)
	private Set<ImageOp> imageOps = new HashSet<>();

	public CollectionWindow(
			String cwId,
			String status,
			String assetName,
			Integer assetScn,
			Long startMillis,
			Long endMillis ) {
		super();
		this.cwId = cwId;
		this.status = status;
		this.assetName = assetName;
		this.assetScn = assetScn;
		this.startMillis = startMillis;
		this.endMillis = endMillis;
	}
	
	public CollectionWindowModel toModel() {
		Set<ImageOpModel> imageOpModels = imageOps
				.stream()
				.map(ImageOp::toModel)
				.collect(Collectors.toSet());
		
		return CollectionWindowModel.builder()
				.cwId(cwId)
				.status(status)
				.assetName(assetName)
				.startMillis(startMillis)
				.endMillis(endMillis)
				.imageOps(imageOpModels)
				.build();
	}
}
