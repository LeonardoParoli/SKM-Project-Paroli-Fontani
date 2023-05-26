package unitTest.domainModel;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import domainModel.ConversionRatio;
import domainModel.Unit;

public class ConversionRatioTest {
	private ConversionRatio conversionRatio;
	private final double ratio = 3.0; 
	private Unit unit1;
	private Unit unit2;
	@Before
	public void setup() {
		unit1 = Mockito.mock(Unit.class);
		unit2 = Mockito.mock(Unit.class);
		conversionRatio = new ConversionRatio(unit1,unit2, ratio);
	}
	
	@Test
	public void getFirstTest() {
		assertThat(conversionRatio.getFirst()).isInstanceOf(Unit.class).isEqualTo(unit1);
	}
	
	@Test
	public void getSecondTest() {
		assertThat(conversionRatio.getSecond()).isInstanceOf(Unit.class).isEqualTo(unit2);
	}
	
	@Test
	public void getRatioTest() {
		assertThat(conversionRatio.getRatio()).isInstanceOf(Double.class).isEqualTo(ratio);
	}
}
