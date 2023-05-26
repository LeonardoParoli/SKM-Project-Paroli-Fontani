package unitTest.domainModel;

import org.junit.Before;
import org.junit.Test;
import static org.assertj.core.api.Assertions.*;
import domainModel.Unit;

public class UnitTest {
	private Unit unit;
	private final String unitName = "testUnit";
	
	@Before
	public void setup() {
		unit = new Unit(unitName);
	}
	
	@Test
	public void getNameTest() {
		assertThat(unit.getName()).isInstanceOf(String.class).isEqualTo(unitName);
	}
	
	@Test
	public void setName() {
		unit.setName("anotherName");
		assertThat(unit.getName()).isInstanceOf(String.class).isEqualTo("anotherName");
	}
}
