package com.maxar.asset.common.customasset.sensormode;

import java.util.Arrays;
import java.util.List;

import org.joda.time.DateTime;

import com.maxar.asset.common.customasset.dwell.DwellCalculator;
import com.maxar.asset.common.customasset.sensormode.qualitycalculator.InlineQualityCalculator;
import com.radiantblue.analytics.aerospace.geometry.PointToPointGeometry;
import com.radiantblue.analytics.core.Context;
import com.radiantblue.analytics.core.measures.Angle;
import com.radiantblue.analytics.core.measures.Length;
import com.radiantblue.analytics.isr.core.component.accgen.IAccess;
import com.radiantblue.analytics.isr.core.component.opgen.IModeAccessTime;
import com.radiantblue.analytics.isr.core.model.sensormode.SensorMode;
import com.radiantblue.analytics.isr.core.op.IModeOp;

// Lifted from Moonset
public class CustomSarSensorMode extends
		SensorMode
{
	private InlineQualityCalculator qualityCalculator;
	private DwellCalculator dwellCalculator;

	// Number of channels or beams
	private Integer nch;

	// Effective antenna area (m^2)
	private Double antArea;

	// Maximum Receive Bandwidth (BWRF) (Hz)
	private Double bwrf;

	// IPR resolution (based on BWRF) (m)
	private Double ipr;

	// Peak RF Transmit Power (W)
	private Double peakPW;

	// Center frequency (Hz)
	private Double fc;

	// Maximum Transmit Duty Factor
	private Double maxTDTY;

	// System thermal noise temperature (dB)
	private Double t0;

	// System Receiver Loss Figure (dB)
	private Double nf;

	// Total System Receive Loss (dB)
	private Double loss;

	// MNR spec (dB)
	private Double mnrReq;

	// Azimuth IPR factor
	private Double azmIPRFac;

	// Range IPR factor
	private Double rngIPRFac;

	// min time between dwells (s)
	private Double minTimeBetweenDwells;

	// For some reason, this has to return List<Object> to override,
	// instead of List<Double>
	@Override
	public List<Object> getQualities(
			final PointToPointGeometry geom ) {
		final double vehVel = geom.source()
				.velocity()
				.length();
		final Length slantRange = geom.range();
		final Angle graze = geom.grazingAngle_atDest();
		final Angle slopeAngle = geom.slopeAngle_atSource();
		final Angle dca = geom.dopplerConeAngle_atSource();

		// get the max quality possible for this sensor mode
		final double quality = qualityCalculator.getQuality(vehVel,
															nch,
															antArea,
															bwrf,
															Length.fromMeters(ipr),
															peakPW,
															fc,
															maxTDTY,
															t0,
															nf,
															loss,
															mnrReq,
															graze,
															slopeAngle,
															dca,
															slantRange);

		final Double[] qualities = {
			quality
		};
		final List<Object> qualityList = Arrays.asList((Object[]) qualities);

		return qualityList;
	}

	public Double getDwellTimeInSeconds(
			final IAccess trimmedAccess,
			final Double quality,
			final DateTime atTime ) {

		final PointToPointGeometry geom = new PointToPointGeometry(
				trimmedAccess.accessRequest()
						.source(),
				trimmedAccess.accessRequest()
						.dest(),
				atTime);

		final double vehVel = geom.source()
				.velocity()
				.length();
		final Length slantRange = geom.range();
		final Angle dca = geom.dopplerConeAngle_atSource();

		// get the dwell time for quality (seconds)
		final double dwell = dwellCalculator.getDwell(	vehVel,
														bwrf,
														Length.fromMeters(ipr),
														fc,
														azmIPRFac,
														dca,
														slantRange);

		// negative dwell means quality is not possible
		return dwell >= 0 ? dwell : null;
	}

	public Double getManeuverTimeInSeconds(
			final IAccess trimmedAccess,
			final Double targetAngleInRadians ) {
		return minTimeBetweenDwells;
	}

	@Override
	public IModeOp createOpImpl(
			final IModeAccessTime arg0,
			final Context arg1 ) {
		// TODO Auto-generated method stub
		return null;
	}

	public InlineQualityCalculator getQualityCalculator() {
		return qualityCalculator;
	}

	public void setQualityCalculator(
			final InlineQualityCalculator qualityCalculator ) {
		this.qualityCalculator = qualityCalculator;
	}

	public DwellCalculator getDwellCalculator() {
		return dwellCalculator;
	}

	public void setDwellCalculator(
			final DwellCalculator dwellCalculator ) {
		this.dwellCalculator = dwellCalculator;
	}

	public Integer getNch() {
		return nch;
	}

	public void setNch(
			final Integer nch ) {
		this.nch = nch;
	}

	public Double getAntArea() {
		return antArea;
	}

	public void setAntArea(
			final Double antArea ) {
		this.antArea = antArea;
	}

	public Double getBwrf() {
		return bwrf;
	}

	public void setBwrf(
			final Double bwrf ) {
		this.bwrf = bwrf;
	}

	public Double getIpr() {
		return ipr;
	}

	public void setIpr(
			final Double ipr ) {
		this.ipr = ipr;
	}

	public Double getPeakPW() {
		return peakPW;
	}

	public void setPeakPW(
			final Double peakPW ) {
		this.peakPW = peakPW;
	}

	public Double getFc() {
		return fc;
	}

	public void setFc(
			final Double fc ) {
		this.fc = fc;
	}

	public Double getMaxTDTY() {
		return maxTDTY;
	}

	public void setMaxTDTY(
			final Double maxTDTY ) {
		this.maxTDTY = maxTDTY;
	}

	public Double getT0() {
		return t0;
	}

	public void setT0(
			final Double t0 ) {
		this.t0 = t0;
	}

	public Double getNf() {
		return nf;
	}

	public void setNf(
			final Double nf ) {
		this.nf = nf;
	}

	public Double getLoss() {
		return loss;
	}

	public void setLoss(
			final Double loss ) {
		this.loss = loss;
	}

	public Double getMnrReq() {
		return mnrReq;
	}

	public void setMnrReq(
			final Double mnrReq ) {
		this.mnrReq = mnrReq;
	}

	public Double getRngIPRFac() {
		return rngIPRFac;
	}

	public void setRngIPRFac(
			final Double rngIPRFac ) {
		this.rngIPRFac = rngIPRFac;
	}

	public Double getAzmIPRFac() {
		return azmIPRFac;
	}

	public void setAzmIPRFac(
			final Double azmIPRFac ) {
		this.azmIPRFac = azmIPRFac;
	}

	public Double getMinTimeBetweenDwells() {
		return minTimeBetweenDwells;
	}

	public void setMinTimeBetweenDwells(
			final Double minTimeBetweenDwells ) {
		this.minTimeBetweenDwells = minTimeBetweenDwells;
	}
}
