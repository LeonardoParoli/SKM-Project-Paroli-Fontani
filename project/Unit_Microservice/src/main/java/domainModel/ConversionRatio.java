package domainModel;

import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name = "conversionRatio")
public class ConversionRatio extends BaseEntity {
	@OneToOne
	@OnDelete(action= OnDeleteAction.CASCADE)
	@JoinColumn(name = "start_unit")
	private Unit first;
	@OneToOne
	@OnDelete(action= OnDeleteAction.CASCADE)
	@JoinColumn(name = "end_unit")
	private Unit second;
	@Column(name = "conversionRatio")
	private double ratio;

	protected ConversionRatio() {
		super();
	};
	
	public ConversionRatio(Unit first, Unit second, double ratio) {
		super(UUID.randomUUID().toString());
		this.first = first;
		this.second = second;
		this.ratio = ratio;
	}

	public Unit getFirst() {
		return first;
	}

	public Unit getSecond() {
		return second;
	}

	public double getRatio() {
		return ratio;
	}
}
