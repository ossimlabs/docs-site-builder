package com.maxar.planning.entity.image;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.maxar.planning.model.image.ImageFrameModel;
import com.maxar.planning.model.image.ImageOpModel;
import com.maxar.planning.entity.link.Link;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
@Table(name = "imageop", indexes = {
	@Index(name = "IDX_fk_collectionwindowkey", columnList = "collectionwindowkey"),
	@Index(name = "IDX_fk_linkkey", columnList = "linkkey")
})
public class ImageOp implements
		Comparable<ImageOp>,
		Serializable
{
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(generator = "uuidGenerator")
	@GenericGenerator(name = "uuidGenerator", strategy = "uuid2")
	@Column(name = "imageopkey", columnDefinition = "UUID")
	@JsonIgnore
	protected UUID imageOpKey;

	@Column(name = "opid")
	private String opId;

	@Column(name = "opstarttimemillis")
	private Long opStartTimeMillis;

	@Column(name = "opendtimemillis")
	private Long opEndTimeMillis;

	@Column(name = "targettype")
	private String targetType;

	@Column(name = "countrycode")
	private String countryCode;

	@Column(name = "sensortype")
	private String sensorType;

	@Column(name = "sensormode")
	private String sensorMode;

	@Column(name = "niirs")
	private Double niirs;

	@Column(name = "elevation")
	private Integer elevation;

	@Column(name = "azimuth")
	private Double azimuth;

	@Column(name = "gsdoriprmeters")
	private Double gsdOrIprMeters;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "collectionwindowkey", referencedColumnName = "collectionwindowkey")
	@OnDelete(action = OnDeleteAction.CASCADE)
	@JsonIgnore
	@EqualsAndHashCode.Exclude
	private CollectionWindow collectionWindow;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "linkkey", referencedColumnName = "linkkey")
	@OnDelete(action = OnDeleteAction.CASCADE)
	private Link link;

	@OneToMany(mappedBy = "imageOp", cascade = CascadeType.ALL, orphanRemoval = true)
	private Set<ImageFrame> imageFrames = new HashSet<>();

	public ImageOp(
			String opId,
			Long opStartTimeMillis,
			Long opEndTimeMillis,
			String targetType,
			String countryCode,
			String sensorType,
			String sensorMode,
			Double niirs,
			Integer elevation,
			Double azimuth,
			Double gsdOrIprMeters,
			CollectionWindow collectionWindow,
			Link link ) {
		super();
		this.opId = opId;
		this.opStartTimeMillis = opStartTimeMillis;
		this.opEndTimeMillis = opEndTimeMillis;
		this.targetType = targetType;
		this.countryCode = countryCode;
		this.sensorType = sensorType;
		this.sensorMode = sensorMode;
		this.niirs = niirs;
		this.elevation = elevation;
		this.azimuth = azimuth;
		this.gsdOrIprMeters = gsdOrIprMeters;
		this.collectionWindow = collectionWindow;
		this.link = link;
	}

	public ImageOpModel toModel() {
		Set<ImageFrameModel> imageFrameModels = imageFrames.stream()
				.map(ImageFrame::toModel)
				.collect(Collectors.toSet());

		return ImageOpModel.builder()
				.opId(opId)
				.opStartTimeMillis(opStartTimeMillis)
				.opEndTimeMillis(opEndTimeMillis)
				.targetType(targetType)
				.countryCode(countryCode)
				.sensorType(sensorType)
				.sensorMode(sensorMode)
				.niirs(niirs)
				.elevation(elevation)
				.azimuth(azimuth)
				.gsdOrIprMeters(gsdOrIprMeters)
				.link(link.toModel())
				.imageFrames(imageFrameModels)
				.build();

	}

	@Override
	public int compareTo(
			ImageOp o ) {
		return (int) (getOpStartTimeMillis() - o.getOpStartTimeMillis());
	}
}
