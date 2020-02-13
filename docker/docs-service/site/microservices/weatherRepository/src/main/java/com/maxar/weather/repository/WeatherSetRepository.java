package com.maxar.weather.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.maxar.weather.entity.weather.WeatherSet;

@Repository
public interface WeatherSetRepository extends
		JpaRepository<WeatherSet, UUID>
{
	@Transactional
	public long deleteByAtTimeMillis(
			long atTimeMillis );

	public List<WeatherSet> findByAtTimeMillisLessThanEqualOrderByAtTimeMillisDesc(
			Long atTime );
	
	public List<WeatherSet> findByAtTimeMillisBetween(Long start, Long stop);
}
