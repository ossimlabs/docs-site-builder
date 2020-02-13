package com.maxar.ephemeris.dm.dataingest;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.context.support.GenericXmlApplicationContext;

import com.maxar.ephemeris.entity.VCM;
import com.maxar.ephemeris.repository.EphemerisRepository;
import com.maxar.manager.dataingest.DataTypeParser;

public class EphemerisFileIngesterTest
{
	private static final String EPHEMERIS_INGESTER_BEAN_FILE = "testephemeris.xml";

	private static final String EPHEMERIS_TLE_DATA_FILE = "data.tle";
	
	private static final String EPHEMERIS_SV_DATA_FILE = "data.sv";
	
	private static final String EPHEMERIS_VCM_DATA_FILE = "data.vcm";
	
	private static final int EPHEMERIS_VCM_SCN = 99901;

	private EphemerisRepository ephemerisRepository;

	private Map<String, EphemerisFileIngester> ingester;

	private File ephemerisTleDataFile;
	
	private File ephemerisStateVectorDataFile;
	
	private File ephemerisVcmDataFile;

	@Before
	public void setUp()
			throws IOException {
		try (GenericXmlApplicationContext context = new GenericXmlApplicationContext()) {
			context.load(EPHEMERIS_INGESTER_BEAN_FILE);

			context.refresh();

			ingester = context.getBeansOfType(EphemerisFileIngester.class);

			final URL ephemerisTleDataUrl = Thread.currentThread()
					.getContextClassLoader()
					.getResource(EPHEMERIS_TLE_DATA_FILE);

			ephemerisTleDataFile = new File(
					ephemerisTleDataUrl.getFile());
			
			final URL ephemerisStateVectorDataUrl = Thread.currentThread()
					.getContextClassLoader()
					.getResource(EPHEMERIS_SV_DATA_FILE);

			ephemerisStateVectorDataFile = new File(
					ephemerisStateVectorDataUrl.getFile());
			
			final URL ephemerisVcmDataUrl = Thread.currentThread()
					.getContextClassLoader()
					.getResource(EPHEMERIS_VCM_DATA_FILE);

			ephemerisVcmDataFile = new File(
					ephemerisVcmDataUrl.getFile());

			ephemerisRepository = Mockito.mock(EphemerisRepository.class);
		}
	}

	@Test
	public void testMockTleIngestToDatabase() {
		Mockito.when(ephemerisRepository.saveAll(ArgumentMatchers.any()))
				.thenReturn(Collections.emptyList());

		EphemerisFileIngester tleIngester = ingester.get("TleFileIngester");
		
		tleIngester.init();
		tleIngester.ingestFile(ephemerisTleDataFile,
							null);

		Assert.assertTrue(true);

		final DataTypeParser<?> parser = tleIngester.getParser();

		Assert.assertTrue(parser instanceof SampleTleParser);

		final EphemerisRepository repository = tleIngester.getRepository();

		Assert.assertNull(repository);
	}
	
	@Test
	public void testMockStateVectorIngestToDatabase() {
		Mockito.when(ephemerisRepository.saveAll(ArgumentMatchers.any()))
				.thenReturn(Collections.emptyList());

		EphemerisFileIngester stateVectorIngester = ingester.get("StateVectorFileIngester");
		
		stateVectorIngester.init();
		stateVectorIngester.ingestFile(ephemerisStateVectorDataFile,
							null);

		Assert.assertTrue(true);

		final DataTypeParser<?> parser = stateVectorIngester.getParser();

		Assert.assertTrue(parser instanceof SampleStateVectorParser);

		final EphemerisRepository repository = stateVectorIngester.getRepository();

		Assert.assertNull(repository);
	}
	
	@Test
	public void testMockVcmIngestToDatabase() {
		Mockito.when(ephemerisRepository.saveAll(ArgumentMatchers.any()))
				.thenReturn(Collections.emptyList());

		EphemerisFileIngester vcmIngester = ingester.get("VcmFileIngester");
		
		vcmIngester.init();
		vcmIngester.ingestFile(ephemerisVcmDataFile,
							null);

		Assert.assertTrue(true);

		final DataTypeParser<?> parser = vcmIngester.getParser();

		Assert.assertTrue(parser instanceof VcmParser);
		
		List<VCM> vcms = new ArrayList<>();
		
		try {
			vcms = ((VcmParser)parser).parseData(new FileInputStream(ephemerisVcmDataFile));
		}
		catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		Assert.assertEquals(1, vcms.size());
		
		VCM vcm = vcms.get(0);
		
		Assert.assertEquals(EPHEMERIS_VCM_SCN, vcm.getScn());

		final EphemerisRepository repository = vcmIngester.getRepository();

		Assert.assertNull(repository);
	}

	@Test
	public void testBadTleIngesterDirectories() {
		// can use the ephemeris data file since it is not a directory
		EphemerisFileIngester tleIngester = ingester.get("TleFileIngester");
		
		tleIngester.setSuccessDir(ephemerisTleDataFile);
		tleIngester.setErrorDir(ephemerisTleDataFile);

		tleIngester.init();

		Assert.assertTrue(true);
	}
	
	@Test
	public void testBadStateVectorIngesterDirectories() {
		// can use the ephemeris data file since it is not a directory
		EphemerisFileIngester tleIngester = ingester.get("StateVectorFileIngester");
		
		tleIngester.setSuccessDir(ephemerisStateVectorDataFile);
		tleIngester.setErrorDir(ephemerisStateVectorDataFile);

		tleIngester.init();

		Assert.assertTrue(true);
	}
	
	@Test
	public void testBadVcmIngesterDirectories() {
		// can use the ephemeris data file since it is not a directory
		EphemerisFileIngester vcmIngester = ingester.get("VcmFileIngester");
		
		vcmIngester.setSuccessDir(ephemerisVcmDataFile);
		vcmIngester.setErrorDir(ephemerisVcmDataFile);

		vcmIngester.init();

		Assert.assertTrue(true);
	}
}
