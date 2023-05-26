package unitTest.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import domainModel.ConversionRatio;
import domainModel.Unit;
import service.ConversionRatioDAO;

import static org.assertj.core.api.Assertions.*;

import java.util.ArrayList;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;

@RunWith(MockitoJUnitRunner.class)
public class ConversionRatioDAOTest {
	@Mock
	EntityManager manager;

	@Mock
	TypedQuery<ConversionRatio> query;
	ConversionRatioDAO dao = new ConversionRatioDAO();
	private ConversionRatio conversionRatio1;
	private ConversionRatio conversionRatio2;
	private ConversionRatio conversionRatio3;
	private final double ratio=3.0;
	
	@Before
	public void setup() {
		Unit unit1 = Mockito.mock(Unit.class);
		Mockito.when(unit1.getId()).thenReturn((long) 1);
		Unit unit2 = Mockito.mock(Unit.class);
		Mockito.when(unit2.getId()).thenReturn((long) 2);
		Unit unit3 = Mockito.mock(Unit.class);
		conversionRatio1 = new ConversionRatio(unit1,unit2,ratio);
		conversionRatio2 = new ConversionRatio(unit2,unit3,ratio);
		conversionRatio3 = new ConversionRatio(unit1,unit3,ratio);
		Mockito.when(manager.createQuery(Mockito.any(String.class), Mockito.eq(ConversionRatio.class))).thenReturn(query);
		Mockito.when(manager.createQuery(Mockito.any(String.class))).thenReturn(query);
	    Mockito.when(query.setParameter(Mockito.any(String.class), Mockito.any())).thenReturn(query);
	    dao.setEntityManager(manager);
	}
	
	@Test
	public void getConversionRatioByIdTest() {
		Mockito.when(query.getSingleResult()).thenReturn(conversionRatio1);
		assertThat(dao.getConversionRatio((long)123)).isInstanceOf(ConversionRatio.class).isNotNull();
	}
	
	@Test 
	public void getConversionRatioByIdFailTest() {
		Mockito.when(query.getSingleResult()).thenThrow(new NoResultException());
		assertThat(dao.getConversionRatio((long)123)).isNull();
	}
	
	@Test
	public void getConversionRatioByUnitsIdTest() {
		Mockito.when(query.getSingleResult()).thenReturn(conversionRatio1);
		assertThat(dao.getConversionRatio(conversionRatio1.getFirst().getId(), conversionRatio1.getSecond().getId())).isInstanceOf(ConversionRatio.class).isNotNull();
	}
	
	@Test
	public void getConversionRatioByUnitsIdFailTest() {
		Mockito.when(query.getSingleResult()).thenThrow(new NoResultException());
		assertThat(dao.getConversionRatio(conversionRatio1.getFirst().getId(), conversionRatio1.getSecond().getId())).isNull();
	}
	
	@Test
	public void getAllConversionRatioTest() {
		ArrayList<ConversionRatio> list = new ArrayList<>();
		list.add(conversionRatio1);
		list.add(conversionRatio2);
		list.add(conversionRatio3);
		Mockito.when(query.getResultList()).thenReturn(list);
		assertThat(dao.getAll()).isInstanceOf(ArrayList.class).asList().contains(conversionRatio1,conversionRatio2,conversionRatio3);
	}
	
	@Test
	public void getConversionRatioByStartUnitIdTest() {
		ArrayList<ConversionRatio> list = new ArrayList<>();
		list.add(conversionRatio1);
		list.add(conversionRatio3);
		Mockito.when(query.getResultList()).thenReturn(list);
		assertThat(dao.getConversionRatioListByStartUnitId((long)123)).isInstanceOf(ArrayList.class).asList().contains(conversionRatio1,conversionRatio3);
	}
	
	@Test
	public void getConversionRatioByEndUnitIdTest() {
		ArrayList<ConversionRatio> list = new ArrayList<>();
		list.add(conversionRatio1);
		list.add(conversionRatio2);
		Mockito.when(query.getResultList()).thenReturn(list);
		assertThat(dao.getConversionRatioListByEndUnitId((long)123)).isInstanceOf(ArrayList.class).asList().contains(conversionRatio1,conversionRatio2);
	}
	

	
	
	
}
