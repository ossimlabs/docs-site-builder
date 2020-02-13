package com.maxar.access.common.utils;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.apache.log4j.Logger;

import com.radiantblue.analytics.aerospace.geometry.constraint.AngleConstraint;
import com.radiantblue.analytics.aerospace.geometry.constraint.DoubleConstraint;
import com.radiantblue.analytics.aerospace.geometry.constraint.LengthConstraint;
import com.radiantblue.analytics.core.analysis.interval.DoubleInterval;
import com.radiantblue.analytics.core.constraint.IAccessConstraint;
import com.radiantblue.analytics.core.log.SourceLogger;
import com.radiantblue.analytics.core.measures.AngularRange;
import com.radiantblue.analytics.core.measures.Length;
import com.radiantblue.analytics.core.measures.LengthRange;

public class ConstraintDetail
{

	private static Logger logger = SourceLogger
			.getLogger(
					ConstraintDetail.class.getName());

	Class<?> constraintClass = null;
	IAccessConstraint instance = null;

	public ConstraintDetail(
			final Class<?> constraintClass,
			final IAccessConstraint instance ) {
		this.constraintClass = constraintClass;
		this.instance = instance;
	}

	public IAccessConstraint toConstraint(
			Double min,
			Double max ) {

		if (min == null) {
			min = Double.MIN_VALUE;
		}

		if (max == null) {
			max = Double.MAX_VALUE;
		}

		if (AngleConstraint.class
				.isAssignableFrom(
						constraintClass)) {

			return toConstraint(
					AngularRange
							.createFromDegrees(
									min,
									max));

		}
		else if (LengthConstraint.class
				.isAssignableFrom(
						constraintClass)) {

			return toConstraint(
					new LengthRange(
							Length
									.fromMeters(
											min),
							Length
									.fromMeters(
											max)));

		}
		else if (DoubleConstraint.class
				.isAssignableFrom(
						constraintClass)) {

			return toConstraint(
					new DoubleInterval(
							min,
							max));

		}
		return null;
	}

	public IAccessConstraint toConstraint(
			final AngularRange ar ) {

		try {
			if (AngleConstraint.class
					.isAssignableFrom(
							constraintClass)) {

				Constructor<?> ctor;

				try {
					ctor = constraintClass.getDeclaredConstructor();
				}
				catch (NoSuchMethodException | SecurityException e) {
					logger
							.error(
									"Problem getting noarg constructor for class: " + constraintClass.getName() + " "
											+ e.getLocalizedMessage(),
									e);
					return null;
				}

				final AngleConstraint c = (AngleConstraint) ctor.newInstance();
				c
						.setRange(
								ar);

				return c;
			}
		}
		catch (final InstantiationException | IllegalAccessException | InvocationTargetException e) {
			logger
					.error(
							"Problem getting Angle Constraint: " + e.getLocalizedMessage(),
							e);
		}

		return null;
	}

	public IAccessConstraint toConstraint(
			final DoubleInterval di ) {

		try {
			if (DoubleConstraint.class
					.isAssignableFrom(
							constraintClass)) {

				Constructor<?> ctor;

				try {
					ctor = constraintClass.getDeclaredConstructor();
				}
				catch (NoSuchMethodException | SecurityException e) {
					logger
							.error(
									"Problem getting noarg constructor for class: " + constraintClass.getName() + " "
											+ e.getLocalizedMessage(),
									e);
					return null;
				}

				final DoubleConstraint c = (DoubleConstraint) ctor.newInstance();
				c
						.setRange(
								di);

				return c;
			}
		}
		catch (final InstantiationException | IllegalAccessException | InvocationTargetException e) {
			logger
					.error(
							"Problem getting Double Constraint: " + e.getLocalizedMessage(),
							e);
		}
		return null;
	}

	public IAccessConstraint toConstraint(
			final LengthRange lr ) {

		try {
			if (LengthConstraint.class
					.isAssignableFrom(
							constraintClass)) {

				Constructor<?> ctor;

				try {
					ctor = constraintClass.getDeclaredConstructor();
				}
				catch (NoSuchMethodException | SecurityException e) {
					logger
							.error(
									"Problem getting noarg constructor for class: " + constraintClass.getName() + " "
											+ e.getLocalizedMessage(),
									e);
					return null;
				}

				final LengthConstraint c = (LengthConstraint) ctor.newInstance();
				c
						.setRange(
								lr);

				return c;
			}
		}
		catch (final InstantiationException | IllegalAccessException | InvocationTargetException e) {
			logger
					.error(
							"Problem getting Length Constraint: " + e.getLocalizedMessage(),
							e);
		}
		return null;
	}

}
