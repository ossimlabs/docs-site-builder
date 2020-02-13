package com.maxar.access.common.utils;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.apache.log4j.Logger;
import org.reflections.Reflections;
import org.springframework.stereotype.Component;

import com.radiantblue.analytics.aerospace.geometry.constraint.AngleConstraint;
import com.radiantblue.analytics.aerospace.geometry.constraint.DoubleConstraint;
import com.radiantblue.analytics.aerospace.geometry.constraint.IGeometryConstraint;
import com.radiantblue.analytics.aerospace.geometry.constraint.LengthConstraint;
import com.radiantblue.analytics.core.constraint.IAccessConstraint;
import com.radiantblue.analytics.core.log.SourceLogger;

@Component
public class ServerConstraints
{
	private static Logger logger = SourceLogger
			.getLogger(
					new Object() {}.getClass().getEnclosingClass().getName());

	protected Map<String, ConstraintDetail> constraints = null;
	
	@PostConstruct
	public void init() {
		constraints = detectConstraints();
	}

	public Map<String, IAccessConstraint> getAllConstraints() {

		final Map<String, IAccessConstraint> allConstraints = new HashMap<>();

		for (final ConstraintDetail cd : constraints.values()) {
			allConstraints
					.put(
							cd.instance.getName(),
							cd.instance);
		}

		return allConstraints;
	}

	public ConstraintDetail getConstraintByName(
			final String name ) {
		return constraints
				.get(
						name);
	}

	private Map<String, ConstraintDetail> detectConstraints() {

		logger
				.debug(
						"Detecting valid constraints:");

		final Map<String, ConstraintDetail> constraints = new HashMap<>();

		final Reflections reflections = new Reflections(
				"com.radiantblue.analytics");

		try {
			final Set<Class<? extends IGeometryConstraint>> subTypes = reflections
					.getSubTypesOf(
							IGeometryConstraint.class);

			logger
					.debug(
							"Num subclasses: " + subTypes.size());

			for (final Class<? extends IGeometryConstraint> clazz : subTypes) {

				logger
						.debug(
								"Class: " + clazz.getCanonicalName());

				// skip abstract classes
				if (Modifier
						.isAbstract(
								clazz.getModifiers())) {
					logger
							.debug(
									"Skipping because " + clazz.getCanonicalName() + " is abstract");
					continue;
				}

				// skip constraints without default constructors
				boolean hasDefaultConstructor = false;
				for (final Constructor<?> c : clazz.getDeclaredConstructors()) {
					hasDefaultConstructor |= (c.getParameterTypes().length == 0);
				}

				if (!hasDefaultConstructor) {
					logger
							.debug(
									"Skipping because " + clazz.getCanonicalName()
											+ " does not have a default constructor");
					continue;
				}

				IAccessConstraint instance = null;

				if (AngleConstraint.class
						.isAssignableFrom(
								clazz)) {
					instance = clazz.getConstructor().newInstance();
				}
				else if (LengthConstraint.class
						.isAssignableFrom(
								clazz)) {
					instance = clazz.getConstructor().newInstance();
				}
				else if (DoubleConstraint.class
						.isAssignableFrom(
								clazz)) {
					instance = clazz.getConstructor().newInstance();
				}

				if (instance != null) {
					logger
							.debug(
									"Name: " + instance.getName());

					constraints
							.put(
									instance.getName(),
									new ConstraintDetail(
											clazz,
											instance));
				}
			}
		}
		catch (final IllegalAccessException | SecurityException | IllegalArgumentException | InstantiationException | InvocationTargetException e) {
			logger
					.error(
							"Problem detecting constraints: " + e.getLocalizedMessage(),
							e);
		}
		catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return constraints;
	}

}
