package service;

import java.util.List;

import javax.enterprise.context.Dependent;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.transaction.Transactional;

import domainModel.ConversionRatio;

@Dependent
public class ConversionRatioDAO {
	@PersistenceContext
	private EntityManager em;
	
	@Transactional
	public void save(ConversionRatio conversionRatio) {
		em.persist(conversionRatio);
	}

	public List<ConversionRatio> getAll() {
		Query query = em.createQuery("FROM ConversionRatio");
		@SuppressWarnings("unchecked")
		List<ConversionRatio> list = query.getResultList();
		return list;
	}

	public ConversionRatio getConversionRatio(Long id) {
		Query query = em.createQuery("FROM ConversionRatio WHERE id =:conversionRatio_id", ConversionRatio.class);
		query.setParameter("conversionRatio_id", id);
		ConversionRatio conversioRatio;
		try {
			conversioRatio = (ConversionRatio) query.getSingleResult();
		} catch (Exception e) {
			conversioRatio = null;
		}
		return 	conversioRatio;
	}
	
	public ConversionRatio getConversionRatio(Long startUnitId, Long endUnitId) {
		Query query = em.createQuery("FROM ConversionRatio WHERE first.id =:startUnitId and second.id = :endUnitId ", ConversionRatio.class);
		query.setParameter("startUnitId", startUnitId);
		query.setParameter("endUnitId", endUnitId);
		ConversionRatio conversioRatio;
		try {
			conversioRatio = (ConversionRatio) query.getSingleResult();
		} catch (Exception e) {
			conversioRatio = null;
		}
		return 	conversioRatio;
	}
	
	public List<ConversionRatio> getConversionRatioListByStartUnitId(Long startUnitId) {
		Query query = em.createQuery("FROM ConversionRatio WHERE first.id =:startunit_id");
		query.setParameter("startunit_id", startUnitId);
		@SuppressWarnings("unchecked")
		List<ConversionRatio> conversionRatio = query.getResultList();
		return conversionRatio;
	}
	
	public List<ConversionRatio> getConversionRatioListByEndUnitId(Long endUnitId) {
		Query query = em.createQuery("FROM ConversionRatio WHERE second.id =:endunit_id");
		query.setParameter("endunit_id", endUnitId);
		@SuppressWarnings("unchecked")
		List<ConversionRatio> conversionRatio = query.getResultList();
		return conversionRatio;
	}
	
	@Transactional
	public void delete(Long id) {
		em.remove(em.find(ConversionRatio.class,id));
	}

	public void setEntityManager(EntityManager manager) {
		this.em=manager;
	}
}