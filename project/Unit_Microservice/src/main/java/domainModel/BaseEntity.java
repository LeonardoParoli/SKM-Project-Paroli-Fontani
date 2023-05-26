package domainModel;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;


@MappedSuperclass
public abstract class BaseEntity {
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="entity_id")
	private Long id;
	@Column(unique = true)
	private String uuid;

	protected BaseEntity() {}

	public BaseEntity(String uuid) {
		if (uuid == null) {
			throw new IllegalArgumentException("uuid cannot be null");
		}
		this.uuid = uuid;
	}
	
	public String getUuid() {
		return uuid;
	}
	
	public Long getId() {
		return id;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof BaseEntity)) {
			return false;
		}
		return uuid.equals(((BaseEntity) obj).getUuid());
	} 
}
