package service;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.transaction.Transactional;

import domainModel.Unit;

@ApplicationScoped
public class UnitDAO {
	@PersistenceContext
	private EntityManager em;
	
	@Transactional
	public void save(Unit unit) {
		em.persist(unit);
	}

	public List<Unit> getAll() {
		Query query = em.createQuery("FROM Unit");
		@SuppressWarnings("unchecked")
		List<Unit> list = query.getResultList();
		return list;
	}

	public Unit getUnit(Long id) {
		Query query = em.createQuery("FROM Unit WHERE id =:unit_id", Unit.class);
		query.setParameter("unit_id", id);
		Unit unit;
		try {
			unit = (Unit) query.getSingleResult();
		} catch (Exception e) {
			unit = null;
		}
		return unit;
	}
	
	public Unit getUnit(String name) {
		Query query = em.createQuery("FROM Unit WHERE name =:unit_name", Unit.class);
		query.setParameter("unit_name", name);
		Unit unit;
		try {
			unit = (Unit) query.getSingleResult();
		} catch (Exception e) {
			unit = null;
		}
		return unit;
	}

	@Transactional
	public void update(Long id, String name) {
		Unit unit = em.find(Unit.class, id);
		unit.setName(name);
	}

	@Transactional
	public void delete(Long id) {
		em.remove(em.find(Unit.class,id));
	}

	public void setEntityManager(EntityManager manager) {
		this.em=manager;
	}
}
