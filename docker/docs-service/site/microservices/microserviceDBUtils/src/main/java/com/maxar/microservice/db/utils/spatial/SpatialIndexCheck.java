package com.maxar.microservice.db.utils.spatial;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Query;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SpatialIndexCheck implements
		InitializingBean
{
	private static final Logger LOG = Logger.getLogger(SpatialIndexCheck.class);

	@Autowired
	private EntityManagerFactory emf;

	@Override
	public void afterPropertiesSet()
			throws Exception {
		createSpatialIndex();
	}

	protected void createSpatialIndex() {
		final EntityManager entityManager = emf.createEntityManager();
		final Query geoTables = entityManager
				.createNativeQuery("select g.f_table_name, g.f_geometry_column from public.geometry_columns g");

		final List<Object[]> geoColumns = geoTables.getResultList();

		if (!geoColumns.isEmpty()) {
			for (final Object[] resultRow : geoColumns) {
				final String geoTable = (String) resultRow[0];
				final String geoColumn = (String) resultRow[1];

				final String indexName = "idx_spatial_" + geoTable + "_" + geoColumn;

				final Query checkIndex = entityManager
						.createNativeQuery("select indexname from pg_indexes where indexname ='" + indexName + "'");

				if (checkIndex.getResultList()
						.size() == 0) {
					LOG.info("Creating index " + indexName);
					
					final Query q = entityManager.createNativeQuery("create index " + indexName + " on " + geoTable
							+ " using gist (" + geoColumn + ")");

					entityManager.getTransaction()
							.begin();
					q.executeUpdate();
					entityManager.getTransaction()
							.commit();
				}
			}
		}
			
		entityManager.close();
	}
}
