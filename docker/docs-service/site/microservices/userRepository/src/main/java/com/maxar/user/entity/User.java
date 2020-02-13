package com.maxar.user.entity;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@Entity
@EqualsAndHashCode(exclude = "sessions")
@NoArgsConstructor
@Table(name = "user_table")
public class User implements
		Serializable
{
	private static final long serialVersionUID = -3070942985555695153L;

	@Column(name = "username")
	@Id
	private String username;
	
	@Column(name = "preferences")
	private String preferences;

	@JoinTable(name = "user_session", joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "session_id"))
	@ManyToMany(cascade = {
		CascadeType.PERSIST,
		CascadeType.MERGE
	})
	private Set<Session> sessions = new HashSet<>();

	/**
	 * Add a session for the user, and also add the user to the session.
	 *
	 * @param session
	 *            The session to add.
	 * @return True if the user did not already have the session and if the session
	 *         did not already have the user and both were successfully added.
	 */
	public boolean addSession(
			final Session session ) {
		final boolean addedSession = sessions.add(session);

		final boolean addedThis = session.getUsers()
				.add(this);

		return addedSession && addedThis;
	}

	/**
	 * Remove a session from a user, and also remove the user from the session.
	 *
	 * @param session
	 *            The session to remove.
	 * @return True if the user had the session and if the session had the user and
	 *         both were successfully removed.
	 */
	public boolean removeSession(
			final Session session ) {
		final boolean removedSession = sessions.remove(session);

		final boolean removedThis = session.getUsers()
				.remove(this);

		return removedSession && removedThis;
	}
}
