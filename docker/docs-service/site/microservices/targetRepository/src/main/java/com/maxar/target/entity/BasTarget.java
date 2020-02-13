package com.maxar.target.entity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.joda.time.DateTime;
import org.locationtech.jts.geom.Geometry;

import com.maxar.common.utils.WacUtils;
import com.maxar.target.model.BasTargetModel;
import com.maxar.target.model.BasWtmModel;
import com.maxar.target.model.TargetModel;
import com.maxar.target.model.TargetType;
import com.radiantblue.analytics.geodesy.geometry.GeodeticMultiPolygon;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Table(name = "bastarget")
@Data
@EqualsAndHashCode(callSuper = true)
public class BasTarget extends
		Target
{
	private static final long serialVersionUID = 1L;

	@OneToMany(fetch = FetchType.EAGER, mappedBy = "parentBas", cascade = CascadeType.ALL)
	protected Set<BasWtm> wtms;

	public BasTarget() {
		super(
				TargetType.BAS);
	}

	public BasTarget(
			final BasTarget bas ) {
		super(
				bas);
		wtms = bas.wtms;
	}

	public BasTarget(
			final String targetId,
			final String targetName,
			final Set<BasWtm> wtms ) {
		super(
				TargetType.BAS,
				targetId,
				targetName,
				null, // description
				"ZZ", // country code
				"ZZ", // geo region
				null); // order of battle
		this.wtms = wtms;

		updateGeometry();
	}

	@Override
	public String toString() {
		return String.format(	"BAS: %s %d %s",
								targetId,
								wtms.size(),
								targetName);
	}

	public void updateGeometry() {
		if ((wtms == null) || (wtms.size() == 0)) {
			return;
		}

		// TODO: USE LIST: Sort wtms by id
		final BasWtm[] sorted = wtms.toArray(new BasWtm[wtms.size()]);
		Arrays.sort(sorted);

		final ArrayList<Geometry> polys = new ArrayList<Geometry>();

		// loop over wtms creating geometries for each and splitting them on
		// dateline and then making a multi-polygon from all of them
		for (final BasWtm wtm : sorted) {
			final Geometry geom = WacUtils.getInstance()
					.buildPolyFromId(wtm.mapGridId)
					.splitOnDateLine()
					.jtsGeometry();
			polys.add(geom);
		}

		geometry = GeodeticMultiPolygon.createFromJTSGeoms(polys)
				.jtsGeometry_deg();
	}

	@Override
	public TargetModel toModel(
			final DateTime czmlStartTime,
			final DateTime czmlStopTime ) {
		final Set<BasWtmModel> modelWtms = wtms.stream()
				.map(bw -> bw.getMapGridId())
				.map(BasWtmModel::new)
				.collect(Collectors.toSet());

		return BasTargetModel.basBuilder()
				.targetType(targetType)
				.targetId(targetId)
				.targetName(targetName)
				.description(description)
				.countryCode(countryCode)
				.geoRegion(geoRegion)
				.orderOfBattle(orderOfBattle)
				.geometry(geometry)
				.wtms(modelWtms)
				.czmlStartTime(czmlStartTime)
				.czmlStopTime(czmlStopTime)
				.build();
	}
}
