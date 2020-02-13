package com.maxar.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.maxar.user.entity.Session;

@Repository
public interface SessionRepository extends
		JpaRepository<Session, String>
{
}
