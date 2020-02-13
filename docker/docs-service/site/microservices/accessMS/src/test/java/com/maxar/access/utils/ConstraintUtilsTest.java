package com.maxar.access.utils;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import com.maxar.access.common.utils.ConstraintUtils;
import com.maxar.access.model.AccessConstraint;
import com.radiantblue.analytics.core.constraint.IAccessConstraint;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@TestPropertySource(locations = "classpath:testaccessms.properties")
public class ConstraintUtilsTest
{
	@Autowired
	private ConstraintUtils constraintUtils;

	@Test
	public void testGetConstraintNames() {
		final List<String> names = constraintUtils.getAllConstraintNames();

		for (final String name : names) {
			System.out
					.println(
							"Constraint name: " + name);
		}

		Assert
				.assertEquals(
						26,
						names.size());
	}

	@Test
	public void testGenerateIAccessConstraint() {
		final List<String> names = constraintUtils.getAllConstraintNames();

		for (final String name : names) {
			final AccessConstraint jsonConstraint = new AccessConstraint();
			jsonConstraint
					.setName(
							name);
			jsonConstraint
					.setMinValue(
							1.0);
			jsonConstraint
					.setMaxValue(
							2.0);

			final IAccessConstraint serverConstraint = constraintUtils
					.buildConstraint(
							jsonConstraint);

			Assert
					.assertNotNull(
							serverConstraint);
			Assert
					.assertTrue(
							serverConstraint instanceof IAccessConstraint);
			Assert
					.assertEquals(
							name,
							serverConstraint.getName());
		}
	}
}
