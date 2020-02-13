package com.maxar.workflow.service;

import java.util.Collections;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import com.maxar.workflow.exception.CzmlGenerationException;
import com.maxar.workflow.model.CzmlErrorReporterWithId;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@TestPropertySource(locations = "classpath:testworkflowms.properties")
public class WorkflowMapServiceTest
{
	private static final String EXAMPLE_SESSION = "session";

	private static final String EXAMPLE_CZML_ERROR_STRING = "error generating CZML";

	@Autowired
	private WorkflowMapService workflowMapService;

	@MockBean
	private ApiService apiService;

	@Test
	public void testDeleteAllCzml()
			throws CzmlGenerationException {
		Mockito.doNothing()
				.when(apiService)
				.deleteCzml(Mockito.eq(EXAMPLE_SESSION));

		final CzmlErrorReporterWithId result = workflowMapService.deleteAllCzml(EXAMPLE_SESSION);

		Assert.assertNull(result);
	}

	@Test
	public void testDeleteAllCzmlFailure()
			throws CzmlGenerationException {
		Mockito.doThrow(new CzmlGenerationException(
				EXAMPLE_CZML_ERROR_STRING))
				.when(apiService)
				.deleteCzml(Mockito.eq(EXAMPLE_SESSION));

		final CzmlErrorReporterWithId result = workflowMapService.deleteAllCzml(EXAMPLE_SESSION);

		Assert.assertNotNull(result);
		Assert.assertEquals(EXAMPLE_SESSION,
							result.getId());
		Assert.assertEquals(EXAMPLE_CZML_ERROR_STRING,
							result.getCzmlError());
	}

	@Test
	public void testDisplayCzml()
			throws CzmlGenerationException {
		Mockito.doNothing()
				.when(apiService)
				.displayCzml(	Mockito.eq(EXAMPLE_SESSION),
								Mockito.anyList());

		final CzmlErrorReporterWithId result = workflowMapService.displayCzml(	EXAMPLE_SESSION,
																				Collections.emptyList());

		Assert.assertNull(result);
	}

	@Test
	public void testDisplayCzmlFailure()
			throws CzmlGenerationException {
		Mockito.doThrow(new CzmlGenerationException(
				EXAMPLE_CZML_ERROR_STRING))
				.when(apiService)
				.displayCzml(	Mockito.eq(EXAMPLE_SESSION),
								Mockito.anyList());

		final CzmlErrorReporterWithId result = workflowMapService.displayCzml(	EXAMPLE_SESSION,
																				Collections.emptyList());

		Assert.assertNotNull(result);
		Assert.assertEquals(EXAMPLE_SESSION,
							result.getId());
		Assert.assertEquals(EXAMPLE_CZML_ERROR_STRING,
							result.getCzmlError());
	}
}
