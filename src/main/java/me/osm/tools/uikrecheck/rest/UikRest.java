package me.osm.tools.uikrecheck.rest;

import java.util.Date;
import java.util.List;
import java.util.Map;

import me.osm.tools.uikrecheck.bobjects.Revision;
import me.osm.tools.uikrecheck.bobjects.Uik;
import me.osm.tools.uikrecheck.dao.UIKDao;
import me.osm.tools.uikrecheck.hutil.SessionFactorySinglton;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.Transaction;
import org.json.JSONObject;
import org.restexpress.Request;
import org.restexpress.Response;

public class UikRest {
	
	public JSONObject read(Request request, Response response) {

		Transaction trx = SessionFactorySinglton.getFactory().getCurrentSession().beginTransaction();
		try {
			Uik uik = null;
			if(request.getHeader("id") == null) {
				uik = UIKDao.randomUnreviwed();
			}
			else {
				uik = UIKDao.getById(Integer.parseInt(request.getHeader("id")));
			}
			
			if(uik != null) {
				JSONObject result = new JSONObject();
				
				result.put("name", uik.getName());
				result.put("id", uik.getId());
				result.put("lon", uik.getLon());
				result.put("lat", uik.getLat());
				
				result.put("_hits", UIKDao.countUnreviwed());
				
				return result; 
			}
			else {
				response.setResponseCode(404);
				return null;
			} 
		}
		finally {
			trx.commit();
		}
	}
	
	public void create(Request request, Response response) {
		Transaction trx = SessionFactorySinglton.getFactory().getCurrentSession().beginTransaction();
		try {
			int subj = Integer.parseInt(request.getHeader("id"));
			Map<String, List<String>> body = request.getBodyFromUrlFormEncoded();
			String action = optString(body.get("action"));
			String comment = optString(body.get("comment"));
			
			if(action != null) {
				Revision revision = new Revision();
				revision.setAction(action);
				revision.setNote(StringUtils.substring(comment, 0, 1000));
				revision.setRevisionDate(new Date());
				
				Uik subject = new Uik();
				subject.setId(subj);
				revision.setSubject(subject);
				
				SessionFactorySinglton.getFactory().getCurrentSession().save(revision);
			}
			trx.commit();
		}
		catch (Exception e){
			response.setResponseCode(500);
			throw new RuntimeException(e);
		}
		finally {
			if(!trx.wasCommitted()) {
				trx.rollback();
			}
		}
		
		response.setResponseCode(200);
		response.setBody("OK");
		response.setContentType("text");
	}

	private String optString(List<String> list) {
		if(list != null && !list.isEmpty()) {
			return list.get(0);
		}
		return null;
	}
	
}
