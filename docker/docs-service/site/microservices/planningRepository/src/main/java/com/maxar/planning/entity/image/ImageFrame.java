package com.maxar.planning.entity.image;

import java.io.Serializable;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.locationtech.jts.geom.Geometry;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.maxar.common.utils.GeometryDeserializer;
import com.maxar.common.utils.GeometrySerializer;
import com.maxar.planning.model.image.ImageFrameModel;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
@Table(name = "imageframe")
public class ImageFrame implements
		Serializable
{
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(generator = "uuidGenerator")
	@GenericGenerator(name = "uuidGenerator", strategy = "uuid2")
	@Column(name = "imageframekey", columnDefinition = "UUID")
	@JsonIgnore
	protected UUID imageFrameKey;

	@Column(name = "imageid")
	private String imageId;

	@Column(name = "niirs")
	private double niirs;
	
	@Column(name = "gsdmeters")
	private double gsdMeters;
	
	@Column(name = "starttimemillis")
	private long startTimeMillis;
	
	@Column(name = "stoptimemillis")
	private long stopTimeMillis;

	@JsonSerialize(using = GeometrySerializer.class)
	@JsonDeserialize(using = GeometryDeserializer.class)
	@Column(name = "polygon")
	private Geometry polygon;
	
	@JsonSerialize(using = GeometrySerializer.class)
	@JsonDeserialize(using = GeometryDeserializer.class)
	@Column(name = "centercoord")
	private Geometry centerCoord;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "imageopkey", referencedColumnName = "imageopkey")
	@OnDelete(action = OnDeleteAction.CASCADE)
	@JsonIgnore
	@EqualsAndHashCode.Exclude
	private ImageOp imageOp;

	public ImageFrame(
			String imageId,
			double niirs,
			double gsdMeters,
			long startTimeMillis,
			long stopTimeMillis,
			Geometry polygon,
			Geometry centerCoord,
			ImageOp imageOp ) {
		super();
		this.imageId = imageId;
		this.niirs = niirs;
		this.gsdMeters = gsdMeters;
		this.startTimeMillis = startTimeMillis;
		this.stopTimeMillis = stopTimeMillis;
		this.polygon = polygon;
		this.centerCoord = centerCoord;
		this.imageOp = imageOp;
	}
	
	public ImageFrameModel toModel() {
		return ImageFrameModel.builder()
				.imageId(imageId)
				.niirs(niirs)
				.gsdMeters(gsdMeters)
				.startTimeMillis(startTimeMillis)
				.stopTimeMillis(stopTimeMillis)
				.polygon(polygon)
				.centerCoord(centerCoord)
				.build();
	}
}
