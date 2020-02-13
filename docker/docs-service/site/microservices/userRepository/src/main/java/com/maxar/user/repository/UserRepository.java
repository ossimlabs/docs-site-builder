package com.maxar.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.maxar.user.entity.User;

@Repository
public interface UserRepository extends
		JpaRepository<User, String>
{
}
