package unitTest.service;


import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.*;

import java.util.ArrayList;

import domainModel.Unit;
import service.UnitDAO;

@RunWith(MockitoJUnitRunner.class)
public class UnitDAOTest {

	@Mock
	EntityManager manager;

	@Mock
	TypedQuery<Unit> query;
	
	UnitDAO dao = new UnitDAO();
	
	Unit first;
	Unit second;
	Unit last;
	
	@Before
	public void setup() {
		first = new Unit("first");
		second = new Unit("second");
		last = new Unit("last");
		Mockito.when(manager.createQuery(Mockito.any(String.class), Mockito.eq(Unit.class))).thenReturn(query);
		Mockito.when(manager.createQuery(Mockito.any(String.class))).thenReturn(query);
	    Mockito.when(query.setParameter(Mockito.any(String.class), Mockito.any(String.class))).thenReturn(query);
	    dao.setEntityManager(manager);
	}
	
	@Test
	public void getUnitByIdTest() {
		Mockito.when(query.getSingleResult()).thenReturn(first);
		assertThat(dao.getUnit((long) 123)).isInstanceOf(Unit.class).isNotNull();
	}
	
	@Test
	public void getUnitByIdFailTest() {
		Mockito.when(query.getSingleResult()).thenThrow(new NoResultException());
		assertThat(dao.getUnit((long) 123)).isNull();
	}
	
	@Test
	public void getUnitByNameTest() {
		Mockito.when(query.getSingleResult()).thenReturn(first);
		assertThat(dao.getUnit(first.getName())).isInstanceOf(Unit.class).isNotNull();
	}
	
	@Test
	public void getUnitByNameFailTest() {
		Mockito.when(query.getSingleResult()).thenThrow(new NoResultException());
		assertThat(dao.getUnit(first.getName())).isNull();
	}
	
	
	@Test
	public void getAllUnitTest() {
		ArrayList<Unit> list = new ArrayList<>();
		list.add(first);
		list.add(second);
		list.add(last);
		Mockito.when(query.getResultList()).thenReturn(list);
		assertThat(dao.getAll()).isInstanceOf(ArrayList.class).asList().contains(first,second,last);
	}
}
