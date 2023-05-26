package domainModel;

import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name="unit")
public class Unit extends BaseEntity {
	@Column(name="unitName", unique=true, nullable=false)
	private String name;
	
	protected Unit() {
		super();
	};
	
	public Unit(String name) {
		super(UUID.randomUUID().toString());
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name=name;
	}
}
