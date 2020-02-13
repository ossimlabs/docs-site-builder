package com.maxar.geometric.intersection.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.locationtech.jts.geom.Polygon;

import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "areasofinterest")
@Data
@NoArgsConstructor
public class AreaOfInterest implements
		Serializable
{
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "id")
	private String id;

	@Column(name = "geometry", columnDefinition = "geometry(Polygon)")
	private Polygon geometry;
}
