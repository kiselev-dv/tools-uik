package me.osm.tools.uikrecheck.dao;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.ExistsSubqueryExpression;

import me.osm.tools.uikrecheck.bobjects.Revision;
import me.osm.tools.uikrecheck.bobjects.Uik;
import me.osm.tools.uikrecheck.hutil.SessionFactorySinglton;


public class UIKDao {
	
	public static Uik getById(int id) {
		return (Uik)SessionFactorySinglton.getFactory().getCurrentSession().get(Uik.class, id);
	}

	@SuppressWarnings("unchecked")
	public static List<Uik> listAll() {
		Criteria c = getBaseCriteria();
		return c.list();
	}

	@SuppressWarnings("unchecked")
	public static List<Uik> listUnreviwed() {
		
		Session session = SessionFactorySinglton.getFactory().getCurrentSession();
		Query q = session.createQuery("select uik from " + Uik.class.getName() + " uik "
				+ "where uik not in (select distinct rev.subject from " + Revision.class.getName() + " rev)");
		
		return q.list();
	}

	@SuppressWarnings("unchecked")
	public static Uik randomUnreviwed() {
		
		Session session = SessionFactorySinglton.getFactory().getCurrentSession();
		Query q = session.createQuery("select uik from " + Uik.class.getName() + " uik "
				+ "where uik not in (select distinct rev.subject from " + Revision.class.getName() + " rev) "
				+ "order by rand()");
		
		q.setMaxResults(1);
		q.setFetchSize(0);
		
		List list = q.list();
		
		return list.isEmpty() ? null : (Uik)list.get(0);
	}

	@SuppressWarnings("unchecked")
	public static int countUnreviwed() {
		
		Session session = SessionFactorySinglton.getFactory().getCurrentSession();
		
		Query q = session.createQuery("select count(*) from " + Uik.class.getName() + " uik "
				+ "where uik.id not in (select distinct rev.subject.id from " + Revision.class.getName() + " rev)");
		
		return ((Number)q.uniqueResult()).intValue();
	}

	public static int save(Uik subj) {
		return (Integer)SessionFactorySinglton.getFactory().getCurrentSession().save(subj);
	}

	public static void update(Uik subj) {
		SessionFactorySinglton.getFactory().getCurrentSession().merge(subj);
	}

	public static void delete(Uik subj) {
		SessionFactorySinglton.getFactory().getCurrentSession().delete(subj);
	}

	private static Criteria getBaseCriteria() {
		Session session = SessionFactorySinglton.getFactory().getCurrentSession();
		Criteria c = session.createCriteria(Uik.class, "uik");
		return c;
	}
	
}
