package com.maxar.weather.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.maxar.weather.entity.map.MapGrid;

public interface BaseMapGridRepository<T extends MapGrid> extends
		JpaRepository<T, UUID>
{
	public List<T> findById(
			String id );
}
