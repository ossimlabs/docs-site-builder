package com.maxar.mission.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.maxar.mission.entity.Track;

@Repository
public interface TrackRepository extends
		JpaRepository<Track, UUID>
{
	Track findById(
			final String id );
}
