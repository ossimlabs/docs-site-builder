package com.maxar.user.entity;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.Table;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@Entity
@EqualsAndHashCode
@NoArgsConstructor
@Table(name = "session")
public class Session implements
		Serializable
{
	private static final long serialVersionUID = 1537151175502443176L;

	@Column(name = "UUID")
	@EqualsAndHashCode.Exclude
	@Id
	private UUID uuid;

	@Column(name = "id")
	private String id;

	@ManyToMany(mappedBy = "sessions")
	@EqualsAndHashCode.Exclude
	private Set<User> users = new HashSet<>();

}
