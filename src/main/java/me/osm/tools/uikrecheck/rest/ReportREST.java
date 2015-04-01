package me.osm.tools.uikrecheck.rest;

import java.util.List;

import me.osm.tools.uikrecheck.bobjects.Revision;
import me.osm.tools.uikrecheck.hutil.SessionFactorySinglton;

import org.hibernate.Query;
import org.hibernate.Transaction;
import org.json.JSONArray;
import org.json.JSONObject;
import org.restexpress.Request;
import org.restexpress.Response;

public class ReportREST {
	
	@SuppressWarnings("unchecked")
	public JSONObject read(Request request, Response response) {
		
		JSONObject result = new JSONObject();
		JSONArray reports = new JSONArray();
		result.put("reports", reports);
		
		Transaction trx = SessionFactorySinglton.getFactory().getCurrentSession().beginTransaction();
		try {
			StringBuilder queryString = new StringBuilder(); 
			
			queryString.append("select rev.subject.id, rev.subject.name, rev.action, rev.note ");
			queryString.append("from " + Revision.class.getName() + " rev ");
			
			String actionFilter = request.getHeader("action");
			if(actionFilter != null) {
				queryString.append("where action=:act ");
			}
			
			queryString.append("order by rev.subject.id ");
			
			Query q = SessionFactorySinglton.getFactory()
					.getCurrentSession().createQuery(queryString.toString());
			
			if(actionFilter != null) {
				q.setString("act", actionFilter);
			}
			
			for(Object[] row : (List<Object[]>)q.list()) {
				
				JSONObject revision = new JSONObject();
				
				revision.put("id", ((Number)row[0]).intValue());
				revision.put("name", (String)row[1]);
				revision.put("action", (String)row[2]);
				revision.put("comment", (String)row[3]);

				reports.put(revision);
			}
			
			return result;
		}
		finally {
			trx.commit();
		}
	}
}
