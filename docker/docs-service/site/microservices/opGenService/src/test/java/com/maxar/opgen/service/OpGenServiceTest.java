package com.maxar.opgen.service;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import com.maxar.asset.common.exception.SensorModeNameDoesNotExistException;
import com.maxar.opgen.model.Op;
import com.maxar.opgen.model.OpRequest;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@TestPropertySource(locations = "classpath:testopgen.properties")
public class OpGenServiceTest
{
	private static final String TEST_ASSET_NAME = "ASSET_NAME";

	@Autowired
	private OpGenService opGenService;

	@Test
	public void testCreateOpAtTimeNullAsset()
			throws SensorModeNameDoesNotExistException {
		final OpRequest request = new OpRequest();
		request.setAssetName(TEST_ASSET_NAME);

		final List<Op> ops = opGenService.createOpAtTime(	request,
															null);

		Assert.assertEquals(	1,
						ops.size());
		final Op op = ops.get(0);
		Assert.assertEquals(	false,
						op.isValid());
		Assert.assertEquals(	"Asset '" + TEST_ASSET_NAME + "' was not found",
						op.getReason());
	}
}
