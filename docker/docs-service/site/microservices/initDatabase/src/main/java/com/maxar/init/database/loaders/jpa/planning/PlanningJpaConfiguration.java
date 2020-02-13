package com.maxar.init.database.loaders.jpa.planning;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;

import com.zaxxer.hikari.HikariDataSource;

@Configuration
@EnableJpaRepositories(basePackages = "com.maxar.planning.repository", entityManagerFactoryRef = "planningEntityManagerFactory", transactionManagerRef = "planningTransactionManager")
public class PlanningJpaConfiguration
{
	@Bean
	@Primary
	@ConfigurationProperties("planning.datasource")
	public DataSourceProperties planningDataSourceProperties() {
		return new DataSourceProperties();
	}

	@Bean
	@Primary
	@ConfigurationProperties("planning.datasource.configuration")
	public DataSource planningDataSource() {
		return planningDataSourceProperties().initializeDataSourceBuilder()
				.type(HikariDataSource.class)
				.build();
	}

	@Bean(name = "planningEntityManagerFactory")
	@Primary
	public LocalContainerEntityManagerFactoryBean planningEntityManagerFactory(
			final EntityManagerFactoryBuilder builder ) {
		return builder.dataSource(planningDataSource())
				.packages(	"com.maxar.planning.repository",
							"com.maxar.planning.entity")
				.build();
	}

	@Bean
	@Primary
	public PlatformTransactionManager planningTransactionManager(
			@Qualifier("planningEntityManagerFactory")
			final EntityManagerFactory planningEntityManagerFactory ) {
		return new JpaTransactionManager(
				planningEntityManagerFactory);
	}
}
