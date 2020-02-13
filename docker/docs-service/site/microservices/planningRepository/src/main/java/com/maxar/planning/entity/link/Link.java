package com.maxar.planning.entity.link;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.maxar.planning.model.link.LinkModel;
import com.maxar.planning.entity.image.ImageOp;
import com.maxar.planning.entity.tasking.Tasking;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
@Table(name = "link", indexes = {
	@Index(name = "IDX_crid", columnList = "crid"),
	@Index(name = "IDX_targetid", columnList = "targetid")
})
public class Link implements
		Serializable
{
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(generator = "uuidGenerator")
	@GenericGenerator(name = "uuidGenerator", strategy = "uuid2")
	@Column(name = "linkkey", columnDefinition = "UUID")
	@JsonIgnore
	protected UUID linkKey;

	@Column(name = "crid")
	private String crId;

	@Column(name = "targetid")
	private String targetId;

	@OneToMany(mappedBy = "collectionWindow", cascade = CascadeType.ALL, orphanRemoval = true)
	@JsonIgnore
	private Set<ImageOp> imageOps = new HashSet<>();

	@OneToMany(mappedBy = "link", cascade = CascadeType.ALL, orphanRemoval = true)
	@JsonIgnore
	private Set<Tasking> taskings = new HashSet<>();

	public Link(
			String crId,
			String tgtId ) {
		this.crId = crId;
		this.targetId = tgtId;
	}

	public LinkModel toModel() {
		return LinkModel
				.builder()
				.crId(
						crId)
				.targetId(
						targetId)
				.build();
	}
	
	@Override
    public boolean equals(Object o) {

        if (o == this) return true;
        if (!(o instanceof Link)) {
            return false;
        }

        Link link = (Link) o;

        return link.crId.equals(crId) &&
                link.targetId.equals(targetId);
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 31 * result + crId.hashCode();
        result = 31 * result + targetId.hashCode();
        return result;
    }
}
