package com.maxar.init.database.loaders.jpa.tasking;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;

import com.zaxxer.hikari.HikariDataSource;

@Configuration
@EnableJpaRepositories(basePackages = "com.maxar.mission.repository", entityManagerFactoryRef = "missionEntityManagerFactory", transactionManagerRef = "missionTransactionManager")
public class MissionJpaConfiguration
{
	@Bean
	@ConfigurationProperties("mission.datasource")
	public DataSourceProperties missionDataSourceProperties() {
		return new DataSourceProperties();
	}

	@Bean
	@ConfigurationProperties("mission.datasource.configuration")
	public DataSource missionDataSource() {
		return missionDataSourceProperties().initializeDataSourceBuilder()
				.type(HikariDataSource.class)
				.build();
	}

	@Bean(name = "missionEntityManagerFactory")
	public LocalContainerEntityManagerFactoryBean missionEntityManagerFactory(
			final EntityManagerFactoryBuilder builder ) {
		return builder.dataSource(missionDataSource())
				.packages(	"com.maxar.mission.repository",
							"com.maxar.mission.entity")
				.build();
	}

	@Bean
	public PlatformTransactionManager missionTransactionManager(
			@Qualifier("missionEntityManagerFactory")
			final EntityManagerFactory missionEntityManagerFactory ) {
		return new JpaTransactionManager(
				missionEntityManagerFactory);
	}
}
